package com.expensetracker.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expensetracker.app.core.notification.NotificationHelper
import com.expensetracker.app.domain.repository.AiRepository
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class MonthlyInsightsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val aiRepository: AiRepository,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now()
            val lastMonthStart = today.withDayOfMonth(1).minusMonths(1)
            val lastMonthEnd = lastMonthStart.withDayOfMonth(lastMonthStart.lengthOfMonth())

            val transactions = transactionRepository
                .observeByDateRange(lastMonthStart, lastMonthEnd)
                .first()
            val categories = categoryRepository.observeAll().first()

            if (transactions.isEmpty()) return Result.success()

            val insights = aiRepository.generateMonthlyInsights(transactions, categories).getOrThrow()
            aiRepository.saveInsights(insights)
            notificationHelper.notifyAiInsights(insights.size)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "monthly_insights_worker"
    }
}

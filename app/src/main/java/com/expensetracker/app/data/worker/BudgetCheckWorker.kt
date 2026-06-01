package com.expensetracker.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.expensetracker.app.core.notification.NotificationHelper
import com.expensetracker.app.data.budget.BudgetAlertChecker
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class BudgetCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val budgetAlertChecker: BudgetAlertChecker,
    private val categoryRepository: CategoryRepository,
    private val preferencesRepository: PreferencesRepository,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = preferencesRepository.preferences.first()
            if (!prefs.budgetAlertsEnabled) return Result.success()

            val categoryId = inputData.getLong(KEY_CATEGORY_ID, -1L)
            if (categoryId == -1L) return Result.success()

            val categories = categoryRepository.observeAll().first().associateBy { it.id }
            val alerts = budgetAlertChecker.checkThresholdsForCategory(categoryId)

            for (alert in alerts) {
                val budget = alert.budget
                val categoryName = categories[budget.categoryId]?.name ?: "Overall"
                notificationHelper.notifyBudgetAlert(
                    budgetId = budget.id,
                    categoryName = categoryName,
                    spentPct = (alert.spentPercentage * 100).toInt(),
                    spentAmount = alert.spentAmount,
                    limitAmount = budget.amount,
                    currency = "VND",
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "budget_check"
        const val KEY_CATEGORY_ID = "category_id"

        fun inputDataFor(categoryId: Long): Data =
            Data.Builder().putLong(KEY_CATEGORY_ID, categoryId).build()
    }
}

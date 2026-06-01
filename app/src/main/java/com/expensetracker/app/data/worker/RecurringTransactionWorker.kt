package com.expensetracker.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expensetracker.app.core.notification.NotificationHelper
import com.expensetracker.app.domain.model.RecurrenceFrequency
import com.expensetracker.app.domain.model.RecurringTransaction
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.repository.RecurringTransactionRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalDateTime

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringRepo: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now()
            val dueSchedules = recurringRepo.getDue(today)
            val created = mutableListOf<String>()

            for (schedule in dueSchedules) {
                val count = processSchedule(schedule, today)
                if (count > 0) {
                    created.add("${schedule.note} (${schedule.amount.toLong()})")
                }
            }

            if (created.isNotEmpty()) {
                notificationHelper.notifyRecurringCreated(created.size, created)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun processSchedule(schedule: RecurringTransaction, today: LocalDate): Int {
        var current = schedule.nextOccurrence
        var count = 0

        while (current <= today && count < MAX_CATCH_UP) {
            val transaction = Transaction(
                id = 0L,
                walletId = schedule.walletId,
                categoryId = schedule.categoryId,
                amount = schedule.amount,
                type = schedule.type,
                note = schedule.note,
                date = current,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                photoUri = null,
                location = null,
                recurringId = schedule.id,
                parentSplitId = null,
                tags = emptyList(),
            )
            transactionRepository.add(transaction)
            current = computeNextOccurrence(current, schedule)
            count++
        }

        val isStillActive = schedule.endDate == null || current <= schedule.endDate
        recurringRepo.update(
            schedule.copy(
                nextOccurrence = current,
                lastProcessed = today,
                isActive = isStillActive,
            )
        )
        return count
    }

    private fun computeNextOccurrence(from: LocalDate, schedule: RecurringTransaction): LocalDate {
        val n = schedule.interval.toLong()
        return when (schedule.frequency) {
            RecurrenceFrequency.DAILY -> from.plusDays(n)
            RecurrenceFrequency.WEEKLY -> from.plusWeeks(n)
            RecurrenceFrequency.MONTHLY -> from.plusMonths(n)
            RecurrenceFrequency.YEARLY -> from.plusYears(n)
        }
    }

    companion object {
        const val WORK_NAME = "recurring_transactions"
        private const val MAX_CATCH_UP = 365
    }
}

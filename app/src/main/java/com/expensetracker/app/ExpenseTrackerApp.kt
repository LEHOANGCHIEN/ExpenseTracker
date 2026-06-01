package com.expensetracker.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.expensetracker.app.core.notification.NotificationHelper
import com.expensetracker.app.data.worker.DailyReminderWorker
import com.expensetracker.app.data.worker.MonthlyInsightsWorker
import com.expensetracker.app.data.worker.RecurringTransactionWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.LocalDateTime
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ExpenseTrackerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannels()
        scheduleRecurringWorker()
        scheduleMonthlyInsightsWorker()
        scheduleDailyReminderWorker()
    }

    private fun scheduleRecurringWorker() {
        val wm = WorkManager.getInstance(this)
        wm.enqueueUniquePeriodicWork(
            RecurringTransactionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RecurringTransactionWorker>(1, TimeUnit.DAYS).build(),
        )
        wm.enqueue(OneTimeWorkRequestBuilder<RecurringTransactionWorker>().build())
    }

    private fun scheduleMonthlyInsightsWorker() {
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MonthlyInsightsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<MonthlyInsightsWorker>(30, TimeUnit.DAYS).build(),
        )
    }

    private fun scheduleDailyReminderWorker() {
        // Schedule daily at 8 PM; calculate initial delay to next 8 PM
        val now = LocalDateTime.now()
        val target = now.withHour(20).withMinute(0).withSecond(0).withNano(0)
        val nextTarget = if (now.isBefore(target)) target else target.plusDays(1)
        val initialDelay = Duration.between(now, nextTarget).toMillis()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DailyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build(),
        )
    }
}

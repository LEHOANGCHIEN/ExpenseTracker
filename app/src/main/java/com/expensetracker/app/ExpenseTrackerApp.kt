package com.expensetracker.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.expensetracker.app.data.worker.RecurringTransactionWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ExpenseTrackerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleRecurringWorker()
    }

    private fun scheduleRecurringWorker() {
        val wm = WorkManager.getInstance(this)

        // Daily periodic check
        wm.enqueueUniquePeriodicWork(
            RecurringTransactionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RecurringTransactionWorker>(1, TimeUnit.DAYS).build(),
        )

        // One-time run on app start to catch missed schedules
        wm.enqueue(OneTimeWorkRequestBuilder<RecurringTransactionWorker>().build())
    }
}

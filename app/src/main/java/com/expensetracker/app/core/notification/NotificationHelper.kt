package com.expensetracker.app.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.expensetracker.app.MainActivity
import com.expensetracker.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_BUDGET_ALERTS = "budget_alerts"
        const val CHANNEL_DAILY_REMINDER = "daily_reminder"
        const val CHANNEL_RECURRING = "recurring"
        const val CHANNEL_AI_INSIGHTS = "ai_insights"

        private const val PREFS_NAME = "notification_prefs"
        private const val KEY_FIRED_ALERTS = "fired_alerts"
    }

    private val manager = context.getSystemService(NotificationManager::class.java)
    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    // ---- Channel setup ----

    fun createChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                context.getString(R.string.notification_channel_budget),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Alerts when you approach or exceed a budget limit" },

            NotificationChannel(
                CHANNEL_DAILY_REMINDER,
                context.getString(R.string.notification_channel_reminder),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Reminder to log your daily expenses" },

            NotificationChannel(
                CHANNEL_RECURRING,
                context.getString(R.string.notification_channel_recurring),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Notifies when recurring transactions are auto-created" },

            NotificationChannel(
                CHANNEL_AI_INSIGHTS,
                context.getString(R.string.notification_channel_insights),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Notifies when new AI spending insights are available" },
        )
        channels.forEach { manager.createNotificationChannel(it) }
    }

    // ---- Budget alert ----

    fun notifyBudgetAlert(
        budgetId: Long,
        categoryName: String,
        spentPct: Int,
        spentAmount: Double,
        limitAmount: Double,
        currency: String,
    ) {
        // Deduplicate: only fire once per threshold bracket per period
        val thresholdBracket = when {
            spentPct >= 100 -> 100
            spentPct >= 90 -> 90
            spentPct >= 80 -> 80
            else -> return // below 80%, no alert
        }
        val periodKey = YearMonth.now().toString()
        val alertKey = "b_${budgetId}_${thresholdBracket}_$periodKey"
        val firedAlerts = prefs.getStringSet(KEY_FIRED_ALERTS, emptySet()) ?: emptySet()
        if (alertKey in firedAlerts) return
        prefs.edit().putStringSet(KEY_FIRED_ALERTS, firedAlerts + alertKey).apply()

        if (!hasPermission()) return

        val message = when {
            spentPct >= 100 -> "You've exceeded your $categoryName budget! (${spentPct}% used)"
            else -> "You've used $spentPct% of your $categoryName budget"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Budget Alert: $categoryName")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent())
            .build()

        NotificationManagerCompat.from(context).notify(
            (CHANNEL_BUDGET_ALERTS + budgetId).hashCode(),
            notification,
        )
    }

    // ---- Daily reminder ----

    fun notifyDailyReminder() {
        if (!hasPermission()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_reminder_title))
            .setContentText(context.getString(R.string.notification_reminder_message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent())
            .build()

        NotificationManagerCompat.from(context).notify(
            CHANNEL_DAILY_REMINDER.hashCode(),
            notification,
        )
    }

    // ---- Recurring transactions ----

    fun notifyRecurringCreated(count: Int, descriptions: List<String>) {
        if (!hasPermission() || count == 0) return
        val body = when {
            count == 1 -> "Created: ${descriptions.firstOrNull() ?: "1 transaction"}"
            else -> "$count recurring transactions were auto-created"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_RECURRING)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle(context.getString(R.string.notification_recurring_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(descriptions.joinToString("\n")))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent())
            .build()

        NotificationManagerCompat.from(context).notify(
            CHANNEL_RECURRING.hashCode(),
            notification,
        )
    }

    // ---- AI Insights ----

    fun notifyAiInsights(count: Int) {
        if (!hasPermission() || count == 0) return
        val notification = NotificationCompat.Builder(context, CHANNEL_AI_INSIGHTS)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(context.getString(R.string.notification_insights_title))
            .setContentText("$count new spending insight${if (count > 1) "s are" else " is"} ready")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent())
            .build()

        NotificationManagerCompat.from(context).notify(
            CHANNEL_AI_INSIGHTS.hashCode(),
            notification,
        )
    }

    // ---- Helpers ----

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun mainPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

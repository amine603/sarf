package com.cash.guide.domain.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object CreditReminderScheduler {

    const val EXTRA_CALCULATION_ID = "extra_calculation_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_AMOUNT_FORMATTED = "extra_amount_formatted"
    const val ACTION_MARK_PAID = "com.cash.guide.ACTION_MARK_PAID"

    fun scheduleReminder(
        context: Context,
        calculationId: String,
        title: String,
        amountFormatted: String,
        reminderTimeEpochMs: Long
    ) {
        if (reminderTimeEpochMs <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, CreditReminderReceiver::class.java).apply {
            putExtra(EXTRA_CALCULATION_ID, calculationId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_AMOUNT_FORMATTED, amountFormatted)
        }

        val requestCode = calculationId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimeEpochMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimeEpochMs,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeEpochMs,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeEpochMs,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for devices restricting exact alarms
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTimeEpochMs,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, calculationId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, CreditReminderReceiver::class.java)
        val requestCode = calculationId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        // Also dismiss any active notification for this calculation
        NotificationManagerCompat.from(context).cancel(requestCode)
    }
}

package com.cash.guide.domain.reminder

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.cash.guide.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class CreditDueUrgency {
    SETTLED,
    OVERDUE,
    DUE_TODAY,
    DUE_TOMORROW,
    UPCOMING,
    NONE
}

data class CreditDueInfo(
    val urgency: CreditDueUrgency,
    val displayText: String,
    val badgeColor: Color,
    val textColor: Color,
    val hasReminder: Boolean = false,
    val daysRemaining: Int = 0
)

object CreditStatusHelper {

    fun computeDueInfo(
        context: Context,
        paymentStatus: String,
        calcType: String,
        dueDateEpochMs: Long?,
        reminderEnabled: Boolean,
        nowEpochMs: Long = System.currentTimeMillis()
    ): CreditDueInfo? {
        if (calcType != "CREDIT") return null

        if (paymentStatus == "PAID") {
            return CreditDueInfo(
                urgency = CreditDueUrgency.SETTLED,
                displayText = context.getString(R.string.credit_stamp_settled),
                badgeColor = Color(0xFFDCFCE7), // Soft mint green
                textColor = Color(0xFF16A34A),  // Fresh green ink
                hasReminder = false,
                daysRemaining = 0
            )
        }

        if (dueDateEpochMs == null) {
            return null
        }

        val days = getDaysDifference(targetEpochMs = dueDateEpochMs, nowEpochMs = nowEpochMs)
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val formattedDate = dateFormat.format(java.util.Date(dueDateEpochMs))

        return when {
            days < 0 -> {
                val overdueDays = kotlin.math.abs(days)
                CreditDueInfo(
                    urgency = CreditDueUrgency.OVERDUE,
                    displayText = if (overdueDays == 1) {
                        context.getString(R.string.credit_stamp_overdue_today)
                    } else {
                        context.getString(R.string.credit_stamp_overdue, overdueDays)
                    },
                    badgeColor = Color(0xFFFEE2E2), // Soft pink-red
                    textColor = Color(0xFFDC2626),  // Vivid red ink
                    hasReminder = reminderEnabled,
                    daysRemaining = days
                )
            }
            days == 0 -> {
                CreditDueInfo(
                    urgency = CreditDueUrgency.DUE_TODAY,
                    displayText = context.getString(R.string.credit_stamp_today),
                    badgeColor = Color(0xFFFFEDD5), // Soft peach
                    textColor = Color(0xFFEA580C),  // Terracotta ink
                    hasReminder = reminderEnabled,
                    daysRemaining = 0
                )
            }
            days == 1 -> {
                CreditDueInfo(
                    urgency = CreditDueUrgency.DUE_TOMORROW,
                    displayText = context.getString(R.string.credit_stamp_tomorrow),
                    badgeColor = Color(0xFFFEF3C7), // Soft yellow
                    textColor = Color(0xFFD97706),  // Amber ink
                    hasReminder = reminderEnabled,
                    daysRemaining = 1
                )
            }
            else -> {
                CreditDueInfo(
                    urgency = CreditDueUrgency.UPCOMING,
                    displayText = context.getString(R.string.credit_stamp_upcoming, days, formattedDate),
                    badgeColor = Color(0xFFF3F4F6), // Soft grey paper
                    textColor = Color(0xFF4B5563),  // Graphite ink
                    hasReminder = reminderEnabled,
                    daysRemaining = days
                )
            }
        }
    }

    /**
     * Computes difference in calendar days between target date and now.
     * Negative means target is in the past (overdue).
     * 0 means today.
     * Positive means in the future.
     */
    fun getDaysDifference(targetEpochMs: Long, nowEpochMs: Long = System.currentTimeMillis()): Int {
        val cTarget = Calendar.getInstance().apply {
            timeInMillis = targetEpochMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cNow = Calendar.getInstance().apply {
            timeInMillis = nowEpochMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = cTarget.timeInMillis - cNow.timeInMillis
        return (diffMs / (24 * 60 * 60 * 1000L)).toInt()
    }
}

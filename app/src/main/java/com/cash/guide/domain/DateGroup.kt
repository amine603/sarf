package com.cash.guide.domain

import android.content.Context
import com.cash.guide.R
import com.cash.guide.data.db.CalculationWithItems
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CalculationDateGroup(
    val header: String,
    val calculations: List<CalculationWithItems>
)

object DateGroupHelper {

    fun groupByDate(
        items: List<CalculationWithItems>,
        todayString: String,
        yesterdayString: String,
        thisWeekString: String,
        nowEpochMs: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): List<CalculationDateGroup> {
        if (items.isEmpty()) return emptyList()

        val today = Instant.ofEpochMilli(nowEpochMs).atZone(zoneId).toLocalDate()
        val yesterday = today.minusDays(1)
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)

        val currentYear = today.year
        val monthFormatter = DateTimeFormatter.ofPattern("MMMM", locale)
        val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)

        // LinkedHashMap to preserve order of groups based on most recent calculations
        val grouped = LinkedHashMap<String, MutableList<CalculationWithItems>>()

        val sortedItems = items.sortedByDescending { it.calculation.updatedAtEpochMs }

        sortedItems.forEach { item ->
            val itemDate = Instant.ofEpochMilli(item.calculation.updatedAtEpochMs).atZone(zoneId).toLocalDate()
            val header = when {
                itemDate.isEqual(today) -> todayString
                itemDate.isEqual(yesterday) -> yesterdayString
                !itemDate.isBefore(startOfWeek) -> thisWeekString
                itemDate.year == currentYear -> itemDate.format(monthFormatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                else -> itemDate.format(monthYearFormatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            }
            grouped.getOrPut(header) { mutableListOf() }.add(item)
        }

        return grouped.map { (header, list) ->
            CalculationDateGroup(header = header, calculations = list)
        }
    }

    fun formatHomeCalculationSubtitle(
        epochMs: Long,
        locale: Locale
    ): String {
        val zoneId = ZoneId.systemDefault()
        val now = LocalDate.now(zoneId)
        val itemDate = Instant.ofEpochMilli(epochMs).atZone(zoneId).toLocalDate()

        return if (itemDate.isEqual(now) || itemDate.isEqual(now.minusDays(1))) {
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
            Instant.ofEpochMilli(epochMs).atZone(zoneId).format(timeFormatter)
        } else {
            val dateFormatter = DateTimeFormatter.ofPattern("d MMMM", locale)
            itemDate.format(dateFormatter)
        }
    }

    fun formatHistoryCalculationSubtitle(
        calc: CalculationWithItems,
        context: Context
    ): String {
        val currency = runCatching { MoneyUnit.valueOf(calc.calculation.currency) }.getOrDefault(MoneyUnit.DIRHAM)
        val currencySuffix = if (currency == MoneyUnit.DIRHAM) {
            context.getString(R.string.currency_dirham)
        } else {
            context.getString(R.string.currency_rial)
        }

        if (!calc.calculation.note.isNullOrBlank()) {
            return calc.calculation.note!!
        }

        val labeledItems = calc.items.filter { it.label.isNotBlank() }
        if (labeledItems.isNotEmpty()) {
            val first = labeledItems[0]
            return if (calc.items.size == 1 || calc.calculation.title.equals("Calcul", ignoreCase = true) || calc.calculation.title.equals("حساب", ignoreCase = true)) {
                val amountFormatted = JournalLedgerManager.formatTotal(first.amountCentimes, currency)
                "${first.label} — $amountFormatted $currencySuffix"
            } else {
                first.label
            }
        }

        if (calc.items.isNotEmpty()) {
            return context.getString(R.string.calculation_lines_count, calc.items.size)
        }

        val locale = context.resources.configuration.locales[0]
        return formatHomeCalculationSubtitle(calc.calculation.updatedAtEpochMs, locale)
    }
}

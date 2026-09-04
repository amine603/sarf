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
}

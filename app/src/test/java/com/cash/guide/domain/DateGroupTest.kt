package com.cash.guide.domain

import com.cash.guide.data.db.CalculationEntity
import com.cash.guide.data.db.CalculationWithItems
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class DateGroupTest {

    private val zoneId = ZoneId.of("UTC")
    // Reference date: Wednesday, September 2, 2026
    private val nowEpoch = LocalDate.of(2026, 9, 2).atStartOfDay(zoneId).toInstant().toEpochMilli() + 3600000L

    @Test
    fun groupByDate_groupsCorrectlyIntoTodayYesterdayAndWeek() {
        val todayCalc = CalculationWithItems(
            CalculationEntity("1", "Today Calc", "DIRHAM", nowEpoch, nowEpoch, "SAVED"),
            emptyList()
        )
        val yesterdayEpoch = LocalDate.of(2026, 9, 1).atStartOfDay(zoneId).toInstant().toEpochMilli() + 3600000L
        val yesterdayCalc = CalculationWithItems(
            CalculationEntity("2", "Yesterday Calc", "DIRHAM", yesterdayEpoch, yesterdayEpoch, "SAVED"),
            emptyList()
        )
        val mondayEpoch = LocalDate.of(2026, 8, 31).atStartOfDay(zoneId).toInstant().toEpochMilli() + 3600000L
        val weekCalc = CalculationWithItems(
            CalculationEntity("3", "Monday Calc", "DIRHAM", mondayEpoch, mondayEpoch, "SAVED"),
            emptyList()
        )

        val groups = DateGroupHelper.groupByDate(
            items = listOf(todayCalc, yesterdayCalc, weekCalc),
            todayString = "Aujourd’hui",
            yesterdayString = "Hier",
            thisWeekString = "Cette semaine",
            nowEpochMs = nowEpoch,
            zoneId = zoneId,
            locale = Locale.FRENCH
        )

        assertEquals(3, groups.size)
        assertEquals("Aujourd’hui", groups[0].header)
        assertEquals(1, groups[0].calculations.size)
        assertEquals("Hier", groups[1].header)
        assertEquals(1, groups[1].calculations.size)
        assertEquals("Cette semaine", groups[2].header)
        assertEquals(1, groups[2].calculations.size)
    }
}

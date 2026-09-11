package com.cash.guide.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.DateGroupHelper
import com.cash.guide.domain.JournalLedgerManager
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.notebook.HisabiMetrics
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.JournalDateRuleBand
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.NotebookCalculationRow
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.journalVisualOnRule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MonthCalculationsScreen(
    year: Int,
    month: Int, // 1-12
    repository: CalculationRepository,
    onOpenCalculation: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    val allCalculations by repository.observeAllSaved().collectAsState(initial = emptyList())

    val locale = LocalConfiguration.current.locales[0]
    val monthTitle = remember(year, month, locale) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, (month - 1).coerceIn(0, 11))
        val monthName = SimpleDateFormat("MMMM", locale).format(cal.time).replaceFirstChar { it.uppercase() }
        "$monthName $year"
    }

    // Filter calculations for this year and month
    val monthCalculations = remember(allCalculations, year, month) {
        val cal = Calendar.getInstance()
        allCalculations.filter { calc ->
            cal.timeInMillis = calc.calculation.updatedAtEpochMs
            cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month
        }.sortedByDescending { it.calculation.updatedAtEpochMs }
    }

    // Group calculations by day of month
    val dayGroups = remember(monthCalculations, locale) {
        val cal = Calendar.getInstance()
        val monthFmt = SimpleDateFormat("MMMM", locale)
        monthCalculations.groupBy { calc ->
            cal.timeInMillis = calc.calculation.updatedAtEpochMs
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val monthName = monthFmt.format(cal.time).replaceFirstChar { it.uppercase() }
            "$day $monthName"
        }
    }

    // Total for this month
    val totalCentimes = remember(monthCalculations) {
        monthCalculations.sumOf { it.totalCentimes }
    }
    val totalFormatted = remember(totalCentimes) {
        JournalLedgerManager.formatTotal(totalCentimes, MoneyUnit.DIRHAM)
    }

    JournalRuledDocument(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        // Line 1: Header with Back button + Month Title sitting directly on ruled line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(JournalRuleSpacing)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(JournalRuleSpacing)
                    .clickable(role = Role.Button, onClick = onNavigateBack),
                contentAlignment = Alignment.BottomCenter
            ) {
                HisabiSketchIcon(
                    symbol = HisabiSymbol.Back,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = JournalInk,
                    size = 20.dp,
                    modifier = Modifier.journalVisualOnRule(gapAboveRule = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = monthTitle,
                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                fontSize = if (isRtl) 16.5.sp else 17.sp,
                fontWeight = FontWeight.Bold,
                color = JournalInk,
                style = TextStyle(platformStyle = NoFontPadding),
                modifier = Modifier.journalBaselineOnRule()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Month summary on the right
            Text(
                text = "$totalFormatted ${stringResource(R.string.currency_dirham)}",
                fontFamily = PatrickHandFamily,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = JournalInk,
                style = TextStyle(platformStyle = NoFontPadding),
                modifier = Modifier.journalBaselineOnRule()
            )
        }

        // 1 empty notebook line
        Spacer(modifier = Modifier.height(JournalRuleSpacing))

        if (monthCalculations.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.history_month_empty, monthTitle),
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 14.5.sp else 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = JournalMutedInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule()
                )
            }
        } else {
            // Display calculations grouped by day with full date and exact time
            val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

            dayGroups.forEach { (dayHeader, calcs) ->
                JournalDateRuleBand(title = dayHeader)

                calcs.forEachIndexed { idx, calc ->
                    val currency = runCatching { MoneyUnit.valueOf(calc.calculation.currency) }.getOrDefault(MoneyUnit.DIRHAM)
                    val calcTotal = JournalLedgerManager.formatTotal(calc.totalCentimes, currency)
                    val currencySuffix = if (currency == MoneyUnit.DIRHAM) {
                        stringResource(R.string.currency_dirham)
                    } else {
                        stringResource(R.string.currency_rial)
                    }

                    NotebookCalculationRow(
                        index = idx,
                        title = calc.calculation.title,
                        totalAmount = calcTotal,
                        currencySuffix = currencySuffix,
                        isPinned = false,
                        paymentStatus = calc.calculation.paymentStatus,
                        calcType = calc.calculation.calcType,
                        dueDateEpochMs = calc.calculation.dueDateEpochMs,
                        reminderEnabled = calc.calculation.reminderEnabled,
                        onClick = { onOpenCalculation(calc.calculation.id) },
                        onMoreClick = {}
                    )
                }

                Spacer(modifier = Modifier.height(JournalRuleSpacing))
            }
        }

        Spacer(modifier = Modifier.height(JournalRuleSpacing * 4))
    }
}

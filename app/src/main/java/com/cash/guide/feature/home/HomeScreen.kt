package com.cash.guide.feature.home

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.JournalLedgerManager
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.notebook.DeleteConfirmationDialog
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.JournalBaselineHighlightedText
import com.cash.guide.ui.notebook.JournalCalculationRow
import com.cash.guide.ui.notebook.JournalDateRuleBand
import com.cash.guide.ui.notebook.getDateTimelineStyle
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.JournalNewCalculationButton
import com.cash.guide.ui.notebook.JournalRecentHeader
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.SavedCalculationActionsSheet
import com.cash.guide.ui.notebook.TajawalFamily

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.cash.guide.app.LocalizedContextWrapper
import com.cash.guide.domain.DateGroupHelper
import com.cash.guide.ui.notebook.JournalPrimaryActionButton
import com.cash.guide.ui.notebook.JournalFloatingActionButton
import com.cash.guide.ui.notebook.JournalInlineSearchRow
import com.cash.guide.ui.notebook.JournalFavoritesHeader
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.cash.guide.ui.notebook.MonthPickerDialog
import java.text.SimpleDateFormat
import java.util.Date

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is LocalizedContextWrapper -> originalActivity ?: baseContext.findActivity()
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNewCalculation: () -> Unit,
    onOpenCalculation: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMonthCalculations: (year: Int, month: Int) -> Unit = { _, _ -> },
    onOpenStyleShowcase: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    var showMonthPicker by remember { mutableStateOf(false) }
    val currentMonthYear = remember(context) {
        val locale = context.resources.configuration.locales[0]
        SimpleDateFormat("MMMM yyyy", locale).format(Date()).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(locale) else it.toString()
        }
    }

    LaunchedEffect(context) {
        viewModel.loadRecent(context)
    }

    Box(modifier = modifier.fillMaxSize()) {
        JournalRuledDocument(modifier = Modifier.fillMaxSize()) {
            // Line 1: Greeting Band ("Bonjour Youssef :)" / "صباح الخير يوسف :)" + Month Year sitting directly on the ruled line)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isRtl) {
                        Text(
                            text = "صباح الخير ",
                            fontFamily = TajawalFamily,
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 6.0.dp)
                        )
                        Text(
                            text = "يوسف",
                            fontFamily = TajawalFamily,
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 6.0.dp)
                        )
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Smile,
                            contentDescription = null,
                            tint = JournalInk,
                            size = 18.dp,
                            modifier = Modifier.offset(y = 1.0.dp)
                        )
                    } else {
                        Text(
                            text = "Bonjour ",
                            fontFamily = PatrickHandFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 5.5.dp)
                        )
                        Text(
                            text = "Youssef",
                            fontFamily = PatrickHandFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 5.5.dp)
                        )
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Smile,
                            contentDescription = null,
                            tint = JournalInk,
                            size = 18.dp,
                            modifier = Modifier.offset(y = 1.0.dp)
                        )
                    }
                }

                Text(
                    text = currentMonthYear,
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 14.5.sp else 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = JournalMutedInk.copy(alpha = 0.80f),
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                )
            }

            // Skip 1 ruled line ("na99ez star o dir search")
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Prominent Ruled-Line Search Row with Calendar icon popup
            JournalInlineSearchRow(
                query = state.searchQuery,
                onQueryChange = { query -> viewModel.updateSearchQuery(query) },
                onOpenCalendar = { showMonthPicker = true },
                isDateFiltered = state.selectedDateEpoch != null,
                showUnderline = false
            )

            // Skip 1 ruled line under search ("tale3 vos favoris lstar li lfo9")
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Section: Vos favoris ("o 9bel vos calculs dir vos favourites o hna ykouno l7issabat matalan li dayr lihom pin")
            if (!state.isEmpty) {
                if (state.favoriteCalculations.isNotEmpty() || !state.isFiltering) {
                    JournalFavoritesHeader()

                    if (state.favoriteCalculations.isNotEmpty()) {
                        state.favoriteCalculations.forEachIndexed { idx, calc ->
                            val currency = runCatching { MoneyUnit.valueOf(calc.calculation.currency) }.getOrDefault(MoneyUnit.DIRHAM)
                            val totalFormatted = JournalLedgerManager.formatTotal(calc.totalCentimes, currency)
                            val currencySuffix = if (currency == MoneyUnit.DIRHAM) {
                                stringResource(R.string.currency_dirham)
                            } else {
                                stringResource(R.string.currency_rial)
                            }

                            JournalCalculationRow(
                                index = idx,
                                title = calc.calculation.title,
                                totalAmount = totalFormatted,
                                currencySuffix = currencySuffix,
                                onClick = { onOpenCalculation(calc.calculation.id) },
                                onMoreClick = { viewModel.selectCalculationForAction(calc) }
                            )
                        }

                        // 1 empty notebook line after favorites before "Vos calculs"
                        Spacer(modifier = Modifier.height(JournalRuleSpacing))
                    } else {
                        // Subtle hint row sitting directly on the ruled line
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(JournalRuleSpacing)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = stringResource(R.string.home_favorites_empty_hint),
                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                fontSize = if (isRtl) 14.sp else 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = JournalMutedInk.copy(alpha = 0.65f),
                                style = TextStyle(platformStyle = NoFontPadding),
                                modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                            )
                        }

                        // 1 empty notebook line before "Vos calculs"
                        Spacer(modifier = Modifier.height(JournalRuleSpacing))
                    }
                }

                // Section header ("VOS CALCULS" / "حساباتك") with edge-to-edge flush highlight
                JournalRecentHeader()

                // Date-grouped saved calculations (directly under each other without blank lines between items)
                state.displayDateGroups.forEachIndexed { groupIndex, group ->
                    JournalDateRuleBand(title = group.header)

                    val todayText = stringResource(R.string.date_today)
                    val yesterdayText = stringResource(R.string.date_yesterday)
                    val timelineStyle = getDateTimelineStyle(group.header, todayText, yesterdayText)

                    group.calculations.forEachIndexed { idx, calc ->
                        val currency = runCatching { MoneyUnit.valueOf(calc.calculation.currency) }.getOrDefault(MoneyUnit.DIRHAM)
                        val totalFormatted = JournalLedgerManager.formatTotal(calc.totalCentimes, currency)
                        val currencySuffix = if (currency == MoneyUnit.DIRHAM) {
                            stringResource(R.string.currency_dirham)
                        } else {
                            stringResource(R.string.currency_rial)
                        }

                        JournalCalculationRow(
                            index = idx,
                            title = calc.calculation.title,
                            totalAmount = totalFormatted,
                            currencySuffix = currencySuffix,
                            onClick = { onOpenCalculation(calc.calculation.id) },
                            onMoreClick = { viewModel.selectCalculationForAction(calc) },
                            dotColorOverride = timelineStyle.dotColor
                        )
                    }

                    // 1 empty notebook line between date groups
                    if (groupIndex < state.displayDateGroups.lastIndex) {
                        Spacer(modifier = Modifier.height(JournalRuleSpacing))
                    }
                }

                // "Voir tout" under the content on the right (with 1 skipped line before it)
                if (state.displayDateGroups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(JournalRuleSpacing))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JournalRuleSpacing)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = if (isRtl) Arrangement.Start else Arrangement.End
                    ) {
                        Text(
                            text = if (isRtl) "← ${stringResource(R.string.home_see_all)}" else "${stringResource(R.string.home_see_all)} →",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = if (isRtl) 15.sp else 16.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JournalWritingInk.copy(alpha = 0.85f),
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier
                                .clickable(role = Role.Button, onClick = onOpenHistory)
                                .offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                        )
                    }
                }
            } else if (!state.isLoading) {
                // Empty state sitting directly on the ruled line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (state.isFiltering) {
                            stringResource(R.string.home_no_results_for_date)
                        } else {
                            stringResource(R.string.home_empty_title)
                        },
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                    )
                }
            }

            // Bottom Spacers: 6 notebook lines for full scrolling clearance above dock and FAB
            Spacer(modifier = Modifier.height(JournalRuleSpacing * 6))
        }

        // Action Sheet
        val actionCalc = state.selectedCalculationForAction
        if (actionCalc != null) {
            val isPinned = actionCalc.calculation.id in state.pinnedCalculationIds
            SavedCalculationActionsSheet(
                calculationTitle = actionCalc.calculation.title,
                isPinned = isPinned,
                onTogglePin = {
                    viewModel.togglePin(actionCalc.calculation.id)
                },
                onEdit = {
                    onOpenCalculation(actionCalc.calculation.id)
                    viewModel.selectCalculationForAction(null)
                },
                onDuplicate = {
                    viewModel.duplicateCalculation(actionCalc) { newId ->
                        onOpenCalculation(newId)
                    }
                    viewModel.selectCalculationForAction(null)
                },
                onDelete = {
                    viewModel.requestDelete(actionCalc)
                },
                onDismiss = {
                    viewModel.selectCalculationForAction(null)
                }
            )
        }

        // Delete Confirmation Dialog
        if (state.calculationToDelete != null) {
            DeleteConfirmationDialog(
                onConfirmDelete = { viewModel.confirmDelete() },
                onDismiss = { viewModel.dismissDeleteDialog() }
            )
        }

        // Month Picker Dialog Popup when calendar icon is clicked
        if (showMonthPicker) {
            val cal = remember { java.util.Calendar.getInstance() }
            MonthPickerDialog(
                initialYear = cal.get(java.util.Calendar.YEAR),
                initialMonth = cal.get(java.util.Calendar.MONTH) + 1,
                onDismiss = { showMonthPicker = false },
                onSelectMonth = { year, month ->
                    showMonthPicker = false
                    onOpenMonthCalculations(year, month)
                }
            )
        }

        // Floating Action Button at the bottom-right ("+ Nouveau calcul" / "+ حساب جديد")
        // Anchored strictly to the physical right ("3la limen") in both French (LTR) and Arabic (RTL)
        JournalFloatingActionButton(
            onClick = onNewCalculation,
            modifier = Modifier
                .align(if (isRtl) Alignment.BottomStart else Alignment.BottomEnd)
                .padding(
                    start = if (isRtl) 18.dp else 0.dp,
                    end = if (isRtl) 0.dp else 18.dp,
                    bottom = 18.dp
                )
        )
    }
}


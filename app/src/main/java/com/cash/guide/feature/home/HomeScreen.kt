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
import com.cash.guide.ui.notebook.JournalTwoLineCalculationRow
import com.cash.guide.ui.notebook.JournalFavoritesHeader

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
    onOpenStyleShowcase: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    LaunchedEffect(context) {
        viewModel.loadRecent(context)
    }

    Box(modifier = modifier.fillMaxSize()) {
        JournalRuledDocument(modifier = Modifier.fillMaxSize()) {
            // Line 1: Greeting Band ("Bonjour Youssef" / "مرحباً يوسف" sitting directly on the ruled line)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.home_greeting),
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 18.5.sp else 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.offset(y = if (isRtl) 5.7.dp else 2.5.dp)
                )
            }

            // Line 2: Prominent Ruled-Line Search Row with attached thin line and Calendar icon
            JournalInlineSearchRow(
                query = state.searchQuery,
                onQueryChange = { query -> viewModel.updateSearchQuery(query) },
                onOpenCalendar = {
                    val activity = context.findActivity() ?: return@JournalInlineSearchRow
                    val cal = java.util.Calendar.getInstance()
                    state.selectedDateEpoch?.let { cal.timeInMillis = it }
                    android.app.DatePickerDialog(
                        activity,
                        { _, year, month, dayOfMonth ->
                            val picked = java.util.Calendar.getInstance().apply {
                                set(java.util.Calendar.YEAR, year)
                                set(java.util.Calendar.MONTH, month)
                                set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                set(java.util.Calendar.HOUR_OF_DAY, 12)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                            }
                            viewModel.filterByDate(picked.timeInMillis)
                        },
                        cal.get(java.util.Calendar.YEAR),
                        cal.get(java.util.Calendar.MONTH),
                        cal.get(java.util.Calendar.DAY_OF_MONTH)
                    ).show()
                },
                isDateFiltered = state.selectedDateEpoch != null,
                showUnderline = true
            )

            // Lines 3 & 4: Skip 2 ruled lines ("na9ezz 2 stoura ta7t search")
            Spacer(modifier = Modifier.height(JournalRuleSpacing * 2))

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
                            val subtitle = DateGroupHelper.formatHomeCalculationSubtitle(
                                epochMs = calc.calculation.updatedAtEpochMs,
                                locale = context.resources.configuration.locales[0]
                            )

                            JournalTwoLineCalculationRow(
                                index = idx,
                                title = calc.calculation.title,
                                subtitle = subtitle,
                                totalAmount = totalFormatted,
                                currencySuffix = currencySuffix,
                                isPinned = false,
                                onClick = { onOpenCalculation(calc.calculation.id) },
                                onMoreClick = { viewModel.selectCalculationForAction(calc) }
                            )
                        }
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
                                fontSize = if (isRtl) 13.5.sp else 14.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = JournalMutedInk.copy(alpha = 0.65f),
                                style = TextStyle(platformStyle = NoFontPadding),
                                modifier = Modifier.offset(y = if (isRtl) 5.7.dp else 2.5.dp)
                            )
                        }
                    }

                    // 1 empty notebook line before "Vos calculs"
                    Spacer(modifier = Modifier.height(JournalRuleSpacing))
                }

                // Section header ("VOS CALCULS" / "حساباتك") with edge-to-edge flush highlight
                JournalRecentHeader()

                // Skip 1 line before Aujourd'hui ("na9ez star 3ad dir aujourduit")
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Date-grouped saved calculations (each calculation is exactly 2 ruled lines = 58dp, zero cards)
                state.displayDateGroups.forEachIndexed { groupIndex, group ->
                    JournalDateRuleBand(title = group.header)

                    group.calculations.forEachIndexed { idx, calc ->
                        val currency = runCatching { MoneyUnit.valueOf(calc.calculation.currency) }.getOrDefault(MoneyUnit.DIRHAM)
                        val totalFormatted = JournalLedgerManager.formatTotal(calc.totalCentimes, currency)
                        val currencySuffix = if (currency == MoneyUnit.DIRHAM) {
                            stringResource(R.string.currency_dirham)
                        } else {
                            stringResource(R.string.currency_rial)
                        }
                        val subtitle = DateGroupHelper.formatHomeCalculationSubtitle(
                            epochMs = calc.calculation.updatedAtEpochMs,
                            locale = context.resources.configuration.locales[0]
                        )

                        JournalTwoLineCalculationRow(
                            index = idx,
                            title = calc.calculation.title,
                            subtitle = subtitle,
                            totalAmount = totalFormatted,
                            currencySuffix = currencySuffix,
                            isPinned = false,
                            onClick = { onOpenCalculation(calc.calculation.id) },
                            onMoreClick = { viewModel.selectCalculationForAction(calc) }
                        )
                    }

                    // 1 empty notebook line between date groups
                    if (groupIndex < state.displayDateGroups.lastIndex) {
                        Spacer(modifier = Modifier.height(JournalRuleSpacing))
                    }
                }

                // "Voir tout" under the content on the right
                if (state.displayDateGroups.isNotEmpty()) {
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
                                .offset(y = if (isRtl) 5.7.dp else 2.5.dp)
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
                        modifier = Modifier.offset(y = if (isRtl) 5.7.dp else 2.5.dp)
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


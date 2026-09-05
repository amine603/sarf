package com.cash.guide.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
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
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.JournalCalculationRow
import com.cash.guide.ui.notebook.JournalDateRuleBand
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.SavedCalculationActionsSheet
import com.cash.guide.ui.notebook.TajawalFamily

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.cash.guide.domain.DateGroupHelper
import com.cash.guide.ui.notebook.JournalFilterBar
import com.cash.guide.ui.notebook.JournalInlineSearchRow
import com.cash.guide.ui.notebook.JournalTwoLineCalculationRow

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    initialQuery: String? = null,
    onOpenCalculation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    LaunchedEffect(context) {
        viewModel.loadAll(context)
        if (!initialQuery.isNullOrBlank()) {
            viewModel.updateSearchQuery(initialQuery)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        JournalRuledDocument(modifier = Modifier.fillMaxSize()) {
            // Line 1: Header Band (Single Screen Title in pink pill sitting directly on ruled line 1)
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
                        .offset(y = 2.0.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HighlighterPink.copy(alpha = 0.40f))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.history_title),
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }

            // Line 2: Handwritten Search Bar sitting directly on ruled line 2
            JournalInlineSearchRow(
                query = state.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) }
            )

            // Line 3: Minimal Filter Bar (الكل ∨, 📅 هذا الشهر ∨)
            JournalFilterBar()

            // Line 4+: Results or Date Groups
            if (state.isSearching) {
                if (state.searchResults.isNotEmpty()) {
                    state.searchResults.forEachIndexed { idx, calc ->
                        val currency = runCatching { MoneyUnit.valueOf(calc.calculation.currency) }.getOrDefault(MoneyUnit.DIRHAM)
                        val totalFormatted = JournalLedgerManager.formatTotal(calc.totalCentimes, currency)
                        val currencySuffix = if (currency == MoneyUnit.DIRHAM) {
                            stringResource(R.string.currency_dirham)
                        } else {
                            stringResource(R.string.currency_rial)
                        }
                        val subtitle = DateGroupHelper.formatHistoryCalculationSubtitle(calc, context)

                        JournalTwoLineCalculationRow(
                            index = idx,
                            title = calc.calculation.title,
                            subtitle = subtitle,
                            totalAmount = totalFormatted,
                            currencySuffix = currencySuffix,
                            onClick = { onOpenCalculation(calc.calculation.id) },
                            onMoreClick = { viewModel.selectCalculationForAction(calc) }
                        )
                    }
                } else {
                    // Empty search result on 1 notebook line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JournalRuleSpacing),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.history_no_results_title),
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = JournalMutedInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 5.7.dp)
                        )
                    }
                }
            } else {
                if (state.allDateGroups.isNotEmpty()) {
                    state.allDateGroups.forEach { group ->
                        JournalDateRuleBand(title = group.header)

                        group.calculations.forEachIndexed { idx, calc ->
                            val currency = runCatching { MoneyUnit.valueOf(calc.calculation.currency) }.getOrDefault(MoneyUnit.DIRHAM)
                            val totalFormatted = JournalLedgerManager.formatTotal(calc.totalCentimes, currency)
                            val currencySuffix = if (currency == MoneyUnit.DIRHAM) {
                                stringResource(R.string.currency_dirham)
                            } else {
                                stringResource(R.string.currency_rial)
                            }
                            val subtitle = DateGroupHelper.formatHistoryCalculationSubtitle(calc, context)

                            JournalTwoLineCalculationRow(
                                index = idx,
                                title = calc.calculation.title,
                                subtitle = subtitle,
                                totalAmount = totalFormatted,
                                currencySuffix = currencySuffix,
                                onClick = { onOpenCalculation(calc.calculation.id) },
                                onMoreClick = { viewModel.selectCalculationForAction(calc) }
                            )
                        }

                        // 1 empty notebook line spacer after each date group
                        Spacer(modifier = Modifier.height(JournalRuleSpacing))
                    }
                } else if (!state.isLoading) {
                    // Empty history on 1 notebook line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JournalRuleSpacing),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.home_empty_title),
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = JournalMutedInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 5.7.dp)
                        )
                    }
                }
            }

            // Bottom Spacers: 5 notebook lines for full scrolling clearance above dock
            Spacer(modifier = Modifier.height(JournalRuleSpacing * 5))
        }

        // Action Sheet
        val actionCalc = state.selectedCalculationForAction
        if (actionCalc != null) {
            SavedCalculationActionsSheet(
                calculationTitle = actionCalc.calculation.title,
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
    }
}

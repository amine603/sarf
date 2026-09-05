package com.cash.guide.feature.home

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
import com.cash.guide.ui.notebook.JournalNewCalculationButton
import com.cash.guide.ui.notebook.JournalRecentHeader
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.SavedCalculationActionsSheet
import com.cash.guide.ui.notebook.TajawalFamily

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

    LaunchedEffect(context) {
        viewModel.loadRecent(context)
    }

    Box(modifier = modifier.fillMaxSize()) {
        JournalRuledDocument(modifier = Modifier.fillMaxSize()) {
            // Line 1: Header Band (Brand name on left + Search on right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: "حسابي • Hssabi" sitting directly on the ruled line
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = 2.0.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(HighlighterPink.copy(alpha = 0.40f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "حسابي",
                            fontFamily = TajawalFamily,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }
                    Text(
                        text = "Hssabi",
                        fontFamily = PatrickHandFamily,
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = 2.5.dp)
                    )
                }

                // Right: Hand-drawn Search icon sitting on the ruled line
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = JournalRuleSpacing)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = stringResource(R.string.home_search_placeholder),
                            onClick = onOpenHistory
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Search,
                        contentDescription = null,
                        tint = JournalInk,
                        size = 18.dp,
                        modifier = Modifier.offset(y = 3.5.dp)
                    )
                }
            }

            // Line 2: "+ Nouveau calcul" sitting directly on the ruled line (matching + Ajouter une ligne)
            JournalNewCalculationButton(onNewCalculation = onNewCalculation)

            // Line 3: 1 empty notebook line
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 4: Recent calculations header sitting directly on the ruled line
            if (!state.isEmpty) {
                JournalRecentHeader(onOpenHistory = onOpenHistory)

                // Date-grouped saved calculations (each calculation is exactly 1 ruled line, zero cards)
                state.recentDateGroups.forEach { group ->
                    JournalDateRuleBand(title = group.header)

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
                            onMoreClick = { viewModel.selectCalculationForAction(calc) }
                        )
                    }

                    // 1 empty notebook line after each date group
                    Spacer(modifier = Modifier.height(JournalRuleSpacing))
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
                        text = stringResource(R.string.home_empty_title),
                        fontFamily = PatrickHandFamily,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = 5.7.dp)
                    )
                }
            }

            // Bottom Spacers: exactly 3 notebook lines
            Spacer(modifier = Modifier.height(JournalRuleSpacing * 3))
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


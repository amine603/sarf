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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.DateGroupHelper
import com.cash.guide.domain.JournalLedgerManager
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.notebook.DeleteConfirmationDialog
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.JournalCalculationRow
import com.cash.guide.ui.notebook.JournalDateRuleBand
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.MonthPickerDialog
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.NotebookCalculationRow
import com.cash.guide.ui.notebook.NotebookDateGroupBlock
import com.cash.guide.ui.notebook.NotebookPrimaryActionButton
import com.cash.guide.ui.notebook.NotebookSearchField
import com.cash.guide.ui.notebook.NotebookSegmentedControl
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.SavedCalculationActionsSheet
import com.cash.guide.ui.notebook.TajawalFamily
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    initialQuery: String? = null,
    onOpenCalculation: (String) -> Unit,
    onNewCalculation: (() -> Unit)? = null,
    onOpenMonthCalculations: ((Int, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val state by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    var showMonthPicker by remember { mutableStateOf(false) }

    val currentMonthYear = remember(context) {
        val locale = context.resources.configuration.locales[0]
        val sdf = java.text.SimpleDateFormat("LLLL yyyy", locale)
        val raw = sdf.format(java.util.Date())
        raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    LaunchedEffect(context) {
        viewModel.loadAll(context)
        if (!initialQuery.isNullOrBlank()) {
            viewModel.updateSearchQuery(initialQuery)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        JournalRuledDocument(modifier = Modifier.fillMaxSize(), clearFocusOnTap = true) {
            // Line 1: Header Band (Compact "Historique" / "السجل" + Month Year sitting directly on the ruled line)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.history_title),
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 18.5.sp else 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                )

                Text(
                    text = currentMonthYear,
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 14.5.sp else 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = JournalMutedInk.copy(alpha = 0.85f),
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                )
            }

            // Line 2: 1 rule spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 3: Ruled-Line Search Row with Calendar icon popup
            NotebookSearchField(
                query = state.searchQuery,
                onQueryChange = { query -> viewModel.updateSearchQuery(query) },
                onOpenCalendar = { showMonthPicker = true }
            )

            // Line 4: 1 rule spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 5: Section Header & Filter: "Tous les calculs" on Start, "Tout / Ce mois" on End (sitting directly on the blue line)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .drawBehind {
                            val h = size.height
                            val w = size.width
                            val washHeight = 22.dp.toPx()
                            val washY = h - (if (isRtl) 16.0.dp.toPx() else 16.5.dp.toPx())
                            val padH = 8.dp.toPx()
                            drawRoundRect(
                                color = HighlighterYellow.copy(alpha = 0.55f),
                                topLeft = Offset(-padH, washY),
                                size = Size(w + padH * 2, washHeight),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                ) {
                    Text(
                        text = stringResource(R.string.history_section_title),
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = if (isRtl) 16.sp else 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                    )
                }

                NotebookSegmentedControl(
                    options = listOf(
                        HistoryFilter.ALL to stringResource(R.string.history_filter_all),
                        HistoryFilter.THIS_MONTH to stringResource(R.string.history_filter_month)
                    ),
                    selectedOption = state.selectedFilter,
                    onSelectOption = { viewModel.setFilter(it) }
                )
            }

            // Line 8+: Results or Date Groups
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
                        val matchingItem = calc.items.firstOrNull { it.label.contains(state.searchQuery, ignoreCase = true) }
                        val subtitle = if (matchingItem != null) {
                            val itemAmt = JournalLedgerManager.formatTotal(matchingItem.amountCentimes, currency)
                            "${matchingItem.label} — $itemAmt $currencySuffix"
                        } else {
                            val itemLabels = calc.items
                                .map { it.label.trim() }
                                .filter { it.isNotBlank() }
                            if (itemLabels.isNotEmpty()) {
                                itemLabels.joinToString(if (isRtl) "، " else ", ")
                            } else if (calc.items.isNotEmpty()) {
                                calc.items.indices.map { idx ->
                                    if (isRtl) "عنصر ${idx + 1}" else "Article ${idx + 1}"
                                }.joinToString(if (isRtl) "، " else ", ")
                            } else {
                                if (isRtl) "بدون عناصر" else "Aucun article"
                            }
                        }

                        NotebookCalculationRow(
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
                            modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                        )
                    }
                }
            } else {
                if (state.allDateGroups.isNotEmpty()) {
                    state.allDateGroups.forEachIndexed { groupIndex, group ->
                        NotebookDateGroupBlock(
                            header = group.header,
                            calculations = group.calculations,
                            onOpenCalculation = onOpenCalculation,
                            onMoreClick = { viewModel.selectCalculationForAction(it) },
                            searchQuery = state.searchQuery
                        )

                        // 1 empty notebook line spacer after each date group
                        if (groupIndex < state.allDateGroups.lastIndex) {
                            Spacer(modifier = Modifier.height(JournalRuleSpacing))
                        }
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
                            modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
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

        // Month Picker Dialog Popup when calendar icon is clicked
        if (showMonthPicker) {
            val cal = remember { java.util.Calendar.getInstance() }
            MonthPickerDialog(
                initialYear = cal.get(java.util.Calendar.YEAR),
                initialMonth = cal.get(java.util.Calendar.MONTH) + 1,
                onDismiss = { showMonthPicker = false },
                onSelectMonth = { year, month ->
                    showMonthPicker = false
                    onOpenMonthCalculations?.invoke(year, month)
                }
            )
        }
    }
}

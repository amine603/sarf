package com.cash.guide.feature.calculs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.notebook.DeleteConfirmationDialog
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.MonthPickerDialog
import com.cash.guide.ui.notebook.NewCalculationSetupSheet
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.NotebookDateGroupBlock
import com.cash.guide.ui.notebook.NotebookHubActionCard
import com.cash.guide.ui.notebook.NotebookSearchField
import com.cash.guide.ui.notebook.NotebookSectionBand
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.SavedCalculationActionsSheet
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.isArabicScript
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.resolveJournalFont
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun CalculsScreen(
    viewModel: CalculsViewModel,
    onNavigateBack: () -> Unit,
    onOpenCalculation: (String) -> Unit,
    onOpenCashRegister: () -> Unit,
    onNewCalculationWithParams: ((title: String, calcType: String, currency: MoneyUnit, templateId: String?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val state by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    var showMonthPicker by remember { mutableStateOf(false) }
    var showNewCalcSetupSheet by remember { mutableStateOf(false) }

    val currentMonthYear = remember(context) {
        val locale = context.resources.configuration.locales[0]
        SimpleDateFormat("MMMM yyyy", locale).format(Date()).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(locale) else it.toString()
        }
    }

    LaunchedEffect(context) {
        viewModel.loadCalculations(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JournalPaper)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar: Back button + Pink Highlighter "Calculs" pill on start, Month/Year on end
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = JournalPaper,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Start: Back button + "Calculs" soft pink pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(
                                    role = Role.Button,
                                    onClick = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        onNavigateBack()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            HisabiSketchIcon(
                                symbol = HisabiSymbol.Back,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = JournalInk,
                                size = 20.dp
                            )
                        }

                        // Pink Highlighter Pill for Title: "Calculs" / "الحسابات"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HighlighterPink.copy(alpha = 0.45f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val titleText = stringResource(R.string.home_action_calculs)
                            Text(
                                text = titleText,
                                fontFamily = resolveJournalFont(titleText, isRtl),
                                fontSize = if (isArabicScript(titleText) || isRtl) 17.sp else 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }
                    }

                    // End: Current Month/Year (e.g. "Septembre 2026")
                    Text(
                        text = currentMonthYear,
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = if (isRtl) 14.5.sp else 15.sp,
                        color = JournalWritingInk.copy(alpha = 0.80f),
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            // Notebook Ruled Body
            JournalRuledDocument(modifier = Modifier.fillMaxSize(), clearFocusOnTap = true) {
                // Line 1: 1 rule spacer
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Line 2: Search field with calendar picker
                NotebookSearchField(
                    query = state.searchQuery,
                    onQueryChange = { query -> viewModel.updateSearchQuery(query) },
                    onOpenCalendar = { showMonthPicker = true },
                    isDateFiltered = state.selectedDateEpoch != null || state.selectedYear != null
                )

                // Line 3: 1 rule spacer
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Line 4-5: Action Card 1: Nouveau calcul (2 rules tall)
                NotebookHubActionCard(
                    title = stringResource(R.string.home_new_calculation),
                    subtitle = stringResource(R.string.calculs_action_new_calculation_subtitle),
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFCE7F3))
                                .border(0.8.dp, Color(0xFFF472B6).copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🧮",
                                fontSize = 18.sp,
                                modifier = Modifier.offset(y = (-0.5).dp)
                            )
                        }
                    },
                    onClick = {
                        showNewCalcSetupSheet = true
                    }
                )

                // 2dp spacing between action cards for separation
                Spacer(modifier = Modifier.height(2.dp))

                // Line 6-7: Action Card 2: Rendu de monnaie (2 rules tall)
                NotebookHubActionCard(
                    title = stringResource(R.string.calculs_action_change_title),
                    subtitle = stringResource(R.string.calculs_action_change_subtitle),
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .border(0.8.dp, Color(0xFF4ADE80).copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💵",
                                fontSize = 18.sp,
                                modifier = Modifier.offset(y = (-0.5).dp)
                            )
                        }
                    },
                    onClick = onOpenCashRegister
                )

                // Line 8: 1 rule spacer
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Line 9: Section header: "Calculs récents" with soft yellow highlighter pill
                NotebookSectionBand(
                    title = stringResource(R.string.home_recent_title),
                    highlightColor = HighlighterYellow,
                    isCentered = false
                )

                // Line 10: 1 rule spacer before list
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Recent calculations list
                if (!state.isEmpty) {
                    state.displayDateGroups.forEachIndexed { groupIndex, group ->
                        NotebookDateGroupBlock(
                            header = group.header,
                            calculations = group.calculations,
                            onOpenCalculation = onOpenCalculation,
                            onMoreClick = { viewModel.selectCalculationForAction(it) },
                            searchQuery = state.searchQuery,
                            pinnedCalculationIds = state.pinnedCalculationIds
                        )

                        if (groupIndex < state.displayDateGroups.lastIndex) {
                            Spacer(modifier = Modifier.height(JournalRuleSpacing))
                        }
                    }
                } else if (!state.isLoading) {
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
                            fontSize = if (isRtl) 14.5.sp else 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = JournalMutedInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.journalBaselineOnRule()
                        )
                    }
                }

                // Bottom spacing for full scroll clearance
                Spacer(modifier = Modifier.height(JournalRuleSpacing * 5))
            }
        }

        // Action Sheet for selected calculation
        val actionCalc = state.selectedCalculationForAction
        if (actionCalc != null) {
            val isPinned = actionCalc.calculation.id in state.pinnedCalculationIds
            SavedCalculationActionsSheet(
                calculationTitle = actionCalc.calculation.title,
                isPinned = isPinned,
                onTogglePin = { viewModel.togglePin(actionCalc.calculation.id) },
                paymentStatus = actionCalc.calculation.paymentStatus,
                onTogglePaymentStatus = {
                    viewModel.togglePaymentStatus(actionCalc.calculation.id, actionCalc.calculation.paymentStatus)
                },
                calcType = actionCalc.calculation.calcType,
                onToggleCalcType = {
                    viewModel.toggleCalcType(actionCalc.calculation.id, actionCalc.calculation.calcType)
                },
                onEdit = {
                    viewModel.selectCalculationForAction(null)
                    onOpenCalculation(actionCalc.calculation.id)
                },
                onDuplicate = {
                    viewModel.duplicateCalculation(actionCalc) { newId ->
                        viewModel.selectCalculationForAction(null)
                        onOpenCalculation(newId)
                    }
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

        // Month Picker Dialog
        if (showMonthPicker) {
            val cal = remember { Calendar.getInstance() }
            MonthPickerDialog(
                initialYear = state.selectedYear ?: cal.get(Calendar.YEAR),
                initialMonth = state.selectedMonth ?: (cal.get(Calendar.MONTH) + 1),
                onDismiss = { showMonthPicker = false },
                onSelectMonth = { year, month ->
                    showMonthPicker = false
                    viewModel.filterByMonth(year, month)
                }
            )
        }

        // New Calculation Setup Sheet
        if (showNewCalcSetupSheet && onNewCalculationWithParams != null) {
            NewCalculationSetupSheet(
                onDismiss = { showNewCalcSetupSheet = false },
                onConfirm = { title, calcType, currency, templateId ->
                    showNewCalcSetupSheet = false
                    onNewCalculationWithParams(title, calcType, currency, templateId)
                }
            )
        }
    }
}

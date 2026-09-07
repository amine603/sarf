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
import androidx.compose.runtime.rememberCoroutineScope
import com.cash.guide.domain.CalculationImageShareHelper
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import com.cash.guide.ui.notebook.journalBaselineOnRule
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.cash.guide.feature.groups.AssignToGroupDialog
import com.cash.guide.app.LocalizedContextWrapper
import com.cash.guide.domain.DateGroupHelper
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.NotebookDateGroupBlock
import com.cash.guide.ui.notebook.NotebookPrimaryActionButton
import com.cash.guide.ui.notebook.NotebookSearchField
import com.cash.guide.ui.notebook.NotebookSectionBand
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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val state by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val coroutineScope = rememberCoroutineScope()

    var showMonthPicker by remember { mutableStateOf(false) }
    var calcToAssignToGroup by remember { mutableStateOf<com.cash.guide.data.db.CalculationWithItems?>(null) }
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
        JournalRuledDocument(modifier = Modifier.fillMaxSize(), clearFocusOnTap = true) {
            // Line 1: Header Band (Compact "Hssabi" / "حسابي" + Month Year sitting directly on the ruled line)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val greetingPrefix = stringResource(R.string.home_greeting_prefix)
                val greetingAnnotated = remember(greetingPrefix, state.userName) {
                    buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Light,
                                color = JournalInk.copy(alpha = 0.72f)
                            )
                        ) {
                            append("$greetingPrefix ")
                        }
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = JournalInk
                            )
                        ) {
                            append(state.userName)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = greetingAnnotated,
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = if (isRtl) 17.5.sp else 19.5.sp,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.journalBaselineOnRule()
                    )
                    Text(
                        text = "😊",
                        fontSize = 15.sp,
                        modifier = Modifier.offset(y = (-3).dp)
                    )
                }

                Text(
                    text = currentMonthYear,
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 14.5.sp else 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = JournalMutedInk.copy(alpha = 0.85f),
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule()
                )
            }

            // Line 2: 1 rule spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 3: Ruled-Line Search Row with Calendar icon popup
            NotebookSearchField(
                query = state.searchQuery,
                onQueryChange = { query -> viewModel.updateSearchQuery(query) },
                onOpenCalendar = { showMonthPicker = true },
                isDateFiltered = state.selectedDateEpoch != null
            )

            // Line 4: 1 rule spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 5: Full-width pink "+ Nouveau calcul" primary button (29dp)
            NotebookPrimaryActionButton(
                text = stringResource(R.string.home_new_calculation),
                onClick = onNewCalculation
            )

            // Line 6: 1 rule spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Section: Recent Calculations ("Calculs récents" / "الحسابات الأخيرة") in soft blue band
            if (!state.isEmpty) {
                NotebookSectionBand(
                    title = stringResource(R.string.home_recent_title),
                    highlightColor = HighlighterBlue,
                    isCentered = true
                )

                // Date-grouped saved calculations with vertical grouping guide
                state.displayDateGroups.forEachIndexed { groupIndex, group ->
                    NotebookDateGroupBlock(
                        header = group.header,
                        calculations = group.calculations,
                        onOpenCalculation = onOpenCalculation,
                        onMoreClick = { viewModel.selectCalculationForAction(it) },
                        searchQuery = state.searchQuery,
                        pinnedCalculationIds = state.pinnedCalculationIds
                    )

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
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable(
                                role = Role.Button,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onOpenHistory()
                                }
                            )
                        ) {
                            if (isRtl) {
                                Text(
                                    text = stringResource(R.string.home_see_all),
                                    fontFamily = TajawalFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = JournalWritingInk.copy(alpha = 0.85f),
                                    style = TextStyle(platformStyle = NoFontPadding),
                                    modifier = Modifier.journalBaselineOnRule()
                                )
                                // Left-pointing handwritten arrow
                                Canvas(
                                    modifier = Modifier
                                        .size(13.dp, 10.dp)
                                        .offset(y = (-3.5).dp)
                                ) {
                                    val strokeW = 1.35.dp.toPx()
                                    val tint = JournalWritingInk.copy(alpha = 0.85f)
                                    val midY = size.height / 2f
                                    drawLine(tint, Offset(size.width, midY), Offset(1f, midY), strokeWidth = strokeW, cap = StrokeCap.Round)
                                    drawLine(tint, Offset(4.5.dp.toPx(), 1f), Offset(1f, midY), strokeWidth = strokeW, cap = StrokeCap.Round)
                                    drawLine(tint, Offset(4.5.dp.toPx(), size.height - 1f), Offset(1f, midY), strokeWidth = strokeW, cap = StrokeCap.Round)
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.home_see_all),
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = JournalWritingInk.copy(alpha = 0.85f),
                                    style = TextStyle(platformStyle = NoFontPadding),
                                    modifier = Modifier.journalBaselineOnRule()
                                )
                                // Right-pointing handwritten arrow
                                Canvas(
                                    modifier = Modifier
                                        .size(13.dp, 10.dp)
                                        .offset(y = (-3.5).dp)
                                ) {
                                    val strokeW = 1.35.dp.toPx()
                                    val tint = JournalWritingInk.copy(alpha = 0.85f)
                                    val midY = size.height / 2f
                                    drawLine(tint, Offset(0f, midY), Offset(size.width - 1f, midY), strokeWidth = strokeW, cap = StrokeCap.Round)
                                    drawLine(tint, Offset(size.width - 4.5.dp.toPx(), 1f), Offset(size.width - 1f, midY), strokeWidth = strokeW, cap = StrokeCap.Round)
                                    drawLine(tint, Offset(size.width - 4.5.dp.toPx(), size.height - 1f), Offset(size.width - 1f, midY), strokeWidth = strokeW, cap = StrokeCap.Round)
                                }
                            }
                        }
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
                        modifier = Modifier.journalBaselineOnRule()
                    )
                }
            }

            // Bottom Spacers: 5 notebook lines for full scrolling clearance above dock
            Spacer(modifier = Modifier.height(JournalRuleSpacing * 5))
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
                onShareImage = {
                    val calcToShare = actionCalc
                    coroutineScope.launch {
                        val groupName = calcToShare.calculation.groupId?.let { gid ->
                            viewModel.repository.getGroup(gid)?.name
                        }
                        CalculationImageShareHelper.shareCalculation(
                            context = context,
                            calculationWithItems = calcToShare,
                            groupName = groupName,
                            isRtl = isRtl
                        )
                    }
                    viewModel.selectCalculationForAction(null)
                },
                onAssignToGroup = {
                    calcToAssignToGroup = actionCalc
                },
                onDelete = {
                    viewModel.requestDelete(actionCalc)
                },
                onDismiss = {
                    viewModel.selectCalculationForAction(null)
                }
            )
        }

        // Assign to Group Dialog
        calcToAssignToGroup?.let { calc ->
            AssignToGroupDialog(
                calculationId = calc.calculation.id,
                currentGroupId = calc.calculation.groupId,
                calculationRepository = viewModel.repository,
                onDismiss = { calcToAssignToGroup = null },
                onAssigned = {
                    calcToAssignToGroup = null
                    viewModel.loadRecent(context)
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
    }
}


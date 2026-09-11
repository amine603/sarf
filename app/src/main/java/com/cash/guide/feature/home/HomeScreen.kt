package com.cash.guide.feature.home

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.cash.guide.domain.CalculationImageShareHelper
import com.cash.guide.domain.export.ExcelExportHelper
import com.cash.guide.domain.export.FileExportManager
import com.cash.guide.domain.export.PdfExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
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
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.resolveJournalFont
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.SavedCalculationActionsSheet
import com.cash.guide.ui.notebook.CreditDueDateDialog
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
import com.cash.guide.domain.RecentActivityItem
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.NotebookDateGroupBlock
import com.cash.guide.ui.notebook.NotebookActivityDateGroupBlock
import com.cash.guide.ui.notebook.NotebookActivityTimelineBlock
import com.cash.guide.ui.notebook.NotebookActivityActionsSheet
import com.cash.guide.ui.notebook.NotebookSpeedDialFab
import com.cash.guide.ui.notebook.isArabicScript
import com.cash.guide.ui.notebook.NotebookSearchField
import com.cash.guide.ui.notebook.NotebookSectionBand
import com.cash.guide.domain.ChecklistShareHelper
import com.cash.guide.domain.NoteShareHelper
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.cash.guide.ui.notebook.MonthPickerDialog
import com.cash.guide.ui.notebook.NewCalculationSetupSheet
import java.text.SimpleDateFormat
import java.util.Date
import com.cash.guide.data.TemplateRepository
import com.cash.guide.data.db.CalculationWithItems

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is LocalizedContextWrapper -> originalActivity ?: baseContext.findActivity()
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNewCalculation: () -> Unit = {},
    onNewCalculationWithParams: ((title: String, calcType: String, currency: MoneyUnit, templateId: String?) -> Unit)? = null,
    onOpenCalculation: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMonthCalculations: (year: Int, month: Int) -> Unit = { _, _ -> },
    onOpenStyleShowcase: () -> Unit = {},
    onOpenCalculs: () -> Unit = {},
    onOpenCashRegister: () -> Unit = {},
    onOpenChecklist: () -> Unit = {},
    onOpenChecklistWithId: (String) -> Unit = {},
    onOpenNotes: () -> Unit = {},
    onOpenNote: (String) -> Unit = {},
    onNewChecklist: () -> Unit = {},
    onNewNote: () -> Unit = {},
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
    var showNewCalcSetupSheet by remember { mutableStateOf(false) }
    var calcToAssignToGroup by remember { mutableStateOf<com.cash.guide.data.db.CalculationWithItems?>(null) }
    var creditDueDateCalc by remember { mutableStateOf<com.cash.guide.data.db.CalculationWithItems?>(null) }
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
                        fontSize = if (isRtl) 15.5.sp else 16.5.sp,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.journalBaselineOnRule()
                    )
                    Text(
                        text = "😊",
                        fontSize = 14.sp,
                        modifier = Modifier.offset(y = (-3).dp)
                    )
                }

                Text(
                    text = currentMonthYear,
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 13.5.sp else 14.sp,
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

            // 1 rule spacer before Activité récente section header
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Section Header: Activité récente in soft pink highlighter pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activityTitle = stringResource(R.string.home_recent_activity_title)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HighlighterPink.copy(alpha = 0.45f))
                        .padding(horizontal = 10.dp, vertical = 2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activityTitle,
                        fontFamily = resolveJournalFont(activityTitle, isRtl),
                        fontSize = if (isArabicScript(activityTitle) || isRtl) 15.sp else 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalWritingInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }

            // 1 rule spacer before activities list
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Recent activities list
            if (state.isFiltering) {
                // When actively searching or filtering, show filtered groups
                if (state.displayActivityGroups.isNotEmpty()) {
                    state.displayActivityGroups.forEachIndexed { groupIndex, group ->
                        NotebookActivityDateGroupBlock(
                            header = group.header,
                            items = group.items,
                            onOpenCalculation = onOpenCalculation,
                            onOpenChecklist = onOpenChecklistWithId,
                            onOpenNote = onOpenNote,
                            onMoreClick = { item -> viewModel.selectActivityForAction(item) },
                            searchQuery = state.searchQuery
                        )
                        if (groupIndex < state.displayActivityGroups.lastIndex) {
                            Spacer(modifier = Modifier.height(JournalRuleSpacing))
                        }
                    }
                }
            } else if (!state.isActivityEmpty) {
                // Section 1: Activités récentes - Top 6 items (2 Notes, 2 Calculs, 2 Checklists) directly without date tag
                if (state.recentActivityItems.isNotEmpty()) {
                    NotebookActivityTimelineBlock(
                        items = state.recentActivityItems,
                        onOpenCalculation = onOpenCalculation,
                        onOpenChecklist = onOpenChecklistWithId,
                        onOpenNote = onOpenNote,
                        onMoreClick = { item -> viewModel.selectActivityForAction(item) },
                        searchQuery = state.searchQuery
                    )
                }

                // Section 2: Aujourd'hui (items from today not in recent top 6)
                if (state.todayActivityItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(JournalRuleSpacing))

                    NotebookActivityDateGroupBlock(
                        header = stringResource(R.string.date_today),
                        items = state.todayActivityItems,
                        onOpenCalculation = onOpenCalculation,
                        onOpenChecklist = onOpenChecklistWithId,
                        onOpenNote = onOpenNote,
                        onMoreClick = { item -> viewModel.selectActivityForAction(item) },
                        searchQuery = state.searchQuery
                    )
                }

                // "Voir tout" under the content on the right (with 1 skipped line before it)
                if (state.displayActivityGroups.isNotEmpty() || state.recentActivityItems.isNotEmpty()) {
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
                                    fontSize = 14.sp,
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
                                    fontSize = 14.5.sp,
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
                        fontSize = if (isRtl) 14.5.sp else 15.sp,
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

        // Scrim overlay when FAB speed dial is open
        if (state.isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.setFabExpanded(false) }
                    )
            )
        }

        // Notebook Speed Dial FAB (52dp pink circle expanding into 3 capsules)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 18.dp, bottom = 18.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            NotebookSpeedDialFab(
                isExpanded = state.isFabExpanded,
                onToggle = { viewModel.toggleFabExpanded() },
                onDismiss = { viewModel.setFabExpanded(false) },
                onNewCalcul = {
                    viewModel.setFabExpanded(false)
                    onOpenCalculs()
                },
                onNewChecklist = {
                    viewModel.setFabExpanded(false)
                    onOpenChecklist()
                },
                onNewNote = {
                    viewModel.setFabExpanded(false)
                    onOpenNotes()
                }
            )
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
                paymentStatus = actionCalc.calculation.paymentStatus,
                onTogglePaymentStatus = {
                    viewModel.togglePaymentStatus(actionCalc.calculation.id, actionCalc.calculation.paymentStatus)
                },
                calcType = actionCalc.calculation.calcType,
                onToggleCalcType = {
                    viewModel.toggleCalcType(actionCalc.calculation.id, actionCalc.calculation.calcType)
                },
                onSetDueDate = {
                    creditDueDateCalc = actionCalc
                    viewModel.selectCalculationForAction(null)
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
                onSaveAsTemplate = {
                    coroutineScope.launch {
                        val nonBlankItems = actionCalc.items
                            .sortedBy { it.position }
                            .map { it.label.trim() }
                            .filter { it.isNotBlank() }
                        TemplateRepository.getInstance(context).saveCustomTemplate(
                            title = actionCalc.calculation.title,
                            calcType = actionCalc.calculation.calcType,
                            currency = actionCalc.calculation.currency,
                            itemLabels = nonBlankItems
                        )
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.template_saved_success),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
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
                onExportPdf = {
                    val calcToExport = actionCalc
                    coroutineScope.launch {
                        val groupName = calcToExport.calculation.groupId?.let { gid ->
                            viewModel.repository.getGroup(gid)?.name
                        }
                        val pdfFile = withContext(Dispatchers.IO) {
                            PdfExportHelper.exportSingleCalculationPdf(
                                context = context,
                                calculationWithItems = calcToExport,
                                groupName = groupName,
                                isRtl = isRtl
                            )
                        }
                        FileExportManager.shareFile(
                            context = context,
                            file = pdfFile,
                            mimeType = FileExportManager.MIME_PDF,
                            subject = calcToExport.calculation.title
                        )
                    }
                    viewModel.selectCalculationForAction(null)
                },
                onExportExcel = {
                    val calcToExport = actionCalc
                    coroutineScope.launch {
                        val groupName = calcToExport.calculation.groupId?.let { gid ->
                            viewModel.repository.getGroup(gid)?.name
                        }
                        val csvFile = withContext(Dispatchers.IO) {
                            ExcelExportHelper.exportSingleCalculation(
                                context = context,
                                calculationWithItems = calcToExport,
                                groupName = groupName
                            )
                        }
                        FileExportManager.shareFile(
                            context = context,
                            file = csvFile,
                            mimeType = FileExportManager.MIME_CSV,
                            subject = calcToExport.calculation.title
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

        // Action Sheet for Checklist and Note activities
        val selectedAct = state.selectedActivityForAction
        if (selectedAct != null && selectedAct !is RecentActivityItem.CalculationActivity) {
            val actTitle = when (selectedAct) {
                is RecentActivityItem.ChecklistActivity -> selectedAct.checklistWithItems.checklist.title
                is RecentActivityItem.NoteActivity -> selectedAct.note.title
                else -> ""
            }
            NotebookActivityActionsSheet(
                title = actTitle,
                onOpen = {
                    when (selectedAct) {
                        is RecentActivityItem.ChecklistActivity -> onOpenChecklistWithId(selectedAct.checklistWithItems.checklist.id)
                        is RecentActivityItem.NoteActivity -> onOpenNote(selectedAct.note.id)
                        else -> Unit
                    }
                    viewModel.selectActivityForAction(null)
                },
                onShare = {
                    coroutineScope.launch {
                        when (selectedAct) {
                            is RecentActivityItem.ChecklistActivity -> {
                                ChecklistShareHelper.shareAsImage(
                                    context = context,
                                    checklistId = selectedAct.checklistWithItems.checklist.id,
                                    title = selectedAct.checklistWithItems.checklist.title,
                                    items = selectedAct.checklistWithItems.items,
                                    isRtl = isRtl
                                )
                            }
                            is RecentActivityItem.NoteActivity -> {
                                NoteShareHelper.shareAsImage(context, selectedAct.note, isRtl)
                            }
                            else -> Unit
                        }
                    }
                    viewModel.selectActivityForAction(null)
                },
                onDelete = {
                    viewModel.promptDeleteActivity(selectedAct)
                },
                onDismiss = {
                    viewModel.selectActivityForAction(null)
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

        // Credit Due Date & Reminder Dialog
        if (creditDueDateCalc != null) {
            val targetCalc = creditDueDateCalc!!
            CreditDueDateDialog(
                initialDueDateEpochMs = targetCalc.calculation.dueDateEpochMs,
                initialReminderEnabled = targetCalc.calculation.reminderEnabled,
                initialReminderTimeEpochMs = targetCalc.calculation.reminderTimeEpochMs,
                onSave = { dueDate, reminderEnabled, reminderTime ->
                    viewModel.updateCreditDueDate(
                        context = context,
                        calculation = targetCalc,
                        dueDateEpochMs = dueDate,
                        reminderEnabled = reminderEnabled,
                        reminderTimeEpochMs = reminderTime
                    )
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.due_date_saved_toast),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    creditDueDateCalc = null
                },
                onClear = {
                    viewModel.updateCreditDueDate(
                        context = context,
                        calculation = targetCalc,
                        dueDateEpochMs = null,
                        reminderEnabled = false,
                        reminderTimeEpochMs = null
                    )
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.due_date_cleared_toast),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    creditDueDateCalc = null
                },
                onDismiss = { creditDueDateCalc = null }
            )
        }

        // Delete Confirmation Dialog
        if (state.calculationToDelete != null || state.activityToDelete != null) {
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

        // New Calculation Setup Sheet (Personnel vs Credit, Title, Currency, Templates)
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


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
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.NotebookDateGroupBlock
import com.cash.guide.ui.notebook.NotebookPrimaryActionButton
import com.cash.guide.ui.notebook.NotebookCashRegisterActionButton
import com.cash.guide.ui.notebook.NotebookChecklistActionButton
import com.cash.guide.ui.notebook.NotebookSearchField
import com.cash.guide.ui.notebook.NotebookSectionBand
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
    onOpenCashRegister: () -> Unit = {},
    onOpenChecklist: () -> Unit = {},
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

            // Line 4: 1 rule spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 5: Full-width pink "+ Nouveau calcul" primary button (29dp)
            NotebookPrimaryActionButton(
                text = stringResource(R.string.home_new_calculation),
                onClick = {
                    if (onNewCalculationWithParams != null) {
                        showNewCalcSetupSheet = true
                    } else {
                        onNewCalculation()
                    }
                }
            )

            // Line 6: Caisse & Rendu de monnaie (Quick Access Action - 29dp)
            NotebookCashRegisterActionButton(
                onClick = onOpenCashRegister
            )

            // Line 7: Checklist (Quick Access Action - 29dp)
            NotebookChecklistActionButton(
                onClick = onOpenChecklist
            )

            // Line 8: 1 rule spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Notebook Filter Tabs Row (29dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Filter: ALL (Tous / الكل)
                val allSelected = state.selectedPaymentFilter == PaymentFilter.ALL
                val allLabel = stringResource(R.string.filter_all)
                Box(
                    modifier = Modifier
                        .height(25.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (allSelected) JournalInk.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                        .clickable(role = Role.Tab) {
                            viewModel.setPaymentFilter(PaymentFilter.ALL)
                        }
                        .drawBehind {
                            if (allSelected) {
                                val strokeW = 2.dp.toPx()
                                val y = size.height - strokeW / 2
                                val insetX = 6.dp.toPx()
                                drawLine(
                                    color = HighlighterPink,
                                    start = Offset(insetX, y),
                                    end = Offset(size.width - insetX, y),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        .padding(horizontal = 11.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = allLabel,
                        fontFamily = resolveJournalFont(allLabel, isRtl),
                        fontSize = if (isRtl) 13.sp else 13.5.sp,
                        fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (allSelected) JournalInk else JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }

                // Vertical Divider 1
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(11.dp)
                        .background(JournalRule.copy(alpha = 0.70f), RoundedCornerShape(0.5.dp))
                )

                // Filter: UNPAID (Crédits / الكريدي)
                val unpaidSelected = state.selectedPaymentFilter == PaymentFilter.UNPAID
                val unpaidLabel = stringResource(R.string.filter_unpaid)
                Box(
                    modifier = Modifier
                        .height(25.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (unpaidSelected) JournalInk.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                        .clickable(role = Role.Tab) {
                            viewModel.setPaymentFilter(PaymentFilter.UNPAID)
                        }
                        .drawBehind {
                            if (unpaidSelected) {
                                val strokeW = 2.dp.toPx()
                                val y = size.height - strokeW / 2
                                val insetX = 6.dp.toPx()
                                drawLine(
                                    color = HighlighterPink,
                                    start = Offset(insetX, y),
                                    end = Offset(size.width - insetX, y),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        .padding(horizontal = 11.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unpaidLabel,
                        fontFamily = resolveJournalFont(unpaidLabel, isRtl),
                        fontSize = if (isRtl) 13.sp else 13.5.sp,
                        fontWeight = if (unpaidSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (unpaidSelected) JournalInk else JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }

                // Vertical Divider 2
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(11.dp)
                        .background(JournalRule.copy(alpha = 0.70f), RoundedCornerShape(0.5.dp))
                )

                // Filter: PAID (Payés / الخالص)
                val paidSelected = state.selectedPaymentFilter == PaymentFilter.PAID
                val paidLabel = stringResource(R.string.filter_paid)
                Box(
                    modifier = Modifier
                        .height(25.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (paidSelected) JournalInk.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                        .clickable(role = Role.Tab) {
                            viewModel.setPaymentFilter(PaymentFilter.PAID)
                        }
                        .drawBehind {
                            if (paidSelected) {
                                val strokeW = 2.dp.toPx()
                                val y = size.height - strokeW / 2
                                val insetX = 6.dp.toPx()
                                drawLine(
                                    color = HighlighterPink,
                                    start = Offset(insetX, y),
                                    end = Offset(size.width - insetX, y),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        .padding(horizontal = 11.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = paidLabel,
                        fontFamily = resolveJournalFont(paidLabel, isRtl),
                        fontSize = if (isRtl) 13.sp else 13.5.sp,
                        fontWeight = if (paidSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (paidSelected) JournalInk else JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }

            // If filtering by UNPAID, show the total debt banner (29dp)
            if (state.selectedPaymentFilter == PaymentFilter.UNPAID) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(27.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, androidx.compose.ui.graphics.Color(0xFFF59E0B).copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                            .background(androidx.compose.ui.graphics.Color(0xFFFFFBEB))
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val bannerTitle = stringResource(R.string.total_unpaid_banner)
                        Text(
                            text = bannerTitle,
                            fontFamily = resolveJournalFont(bannerTitle, isRtl),
                            fontSize = if (isRtl) 13.sp else 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = androidx.compose.ui.graphics.Color(0xFFB45309),
                            style = TextStyle(platformStyle = NoFontPadding)
                        )

                        val totalStr = JournalLedgerManager.formatTotal(state.unpaidTotalCentimes, MoneyUnit.DIRHAM)
                        val dirhamSuffix = stringResource(R.string.currency_dirham)
                        Text(
                            text = "$totalStr $dirhamSuffix",
                            fontFamily = PatrickHandFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color(0xFFB45309),
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }
                }
            }

            // 1 rule spacer before calculations list
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Recent calculations list directly under spacer
            if (!state.isEmpty) {
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


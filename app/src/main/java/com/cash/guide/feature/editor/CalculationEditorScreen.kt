package com.cash.guide.feature.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.JournalKeyboardLanguage
import com.cash.guide.domain.JournalKeyboardMode
import com.cash.guide.domain.MoneyMath
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.ActiveField
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.JournalActionConfirm
import com.cash.guide.ui.notebook.JournalActionDelete
import com.cash.guide.ui.notebook.JournalAddRowButton
import com.cash.guide.ui.notebook.JournalCalculatorPopup
import com.cash.guide.ui.notebook.JournalCompactNumericDock
import com.cash.guide.ui.notebook.JournalEntryRow
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.JournalTextKeyboardDock
import com.cash.guide.ui.notebook.JournalTotalResultBand
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.ManropeFamily
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.ExportOptionsBottomSheet
import com.cash.guide.ui.notebook.UnsavedChangesDialog
import com.cash.guide.ui.notebook.resolveJournalFont
import com.cash.guide.ui.notebook.isArabicScript
import kotlinx.coroutines.launch

@Composable
fun CalculationEditorScreen(
    viewModel: CalculationEditorViewModel,
    calculationId: String? = null,
    initialGroupId: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    var showExportSheet by remember { mutableStateOf(false) }

    LaunchedEffect(calculationId, initialGroupId) {
        viewModel.loadCalculation(calculationId, initialGroupId)
    }

    BackHandler {
        viewModel.handleBackPress(onNavigateBack)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JournalPaper)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            EditorTopBar(
                titleValue = state.title,
                onTitleChange = { viewModel.updateTitle(it) },
                isTitleActive = state.activeField == ActiveField.HEADER_TITLE,
                onTitleClick = { viewModel.selectCalculationTitle() },
                keyboardLanguage = state.keyboardLanguage,
                currency = state.currency,
                onCurrencyToggle = {
                    val next = if (state.currency == MoneyUnit.DIRHAM) MoneyUnit.RIAL else MoneyUnit.DIRHAM
                    viewModel.selectUnit(next)
                },
                paymentStatus = state.paymentStatus,
                onPaymentStatusToggle = { viewModel.togglePaymentStatus() },
                canUndo = state.canUndo,
                onUndoClick = { viewModel.undoDelete() },
                onCalculatorClick = { viewModel.openCalculatorPopup(state.activeRowId) },
                onSaveClick = { viewModel.saveCalculation(onSuccess = onNavigateBack) },
                onShareClick = { showExportSheet = true },
                onBackClick = { viewModel.handleBackPress(onNavigateBack) }
            )

            // Title validation error
            if (state.validationError == "TITLE_REQUIRED") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(JournalActionDelete.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val errorText = stringResource(R.string.editor_title_required)
                    Text(
                        text = errorText,
                        fontFamily = resolveJournalFont(errorText, isRtl),
                        fontSize = if (isRtl) 13.sp else 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JournalActionDelete
                    )
                }
            }

            // Ruled Paper Content
            JournalRuledDocument(
                listState = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Breathing space above the first row, perfectly aligned with the ruled notebook grid
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                state.rows.forEachIndexed { index, row ->
                    val rowNum = index + 1
                    val isTitleActive = state.activeRowId == row.id && state.activeField == ActiveField.TITLE
                    val isAmountActive = state.activeRowId == row.id && state.activeField == ActiveField.AMOUNT
                    val isValid = MoneyMath.isValidExpression(row.amount.text)

                    JournalEntryRow(
                        rowNumber = rowNum,
                        titleValue = row.title,
                        amountValue = row.amount,
                        currencySuffix = if (state.currency == MoneyUnit.DIRHAM) stringResource(R.string.currency_dirham) else stringResource(R.string.currency_rial),
                        isTitleActive = isTitleActive,
                        isAmountActive = isAmountActive,
                        isValid = isValid,
                        onTitleValueChange = { viewModel.updateRowTitle(row.id, it) },
                        onAmountValueChange = { viewModel.updateRowAmount(row.id, it) },
                        onTitleFocused = { viewModel.selectRowField(row.id, ActiveField.TITLE) },
                        onAmountFocused = { viewModel.selectRowField(row.id, ActiveField.AMOUNT) },
                        onDelete = { viewModel.removeRow(row.id) },
                        onConfirm = { viewModel.confirmRowEdit(row.id) }
                    )
                }

                // 1 empty notebook line before Add Row to prevent accidental taps (faux clic)
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Add Row Button
                JournalAddRowButton(
                    onAddRow = {
                        viewModel.addNewRow()
                        coroutineScope.launch {
                            listState.animateScrollToItem(state.rows.size)
                        }
                    }
                )

                // Exactly 2 empty notebook lines between Add Row and Total
                Spacer(modifier = Modifier.height(JournalRuleSpacing * 2))

                // Total Result Band
                val primaryFormatted = com.cash.guide.domain.JournalLedgerManager.formatTotal(state.totalCentimes, state.currency)
                val currencySuffix = if (state.currency == MoneyUnit.DIRHAM) stringResource(R.string.currency_dirham) else stringResource(R.string.currency_rial)
                JournalTotalResultBand(
                    amount = primaryFormatted,
                    suffix = currencySuffix,
                    hasInvalidRows = state.hasInvalidRows,
                    canBreakdown = false,
                    onShowBreakdown = {}
                )

                Spacer(modifier = Modifier.height(JournalRuleSpacing * 2))
            }

            // Keyboard Dock
            if (state.keyboardMode != JournalKeyboardMode.NONE) {
                when (state.keyboardMode) {
                    JournalKeyboardMode.TEXT -> {
                        JournalTextKeyboardDock(
                            language = state.keyboardLanguage,
                            shiftMode = state.shiftMode,
                            expanded = state.keyboardExpanded,
                            onToggleExpand = { viewModel.toggleKeyboardExpanded() },
                            onCycleLanguage = { viewModel.cycleLanguage() },
                            onSelectLanguage = { viewModel.selectLanguage(it) },
                            onToggleShift = { viewModel.toggleShift() },
                            onInsertText = { viewModel.applyTextKey(it) },
                            onBackspace = { viewModel.applyTextBackspace() },
                            onSwitchToNumericMode = { viewModel.switchToNumericMode() },
                            onConfirm = {
                                if (state.activeField == ActiveField.HEADER_TITLE) {
                                    viewModel.confirmCalculationTitle()
                                } else {
                                    state.activeRowId?.let { viewModel.confirmRowEdit(it) }
                                }
                            }
                        )
                    }
                    JournalKeyboardMode.NUMBER -> {
                        JournalCompactNumericDock(
                            expanded = state.keyboardExpanded,
                            onToggleExpand = { viewModel.toggleKeyboardExpanded() },
                            onKey = { viewModel.applyCompactKey(it) },
                            onSwitchToTextMode = { viewModel.switchToTextMode() },
                            onConfirm = {
                                if (state.activeField == ActiveField.HEADER_TITLE) {
                                    viewModel.confirmCalculationTitle()
                                } else {
                                    state.activeRowId?.let { viewModel.confirmRowEdit(it) }
                                }
                            }
                        )
                    }
                    JournalKeyboardMode.NONE -> {}
                }
            }
        }

        // Contextual Calculator Popup
        if (state.calculator.isVisible) {
            JournalCalculatorPopup(
                expression = state.calculator.expression,
                result = state.calculator.result,
                hasError = state.calculator.hasError,
                canConfirm = state.calculator.isEvaluated && state.calculator.result.isNotBlank() && !state.calculator.hasError,
                onKey = { viewModel.applyPopupKey(it) },
                onConfirm = { viewModel.confirmPopupResult() },
                onDismiss = { viewModel.closeCalculatorPopup() }
            )
        }

        // Unsaved Changes Dialog
        if (state.showUnsavedDialog) {
            UnsavedChangesDialog(
                onSave = { viewModel.saveCalculation(onSuccess = onNavigateBack) },
                onDiscard = { viewModel.discardChanges(onNavigateBack) },
                onContinue = { viewModel.dismissUnsavedDialog() }
            )
        }

        // Export Options Bottom Sheet
        if (showExportSheet) {
            ExportOptionsBottomSheet(
                title = state.title.text.ifBlank { stringResource(R.string.editor_new_title) },
                onExportPdf = { viewModel.exportAsPdf(context, isRtl) },
                onExportExcel = { viewModel.exportAsExcel(context) },
                onShareImage = { viewModel.shareAsImage(context, isRtl) },
                onDismiss = { showExportSheet = false }
            )
        }
    }
}

@Composable
private fun EditorTopBar(
    titleValue: TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    isTitleActive: Boolean,
    onTitleClick: () -> Unit,
    keyboardLanguage: JournalKeyboardLanguage,
    currency: MoneyUnit,
    onCurrencyToggle: () -> Unit,
    paymentStatus: String = "PAID",
    onPaymentStatusToggle: () -> Unit = {},
    canUndo: Boolean,
    onUndoClick: () -> Unit,
    onCalculatorClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val infiniteTransition = rememberInfiniteTransition(label = "editor_title_cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 0
                1f at 499
                0f at 500
                0f at 999
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "cursor_blink"
    )
    var titleLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val titleScrollState = rememberScrollState()

    LaunchedEffect(titleScrollState.maxValue, titleValue.text, isTitleActive) {
        if (isTitleActive) {
            titleScrollState.scrollTo(titleScrollState.maxValue)
        }
    }

    val isArabicKeyboard = keyboardLanguage == JournalKeyboardLanguage.ARABIC
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = JournalPaper,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 6.dp)
            ) {
                // --- Line 1 (Navigation Back + Calculation Title + Undo Button + Share Button) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (42dp touch target)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Back,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = JournalInk,
                        size = 20.dp
                    )
                }

                // Title in Watercolor Pink Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isTitleActive) HighlighterPink.copy(alpha = 0.52f)
                            else HighlighterPink.copy(alpha = 0.35f)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            keyboardController?.hide()
                            onTitleClick()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val containerWidth = maxWidth
                        CompositionLocalProvider(
                            LocalLayoutDirection provides if (isArabicKeyboard) LayoutDirection.Rtl else LayoutDirection.Ltr
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(min = containerWidth)
                                    .horizontalScroll(titleScrollState),
                                contentAlignment = Alignment.Center
                            ) {
                                if (titleValue.text.isEmpty()) {
                                    val placeholderText = stringResource(R.string.editor_title_placeholder)
                                    Text(
                                        text = placeholderText,
                                        fontFamily = resolveJournalFont(placeholderText, isRtl),
                                        fontSize = if (isArabicScript(placeholderText) || isRtl) 15.sp else 15.5.sp,
                                        color = JournalMutedInk.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = TextStyle(platformStyle = NoFontPadding)
                                    )
                                } else {
                                    Text(
                                        text = titleValue.text,
                                        fontFamily = resolveJournalFont(titleValue.text, isRtl),
                                        fontSize = if (isArabicScript(titleValue.text) || isRtl) 15.5.sp else 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = JournalInk,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = TextStyle(platformStyle = NoFontPadding),
                                        modifier = Modifier.drawWithContent {
                                            drawContent()
                                            if (isTitleActive && cursorAlpha > 0f) {
                                                val layout = titleLayoutResult
                                                val cursorX = if (layout != null && titleValue.text.isNotEmpty()) {
                                                    val offset = titleValue.selection.end.coerceIn(0, titleValue.text.length)
                                                    val rect = layout.getCursorRect(offset)
                                                    rect.left
                                                } else {
                                                    size.width / 2f
                                                }
                                                val cursorH = 17.dp.toPx()
                                                val cursorTop = (size.height - cursorH) / 2f
                                                drawLine(
                                                    color = JournalInk.copy(alpha = cursorAlpha),
                                                    start = Offset(cursorX, cursorTop),
                                                    end = Offset(cursorX, cursorTop + cursorH),
                                                    strokeWidth = 1.8.dp.toPx(),
                                                    cap = StrokeCap.Round
                                                )
                                            }
                                        },
                                        onTextLayout = { titleLayoutResult = it }
                                    )
                                }

                                // Centered blinking cursor when empty and title is active
                                if (titleValue.text.isEmpty() && isTitleActive && cursorAlpha > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(min = containerWidth)
                                            .height(22.dp)
                                            .drawWithContent {
                                                val cursorX = size.width / 2f
                                                val cursorH = 17.dp.toPx()
                                                val cursorTop = (size.height - cursorH) / 2f
                                                drawLine(
                                                    color = JournalInk.copy(alpha = cursorAlpha),
                                                    start = Offset(cursorX, cursorTop),
                                                    end = Offset(cursorX, cursorTop + cursorH),
                                                    strokeWidth = 1.8.dp.toPx(),
                                                    cap = StrokeCap.Round
                                                )
                                            }
                                    )
                                }
                            }
                        }
                    }
                }

                // Undo Button (42dp touch target)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            enabled = canUndo,
                            onClickLabel = stringResource(R.string.cd_undo),
                            onClick = onUndoClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Undo,
                        contentDescription = stringResource(R.string.cd_undo),
                        tint = if (canUndo) JournalInk else JournalInk.copy(alpha = 0.25f),
                        size = 20.dp
                    )
                }

                // Share Button (42dp touch target)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onShareClick),
                    contentAlignment = Alignment.Center
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Share,
                        contentDescription = stringResource(R.string.action_share_image),
                        tint = JournalInk,
                        size = 20.dp
                    )
                }
            }

            // --- Line 2 (Editor Tools: Currency + Calculator + Save) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Currency Switcher Badge (Right in RTL, Left in LTR)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HighlighterYellow.copy(alpha = 0.50f))
                        .clickable(role = Role.Button, onClick = onCurrencyToggle)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val currencyText = if (currency == MoneyUnit.DIRHAM) stringResource(R.string.currency_dirham) else stringResource(R.string.currency_rial)
                        Text(
                            text = currencyText,
                            fontFamily = resolveJournalFont(currencyText, isRtl),
                            fontSize = if (isRtl) 13.sp else 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Page,
                            contentDescription = null,
                            tint = JournalInk.copy(alpha = 0.6f),
                            size = 12.dp
                        )
                    }
                }

                // 2. Payment Status Rubber Stamp (Kredi / Khlass)
                val isPaid = paymentStatus == "PAID"
                val stampColor = if (isPaid) Color(0xFF15803D) else Color(0xFFC2410C)
                val stampBg = if (isPaid) Color(0xFFDCFCE7).copy(alpha = 0.65f) else Color(0xFFFFEDD5).copy(alpha = 0.65f)
                val stampText = if (isPaid) {
                    stringResource(R.string.payment_status_paid) + " ✓"
                } else {
                    stringResource(R.string.payment_status_unpaid)
                }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = if (isPaid) -2f else 2f
                        }
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            width = 1.35.dp,
                            color = stampColor.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .background(stampBg)
                        .clickable(role = Role.Button, onClick = onPaymentStatusToggle)
                        .padding(horizontal = 9.dp, vertical = 3.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stampText,
                        fontFamily = resolveJournalFont(stampText, isRtl),
                        fontSize = if (isRtl) 12.5.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = stampColor,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }

                // 3. Calculator Button (Center)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onCalculatorClick)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Calculator,
                        contentDescription = stringResource(R.string.editor_open_calculator),
                        tint = JournalInk,
                        size = 18.dp
                    )
                    val calcText = stringResource(R.string.calculator_title)
                    Text(
                        text = calcText,
                        fontFamily = resolveJournalFont(calcText, isRtl),
                        fontSize = if (isRtl) 13.sp else 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }

                // 3. Save ("حفظ") Button with lowered pink underline (Left in RTL, Right in LTR)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(role = Role.Button, onClick = onSaveClick)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .drawBehind {
                            val strokeW = 1.35.dp.toPx()
                            val y = size.height + 2.5.dp.toPx()
                            drawLine(
                                color = HighlighterPink,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val saveText = stringResource(R.string.editor_save)
                    Text(
                        text = saveText,
                        fontFamily = resolveJournalFont(saveText, isRtl),
                        fontSize = if (isRtl) 14.5.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }
        }

        // Subtle edge-to-edge light black line beneath tools (Dirham, Calculator, Save)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(JournalInk.copy(alpha = 0.16f))
        )
    }
}
}

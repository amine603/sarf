package com.cash.guide.feature.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
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
import com.cash.guide.ui.notebook.UnsavedChangesDialog
import kotlinx.coroutines.launch

@Composable
fun CalculationEditorScreen(
    viewModel: CalculationEditorViewModel,
    calculationId: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(calculationId) {
        viewModel.loadCalculation(calculationId)
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
                title = state.title.text,
                onTitleChange = { viewModel.updateTitle(state.title.copy(text = it)) },
                currency = state.currency,
                onCurrencyToggle = {
                    val next = if (state.currency == MoneyUnit.DIRHAM) MoneyUnit.RIAL else MoneyUnit.DIRHAM
                    viewModel.selectUnit(next)
                },
                onCalculatorClick = { viewModel.openCalculatorPopup(state.activeRowId) },
                onSaveClick = { viewModel.saveCalculation(onSuccess = onNavigateBack) },
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
                    Text(
                        text = stringResource(R.string.editor_title_required),
                        fontFamily = ManropeFamily,
                        fontSize = 13.sp,
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

                // Add Row Button
                JournalAddRowButton(
                    onAddRow = {
                        viewModel.addNewRow()
                        coroutineScope.launch {
                            listState.animateScrollToItem(state.rows.size)
                        }
                    }
                )

                // Exactly 3 empty notebook lines between Add Row and Total
                Spacer(modifier = Modifier.height(JournalRuleSpacing * 3))

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
                            onConfirm = { state.activeRowId?.let { viewModel.confirmRowEdit(it) } }
                        )
                    }
                    JournalKeyboardMode.NUMBER -> {
                        JournalCompactNumericDock(
                            expanded = state.keyboardExpanded,
                            onToggleExpand = { viewModel.toggleKeyboardExpanded() },
                            onKey = { viewModel.applyCompactKey(it) },
                            onSwitchToTextMode = { viewModel.switchToTextMode() },
                            onConfirm = { state.activeRowId?.let { viewModel.confirmRowEdit(it) } }
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
    }
}

@Composable
private fun EditorTopBar(
    title: String,
    onTitleChange: (String) -> Unit,
    currency: MoneyUnit,
    onCurrencyToggle: () -> Unit,
    onCalculatorClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = JournalPaper,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            // --- Line 1 (Navigation Back + Calculation Title) ---
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
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(HighlighterPink.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = TajawalFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            textAlign = TextAlign.Center,
                            platformStyle = NoFontPadding
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(JournalInk),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        decorationBox = { innerTextField ->
                            if (title.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.editor_title_placeholder),
                                    fontFamily = TajawalFamily,
                                    fontSize = 17.sp,
                                    color = JournalMutedInk.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // Balance the Back Button for perfect horizontal centering of Title
                Spacer(modifier = Modifier.size(42.dp))
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
                        Text(
                            text = if (currency == MoneyUnit.DIRHAM) stringResource(R.string.currency_dirham) else stringResource(R.string.currency_rial),
                            fontFamily = TajawalFamily,
                            fontSize = 13.5.sp,
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

                // 2. Calculator Button (Center)
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
                    Text(
                        text = stringResource(R.string.calculator_title),
                        fontFamily = TajawalFamily,
                        fontSize = 13.5.sp,
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
                    Text(
                        text = stringResource(R.string.editor_save),
                        fontFamily = TajawalFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }
        }
    }
}

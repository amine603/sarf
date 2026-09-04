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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.cash.guide.ui.notebook.JournalAddRowButton
import com.cash.guide.ui.notebook.JournalCalculatorPopup
import com.cash.guide.ui.notebook.JournalCompactNumericDock
import com.cash.guide.ui.notebook.JournalEntryRow
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.JournalTextKeyboardDock
import com.cash.guide.ui.notebook.JournalTotalResultBand
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.ManropeFamily
import com.cash.guide.ui.notebook.MoneyBreakdownSheet
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
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
                onBreakdownClick = { viewModel.toggleBreakdownSheet(true) },
                onSaveClick = { viewModel.saveCalculation(onSuccess = onNavigateBack) },
                onBackClick = { viewModel.handleBackPress(onNavigateBack) }
            )

            // Ruled Paper Content
            JournalRuledDocument(
                listState = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Section Title spacing
                Spacer(modifier = Modifier.height(8.dp))

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

                // Total Result Band
                val primaryFormatted = com.cash.guide.domain.JournalLedgerManager.formatTotal(state.totalCentimes, state.currency)
                val currencySuffix = if (state.currency == MoneyUnit.DIRHAM) stringResource(R.string.currency_dirham) else stringResource(R.string.currency_rial)
                JournalTotalResultBand(
                    amount = primaryFormatted,
                    suffix = currencySuffix,
                    hasInvalidRows = state.hasInvalidRows,
                    canBreakdown = !state.hasInvalidRows && state.totalCentimes > 0,
                    onShowBreakdown = { viewModel.toggleBreakdownSheet(true) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Keyboard Dock
            if (state.keyboardExpanded) {
                when (state.keyboardMode) {
                    JournalKeyboardMode.TEXT -> {
                        JournalTextKeyboardDock(
                            language = state.keyboardLanguage,
                            shiftMode = state.shiftMode,
                            expanded = state.keyboardExpanded,
                            onToggleExpand = { viewModel.selectRowField(state.activeRowId ?: 1L, ActiveField.NONE) },
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
                            onToggleExpand = { viewModel.selectRowField(state.activeRowId ?: 1L, ActiveField.NONE) },
                            onKey = { viewModel.applyCompactKey(it) },
                            onSwitchToTextMode = { viewModel.switchToTextMode() }
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

        // Money Breakdown Sheet
        if (state.showBreakdownSheet) {
            MoneyBreakdownSheet(
                totalCentimes = state.totalCentimes,
                onDismiss = { viewModel.toggleBreakdownSheet(false) }
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
    onBreakdownClick: () -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button (48dp target)
            Box(
                modifier = Modifier
                    .size(48.dp)
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

            // Title Field
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = TextStyle(
                    fontFamily = PatrickHandFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk
                ),
                singleLine = true,
                cursorBrush = SolidColor(JournalInk),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = stringResource(R.string.editor_title_placeholder),
                            fontFamily = PatrickHandFamily,
                            fontSize = 20.sp,
                            color = JournalMutedInk.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            )

            // Currency Switcher badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(HighlighterYellow.copy(alpha = 0.4f))
                    .clickable(role = Role.Button, onClick = onCurrencyToggle)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currency == MoneyUnit.DIRHAM) stringResource(R.string.currency_dirham) else stringResource(R.string.currency_rial),
                    fontFamily = ManropeFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Banknote Breakdown Icon (48dp target)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onBreakdownClick),
                contentAlignment = Alignment.Center
            ) {
                HisabiSketchIcon(
                    symbol = HisabiSymbol.Wallet,
                    contentDescription = stringResource(R.string.editor_show_money_breakdown),
                    tint = JournalInk,
                    size = 20.dp
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Enregistrer Button
            Button(
                onClick = onSaveClick,
                modifier = Modifier.height(38.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HighlighterPink,
                    contentColor = JournalInk
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.editor_save),
                    fontFamily = ManropeFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

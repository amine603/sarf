package com.cash.guide.feature.cashregister

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.MoneyMath
import com.cash.guide.domain.MoneyPiece
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.notebook.ColorCoral
import com.cash.guide.ui.notebook.ColorOrange
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.JournalDockBg
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.rememberBanknoteImage
import com.cash.guide.ui.notebook.resolveJournalFont

private val ColorEmerald = Color(0xFF2E7D32)

@Composable
fun CashRegisterScreen(
    viewModel: CashRegisterViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    // Handle system back navigation: return to calculator first if in change return step
    BackHandler(enabled = true) {
        if (state.step == CashRegisterStep.CHANGE_RETURN) {
            viewModel.goToCalculator()
        } else {
            onNavigateBack()
        }
    }

    val currencySuffix = if (state.currencyUnit == MoneyUnit.DIRHAM) {
        stringResource(R.string.currency_dirham)
    } else {
        stringResource(R.string.currency_rial)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JournalPaper)
            .drawBehind {
                // 1. Subtle tactile paper grain / flecks
                val dotColor = JournalInk.copy(alpha = 0.022f)
                var px = 18f
                while (px < size.width) {
                    var py = 22f
                    while (py < size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = 0.9f,
                            center = Offset(px, py)
                        )
                        py += 64f
                    }
                    px += 48f
                }

                // 2. 29dp Horizontal Rules
                val rowHeightPx = JournalRuleSpacing.roundToPx().toFloat()
                var y = rowHeightPx
                while (y <= size.height) {
                    drawLine(
                        color = JournalRule.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.6.dp.toPx()
                    )
                    y += rowHeightPx
                }
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Line 1: Header bar strictly resting on ruled line (29dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(JournalRuleSpacing)
                            .clickable(
                                role = Role.Button,
                                onClick = {
                                    if (state.step == CashRegisterStep.CHANGE_RETURN) {
                                        viewModel.goToCalculator()
                                    } else {
                                        onNavigateBack()
                                    }
                                }
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Back,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = JournalInk,
                            size = 20.dp,
                            modifier = Modifier.offset(y = (-4.5).dp)
                        )
                    }

                    val titleText = if (state.step == CashRegisterStep.CALCULATOR) {
                        stringResource(R.string.cash_register_calc_title)
                    } else {
                        stringResource(R.string.cash_register_title)
                    }

                    Text(
                        text = titleText,
                        fontFamily = resolveJournalFont(titleText, isRtl),
                        fontSize = if (isRtl) 16.sp else 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.journalBaselineOnRule()
                    )
                }

                // Currency toggle chip
                Row(
                    modifier = Modifier
                        .offset(y = (-2).dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(JournalDockBg)
                        .border(BorderStroke(0.8.dp, JournalRule.copy(alpha = 0.8f)), RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button) { viewModel.toggleCurrency() }
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currencySuffix,
                        fontFamily = resolveJournalFont(currencySuffix, isRtl),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                    Text(
                        text = "⇅",
                        fontSize = 11.sp,
                        color = JournalMutedInk
                    )
                }
            }

            // Spacer to separate header from content
            Spacer(modifier = Modifier.height(10.dp))

            // Main content switcher
            when (state.step) {
                CashRegisterStep.CALCULATOR -> {
                    CashRegisterCalculatorContent(
                        state = state,
                        currencySuffix = currencySuffix,
                        isRtl = isRtl,
                        onKeyClick = { viewModel.applyCalculatorKey(it) },
                        onNext = { viewModel.goToChangeReturn() },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
                CashRegisterStep.CHANGE_RETURN -> {
                    CashRegisterChangeReturnContent(
                        state = state,
                        currencySuffix = currencySuffix,
                        isRtl = isRtl,
                        onPurchaseChange = { viewModel.setPurchaseText(it) },
                        onReceivedChange = { viewModel.setReceivedText(it) },
                        onPresetSelect = { viewModel.selectPresetReceived(it) },
                        onBackToCalc = { viewModel.goToCalculator() },
                        onNextClient = { viewModel.clear() },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 1: CALCULATOR VIEW
// -----------------------------------------------------------------------------

@Composable
private fun CashRegisterCalculatorContent(
    state: CashRegisterUiState,
    currencySuffix: String,
    isRtl: Boolean,
    onKeyClick: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .padding(horizontal = 14.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP 1/4: Authentic Ruled Ledger Calculation Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(JournalPaper)
                .border(BorderStroke(1.dp, JournalRule.copy(alpha = 0.85f)), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header of the display: icon + label + clear button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🧮", fontSize = 13.sp)
                        val hintHeader = stringResource(R.string.cash_register_calc_title)
                        Text(
                            text = hintHeader,
                            fontFamily = resolveJournalFont(hintHeader, isRtl),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JournalMutedInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }

                    if (state.calcExpression.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.cash_register_clear_input),
                            fontFamily = resolveJournalFont(stringResource(R.string.cash_register_clear_input), isRtl),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorCoral,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ColorCoral.copy(alpha = 0.12f))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onKeyClick("C")
                                }
                                .padding(horizontal = 8.dp, vertical = 2.5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Expression line
                val expressionText = state.calcExpression.ifBlank {
                    stringResource(R.string.cash_register_calc_hint)
                }
                Text(
                    text = expressionText,
                    fontFamily = if (state.calcExpression.isBlank()) resolveJournalFont(expressionText, isRtl) else PatrickHandFamily,
                    fontSize = if (state.calcExpression.isBlank()) 13.sp else 24.sp,
                    fontWeight = if (state.calcExpression.isBlank()) FontWeight.Normal else FontWeight.Medium,
                    color = if (state.calcExpression.isBlank()) JournalMutedInk.copy(alpha = 0.60f) else JournalWritingInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(
                    color = JournalRule.copy(alpha = 0.35f),
                    thickness = 0.8.dp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Evaluated total line
                val displayTotal = state.purchaseText.ifBlank { "0" }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "= $displayTotal",
                            fontFamily = PatrickHandFamily,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorOrange
                        )
                        Text(
                            text = currencySuffix,
                            fontFamily = resolveJournalFont(currencySuffix, isRtl),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    // Secondary currency representation
                    if (state.purchaseCentimes > 0L) {
                        val secondaryValue = if (state.currencyUnit == MoneyUnit.DIRHAM) {
                            val rials = MoneyMath.fromCentimes(state.purchaseCentimes, MoneyUnit.RIAL)
                            "$rials ${stringResource(R.string.currency_rial)}"
                        } else {
                            val dh = MoneyMath.fromCentimes(state.purchaseCentimes, MoneyUnit.DIRHAM)
                            "$dh ${stringResource(R.string.currency_dirham)}"
                        }
                        Text(
                            text = "($secondaryValue)",
                            fontFamily = resolveJournalFont(secondaryValue, isRtl),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = JournalMutedInk,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // BOTTOM: Keypad + Action button docked together
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: C, ÷, ×, ⌫
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcKeyCell(
                    text = "C",
                    backgroundColor = ColorCoral.copy(alpha = 0.14f),
                    textColor = ColorCoral,
                    borderColor = ColorCoral.copy(alpha = 0.35f),
                    onClick = { onKeyClick("C") }
                )
                CalcKeyCell(
                    text = "÷",
                    backgroundColor = JournalDockBg,
                    textColor = JournalWritingInk,
                    onClick = { onKeyClick("÷") }
                )
                CalcKeyCell(
                    text = "×",
                    backgroundColor = JournalDockBg,
                    textColor = JournalWritingInk,
                    onClick = { onKeyClick("×") }
                )
                CalcKeyCell(
                    text = "⌫",
                    backgroundColor = JournalDockBg,
                    textColor = JournalMutedInk,
                    onClick = { onKeyClick("⌫") }
                )
            }

            // Row 2: 7, 8, 9, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcKeyCell(text = "7", onClick = { onKeyClick("7") })
                CalcKeyCell(text = "8", onClick = { onKeyClick("8") })
                CalcKeyCell(text = "9", onClick = { onKeyClick("9") })
                CalcKeyCell(
                    text = "−",
                    backgroundColor = JournalDockBg,
                    textColor = JournalWritingInk,
                    onClick = { onKeyClick("−") }
                )
            }

            // Row 3: 4, 5, 6, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcKeyCell(text = "4", onClick = { onKeyClick("4") })
                CalcKeyCell(text = "5", onClick = { onKeyClick("5") })
                CalcKeyCell(text = "6", onClick = { onKeyClick("6") })
                CalcKeyCell(
                    text = "+",
                    backgroundColor = JournalDockBg,
                    textColor = JournalWritingInk,
                    onClick = { onKeyClick("+") }
                )
            }

            // Row 4: 1, 2, 3, .
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcKeyCell(text = "1", onClick = { onKeyClick("1") })
                CalcKeyCell(text = "2", onClick = { onKeyClick("2") })
                CalcKeyCell(text = "3", onClick = { onKeyClick("3") })
                CalcKeyCell(
                    text = ".",
                    backgroundColor = JournalDockBg,
                    textColor = JournalWritingInk,
                    onClick = { onKeyClick(".") }
                )
            }

            // Row 5: 0 (weight 2f), 00 (weight 1f), = (weight 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcKeyCell(
                    text = "0",
                    weight = 2f,
                    onClick = { onKeyClick("0") }
                )
                CalcKeyCell(
                    text = "00",
                    weight = 1f,
                    onClick = {
                        onKeyClick("0")
                        onKeyClick("0")
                    }
                )
                CalcKeyCell(
                    text = "=",
                    weight = 1f,
                    backgroundColor = HighlighterPink.copy(alpha = 0.55f),
                    textColor = JournalInk,
                    borderColor = HighlighterPink,
                    onClick = { onKeyClick("=") }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Primary button to advance to Change Return
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNext()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HighlighterPink.copy(alpha = 0.90f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                val buttonText = stringResource(R.string.cash_register_go_to_change)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontFamily = resolveJournalFont(buttonText, isRtl),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.CalcKeyCell(
    text: String,
    weight: Float = 1f,
    backgroundColor: Color = JournalPaper,
    textColor: Color = JournalInk,
    borderColor: Color = JournalRule.copy(alpha = 0.70f),
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .weight(weight)
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(BorderStroke(0.85.dp, borderColor), RoundedCornerShape(10.dp))
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = PatrickHandFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            style = TextStyle(platformStyle = NoFontPadding)
        )
    }
}

// -----------------------------------------------------------------------------
// STEP 2: CHANGE RETURN VIEW (CAISSE & RENDU + TSTIFA)
// -----------------------------------------------------------------------------

@Composable
private fun CashRegisterChangeReturnContent(
    state: CashRegisterUiState,
    currencySuffix: String,
    isRtl: Boolean,
    onPurchaseChange: (String) -> Unit,
    onReceivedChange: (String) -> Unit,
    onPresetSelect: (Long) -> Unit,
    onBackToCalc: () -> Unit,
    onNextClient: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // CARD 1: Total des achats (Directly editable input field + Calculator shortcut)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(JournalPaper)
                .border(BorderStroke(0.85.dp, JournalRule.copy(alpha = 0.75f)), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            val labelTotal = stringResource(R.string.cash_register_purchase_total)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🛒", fontSize = 15.sp)
                    Text(
                        text = labelTotal,
                        fontFamily = resolveJournalFont(labelTotal, isRtl),
                        fontSize = if (isRtl) 14.sp else 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (state.purchaseText.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.cash_register_clear_input),
                            fontFamily = resolveJournalFont(stringResource(R.string.cash_register_clear_input), isRtl),
                            fontSize = 12.sp,
                            color = ColorCoral,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onPurchaseChange("") }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Link back to calculator
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HighlighterYellow.copy(alpha = 0.35f))
                            .clickable(role = Role.Button, onClick = onBackToCalc)
                            .padding(horizontal = 7.dp, vertical = 2.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cash_register_back_to_calc),
                            fontFamily = resolveJournalFont(stringResource(R.string.cash_register_back_to_calc), isRtl),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalWritingInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                        Text("🧮", fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Purchase input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JournalDockBg)
                    .border(BorderStroke(0.6.dp, JournalRule.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value = state.purchaseText,
                    onValueChange = onPurchaseChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontFamily = PatrickHandFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isPurchaseValid) JournalWritingInk else ColorCoral,
                        platformStyle = NoFontPadding
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    cursorBrush = SolidColor(JournalInk),
                    decorationBox = { innerTextField ->
                        if (state.purchaseText.isEmpty()) {
                            Text(
                                text = "0.00",
                                fontFamily = PatrickHandFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalMutedInk.copy(alpha = 0.45f),
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }
                        innerTextField()
                    }
                )

                Text(
                    text = currencySuffix,
                    fontFamily = resolveJournalFont(currencySuffix, isRtl),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalMutedInk
                )
            }
        }

        // CARD 2: Amount Received from Customer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(JournalPaper)
                .border(BorderStroke(0.85.dp, JournalRule.copy(alpha = 0.75f)), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            val labelReceived = stringResource(R.string.cash_register_amount_received)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("💵", fontSize = 15.sp)
                    Text(
                        text = labelReceived,
                        fontFamily = resolveJournalFont(labelReceived, isRtl),
                        fontSize = if (isRtl) 14.sp else 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }

                if (state.receivedText.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.cash_register_clear_input),
                        fontFamily = resolveJournalFont(stringResource(R.string.cash_register_clear_input), isRtl),
                        fontSize = 12.sp,
                        color = ColorCoral,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onReceivedChange("") }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Received input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JournalDockBg)
                    .border(BorderStroke(0.6.dp, JournalRule.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value = state.receivedText,
                    onValueChange = onReceivedChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontFamily = PatrickHandFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isReceivedValid) JournalWritingInk else ColorCoral,
                        platformStyle = NoFontPadding
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    cursorBrush = SolidColor(JournalInk),
                    decorationBox = { innerTextField ->
                        if (state.receivedText.isEmpty()) {
                            Text(
                                text = "0.00",
                                fontFamily = PatrickHandFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalMutedInk.copy(alpha = 0.45f),
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }
                        innerTextField()
                    }
                )

                Text(
                    text = currencySuffix,
                    fontFamily = resolveJournalFont(currencySuffix, isRtl),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalMutedInk
                )
            }

            Spacer(Modifier.height(8.dp))

            // Quick Note Chips: [20 DH] [50 DH] [100 DH] [200 DH]
            val presetNotes = listOf(20L, 50L, 100L, 200L)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetNotes.forEach { noteDh ->
                    val chipText = if (state.currencyUnit == MoneyUnit.DIRHAM) {
                        "$noteDh DH"
                    } else {
                        "${noteDh * 20} ريال"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(HighlighterYellow.copy(alpha = 0.25f))
                            .border(
                                BorderStroke(0.8.dp, JournalRule.copy(alpha = 0.65f)),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onPresetSelect(noteDh) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chipText,
                            fontFamily = PatrickHandFamily,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalWritingInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }
                }
            }
        }

        // CARD 3: Change Due Status Banner
        if (state.changeCentimes > 0L) {
            val changeDh = MoneyMath.fromCentimes(state.changeCentimes, MoneyUnit.DIRHAM)
            val changeRial = MoneyMath.fromCentimes(state.changeCentimes, MoneyUnit.RIAL)
            val changeFormatted = if (state.currencyUnit == MoneyUnit.DIRHAM) changeDh else changeRial
            val changeLabel = stringResource(R.string.cash_register_change_due)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HighlighterYellow.copy(alpha = 0.35f))
                    .border(
                        BorderStroke(1.dp, HighlighterPink.copy(alpha = 0.65f)),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = changeLabel,
                    fontFamily = resolveJournalFont(changeLabel, isRtl),
                    fontSize = if (isRtl) 14.sp else 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    style = TextStyle(platformStyle = NoFontPadding)
                )

                Spacer(Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = changeFormatted,
                        fontFamily = PatrickHandFamily,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorOrange
                    )
                    Text(
                        text = currencySuffix,
                        fontFamily = resolveJournalFont(currencySuffix, isRtl),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                val secondaryText = if (state.currencyUnit == MoneyUnit.DIRHAM) {
                    "= $changeRial ${stringResource(R.string.currency_rial)}"
                } else {
                    "= $changeDh ${stringResource(R.string.currency_dirham)}"
                }
                Text(
                    text = secondaryText,
                    fontFamily = resolveJournalFont(secondaryText, isRtl),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = JournalMutedInk
                )
            }
        } else if (state.isExactAmount) {
            val exactText = stringResource(R.string.cash_register_exact_amount)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ColorEmerald.copy(alpha = 0.14f))
                    .border(BorderStroke(0.85.dp, ColorEmerald.copy(alpha = 0.45f)), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓ $exactText",
                    fontFamily = resolveJournalFont(exactText, isRtl),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorEmerald
                )
            }
        } else if (state.isInsufficient && state.shortageCentimes > 0L) {
            val shortageDh = MoneyMath.fromCentimes(state.shortageCentimes, state.currencyUnit)
            val shortageMsg = stringResource(R.string.cash_register_insufficient, "$shortageDh $currencySuffix")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ColorCoral.copy(alpha = 0.12f))
                    .border(BorderStroke(0.85.dp, ColorCoral.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚠️ $shortageMsg",
                    fontFamily = resolveJournalFont(shortageMsg, isRtl),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorCoral
                )
            }
        }

        // CARD 4: Tstifa dial l-wra9 o l-coins (Clean Banknotes & Coins Board)
        if (state.pieces.isNotEmpty()) {
            CashRegisterDenominationsBoard(
                pieces = state.pieces,
                isRtl = isRtl
            )
        }

        Spacer(Modifier.height(4.dp))

        // Bottom bar: Client suivant button
        Button(
            onClick = onNextClient,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HighlighterPink.copy(alpha = 0.90f),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            val nextText = stringResource(R.string.cash_register_next_client)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("↺", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = nextText,
                    fontFamily = resolveJournalFont(nextText, isRtl),
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(platformStyle = NoFontPadding)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TSTIFA BOARD: Clean Banknotes and Coins without Shadows or Redundant Labels
// -----------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CashRegisterDenominationsBoard(
    pieces: List<MoneyPiece>,
    isRtl: Boolean,
    modifier: Modifier = Modifier
) {
    val banknotes = remember(pieces) { pieces.filter { it.denomination.valueCentimes >= 2_000L } }
    val coins = remember(pieces) { pieces.filter { it.denomination.valueCentimes < 2_000L } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(JournalPaper)
            .border(BorderStroke(0.85.dp, JournalRule.copy(alpha = 0.70f)), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        // Section: Banknotes (L-Wra9)
        if (banknotes.isNotEmpty()) {
            val headingBanknotes = stringResource(R.string.cash_register_heading_banknotes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("💵", fontSize = 13.sp)
                Text(
                    text = headingBanknotes,
                    fontFamily = resolveJournalFont(headingBanknotes, isRtl),
                    color = JournalWritingInk,
                    fontSize = if (isRtl) 13.sp else 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(platformStyle = NoFontPadding)
                )
            }

            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                banknotes.forEach { piece ->
                    BanknoteDisplayItem(piece = piece)
                }
            }
        }

        if (banknotes.isNotEmpty() && coins.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = JournalRule.copy(alpha = 0.35f),
                thickness = 0.8.dp
            )
        }

        // Section: Coins (L-Coins / D-Drahem)
        if (coins.isNotEmpty()) {
            val headingCoins = stringResource(R.string.cash_register_heading_coins)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("🪙", fontSize = 13.sp)
                Text(
                    text = headingCoins,
                    fontFamily = resolveJournalFont(headingCoins, isRtl),
                    color = JournalWritingInk,
                    fontSize = if (isRtl) 13.sp else 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(platformStyle = NoFontPadding)
                )
            }

            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                coins.forEach { piece ->
                    CoinDisplayItem(piece = piece)
                }
            }
        }
    }
}

/**
 * Clean Banknote representation:
 * - Real BAM banknote transparent image
 * - NO shadow, NO artificial black halo underneath
 * - Proportional ~1.7:1 aspect ratio
 * - Prominent, bold count badge (e.g. 2×) in top corner if count > 1
 * - No redundant denomination text
 */
@Composable
private fun BanknoteDisplayItem(
    piece: MoneyPiece,
    modifier: Modifier = Modifier
) {
    val bitmap = rememberBanknoteImage(piece.denomination.assetPath)
    val count = piece.count

    Box(
        modifier = modifier
            .width(120.dp)
            .height(70.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(3.dp))
            )
        }

        // Prominent Count Badge (e.g. "2×")
        if (count > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-5).dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(HighlighterPink)
                    .border(BorderStroke(1.5.dp, JournalPaper), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${count}×",
                    fontFamily = PatrickHandFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(platformStyle = NoFontPadding)
                )
            }
        }
    }
}

/**
 * Clean Coin representation:
 * - Real BAM coin transparent circular image
 * - Significantly enlarged diameters for clear legibility
 * - Prominent count badge (e.g. 2×) in top corner if count > 1
 * - No redundant denomination text
 */
@Composable
private fun CoinDisplayItem(
    piece: MoneyPiece,
    modifier: Modifier = Modifier
) {
    val bitmap = rememberBanknoteImage(piece.denomination.assetPath)
    val count = piece.count
    val sizeDp = when (piece.denomination.valueCentimes) {
        1_000L -> 68.dp // 10 DH (bimetallic, large)
        500L -> 64.dp   // 5 DH (bimetallic)
        200L -> 60.dp   // 2 DH
        100L -> 56.dp   // 1 DH
        50L -> 52.dp    // 50c
        20L -> 48.dp    // 20c
        10L -> 44.dp    // 10c
        else -> 50.dp
    }

    Box(
        modifier = modifier.size(sizeDp + 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(sizeDp)
                    .clip(CircleShape)
            )
        }

        // Prominent Count Badge (e.g. "2×")
        if (count > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-2).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HighlighterYellow)
                    .border(BorderStroke(1.dp, JournalPaper), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "${count}×",
                    fontFamily = PatrickHandFamily,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalWritingInk,
                    style = TextStyle(platformStyle = NoFontPadding)
                )
            }
        }
    }
}

package com.cash.guide.feature.cashregister

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.HighlighterGreen
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.JournalDockBg
import com.cash.guide.ui.notebook.JournalDoubleUnderline
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalKeyDigitStyle
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.JournalDenominationsBoard
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.journalOperatorDab
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
// STEP 1: CALCULATOR VIEW (AUTHENTIC JOURNAL KEYPAD & LEDGER DISPLAY)
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
            .padding(bottom = 8.dp)
    ) {
        // TOP: Ruled Ledger Calculation Display (Encadred with Theme Ink Outline, Fixed & Grand)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.2.dp, JournalWritingInk), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Header of the display: sketch icon + label + clear button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Calculator,
                            contentDescription = null,
                            tint = JournalInk,
                            size = 17.dp
                        )
                        val hintHeader = stringResource(R.string.cash_register_calc_title)
                        Text(
                            text = hintHeader,
                            fontFamily = resolveJournalFont(hintHeader, isRtl),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JournalMutedInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }

                    if (state.calcExpression.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ColorCoral.copy(alpha = 0.12f))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onKeyClick("C")
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HisabiSketchIcon(
                                symbol = HisabiSymbol.Close,
                                contentDescription = null,
                                tint = ColorCoral,
                                size = 11.dp
                            )
                            val clearText = stringResource(R.string.cash_register_clear_input)
                            Text(
                                text = clearText,
                                fontFamily = resolveJournalFont(clearText, isRtl),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorCoral,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Expression line directly on paper rules
                val expressionText = state.calcExpression.ifBlank {
                    stringResource(R.string.cash_register_calc_hint)
                }
                Text(
                    text = expressionText,
                    fontFamily = if (state.calcExpression.isBlank()) resolveJournalFont(expressionText, isRtl) else PatrickHandFamily,
                    fontSize = if (state.calcExpression.isBlank()) 15.sp else 30.sp,
                    fontWeight = if (state.calcExpression.isBlank()) FontWeight.Normal else FontWeight.Medium,
                    color = if (state.calcExpression.isBlank()) JournalMutedInk.copy(alpha = 0.50f) else JournalWritingInk,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(
                    color = JournalRule.copy(alpha = 0.40f),
                    thickness = 0.8.dp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Evaluated total line with JournalDoubleUnderline
                val displayTotal = state.purchaseText.ifBlank { "0" }
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorOrange
                            )
                            Text(
                                text = currencySuffix,
                                fontFamily = resolveJournalFont(currencySuffix, isRtl),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalInk,
                                modifier = Modifier.padding(bottom = 4.dp)
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = JournalMutedInk,
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }
                    }

                    JournalDoubleUnderline(
                        modifier = Modifier.padding(top = 2.dp),
                        color = HighlighterPink,
                        width = 180.dp
                    )
                }
            }
        }

        // 2 ruled lines gap between the calculation cadre and the keypad cadre
        Spacer(modifier = Modifier.height(JournalRuleSpacing * 2))

        // BOTTOM: Docked Keyboard + Journal Action button
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Authentic In-App Keyboard with clean paper background (hiding notebook blue lines) and theme ink outline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(JournalPaper)
                    .border(BorderStroke(1.2.dp, JournalWritingInk), RoundedCornerShape(12.dp))
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .drawBehind {
                                val strokeW = 0.65.dp.toPx()
                                val gridLineColor = JournalRule.copy(alpha = 0.45f)

                                // 4 internal horizontal grid dividers between the 5 rows
                                val rowH = size.height / 5f
                                for (i in 1..4) {
                                    val y = rowH * i
                                    drawLine(
                                        color = gridLineColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeW
                                    )
                                }

                                // 3 vertical dividers for rows 1 to 4
                                val colW = size.width / 4f
                                for (i in 1..3) {
                                    val x = colW * i
                                    drawLine(
                                        color = gridLineColor,
                                        start = Offset(x, 0f),
                                        end = Offset(x, rowH * 4),
                                        strokeWidth = strokeW
                                    )
                                }

                                // Row 5: 0 (span 2), 00 (span 1), = (span 1)
                                // Divider between 0 and 00 (x = 2 * colW)
                                drawLine(
                                    color = gridLineColor,
                                    start = Offset(colW * 2, rowH * 4),
                                    end = Offset(colW * 2, size.height),
                                    strokeWidth = strokeW
                                )
                                // Divider between 00 and = (x = 3 * colW)
                                drawLine(
                                    color = gridLineColor,
                                    start = Offset(colW * 3, rowH * 4),
                                    end = Offset(colW * 3, size.height),
                                    strokeWidth = strokeW
                                )
                            }
                    ) {
                        // Row 1: C, ÷, ×, ⌫
                        Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            JournalCalcKeyCell(
                                text = "C",
                                operatorDabColor = ColorCoral.copy(alpha = 0.60f),
                                onClick = { onKeyClick("C") },
                                modifier = Modifier.weight(1f)
                            )
                            JournalCalcKeyCell(
                                text = "÷",
                                operatorDabColor = HighlighterBlue.copy(alpha = 0.70f),
                                onClick = { onKeyClick("÷") },
                                modifier = Modifier.weight(1f)
                            )
                            JournalCalcKeyCell(
                                text = "×",
                                operatorDabColor = HighlighterGreen.copy(alpha = 0.70f),
                                onClick = { onKeyClick("×") },
                                modifier = Modifier.weight(1f)
                            )
                            JournalCalcBackspaceKeyCell(
                                onClick = { onKeyClick("⌫") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2: 7, 8, 9, −
                        Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            JournalCalcKeyCell(text = "7", onClick = { onKeyClick("7") }, modifier = Modifier.weight(1f))
                            JournalCalcKeyCell(text = "8", onClick = { onKeyClick("8") }, modifier = Modifier.weight(1f))
                            JournalCalcKeyCell(text = "9", onClick = { onKeyClick("9") }, modifier = Modifier.weight(1f))
                            JournalCalcKeyCell(
                                text = "−",
                                operatorDabColor = HighlighterYellow.copy(alpha = 0.70f),
                                onClick = { onKeyClick("−") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 3: 4, 5, 6, +
                        Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            JournalCalcKeyCell(text = "4", onClick = { onKeyClick("4") }, modifier = Modifier.weight(1f))
                            JournalCalcKeyCell(text = "5", onClick = { onKeyClick("5") }, modifier = Modifier.weight(1f))
                            JournalCalcKeyCell(text = "6", onClick = { onKeyClick("6") }, modifier = Modifier.weight(1f))
                            JournalCalcKeyCell(
                                text = "+",
                                operatorDabColor = HighlighterPink.copy(alpha = 0.70f),
                                onClick = { onKeyClick("+") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 4: 1, 2, 3, .
                        Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            JournalCalcKeyCell(text = "1", onClick = { onKeyClick("1") }, modifier = Modifier.weight(1f))
                            JournalCalcKeyCell(text = "2", onClick = { onKeyClick("2") }, modifier = Modifier.weight(1f))
                            JournalCalcKeyCell(text = "3", onClick = { onKeyClick("3") }, modifier = Modifier.weight(1f))
                            JournalCalcKeyCell(text = ".", onClick = { onKeyClick(".") }, modifier = Modifier.weight(1f))
                        }

                        // Row 5: 0 (weight 2f), 00 (weight 1f), = (weight 1f)
                        Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            JournalCalcKeyCell(text = "0", onClick = { onKeyClick("0") }, modifier = Modifier.weight(2f))
                            JournalCalcKeyCell(
                                text = "00",
                                onClick = {
                                    onKeyClick("0")
                                    onKeyClick("0")
                                },
                                modifier = Modifier.weight(1f)
                            )
                            JournalCalcKeyCell(
                                text = "=",
                                operatorDabColor = HighlighterPink.copy(alpha = 0.85f),
                                onClick = { onKeyClick("=") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Primary Journal button to advance to Change Return
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HighlighterPink.copy(alpha = 0.55f))
                    .border(
                        BorderStroke(0.85.dp, HighlighterPink.copy(alpha = 0.85f)),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNext()
                    },
                contentAlignment = Alignment.Center
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
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalCalcKeyCell(
    text: String,
    modifier: Modifier = Modifier,
    operatorDabColor: Color? = null,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dabModifier = if (operatorDabColor != null) {
        Modifier.journalOperatorDab(operatorDabColor, alpha = 0.75f, widthDp = 36.dp, heightDp = 22.dp)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .then(dabModifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = JournalKeyDigitStyle.copy(
                fontSize = if (text == "00") 20.sp else 23.sp,
                fontWeight = if (operatorDabColor != null) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

@Composable
private fun JournalCalcBackspaceKeyCell(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp, 17.dp)) {
            val strokeW = 1.2.dp.toPx()
            val ink = JournalInk.copy(alpha = 0.9f)
            val w = size.width
            val h = size.height

            // Pointed tag shape
            val p = Path().apply {
                moveTo(w * 0.32f, 0f)
                lineTo(w, 0f)
                lineTo(w, h)
                lineTo(w * 0.32f, h)
                lineTo(0f, h / 2f)
                close()
            }
            drawPath(p, color = ink, style = Stroke(width = strokeW))

            // Inner cross '×'
            val cx = w * 0.64f
            val cy = h / 2f
            val d = 3.5.dp.toPx()
            drawLine(ink, Offset(cx - d, cy - d), Offset(cx + d, cy + d), strokeW, StrokeCap.Round)
            drawLine(ink, Offset(cx + d, cy - d), Offset(cx - d, cy + d), strokeW, StrokeCap.Round)
        }
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
        // SECTION 1: Total des achats (sitting directly on notebook ruled paper)
        val labelTotal = stringResource(R.string.cash_register_purchase_total)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(JournalRuleSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HisabiSketchIcon(
                    symbol = HisabiSymbol.Page,
                    contentDescription = null,
                    tint = JournalInk,
                    size = 16.dp
                )
                Text(
                    text = labelTotal,
                    fontFamily = resolveJournalFont(labelTotal, isRtl),
                    fontSize = if (isRtl) 14.sp else 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule()
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
                        .background(HighlighterYellow.copy(alpha = 0.40f))
                        .clickable(role = Role.Button, onClick = onBackToCalc)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Calculator,
                        contentDescription = null,
                        tint = JournalWritingInk,
                        size = 13.dp
                    )
                    Text(
                        text = stringResource(R.string.cash_register_back_to_calc),
                        fontFamily = resolveJournalFont(stringResource(R.string.cash_register_back_to_calc), isRtl),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalWritingInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }
        }

        // Purchase input row directly resting on ruled line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BasicTextField(
                value = state.purchaseText,
                onValueChange = onPurchaseChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontFamily = PatrickHandFamily,
                    fontSize = 28.sp,
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
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalMutedInk.copy(alpha = 0.35f),
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }
                    innerTextField()
                }
            )

            Text(
                text = currencySuffix,
                fontFamily = resolveJournalFont(currencySuffix, isRtl),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = JournalMutedInk
            )
        }

        // Dividing line between Total des achats and Montant reçu du client (clearly visible on ruled paper)
        HorizontalDivider(
            color = JournalWritingInk.copy(alpha = 0.38f),
            thickness = 1.2.dp,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        // SECTION 2: Montant reçu du client
        val labelReceived = stringResource(R.string.cash_register_amount_received)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(JournalRuleSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HisabiSketchIcon(
                    symbol = HisabiSymbol.Wallet,
                    contentDescription = null,
                    tint = JournalInk,
                    size = 16.dp
                )
                Text(
                    text = labelReceived,
                    fontFamily = resolveJournalFont(labelReceived, isRtl),
                    fontSize = if (isRtl) 14.sp else 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule()
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

        // Received input row directly resting on ruled line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BasicTextField(
                value = state.receivedText,
                onValueChange = onReceivedChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontFamily = PatrickHandFamily,
                    fontSize = 28.sp,
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
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalMutedInk.copy(alpha = 0.35f),
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }
                    innerTextField()
                }
            )

            Text(
                text = currencySuffix,
                fontFamily = resolveJournalFont(currencySuffix, isRtl),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = JournalMutedInk
            )
        }

        // Preset Banknote Chips: [20 DH] [50 DH] [100 DH] [200 DH]
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
                        .background(HighlighterYellow.copy(alpha = 0.30f))
                        .border(
                            BorderStroke(0.7.dp, JournalRule.copy(alpha = 0.55f)),
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

        // CHANGE DUE RESULT BAND (sitting directly on ruled lines)
        if (state.changeCentimes > 0L) {
            val changeDh = MoneyMath.fromCentimes(state.changeCentimes, MoneyUnit.DIRHAM)
            val changeRial = MoneyMath.fromCentimes(state.changeCentimes, MoneyUnit.RIAL)
            val changeFormatted = if (state.currencyUnit == MoneyUnit.DIRHAM) changeDh else changeRial
            val changeLabel = stringResource(R.string.cash_register_change_due)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = changeLabel,
                    fontFamily = resolveJournalFont(changeLabel, isRtl),
                    fontSize = if (isRtl) 14.5.sp else 15.sp,
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
                        text = "= $changeFormatted",
                        fontFamily = PatrickHandFamily,
                        fontSize = 34.sp,
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

                JournalDoubleUnderline(
                    modifier = Modifier.padding(top = 2.dp),
                    color = HighlighterPink,
                    width = 170.dp
                )

                Spacer(Modifier.height(3.dp))

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HisabiSketchIcon(
                    symbol = HisabiSymbol.Check,
                    contentDescription = null,
                    tint = ColorEmerald,
                    size = 17.dp
                )
                Text(
                    text = exactText,
                    fontFamily = resolveJournalFont(exactText, isRtl),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorEmerald,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule()
                )
            }
        } else if (state.isInsufficient && state.shortageCentimes > 0L) {
            val shortageDh = MoneyMath.fromCentimes(state.shortageCentimes, state.currencyUnit)
            val shortageMsg = stringResource(R.string.cash_register_insufficient, "$shortageDh $currencySuffix")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HisabiSketchIcon(
                    symbol = HisabiSymbol.Exclamation,
                    contentDescription = null,
                    tint = ColorCoral,
                    size = 17.dp
                )
                Text(
                    text = shortageMsg,
                    fontFamily = resolveJournalFont(shortageMsg, isRtl),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorCoral,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule()
                )
            }
        }

        // TSTIFA TRAY: Banknotes & Coins Board
        if (state.pieces.isNotEmpty()) {
            JournalDenominationsBoard(
                pieces = state.pieces,
                isRtl = isRtl
            )
        }

        Spacer(Modifier.height(4.dp))

        // Bottom bar: Client suivant action button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(HighlighterPink.copy(alpha = 0.55f))
                .border(
                    BorderStroke(0.85.dp, HighlighterPink.copy(alpha = 0.85f)),
                    RoundedCornerShape(12.dp)
                )
                .clickable(role = Role.Button) {
                    onNextClient()
                },
            contentAlignment = Alignment.Center
        ) {
            val nextText = stringResource(R.string.cash_register_next_client)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("↺", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JournalInk)
                Text(
                    text = nextText,
                    fontFamily = resolveJournalFont(nextText, isRtl),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    style = TextStyle(platformStyle = NoFontPadding)
                )
            }
        }
    }
}

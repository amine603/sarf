package com.cash.guide.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.res.stringResource
import com.cash.guide.R
import com.cash.guide.ui.components.AdMobBanner
import com.cash.guide.ui.components.rememberCalculatorKeySound
import com.cash.guide.ui.components.MoneyAmountText
import com.cash.guide.ui.components.rememberIsOnline
import com.cash.guide.ui.theme.SarfGreenStrong
import com.cash.guide.ui.theme.SarfLineAlt
import com.cash.guide.utils.ExpressionMoneyCalculator
import java.util.Locale

@Composable
fun CalculatorTabContent(
    expression: TextFieldValue,
    onExpressionChange: (TextFieldValue) -> Unit,
    lastCalculatorResultCents: Int?,
    onLastCalculatorResultCentsChange: (Int?) -> Unit,
    onSendToSarf: () -> Unit,
    currencyCode: String = "MAD",
    modifier: Modifier = Modifier
) {
    val playKeySound = rememberCalculatorKeySound()
    val lastExpression = remember(expression.text, lastCalculatorResultCents) {
        // Best-effort: when we have a stored result, show the current expression as "done".
        if (lastCalculatorResultCents != null && expression.text.isNotBlank()) expression.text.trim() else null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val decimalSeparator = rememberDecimalSeparator()
        val isOnline = rememberIsOnline()

        // Show ONLY the AdMob banner when online. When offline: show nothing (no "Calculator" title).
        if (isOnline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                AdMobBanner()
            }
        }

        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        val borderColor = if (isDark) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.outline

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = borderColor, shape = MaterialTheme.shapes.extraLarge),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current
                var hasFocus by remember { mutableStateOf(false) }
                var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                val primary = MaterialTheme.colorScheme.primary
                val selectionColors = remember(primary) {
                    TextSelectionColors(
                        handleColor = primary,
                        backgroundColor = primary.copy(alpha = 0.22f)
                    )
                }

                // Keep the expression/editor LTR even in Arabic RTL mode (numbers + cursor feel natural).
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Ltr,
                    LocalTextSelectionColors provides selectionColors
                ) {
                    BasicTextField(
                        value = expression,
                        onValueChange = { newValue ->
                            // allow cursor/selection moves; ignore text edits coming from the IME
                            if (newValue.text == expression.text) onExpressionChange(newValue)
                        },
                        readOnly = true,
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        onTextLayout = { layoutResult = it },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                            textAlign = TextAlign.Start
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                hasFocus = it.isFocused
                                if (it.isFocused) keyboardController?.hide()
                            }
                            // Draw a visible caret on top of the text itself (no overlay composable => taps still work).
                            .drawWithContent {
                                drawContent()
                                val lr = layoutResult
                                if (hasFocus && lr != null) {
                                    val caretIndex = expression.selection.end.coerceIn(0, expression.text.length)
                                    val rect = lr.getCursorRect(caretIndex)
                                    drawLine(
                                        color = primary,
                                        start = androidx.compose.ui.geometry.Offset(rect.left, rect.top),
                                        end = androidx.compose.ui.geometry.Offset(rect.left, rect.bottom),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                            },
                        visualTransformation = OperatorColorTransformation(
                            operatorColor = MaterialTheme.colorScheme.primary
                        ),
                        decorationBox = { inner ->
                            // Show " = " hint when last result exists
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                inner()
                                if (lastExpression != null) {
                                    Text(
                                        text = " =",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    )
                                }
                            }
                        }
                    )
                }

                Spacer(Modifier.height(10.dp))
                MoneyAmountText(
                    cents = lastCalculatorResultCents ?: 0,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                    currencyCode = currencyCode
                )
            }
        }

        // "Send to SARF" (only when we have a valid stored result)
        if (lastCalculatorResultCents != null) {
            OutlinedButton(
                onClick = onSendToSarf,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, SarfLineAlt),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
            ) {
                Text(stringResource(R.string.send_to_sarf), fontWeight = FontWeight.Bold)
            }
        }

        // Keep calculator keypad positions identical in all languages (do NOT mirror in RTL).
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            CalculatorKeypad(
                onPress = { key ->
                    playKeySound(key)
                    when (key) {
                        "C" -> {
                            onExpressionChange(TextFieldValue(""))
                            onLastCalculatorResultCentsChange(null)
                        }
                        "⌫" -> {
                            onExpressionChange(deleteBeforeCursor(expression))
                        }
                        "=" -> {
                            val result = ExpressionMoneyCalculator.evaluateToCents(expression.text)
                            val cents = result.getOrNull()
                            if (cents != null) onLastCalculatorResultCentsChange(cents)
                        }
                        "+", "-", "×", "÷" -> {
                            // Pro calculator feel: no spaces around operators.
                            onExpressionChange(insertAtCursor(expression, key))
                        }
                        ".", "," -> onExpressionChange(insertAtCursor(expression, decimalSeparator.toString()))
                        else -> onExpressionChange(insertAtCursor(expression, key))
                    }
                },
                isDark = isDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun rememberDecimalSeparator(): Char {
    // Morocco often uses ','; use device locale separator.
    return remember {
        runCatching { java.text.DecimalFormatSymbols.getInstance(Locale.getDefault()).decimalSeparator }
            .getOrDefault('.')
    }
}

private fun insertAtCursor(value: TextFieldValue, insert: String): TextFieldValue {
    val text = value.text
    val start = value.selection.start.coerceIn(0, text.length)
    val end = value.selection.end.coerceIn(0, text.length)
    val newText = text.replaceRange(start, end, insert)
    val newCursor = start + insert.length
    return value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(newCursor))
}

private fun deleteBeforeCursor(value: TextFieldValue): TextFieldValue {
    val text = value.text
    val startSel = value.selection.start.coerceIn(0, text.length)
    val endSel = value.selection.end.coerceIn(0, text.length)

    // If selection range, delete it.
    if (startSel != endSel) {
        val newText = text.removeRange(startSel, endSel)
        return value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(startSel))
    }

    // Else delete one char before cursor.
    if (startSel == 0) return value
    val newText = text.removeRange(startSel - 1, startSel)
    return value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(startSel - 1))
}

private class OperatorColorTransformation(
    private val operatorColor: androidx.compose.ui.graphics.Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val out = AnnotatedString.Builder(text.text)
        val s = text.text
        for (i in s.indices) {
            val ch = s[i]
            if (ch == '+' || ch == '-' || ch == '×' || ch == '÷' || ch == '/' || ch == '*' || ch == ',' || ch == '.' || ch == '(' || ch == ')') {
                out.addStyle(SpanStyle(color = operatorColor), i, i + 1)
            }
        }
        return TransformedText(out.toAnnotatedString(), OffsetMapping.Identity)
    }
}

@Composable
private fun CalculatorKeypad(
    onPress: (String) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = 12.dp

    // Use weights so the keypad always fills the available height (big buttons, full screen feel).
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Row 1: C ÷ × ⌫
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalcKey("C", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Clear)
            // Long-press for parentheses without changing keypad layout
            CalcKey(
                "÷",
                onPress,
                isDark,
                Modifier.weight(1f).fillMaxSize(),
                style = CalcKeyStyle.Operator,
                cornerHint = "(",
                onLongPress = { onPress("(") }
            )
            CalcKey(
                "×",
                onPress,
                isDark,
                Modifier.weight(1f).fillMaxSize(),
                style = CalcKeyStyle.Operator,
                cornerHint = ")",
                onLongPress = { onPress(")") }
            )
            CalcKey("⌫", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Clear)
        }

        // Row 2: 7 8 9 -
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalcKey("7", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Number)
            CalcKey("8", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Number)
            CalcKey("9", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Number)
            CalcKey("-", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Operator)
        }

        // Rows 3 + 4 combined (weight 2): "+" spans both rows on the right (next to 6 and 3).
        Row(
            Modifier
                .fillMaxWidth()
                .weight(2f),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // Row 3: 4 5 6
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    CalcKey("4", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Number)
                    CalcKey("5", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Number)
                    CalcKey("6", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Number)
                }

                // Row 4: 1 2 3
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    CalcKey("1", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Number)
                    CalcKey("2", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Number)
                    CalcKey("3", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Number)
                }
            }

            // "+" tall: spans rows 3 & 4
            CalcKey("+", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Operator)
        }

        // Row 5 (bottom): 0 spans 2 boxes on the left, "." and "=" on the right
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CalcKey("0", onPress, isDark, Modifier.weight(2f).fillMaxSize(), style = CalcKeyStyle.Number)
            CalcKey(".", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Number)
            CalcKey("=", onPress, isDark, Modifier.weight(1f).fillMaxSize(), style = CalcKeyStyle.Equals)
        }
    }
}

private enum class CalcKeyStyle { Number, Operator, Utility, Clear, Equals }

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun CalcKey(
    label: String,
    onPress: (String) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    style: CalcKeyStyle,
    cornerHint: String? = null,
    onLongPress: (() -> Unit)? = null
) {
    // More rounded corners for better appearance
    val shape = RoundedCornerShape(24.dp)
    val outline = if (isDark) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.outline
    val containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    val borderColor = when (style) {
        CalcKeyStyle.Operator -> MaterialTheme.colorScheme.primary
        CalcKeyStyle.Equals -> SarfGreenStrong
        CalcKeyStyle.Clear -> MaterialTheme.colorScheme.primary
        CalcKeyStyle.Number, CalcKeyStyle.Utility -> if (isDark) outline else SarfLineAlt
        else -> outline
    }
    val contentColor = when (style) {
        CalcKeyStyle.Operator -> MaterialTheme.colorScheme.primary
        CalcKeyStyle.Equals -> SarfGreenStrong
        CalcKeyStyle.Clear -> MaterialTheme.colorScheme.primary
        else -> if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .combinedClickable(
                onClick = { onPress(label) },
                onLongClick = onLongPress
            ),
        shape = shape,
        color = containerColor,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (cornerHint != null) {
                Text(
                    text = cornerHint,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 10.dp, top = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }

            Text(
                text = label,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}



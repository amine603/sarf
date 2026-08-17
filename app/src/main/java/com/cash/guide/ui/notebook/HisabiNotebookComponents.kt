package com.cash.guide.ui.notebook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.domain.MoneyUnit

@Composable
fun HisabiTopBar(
    title: String,
    onBack: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(HisabiMetrics.TopBarHeight)
                .background(Paper)
                .drawBehind {
                    drawLine(
                        color = Rule.copy(alpha = 0.72f),
                        start = Offset(0f, size.height - 0.5.dp.toPx()),
                        end = Offset(size.width, size.height - 0.5.dp.toPx()),
                        strokeWidth = 0.65.dp.toPx()
                    )
                }
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Right side in RTL: Back action (48dp touch target, 19dp icon)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(role = Role.Button, onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                HisabiSketchIcon(HisabiSymbol.Back, "رجوع", tint = Ink, size = 19.dp)
            }

            // Center Title
            Text(
                text = title,
                style = InterfaceTopBarTitle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Left side in RTL: More action (48dp touch target, 19dp icon)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(role = Role.Button, onClick = onMore),
                contentAlignment = Alignment.Center
            ) {
                HisabiSketchIcon(HisabiSymbol.More, "خيارات", tint = Ink, size = 19.dp)
            }
        }
    }
}

@Composable
fun HisabiUnitTabs(
    selectedUnit: MoneyUnit,
    onSelectUnit: (MoneyUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(HisabiMetrics.Grid * 2) // 58dp
                .drawBehind {
                    drawLine(
                        color = Rule.copy(alpha = 0.54f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 0.55.dp.toPx()
                    )
                }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "العملة:",
                style = arabicWritingStyle(color = MutedInk, sizeSp = 14f),
                modifier = Modifier.padding(start = 4.dp, end = 16.dp)
            )

            // Rial Tab
            val isRial = selectedUnit == MoneyUnit.RIAL
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(role = Role.Tab) { onSelectUnit(MoneyUnit.RIAL) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ريال",
                    style = arabicWritingStyle(
                        color = if (isRial) InkTone.Orange.color else MutedInk,
                        sizeSp = 15f,
                        weight = if (isRial) FontWeight.Bold else FontWeight.Normal
                    ),
                    maxLines = 1
                )
                if (isRial) {
                    Canvas(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.dp)
                            .padding(top = 2.dp)
                    ) {
                        drawLine(
                            color = InkTone.Orange.color,
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = 1.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Dirham Tab
            val isDirham = selectedUnit == MoneyUnit.DIRHAM
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(role = Role.Tab) { onSelectUnit(MoneyUnit.DIRHAM) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "درهم",
                    style = arabicWritingStyle(
                        color = if (isDirham) InkTone.Orange.color else MutedInk,
                        sizeSp = 15f,
                        weight = if (isDirham) FontWeight.Bold else FontWeight.Normal
                    ),
                    maxLines = 1
                )
                if (isDirham) {
                    Canvas(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.dp)
                            .padding(top = 2.dp)
                    ) {
                        drawLine(
                            color = InkTone.Orange.color,
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = 1.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HisabiNotebookRow(
    title: String,
    expression: String,
    rowIndex: Int,
    isActive: Boolean,
    isValid: Boolean,
    canDelete: Boolean,
    onTitleChange: (String) -> Unit,
    onAmountClick: () -> Unit,
    onTitleFocused: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInvalid = !isValid && expression.isNotBlank()
    var wasFocused by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(HisabiMetrics.Grid * 2) // 58dp (2 grid lines)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Delete action touch target (48dp x 48dp accessible touch target)
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(HisabiMetrics.Grid * 2), // 58dp
                contentAlignment = Alignment.Center
            ) {
                if (canDelete) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(role = Role.Button, onClick = onDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Trash,
                            contentDescription = "حذف السطر",
                            tint = MutedInk.copy(alpha = 0.65f),
                            size = 17.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Title field with script-aware font (Tajawal for Arabic, Patrick Hand for Latin)
            val titleTextStyle = notebookTitleStyle(
                text = title,
                color = WritingInk
            )

            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !wasFocused) {
                            onTitleFocused()
                        }
                        wasFocused = focusState.isFocused
                    },
                singleLine = true,
                cursorBrush = SolidColor(Ink),
                textStyle = titleTextStyle,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (title.isEmpty()) {
                            Text(
                                text = "البيان ${rowIndex + 1}...",
                                style = arabicWritingStyle(color = MutedInk.copy(alpha = 0.42f), sizeSp = 14f),
                                modifier = Modifier.baselineOnPaperRule()
                            )
                        }
                        Box(modifier = Modifier.baselineOnPaperRule()) {
                            innerTextField()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Amount field with Manrope font and baseline alignment on paper rule
            val amountStyle = amountWritingStyle(
                color = when {
                    isInvalid -> InkTone.Coral.color
                    expression.isBlank() -> MutedInk.copy(alpha = 0.45f)
                    isActive -> Ink
                    else -> WritingInk
                },
                sizeSp = 16.5f,
                weight = if (isActive || expression.isNotBlank()) FontWeight.Medium else FontWeight.Normal
            )

            Box(
                modifier = Modifier
                    .width(112.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAmountClick
                    ),
                contentAlignment = Alignment.TopEnd
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isInvalid) {
                            HisabiSketchIcon(
                                symbol = HisabiSymbol.Exclamation,
                                contentDescription = "عملية غير صالحة",
                                tint = InkTone.Coral.color,
                                size = 15.dp,
                                modifier = Modifier.offset(y = 6.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = expression.ifBlank { "0" },
                            style = amountStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.baselineOnPaperRule()
                        )

                        if (isActive) {
                            Text(
                                text = " |",
                                style = amountStyle.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isInvalid) InkTone.Coral.color else InkTone.Orange.color
                                ),
                                modifier = Modifier.baselineOnPaperRule()
                            )
                        }
                    }
                }

                // Active subtle underline resting directly on the paper rule
                if (isActive) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(2.dp)
                            .offset(y = HisabiMetrics.WritingBaseline)
                            .align(Alignment.TopEnd)
                    ) {
                        drawLine(
                            color = if (isInvalid) InkTone.Coral.color else InkTone.Orange.color,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HisabiAddRowBand(
    onAddRow: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(HisabiMetrics.Grid * 2) // 58dp
                .clickable(role = Role.Button, onClick = onAddRow)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HisabiSketchIcon(
                symbol = HisabiSymbol.Plus,
                contentDescription = null,
                tint = InkTone.Orange.color,
                size = 16.dp,
                modifier = Modifier.offset(y = 6.dp)
            )
            Text(
                text = "زيد سطر",
                style = arabicWritingStyle(
                    color = InkTone.Orange.color,
                    sizeSp = 14.5f,
                    weight = FontWeight.Medium
                ),
                modifier = Modifier.baselineOnPaperRule()
            )
        }
    }
}

@Composable
fun HisabiTotalSection(
    primaryTotal: String,
    primaryUnit: MoneyUnit,
    secondaryTotal: String,
    secondaryUnit: MoneyUnit,
    hasInvalidRows: Boolean,
    canBreakdown: Boolean,
    onShowBreakdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val clickModifier = if (canBreakdown) {
            Modifier.clickable(
                role = Role.Button,
                onClickLabel = "تفكيك الفلوس",
                onClick = onShowBreakdown
            )
        } else {
            Modifier
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(HisabiMetrics.Grid * 2) // 58dp
                .background(Paper)
                .drawBehind {
                    drawLine(
                        color = Rule.copy(alpha = 0.72f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 0.65.dp.toPx()
                    )
                }
                .then(clickModifier)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (hasInvalidRows) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Exclamation,
                        contentDescription = null,
                        tint = InkTone.Coral.color,
                        size = 16.dp,
                        modifier = Modifier.offset(y = 7.dp)
                    )
                    Text(
                        text = "راجع العمليات الحسابية غير الصالحة",
                        style = arabicWritingStyle(color = InkTone.Coral.color, sizeSp = 13.5f),
                        modifier = Modifier.baselineOnPaperRule()
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "المجموع:",
                        style = arabicWritingStyle(color = Ink, sizeSp = 15f, weight = FontWeight.Medium),
                        modifier = Modifier.baselineOnPaperRule()
                    )
                    Text(
                        text = "$primaryTotal ${primaryUnit.arabicName}",
                        style = TextStyle(
                            fontFamily = TajawalFamily,
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkTone.Orange.color
                        ),
                        modifier = Modifier.baselineOnPaperRule()
                    )
                    Text(
                        text = "($secondaryTotal ${secondaryUnit.arabicName})",
                        style = arabicWritingStyle(color = MutedInk, sizeSp = 13f),
                        modifier = Modifier.baselineOnPaperRule()
                    )
                }

                // Compact breakdown link indicator
                if (canBreakdown) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "تفكيك",
                            style = arabicWritingStyle(color = InkTone.Orange.color, sizeSp = 12.5f, weight = FontWeight.Medium),
                            modifier = Modifier.baselineOnPaperRule()
                        )
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Wallet,
                            contentDescription = "تفكيك الفلوس",
                            tint = InkTone.Orange.color,
                            size = 15.dp,
                            modifier = Modifier.offset(y = 7.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HisabiCalculatorDock(
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PaperWarm,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        border = BorderStroke(0.65.dp, Rule.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            // Collapse / Expand Handle (48dp touch target)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable(role = Role.Button, onClick = onToggleExpand),
                contentAlignment = Alignment.Center
            ) {
                HisabiSketchIcon(
                    symbol = if (expanded) HisabiSymbol.ChevronDown else HisabiSymbol.ChevronUp,
                    contentDescription = if (expanded) "إخفاء لوحة الأرقام" else "إظهار لوحة الأرقام",
                    tint = MutedInk,
                    size = 16.dp
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(2.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(232.dp),
                        verticalArrangement = Arrangement.spacedBy(4.5.dp)
                    ) {
                        // Row 1: 1 | 2 | 3 | +
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.5.dp)
                        ) {
                            listOf("1", "2", "3").forEach { key ->
                                HisabiKeyButton(
                                    text = key,
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    onClick = { onKey(key) }
                                )
                            }
                            HisabiKeyButton(
                                text = "+",
                                isOperator = true,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onKey("+") }
                            )
                        }

                        // Row 2: 4 | 5 | 6 | −
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.5.dp)
                        ) {
                            listOf("4", "5", "6").forEach { key ->
                                HisabiKeyButton(
                                    text = key,
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    onClick = { onKey(key) }
                                )
                            }
                            HisabiKeyButton(
                                text = "−",
                                isOperator = true,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onKey("−") }
                            )
                        }

                        // Row 3: 7 | 8 | 9 | ×
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.5.dp)
                        ) {
                            listOf("7", "8", "9").forEach { key ->
                                HisabiKeyButton(
                                    text = key,
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    onClick = { onKey(key) }
                                )
                            }
                            HisabiKeyButton(
                                text = "×",
                                isOperator = true,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onKey("×") }
                            )
                        }

                        // Row 4: . | 0 | ⌫ | ÷
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.5.dp)
                        ) {
                            HisabiKeyButton(
                                text = ".",
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onKey(".") }
                            )
                            HisabiKeyButton(
                                text = "0",
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onKey("0") }
                            )
                            HisabiKeyButton(
                                text = "⌫",
                                isBackspace = true,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onKey("⌫") }
                            )
                            HisabiKeyButton(
                                text = "÷",
                                isOperator = true,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onKey("÷") }
                            )
                        }

                        // Row 5: = full-width copper/orange button
                        HisabiKeyButton(
                            text = "=",
                            isEquals = true,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            onClick = { onKey("=") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HisabiKeyButton(
    text: String,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false,
    isBackspace: Boolean = false,
    isEquals: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        color = when {
            isEquals -> InkTone.Orange.color
            isOperator -> PaperWarm
            else -> Paper
        },
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            0.65.dp,
            when {
                isEquals -> InkTone.Orange.color
                isOperator -> InkTone.Orange.color.copy(alpha = 0.35f)
                else -> Ink.copy(alpha = 0.18f)
            }
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isBackspace) {
                HisabiSketchIcon(
                    symbol = HisabiSymbol.Backspace,
                    contentDescription = "مسح",
                    tint = Ink,
                    size = 20.dp
                )
            } else {
                Text(
                    text = text,
                    style = when {
                        isEquals -> KeypadDigitStyle.copy(color = Color.White, fontSize = 23.sp)
                        isOperator -> KeypadOperatorStyle
                        else -> KeypadDigitStyle
                    }
                )
            }
        }
    }
}

package com.cash.guide.ui.notebook

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Popup
import com.cash.guide.domain.JournalKeyboardController
import com.cash.guide.domain.JournalKeyboardMode
import com.cash.guide.domain.JournalLedgerManager

/**
 * Ruled document with tactile paper texture and 48dp horizontal rules that scroll with content.
 */
@Composable
fun JournalRuledDocument(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(JournalPaper)
    ) {
        val minimumHeight = maxHeight
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minimumHeight)
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

                            // 2. 48dp Horizontal Rules
                            val grid = JournalRuleSpacing.toPx()
                            var y = grid
                            while (y <= size.height) {
                                drawLine(
                                    color = JournalRule.copy(alpha = 0.52f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 0.55.dp.toPx()
                                )
                                y += grid
                            }
                        },
                    content = content
                )
            }
        }
    }
}

/**
 * Category header sitting on the paper rule with a dusty pink highlighter stroke.
 * Features a hand-drawn calculator outline icon on the far right.
 */
@Composable
fun JournalCategoryHeader(
    categoryName: String,
    modifier: Modifier = Modifier,
    onOpenCalculator: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(JournalRuleSpacing) // exactly 48dp = 1 notebook rule
            .drawBehind {
                val strokeW = 0.55.dp.toPx()
                val y = size.height
                drawLine(
                    color = JournalRule.copy(alpha = 0.52f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeW
                )
            }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        JournalBaselineHighlightedText(
            text = categoryName,
            style = journalCategoryStyle(),
            highlighterColor = HighlighterPink,
            highlighterAlpha = 0.40f,
            horizontalPadding = 5.dp,
            verticalPadding = 1.0.dp,
            seedVariant = 1
        )

        if (onOpenCalculator != null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Ouvrir la calculatrice",
                        onClick = onOpenCalculator
                    )
                    .semantics { testTag = "tag_header_calculator_icon" },
                contentAlignment = Alignment.Center
            ) {
                HisabiSketchIcon(
                    symbol = HisabiSymbol.Calculator,
                    contentDescription = "Ouvrir la calculatrice",
                    tint = JournalInk,
                    size = 20.dp,
                    modifier = Modifier.offset(y = 14.dp)
                )
            }
        }
    }
}

/**
 * Transaction row in LTR French journal style.
 * Exactly 1 visible ruled-paper line (48dp height).
 */
@Composable
fun JournalEntryRow(
    rowNumber: Int,
    titleValue: TextFieldValue,
    amountValue: TextFieldValue,
    currencySuffix: String,
    isTitleActive: Boolean,
    isAmountActive: Boolean,
    isValid: Boolean,
    titleFocusRequester: FocusRequester? = null,
    amountFocusRequester: FocusRequester? = null,
    onTitleValueChange: (TextFieldValue) -> Unit,
    onAmountValueChange: (TextFieldValue) -> Unit,
    onTitleFocused: () -> Unit,
    onAmountFocused: () -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isInvalid = !isValid && amountValue.text.isNotBlank()

    var titleLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val titleTextWidth = titleLayoutResult?.let { if (it.lineCount > 0) it.getLineRight(0) - it.getLineLeft(0) else 0f } ?: 0f

    var amountLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val amountTextWidth = amountLayoutResult?.let { if (it.lineCount > 0) it.getLineRight(0) - it.getLineLeft(0) else 0f } ?: 0f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(JournalRuleSpacing) // exactly 48dp = 1 notebook rule
            .padding(start = 18.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Row Number: "1.", "2.", etc.
        Text(
            text = "$rowNumber.",
            style = journalRowNumberStyle(),
            modifier = Modifier
                .width(24.dp)
                .journalBaselineOnRule()
        )

        // 2. Title Field Container (Subtle blue-gray underline on measured glyph width only when active)
        val titleFocusMod = if (titleFocusRequester != null) {
            Modifier.focusRequester(titleFocusRequester)
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(JournalRuleSpacing)
                .drawBehind {
                    if (isTitleActive) {
                        val strokeW = 1.dp.toPx()
                        val y = size.height
                        val underlineWidth = if (titleTextWidth > 0f) titleTextWidth else 20.dp.toPx()
                        drawLine(
                            color = JournalRule.copy(alpha = 0.35f),
                            start = Offset(0f, y),
                            end = Offset(underlineWidth, y),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                    }
                }
        ) {
            BasicTextField(
                value = titleValue,
                onValueChange = onTitleValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(titleFocusMod)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            onTitleFocused()
                        }
                    }
                    .journalBaselineOnRule(),
                singleLine = true,
                cursorBrush = SolidColor(JournalInk),
                textStyle = journalTitleStyle(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                onTextLayout = { titleLayoutResult = it },
                decorationBox = { innerTextField ->
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // 3. Amount + Suffix Area
        val amountTextColor = when {
            isInvalid -> ColorCoral
            amountValue.text.isBlank() -> JournalMutedInk.copy(alpha = 0.5f)
            else -> JournalInk
        }

        val amountFocusMod = if (amountFocusRequester != null) {
            Modifier.focusRequester(amountFocusRequester)
        } else {
            Modifier
        }

        Row(
            modifier = Modifier
                .height(JournalRuleSpacing)
                .widthIn(min = 85.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            // Amount Container (Faint dusty-pink underline on numeric glyph width only when active)
            Box(
                modifier = Modifier
                    .height(JournalRuleSpacing)
                    .widthIn(min = 28.dp)
                    .drawBehind {
                        if (isAmountActive) {
                            val strokeW = 1.dp.toPx()
                            val y = size.height
                            val underlineWidth = if (amountTextWidth > 0f) amountTextWidth else 18.dp.toPx()
                            drawLine(
                                color = HighlighterPink.copy(alpha = 0.45f),
                                start = Offset(0f, y),
                                end = Offset(underlineWidth, y),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                        }
                    }
            ) {
                BasicTextField(
                    value = amountValue,
                    onValueChange = onAmountValueChange,
                    modifier = Modifier
                        .widthIn(min = 28.dp)
                        .then(amountFocusMod)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                onAmountFocused()
                            }
                        }
                        .journalBaselineOnRule()
                        .semantics { testTag = "tag_row_amount_$rowNumber" },
                    singleLine = true,
                    cursorBrush = SolidColor(JournalInk),
                    textStyle = journalAmountStyle(color = amountTextColor),
                    onTextLayout = { amountLayoutResult = it },
                    decorationBox = { innerTextField ->
                        if (amountValue.text.isEmpty() && !isAmountActive) {
                            Text(
                                text = "0",
                                style = journalAmountStyle(color = JournalMutedInk.copy(alpha = 0.5f)),
                                modifier = Modifier.journalBaselineOnRule()
                            )
                        } else if (!isAmountActive && amountValue.text.isNotEmpty()) {
                            // Inactive Display: Grouped French Number
                            Text(
                                text = JournalLedgerManager.formatFrenchNumber(amountValue.text),
                                style = journalAmountStyle(color = amountTextColor),
                                modifier = Modifier.journalBaselineOnRule()
                            )
                        } else {
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Suffix: Muted pencil ink (outside editable field, no underline)
            Text(
                text = currencySuffix,
                style = journalSuffixStyle(),
                color = JournalMutedInk,
                modifier = Modifier.journalBaselineOnRule()
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // 4. State-Aware Far-Right Action: Delete '×' when inactive, Confirm '✓' when actively editing
        val isRowActive = isTitleActive || isAmountActive
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = JournalRuleSpacing)
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (isRowActive) "Valider la ligne" else "Supprimer la ligne",
                    onClick = if (isRowActive) onConfirm else onDelete
                )
                .semantics {
                    testTag = if (isRowActive) "tag_row_confirm_$rowNumber" else "tag_row_delete_$rowNumber"
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(
                modifier = Modifier
                    .offset(y = 37.dp)
                    .size(JournalActionIconSize) // 12.dp
            ) {
                val strokeW = 1.25.dp.toPx()
                val safePad = 1.6.dp.toPx()
                val w = size.width
                val h = size.height

                if (isRowActive) {
                    // Handwritten '✓' resting directly on top of the paper rule
                    val hookX = w * 0.38f
                    val hookY = h - safePad
                    drawLine(
                        color = JournalActionConfirm,
                        start = Offset(safePad, h * 0.48f),
                        end = Offset(hookX, hookY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = JournalActionConfirm,
                        start = Offset(hookX, hookY),
                        end = Offset(w - safePad, safePad),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                } else {
                    // Handwritten '×' resting directly on top of the paper rule
                    drawLine(
                        color = JournalActionDelete,
                        start = Offset(safePad, safePad),
                        end = Offset(w - safePad, h - safePad),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = JournalActionDelete,
                        start = Offset(w - safePad, safePad),
                        end = Offset(safePad, h - safePad),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * Centered handwritten '+' button to add a new transaction row manually.
 * Occupies exactly 1 rule line (48dp) with a 48dp accessible touch target.
 * Renders regular handwritten text resting directly on the paper rule.
 */
@Composable
fun JournalAddRowButton(
    onAddRow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(JournalRuleSpacing) // exactly 48dp = 1 notebook rule
            .clickable(
                role = Role.Button,
                onClickLabel = "Ajouter une ligne",
                onClick = onAddRow
            )
            .semantics { testTag = "tag_add_row_button" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = TextStyle(
                fontFamily = JournalHandFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = JournalInk,
                platformStyle = NoFontPadding
            ),
            modifier = Modifier.journalBaselineOnRule()
        )
    }
}

/**
 * Single-line French bullet-journal total result band:
 * - Exactly 1 notebook rule line (48dp height).
 * - Sits over a single restrained organic pale-yellow highlighter stroke.
 * - Displays ONLY the active currency unit (e.g. "8 800 rial" or "440 DH").
 * - Prominent numeric value with slightly smaller muted suffix.
 * - Measured FirstBaseline lands directly on the paper rule.
 * - Clickable to open denomination breakdown when valid and > 0.
 */
@Composable
fun JournalTotalResultBand(
    amount: String,
    suffix: String,
    hasInvalidRows: Boolean,
    canBreakdown: Boolean,
    onShowBreakdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clickModifier = if (canBreakdown) {
        Modifier.clickable(
            role = Role.Button,
            onClickLabel = "Afficher la décomposition",
            onClick = onShowBreakdown
        )
    } else {
        Modifier
    }

    val totalColor = if (hasInvalidRows) ColorCoral else JournalInk

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(JournalRuleSpacing) // exactly 48dp = 1 notebook rule
            .semantics { testTag = "tag_total_result_band" },
        contentAlignment = Alignment.Center
    ) {
        JournalBaselineHighlightedResult(
            amount = amount,
            suffix = suffix,
            amountColor = totalColor,
            suffixColor = JournalMutedInk,
            highlighterColor = HighlighterYellow,
            highlighterAlpha = 0.38f,
            horizontalPadding = 8.dp,
            verticalPadding = 2.dp,
            seedVariant = 2,
            modifier = clickModifier
        )
    }
}

/**
 * Journal Compact Numeric Dock:
 * - 4 columns x 3 rows:
 *   1 | 2 | 3 | ⌫
 *   4 | 5 | 6 | .
 *   7 | 8 | 9 | 0
 * - "ABC" switch button on the left of top handle bar
 * - Collapse / Expand handle
 * - Extends behind system navigation bar with no gap
 */
@Composable
fun JournalCompactNumericDock(
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onKey: (String) -> Unit,
    onSwitchToTextMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { testTag = "tag_compact_keypad" },
        color = JournalDockBg,
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 2.dp)
        ) {
            // 1. Top Handle Bar: "ABC" Mode Switch on Left + Centered Chevron + Balanced Right Spacer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: "ABC" Switch to Text Keyboard
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 30.dp)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Passer au clavier texte",
                            onClick = onSwitchToTextMode
                        )
                        .semantics { testTag = "tag_switch_to_text_key" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABC",
                        style = TextStyle(
                            fontFamily = JournalHandFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk
                        )
                    )
                }

                // Center: Collapse / Expand Handle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            role = Role.Button,
                            onClickLabel = if (expanded) "Réduire le clavier" else "Développer le clavier",
                            onClick = onToggleExpand
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(18.dp, 10.dp)) {
                        val strokeW = 1.4.dp.toPx()
                        val inkColor = JournalInk.copy(alpha = 0.85f)
                        if (expanded) {
                            // Chevron Down
                            drawLine(
                                color = inkColor,
                                start = Offset(1.dp.toPx(), 2.dp.toPx()),
                                end = Offset(size.width / 2f, size.height - 2.dp.toPx()),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = inkColor,
                                start = Offset(size.width / 2f, size.height - 2.dp.toPx()),
                                end = Offset(size.width - 1.dp.toPx(), 2.dp.toPx()),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                        } else {
                            // Chevron Up
                            drawLine(
                                color = inkColor,
                                start = Offset(1.dp.toPx(), size.height - 2.dp.toPx()),
                                end = Offset(size.width / 2f, 2.dp.toPx()),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = inkColor,
                                start = Offset(size.width / 2f, 2.dp.toPx()),
                                end = Offset(size.width - 1.dp.toPx(), size.height - 2.dp.toPx()),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Right: Symmetrical 44.dp reserved box for perfect geometric centering
                Spacer(modifier = Modifier.size(width = 44.dp, height = 30.dp))
            }

            if (expanded) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    // Connected Grid of Keys (3 rows x 4 cols)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(135.dp)
                            .drawBehind {
                                val strokeW = 0.8.dp.toPx()
                                val gridLineColor = JournalInk.copy(alpha = 0.75f)

                                // Outer rectangle
                                drawRect(
                                    color = gridLineColor,
                                    topLeft = Offset(0f, 0f),
                                    size = size,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )

                                // 2 Internal horizontal dividers
                                val rowH = size.height / 3f
                                for (i in 1..2) {
                                    val y = rowH * i
                                    drawLine(
                                        color = gridLineColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeW
                                    )
                                }

                                // 3 Internal vertical dividers
                                val colW = size.width / 4f
                                for (i in 1..3) {
                                    val x = colW * i
                                    drawLine(
                                        color = gridLineColor,
                                        start = Offset(x, 0f),
                                        end = Offset(x, size.height),
                                        strokeWidth = strokeW
                                    )
                                }
                            }
                    ) {
                        // Row 1: 1 | 2 | 3 | ⌫
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            JournalKeyCell("1", Modifier.weight(1f)) { onKey("1") }
                            JournalKeyCell("2", Modifier.weight(1f)) { onKey("2") }
                            JournalKeyCell("3", Modifier.weight(1f)) { onKey("3") }
                            JournalKeyCell("⌫", Modifier.weight(1f), isBackspace = true) { onKey("⌫") }
                        }

                        // Row 2: 4 | 5 | 6 | .
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            JournalKeyCell("4", Modifier.weight(1f)) { onKey("4") }
                            JournalKeyCell("5", Modifier.weight(1f)) { onKey("5") }
                            JournalKeyCell("6", Modifier.weight(1f)) { onKey("6") }
                            JournalKeyCell(".", Modifier.weight(1f)) { onKey(".") }
                        }

                        // Row 3: 7 | 8 | 9 | 0
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            JournalKeyCell("7", Modifier.weight(1f)) { onKey("7") }
                            JournalKeyCell("8", Modifier.weight(1f)) { onKey("8") }
                            JournalKeyCell("9", Modifier.weight(1f)) { onKey("9") }
                            JournalKeyCell("0", Modifier.weight(1f)) { onKey("0") }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * Custom In-App French AZERTY Text Keyboard:
 * - 4 rows:
 *   Row 1: A  Z  E  R  T  Y  U  I  O  P
 *   Row 2:  Q  S  D  F  G  H  J  K  L  M
 *   Row 3:   ⇧  W  X  C  V  B  N  '  ⌫
 *   Row 4: 123          espace          .   OK ✓
 * - Vowel long-press shows accent popup (e.g. E -> é, è, ê, ë)
 * - 123 switches to Amount field and numeric mode
 * - OK ✓ confirms and commits edit
 * - Surface: JournalDockBg (#F6F0DF)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JournalTextKeyboardDock(
    expanded: Boolean,
    isShiftActive: Boolean,
    onToggleExpand: () -> Unit,
    onToggleShift: () -> Unit,
    onChar: (String) -> Unit,
    onBackspace: () -> Unit,
    onSwitchToNumericMode: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeAccentChar by remember { mutableStateOf<Char?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { testTag = "tag_text_keypad" },
        color = JournalDockBg,
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            // 1. Top Handle Bar: Centered Chevron when expanded, or action bar when collapsed
            if (expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.size(width = 44.dp, height = 30.dp))

                    // Center: Collapse Handle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Réduire le clavier",
                                onClick = onToggleExpand
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(18.dp, 10.dp)) {
                            val strokeW = 1.4.dp.toPx()
                            val inkColor = JournalInk.copy(alpha = 0.85f)
                            // Chevron Down
                            drawLine(
                                color = inkColor,
                                start = Offset(1.dp.toPx(), 2.dp.toPx()),
                                end = Offset(size.width / 2f, size.height - 2.dp.toPx()),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = inkColor,
                                start = Offset(size.width / 2f, size.height - 2.dp.toPx()),
                                end = Offset(size.width - 1.dp.toPx(), 2.dp.toPx()),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(width = 44.dp, height = 30.dp))
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: "123" Switch to Numeric Keyboard
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 30.dp)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Passer au clavier numérique",
                                onClick = onSwitchToNumericMode
                            )
                            .semantics { testTag = "tag_switch_to_num_key_top" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "123",
                            style = TextStyle(
                                fontFamily = JournalHandFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalInk
                            )
                        )
                    }

                    // Center: Expand Chevron Up
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Développer le clavier",
                                onClick = onToggleExpand
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(18.dp, 10.dp)) {
                            val strokeW = 1.4.dp.toPx()
                            val inkColor = JournalInk.copy(alpha = 0.85f)
                            drawLine(
                                color = inkColor,
                                start = Offset(1.dp.toPx(), size.height - 2.dp.toPx()),
                                end = Offset(size.width / 2f, 2.dp.toPx()),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = inkColor,
                                start = Offset(size.width / 2f, 2.dp.toPx()),
                                end = Offset(size.width - 1.dp.toPx(), size.height - 2.dp.toPx()),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Right: "OK ✓" Confirm Button
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 30.dp)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Confirmer",
                                onClick = onConfirm
                            )
                            .semantics { testTag = "tag_confirm_key_top" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OK ✓",
                            style = TextStyle(
                                fontFamily = JournalHandFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalActionConfirm
                            )
                        )
                    }
                }
            }

            if (expanded) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            val row1 = listOf('A', 'Z', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P')
                            val row2 = listOf('Q', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M')
                            val row3 = listOf('W', 'X', 'C', 'V', 'B', 'N', '\'')

                            // Row 1: A Z E R T Y U I O P
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                row1.forEach { ch ->
                                    val displayStr = if (isShiftActive) ch.uppercase() else ch.lowercase()
                                    JournalTextKeyCell(
                                        label = displayStr,
                                        modifier = Modifier.weight(1f),
                                        hasAccents = JournalKeyboardController.getAccents(ch).isNotEmpty(),
                                        onClick = { onChar(displayStr) },
                                        onLongClick = { activeAccentChar = ch }
                                    )
                                }
                            }

                            // Row 2: Q S D F G H J K L M
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp)
                            ) {
                                row2.forEach { ch ->
                                    val displayStr = if (isShiftActive) ch.uppercase() else ch.lowercase()
                                    JournalTextKeyCell(
                                        label = displayStr,
                                        modifier = Modifier.weight(1f),
                                        hasAccents = JournalKeyboardController.getAccents(ch).isNotEmpty(),
                                        onClick = { onChar(displayStr) },
                                        onLongClick = { activeAccentChar = ch }
                                    )
                                }
                            }

                            // Row 3: ⇧ W X C V B N ' ⌫
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                // Shift
                                JournalTextKeyCell(
                                    label = "⇧",
                                    isShift = true,
                                    isShiftActive = isShiftActive,
                                    modifier = Modifier.weight(1.3f),
                                    onClick = onToggleShift
                                )

                                row3.forEach { ch ->
                                    val displayStr = if (ch == '\'') "'" else (if (isShiftActive) ch.uppercase() else ch.lowercase())
                                    JournalTextKeyCell(
                                        label = displayStr,
                                        modifier = Modifier.weight(1f),
                                        hasAccents = JournalKeyboardController.getAccents(ch).isNotEmpty(),
                                        onClick = { onChar(displayStr) },
                                        onLongClick = if (ch != '\'') { { activeAccentChar = ch } } else null
                                    )
                                }

                                // Backspace
                                JournalTextKeyCell(
                                    label = "⌫",
                                    isBackspace = true,
                                    modifier = Modifier.weight(1.3f),
                                    onClick = onBackspace
                                )
                            }

                            // Row 4: 123 | espace | . | OK ✓
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                JournalTextKeyCell(
                                    label = "123",
                                    modifier = Modifier.weight(1.4f),
                                    onClick = onSwitchToNumericMode
                                )
                                JournalTextKeyCell(
                                    label = "espace",
                                    isSpace = true,
                                    modifier = Modifier.weight(4.4f),
                                    onClick = { onChar(" ") }
                                )
                                JournalTextKeyCell(
                                    label = ".",
                                    modifier = Modifier.weight(1.2f),
                                    onClick = { onChar(".") }
                                )
                                JournalTextKeyCell(
                                    label = "OK ✓",
                                    isConfirm = true,
                                    modifier = Modifier.weight(1.8f),
                                    onClick = onConfirm
                                )
                            }
                        }

                        // Floating Accent Popup
                        if (activeAccentChar != null) {
                            val char = activeAccentChar!!
                            val accents = JournalKeyboardController.getAccents(
                                if (isShiftActive) char.uppercaseChar() else char.lowercaseChar()
                            )
                            if (accents.isNotEmpty()) {
                                Popup(
                                    alignment = Alignment.TopCenter,
                                    offset = androidx.compose.ui.unit.IntOffset(0, -65),
                                    onDismissRequest = { activeAccentChar = null }
                                ) {
                                    Surface(
                                        color = JournalPaper,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, JournalRule.copy(alpha = 0.65f)),
                                        shadowElevation = 6.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            accents.forEach { acc ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 38.dp, height = 44.dp)
                                                        .clickable(role = Role.Button) {
                                                            onChar(acc)
                                                            activeAccentChar = null
                                                        }
                                                        .drawBehind {
                                                            if (acc.startsWith("é") || acc.startsWith("É") || acc.startsWith("à")) {
                                                                drawRect(HighlighterPink.copy(alpha = 0.25f))
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = acc,
                                                        style = TextStyle(
                                                            fontFamily = JournalHandFamily,
                                                            fontSize = 24.sp,
                                                            color = JournalInk
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun JournalTextKeyCell(
    label: String,
    modifier: Modifier = Modifier,
    isShift: Boolean = false,
    isShiftActive: Boolean = false,
    isBackspace: Boolean = false,
    isSpace: Boolean = false,
    isConfirm: Boolean = false,
    hasAccents: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val clickModifier = if (onLongClick != null) {
        Modifier.combinedClickable(
            role = Role.Button,
            onClick = onClick,
            onLongClick = onLongClick
        )
    } else {
        Modifier.clickable(
            role = Role.Button,
            onClick = onClick
        )
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .then(clickModifier)
            .drawBehind {
                val strokeW = 0.6.dp.toPx()
                val dividerColor = JournalRule.copy(alpha = 0.35f)
                // Subtle pencil bottom line
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeW
                )
                // Shift active dab
                if (isShift && isShiftActive) {
                    drawCircle(
                        color = HighlighterPink.copy(alpha = 0.40f),
                        radius = size.minDimension * 0.35f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isBackspace) {
            // Hand-drawn backspace symbol
            Canvas(modifier = Modifier.size(22.dp, 16.dp)) {
                val strokeW = 1.15.dp.toPx()
                val ink = JournalInk.copy(alpha = 0.9f)
                val w = size.width
                val h = size.height

                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.32f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h)
                    lineTo(w * 0.32f, h)
                    lineTo(0f, h / 2f)
                    close()
                }
                drawPath(p, color = ink, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW))

                val cx = w * 0.64f
                val cy = h / 2f
                val d = 3.0.dp.toPx()
                drawLine(ink, Offset(cx - d, cy - d), Offset(cx + d, cy + d), strokeW, StrokeCap.Round)
                drawLine(ink, Offset(cx + d, cy - d), Offset(cx - d, cy + d), strokeW, StrokeCap.Round)
            }
        } else if (isShift) {
            // Upward arrow ⇧
            Canvas(modifier = Modifier.size(18.dp, 18.dp)) {
                val strokeW = 1.4.dp.toPx()
                val ink = if (isShiftActive) JournalInk else JournalInk.copy(alpha = 0.85f)
                val w = size.width
                val h = size.height

                // Arrow head
                drawLine(ink, Offset(w * 0.15f, h * 0.48f), Offset(w * 0.5f, h * 0.15f), strokeW, StrokeCap.Round)
                drawLine(ink, Offset(w * 0.85f, h * 0.48f), Offset(w * 0.5f, h * 0.15f), strokeW, StrokeCap.Round)
                // Arrow stem
                drawLine(ink, Offset(w * 0.5f, h * 0.18f), Offset(w * 0.5f, h * 0.82f), strokeW, StrokeCap.Round)
            }
        } else {
            val textColor = when {
                isConfirm -> JournalActionConfirm
                isSpace -> JournalMutedInk
                else -> JournalInk
            }
            val fontSize = when {
                isSpace -> 18.sp
                isConfirm -> 17.sp
                label == "123" -> 18.sp
                label == "." -> 24.sp
                else -> 21.sp
            }
            val fontWeight = when {
                isConfirm || label == "123" -> FontWeight.Bold
                else -> FontWeight.Normal
            }

            Text(
                text = label,
                style = TextStyle(
                    fontFamily = JournalHandFamily,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = textColor
                )
            )
        }
    }
}

/**
 * Large Full Calculator Popup Modal:
 * - Centered on dimmed notebook paper background (~90-94% width, ~68-74% usable height)
 * - Top-right hand-drawn '×' close icon (48dp x 48dp target)
 * - Expression & evaluated result displays
 * - Full arithmetic keypad (4x4 grid with pastel operator dabs + full-width '=' row)
 * - Full-width 'Confirmer' action with pink marker stroke
 * - Back button closes popup without applying
 */
@Composable
fun JournalCalculatorPopup(
    expression: String,
    result: String,
    hasError: Boolean,
    canConfirm: Boolean,
    onKey: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner card modal
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.74f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // prevent closing when tapping card surface
                )
                .semantics { testTag = "tag_popup_container" },
            color = JournalPaper,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.85.dp, JournalInk.copy(alpha = 0.82f)),
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Subtle tactile paper grain
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dotColor = JournalInk.copy(alpha = 0.022f)
                    var px = 16f
                    while (px < size.width) {
                        var py = 20f
                        while (py < size.height) {
                            drawCircle(dotColor, radius = 0.9f, center = Offset(px, py))
                            py += 64f
                        }
                        px += 48f
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Bar: Close "×" button on upper-right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Fermer la calculatrice",
                                    onClick = onDismiss
                                )
                                .semantics { testTag = "tag_popup_close_button" },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val strokeW = 1.45.dp.toPx()
                                val ink = JournalInk.copy(alpha = 0.9f)
                                val pad = 2.dp.toPx()
                                drawLine(ink, Offset(pad, pad), Offset(size.width - pad, size.height - pad), strokeW, StrokeCap.Round)
                                drawLine(ink, Offset(size.width - pad, pad), Offset(pad, size.height - pad), strokeW, StrokeCap.Round)
                            }
                        }
                    }

                    // Display Area: Expression & Result
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (expression.isNotBlank()) {
                            val formattedExpr = remember(expression) {
                                JournalLedgerManager.formatDisplayExpression(expression)
                            }
                            Text(
                                text = formattedExpr,
                                style = TextStyle(
                                    fontFamily = JournalHandFamily,
                                    fontSize = 22.sp,
                                    color = JournalInk
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { testTag = "tag_popup_expression_display" }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (hasError) {
                            Text(
                                text = "Opération non valide",
                                style = TextStyle(
                                    fontFamily = JournalHandFamily,
                                    fontSize = 20.sp,
                                    color = ColorCoral
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { testTag = "tag_popup_result_display" }
                            )
                        } else if (result.isNotBlank()) {
                            Text(
                                text = result,
                                style = TextStyle(
                                    fontFamily = JournalHandFamily,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalInk
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { testTag = "tag_popup_result_display" }
                            )
                        } else if (expression.isBlank()) {
                            Text(
                                text = "0",
                                style = TextStyle(
                                    fontFamily = JournalHandFamily,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalMutedInk.copy(alpha = 0.5f)
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { testTag = "tag_popup_result_display" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Full Arithmetic Keypad: 4x4 Grid + '=' Row + 'Confirmer' Row
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                                .drawBehind {
                                    val strokeW = 0.8.dp.toPx()
                                    val gridLineColor = JournalInk.copy(alpha = 0.75f)

                                    drawRect(
                                        color = gridLineColor,
                                        topLeft = Offset(0f, 0f),
                                        size = size,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                    )

                                    val rowH = size.height / 4f
                                    for (i in 1..3) {
                                        val y = rowH * i
                                        drawLine(
                                            color = gridLineColor,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeW
                                        )
                                    }

                                    val colW = size.width / 4f
                                    for (i in 1..3) {
                                        val x = colW * i
                                        drawLine(
                                            color = gridLineColor,
                                            start = Offset(x, 0f),
                                            end = Offset(x, size.height),
                                            strokeWidth = strokeW
                                        )
                                    }
                                }
                        ) {
                            // Row 1: 1 | 2 | 3 | +
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                JournalKeyCell("1", Modifier.weight(1f)) { onKey("1") }
                                JournalKeyCell("2", Modifier.weight(1f)) { onKey("2") }
                                JournalKeyCell("3", Modifier.weight(1f)) { onKey("3") }
                                JournalKeyCell("+", Modifier.weight(1f), operatorDabColor = HighlighterPink) { onKey("+") }
                            }

                            // Row 2: 4 | 5 | 6 | −
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                JournalKeyCell("4", Modifier.weight(1f)) { onKey("4") }
                                JournalKeyCell("5", Modifier.weight(1f)) { onKey("5") }
                                JournalKeyCell("6", Modifier.weight(1f)) { onKey("6") }
                                JournalKeyCell("−", Modifier.weight(1f), operatorDabColor = HighlighterYellow) { onKey("−") }
                            }

                            // Row 3: 7 | 8 | 9 | ×
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                JournalKeyCell("7", Modifier.weight(1f)) { onKey("7") }
                                JournalKeyCell("8", Modifier.weight(1f)) { onKey("8") }
                                JournalKeyCell("9", Modifier.weight(1f)) { onKey("9") }
                                JournalKeyCell("×", Modifier.weight(1f), operatorDabColor = HighlighterGreen) { onKey("×") }
                            }

                            // Row 4: . | 0 | ⌫ | ÷
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                JournalKeyCell(".", Modifier.weight(1f)) { onKey(".") }
                                JournalKeyCell("0", Modifier.weight(1f)) { onKey("0") }
                                JournalKeyCell("⌫", Modifier.weight(1f), isBackspace = true) { onKey("⌫") }
                                JournalKeyCell("÷", Modifier.weight(1f), operatorDabColor = HighlighterBlue) { onKey("÷") }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // '=' Button (Full Width, Centered Pink Dab)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Calculer le résultat",
                                    onClick = { onKey("=") }
                                )
                                .journalOperatorDab(
                                    color = HighlighterPink,
                                    alpha = 0.78f,
                                    widthDp = 54.dp,
                                    heightDp = 20.dp
                                )
                                .semantics { testTag = "tag_popup_equals_key" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "=",
                                style = JournalKeyDigitStyle.copy(fontSize = 24.sp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 'Confirmer' Action Button (Organic Pink Marker Stroke centered around text)
                        val confirmAlpha = if (canConfirm) 0.85f else 0.28f
                        val confirmTextAlpha = if (canConfirm) 1f else 0.35f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable(
                                    enabled = canConfirm,
                                    role = Role.Button,
                                    onClickLabel = "Confirmer le montant",
                                    onClick = onConfirm
                                )
                                .semantics { testTag = "tag_popup_confirm_button" },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier.journalHighlighter(
                                    color = HighlighterPink,
                                    alpha = confirmAlpha,
                                    horizontalPadding = 22.dp,
                                    verticalPadding = 1.0.dp,
                                    seedVariant = 3
                                )
                            ) {
                                Text(
                                    text = "Confirmer",
                                    style = TextStyle(
                                        fontFamily = JournalHandFamily,
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JournalInk.copy(alpha = confirmTextAlpha),
                                        platformStyle = NoFontPadding
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalKeyCell(
    text: String,
    modifier: Modifier = Modifier,
    operatorDabColor: Color? = null,
    isBackspace: Boolean = false,
    onClick: () -> Unit
) {
    val dabModifier = if (operatorDabColor != null) {
        Modifier.journalOperatorDab(operatorDabColor, alpha = 0.72f, widthDp = 34.dp, heightDp = 18.dp)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(role = Role.Button, onClick = onClick)
            .then(dabModifier),
        contentAlignment = Alignment.Center
    ) {
        if (isBackspace) {
            // Hand-drawn backspace symbol
            Canvas(modifier = Modifier.size(22.dp, 16.dp)) {
                val strokeW = 1.15.dp.toPx()
                val ink = JournalInk.copy(alpha = 0.9f)
                val w = size.width
                val h = size.height

                // Pointed tag shape
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.32f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h)
                    lineTo(w * 0.32f, h)
                    lineTo(0f, h / 2f)
                    close()
                }
                drawPath(p, color = ink, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW))

                // Inner cross '×'
                val cx = w * 0.64f
                val cy = h / 2f
                val d = 3.2.dp.toPx()
                drawLine(ink, Offset(cx - d, cy - d), Offset(cx + d, cy + d), strokeW, StrokeCap.Round)
                drawLine(ink, Offset(cx + d, cy - d), Offset(cx - d, cy + d), strokeW, StrokeCap.Round)
            }
        } else {
            Text(
                text = text,
                style = JournalKeyDigitStyle
            )
        }
    }
}

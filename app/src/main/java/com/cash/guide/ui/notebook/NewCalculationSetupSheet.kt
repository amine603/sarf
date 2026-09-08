package com.cash.guide.ui.notebook

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cash.guide.R
import com.cash.guide.domain.AndroidIcuGraphemeSegmenter
import com.cash.guide.domain.JournalKeyboardController
import com.cash.guide.domain.JournalKeyboardLanguage
import com.cash.guide.domain.JournalKeyboardMode
import com.cash.guide.domain.JournalShiftMode
import com.cash.guide.domain.JournalShiftState
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.domain.ShiftAction

/**
 * Centered Notebook Paper Dialog for New Calculation Setup:
 * - Rows sit strictly on 29.dp blue ruled lines matching HomeScreen aesthetic.
 * - Row 1 (Top): Name input case ("Nom du calcul ou client") with pencil icon and clear '✕' button.
 * - Row 2: "Type de calcul" on start, "Personnel | Crédit" on end in the same row.
 * - Row 3: "Devise" on start, "DH | Rial" on end in the same row.
 * - Row 4: "Modèles de calcul" on start, compact notebook dropdown on end.
 * - Selecting a template properly sets cursor at the end and allows easy backspacing or clearing.
 * - Docked custom keyboard at bottom without excess padding.
 */
@Composable
fun NewCalculationSetupSheet(
    defaultCurrency: MoneyUnit = MoneyUnit.DIRHAM,
    onConfirm: (title: String, calcType: String, currency: MoneyUnit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    var titleValue by remember { mutableStateOf(TextFieldValue("")) }
    var selectedType by remember { mutableStateOf("PERSONNEL") } // "PERSONNEL" or "CREDIT"
    var selectedCurrency by remember { mutableStateOf(defaultCurrency) }
    var selectedModelName by remember { mutableStateOf<String?>(null) }
    var showModelDropdown by remember { mutableStateOf(false) }

    var keyboardMode by remember { mutableStateOf(JournalKeyboardMode.NONE) }
    var keyboardLanguage by remember {
        mutableStateOf(if (isRtl) JournalKeyboardLanguage.ARABIC else JournalKeyboardLanguage.FRENCH)
    }
    var keyboardExpanded by remember { mutableStateOf(true) }
    var shiftState by remember { mutableStateOf(JournalShiftState(mode = JournalShiftMode.ONE_SHOT)) }
    val graphemeSegmenter = remember { AndroidIcuGraphemeSegmenter() }

    fun submit() {
        onConfirm(titleValue.text.trim(), selectedType, selectedCurrency)
    }

    Dialog(
        onDismissRequest = {
            if (keyboardMode != JournalKeyboardMode.NONE) {
                keyboardMode = JournalKeyboardMode.NONE
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        CompositionLocalProvider(
            LocalContext provides context,
            LocalConfiguration provides configuration,
            LocalLayoutDirection provides layoutDirection
        ) {
            BackHandler(enabled = true) {
                if (keyboardMode != JournalKeyboardMode.NONE) {
                    keyboardMode = JournalKeyboardMode.NONE
                } else {
                    onDismiss()
                }
            }

            // Dimmed Scrim Background (tap outside to close keyboard or dialog)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.50f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (keyboardMode != JournalKeyboardMode.NONE) {
                            keyboardMode = JournalKeyboardMode.NONE
                        } else {
                            onDismiss()
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Centered Area: Holds the Ruled Paper Card in the space above the keyboard
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Authentic Ruled Notebook Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 390.dp)
                                .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.2.dp,
                                    color = JournalRule.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { /* Prevent clicks on card from closing dialog */ },
                            color = JournalPaper,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(top = 0.dp, bottom = 18.dp)
                            ) {
                                // 1. Header: Title + Close Button (Sitting directly on top line, height 29.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing)
                                        .drawBehind {
                                            drawLine(
                                                color = JournalRule.copy(alpha = 0.50f),
                                                start = Offset(0f, size.height),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 0.8.dp.toPx()
                                            )
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val sheetTitle = stringResource(R.string.new_calc_sheet_title)
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .offset(y = (-6.5).dp)
                                                .background(JournalInk, CircleShape)
                                        )
                                        Text(
                                            text = sheetTitle,
                                            fontFamily = resolveJournalFont(sheetTitle, isRtl),
                                            fontSize = if (isRtl) 16.5.sp else 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalInk,
                                            style = TextStyle(platformStyle = NoFontPadding),
                                            modifier = Modifier.journalBaselineOnRule()
                                        )
                                    }

                                    // Sketch '✕' close button
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .offset(y = (-1.5).dp)
                                            .clip(CircleShape)
                                            .clickable(role = Role.Button) { onDismiss() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✕",
                                            fontFamily = JournalHandFamily,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalMutedInk
                                        )
                                    }
                                }

                                // 2. Skipped Line 1: Empty ruled notebook line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing)
                                        .drawBehind {
                                            drawLine(
                                                color = JournalRule.copy(alpha = 0.50f),
                                                start = Offset(0f, size.height),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 0.8.dp.toPx()
                                            )
                                        }
                                )

                                // 3. Title Line: "Titre :" + placeholder / typed text + blinking cursor + ✕ clear
                                val namePlaceholder = stringResource(R.string.new_calc_name_placeholder)
                                val isEditingName = keyboardMode != JournalKeyboardMode.NONE
                                val infiniteTransition = rememberInfiniteTransition(label = "new_calc_cursor")
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

                                LaunchedEffect(titleScrollState.maxValue, titleValue.text, isEditingName) {
                                    if (isEditingName) {
                                        titleScrollState.scrollTo(titleScrollState.maxValue)
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing)
                                        .drawBehind {
                                            drawLine(
                                                color = JournalRule.copy(alpha = 0.50f),
                                                start = Offset(0f, size.height),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 0.8.dp.toPx()
                                            )
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(JournalRuleSpacing)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                keyboardMode = JournalKeyboardMode.TEXT
                                            },
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val titleLabel = if (isRtl) "العنوان :" else "Titre :"
                                        Text(
                                            text = titleLabel,
                                            fontFamily = resolveJournalFont(titleLabel, isRtl),
                                            fontSize = if (isRtl) 13.5.sp else 14.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = JournalMutedInk,
                                            style = TextStyle(platformStyle = NoFontPadding),
                                            modifier = Modifier.journalBaselineOnRule()
                                        )

                                        if (titleValue.text.isEmpty()) {
                                            Row(
                                                verticalAlignment = Alignment.Bottom,
                                                modifier = Modifier.height(JournalRuleSpacing)
                                            ) {
                                                if (cursorAlpha > 0.5f) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.dp)
                                                            .height(17.dp)
                                                            .offset(y = (-3).dp)
                                                            .background(JournalInk)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                }
                                                Text(
                                                    text = namePlaceholder,
                                                    fontFamily = resolveJournalFont(namePlaceholder, isRtl),
                                                    fontSize = 13.5.sp,
                                                    color = JournalMutedInk.copy(alpha = 0.50f),
                                                    style = TextStyle(platformStyle = NoFontPadding),
                                                    maxLines = 1,
                                                    modifier = Modifier.journalBaselineOnRule()
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(JournalRuleSpacing)
                                                    .horizontalScroll(titleScrollState),
                                                contentAlignment = Alignment.BottomStart
                                            ) {
                                                Text(
                                                    text = titleValue.text,
                                                    fontFamily = resolveJournalFont(titleValue.text, isRtl),
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = JournalInk,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    style = TextStyle(platformStyle = NoFontPadding),
                                                    onTextLayout = { titleLayoutResult = it },
                                                    modifier = Modifier
                                                        .journalBaselineOnRule()
                                                        .drawWithContent {
                                                            drawContent()
                                                            if (cursorAlpha > 0.5f) {
                                                                val layout = titleLayoutResult
                                                                val cursorX = if (layout != null && titleValue.text.isNotEmpty()) {
                                                                    val offset = titleValue.selection.end.coerceIn(0, titleValue.text.length)
                                                                    layout.getCursorRect(offset).left
                                                                } else {
                                                                    0f
                                                                }
                                                                val h = 17.dp.toPx()
                                                                val bottom = size.height - 2.dp.toPx()
                                                                drawLine(
                                                                    color = JournalInk,
                                                                    start = Offset(cursorX, bottom - h),
                                                                    end = Offset(cursorX, bottom),
                                                                    strokeWidth = 2.dp.toPx(),
                                                                    cap = StrokeCap.Round
                                                                )
                                                            }
                                                        }
                                                )
                                            }
                                        }
                                    }

                                    // Clear '✕' button when text is present
                                    if (titleValue.text.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .offset(y = (-1.5).dp)
                                                .clip(CircleShape)
                                                .clickable(role = Role.Button) {
                                                    titleValue = TextFieldValue("", selection = TextRange.Zero)
                                                    selectedModelName = null
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "✕",
                                                fontFamily = JournalHandFamily,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JournalMutedInk.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }

                                // 4. Skipped Line 2: Empty ruled notebook line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing)
                                        .drawBehind {
                                            drawLine(
                                                color = JournalRule.copy(alpha = 0.50f),
                                                start = Offset(0f, size.height),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 0.8.dp.toPx()
                                            )
                                        }
                                )

                                val configuration = LocalConfiguration.current
                                val screenWidth = configuration.screenWidthDp.dp
                                val cardWidth = (screenWidth - 40.dp).coerceAtMost(390.dp)
                                val contentWidth = cardWidth - 32.dp // inside padding horizontal 16.dp
                                val optionsBlockWidth = if (isRtl) 124.dp else 129.dp
                                // Push options rightward so Crédit sits close to the right edge, without overflowing on smaller screens:
                                val labelColumnWidth = (contentWidth - optionsBlockWidth - 10.dp).coerceIn(115.dp, 175.dp)

                                // 5. Row: Type de calcul ("Type de calcul" on left, "Personnel | Crédit" on right)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing)
                                        .drawBehind {
                                            drawLine(
                                                color = JournalRule.copy(alpha = 0.50f),
                                                start = Offset(0f, size.height),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 0.8.dp.toPx()
                                            )
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val typeLabel = stringResource(R.string.new_calc_type_label)
                                    Box(
                                        modifier = Modifier
                                            .width(labelColumnWidth)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = typeLabel,
                                            fontFamily = resolveJournalFont(typeLabel, isRtl),
                                            fontSize = if (isRtl) 13.sp else 13.5.sp,
                                            color = JournalMutedInk,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }

                                    // In same row: Personnel | Crédit (shifted right, matching home filter tabs with pink underline)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val isPersonnel = selectedType == "PERSONNEL"
                                        val personnelLabel = stringResource(R.string.calc_type_personnel)
                                        Box(
                                            modifier = Modifier
                                                .height(25.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isPersonnel) JournalInk.copy(alpha = 0.08f)
                                                    else Color.Transparent
                                                )
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    role = Role.RadioButton
                                                ) { selectedType = "PERSONNEL" }
                                                .drawBehind {
                                                    if (isPersonnel) {
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
                                                .padding(horizontal = 9.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = personnelLabel,
                                                fontFamily = resolveJournalFont(personnelLabel, isRtl),
                                                fontSize = if (isRtl) 13.sp else 13.5.sp,
                                                fontWeight = if (isPersonnel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isPersonnel) JournalInk else JournalMutedInk,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        }

                                        // Vertical divider
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(11.dp)
                                                .background(JournalRule.copy(alpha = 0.70f), RoundedCornerShape(0.5.dp))
                                        )

                                        val isCredit = selectedType == "CREDIT"
                                        val creditLabel = stringResource(R.string.calc_type_credit)
                                        Box(
                                            modifier = Modifier
                                                .height(25.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isCredit) JournalInk.copy(alpha = 0.08f)
                                                    else Color.Transparent
                                                )
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    role = Role.RadioButton
                                                ) { selectedType = "CREDIT" }
                                                .drawBehind {
                                                    if (isCredit) {
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
                                                .padding(horizontal = 9.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = creditLabel,
                                                fontFamily = resolveJournalFont(creditLabel, isRtl),
                                                fontSize = if (isRtl) 13.sp else 13.5.sp,
                                                fontWeight = if (isCredit) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCredit) JournalInk else JournalMutedInk,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        }
                                    }
                                }

                                // 6. Row: Devise ("Devise" on left, "DH | Rial" on right)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing)
                                        .drawBehind {
                                            drawLine(
                                                color = JournalRule.copy(alpha = 0.50f),
                                                start = Offset(0f, size.height),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 0.8.dp.toPx()
                                            )
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val currencyLabel = stringResource(R.string.new_calc_currency_label)
                                    Box(
                                        modifier = Modifier
                                            .width(labelColumnWidth)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = currencyLabel,
                                            fontFamily = resolveJournalFont(currencyLabel, isRtl),
                                            fontSize = if (isRtl) 13.sp else 13.5.sp,
                                            color = JournalMutedInk,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }

                                    // In same row: DH | Rial (starts at exact same X position as Personnel!)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val isDirham = selectedCurrency == MoneyUnit.DIRHAM
                                        val dirhamLabel = if (isRtl) "درهم (DH)" else "DH"
                                        Box(
                                            modifier = Modifier
                                                .height(25.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isDirham) JournalInk.copy(alpha = 0.08f)
                                                    else Color.Transparent
                                                )
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    role = Role.RadioButton
                                                ) { selectedCurrency = MoneyUnit.DIRHAM }
                                                .drawBehind {
                                                    if (isDirham) {
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
                                                .padding(horizontal = 10.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dirhamLabel,
                                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                fontSize = 13.5.sp,
                                                fontWeight = if (isDirham) FontWeight.Bold else FontWeight.Normal,
                                                color = JournalInk,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        }

                                        // Vertical divider
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(11.dp)
                                                .background(JournalRule.copy(alpha = 0.70f), RoundedCornerShape(0.5.dp))
                                        )

                                        val isRial = selectedCurrency == MoneyUnit.RIAL
                                        val rialLabel = if (isRtl) "ريال (rial)" else "Rial"
                                        Box(
                                            modifier = Modifier
                                                .height(25.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isRial) JournalInk.copy(alpha = 0.08f)
                                                    else Color.Transparent
                                                )
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    role = Role.RadioButton
                                                ) { selectedCurrency = MoneyUnit.RIAL }
                                                .drawBehind {
                                                    if (isRial) {
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
                                                .padding(horizontal = 10.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = rialLabel,
                                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                fontSize = 13.5.sp,
                                                fontWeight = if (isRial) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isRial) JournalInk else JournalMutedInk,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        }
                                    }
                                }

                                // 7. Row: Modèles de calcul ("Modèles de calcul" on left, clear selector on right)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing)
                                        .drawBehind {
                                            drawLine(
                                                color = JournalRule.copy(alpha = 0.50f),
                                                start = Offset(0f, size.height),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 0.8.dp.toPx()
                                            )
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val templatesLabel = stringResource(R.string.new_calc_templates_label)
                                    Box(
                                        modifier = Modifier
                                            .width(labelColumnWidth)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = templatesLabel,
                                            fontFamily = resolveJournalFont(templatesLabel, isRtl),
                                            fontSize = if (isRtl) 13.sp else 13.5.sp,
                                            color = JournalMutedInk,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }

                                    // In same row: Dropdown menu selector (starts at exact same X position as Personnel and DH!)
                                    Box(
                                        modifier = Modifier.wrapContentSize()
                                    ) {
                                        val hasModel = selectedModelName != null
                                        val buttonText = selectedModelName ?: if (isRtl) "اختر نموذجاً" else "Choisir un modèle"

                                        Box(
                                            modifier = Modifier
                                                .height(25.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (hasModel) HighlighterPink.copy(alpha = 0.12f)
                                                    else JournalInk.copy(alpha = 0.05f)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (hasModel) HighlighterPink else JournalRule.copy(alpha = 0.85f),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) { showModelDropdown = true }
                                                .padding(horizontal = 8.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                if (!hasModel) {
                                                    Text(
                                                        text = "📋",
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                Text(
                                                    text = buttonText,
                                                    fontFamily = resolveJournalFont(buttonText, isRtl),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (hasModel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (hasModel) JournalInk else JournalMutedInk,
                                                    style = TextStyle(platformStyle = NoFontPadding),
                                                    maxLines = 1
                                                )
                                                HisabiSketchIcon(
                                                    symbol = HisabiSymbol.ChevronDown,
                                                    contentDescription = null,
                                                    tint = if (hasModel) JournalInk else JournalMutedInk,
                                                    size = 10.dp
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = showModelDropdown,
                                            onDismissRequest = { showModelDropdown = false },
                                            modifier = Modifier
                                                .background(JournalPaper)
                                                .border(1.dp, JournalRule.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                        ) {
                                            // Explanatory Header
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = if (isRtl) "اختر نموذجاً لملء العنوان تلقائياً :" else "Modèles prêts pour le titre :",
                                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = JournalMutedInk,
                                                    style = TextStyle(platformStyle = NoFontPadding)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(JournalRule.copy(alpha = 0.35f))
                                            )

                                            // Clear option
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = if (isRtl) "✕ بدون نموذج (فارغ)" else "✕ Aucun modèle",
                                                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                        fontSize = 13.sp,
                                                        color = JournalActionDelete.copy(alpha = 0.85f)
                                                    )
                                                },
                                                onClick = {
                                                    selectedModelName = null
                                                    titleValue = TextFieldValue("")
                                                    showModelDropdown = false
                                                }
                                            )

                                            val sampleTemplates = if (isRtl) {
                                                listOf(
                                                    "🛒 سلعة وتجارة",
                                                    "🏗️ ورشة وبناء",
                                                    "☕ مصاريف يومية",
                                                    "🚗 تنقل وسفر",
                                                    "🏠 كراء ومنزل"
                                                )
                                            } else {
                                                listOf(
                                                    "🛒 Commerce & Marchandise",
                                                    "🏗️ Chantier & Travaux",
                                                    "☕ Dépenses quotidiennes",
                                                    "🚗 Transport & Carburant",
                                                    "🏠 Loyer & Maison"
                                                )
                                            }

                                            sampleTemplates.forEach { tpl ->
                                                val isSelected = selectedModelName == tpl || (selectedModelName != null && tpl.startsWith(selectedModelName!!.take(4)))
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            if (isSelected) {
                                                                Text(
                                                                    text = "✓",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = JournalInk
                                                                )
                                                            }
                                                            Text(
                                                                text = tpl,
                                                                fontFamily = resolveJournalFont(tpl, isRtl),
                                                                fontSize = 13.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                color = JournalInk
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedModelName = tpl
                                                        titleValue = TextFieldValue(
                                                            text = tpl,
                                                            selection = TextRange(tpl.length)
                                                        )
                                                        showModelDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // 8. Extra space between models and Commencer (larger than standard line)
                                Spacer(modifier = Modifier.height(38.dp))

                                // 9. Action Button: "Commencer" / "ابدأ الحساب"
                                val startLabel = stringResource(R.string.new_calc_start_action)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (selectedType == "CREDIT") Color(0xFFC2410C)
                                                else HighlighterPink.copy(alpha = 0.88f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (selectedType == "CREDIT") Color(0xFF9A3412) else JournalInk.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(role = Role.Button) { submit() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = startLabel,
                                            fontFamily = resolveJournalFont(startLabel, isRtl),
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedType == "CREDIT") Color.White else JournalInk,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 7. Custom In-App Keyboard Dock (Positioned at bottom with zero extra padding)
                    if (keyboardMode != JournalKeyboardMode.NONE) {
                        when (keyboardMode) {
                            JournalKeyboardMode.TEXT -> {
                                JournalTextKeyboardDock(
                                    language = keyboardLanguage,
                                    shiftMode = shiftState.mode,
                                    expanded = keyboardExpanded,
                                    onToggleExpand = { keyboardExpanded = !keyboardExpanded },
                                    onCycleLanguage = {
                                        keyboardLanguage = when (keyboardLanguage) {
                                            JournalKeyboardLanguage.FRENCH -> JournalKeyboardLanguage.ARABIC
                                            JournalKeyboardLanguage.ARABIC -> JournalKeyboardLanguage.ENGLISH
                                            JournalKeyboardLanguage.ENGLISH -> JournalKeyboardLanguage.FRENCH
                                        }
                                    },
                                    onSelectLanguage = { keyboardLanguage = it },
                                    onToggleShift = {
                                        shiftState = JournalKeyboardController.reduceShift(
                                            state = shiftState,
                                            action = ShiftAction.UserTapShift(SystemClock.uptimeMillis()),
                                            monotonicNow = { SystemClock.uptimeMillis() }
                                        )
                                    },
                                    onInsertText = { char ->
                                        titleValue = JournalKeyboardController.insertText(titleValue, char)
                                        if (keyboardLanguage != JournalKeyboardLanguage.ARABIC) {
                                            shiftState = JournalKeyboardController.reduceShift(
                                                state = shiftState,
                                                action = ShiftAction.UserTypedText(char),
                                                monotonicNow = { SystemClock.uptimeMillis() }
                                            )
                                        }
                                    },
                                    onBackspace = {
                                        titleValue = JournalKeyboardController.deleteBackward(titleValue, graphemeSegmenter)
                                    },
                                    onSwitchToNumericMode = {
                                        keyboardMode = JournalKeyboardMode.NUMBER
                                    },
                                    onConfirm = {
                                        keyboardMode = JournalKeyboardMode.NONE
                                    }
                                )
                            }
                            JournalKeyboardMode.NUMBER -> {
                                JournalCompactNumericDock(
                                    expanded = keyboardExpanded,
                                    onToggleExpand = { keyboardExpanded = !keyboardExpanded },
                                    onKey = { key ->
                                        if (key == "⌫") {
                                            titleValue = JournalKeyboardController.deleteBackward(titleValue, graphemeSegmenter)
                                        } else {
                                            titleValue = JournalKeyboardController.insertText(titleValue, key)
                                        }
                                    },
                                    onSwitchToTextMode = {
                                        keyboardMode = JournalKeyboardMode.TEXT
                                    },
                                    onConfirm = {
                                        keyboardMode = JournalKeyboardMode.NONE
                                    }
                                )
                            }
                            JournalKeyboardMode.NONE -> {}
                        }
                    }
                }
            }
        }
    }
}

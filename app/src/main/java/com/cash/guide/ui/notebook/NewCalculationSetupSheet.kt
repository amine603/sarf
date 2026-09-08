package com.cash.guide.ui.notebook

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
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
 * - Centered on screen as an authentic ruled notebook sheet.
 * - Background features 29.dp horizontal ruling lines and pencil margin.
 * - Selection options styled as notebook washi tabs / stickers with pastel washes.
 * - Input line sits directly on the notebook ruled line with blinking pencil cursor.
 * - In-app keyboard docks smoothly at the bottom with zero excess padding.
 * - Android Back button dismisses only the keyboard when open, or the dialog when closed.
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
                    // Centered Area: Holds the Notebook Card in upper/middle space
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Authentic Notebook Paper Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 420.dp)
                                .shadow(elevation = 10.dp, shape = RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = 1.2.dp,
                                    color = JournalRule.copy(alpha = 0.90f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { /* Prevent clicks on the card from dismissing */ },
                            color = JournalPaper,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawBehind {
                                        val grid = JournalRuleSpacing.toPx() // 29.dp rhythm
                                        var y = grid
                                        while (y <= size.height) {
                                            drawLine(
                                                color = JournalRule.copy(alpha = 0.45f),
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = 0.6.dp.toPx()
                                            )
                                            y += grid
                                        }

                                        // Left/Right subtle notebook margin rule
                                        val marginX = if (isRtl) size.width - 24.dp.toPx() else 24.dp.toPx()
                                        drawLine(
                                            color = JournalRule.copy(alpha = 0.30f),
                                            start = Offset(marginX, 0f),
                                            end = Offset(marginX, size.height),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                                // 1. Header: Title and Close button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val sheetTitle = stringResource(R.string.new_calc_sheet_title)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(JournalInk, CircleShape)
                                        )
                                        Text(
                                            text = sheetTitle,
                                            fontFamily = resolveJournalFont(sheetTitle, isRtl),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalInk,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }

                                    // Hand-drawn sketch '✕' close button
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .clickable(role = Role.Button) { onDismiss() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✕",
                                            fontFamily = JournalHandFamily,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalMutedInk
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // 2. Type Selection (شخصي / كريدي) - Notebook Tab Stickers
                                val typeSectionLabel = stringResource(R.string.new_calc_type_label)
                                Text(
                                    text = typeSectionLabel,
                                    fontFamily = resolveJournalFont(typeSectionLabel, isRtl),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = JournalMutedInk,
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Option: Personnel (Notebook Tab)
                                    val isPersonnel = selectedType == "PERSONNEL"
                                    val personnelLabel = stringResource(R.string.calc_type_personnel)
                                    val personnelBg by animateColorAsState(
                                        targetValue = if (isPersonnel) HighlighterYellow.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.25f),
                                        label = "personnelTabBg"
                                    )
                                    val personnelBorderColor by animateColorAsState(
                                        targetValue = if (isPersonnel) JournalInk.copy(alpha = 0.65f) else JournalRule.copy(alpha = 0.70f),
                                        label = "personnelBorderColor"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(personnelBg)
                                            .border(
                                                width = if (isPersonnel) 1.2.dp else 0.8.dp,
                                                color = personnelBorderColor,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(role = Role.RadioButton) { selectedType = "PERSONNEL" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (isPersonnel) "✓ ✏️" else "✏️",
                                                fontFamily = resolveJournalFont(personnelLabel, isRtl),
                                                fontSize = 13.sp,
                                                color = JournalInk,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                            Text(
                                                text = personnelLabel,
                                                fontFamily = resolveJournalFont(personnelLabel, isRtl),
                                                fontSize = 14.5.sp,
                                                fontWeight = if (isPersonnel) FontWeight.Bold else FontWeight.Normal,
                                                color = JournalInk,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        }
                                    }

                                    // Option: Crédit (Notebook Tab)
                                    val isCredit = selectedType == "CREDIT"
                                    val creditLabel = stringResource(R.string.calc_type_credit)
                                    val creditBg by animateColorAsState(
                                        targetValue = if (isCredit) HighlighterPink.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.25f),
                                        label = "creditTabBg"
                                    )
                                    val creditBorderColor by animateColorAsState(
                                        targetValue = if (isCredit) Color(0xFFC2410C) else JournalRule.copy(alpha = 0.70f),
                                        label = "creditBorderColor"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(creditBg)
                                            .border(
                                                width = if (isCredit) 1.2.dp else 0.8.dp,
                                                color = creditBorderColor,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(role = Role.RadioButton) { selectedType = "CREDIT" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (isCredit) "✓ 🏷️" else "🏷️",
                                                fontFamily = resolveJournalFont(creditLabel, isRtl),
                                                fontSize = 13.sp,
                                                color = if (isCredit) Color(0xFFC2410C) else JournalInk,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                            Text(
                                                text = creditLabel,
                                                fontFamily = resolveJournalFont(creditLabel, isRtl),
                                                fontSize = 14.5.sp,
                                                fontWeight = if (isCredit) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCredit) Color(0xFFC2410C) else JournalInk,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // 3. Nom du calcul ou client - Written on Ruled Paper Line
                                val nameLabel = stringResource(R.string.new_calc_name_label)
                                Text(
                                    text = nameLabel,
                                    fontFamily = resolveJournalFont(nameLabel, isRtl),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = JournalMutedInk,
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

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

                                // Handwritten line container with pencil baseline
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.40f))
                                        .border(
                                            width = if (isEditingName) 1.2.dp else 0.8.dp,
                                            color = if (isEditingName) JournalInk.copy(alpha = 0.65f) else JournalRule.copy(alpha = 0.75f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            keyboardMode = JournalKeyboardMode.TEXT
                                        }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (titleValue.text.isEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable {
                                                    keyboardMode = JournalKeyboardMode.TEXT
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isEditingName && cursorAlpha > 0.5f) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(2.dp)
                                                        .height(17.dp)
                                                        .background(JournalInk)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                            }
                                            Text(
                                                text = namePlaceholder,
                                                fontFamily = resolveJournalFont(namePlaceholder, isRtl),
                                                fontSize = 13.5.sp,
                                                color = JournalMutedInk.copy(alpha = 0.55f),
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    keyboardMode = JournalKeyboardMode.TEXT
                                                }
                                                .horizontalScroll(titleScrollState),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = titleValue.text,
                                                fontFamily = resolveJournalFont(titleValue.text, isRtl),
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = JournalInk,
                                                maxLines = 1,
                                                softWrap = false,
                                                style = TextStyle(platformStyle = NoFontPadding),
                                                onTextLayout = { titleLayoutResult = it },
                                                modifier = Modifier.drawWithContent {
                                                    drawContent()
                                                    if (isEditingName && cursorAlpha > 0.5f) {
                                                        val layout = titleLayoutResult
                                                        val cursorX = if (layout != null && titleValue.text.isNotEmpty()) {
                                                            val offset = titleValue.selection.end.coerceIn(0, titleValue.text.length)
                                                            layout.getCursorRect(offset).left
                                                        } else {
                                                            0f
                                                        }
                                                        val topY = 3.dp.toPx()
                                                        val bottomY = size.height - 3.dp.toPx()
                                                        drawLine(
                                                            color = JournalInk,
                                                            start = Offset(cursorX, topY),
                                                            end = Offset(cursorX, bottomY),
                                                            strokeWidth = 2.dp.toPx()
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // 4. Devise (Currency: DH vs Rial) - Notebook Sticker Stamps
                                val currencyLabel = stringResource(R.string.new_calc_currency_label)
                                Text(
                                    text = currencyLabel,
                                    fontFamily = resolveJournalFont(currencyLabel, isRtl),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = JournalMutedInk,
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val isDirham = selectedCurrency == MoneyUnit.DIRHAM
                                    val dirhamBg by animateColorAsState(
                                        targetValue = if (isDirham) HighlighterYellow.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.25f),
                                        label = "dirhamTabBg"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(dirhamBg)
                                            .border(
                                                width = if (isDirham) 1.2.dp else 0.8.dp,
                                                color = if (isDirham) JournalInk.copy(alpha = 0.60f) else JournalRule.copy(alpha = 0.70f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(role = Role.RadioButton) { selectedCurrency = MoneyUnit.DIRHAM },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isRtl) "درهم (DH)" else "DH (Dirham)",
                                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                            fontSize = 14.sp,
                                            fontWeight = if (isDirham) FontWeight.Bold else FontWeight.Normal,
                                            color = JournalInk,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }

                                    val isRial = selectedCurrency == MoneyUnit.RIAL
                                    val rialBg by animateColorAsState(
                                        targetValue = if (isRial) HighlighterYellow.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.25f),
                                        label = "rialTabBg"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(rialBg)
                                            .border(
                                                width = if (isRial) 1.2.dp else 0.8.dp,
                                                color = if (isRial) JournalInk.copy(alpha = 0.60f) else JournalRule.copy(alpha = 0.70f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(role = Role.RadioButton) { selectedCurrency = MoneyUnit.RIAL },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isRtl) "ريال (rial)" else "Rial (ريال)",
                                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                            fontSize = 14.sp,
                                            fontWeight = if (isRial) FontWeight.Bold else FontWeight.Normal,
                                            color = JournalInk,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // 5. Modèles de calcul (Templates) - Stamped Tags
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val templatesLabel = stringResource(R.string.new_calc_templates_label)
                                    Text(
                                        text = templatesLabel,
                                        fontFamily = resolveJournalFont(templatesLabel, isRtl),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = JournalMutedInk,
                                        style = TextStyle(platformStyle = NoFontPadding)
                                    )

                                    val soonLabel = stringResource(R.string.new_calc_templates_soon)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(JournalRule.copy(alpha = 0.35f))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = soonLabel,
                                            fontFamily = resolveJournalFont(soonLabel, isRtl),
                                            fontSize = 9.5.sp,
                                            color = JournalMutedInk,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val sampleTemplates = if (isRtl) {
                                        listOf("🛒 سلعة وتجارة", "🏗️ ورشة وبناء", "☕ مصاريف يومية")
                                    } else {
                                        listOf("🛒 Commerce", "🏗️ Chantier", "☕ Dépenses")
                                    }

                                    for (tpl in sampleTemplates) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.White.copy(alpha = 0.30f))
                                                .border(0.7.dp, JournalRule.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                                                .clickable {
                                                    titleValue = TextFieldValue(tpl)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tpl,
                                                fontFamily = resolveJournalFont(tpl, isRtl),
                                                fontSize = 11.sp,
                                                color = JournalMutedInk.copy(alpha = 0.70f),
                                                maxLines = 1,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // 6. Action Button: "Commencer" / "ابدأ الحساب" - Notebook Marker Strip
                                val startLabel = stringResource(R.string.new_calc_start_action)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (selectedType == "CREDIT") Color(0xFFC2410C)
                                            else HighlighterPink.copy(alpha = 0.88f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (selectedType == "CREDIT") Color(0xFF9A3412) else JournalInk.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable(role = Role.Button) { submit() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = startLabel,
                                        fontFamily = resolveJournalFont(startLabel, isRtl),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedType == "CREDIT") Color.White else JournalInk,
                                        style = TextStyle(platformStyle = NoFontPadding)
                                    )
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

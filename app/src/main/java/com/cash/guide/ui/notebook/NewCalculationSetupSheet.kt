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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.AndroidIcuGraphemeSegmenter
import com.cash.guide.domain.JournalKeyboardController
import com.cash.guide.domain.JournalKeyboardLanguage
import com.cash.guide.domain.JournalKeyboardMode
import com.cash.guide.domain.JournalShiftMode
import com.cash.guide.domain.JournalShiftState
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.domain.ShiftAction

@OptIn(ExperimentalMaterial3Api::class)
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

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun submit() {
        onConfirm(titleValue.text.trim(), selectedType, selectedCurrency)
    }

    fun dismissWithAnimation() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        ),
        containerColor = JournalPaper,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(JournalRule.copy(alpha = 0.75f), RoundedCornerShape(2.dp))
                )
            }
        }
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
                    dismissWithAnimation()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .then(
                        if (keyboardMode != JournalKeyboardMode.NONE) Modifier.fillMaxHeight(0.92f)
                        else Modifier
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp)
                ) {
                    // Header: Title sitting on notebook paper
                val sheetTitle = stringResource(R.string.new_calc_sheet_title)
                Text(
                    text = sheetTitle,
                    fontFamily = resolveJournalFont(sheetTitle, isRtl),
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    textAlign = TextAlign.Start,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Type Selection (شخصي / كريدي)
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
                    // Option: Personnel
                    val isPersonnel = selectedType == "PERSONNEL"
                    val personnelLabel = stringResource(R.string.calc_type_personnel)
                    val personnelBg by animateColorAsState(
                        targetValue = if (isPersonnel) HighlighterBlue.copy(alpha = 0.55f) else Color.Transparent,
                        label = "personnelBg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(personnelBg)
                            .border(
                                width = if (isPersonnel) 1.5.dp else 1.dp,
                                color = if (isPersonnel) JournalInk.copy(alpha = 0.55f) else JournalRule,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedType = "PERSONNEL" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📝 $personnelLabel",
                            fontFamily = resolveJournalFont(personnelLabel, isRtl),
                            fontSize = 14.5.sp,
                            fontWeight = if (isPersonnel) FontWeight.Bold else FontWeight.Normal,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }

                    // Option: Crédit
                    val isCredit = selectedType == "CREDIT"
                    val creditLabel = stringResource(R.string.calc_type_credit)
                    val creditBg by animateColorAsState(
                        targetValue = if (isCredit) Color(0xFFFFF1EB) else Color.Transparent,
                        label = "creditBg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(creditBg)
                            .border(
                                width = if (isCredit) 1.5.dp else 1.dp,
                                color = if (isCredit) Color(0xFFC2410C) else JournalRule,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedType = "CREDIT" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏷️ $creditLabel",
                            fontFamily = resolveJournalFont(creditLabel, isRtl),
                            fontSize = 14.5.sp,
                            fontWeight = if (isCredit) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCredit) Color(0xFFC2410C) else JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Calculation Name / Client Name
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.55f))
                            .border(
                                width = if (isEditingName) 1.5.dp else 1.dp,
                                color = if (isEditingName) JournalInk.copy(alpha = 0.65f) else JournalRule,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                keyboardMode = JournalKeyboardMode.TEXT
                            }
                            .padding(horizontal = 12.dp),
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
                                            .height(18.dp)
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

                // Section 3: Currency Selection (درهم / ريال)
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
                        targetValue = if (isDirham) HighlighterYellow.copy(alpha = 0.65f) else Color.Transparent,
                        label = "dirhamBg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(dirhamBg)
                            .border(
                                width = if (isDirham) 1.5.dp else 1.dp,
                                color = if (isDirham) JournalInk.copy(alpha = 0.50f) else JournalRule,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCurrency = MoneyUnit.DIRHAM },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRtl) "درهم (DH)" else "DH (Dirham)",
                            fontFamily = if (isRtl) CreamFrothFamily else PatrickHandFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isDirham) FontWeight.Bold else FontWeight.Normal,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }

                    val isRial = selectedCurrency == MoneyUnit.RIAL
                    val rialBg by animateColorAsState(
                        targetValue = if (isRial) HighlighterYellow.copy(alpha = 0.65f) else Color.Transparent,
                        label = "rialBg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(rialBg)
                            .border(
                                width = if (isRial) 1.5.dp else 1.dp,
                                color = if (isRial) JournalInk.copy(alpha = 0.50f) else JournalRule,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCurrency = MoneyUnit.RIAL },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRtl) "ريال (rial)" else "Rial (ريال)",
                            fontFamily = if (isRtl) CreamFrothFamily else PatrickHandFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isRial) FontWeight.Bold else FontWeight.Normal,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 4: Upcoming Templates Slot ("قوالب جاهزة")
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
                            .background(JournalRule.copy(alpha = 0.50f))
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
                                .background(Color.White.copy(alpha = 0.35f))
                                .border(0.8.dp, JournalRule.copy(alpha = 0.70f), RoundedCornerShape(6.dp))
                                .clickable {
                                    titleValue = TextFieldValue(tpl)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tpl,
                                fontFamily = resolveJournalFont(tpl, isRtl),
                                fontSize = 11.5.sp,
                                color = JournalMutedInk.copy(alpha = 0.65f),
                                maxLines = 1,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button: "Commencer" / "ابدأ الحساب"
                val startLabel = stringResource(R.string.new_calc_start_action)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedType == "CREDIT") Color(0xFFC2410C)
                            else HighlighterPink.copy(alpha = 0.85f)
                        )
                        .clickable { submit() },
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

                Spacer(modifier = Modifier.height(if (keyboardMode != JournalKeyboardMode.NONE) 12.dp else 32.dp))
            }

            // Custom In-App Keyboard Dock
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


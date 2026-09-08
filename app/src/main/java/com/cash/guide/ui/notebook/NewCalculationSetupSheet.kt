package com.cash.guide.ui.notebook

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.MoneyUnit

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

    var titleText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PERSONNEL") } // "PERSONNEL" or "CREDIT"
    var selectedCurrency by remember { mutableStateOf(defaultCurrency) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun submit() {
        onConfirm(titleText.trim(), selectedType, selectedCurrency)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .navigationBarsPadding()
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.50f))
                        .border(1.dp, JournalRule, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (titleText.isEmpty()) {
                        Text(
                            text = namePlaceholder,
                            fontFamily = resolveJournalFont(namePlaceholder, isRtl),
                            fontSize = 13.5.sp,
                            color = JournalMutedInk.copy(alpha = 0.55f),
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }
                    BasicTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        singleLine = true,
                        cursorBrush = SolidColor(JournalInk),
                        textStyle = TextStyle(
                            fontFamily = resolveJournalFont(titleText, isRtl),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = JournalInk,
                            platformStyle = NoFontPadding
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        modifier = Modifier.fillMaxWidth()
                    )
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
                                .border(0.8.dp, JournalRule.copy(alpha = 0.70f), RoundedCornerShape(6.dp)),
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

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

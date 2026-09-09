package com.cash.guide.feature.cashregister

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.MoneyMath
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.notebook.BreakdownDenominationBlock
import com.cash.guide.ui.notebook.ColorCoral
import com.cash.guide.ui.notebook.ColorOrange
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.JournalDockBg
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.NotebookHighlightedBadge
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.isArabicScript
import com.cash.guide.ui.notebook.resolveJournalFont

private val ColorEmerald = Color(0xFF2E7D32)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CashRegisterScreen(
    viewModel: CashRegisterViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val context = LocalContext.current

    val currencySuffix = if (state.currencyUnit == MoneyUnit.DIRHAM) {
        stringResource(R.string.currency_dirham)
    } else {
        stringResource(R.string.currency_rial)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JournalPaper)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Text(
                            text = if (isRtl) "→" else "←",
                            fontFamily = PatrickHandFamily,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk
                        )
                    }

                    NotebookHighlightedBadge(
                        text = stringResource(R.string.cash_register_title),
                        highlighterColor = HighlighterYellow.copy(alpha = 0.5f),
                        fontSize = 17.sp,
                        horizontalPadding = 12.dp,
                        verticalPadding = 4.dp
                    )
                }

                // Currency toggle chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(JournalDockBg)
                        .border(BorderStroke(0.8.dp, JournalRule.copy(alpha = 0.8f)), RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button) { viewModel.toggleCurrency() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currencySuffix,
                        fontFamily = resolveJournalFont(currencySuffix, isRtl),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                    Text(
                        text = "⇅",
                        fontSize = 12.sp,
                        color = JournalMutedInk
                    )
                }
            }

            // Notebook ruled lines content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .drawBehind {
                        val spacingPx = JournalRuleSpacing.roundToPx().toFloat()
                        val lineCount = (size.height / spacingPx).toInt() + 1
                        val stroke = 0.6.dp.toPx()
                        val lineColor = JournalRule.copy(alpha = 0.28f)
                        for (i in 1..lineCount) {
                            val y = i * spacingPx
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = stroke
                            )
                        }
                    }
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // SECTION 1: Purchase Total
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(JournalPaper)
                            .border(BorderStroke(1.dp, JournalRule.copy(alpha = 0.8f)), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        val labelPurchase = stringResource(R.string.cash_register_purchase_total)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🛒", fontSize = 16.sp)
                                Text(
                                    text = labelPurchase,
                                    fontFamily = resolveJournalFont(labelPurchase, isRtl),
                                    fontSize = if (isRtl) 14.5.sp else 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalInk,
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )
                            }

                            if (state.purchaseText.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.cash_register_clear_input),
                                    fontFamily = resolveJournalFont(stringResource(R.string.cash_register_clear_input), isRtl),
                                    fontSize = 12.sp,
                                    color = ColorCoral,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setPurchaseText("") }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(JournalDockBg)
                                .border(BorderStroke(0.6.dp, JournalRule.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BasicTextField(
                                value = state.purchaseText,
                                onValueChange = { viewModel.setPurchaseText(it) },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 24.sp,
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
                                            fontSize = 24.sp,
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalMutedInk
                            )
                        }
                    }
                }

                // SECTION 2: Amount Received
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(JournalPaper)
                            .border(BorderStroke(1.dp, JournalRule.copy(alpha = 0.8f)), RoundedCornerShape(12.dp))
                            .padding(14.dp)
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
                                Text("💵", fontSize = 16.sp)
                                Text(
                                    text = labelReceived,
                                    fontFamily = resolveJournalFont(labelReceived, isRtl),
                                    fontSize = if (isRtl) 14.5.sp else 15.sp,
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
                                        .clickable { viewModel.setReceivedText("") }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(JournalDockBg)
                                .border(BorderStroke(0.6.dp, JournalRule.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BasicTextField(
                                value = state.receivedText,
                                onValueChange = { viewModel.setReceivedText(it) },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 24.sp,
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
                                            fontSize = 24.sp,
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalMutedInk
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Quick Note Chips: [20 DH] [50 DH] [100 DH] [200 DH]
                        val presetNotes = listOf(20L, 50L, 100L, 200L)
                        Text(
                            text = stringResource(R.string.cash_register_quick_presets),
                            fontFamily = resolveJournalFont(stringResource(R.string.cash_register_quick_presets), isRtl),
                            fontSize = 12.sp,
                            color = JournalMutedInk,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HighlighterYellow.copy(alpha = 0.25f))
                                        .border(
                                            BorderStroke(0.8.dp, JournalRule.copy(alpha = 0.7f)),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectPresetReceived(noteDh) }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = chipText,
                                        fontFamily = PatrickHandFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JournalWritingInk,
                                        style = TextStyle(platformStyle = NoFontPadding)
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 3: Change Due / Status Banner
                if (state.changeCentimes > 0L) {
                    item {
                        val changeDh = MoneyMath.fromCentimes(state.changeCentimes, MoneyUnit.DIRHAM)
                        val changeRial = MoneyMath.fromCentimes(state.changeCentimes, MoneyUnit.RIAL)
                        val changeFormatted = if (state.currencyUnit == MoneyUnit.DIRHAM) changeDh else changeRial
                        val changeLabel = stringResource(R.string.cash_register_change_due)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(HighlighterYellow.copy(alpha = 0.40f))
                                .border(
                                    BorderStroke(1.2.dp, HighlighterPink.copy(alpha = 0.7f)),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = changeLabel,
                                fontFamily = resolveJournalFont(changeLabel, isRtl),
                                fontSize = if (isRtl) 15.sp else 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalInk,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = changeFormatted,
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorOrange
                                )
                                Text(
                                    text = currencySuffix,
                                    fontFamily = resolveJournalFont(currencySuffix, isRtl),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalInk,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }

                            // Secondary Rial / Dirham representation
                            val secondaryText = if (state.currencyUnit == MoneyUnit.DIRHAM) {
                                "= $changeRial ${stringResource(R.string.currency_rial)}"
                            } else {
                                "= $changeDh ${stringResource(R.string.currency_dirham)}"
                            }
                            Text(
                                text = secondaryText,
                                fontFamily = resolveJournalFont(secondaryText, isRtl),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = JournalMutedInk
                            )
                        }
                    }
                } else if (state.isExactAmount) {
                    item {
                        val exactText = stringResource(R.string.cash_register_exact_amount)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorEmerald.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, ColorEmerald.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓ $exactText",
                                fontFamily = resolveJournalFont(exactText, isRtl),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorEmerald
                            )
                        }
                    }
                } else if (state.isInsufficient && state.shortageCentimes > 0L) {
                    item {
                        val shortageDh = MoneyMath.fromCentimes(state.shortageCentimes, state.currencyUnit)
                        val shortageMsg = stringResource(R.string.cash_register_insufficient, "$shortageDh $currencySuffix")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorCoral.copy(alpha = 0.12f))
                                .border(BorderStroke(1.dp, ColorCoral.copy(alpha = 0.6f)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚠️ $shortageMsg",
                                fontFamily = resolveJournalFont(shortageMsg, isRtl),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorCoral
                            )
                        }
                    }
                }

                // SECTION 4: Real Banknotes & Coins breakdown
                if (state.pieces.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(14.dp)
                                    .background(HighlighterPink.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                            )
                            val heading = stringResource(R.string.cash_register_pieces_heading)
                            Text(
                                text = heading,
                                fontFamily = resolveJournalFont(heading, isRtl),
                                color = JournalWritingInk,
                                fontSize = if (isRtl) 14.sp else 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(0.6.dp)
                                    .background(JournalRule.copy(alpha = 0.6f))
                            )
                        }
                    }

                    state.pieces.forEach { piece ->
                        item(key = piece.denomination.label) {
                            BreakdownDenominationBlock(piece = piece)
                        }
                    }
                }
            }

            // Bottom bar: Client suivant button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JournalPaper)
                    .border(BorderStroke(0.6.dp, JournalRule.copy(alpha = 0.4f)), RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Button(
                    onClick = { viewModel.clear() },
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
                        Text("↺", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = nextText,
                            fontFamily = resolveJournalFont(nextText, isRtl),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }
                }
            }
        }
    }
}

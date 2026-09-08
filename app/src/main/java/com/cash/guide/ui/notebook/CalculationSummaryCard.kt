package com.cash.guide.ui.notebook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.cash.guide.R
import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.JournalLedgerManager
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.HighlighterGreen
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow

@Composable
fun CalculationSummaryCard(
    calculationWithItems: CalculationWithItems,
    searchQuery: String? = null,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calc = calculationWithItems.calculation
    val currency = runCatching { MoneyUnit.valueOf(calc.currency) }.getOrDefault(MoneyUnit.DIRHAM)
    val totalFormatted = JournalLedgerManager.formatTotal(calculationWithItems.totalCentimes, currency)
    val currencySuffix = if (currency == MoneyUnit.DIRHAM) {
        stringResource(R.string.currency_dirham)
    } else {
        stringResource(R.string.currency_rial)
    }

    // Find matching item snippet if searchQuery is present
    val matchingItemSnippet = if (!searchQuery.isNullOrBlank()) {
        val q = searchQuery.trim().lowercase()
        calculationWithItems.items.firstOrNull { it.label.lowercase().contains(q) }?.let { item ->
            val itemAmount = JournalLedgerManager.formatFrenchNumber(
                com.cash.guide.domain.MoneyMath.fromCentimes(item.amountCentimes, currency)
            )
            "${item.label} — $itemAmount $currencySuffix"
        }
    } else null

    val pastelColors = listOf(
        HighlighterPink.copy(alpha = 0.45f),
        HighlighterYellow.copy(alpha = 0.50f),
        HighlighterGreen.copy(alpha = 0.45f),
        HighlighterBlue.copy(alpha = 0.45f)
    )
    val badgeTint = pastelColors[kotlin.math.abs(calc.id.hashCode()) % pastelColors.size]
    val isLatinSuffix = currencySuffix.contains(Regex("[a-zA-Z]"))
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = JournalPaper,
        border = BorderStroke(0.8.dp, JournalRule.copy(alpha = 0.50f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main clickable card area
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decorative notebook badge
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = badgeTint
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Page,
                            contentDescription = null,
                            tint = JournalInk,
                            size = 15.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val displayTitle = calc.title.ifBlank { stringResource(R.string.editor_new_title) }
                    Text(
                        text = displayTitle,
                        fontFamily = resolveJournalFont(displayTitle, isRtl),
                        fontSize = if (isArabicScript(displayTitle) || isRtl) 15.sp else 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Match snippet if present
                    if (matchingItemSnippet != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HighlighterPink.copy(alpha = 0.40f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = matchingItemSnippet,
                                fontFamily = resolveJournalFont(matchingItemSnippet, isRtl),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = JournalWritingInk
                            )
                        }
                    }

                }

                Spacer(modifier = Modifier.width(8.dp))

                // Total Amount & Unit (handwritten font unified with editor)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = totalFormatted,
                        fontFamily = PatrickHandFamily,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = androidx.compose.ui.text.TextStyle(platformStyle = NoFontPadding)
                    )
                    Text(
                        text = currencySuffix,
                        fontFamily = if (isLatinSuffix) PatrickHandFamily else TajawalFamily,
                        fontSize = if (isLatinSuffix) 13.5.sp else 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = JournalMutedInk,
                        style = androidx.compose.ui.text.TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }

            // More Options (dedicated 48dp target)
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onMoreClick),
                contentAlignment = Alignment.Center
            ) {
                HisabiSketchIcon(
                    symbol = HisabiSymbol.More,
                    contentDescription = stringResource(R.string.cd_more_options),
                    tint = JournalMutedInk,
                    size = 20.dp
                )
            }
        }
    }
}

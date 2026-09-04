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
import com.cash.guide.R
import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.JournalLedgerManager
import com.cash.guide.domain.MoneyUnit

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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = JournalDockBg.copy(alpha = 0.65f),
        border = BorderStroke(0.65.dp, JournalRule.copy(alpha = 0.75f)),
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
                    .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = calc.title.ifBlank { stringResource(R.string.editor_new_title) },
                        fontFamily = PatrickHandFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Match snippet if present
                    if (matchingItemSnippet != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(HighlighterYellow.copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = matchingItemSnippet,
                                fontFamily = ManropeFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = JournalWritingInk
                            )
                        }
                    }

                    // Row count
                    val lineCount = calculationWithItems.items.size
                    Text(
                        text = stringResource(
                            if (lineCount == 1) R.string.card_lines_singular else R.string.card_lines_plural,
                            lineCount
                        ),
                        fontFamily = ManropeFamily,
                        fontSize = 12.sp,
                        color = JournalMutedInk
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Total Amount & Unit
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = totalFormatted,
                        fontFamily = ManropeFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk
                    )
                    Text(
                        text = currencySuffix,
                        fontFamily = ManropeFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JournalMutedInk
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

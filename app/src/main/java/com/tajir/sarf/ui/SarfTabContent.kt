package com.tajir.sarf.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.tajir.sarf.data.Denominations
import com.tajir.sarf.domain.ChangeCalculator
import com.tajir.sarf.domain.ChangeResult
import com.tajir.sarf.ui.components.DenominationCard
import com.tajir.sarf.ui.components.MoneyAmountText
import com.tajir.sarf.ui.components.AdMobBanner
import com.tajir.sarf.ui.components.rememberIsOnline
import com.tajir.sarf.ui.components.rememberInterstitialAdController
import com.tajir.sarf.utils.MoneyInputParser
import androidx.compose.ui.res.stringResource
import com.tajir.sarf.R
import com.tajir.sarf.ui.theme.SarfLineAlt

@Composable
fun SarfTabContent(
    priceText: String,
    onPriceTextChange: (String) -> Unit,
    paidText: String,
    onPaidTextChange: (String) -> Unit,
    result: ChangeResult?,
    onResultChange: (ChangeResult?) -> Unit,
    errorMessage: String?,
    onErrorMessageChange: (String?) -> Unit,
    localCurrencyCode: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOnline = rememberIsOnline()
    val interstitial = rememberInterstitialAdController(isOnline = isOnline)
    var calculateCount by rememberSaveable { mutableIntStateOf(0) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = Color.White.copy(alpha = 0.22f)
    val fieldColors = if (isDark) {
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedLabelColor = Color.White.copy(alpha = 0.9f),
            unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
            cursorColor = Color.White
        )
    } else {
        OutlinedTextFieldDefaults.colors()
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val excludedDenoms = rememberSaveable { mutableStateListOf<Int>() }
    var lastPriceCents by remember { mutableStateOf<Int?>(null) }
    var lastPaidCents by remember { mutableStateOf<Int?>(null) }
    val invalidPriceFallback = stringResource(R.string.error_invalid_price)
    val invalidPaidFallback = stringResource(R.string.error_invalid_paid)

    val cols = 3
    fun fullSpan() = GridItemSpan(cols)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { fullSpan() }) {
                val cardShape = MaterialTheme.shapes.large
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isDark) borderColor else MaterialTheme.colorScheme.outline,
                            cardShape
                        ),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    val success = result as? ChangeResult.Success
                    if (success != null) {
                        // Result mode: keep the same card but show big result to free space for money breakdown below.
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val priceInline = Denominations.formatCentsCompactNumber(lastPriceCents ?: 0)
                                val paidInline = Denominations.formatCentsCompactNumber(lastPaidCents ?: 0)

                                Text(
                                    text = buildAnnotatedString {
                                        append(stringResource(R.string.price_label))
                                        append(": ")
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                        ) { append(priceInline) }
                                        append("    ")
                                        append(stringResource(R.string.paid_label))
                                        append(": ")
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                        ) { append(paidInline) }
                                    },
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                TextButton(onClick = { onResultChange(null) }) { Text(stringResource(R.string.edit)) }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary,
                                        androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                                    )
                                    .padding(horizontal = 22.dp, vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                MoneyAmountText(
                                    cents = success.totalChangeCents,
                                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    currencyCode = localCurrencyCode
                                )
                            }
                        }
                    } else {
                        // Input mode
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = {
                                    onPriceTextChange(MoneyInputParser.sanitizeForTyping(it))
                                    onErrorMessageChange(null)
                                    onResultChange(null)
                                },
                                label = { Text(stringResource(R.string.price_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = fieldColors,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { /* default focus traversal */ }
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = paidText,
                                onValueChange = {
                                    onPaidTextChange(MoneyInputParser.sanitizeForTyping(it))
                                    onErrorMessageChange(null)
                                    onResultChange(null)
                                },
                                label = { Text(stringResource(R.string.paid_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = fieldColors,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                ),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        // Hide keyboard and clear focus so results are visible.
                                        keyboardController?.hide()
                                        focusManager.clearFocus()

                                        onErrorMessageChange(null)
                                        val priceCents = MoneyInputParser.parseToCents(priceText)
                                            .getOrElse {
                                                onErrorMessageChange(it.message ?: invalidPriceFallback)
                                                onResultChange(null)
                                                return@OutlinedButton
                                            }
                                        val paidCents = MoneyInputParser.parseToCents(paidText)
                                            .getOrElse {
                                                onErrorMessageChange(it.message ?: invalidPaidFallback)
                                                onResultChange(null)
                                                return@OutlinedButton
                                            }

                                        lastPriceCents = priceCents
                                        lastPaidCents = paidCents
                                        val r = ChangeCalculator.calculateChangeBreakdown(
                                            priceCents,
                                            paidCents,
                                            excludedDenominationsCents = excludedDenoms.toSet(),
                                            denominations = Denominations.allForCurrency(localCurrencyCode)
                                        )
                                        onResultChange(r)
                                        if (r is ChangeResult.Error) onErrorMessageChange(r.message)

                                        // Show interstitial every 4 successful calculations (4, 8, 12, ...).
                                        calculateCount += 1
                                        if (isOnline && calculateCount % 4 == 0) {
                                            (context as? Activity)?.let { interstitial.showIfReady(it) }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        SarfLineAlt
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) { Text(stringResource(R.string.calculate), fontWeight = FontWeight.Bold) }

                                OutlinedButton(
                                    onClick = {
                                        onPriceTextChange("")
                                        onPaidTextChange("")
                                        onResultChange(null)
                                        onErrorMessageChange(null)
                                        excludedDenoms.clear()
                                        lastPriceCents = null
                                        lastPaidCents = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) { Text(stringResource(R.string.clear), fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }

            errorMessage?.let { error ->
                item(span = { fullSpan() }) {
                    val errShape = MaterialTheme.shapes.large
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, errShape),
                        shape = errShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            when (val changeResult = result) {
                is ChangeResult.Success -> {
                    if (excludedDenoms.isNotEmpty()) {
                        item(span = { fullSpan() }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        excludedDenoms.clear()
                                        val p = lastPriceCents
                                        val paid = lastPaidCents
                                        if (p != null && paid != null) {
                                            val r = ChangeCalculator.calculateChangeBreakdown(
                                                p,
                                                paid,
                                                denominations = Denominations.allForCurrency(localCurrencyCode)
                                            )
                                            onResultChange(r)
                                            if (r is ChangeResult.Error) onErrorMessageChange(r.message) else onErrorMessageChange(null)
                                        }
                                    }
                                ) {
                                    Text(stringResource(R.string.reset))
                                }
                            }
                        }
                    }

                    item(span = { fullSpan() }) {
                        val bills = changeResult.breakdown.filter { !it.isCoin }
                        val coins = changeResult.breakdown.filter { it.isCoin }
                        val gap = 10.dp
                        fun computeWith(excluded: Set<Int>): ChangeResult? {
                            val p = lastPriceCents
                            val paid = lastPaidCents
                            if (p != null && paid != null) {
                                return ChangeCalculator.calculateChangeBreakdown(
                                    p,
                                    paid,
                                    excludedDenominationsCents = excluded,
                                    denominations = Denominations.allForCurrency(localCurrencyCode)
                                )
                            }
                            return null
                        }

                        fun applyResult(r: ChangeResult) {
                            onResultChange(r)
                            if (r is ChangeResult.Error) onErrorMessageChange(r.message) else onErrorMessageChange(null)
                        }

                        fun tryExcludeDenomination(valueCents: Int, isCoin: Boolean) {
                            if (excludedDenoms.contains(valueCents)) return
                            val candidate = excludedDenoms.toSet() + valueCents
                            val r = computeWith(candidate) ?: return
                            if (r is ChangeResult.Error) {
                                val msgRes = if (isCoin) R.string.toast_last_coin else R.string.toast_cannot_remove_denom
                                Toast.makeText(context, context.getString(msgRes), Toast.LENGTH_SHORT).show()
                                return
                            }
                            excludedDenoms.add(valueCents)
                            applyResult(r)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left 2/3: banknotes (bigger)
                            Column(
                                modifier = Modifier.weight(2f),
                                verticalArrangement = Arrangement.spacedBy(gap)
                            ) {
                                bills.forEach { item ->
                                    DenominationCard(
                                        label = item.displayLabel,
                                        count = item.count,
                                        assetPath = item.assetPath,
                                        isCoin = item.isCoin,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                        // Slightly taller/larger banknotes.
                                        .aspectRatio(2.0f)
                                            .combinedClickable(
                                                onClick = {},
                                                onDoubleClick = {
                                                    tryExcludeDenomination(item.valueCents, isCoin = false)
                                                }
                                            ),
                                        showCountWhenOne = false,
                                        showLabel = false
                                    )
                                }
                            }

                            // Right 1/3: coins (always the right column, stacked from top)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(gap)
                            ) {
                                coins.forEach { item ->
                                    DenominationCard(
                                        label = item.displayLabel,
                                        count = item.count,
                                        assetPath = item.assetPath,
                                        isCoin = item.isCoin,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .combinedClickable(
                                                onClick = {},
                                                onDoubleClick = {
                                                    tryExcludeDenomination(item.valueCents, isCoin = true)
                                                }
                                            ),
                                        showCountWhenOne = false,
                                        showLabel = false
                                    )
                                }
                            }
                        }
                    }
                }

                is ChangeResult.Error -> Unit
                null -> Unit
            }
        }

        // Bottom banner on SARF page.
        if (isOnline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                AdMobBanner()
            }
        }
    }
}



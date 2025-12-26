package com.tajir.sarf.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.tajir.sarf.data.CurrencyGuide
import com.tajir.sarf.data.Denominations
import com.tajir.sarf.data.Countries
import com.tajir.sarf.domain.ChangeCalculator
import com.tajir.sarf.domain.ChangeResult
import com.tajir.sarf.ui.components.CurrencyMoneyGridCard
import com.tajir.sarf.ui.components.DenominationCard
import com.tajir.sarf.ui.components.rememberIsOnline
import com.tajir.sarf.ui.theme.SarfLineAlt
import com.tajir.sarf.utils.MoneyInputParser
import com.tajir.sarf.utils.toLatinDigits
import com.tajir.sarf.R
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeTabContent(
    onOpenCard: (valueCents: Int) -> Unit,
    onCalculateChange: () -> Unit,
    ratesState: RatesUiState,
    onPickHomeCurrency: (String) -> Unit,
    localCurrencyCode: String,
    selectedCountryCode: String,
    onChangeCountry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Design rule: clean white background for the tourist "guide" screen.
    val showHomeCurrencyDialog = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(ratesState.homeCurrency) {
        // First launch (or not set): prompt user to pick a home currency.
        showHomeCurrencyDialog.value = (ratesState.homeCurrency == null)
    }

    if (showHomeCurrencyDialog.value) {
        HomeCurrencyPickerDialog(
            onDismiss = { /* keep until picked */ },
            onPick = {
                onPickHomeCurrency(it)
                showHomeCurrencyDialog.value = false
            }
        )
    }

    val showAmountBreakdown = rememberSaveable { mutableStateOf(false) }

    if (showAmountBreakdown.value) {
        AmountBreakdownScreen(
            localCurrencyCode = localCurrencyCode,
            onBack = { showAmountBreakdown.value = false },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(6.dp))

        // Country context header (flag + name + currency) + small change action.
        val country = remember(selectedCountryCode) { Countries.byCode(selectedCountryCode) }
        val countryName = country?.let { stringResource(it.nameResId) } ?: selectedCountryCode
        val currencyCode = country?.localCurrencyCode ?: localCurrencyCode
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${flagEmoji(selectedCountryCode)}  $countryName  •  $currencyCode",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f)
            )

            val showCountryDialog = rememberSaveable { mutableStateOf(false) }
            OutlinedButton(
                onClick = { showCountryDialog.value = true },
                shape = RoundedCornerShape(999.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(stringResource(R.string.change_country), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }

            if (showCountryDialog.value) {
                CountryPickerDialog(
                    selectedCountryCode = selectedCountryCode,
                    onDismiss = { showCountryDialog.value = false },
                    onPick = {
                        onChangeCountry(it)
                        showCountryDialog.value = false
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_currency_guide_title),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.home_currency_guide_subtitle),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { showAmountBreakdown.value = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.home_breakdown_amount), fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(14.dp))

        // 2-column grid, cards show ONLY the money image.
        val cards = CurrencyGuide.cardsForCurrency(localCurrencyCode)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
        ) {
            items(cards) { card ->
                CurrencyMoneyGridCard(
                    card = card,
                    onClick = { onOpenCard(card.denomination.valueCents) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (card.denomination.isCoin) 150.dp else 120.dp)
                )
            }
        }
    }
}

@Composable
fun CurrencyCardDetailScreen(
    valueCents: Int,
    onBack: () -> Unit,
    ratesState: RatesUiState,
    onEnsureRate: (baseCurrency: String, forceRefresh: Boolean) -> Unit,
    onEnsureUsdRate: (baseCurrency: String) -> Unit,
    localCurrencyCode: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val card = CurrencyGuide.byValueCents(localCurrencyCode, valueCents)
    val homeCurrency = ratesState.homeCurrency
    val isOnline = rememberIsOnline()

    // Auto-load conversion rate when entering the detail screen (show conversion immediately).
    LaunchedEffect(valueCents, homeCurrency, localCurrencyCode, ratesState.localToHomeRate, ratesState.isLoading, ratesState.lastError) {
        if (homeCurrency != null && ratesState.localToHomeRate == null && !ratesState.isLoading && ratesState.lastError == null) {
            onEnsureRate(localCurrencyCode, false)
        }
    }

    // Auto-load USD reference rate (used for the small "≈ ... USD" line under Conversion).
    LaunchedEffect(valueCents, localCurrencyCode, ratesState.localToUsdRate, ratesState.usdIsLoading, ratesState.usdLastError) {
        if (ratesState.localToUsdRate == null && !ratesState.usdIsLoading && ratesState.usdLastError == null) {
            onEnsureUsdRate(localCurrencyCode)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.details_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(44.dp))
            }
        }

        if (card != null) {
            item {
                DenominationCard(
                    label = card.denomination.displayLabel,
                    count = 1,
                    assetPath = card.denomination.assetPath,
                    isCoin = card.denomination.isCoin,
                    showLabel = false,
                    showCountWhenOne = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (card.denomination.isCoin) 220.dp else 160.dp)
                )
            }

            item {
                Text(
                    text = card.denomination.displayLabel,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            item {
                val home = ratesState.homeCurrency
                val rate = ratesState.localToHomeRate
                val localValue = card.denomination.valueCents / 100.0
                val conversionText = when {
                    home == null -> stringResource(R.string.select_home_currency_settings)
                    rate != null -> {
                        val converted = localValue * rate
                        val formatted = String.format(java.util.Locale.US, "%.2f", converted)
                        "≈ $formatted $home"
                    }
                    ratesState.lastError != null -> stringResource(R.string.rate_unavailable)
                    ratesState.isLoading -> stringResource(R.string.loading)
                    else -> stringResource(R.string.loading)
                }.toLatinDigits()

                val boxShape = MaterialTheme.shapes.large
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, boxShape)
                        .background(MaterialTheme.colorScheme.surface, boxShape)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.conversion_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = conversionText,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Small USD reference under the home-currency value (approx).
                            val usdRate = ratesState.localToUsdRate
                            if (usdRate != null) {
                                val usd = localValue * usdRate
                                val formattedUsd = String.format(java.util.Locale.US, "%.2f", usd)
                                Text(
                                    text = "≈ $formattedUsd USD".toLatinDigits(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            tonalElevation = 0.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt)
                        ) {
                            IconButton(
                                onClick = { onEnsureRate(localCurrencyCode, true) },
                                enabled = !ratesState.isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = stringResource(R.string.refresh),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    val updatedAtText = ratesState.rateUpdatedAtMs?.takeIf { it > 0 }?.let {
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(it)).toLatinDigits()
                    }
                    if (updatedAtText != null) {
                        Text(
                            text = stringResource(R.string.last_updated, updatedAtText),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                        )
                    }
                    if (!isOnline && (ratesState.rateFromCache || ratesState.rateUpdatedAtMs != null)) {
                        Text(
                            text = stringResource(R.string.using_last_saved_rate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                        )
                    }
                }
            }

            item {
                val boxShape = MaterialTheme.shapes.large
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, boxShape)
                        .background(MaterialTheme.colorScheme.surface, boxShape)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = CurrencyGuide.localize(context, card.detailDescription),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
                    )
                }
            }

            if (card.examplePrices.isNotEmpty()) {
                item {
                    val boxShape = MaterialTheme.shapes.large
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, boxShape)
                            .background(MaterialTheme.colorScheme.surface, boxShape)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.examples_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        ExampleChips(
                            examples = card.examplePrices.map { CurrencyGuide.localize(context, it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(10.dp)) }
        } else {
            item {
                Text(
                    text = stringResource(R.string.no_details_available),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f)
                )
            }
        }
    }
}

@Composable
private fun ExampleChips(
    examples: List<String>,
    modifier: Modifier = Modifier
) {
    val rows = examples.chunked(2)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { text ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(999.dp),
                        color = Color.Transparent,
                        tonalElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt)
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeCurrencyPickerDialog(
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    val currencies = listOf("EUR", "USD", "GBP", "CAD")

    BasicAlertDialog(onDismissRequest = onDismiss) {
        val shape = MaterialTheme.shapes.large
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_currency_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.home_currency_picker_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    currencies.forEach { code ->
                        OutlinedButton(
                            onClick = { onPick(code) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                SarfLineAlt
                            )
                        ) {
                            Text(text = code, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CountryPickerDialog(
    selectedCountryCode: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        val shape = MaterialTheme.shapes.large
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.change_country),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(Countries.ALL) { c ->
                        val selected = c.code.equals(selectedCountryCode, ignoreCase = true)
                        val rowShape = MaterialTheme.shapes.large
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(rowShape)
                                .clickable { onPick(c.code) },
                            shape = rowShape,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "${flagEmoji(c.code)}  ${stringResource(c.nameResId)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = c.localCurrencyCode,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }
                                if (selected) {
                                    Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun flagEmoji(countryCode: String): String {
    val code = countryCode.trim().uppercase()
    if (code.length != 2) return ""
    val first = code[0]
    val second = code[1]
    if (first !in 'A'..'Z' || second !in 'A'..'Z') return ""
    val base = 0x1F1E6
    val a = 'A'.code
    return String(intArrayOf(base + (first.code - a), base + (second.code - a)), 0, 2)
}

@Composable
private fun AmountBreakdownScreen(
    localCurrencyCode: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val input = rememberSaveable { mutableStateOf("") }
    val error = rememberSaveable { mutableStateOf<String?>(null) }
    val result = rememberSaveable { mutableStateOf<ChangeResult.Success?>(null) }
    val excludedDenoms = rememberSaveable { mutableStateListOf<Int>() }
    val lastAmountCents = rememberSaveable { mutableStateOf<Int?>(null) }

    fun computeWith(excluded: Set<Int>, cents: Int): ChangeResult {
        val denoms = Denominations.allForCurrency(localCurrencyCode)
        return ChangeCalculator.calculateChangeBreakdown(
            priceCents = 0,
            paidCents = cents,
            excludedDenominationsCents = excluded,
            denominations = denoms
        )
    }

    fun recompute() {
        val cents = lastAmountCents.value ?: return
        val r = computeWith(excludedDenoms.toSet(), cents)
        result.value = r as? ChangeResult.Success
        error.value = if (r is ChangeResult.Error) r.message else null
    }

    fun tryExcludeDenomination(valueCents: Int, isCoin: Boolean) {
        if (excludedDenoms.contains(valueCents)) return

        if (isCoin) {
            val coinDenoms = Denominations.allForCurrency(localCurrencyCode).filter { it.isCoin }.map { it.valueCents }
            val remainingCoinDenoms = coinDenoms.filter { !excludedDenoms.contains(it) }
            val isLastRemainingCoin = remainingCoinDenoms.size <= 1 && remainingCoinDenoms.contains(valueCents)
            if (isLastRemainingCoin) {
                Toast.makeText(context, context.getString(R.string.toast_last_coin), Toast.LENGTH_SHORT).show()
                return
            }
        }

        val cents = lastAmountCents.value ?: return
        val candidate = excludedDenoms.toSet() + valueCents
        val r = computeWith(candidate, cents)
        if (r is ChangeResult.Error) {
            Toast.makeText(context, context.getString(R.string.toast_cannot_remove_denom), Toast.LENGTH_SHORT).show()
            return
        }
        excludedDenoms.add(valueCents)
        result.value = r as? ChangeResult.Success
        error.value = null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.breakdown_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(44.dp))
            }
        }

        item {
            val boxShape = MaterialTheme.shapes.large
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, boxShape)
                    .background(MaterialTheme.colorScheme.surface, boxShape)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.breakdown_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )

                OutlinedTextField(
                    value = input.value,
                    onValueChange = {
                        input.value = MoneyInputParser.sanitizeForTyping(it)
                        error.value = null
                        result.value = null
                        excludedDenoms.clear()
                        lastAmountCents.value = null
                    },
                    label = { Text(stringResource(R.string.amount_label)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        val cents = MoneyInputParser.parseToCents(input.value).getOrElse {
                            error.value = it.message ?: context.getString(R.string.invalid_amount)
                            return@OutlinedButton
                        }
                        if (cents <= 0) {
                            error.value = context.getString(R.string.enter_positive_amount)
                            return@OutlinedButton
                        }
                        lastAmountCents.value = cents
                        excludedDenoms.clear()

                        val r = computeWith(emptySet(), cents)
                        result.value = r as? ChangeResult.Success
                        error.value = if (r is ChangeResult.Error) r.message else null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.show), fontWeight = FontWeight.Bold)
                }

                error.value?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        result.value?.let { success ->
            item {
                // Match "Monnaie" page: show the money breakdown directly (no "Result" title, no container box).
                val bills = success.breakdown.filter { !it.isCoin }
                val coins = success.breakdown.filter { it.isCoin }
                val gap = 10.dp

                // Optional reset when exclusions are active.
                if (excludedDenoms.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                excludedDenoms.clear()
                                recompute()
                            },
                            shape = RoundedCornerShape(999.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(stringResource(R.string.reset), fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.Top
                ) {
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
                                showLabel = false,
                                showCountWhenOne = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2.0f)
                                    .combinedClickable(
                                        onClick = {},
                                        onDoubleClick = { tryExcludeDenomination(item.valueCents, isCoin = false) }
                                    )
                            )
                        }
                    }

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
                                showLabel = false,
                                showCountWhenOne = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .combinedClickable(
                                        onClick = {},
                                        onDoubleClick = { tryExcludeDenomination(item.valueCents, isCoin = true) }
                                    )
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(10.dp)) }
    }
}



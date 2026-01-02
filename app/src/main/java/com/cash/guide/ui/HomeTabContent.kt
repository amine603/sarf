package com.cash.guide.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.material.icons.outlined.Attractions
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.CarRental
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsSubway
import androidx.compose.material.icons.outlined.DinnerDining
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material.icons.outlined.TaxiAlert
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.AirplaneTicket
import androidx.compose.material.icons.outlined.Tour
import com.cash.guide.data.ValueExamples
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.cash.guide.data.CurrencyGuide
import com.cash.guide.data.Denominations
import com.cash.guide.data.Countries
import com.cash.guide.domain.ChangeCalculator
import com.cash.guide.domain.ChangeResult
import com.cash.guide.ui.components.CurrencyMoneyGridCard
import com.cash.guide.ui.components.DenominationCard
import com.cash.guide.ui.components.rememberAssetImage
import com.cash.guide.ui.components.rememberIsOnline
import com.cash.guide.ui.components.rememberInterstitialAdController
import com.cash.guide.ui.theme.SarfLineAlt
import android.app.Activity
import com.cash.guide.utils.MoneyInputParser
import com.cash.guide.utils.toLatinDigits
import com.cash.guide.R
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
    onViewValueExamples: () -> Unit = {},
    showAmountBreakdown: Boolean,
    onShowAmountBreakdown: (Boolean) -> Unit,
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

    if (showAmountBreakdown) {
        AmountBreakdownScreen(
            localCurrencyCode = localCurrencyCode,
            onBack = { onShowAmountBreakdown(false) },
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onShowAmountBreakdown(true) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.home_breakdown_amount),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            OutlinedButton(
                onClick = { onViewValueExamples() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.view_value_examples),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 2-column grid, cards show ONLY the money image.
        val cards = CurrencyGuide.cardsForCurrency(localCurrencyCode)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
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
                // Fixed size container for coins to ensure consistent sizing across all currencies
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (card.denomination.isCoin) 180.dp else 160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (card.denomination.isCoin) {
                        // For coins: use fixed size so all coins appear the same size
                        val image = rememberAssetImage(card.denomination.assetPath, trimTransparentPadding = false)
                        image?.let {
                            Image(
                                bitmap = it,
                                contentDescription = card.denomination.displayLabel,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(140.dp)
                            )
                        }
                    } else {
                        // For banknotes: use DenominationCard as before
                        DenominationCard(
                            label = card.denomination.displayLabel,
                            count = 1,
                            assetPath = card.denomination.assetPath,
                            isCoin = false,
                            showLabel = false,
                            showCountWhenOne = false,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
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

            // Value Examples from ValueExamples data - filtered by denomination value
            item {
                val boxShape = MaterialTheme.shapes.large
                // Get all value examples for this currency
                val allExamples = ValueExamples.examplesForCurrency(localCurrencyCode).values.flatten()
                // Filter examples that match the denomination value (within the price range)
                val denominationValueCents = card.denomination.valueCents
                val relevantExamples = allExamples.filter { example ->
                    // Show examples where the denomination value falls within the example's price range
                    denominationValueCents >= example.minValueCents && denominationValueCents <= example.maxValueCents
                }
                
                if (relevantExamples.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, boxShape)
                            .background(MaterialTheme.colorScheme.surface, boxShape)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.examples_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Use a regular Column with Rows to create a 2-column grid
                        // This avoids the LazyVerticalGrid constraint issue inside LazyColumn
                        val rows = relevantExamples.chunked(2)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    row.forEach { example ->
                                        ValueExampleGridItem(
                                            example = example,
                                            currencyCode = localCurrencyCode,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Add spacer if odd number of items
                                    if (row.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
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
private fun ValueExampleGridItem(
    example: ValueExamples.ValueExample,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val icon = getIconForValueExample(example.iconName)
    val iconColor = example.category.iconColor
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icon with colored background - larger size to save space
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = iconColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = ValueExamples.localizeTitle(LocalContext.current, example.title),
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )
        }
        
        // Text below icon
        Text(
            text = ValueExamples.localizeTitle(LocalContext.current, example.title),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun getIconForValueExample(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "water" -> Icons.Outlined.WaterDrop
        "coffee" -> Icons.Outlined.Coffee
        "bakery" -> Icons.Outlined.BakeryDining
        "fastfood" -> Icons.Outlined.Fastfood
        "restaurant" -> Icons.Outlined.Restaurant
        "dinner" -> Icons.Outlined.DinnerDining
        "bus" -> Icons.Outlined.DirectionsBus
        "metro" -> Icons.Outlined.DirectionsSubway
        "taxi" -> Icons.Outlined.TaxiAlert
        "airport" -> Icons.Outlined.AirplaneTicket
        "train" -> Icons.Outlined.Train
        "cinema" -> Icons.Outlined.Event
        "museum" -> Icons.Outlined.Museum
        "attraction" -> Icons.Outlined.Attractions
        "tour" -> Icons.Outlined.Tour
        "event" -> Icons.Outlined.Event
        "hostel", "hotel" -> Icons.Outlined.Hotel
        "airbnb" -> Icons.Outlined.Hotel
        "car" -> Icons.Outlined.CarRental
        "sim" -> Icons.Outlined.SimCard
        "data", "topup" -> Icons.Outlined.PhoneAndroid
        "toiletries" -> Icons.Outlined.LocalPharmacy
        "laundry" -> Icons.Outlined.LocalLaundryService
        else -> Icons.Outlined.Attractions
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeCurrencyPickerDialog(
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    // Only currencies that are actually supported in the app (have denominations)
    val currencies = listOf("MAD", "EUR", "SAR", "USD", "GBP")

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
    val breakdownShowCount = rememberSaveable { mutableStateOf(0) }
    val isOnline = rememberIsOnline()
    val interstitialAd = rememberInterstitialAdController(isOnline = isOnline)

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

                        // Show interstitial every 3 successful breakdowns (3, 6, 9, ...).
                        if (r is ChangeResult.Success) {
                            breakdownShowCount.value += 1
                            if (isOnline && breakdownShowCount.value % 3 == 0) {
                                (context as? Activity)?.let { interstitialAd.showIfReady(it) }
                            }
                        }
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



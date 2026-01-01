package com.tajir.sarf.ui

import com.tajir.sarf.domain.ChangeResult
import com.tajir.sarf.utils.MoneyInputParser
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarRate
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.tajir.sarf.R
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.tajir.sarf.data.Countries
import com.tajir.sarf.settings.AppSettings
import com.tajir.sarf.ui.theme.SarfLineAlt
import com.tajir.sarf.ui.ValueExamplesScreen
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Main screen of the Sarf app
 * Allows user to enter price and paid amount, then calculates change breakdown
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen() {
    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0 = Home, 1 = Calculator, 2 = SARF
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var homeDetailValueCents by rememberSaveable { mutableStateOf<Int?>(null) }
    var showValueExamples by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val ratesVm: RatesViewModel = viewModel()
    val ratesState by ratesVm.state.collectAsState()

    val settings = remember(context) { AppSettings(context) }
    val scope = rememberCoroutineScope()
    val onboardingDone by settings.onboardingDone.collectAsState(initial = false)
    val selectedCountryCode by settings.selectedCountryCode.collectAsState(initial = "MA")
    val selectedCountryCodeNonNull = selectedCountryCode ?: "MA"
    val localCurrencyCode = remember(selectedCountryCodeNonNull) {
        Countries.byCode(selectedCountryCodeNonNull)?.localCurrencyCode ?: "MAD"
    }

    if (!onboardingDone) {
        OnboardingScreen(
            settings = settings,
            onDone = {
                // Recompose will show main UI.
            },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    // Calculator tab state
    var calcExpression by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var lastCalculatorResultCents by rememberSaveable { mutableStateOf<Int?>(null) }

    // SARF tab state
    var priceText by rememberSaveable { mutableStateOf("") }
    var paidText by rememberSaveable { mutableStateOf("") }
    var sarfResult by remember { mutableStateOf<ChangeResult?>(null) }
    var sarfError by remember { mutableStateOf<String?>(null) }

    val isHome = !showSettings && selectedTab == 0 && homeDetailValueCents == null && !showValueExamples

    BackHandler(enabled = true) {
        when {
            showExitDialog -> {
                // If dialog is showing, back just dismisses it.
                showExitDialog = false
            }
            showSettings -> {
                showSettings = false
            }
            showValueExamples -> {
                showValueExamples = false
            }
            homeDetailValueCents != null -> {
                homeDetailValueCents = null
            }
            selectedTab != 0 -> {
                selectedTab = 0
            }
            else -> {
                // Home page: ask for exit confirmation.
                showExitDialog = true
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (showSettings) {
            SettingsScreen(
                onBack = { showSettings = false },
                onRefreshRates = { ratesVm.refreshRates() }
            )
            return@Surface
        }

        if (showExitDialog && isHome) {
            BasicAlertDialog(
                onDismissRequest = { showExitDialog = false }
            ) {
                val dialogShape = MaterialTheme.shapes.large
                Surface(
                    shape = dialogShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.exit_title),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = stringResource(R.string.exit_message),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Rate button
                            OutlinedButton(
                                onClick = {
                                    showExitDialog = false
                                    val market = android.net.Uri.parse("market://details?id=${context.packageName}")
                                    val web = android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, market).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    runCatching { context.startActivity(intent) }.getOrElse {
                                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, web).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = SarfLineAlt
                                ),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Outlined.StarRate,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = stringResource(R.string.settings_rate),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Cancel and Exit buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showExitDialog = false },
                                    modifier = Modifier.weight(1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = SarfLineAlt
                                    ),
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.exit_cancel),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        showExitDialog = false
                                        (context as? Activity)?.finish()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = SarfLineAlt
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.exit_confirm),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedTab == 0 && homeDetailValueCents != null) {
                // Detail screen uses its own clean top row inside the screen.
                Spacer(Modifier.height(4.dp))
            } else {
                // Top bar: 4 equal cells (Home / Calculator / SARF / Settings), full width.
                FourCellsTopBar(
                    homeLabel = stringResource(R.string.tab_home),
                    calculatorLabel = stringResource(R.string.tab_calculator),
                    sarfLabel = stringResource(R.string.tab_sarf),
                    selectedTab = selectedTab,
                    settingsSelected = showSettings,
                    onSelectTab = { selectedTab = it },
                    onOpenSettings = { showSettings = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        // Pro polish: add horizontal padding and rounded corners.
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp)
                )
            }

            when (selectedTab) {
                0 -> {
                    when {
                        showValueExamples -> {
                            ValueExamplesScreen(
                                currencyCode = localCurrencyCode,
                                onBack = { showValueExamples = false },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        homeDetailValueCents != null -> {
                            CurrencyCardDetailScreen(
                                valueCents = homeDetailValueCents!!,
                                onBack = { homeDetailValueCents = null },
                                ratesState = ratesState,
                                onEnsureRate = { base, force -> ratesVm.ensureRateLoaded(baseCurrency = base, forceRefresh = force) },
                                onEnsureUsdRate = { base -> ratesVm.ensureUsdRateLoaded(baseCurrency = base, forceRefresh = false) },
                                localCurrencyCode = localCurrencyCode,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            HomeTabContent(
                                onOpenCard = { homeDetailValueCents = it },
                                onCalculateChange = { selectedTab = 2 },
                                ratesState = ratesState,
                                onPickHomeCurrency = { code -> ratesVm.setHomeCurrency(code) },
                                localCurrencyCode = localCurrencyCode,
                                selectedCountryCode = selectedCountryCodeNonNull,
                                onChangeCountry = { code ->
                                    scope.launch { settings.setSelectedCountryCode(code) }
                                },
                                onViewValueExamples = { showValueExamples = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                1 -> CalculatorTabContent(
                    expression = calcExpression,
                    onExpressionChange = { calcExpression = it },
                    lastCalculatorResultCents = lastCalculatorResultCents,
                    onLastCalculatorResultCentsChange = { lastCalculatorResultCents = it },
                    onSendToSarf = {
                        val cents = lastCalculatorResultCents ?: return@CalculatorTabContent
                        priceText = MoneyInputParser.formatCentsForInputCompact(cents)
                        paidText = ""
                        sarfResult = null
                        sarfError = null
                        selectedTab = 2
                    },
                    modifier = Modifier.fillMaxSize()
                )

                else -> SarfTabContent(
                    priceText = priceText,
                    onPriceTextChange = { priceText = it },
                    paidText = paidText,
                    onPaidTextChange = { paidText = it },
                    result = sarfResult,
                    onResultChange = { sarfResult = it },
                    errorMessage = sarfError,
                    onErrorMessageChange = { sarfError = it },
                    localCurrencyCode = localCurrencyCode,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun FourCellsTopBar(
    homeLabel: String,
    calculatorLabel: String,
    sarfLabel: String,
    selectedTab: Int,
    settingsSelected: Boolean,
    onSelectTab: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val shape = RoundedCornerShape(18.dp)
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.16f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.70f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.outline

    Card(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Tabs area takes the remaining width (full width bar overall).
            Row(modifier = Modifier.weight(1f).fillMaxSize()) {
                TabTextCell(
                    label = homeLabel,
                    selected = selectedTab == 0,
                    onClick = { onSelectTab(0) },
                    modifier = Modifier.weight(1f).fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxSize()
                        .background(dividerColor)
                )

                TabTextCell(
                    label = calculatorLabel,
                    selected = selectedTab == 1,
                    onClick = { onSelectTab(1) },
                    modifier = Modifier.weight(1f).fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxSize()
                        .background(dividerColor)
                )

                TabTextCell(
                    label = sarfLabel,
                    selected = selectedTab == 2,
                    onClick = { onSelectTab(2) },
                    modifier = Modifier.weight(1f).fillMaxSize()
                )
            }

            // Divider between tabs and gear.
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxSize()
                    .background(dividerColor)
            )

            // Settings is a smaller fixed cell (content-sized feel).
            TabIconCell(
                icon = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.settings),
                selected = settingsSelected,
                onClick = onOpenSettings,
                modifier = Modifier
                    .width(56.dp)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun TabTextCell(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.20f else 0.10f) else MaterialTheme.colorScheme.surface
    val fg = when {
        isDark -> Color.White
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val weight = FontWeight.Bold

    Box(
        modifier = modifier
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = weight,
            color = fg
        )
    }
}

@Composable
private fun TabIconCell(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.20f else 0.10f) else MaterialTheme.colorScheme.surface
    val fg = when {
        isDark -> Color.White
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = fg
        )
    }
}


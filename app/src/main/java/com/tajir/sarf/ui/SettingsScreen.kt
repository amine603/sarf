package com.tajir.sarf.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.PrivacyTip
import com.tajir.sarf.R
import com.tajir.sarf.settings.AppLanguage
import com.tajir.sarf.settings.AppSettings
import com.tajir.sarf.settings.ThemeMode
import kotlinx.coroutines.launch

private const val PRIVACY_POLICY_URL = "https://example.com/privacy"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onRefreshRates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val scope = rememberCoroutineScope()

    val themeMode by settings.themeMode.collectAsState(initial = ThemeMode.LIGHT)
    val language by settings.language.collectAsState(initial = AppLanguage.FR)
    val homeCurrency by settings.homeCurrency.collectAsState(initial = null)

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
        ) {
            item {
                val shape = MaterialTheme.shapes.large
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            stringResource(R.string.settings_home_currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )

                        val options = listOf("EUR", "USD", "GBP", "CAD")
                        options.forEach { code ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch { settings.setHomeCurrency(code) }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = homeCurrency == code,
                                    onClick = { scope.launch { settings.setHomeCurrency(code) } }
                                )
                                Text(text = code, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            item {
                val shape = MaterialTheme.shapes.large
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

                        LanguageRow(
                            label = stringResource(R.string.settings_language_fr),
                            selected = language == AppLanguage.FR,
                            onSelect = {
                                scope.launch {
                                    settings.setLanguage(AppLanguage.FR)
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("fr"))
                                }
                            }
                        )
                        LanguageRow(
                            label = stringResource(R.string.settings_language_ar),
                            selected = language == AppLanguage.AR,
                            onSelect = {
                                scope.launch {
                                    settings.setLanguage(AppLanguage.AR)
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
                                }
                            }
                        )
                    LanguageRow(
                        label = stringResource(R.string.settings_language_en),
                        selected = language == AppLanguage.EN,
                        onSelect = {
                            scope.launch {
                                settings.setLanguage(AppLanguage.EN)
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                            }
                        }
                    )
                    LanguageRow(
                        label = stringResource(R.string.settings_language_es),
                        selected = language == AppLanguage.ES,
                        onSelect = {
                            scope.launch {
                                settings.setLanguage(AppLanguage.ES)
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                            }
                        }
                    )
                    LanguageRow(
                        label = stringResource(R.string.settings_language_pt),
                        selected = language == AppLanguage.PT,
                        onSelect = {
                            scope.launch {
                                settings.setLanguage(AppLanguage.PT)
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("pt"))
                            }
                        }
                    )
                    }
                }
            }

            item {
                val shape = MaterialTheme.shapes.large
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

                        ThemeRow(stringResource(R.string.settings_theme_light), themeMode == ThemeMode.LIGHT) {
                            scope.launch { settings.setThemeMode(ThemeMode.LIGHT) }
                        }
                        ThemeRow(stringResource(R.string.settings_theme_dark), themeMode == ThemeMode.DARK) {
                            scope.launch { settings.setThemeMode(ThemeMode.DARK) }
                        }
                    }
                }
            }

            item {
                // Bottom actions: each item is its own rounded outlined row (no big shared container).
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsActionRow(
                        title = stringResource(R.string.settings_refresh_rates),
                        onClick = onRefreshRates,
                        leadingIcon = Icons.Outlined.Refresh
                    )
                    SettingsActionRow(
                        title = stringResource(R.string.settings_share),
                        onClick = {
                            val url = "https://play.google.com/store/apps/details?id=${context.packageName}"
                            val text = context.getString(R.string.share_text, url)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, null))
                        },
                        leadingIcon = Icons.Outlined.Share
                    )
                    SettingsActionRow(
                        title = stringResource(R.string.settings_rate),
                        onClick = {
                            val market = Uri.parse("market://details?id=${context.packageName}")
                            val web = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                            val intent = Intent(Intent.ACTION_VIEW, market).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(intent) }.getOrElse {
                                context.startActivity(Intent(Intent.ACTION_VIEW, web).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        },
                        leadingIcon = Icons.Outlined.StarRate
                    )
                    SettingsActionRow(
                        title = stringResource(R.string.settings_privacy),
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        leadingIcon = Icons.Outlined.PrivacyTip,
                        trailing = {
                            androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ThemeRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    onClick: () -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val shape = MaterialTheme.shapes.large
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}



package com.tajir.sarf.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.tajir.sarf.data.Countries
import com.tajir.sarf.settings.AppLanguage
import com.tajir.sarf.settings.AppSettings
import com.tajir.sarf.ui.theme.SarfLineAlt
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.ui.res.stringResource
import com.tajir.sarf.R

@Composable
fun OnboardingScreen(
    settings: AppSettings,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Check if language is already set, if so start at country selection step
    val currentLanguage by settings.language.collectAsState(initial = AppLanguage.SYSTEM)
    
    var step by rememberSaveable { mutableStateOf(0) } // 0 = language, 1 = country
    var selectedLang by remember { mutableStateOf<AppLanguage?>(null) }
    var selectedCountry by remember { mutableStateOf<Countries.CountrySpec?>(null) }
    val scope = rememberCoroutineScope()
    
    // Automatically move to country selection step when language is set
    LaunchedEffect(currentLanguage) {
        if (currentLanguage != AppLanguage.SYSTEM && step == 0) {
            step = 1
        }
    }

    data class LanguageOption(val label: String, val value: AppLanguage)
    val languages = listOf(
        LanguageOption(label = "العربية", value = AppLanguage.AR),
        LanguageOption(label = "Français", value = AppLanguage.FR),
        LanguageOption(label = "English", value = AppLanguage.EN),
        LanguageOption(label = "Español", value = AppLanguage.ES),
        LanguageOption(label = "Português", value = AppLanguage.PT),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (step == 0) stringResource(R.string.onboarding_choose_language_title) else stringResource(R.string.onboarding_choose_country_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (step == 0) stringResource(R.string.onboarding_choose_language_subtitle) else stringResource(R.string.onboarding_choose_country_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f)
        )

        if (step == 0) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(languages) { opt ->
                    LanguageChoiceRow(
                        label = opt.label,
                        selected = selectedLang == opt.value,
                        onClick = { selectedLang = opt.value }
                    )
                }
            }
            OutlinedButton(
                onClick = {
                    val lang = selectedLang ?: return@OutlinedButton
                    scope.launch {
                        settings.setLanguage(lang)
                        val locales = when (lang) {
                            AppLanguage.AR -> LocaleListCompat.forLanguageTags("ar")
                            AppLanguage.FR -> LocaleListCompat.forLanguageTags("fr")
                            AppLanguage.EN -> LocaleListCompat.forLanguageTags("en")
                            AppLanguage.ES -> LocaleListCompat.forLanguageTags("es")
                            AppLanguage.PT -> LocaleListCompat.forLanguageTags("pt")
                            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                        }
                        AppCompatDelegate.setApplicationLocales(locales)
                        step = 1
                    }
                },
                enabled = selectedLang != null,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(stringResource(R.string.continue_button), fontWeight = FontWeight.ExtraBold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(Countries.ALL) { c ->
                    CountryChoiceRow(
                        label = stringResource(c.nameResId),
                        subtitle = if (c.assetsAvailable) stringResource(R.string.available) else stringResource(R.string.coming_soon),
                        enabled = c.assetsAvailable,
                        selected = selectedCountry?.code == c.code,
                        onClick = { selectedCountry = c }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        step = 0
                        selectedCountry = null
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.back), fontWeight = FontWeight.ExtraBold)
                }
                OutlinedButton(
                    onClick = {
                        val country = selectedCountry ?: return@OutlinedButton
                        scope.launch {
                            settings.setSelectedCountryCode(country.code)
                            settings.setOnboardingDone(true)
                            onDone()
                        }
                    },
                    enabled = selectedCountry != null,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SarfLineAlt),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Text(stringResource(R.string.start), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun LanguageChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val borderWidth = if (selected) 2.dp else 1.dp
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = borderWidth,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CountryChoiceRow(
    label: String,
    subtitle: String,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val bg = MaterialTheme.colorScheme.surface
    val fg = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val borderWidth = if (selected) 2.dp else 1.dp
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = fg
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = fg.copy(alpha = 0.75f)
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}





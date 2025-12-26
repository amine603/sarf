package com.tajir.sarf.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AppLanguage { SYSTEM, FR, AR, EN, ES, PT }

private val Context.dataStore by preferencesDataStore(name = "sarf_settings")

object SettingsKeys {
    val THEME_MODE: Preferences.Key<String> = stringPreferencesKey("theme_mode")
    val LANGUAGE: Preferences.Key<String> = stringPreferencesKey("language")
    val HOME_CURRENCY: Preferences.Key<String> = stringPreferencesKey("home_currency") // e.g. "EUR"
    val COUNTRY_CODE: Preferences.Key<String> = stringPreferencesKey("country_code") // e.g. "MA"
    val ONBOARDING_DONE: Preferences.Key<Boolean> = booleanPreferencesKey("onboarding_done")
}

class AppSettings(private val context: Context) {
    val themeMode: Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[SettingsKeys.THEME_MODE]
            val parsed = runCatching { ThemeMode.valueOf(raw ?: "") }.getOrNull()
            // We no longer expose SYSTEM in Settings. Treat missing/invalid/SYSTEM as LIGHT for a predictable UX.
            when (parsed ?: ThemeMode.SYSTEM) {
                ThemeMode.DARK -> ThemeMode.DARK
                ThemeMode.LIGHT -> ThemeMode.LIGHT
                ThemeMode.SYSTEM -> ThemeMode.LIGHT
            }
        }

    val language: Flow<AppLanguage> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[SettingsKeys.LANGUAGE]
            val parsed = runCatching { AppLanguage.valueOf(raw ?: "") }.getOrNull()
            // Treat missing/invalid/SYSTEM as FR (app default).
            when (parsed ?: AppLanguage.SYSTEM) {
                AppLanguage.AR -> AppLanguage.AR
                AppLanguage.FR -> AppLanguage.FR
                AppLanguage.EN -> AppLanguage.EN
                AppLanguage.ES -> AppLanguage.ES
                AppLanguage.PT -> AppLanguage.PT
                AppLanguage.SYSTEM -> AppLanguage.FR
            }
        }

    /**
     * User's "home" currency for tourist conversions (e.g. EUR, USD, GBP).
     * Null means not chosen yet (first launch prompt should be shown).
     */
    val homeCurrency: Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[SettingsKeys.HOME_CURRENCY]?.trim()?.uppercase()?.takeIf { it.length == 3 }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[SettingsKeys.THEME_MODE] = mode.name }
    }

    suspend fun setLanguage(lang: AppLanguage) {
        context.dataStore.edit { it[SettingsKeys.LANGUAGE] = lang.name }
    }

    suspend fun setHomeCurrency(code: String?) {
        context.dataStore.edit { prefs ->
            if (code.isNullOrBlank()) {
                prefs.remove(SettingsKeys.HOME_CURRENCY)
            } else {
                prefs[SettingsKeys.HOME_CURRENCY] = code.trim().uppercase()
            }
        }
    }

    val selectedCountryCode: Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[SettingsKeys.COUNTRY_CODE]?.trim()?.uppercase()?.takeIf { it.isNotBlank() }
        }

    suspend fun setSelectedCountryCode(code: String) {
        context.dataStore.edit { it[SettingsKeys.COUNTRY_CODE] = code.trim().uppercase() }
    }

    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[SettingsKeys.ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[SettingsKeys.ONBOARDING_DONE] = done }
    }
}



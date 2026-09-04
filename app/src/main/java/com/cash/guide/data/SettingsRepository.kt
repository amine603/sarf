package com.cash.guide.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cash.guide.domain.MoneyUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hssabi_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_LANGUAGE] ?: "fr"
    }

    val defaultCurrency: Flow<MoneyUnit> = context.dataStore.data.map { preferences ->
        when (preferences[PreferencesKeys.DEFAULT_CURRENCY]) {
            "RIAL" -> MoneyUnit.RIAL
            else -> MoneyUnit.DIRHAM
        }
    }

    suspend fun setAppLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE] = languageCode
        }
    }

    suspend fun setDefaultCurrency(unit: MoneyUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_CURRENCY] = unit.name
        }
    }
}

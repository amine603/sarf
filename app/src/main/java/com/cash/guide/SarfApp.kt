package com.cash.guide

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.cash.guide.settings.AppLanguage
import com.cash.guide.settings.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SarfApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Apply persisted language as early as possible so Arabic (and RTL) is used immediately at app start.
        runBlocking {
            val lang = AppSettings(this@SarfApp).language.first()
            val locales = when (lang) {
                AppLanguage.AR -> LocaleListCompat.forLanguageTags("ar")
                AppLanguage.FR -> LocaleListCompat.forLanguageTags("fr")
                AppLanguage.EN -> LocaleListCompat.forLanguageTags("en")
                AppLanguage.ES -> LocaleListCompat.forLanguageTags("es")
                AppLanguage.PT -> LocaleListCompat.forLanguageTags("pt")
                AppLanguage.SYSTEM -> LocaleListCompat.forLanguageTags("en") // Default to English
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}






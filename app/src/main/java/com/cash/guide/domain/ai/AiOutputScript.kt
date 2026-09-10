package com.cash.guide.domain.ai

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AiOutputScript(val key: String, val label: String) {
    ARABIC("ar", "العربية"),
    FRANCO("franco", "العرنسية"),
    FRENCH("fr", "Français")
}

class AiOutputScriptManager private constructor(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sarf_ai_script_prefs", Context.MODE_PRIVATE)

    private val _selectedScript = MutableStateFlow(loadScript())
    val selectedScript: StateFlow<AiOutputScript> = _selectedScript.asStateFlow()

    private fun defaultForLocale(): AiOutputScript {
        val appLang = context.resources.configuration.locales.get(0)?.language ?: "ar"
        return if (appLang == "fr") AiOutputScript.FRENCH else AiOutputScript.ARABIC
    }

    private fun loadScript(): AiOutputScript {
        val saved = prefs.getString(KEY_SCRIPT, null)
        if (saved != null) {
            return AiOutputScript.entries.firstOrNull { it.key == saved } ?: defaultForLocale()
        }
        return defaultForLocale()
    }

    fun hasExplicitUserPreference(): Boolean {
        return prefs.contains(KEY_EXPLICIT_PREFERENCE)
    }

    fun syncWithAppLanguage(languageCode: String) {
        val target = if (languageCode == "fr") AiOutputScript.FRENCH else AiOutputScript.ARABIC
        prefs.edit()
            .putString(KEY_SCRIPT, target.key)
            .remove(KEY_EXPLICIT_PREFERENCE)
            .apply()
        _selectedScript.value = target
    }

    fun setScript(script: AiOutputScript, fromUserExplicit: Boolean = true) {
        prefs.edit()
            .putString(KEY_SCRIPT, script.key)
            .apply {
                if (fromUserExplicit) {
                    putBoolean(KEY_EXPLICIT_PREFERENCE, true)
                }
            }
            .apply()
        _selectedScript.value = script
    }

    companion object {
        private const val KEY_SCRIPT = "key_ai_output_script"
        private const val KEY_EXPLICIT_PREFERENCE = "key_explicit_user_preference"

        @Volatile
        private var instance: AiOutputScriptManager? = null

        fun getInstance(context: Context): AiOutputScriptManager {
            return instance ?: synchronized(this) {
                instance ?: AiOutputScriptManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

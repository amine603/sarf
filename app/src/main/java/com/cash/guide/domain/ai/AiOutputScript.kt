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

class AiOutputScriptManager private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sarf_ai_script_prefs", Context.MODE_PRIVATE)

    private val _selectedScript = MutableStateFlow(loadScript())
    val selectedScript: StateFlow<AiOutputScript> = _selectedScript.asStateFlow()

    private fun loadScript(): AiOutputScript {
        val saved = prefs.getString(KEY_SCRIPT, AiOutputScript.ARABIC.key)
        return AiOutputScript.entries.firstOrNull { it.key == saved } ?: AiOutputScript.ARABIC
    }

    fun setScript(script: AiOutputScript) {
        prefs.edit().putString(KEY_SCRIPT, script.key).apply()
        _selectedScript.value = script
    }

    companion object {
        private const val KEY_SCRIPT = "key_ai_output_script"

        @Volatile
        private var instance: AiOutputScriptManager? = null

        fun getInstance(context: Context): AiOutputScriptManager {
            return instance ?: synchronized(this) {
                instance ?: AiOutputScriptManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

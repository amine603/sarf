package com.cash.guide.domain.ai

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiVoiceOnboardingManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _hasSeenOnboarding = MutableStateFlow(prefs.getBoolean(KEY_HAS_SEEN, false))
    val hasSeenOnboarding: StateFlow<Boolean> = _hasSeenOnboarding.asStateFlow()

    fun markSeen() {
        prefs.edit().putBoolean(KEY_HAS_SEEN, true).apply()
        _hasSeenOnboarding.value = true
    }

    companion object {
        private const val PREFS_NAME = "sarf_ai_voice_onboarding"
        private const val KEY_HAS_SEEN = "has_seen_ai_voice_onboarding_v1"

        @Volatile
        private var INSTANCE: AiVoiceOnboardingManager? = null

        fun getInstance(context: Context): AiVoiceOnboardingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AiVoiceOnboardingManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

package com.cash.guide.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom

class SecurityRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_LOCK_ENABLED = booleanPreferencesKey("is_lock_enabled")
        val USE_BIOMETRICS = booleanPreferencesKey("use_biometrics")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val LOCK_TIMEOUT_SECONDS = intPreferencesKey("lock_timeout_seconds")
    }

    // Dynamic session lock state (in-memory)
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private var lastBackgroundTimestampMs: Long = 0L

    val isLockEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.IS_LOCK_ENABLED] ?: false
    }

    val useBiometrics: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.USE_BIOMETRICS] ?: true
    }

    val hasPinSet: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val hash = prefs[PreferencesKeys.PIN_HASH]
        !hash.isNullOrBlank()
    }

    val lockTimeoutSeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LOCK_TIMEOUT_SECONDS] ?: 0 // Default: Immediately
    }

    suspend fun setLockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_LOCK_ENABLED] = enabled
        }
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    suspend fun setUseBiometrics(use: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USE_BIOMETRICS] = use
        }
    }

    suspend fun setLockTimeoutSeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LOCK_TIMEOUT_SECONDS] = seconds
        }
    }

    suspend fun setPin(pin: String) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PIN_SALT] = salt
            prefs[PreferencesKeys.PIN_HASH] = hash
        }
    }

    suspend fun verifyPin(inputPin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val salt = prefs[PreferencesKeys.PIN_SALT] ?: return false
        val savedHash = prefs[PreferencesKeys.PIN_HASH] ?: return false
        val computedHash = hashPin(inputPin, salt)
        return computedHash == savedHash
    }

    suspend fun clearPin() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.PIN_HASH)
            prefs.remove(PreferencesKeys.PIN_SALT)
            prefs[PreferencesKeys.IS_LOCK_ENABLED] = false
        }
        _isAppLocked.value = false
    }

    fun lock() {
        _isAppLocked.value = true
    }

    fun unlock() {
        _isAppLocked.value = false
        lastBackgroundTimestampMs = 0L
    }

    fun onAppBackgrounded(timestampMs: Long = System.currentTimeMillis()) {
        lastBackgroundTimestampMs = timestampMs
    }

    suspend fun onAppForegrounded(timestampMs: Long = System.currentTimeMillis()): Boolean {
        val enabled = isLockEnabled.first()
        if (!enabled) return false

        val timeoutSeconds = lockTimeoutSeconds.first()
        val elapsedSeconds = if (lastBackgroundTimestampMs > 0) {
            (timestampMs - lastBackgroundTimestampMs) / 1000
        } else {
            Long.MAX_VALUE
        }

        if (elapsedSeconds >= timeoutSeconds) {
            _isAppLocked.value = true
            return true
        }
        return false
    }

    companion object {
        fun hashPin(pin: String, salt: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val input = "$pin:$salt".toByteArray(Charsets.UTF_8)
            val digest = md.digest(input)
            return digest.joinToString("") { "%02x".format(it) }
        }

        fun generateSalt(): String {
            val random = SecureRandom()
            val saltBytes = ByteArray(16)
            random.nextBytes(saltBytes)
            return saltBytes.joinToString("") { "%02x".format(it) }
        }
    }
}

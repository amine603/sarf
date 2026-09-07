package com.cash.guide.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.data.SettingsRepository
import com.cash.guide.data.backup.BackupManager
import com.cash.guide.domain.MoneyUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.appLanguage,
                settingsRepository.defaultCurrency
            ) { lang, currency ->
                _uiState.value.copy(
                    currentLanguage = lang,
                    defaultCurrency = currency,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectLanguage(languageCode: String) {
        viewModelScope.launch {
            settingsRepository.setAppLanguage(languageCode)
            val locale = when (languageCode) {
                "dar" -> java.util.Locale("ar", "MA")
                "ar" -> java.util.Locale("ar")
                "en" -> java.util.Locale("en")
                else -> java.util.Locale("fr")
            }
            java.util.Locale.setDefault(locale)
            val appLocaleTag = when (languageCode) {
                "dar" -> "ar-MA"
                "ar" -> "ar"
                "en" -> "en"
                else -> "fr"
            }
            val appLocale = LocaleListCompat.forLanguageTags(appLocaleTag)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun selectDefaultCurrency(unit: MoneyUnit) {
        viewModelScope.launch {
            settingsRepository.setDefaultCurrency(unit)
        }
    }

    fun exportBackupToUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingBackup = true) }
            val success = try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    backupManager.writeBackupToStream(stream)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                _uiState.update { it.copy(isProcessingBackup = false) }
            }
            onResult(success)
        }
    }

    fun shareBackup(context: Context, chooserTitle: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingBackup = true) }
            val success = try {
                val shareUri = backupManager.createShareableBackupFile(context)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Sarf Backup (.calc)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, chooserTitle).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                _uiState.update { it.copy(isProcessingBackup = false) }
            }
            onResult(success)
        }
    }

    fun loadBackupForInspection(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingBackup = true) }
            try {
                val result = context.contentResolver.openInputStream(uri)?.use { stream ->
                    backupManager.readBackupFromStream(stream)
                }
                val payload = result?.getOrNull()
                if (payload != null) {
                    _uiState.update { it.copy(restoreCandidate = payload, isProcessingBackup = false) }
                    onResult(true)
                } else {
                    _uiState.update { it.copy(isProcessingBackup = false) }
                    onResult(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isProcessingBackup = false) }
                onResult(false)
            }
        }
    }

    fun confirmRestore(replaceExisting: Boolean, onResult: (Boolean) -> Unit) {
        val payload = _uiState.value.restoreCandidate ?: run {
            onResult(false)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingBackup = true) }
            val success = try {
                backupManager.restore(payload, replaceExisting)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                _uiState.update { it.copy(restoreCandidate = null, isProcessingBackup = false) }
            }
            onResult(success)
        }
    }

    fun dismissRestoreDialog() {
        _uiState.update { it.copy(restoreCandidate = null) }
    }
}

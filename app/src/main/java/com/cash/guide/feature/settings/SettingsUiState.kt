package com.cash.guide.feature.settings

import com.cash.guide.data.backup.BackupPayload
import com.cash.guide.domain.MoneyUnit

data class SettingsUiState(
    val currentLanguage: String = "fr",
    val defaultCurrency: MoneyUnit = MoneyUnit.DIRHAM,
    val isLoading: Boolean = true,
    val isProcessingBackup: Boolean = false,
    val restoreCandidate: BackupPayload? = null
)

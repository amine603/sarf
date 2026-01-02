package com.cash.guide.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.data.Countries
import com.cash.guide.data.RateResult
import com.cash.guide.data.RatesRepository
import com.cash.guide.settings.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RatesUiState(
    val homeCurrency: String? = null,
    val baseCurrency: String = "MAD",
    val isLoading: Boolean = false,
    val lastError: String? = null,
    val localToHomeRate: Double? = null,
    val rateFromCache: Boolean = false,
    val rateUpdatedAtMs: Long? = null,

    // USD reference (Value Understanding system)
    val usdIsLoading: Boolean = false,
    val usdLastError: String? = null,
    val localToUsdRate: Double? = null,
    val usdRateFromCache: Boolean = false,
    val usdRateUpdatedAtMs: Long? = null
)

class RatesViewModel(app: Application) : AndroidViewModel(app) {
    private val settings = AppSettings(app)
    private val repo = RatesRepository(app)

    private val _state = MutableStateFlow(RatesUiState())
    val state: StateFlow<RatesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settings.homeCurrency.collectLatest { code ->
                // Keep UI in sync with Settings even if changed from SettingsScreen.
                val normalized = code?.trim()?.uppercase()?.takeIf { it.length == 3 }
                val prev = _state.value.homeCurrency
                if (prev != normalized) {
                    _state.value = _state.value.copy(
                        homeCurrency = normalized,
                        // Clear the in-memory rate when currency changes (will be reloaded on demand).
                        localToHomeRate = null,
                        lastError = null,
                        rateFromCache = false,
                        rateUpdatedAtMs = null
                    )
                }
            }
        }

        viewModelScope.launch {
            settings.selectedCountryCode.collectLatest { countryCode ->
                val base = resolveBaseCurrency(countryCode)
                if (_state.value.baseCurrency != base) {
                    _state.value = _state.value.copy(
                        baseCurrency = base,
                        localToHomeRate = null,
                        lastError = null,
                        rateFromCache = false,
                        rateUpdatedAtMs = null,

                        localToUsdRate = null,
                        usdLastError = null,
                        usdRateFromCache = false,
                        usdRateUpdatedAtMs = null
                    )
                }
            }
        }
    }

    fun setHomeCurrency(code: String) {
        viewModelScope.launch {
            settings.setHomeCurrency(code)
            _state.value = _state.value.copy(homeCurrency = code.trim().uppercase())
            ensureRateLoaded(forceRefresh = true)
        }
    }

    /**
     * Ensure we have a rate loaded for the selected home currency.
     * This will fetch at most once per session unless forceRefresh is true.
     */
    fun ensureRateLoaded(baseCurrency: String? = null, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val home = settings.homeCurrency.first()
            val base = (baseCurrency ?: resolveBaseCurrency(settings.selectedCountryCode.first())).trim().uppercase()
            _state.value = _state.value.copy(homeCurrency = home, baseCurrency = base)

            if (home == null) return@launch

            val normalizedHome = home.trim().uppercase()
            if (base == normalizedHome) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    localToHomeRate = 1.0,
                    rateFromCache = true,
                    rateUpdatedAtMs = System.currentTimeMillis(),
                    lastError = null
                )
                return@launch
            }

            val existing = _state.value.localToHomeRate
            if (!forceRefresh && existing != null && _state.value.homeCurrency == normalizedHome && _state.value.baseCurrency == base) return@launch

            _state.value = _state.value.copy(isLoading = true, lastError = null)
            when (val res = repo.getRate(base = base, target = normalizedHome, forceRefresh = forceRefresh)) {
                is RateResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        localToHomeRate = res.rate,
                        rateFromCache = res.fromCache,
                        rateUpdatedAtMs = res.updatedAtMs,
                        lastError = null
                    )
                }
                RateResult.Unavailable -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        localToHomeRate = null,
                        rateFromCache = false,
                        rateUpdatedAtMs = null,
                        lastError = "unavailable"
                    )
                }
            }
        }
    }

    fun refreshRates() {
        ensureRateLoaded(forceRefresh = true)
    }

    /**
     * Ensure we have a USD reference rate loaded for the current local currency.
     * This powers the "Value Understanding" icon buckets.
     */
    fun ensureUsdRateLoaded(baseCurrency: String? = null, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val base = (baseCurrency ?: resolveBaseCurrency(settings.selectedCountryCode.first())).trim().uppercase()
            _state.value = _state.value.copy(baseCurrency = base)

            if (base == "USD") {
                _state.value = _state.value.copy(
                    usdIsLoading = false,
                    localToUsdRate = 1.0,
                    usdRateFromCache = true,
                    usdRateUpdatedAtMs = System.currentTimeMillis(),
                    usdLastError = null
                )
                return@launch
            }

            val existing = _state.value.localToUsdRate
            if (!forceRefresh && existing != null && _state.value.baseCurrency == base) return@launch

            _state.value = _state.value.copy(usdIsLoading = true, usdLastError = null)
            when (val res = repo.getRate(base = base, target = "USD", forceRefresh = forceRefresh)) {
                is RateResult.Success -> {
                    _state.value = _state.value.copy(
                        usdIsLoading = false,
                        localToUsdRate = res.rate,
                        usdRateFromCache = res.fromCache,
                        usdRateUpdatedAtMs = res.updatedAtMs,
                        usdLastError = null
                    )
                }
                RateResult.Unavailable -> {
                    _state.value = _state.value.copy(
                        usdIsLoading = false,
                        localToUsdRate = null,
                        usdRateFromCache = false,
                        usdRateUpdatedAtMs = null,
                        usdLastError = "unavailable"
                    )
                }
            }
        }
    }

    private fun resolveBaseCurrency(countryCode: String?): String {
        val code = countryCode?.trim()?.uppercase()
        return Countries.byCode(code)?.localCurrencyCode ?: "MAD"
    }
}



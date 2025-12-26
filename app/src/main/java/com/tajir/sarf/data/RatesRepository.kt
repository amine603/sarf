package com.tajir.sarf.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tajir.sarf.network.ExchangeRateApi
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private val Context.ratesStore by preferencesDataStore(name = "sarf_rates_cache")

/**
 * Fetches and caches exchange rates.
 *
 * - Fetch once per session (ViewModel keeps it in memory)
 * - Cache in DataStore with timestamp
 * - If offline/fetch fails, fall back to cached rates
 */
class RatesRepository(private val context: Context) {

    private val api: ExchangeRateApi by lazy {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()

        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }

    private fun ratesKey(base: String) = stringPreferencesKey("rates_$base")
    private fun tsKey(base: String) = longPreferencesKey("rates_${base}_ts")

    /**
     * Returns the rate for 1 [base] -> [target].
     *
     * If [forceRefresh] is false and cached data is fresh, uses cache.
     * Otherwise tries network first and falls back to cache on failure.
     */
    suspend fun getRate(base: String, target: String, forceRefresh: Boolean = false): RateResult {
        val normalizedBase = base.uppercase()
        val normalizedTarget = target.uppercase()

        val cached = readCachedRates(normalizedBase)
        val now = System.currentTimeMillis()
        val maxAgeMs = 12L * 60L * 60L * 1000L // 12 hours

        val cachedRate = cached?.rates?.get(normalizedTarget)
        val cacheFresh = cached != null && (now - cached.timestampMs) <= maxAgeMs

        if (!forceRefresh && cacheFresh && cachedRate != null) {
            return RateResult.Success(
                rate = cachedRate,
                fromCache = true,
                updatedAtMs = cached.timestampMs
            )
        }

        // Try network
        val network = runCatching { api.latest(normalizedBase) }.getOrNull()
        val networkRates = network?.rates
        val networkRate = networkRates?.get(normalizedTarget)

        if (networkRates != null && networkRate != null) {
            writeCachedRates(
                base = normalizedBase,
                timestampMs = now,
                rates = networkRates
            )
            return RateResult.Success(rate = networkRate, fromCache = false, updatedAtMs = now)
        }

        // Fallback to cache if available
        if (cachedRate != null) {
            return RateResult.Success(
                rate = cachedRate,
                fromCache = true,
                updatedAtMs = cached?.timestampMs ?: 0L
            )
        }

        return RateResult.Unavailable
    }

    private data class CachedRates(val timestampMs: Long, val rates: Map<String, Double>)

    private suspend fun readCachedRates(base: String): CachedRates? {
        val prefs = context.ratesStore.data.first()
        val raw = prefs[ratesKey(base)] ?: return null
        val ts = prefs[tsKey(base)] ?: return null
        val parsed = parseRates(raw)
        if (parsed.isEmpty()) return null
        return CachedRates(timestampMs = ts, rates = parsed)
    }

    private suspend fun writeCachedRates(base: String, timestampMs: Long, rates: Map<String, Double>) {
        val raw = serializeRates(rates)
        context.ratesStore.edit { prefs ->
            prefs[ratesKey(base)] = raw
            prefs[tsKey(base)] = timestampMs
        }
    }

    private fun serializeRates(rates: Map<String, Double>): String {
        // Simple, deterministic, no-JSON to keep storage light.
        // Format: "EUR=0.092;USD=0.101;..."
        return rates.entries
            .sortedBy { it.key }
            .joinToString(separator = ";") { (k, v) -> "${k.uppercase()}=$v" }
    }

    private fun parseRates(raw: String): Map<String, Double> {
        if (raw.isBlank()) return emptyMap()
        val out = LinkedHashMap<String, Double>()
        raw.split(";").forEach { part ->
            val idx = part.indexOf("=")
            if (idx <= 0 || idx >= part.length - 1) return@forEach
            val k = part.substring(0, idx).trim().uppercase()
            val v = part.substring(idx + 1).trim().toDoubleOrNull() ?: return@forEach
            out[k] = v
        }
        return out
    }
}

sealed class RateResult {
    data class Success(
        val rate: Double,
        val fromCache: Boolean,
        val updatedAtMs: Long
    ) : RateResult()

    data object Unavailable : RateResult()
}






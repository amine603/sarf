package com.cash.guide.network

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Free exchange-rate API (no key) used for tourist-friendly conversions.
 *
 * Endpoint: https://open.er-api.com/v6/latest/{base}
 */
interface ExchangeRateApi {
    @GET("v6/latest/{base}")
    suspend fun latest(@Path("base") base: String): ExchangeRatesResponse
}

data class ExchangeRatesResponse(
    val result: String? = null,
    val time_last_update_unix: Long? = null,
    val base_code: String? = null,
    val rates: Map<String, Double>? = null,
)






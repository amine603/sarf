package com.tajir.sarf.data

import com.tajir.sarf.R

/**
 * Countries supported by the currency guide.
 *
 * NOTE: Only Morocco has bundled currency images in assets right now.
 * Other countries can be added later by adding their assets and denomination lists.
 */
object Countries {
    data class CountrySpec(
        val code: String,          // e.g. "MA"
        val nameResId: Int,        // localized country name (via string resources)
        val localCurrencyCode: String, // e.g. "MAD"
        val assetsAvailable: Boolean
    )

    val ALL: List<CountrySpec> = listOf(
        CountrySpec(code = "MA", nameResId = R.string.country_ma, localCurrencyCode = "MAD", assetsAvailable = true),
        // Euro area (EUR) — uses assets in app/src/main/assets/europic/
        CountrySpec(code = "ES", nameResId = R.string.country_es, localCurrencyCode = "EUR", assetsAvailable = true),
        CountrySpec(code = "PT", nameResId = R.string.country_pt, localCurrencyCode = "EUR", assetsAvailable = true),
        CountrySpec(code = "FR", nameResId = R.string.country_fr, localCurrencyCode = "EUR", assetsAvailable = true),
        CountrySpec(code = "DE", nameResId = R.string.country_de, localCurrencyCode = "EUR", assetsAvailable = true),
        CountrySpec(code = "IT", nameResId = R.string.country_it, localCurrencyCode = "EUR", assetsAvailable = true),
        CountrySpec(code = "NL", nameResId = R.string.country_nl, localCurrencyCode = "EUR", assetsAvailable = true),
        CountrySpec(code = "BE", nameResId = R.string.country_be, localCurrencyCode = "EUR", assetsAvailable = true),
        CountrySpec(code = "AT", nameResId = R.string.country_at, localCurrencyCode = "EUR", assetsAvailable = true),
        CountrySpec(code = "IE", nameResId = R.string.country_ie, localCurrencyCode = "EUR", assetsAvailable = true),
        CountrySpec(code = "GR", nameResId = R.string.country_gr, localCurrencyCode = "EUR", assetsAvailable = true),
        // Saudi Arabia (SAR) — uses assets in app/src/main/assets/saudipic/
        CountrySpec(code = "SA", nameResId = R.string.country_sa, localCurrencyCode = "SAR", assetsAvailable = true),
        CountrySpec(code = "US", nameResId = R.string.country_us, localCurrencyCode = "USD", assetsAvailable = false),
        CountrySpec(code = "GB", nameResId = R.string.country_gb, localCurrencyCode = "GBP", assetsAvailable = false),
    )

    fun byCode(code: String?): CountrySpec? =
        ALL.firstOrNull { it.code.equals(code, ignoreCase = true) }
}





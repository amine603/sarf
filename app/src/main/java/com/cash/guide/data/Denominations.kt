package com.cash.guide.data

/**
 * Moroccan currency denominations, represented in **cents** for safe centimes support.
 *
 * Single source of truth mapping: denomination (cents) -> asset path + display label.
 */
object Denominations {
    /**
     * Denomination metadata used throughout the app.
     *
     * valueCents examples:
     * - 200 DHS  -> 20000
     * - 0.50 DHS -> 50
     */
    data class DenominationSpec(
        val valueCents: Int,
        val assetPath: String,
        val displayLabel: String,
        val isCoin: Boolean
    )

    /**
     * Moroccan denominations (DHS), in descending order (largest first).
     * This order is important for the greedy algorithm.
     */
    val MAD: List<DenominationSpec> = listOf(
        DenominationSpec(valueCents = 20000, assetPath = "sarfpic/200dh.png", displayLabel = "200 DHS", isCoin = false),
        DenominationSpec(valueCents = 10000, assetPath = "sarfpic/100dh.png", displayLabel = "100 DHS", isCoin = false),
        DenominationSpec(valueCents = 5000, assetPath = "sarfpic/50dh.png", displayLabel = "50 DHS", isCoin = false),
        DenominationSpec(valueCents = 2000, assetPath = "sarfpic/20dh.png", displayLabel = "20 DHS", isCoin = false),
        DenominationSpec(valueCents = 1000, assetPath = "sarfpic/10dh.png", displayLabel = "10 DHS", isCoin = true),
        DenominationSpec(valueCents = 500, assetPath = "sarfpic/5dh.png", displayLabel = "5 DHS", isCoin = true),
        DenominationSpec(valueCents = 200, assetPath = "sarfpic/2dh.png", displayLabel = "2 DHS", isCoin = true),
        DenominationSpec(valueCents = 100, assetPath = "sarfpic/1dh.png", displayLabel = "1 DHS", isCoin = true),
        DenominationSpec(valueCents = 50, assetPath = "sarfpic/50centime.png", displayLabel = "0.50 DHS", isCoin = true),
        DenominationSpec(valueCents = 20, assetPath = "sarfpic/20centime.png", displayLabel = "0.20 DHS", isCoin = true),
        DenominationSpec(valueCents = 10, assetPath = "sarfpic/10centime.png", displayLabel = "0.10 DHS", isCoin = true),
    )

    /**
     * Euro denominations (EUR), in descending order (largest first).
     *
     * Asset paths must match files in: app/src/main/assets/europic/
     * (Spaces in filenames are OK; assets are loaded by exact path.)
     */
    val EUR: List<DenominationSpec> = listOf(
        DenominationSpec(valueCents = 50000, assetPath = "europic/500 euros.png", displayLabel = "500 €", isCoin = false),
        DenominationSpec(valueCents = 20000, assetPath = "europic/200 euros.png", displayLabel = "200 €", isCoin = false),
        DenominationSpec(valueCents = 10000, assetPath = "europic/100 euros.png", displayLabel = "100 €", isCoin = false),
        DenominationSpec(valueCents = 5000, assetPath = "europic/50 euros.png", displayLabel = "50 €", isCoin = false),
        DenominationSpec(valueCents = 2000, assetPath = "europic/20 euros.png", displayLabel = "20 €", isCoin = false),
        DenominationSpec(valueCents = 1000, assetPath = "europic/10 euros.png", displayLabel = "10 €", isCoin = false),
        DenominationSpec(valueCents = 500, assetPath = "europic/5 euros.png", displayLabel = "5 €", isCoin = false),
        DenominationSpec(valueCents = 200, assetPath = "europic/2 euros.png", displayLabel = "2 €", isCoin = true),
        DenominationSpec(valueCents = 100, assetPath = "europic/1 euro.png", displayLabel = "1 €", isCoin = true),
        DenominationSpec(valueCents = 50, assetPath = "europic/50 cents.png", displayLabel = "0.50 €", isCoin = true),
        DenominationSpec(valueCents = 20, assetPath = "europic/20 cents.png", displayLabel = "0.20 €", isCoin = true),
        DenominationSpec(valueCents = 10, assetPath = "europic/10 cents.png", displayLabel = "0.10 €", isCoin = true),
        DenominationSpec(valueCents = 5, assetPath = "europic/5 cents.png", displayLabel = "0.05 €", isCoin = true),
        DenominationSpec(valueCents = 2, assetPath = "europic/2 cents.png", displayLabel = "0.02 €", isCoin = true),
        DenominationSpec(valueCents = 1, assetPath = "europic/1 cent.png", displayLabel = "0.01 €", isCoin = true),
    )

    /**
     * Saudi Riyal denominations (SAR), in descending order (largest first).
     *
     * Asset paths must match files in: app/src/main/assets/saudipic/
     * Supports both .png and .jpg formats.
     */
    val SAR: List<DenominationSpec> = listOf(
        DenominationSpec(valueCents = 50000, assetPath = "saudipic/500.png", displayLabel = "500 ر.س", isCoin = false),
        DenominationSpec(valueCents = 20000, assetPath = "saudipic/200.jpg", displayLabel = "200 ر.س", isCoin = false),
        DenominationSpec(valueCents = 10000, assetPath = "saudipic/100.png", displayLabel = "100 ر.س", isCoin = false),
        DenominationSpec(valueCents = 5000, assetPath = "saudipic/50.jpg", displayLabel = "50 ر.س", isCoin = false),
        DenominationSpec(valueCents = 1000, assetPath = "saudipic/10.jpg", displayLabel = "10 ر.س", isCoin = false),
        DenominationSpec(valueCents = 500, assetPath = "saudipic/5.jpg", displayLabel = "5 ر.س", isCoin = false),
        DenominationSpec(valueCents = 200, assetPath = "saudipic/2 r.png", displayLabel = "2 ر.س", isCoin = true),
        DenominationSpec(valueCents = 100, assetPath = "saudipic/1 r.png", displayLabel = "1 ر.س", isCoin = true),
        DenominationSpec(valueCents = 50, assetPath = "saudipic/50 h.png", displayLabel = "0.50 ر.س", isCoin = true),
        DenominationSpec(valueCents = 25, assetPath = "saudipic/25 h.png", displayLabel = "0.25 ر.س", isCoin = true),
        DenominationSpec(valueCents = 10, assetPath = "saudipic/10 h.png", displayLabel = "0.10 ر.س", isCoin = true),
        DenominationSpec(valueCents = 5, assetPath = "saudipic/6 h.png", displayLabel = "0.05 ر.س", isCoin = true),
        DenominationSpec(valueCents = 1, assetPath = "saudipic/1 h.png", displayLabel = "0.01 ر.س", isCoin = true),
    )

    /**
     * US Dollar denominations (USD), in descending order (largest first).
     *
     * Asset paths must match files in: app/src/main/assets/dollarpic/
     * Supports both .png and .jpg formats.
     */
    val USD: List<DenominationSpec> = listOf(
        DenominationSpec(valueCents = 10000, assetPath = "dollarpic/100.jpg", displayLabel = "$100", isCoin = false),
        DenominationSpec(valueCents = 5000, assetPath = "dollarpic/50.jpg", displayLabel = "$50", isCoin = false),
        DenominationSpec(valueCents = 2000, assetPath = "dollarpic/20.jpg", displayLabel = "$20", isCoin = false),
        DenominationSpec(valueCents = 1000, assetPath = "dollarpic/10.jpg", displayLabel = "$10", isCoin = false),
        DenominationSpec(valueCents = 500, assetPath = "dollarpic/5.jpg", displayLabel = "$5", isCoin = false),
        DenominationSpec(valueCents = 200, assetPath = "dollarpic/2.jpg", displayLabel = "$2", isCoin = false),
        DenominationSpec(valueCents = 100, assetPath = "dollarpic/1.jpg", displayLabel = "$1", isCoin = false),
        DenominationSpec(valueCents = 25, assetPath = "dollarpic/25 cents.png", displayLabel = "$0.25", isCoin = true),
        DenominationSpec(valueCents = 10, assetPath = "dollarpic/10 cents.png", displayLabel = "$0.10", isCoin = true),
        DenominationSpec(valueCents = 5, assetPath = "dollarpic/5 cents.png", displayLabel = "$0.05", isCoin = true),
        DenominationSpec(valueCents = 1, assetPath = "dollarpic/1 cent.png", displayLabel = "$0.01", isCoin = true),
    )

    /**
     * British Pound denominations (GBP), in descending order (largest first).
     *
     * Asset paths must match files in: app/src/main/assets/ukpic/
     * Supports both .png formats.
     */
    val GBP: List<DenominationSpec> = listOf(
        DenominationSpec(valueCents = 5000, assetPath = "ukpic/50.png", displayLabel = "£50", isCoin = false),
        DenominationSpec(valueCents = 2000, assetPath = "ukpic/20.png", displayLabel = "£20", isCoin = false),
        DenominationSpec(valueCents = 1000, assetPath = "ukpic/10.png", displayLabel = "£10", isCoin = false),
        DenominationSpec(valueCents = 500, assetPath = "ukpic/5.png", displayLabel = "£5", isCoin = false),
        DenominationSpec(valueCents = 200, assetPath = "ukpic/2pound.png", displayLabel = "£2", isCoin = true),
        DenominationSpec(valueCents = 100, assetPath = "ukpic/1pound.png", displayLabel = "£1", isCoin = true),
        DenominationSpec(valueCents = 50, assetPath = "ukpic/50p.png", displayLabel = "50p", isCoin = true),
        DenominationSpec(valueCents = 20, assetPath = "ukpic/20p.png", displayLabel = "20p", isCoin = true),
        DenominationSpec(valueCents = 10, assetPath = "ukpic/10p.png", displayLabel = "10p", isCoin = true),
        DenominationSpec(valueCents = 5, assetPath = "ukpic/5p.png", displayLabel = "5p", isCoin = true),
        DenominationSpec(valueCents = 2, assetPath = "ukpic/2p.png", displayLabel = "2p", isCoin = true),
        DenominationSpec(valueCents = 1, assetPath = "ukpic/1p.png", displayLabel = "1p", isCoin = true),
    )

    /**
     * Backwards-compatible alias: historically the app was MAD-only.
     */
    val ALL: List<DenominationSpec> = MAD

    fun allForCurrency(currencyCode: String?): List<DenominationSpec> {
        return when (currencyCode?.trim()?.uppercase()) {
            "EUR" -> EUR
            "MAD" -> MAD
            "SAR" -> SAR
            "USD" -> USD
            "GBP" -> GBP
            else -> MAD
        }
    }

    /**
     * Format cents -> "129.00 DHS"
     */
    fun formatCents(cents: Int): String {
        val abs = kotlin.math.abs(cents)
        val mad = abs / 100
        val cent = abs % 100
        val sign = if (cents < 0) "-" else ""
        return "$sign$mad.${cent.toString().padStart(2, '0')} DHS"
    }

    fun formatCentsCompact(cents: Int): String {
        val abs = kotlin.math.abs(cents)
        val mad = abs / 100
        val cent = abs % 100
        val sign = if (cents < 0) "-" else ""
        return if (cent == 0) "$sign$mad DHS" else "$sign$mad.${cent.toString().padStart(2, '0')} DHS"
    }

    /**
     * Format cents -> "129.00" (no currency suffix, for UI that wants localized currency rendering).
     */
    fun formatCentsCompactNumber(cents: Int): String {
        val abs = kotlin.math.abs(cents)
        val mad = abs / 100
        val cent = abs % 100
        val sign = if (cents < 0) "-" else ""
        return if (cent == 0) "$sign$mad" else "$sign$mad.${cent.toString().padStart(2, '0')}"
    }

    fun getSpec(valueCents: Int): DenominationSpec? = ALL.firstOrNull { it.valueCents == valueCents }
}


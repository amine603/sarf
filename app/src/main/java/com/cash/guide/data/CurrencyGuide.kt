package com.cash.guide.data

import android.content.Context

/**
 * Offline-friendly tourist "value guide" text for each denomination.
 *
 * This is intentionally simple (short sentences) and does NOT rely on network.
 */
object CurrencyGuide {
    data class ValueCard(
        val denomination: Denominations.DenominationSpec,
        val shortHint: String,
        val detailTitle: String,
        val detailDescription: String,
        val examplePrices: List<String> = emptyList()
    )

    /**
     * Backwards-compatible default (MAD).
     */
    val ALL: List<ValueCard> = cardsForCurrency("MAD")

    fun cardsForCurrency(currencyCode: String?): List<ValueCard> {
        val code = currencyCode?.trim()?.uppercase()
        val denoms = Denominations.allForCurrency(code)
        return when (code) {
            "EUR" -> buildEuroCards(denoms)
            "SAR" -> buildSaudiCards(denoms)
            "USD" -> buildUSDCards(denoms)
            "GBP" -> buildGBPCards(denoms)
            else -> buildMoroccoCards(denoms)
        }
    }

    fun byValueCents(currencyCode: String?, valueCents: Int): ValueCard? =
        cardsForCurrency(currencyCode).firstOrNull { it.denomination.valueCents == valueCents }

    /**
     * Localize CurrencyGuide content (short hints, descriptions, examples).
     *
     * UI text (buttons, labels, etc.) must use Android string resources.
     * This helper is ONLY for the offline guide content kept in Kotlin.
     *
     * If a translation is missing, it gracefully falls back to the original text.
     */
    fun localize(context: Context, text: String): String {
        val key = text.trim()
        val lang = runCatching { context.resources.configuration.locales[0].language }.getOrNull()?.lowercase() ?: "en"
        val map = when (lang) {
            "fr" -> FR
            "ar" -> AR
            "es" -> ES
            "pt" -> PT
            else -> null
        }
        return map?.get(key) ?: key
    }

    private fun buildMoroccoCards(denoms: List<Denominations.DenominationSpec>): List<ValueCard> {
        return denoms.mapNotNull { spec ->
            when (spec.valueCents) {
                20000 -> ValueCard(
                    denomination = spec,
                    shortHint = "≈ nice dinner for two",
                    detailTitle = "200 DHS",
                    detailDescription = "A high-value banknote. Often used for meals, shopping, or larger payments.",
                    examplePrices = listOf(
                        "Dinner for two",
                        "Taxi across the city",
                        "Souvenir bundle",
                        "Groceries + extras",
                        "Hammam / spa entry",
                        "Intercity bus / shared taxi",
                        "Museum + snacks",
                        "Phone data top‑up",
                        "Small day trip transport",
                        "Restaurant + dessert"
                    )
                )

                10000 -> ValueCard(
                    denomination = spec,
                    shortHint = "≈ taxi long ride",
                    detailTitle = "100 DHS",
                    detailDescription = "Very common for daily spending and taxis. Many shops can break this note.",
                    examplePrices = listOf(
                        "Taxi ride",
                        "Lunch for one",
                        "Groceries",
                        "Museum tickets",
                        "Café + snack",
                        "Train / bus ticket (short)",
                        "Phone data top‑up",
                        "Souvenir",
                        "Basic essentials",
                        "Small attraction entry"
                    )
                )

                5000 -> ValueCard(
                    denomination = spec,
                    shortHint = "≈ coffee + sandwich",
                    detailTitle = "50 DHS",
                    detailDescription = "A popular everyday note. Great for cafés, snacks, and small purchases.",
                    examplePrices = listOf(
                        "Coffee + snack",
                        "Sandwich",
                        "Fresh juice",
                        "Bakery items",
                        "Short taxi (very short)",
                        "Small shopping",
                        "Local attraction",
                        "SIM / data top‑up (small)",
                        "Water + snack",
                        "Street food"
                    )
                )

                2000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common daily use",
                    detailTitle = "20 DHS",
                    detailDescription = "One of the most used notes. Handy for quick purchases and short trips.",
                    examplePrices = listOf(
                        "Snack + drink",
                        "Coffee",
                        "Fresh juice",
                        "Bread + essentials",
                        "Small entry fee",
                        "Bus ticket (sometimes)",
                        "Tips (small)",
                        "Bottled water",
                        "Bakery item",
                        "Quick takeaway"
                    )
                )

                1000 -> ValueCard(
                    denomination = spec,
                    shortHint = "≈ quick coffee",
                    detailTitle = "10 DHS",
                    detailDescription = "A very common coin. Used everywhere for small items and change.",
                    examplePrices = listOf(
                        "Coffee (small)",
                        "Mint tea (sometimes)",
                        "Small tips",
                        "Bread",
                        "Candy / small treat",
                        "Extra change",
                        "Water (sometimes)",
                        "Street snack (small)"
                    )
                )

                500 -> ValueCard(
                    denomination = spec,
                    shortHint = "≈ bottle of water",
                    detailTitle = "5 DHS",
                    detailDescription = "Common for small purchases and getting the right change.",
                    examplePrices = listOf(
                        "Water bottle (small)",
                        "Bread",
                        "Candy",
                        "Small snack",
                        "Exact change",
                        "Extra coin for rounding"
                    )
                )

                200 -> ValueCard(
                    denomination = spec,
                    shortHint = "≈ small snack",
                    detailTitle = "2 DHS",
                    detailDescription = "Useful for tiny purchases and as change.",
                    examplePrices = listOf(
                        "Extra change",
                        "Candy",
                        "Rounding",
                        "Small tip (sometimes)",
                        "Exact change"
                    )
                )

                100 -> ValueCard(
                    denomination = spec,
                    shortHint = "≈ small tip",
                    detailTitle = "1 DHS",
                    detailDescription = "Small coin used mainly for change.",
                    examplePrices = listOf(
                        "Change",
                        "Rounding",
                        "Small tip (sometimes)",
                        "Exact change"
                    )
                )

                50 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "0.50 DHS",
                    detailDescription = "Small coin used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Change")
                )

                20 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "0.20 DHS",
                    detailDescription = "Small coin used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Change")
                )

                10 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "0.10 DHS",
                    detailDescription = "Small coin used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Change")
                )

                else -> null
            }
        }
    }

    private fun buildSaudiCards(denoms: List<Denominations.DenominationSpec>): List<ValueCard> {
        return denoms.mapNotNull { spec ->
            when (spec.valueCents) {
                50000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Very high value",
                    detailTitle = "500 ر.س",
                    detailDescription = "A very high-value note. Rare in daily use. Many places may refuse it.",
                    examplePrices = listOf(
                        "Hotel deposit",
                        "Large purchase",
                        "Long-distance travel",
                        "Electronics (part)",
                        "Multi-day expenses",
                        "Not always accepted"
                    )
                )
                20000 -> ValueCard(
                    denomination = spec,
                    shortHint = "High value",
                    detailTitle = "200 ر.س",
                    detailDescription = "High-value note. Not always accepted in small shops.",
                    examplePrices = listOf(
                        "Shopping",
                        "Nice dinner for two",
                        "Hotel night (budget)",
                        "Intercity transport",
                        "Grocery run (big)",
                        "Not always accepted"
                    )
                )
                10000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common for shopping",
                    detailTitle = "100 ر.س",
                    detailDescription = "Common for shopping and travel expenses. Some small places may prefer smaller notes.",
                    examplePrices = listOf(
                        "Groceries + extras",
                        "Intercity transport",
                        "Restaurant for two",
                        "Museum + snacks",
                        "Day pass transport",
                        "Shopping essentials"
                    )
                )
                5000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Everyday payments",
                    detailTitle = "50 ر.س",
                    detailDescription = "A very common banknote for everyday spending.",
                    examplePrices = listOf(
                        "Restaurant",
                        "Museum + snacks",
                        "Groceries",
                        "Local train tickets",
                        "City activities",
                        "Shopping items"
                    )
                )
                1000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small note",
                    detailTitle = "10 ر.س",
                    detailDescription = "Very common for quick purchases and small expenses.",
                    examplePrices = listOf(
                        "Coffee + snack",
                        "Bakery items",
                        "Short metro tickets",
                        "Small souvenirs",
                        "Water + snack",
                        "Quick takeaway"
                    )
                )
                500 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small purchases",
                    detailTitle = "5 ر.س",
                    detailDescription = "Great for small purchases and change.",
                    examplePrices = listOf(
                        "Coffee",
                        "Bakery item",
                        "Snack",
                        "Bottle of water",
                        "Small ticket (sometimes)"
                    )
                )
                200 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common coin",
                    detailTitle = "2 ر.س",
                    detailDescription = "A common coin used everywhere.",
                    examplePrices = listOf(
                        "Small snack",
                        "Water (sometimes)",
                        "Coffee (sometimes)",
                        "Bus ticket (sometimes)",
                        "Extra change"
                    )
                )
                100 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common coin",
                    detailTitle = "1 ر.س",
                    detailDescription = "A common coin used everywhere.",
                    examplePrices = listOf(
                        "Small snack (sometimes)",
                        "Extra change",
                        "Rounding",
                        "Small tip (sometimes)"
                    )
                )
                50 -> ValueCard(
                    denomination = spec,
                    shortHint = "Coins / change",
                    detailTitle = "0.50 ر.س",
                    detailDescription = "Used for exact change and small purchases.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                25 -> ValueCard(
                    denomination = spec,
                    shortHint = "Coins / change",
                    detailTitle = "0.25 ر.س",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                10 -> ValueCard(
                    denomination = spec,
                    shortHint = "Coins / change",
                    detailTitle = "0.10 ر.س",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                5 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "0.05 ر.س",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                1 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "0.01 ر.س",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                else -> null
            }
        }
    }

    private fun buildUSDCards(denoms: List<Denominations.DenominationSpec>): List<ValueCard> {
        return denoms.mapNotNull { spec ->
            when (spec.valueCents) {
                10000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Very high value",
                    detailTitle = "$100",
                    detailDescription = "A very high-value note. Rare in daily use. Many places may refuse it.",
                    examplePrices = listOf(
                        "Hotel deposit",
                        "Large purchase",
                        "Long-distance travel",
                        "Electronics (part)",
                        "Multi-day expenses",
                        "Not always accepted"
                    )
                )
                5000 -> ValueCard(
                    denomination = spec,
                    shortHint = "High value",
                    detailTitle = "$50",
                    detailDescription = "High-value note. Not always accepted in small shops.",
                    examplePrices = listOf(
                        "Shopping",
                        "Nice dinner for two",
                        "Hotel night (budget)",
                        "Intercity transport",
                        "Grocery run (big)",
                        "Not always accepted"
                    )
                )
                2000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common for shopping",
                    detailTitle = "$20",
                    detailDescription = "One of the most used notes. Great for daily expenses and shopping.",
                    examplePrices = listOf(
                        "Lunch",
                        "Local transport day pass",
                        "Coffee + snack",
                        "Museum ticket",
                        "SIM / data top‑up",
                        "Groceries (small)"
                    )
                )
                1000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small note",
                    detailTitle = "$10",
                    detailDescription = "Very common for quick purchases and small expenses.",
                    examplePrices = listOf(
                        "Coffee + snack",
                        "Bakery items",
                        "Short metro tickets",
                        "Small souvenirs",
                        "Water + snack",
                        "Quick takeaway"
                    )
                )
                500 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small purchases",
                    detailTitle = "$5",
                    detailDescription = "Great for small purchases and change.",
                    examplePrices = listOf(
                        "Coffee",
                        "Bakery item",
                        "Snack",
                        "Bottle of water",
                        "Small ticket (sometimes)"
                    )
                )
                200 -> ValueCard(
                    denomination = spec,
                    shortHint = "Rare note",
                    detailTitle = "$2",
                    detailDescription = "A rare banknote. Not commonly used in daily transactions.",
                    examplePrices = listOf(
                        "Small purchases",
                        "Exact change",
                        "Rarely seen"
                    )
                )
                100 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common note",
                    detailTitle = "$1",
                    detailDescription = "The most common banknote. Used everywhere for small purchases.",
                    examplePrices = listOf(
                        "Coffee",
                        "Snack",
                        "Bottle of water",
                        "Small ticket",
                        "Tips"
                    )
                )
                25 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common coin",
                    detailTitle = "$0.25",
                    detailDescription = "A very common coin (quarter). Used everywhere for exact change.",
                    examplePrices = listOf(
                        "Exact change",
                        "Parking meter",
                        "Vending machine",
                        "Small tip",
                        "Rounding"
                    )
                )
                10 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common coin",
                    detailTitle = "$0.10",
                    detailDescription = "A common coin (dime). Used for exact change.",
                    examplePrices = listOf(
                        "Exact change",
                        "Vending machine",
                        "Rounding",
                        "Small change"
                    )
                )
                5 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "$0.05",
                    detailDescription = "A small coin (nickel). Used for exact change.",
                    examplePrices = listOf(
                        "Exact change",
                        "Rounding",
                        "Small change"
                    )
                )
                1 -> ValueCard(
                    denomination = spec,
                    shortHint = "Smallest change",
                    detailTitle = "$0.01",
                    detailDescription = "The smallest coin (penny). Used for exact change.",
                    examplePrices = listOf(
                        "Exact change",
                        "Rounding",
                        "Smallest change"
                    )
                )
                else -> null
            }
        }
    }

    private fun buildGBPCards(denoms: List<Denominations.DenominationSpec>): List<ValueCard> {
        return denoms.mapNotNull { spec ->
            when (spec.valueCents) {
                5000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Very high value",
                    detailTitle = "£50",
                    detailDescription = "A very high-value note. Rare in daily use. Many places may refuse it.",
                    examplePrices = listOf(
                        "Hotel deposit",
                        "Large purchase",
                        "Long-distance travel",
                        "Electronics (part)",
                        "Multi-day expenses",
                        "Not always accepted"
                    )
                )
                2000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common for shopping",
                    detailTitle = "£20",
                    detailDescription = "One of the most used notes. Great for daily expenses and shopping.",
                    examplePrices = listOf(
                        "Lunch",
                        "Local transport day pass",
                        "Coffee + snack",
                        "Museum ticket",
                        "SIM / data top‑up",
                        "Groceries (small)"
                    )
                )
                1000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small note",
                    detailTitle = "£10",
                    detailDescription = "Very common for quick purchases and small expenses.",
                    examplePrices = listOf(
                        "Coffee + snack",
                        "Bakery items",
                        "Short metro tickets",
                        "Small souvenirs",
                        "Water + snack",
                        "Quick takeaway"
                    )
                )
                500 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small purchases",
                    detailTitle = "£5",
                    detailDescription = "Great for small purchases and change.",
                    examplePrices = listOf(
                        "Coffee",
                        "Bakery item",
                        "Snack",
                        "Bottle of water",
                        "Small ticket (sometimes)"
                    )
                )
                200 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common coin",
                    detailTitle = "£2",
                    detailDescription = "A common coin used everywhere.",
                    examplePrices = listOf(
                        "Small snack",
                        "Water (sometimes)",
                        "Coffee (sometimes)",
                        "Bus ticket (sometimes)",
                        "Extra change"
                    )
                )
                100 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common coin",
                    detailTitle = "£1",
                    detailDescription = "A common coin used everywhere.",
                    examplePrices = listOf(
                        "Small snack (sometimes)",
                        "Extra change",
                        "Rounding",
                        "Small tip (sometimes)"
                    )
                )
                50 -> ValueCard(
                    denomination = spec,
                    shortHint = "Coins / change",
                    detailTitle = "50p",
                    detailDescription = "Used for exact change and small purchases.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                20 -> ValueCard(
                    denomination = spec,
                    shortHint = "Coins / change",
                    detailTitle = "20p",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                10 -> ValueCard(
                    denomination = spec,
                    shortHint = "Coins / change",
                    detailTitle = "10p",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                5 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "5p",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                2 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "2p",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                1 -> ValueCard(
                    denomination = spec,
                    shortHint = "Smallest change",
                    detailTitle = "1p",
                    detailDescription = "The smallest coin. Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Smallest change")
                )
                else -> null
            }
        }
    }

    private fun buildEuroCards(denoms: List<Denominations.DenominationSpec>): List<ValueCard> {
        return denoms.mapNotNull { spec ->
            when (spec.valueCents) {
                50000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Very high value",
                    detailTitle = "500 €",
                    detailDescription = "A very high-value note. Rare in daily use. Many places may refuse it.",
                    examplePrices = listOf(
                        "Hotel deposit",
                        "Large purchase",
                        "Long-distance travel",
                        "Electronics (part)",
                        "Multi-day expenses",
                        "Not always accepted"
                    )
                )
                20000 -> ValueCard(
                    denomination = spec,
                    shortHint = "High value",
                    detailTitle = "200 €",
                    detailDescription = "High-value note. Not always accepted in small shops.",
                    examplePrices = listOf(
                        "Shopping",
                        "Nice dinner for two",
                        "Hotel night (budget)",
                        "Intercity transport",
                        "Grocery run (big)",
                        "Not always accepted"
                    )
                )
                10000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common for shopping",
                    detailTitle = "100 €",
                    detailDescription = "Common for shopping and travel expenses. Some small places may prefer smaller notes.",
                    examplePrices = listOf(
                        "Groceries + extras",
                        "Intercity transport",
                        "Restaurant for two",
                        "Museum + snacks",
                        "Day pass transport",
                        "Shopping essentials"
                    )
                )
                5000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Everyday payments",
                    detailTitle = "50 €",
                    detailDescription = "A very common banknote for everyday spending.",
                    examplePrices = listOf(
                        "Restaurant",
                        "Museum + snacks",
                        "Groceries",
                        "Local train tickets",
                        "City activities",
                        "Shopping items"
                    )
                )
                2000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Daily use",
                    detailTitle = "20 €",
                    detailDescription = "One of the most used notes. Great for daily expenses.",
                    examplePrices = listOf(
                        "Lunch",
                        "Local transport day pass",
                        "Coffee + snack",
                        "Museum ticket",
                        "SIM / data top‑up",
                        "Groceries (small)"
                    )
                )
                1000 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small note",
                    detailTitle = "10 €",
                    detailDescription = "Very common for quick purchases and small expenses.",
                    examplePrices = listOf(
                        "Coffee + snack",
                        "Bakery items",
                        "Short metro tickets",
                        "Small souvenirs",
                        "Water + snack",
                        "Quick takeaway"
                    )
                )
                500 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small purchases",
                    detailTitle = "5 €",
                    detailDescription = "Great for small purchases and change.",
                    examplePrices = listOf(
                        "Coffee",
                        "Bakery item",
                        "Snack",
                        "Bottle of water",
                        "Small ticket (sometimes)"
                    )
                )
                200 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common coin",
                    detailTitle = "2 €",
                    detailDescription = "A common coin used everywhere.",
                    examplePrices = listOf(
                        "Small snack",
                        "Water (sometimes)",
                        "Coffee (sometimes)",
                        "Bus ticket (sometimes)",
                        "Extra change"
                    )
                )
                100 -> ValueCard(
                    denomination = spec,
                    shortHint = "Common coin",
                    detailTitle = "1 €",
                    detailDescription = "A common coin used everywhere.",
                    examplePrices = listOf(
                        "Small snack (sometimes)",
                        "Extra change",
                        "Rounding",
                        "Small tip (sometimes)"
                    )
                )
                50 -> ValueCard(
                    denomination = spec,
                    shortHint = "Coins / change",
                    detailTitle = "0.50 €",
                    detailDescription = "Used for exact change and small purchases.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                20 -> ValueCard(
                    denomination = spec,
                    shortHint = "Coins / change",
                    detailTitle = "0.20 €",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                10 -> ValueCard(
                    denomination = spec,
                    shortHint = "Coins / change",
                    detailTitle = "0.10 €",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                5 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "0.05 €",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                2 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "0.02 €",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                1 -> ValueCard(
                    denomination = spec,
                    shortHint = "Small change",
                    detailTitle = "0.01 €",
                    detailDescription = "Used for exact change.",
                    examplePrices = listOf("Exact change", "Rounding", "Small change")
                )
                else -> null
            }
        }
    }

    // --- Translations for guide content (best-effort, short & simple) ---

    private val FR: Map<String, String> = mapOf(
        // Short hints
        "≈ nice dinner for two" to "≈ bon dîner pour deux",
        "≈ taxi long ride" to "≈ long trajet en taxi",
        "≈ coffee + sandwich" to "≈ café + sandwich",
        "Common daily use" to "Usage quotidien",
        "≈ quick coffee" to "≈ petit café",
        "≈ bottle of water" to "≈ bouteille d’eau",
        "≈ small snack" to "≈ petit snack",
        "≈ small tip" to "≈ petit pourboire",
        "Small change" to "Petite monnaie",
        "Very high value" to "Très grande valeur",
        "High value" to "Grande valeur",
        "Common for shopping" to "Courant pour les achats",
        "Everyday payments" to "Paiements du quotidien",
        "Daily use" to "Usage quotidien",
        "Small note" to "Petit billet",
        "Small purchases" to "Petits achats",
        "Common coin" to "Pièce courante",
        "Coins / change" to "Pièces / monnaie",

        // Descriptions (MAD)
        "A high-value banknote. Often used for meals, shopping, or larger payments." to "Billet de grande valeur, souvent utilisé pour les repas, le shopping ou les paiements importants.",
        "Very common for daily spending and taxis. Many shops can break this note." to "Très courant pour les dépenses quotidiennes et les taxis. Beaucoup de commerces peuvent rendre la monnaie.",
        "A popular everyday note. Great for cafés, snacks, and small purchases." to "Billet très utilisé au quotidien : cafés, snacks et petits achats.",
        "One of the most used notes. Handy for quick purchases and short trips." to "Un des billets les plus utilisés. Pratique pour les achats rapides et les petits trajets.",
        "A very common coin. Used everywhere for small items and change." to "Pièce très courante, utilisée partout pour les petits achats et la monnaie.",
        "Common for small purchases and getting the right change." to "Courant pour les petites dépenses et pour faire l’appoint.",
        "Useful for tiny purchases and as change." to "Utile pour de très petits achats et comme monnaie.",
        "Small coin used mainly for change." to "Petite pièce utilisée surtout pour la monnaie.",
        "Small coin used for exact change." to "Petite pièce utilisée pour faire l’appoint.",

        // Descriptions (EUR)
        "A very high-value note. Rare in daily use. Many places may refuse it." to "Billet de très grande valeur, rare au quotidien. Beaucoup d’endroits peuvent le refuser.",
        "High-value note. Not always accepted in small shops." to "Billet de grande valeur, pas toujours accepté dans les petits commerces.",
        "Common for shopping and travel expenses. Some small places may prefer smaller notes." to "Courant pour les achats et les dépenses de voyage. Certains petits commerces préfèrent des billets plus petits.",
        "A very common banknote for everyday spending." to "Billet très courant pour les dépenses du quotidien.",
        "One of the most used notes. Great for daily expenses." to "Un des billets les plus utilisés. Idéal pour les dépenses quotidiennes.",
        "One of the most used notes. Great for daily expenses and shopping." to "Un des billets les plus utilisés. Idéal pour les dépenses quotidiennes et le shopping.",
        "Very common for quick purchases and small expenses." to "Très courant pour les achats rapides et les petites dépenses.",
        "Great for small purchases and change." to "Parfait pour les petits achats et la monnaie.",
        "A common coin used everywhere." to "Pièce courante utilisée partout.",
        "Used for exact change and small purchases." to "Utilisée pour faire l’appoint et les petits achats.",
        "Used for exact change." to "Utilisée pour faire l’appoint.",
        "Rare note" to "Billet rare",
        "Common note" to "Billet courant",
        "A rare banknote. Not commonly used in daily transactions." to "Billet rare, peu utilisé dans les transactions quotidiennes.",
        "The most common banknote. Used everywhere for small purchases." to "Le billet le plus courant, utilisé partout pour les petits achats.",
        "The smallest coin (penny). Used for exact change." to "La plus petite pièce (centime). Utilisée pour faire l’appoint.",
        "The smallest coin. Used for exact change." to "La plus petite pièce. Utilisée pour faire l’appoint.",
        "Smallest change" to "Plus petite monnaie",
        "Rarely seen" to "Rarement vu",

        // Examples (generic)
        "Exact change" to "Monnaie exacte",
        "Rounding" to "Arrondi",
        "Change" to "Monnaie",
        "Small change" to "Petite monnaie",
        "Not always accepted" to "Pas toujours accepté",

        // Examples (MAD)
        "Dinner for two" to "Dîner pour deux",
        "Taxi across the city" to "Taxi à travers la ville",
        "Souvenir bundle" to "Lot de souvenirs",
        "Groceries + extras" to "Courses + extras",
        "Hammam / spa entry" to "Entrée hammam / spa",
        "Intercity bus / shared taxi" to "Bus interurbain / grand taxi",
        "Museum + snacks" to "Musée + snacks",
        "Phone data top‑up" to "Recharge internet",
        "Small day trip transport" to "Transport pour une petite excursion",
        "Restaurant + dessert" to "Restaurant + dessert",
        "Taxi ride" to "Trajet en taxi",
        "Lunch for one" to "Déjeuner pour une personne",
        "Groceries" to "Courses",
        "Museum tickets" to "Billets de musée",
        "Café + snack" to "Café + snack",
        "Train / bus ticket (short)" to "Billet train / bus (court)",
        "Souvenir" to "Souvenir",
        "Basic essentials" to "Essentiels",
        "Small attraction entry" to "Entrée petite attraction",
        "Coffee + snack" to "Café + snack",
        "Sandwich" to "Sandwich",
        "Fresh juice" to "Jus frais",
        "Bakery items" to "Viennoiseries",
        "Short taxi (very short)" to "Petit taxi (très court)",
        "Small shopping" to "Petits achats",
        "Local attraction" to "Attraction locale",
        "SIM / data top‑up (small)" to "Recharge SIM / internet (petite)",
        "Water + snack" to "Eau + snack",
        "Street food" to "Street food",
        "Snack + drink" to "Snack + boisson",
        "Coffee" to "Café",
        "Bread + essentials" to "Pain + essentiels",
        "Small entry fee" to "Petite entrée",
        "Bus ticket (sometimes)" to "Ticket de bus (parfois)",
        "Tips (small)" to "Pourboires (petits)",
        "Bottled water" to "Eau en bouteille",
        "Bakery item" to "Viennoiserie",
        "Quick takeaway" to "À emporter rapide",
        "Coffee (small)" to "Café (petit)",
        "Mint tea (sometimes)" to "Thé à la menthe (parfois)",
        "Small tips" to "Petits pourboires",
        "Bread" to "Pain",
        "Candy / small treat" to "Bonbon / petite douceur",
        "Extra change" to "Monnaie en plus",
        "Water (sometimes)" to "Eau (parfois)",
        "Street snack (small)" to "Snack de rue (petit)",
        "Water bottle (small)" to "Bouteille d’eau (petite)",
        "Candy" to "Bonbon",
        "Small snack" to "Petit snack",
        "Extra coin for rounding" to "Pièce pour l’arrondi",
        "Small tip (sometimes)" to "Petit pourboire (parfois)",

        // Examples (EUR)
        "Hotel deposit" to "Dépôt d’hôtel",
        "Large purchase" to "Gros achat",
        "Long-distance travel" to "Voyage longue distance",
        "Electronics (part)" to "Électronique (partie)",
        "Multi-day expenses" to "Dépenses sur plusieurs jours",
        "Shopping" to "Shopping",
        "Nice dinner for two" to "Beau dîner pour deux",
        "Hotel night (budget)" to "Nuit d’hôtel (budget)",
        "Intercity transport" to "Transport interurbain",
        "Grocery run (big)" to "Grosses courses",
        "Restaurant for two" to "Restaurant pour deux",
        "Day pass transport" to "Pass transport journée",
        "Shopping essentials" to "Achats essentiels",
        "Restaurant" to "Restaurant",
        "Local train tickets" to "Billets de train locaux",
        "City activities" to "Activités en ville",
        "Shopping items" to "Articles shopping",
        "Lunch" to "Déjeuner",
        "Local transport day pass" to "Pass transport local",
        "Museum ticket" to "Billet de musée",
        "SIM / data top‑up" to "Recharge SIM / internet",
        "Groceries (small)" to "Petites courses",
        "Short metro tickets" to "Tickets métro (courts)",
        "Small souvenirs" to "Petits souvenirs",
        "Snack" to "Snack",
        "Bottle of water" to "Bouteille d’eau",
        "Small ticket (sometimes)" to "Petit ticket (parfois)",
        "Small snack (sometimes)" to "Petit snack (parfois)",
        "Coffee (sometimes)" to "Café (parfois)"
    )

    private val AR: Map<String, String> = mapOf(
        // Short hints
        "≈ nice dinner for two" to "≈ عشاء جيد لشخصين",
        "≈ taxi long ride" to "≈ رحلة تاكسي طويلة",
        "≈ coffee + sandwich" to "≈ قهوة + ساندويتش",
        "Common daily use" to "استخدام يومي",
        "≈ quick coffee" to "≈ قهوة سريعة",
        "≈ bottle of water" to "≈ زجاجة ماء",
        "≈ small snack" to "≈ وجبة خفيفة صغيرة",
        "≈ small tip" to "≈ إكرامية صغيرة",
        "Small change" to "فكة بسيطة",
        "Very high value" to "قيمة عالية جداً",
        "High value" to "قيمة عالية",
        "Common for shopping" to "شائع للتسوق",
        "Everyday payments" to "مدفوعات يومية",
        "Daily use" to "استخدام يومي",
        "Small note" to "ورقة صغيرة",
        "Small purchases" to "مشتريات صغيرة",
        "Common coin" to "عملة شائعة",
        "Coins / change" to "عملات / فكة",

        // Descriptions (MAD)
        "A high-value banknote. Often used for meals, shopping, or larger payments." to "ورقة نقدية عالية القيمة تُستخدم غالباً للوجبات أو التسوق أو المدفوعات الكبيرة.",
        "Very common for daily spending and taxis. Many shops can break this note." to "شائعة جداً للمصاريف اليومية والتاكسي. كثير من المحلات تستطيع صرفها.",
        "A popular everyday note. Great for cafés, snacks, and small purchases." to "ورقة مستخدمة يومياً، مناسبة للمقاهي والوجبات الخفيفة والمشتريات الصغيرة.",
        "One of the most used notes. Handy for quick purchases and short trips." to "من أكثر الأوراق استعمالاً، مفيدة للمشتريات السريعة والرحلات القصيرة.",
        "A very common coin. Used everywhere for small items and change." to "عملة شائعة جداً تُستخدم في كل مكان للمشتريات الصغيرة والفكة.",
        "Common for small purchases and getting the right change." to "شائعة للمشتريات الصغيرة ولإعطاء الفكة الصحيحة.",
        "Useful for tiny purchases and as change." to "مفيدة للمشتريات البسيطة وكفكة.",
        "Small coin used mainly for change." to "عملة صغيرة تُستخدم غالباً للفكة.",
        "Small coin used for exact change." to "عملة صغيرة تُستخدم للفكة الدقيقة.",

        // Descriptions (EUR)
        "A very high-value note. Rare in daily use. Many places may refuse it." to "ورقة عالية جداً في القيمة ونادرة في الاستعمال اليومي. قد ترفضها بعض الأماكن.",
        "High-value note. Not always accepted in small shops." to "ورقة عالية القيمة ولا تُقبل دائماً في المحلات الصغيرة.",
        "Common for shopping and travel expenses. Some small places may prefer smaller notes." to "شائعة للتسوق ومصاريف السفر. بعض الأماكن تفضّل أوراقاً أصغر.",
        "A very common banknote for everyday spending." to "ورقة شائعة جداً للمصاريف اليومية.",
        "One of the most used notes. Great for daily expenses." to "من أكثر الأوراق استعمالاً، مناسبة للمصاريف اليومية.",
        "One of the most used notes. Great for daily expenses and shopping." to "من أكثر الأوراق استعمالاً، مناسبة للمصاريف اليومية والتسوق.",
        "Very common for quick purchases and small expenses." to "شائعة جداً للمشتريات السريعة والمصاريف الصغيرة.",
        "Great for small purchases and change." to "مناسبة للمشتريات الصغيرة والفكة.",
        "A common coin used everywhere." to "عملة شائعة تُستخدم في كل مكان.",
        "Used for exact change and small purchases." to "تُستخدم للفكة الدقيقة والمشتريات الصغيرة.",
        "Used for exact change." to "تُستخدم للفكة الدقيقة.",
        "Rare note" to "ورقة نادرة",
        "Common note" to "ورقة شائعة",
        "A rare banknote. Not commonly used in daily transactions." to "ورقة نقدية نادرة لا تُستخدم عادة في المعاملات اليومية.",
        "The most common banknote. Used everywhere for small purchases." to "أكثر الأوراق النقدية شيوعاً، تُستخدم في كل مكان للمشتريات الصغيرة.",
        "The smallest coin (penny). Used for exact change." to "أصغر عملة (سنت). تُستخدم للفكة الدقيقة.",
        "The smallest coin. Used for exact change." to "أصغر عملة. تُستخدم للفكة الدقيقة.",
        "Smallest change" to "أصغر فكة",
        "Rarely seen" to "نادراً ما تُرى",

        // Examples (generic)
        "Exact change" to "فكة دقيقة",
        "Rounding" to "تقريب",
        "Change" to "فكة",
        "Small change" to "فكة بسيطة",
        "Not always accepted" to "قد لا تُقبل دائماً",

        // Examples (MAD)
        "Dinner for two" to "عشاء لشخصين",
        "Taxi across the city" to "تاكسي عبر المدينة",
        "Souvenir bundle" to "مجموعة تذكارات",
        "Groceries + extras" to "بقالة + إضافات",
        "Hammam / spa entry" to "دخول حمام / سبا",
        "Intercity bus / shared taxi" to "حافلة بين المدن / تاكسي مشترك",
        "Museum + snacks" to "متحف + سناكات",
        "Phone data top‑up" to "تعبئة إنترنت للهاتف",
        "Small day trip transport" to "مواصلات لرحلة يومية صغيرة",
        "Restaurant + dessert" to "مطعم + حلوى",
        "Taxi ride" to "رحلة تاكسي",
        "Lunch for one" to "غداء لشخص واحد",
        "Groceries" to "بقالة",
        "Museum tickets" to "تذاكر متحف",
        "Café + snack" to "مقهى + وجبة خفيفة",
        "Train / bus ticket (short)" to "تذكرة قطار/حافلة (قصيرة)",
        "Souvenir" to "تذكار",
        "Basic essentials" to "أساسيات",
        "Small attraction entry" to "دخول مَعلَم صغير",
        "Coffee + snack" to "قهوة + وجبة خفيفة",
        "Sandwich" to "ساندويتش",
        "Fresh juice" to "عصير طازج",
        "Bakery items" to "مخبوزات",
        "Short taxi (very short)" to "تاكسي قصير جداً",
        "Small shopping" to "تسوق بسيط",
        "Local attraction" to "مَعلَم محلي",
        "SIM / data top‑up (small)" to "تعبئة SIM/إنترنت (صغيرة)",
        "Water + snack" to "ماء + سناك",
        "Street food" to "أكل الشارع",
        "Snack + drink" to "سناك + مشروب",
        "Coffee" to "قهوة",
        "Bread + essentials" to "خبز + أساسيات",
        "Small entry fee" to "رسوم دخول صغيرة",
        "Bus ticket (sometimes)" to "تذكرة حافلة (أحياناً)",
        "Tips (small)" to "إكراميات (صغيرة)",
        "Bottled water" to "ماء معبأ",
        "Bakery item" to "مخبوزات",
        "Quick takeaway" to "أكل سريع (تيك أواي)",
        "Coffee (small)" to "قهوة (صغيرة)",
        "Mint tea (sometimes)" to "شاي بالنعناع (أحياناً)",
        "Small tips" to "إكراميات صغيرة",
        "Bread" to "خبز",
        "Candy / small treat" to "حلوى / قطعة صغيرة",
        "Extra change" to "فكة إضافية",
        "Water (sometimes)" to "ماء (أحياناً)",
        "Street snack (small)" to "سناك شارع (صغير)",
        "Water bottle (small)" to "زجاجة ماء (صغيرة)",
        "Candy" to "حلوى",
        "Small snack" to "سناك صغير",
        "Extra coin for rounding" to "عملة للتقريب",
        "Small tip (sometimes)" to "إكرامية صغيرة (أحياناً)",

        // Examples (EUR)
        "Hotel deposit" to "عربون فندق",
        "Large purchase" to "شراء كبير",
        "Long-distance travel" to "سفر لمسافة طويلة",
        "Electronics (part)" to "إلكترونيات (جزء)",
        "Multi-day expenses" to "مصاريف لعدة أيام",
        "Shopping" to "تسوق",
        "Nice dinner for two" to "عشاء جميل لشخصين",
        "Hotel night (budget)" to "ليلة فندق (اقتصادي)",
        "Intercity transport" to "مواصلات بين المدن",
        "Grocery run (big)" to "تسوق بقالة كبير",
        "Restaurant for two" to "مطعم لشخصين",
        "Day pass transport" to "بطاقة مواصلات يومية",
        "Shopping essentials" to "احتياجات أساسية",
        "Restaurant" to "مطعم",
        "Local train tickets" to "تذاكر قطار محلية",
        "City activities" to "أنشطة داخل المدينة",
        "Shopping items" to "مشتريات",
        "Lunch" to "غداء",
        "Local transport day pass" to "بطاقة مواصلات محلية يومية",
        "Museum ticket" to "تذكرة متحف",
        "SIM / data top‑up" to "تعبئة SIM/إنترنت",
        "Groceries (small)" to "بقالة (صغيرة)",
        "Short metro tickets" to "تذاكر مترو قصيرة",
        "Small souvenirs" to "تذكارات صغيرة",
        "Snack" to "سناك",
        "Bottle of water" to "زجاجة ماء",
        "Small ticket (sometimes)" to "تذكرة صغيرة (أحياناً)",
        "Small snack (sometimes)" to "سناك صغير (أحياناً)",
        "Coffee (sometimes)" to "قهوة (أحياناً)"
    )

    // For ES/PT we keep a basic translation for the most visible strings; anything missing falls back to EN.
    private val ES: Map<String, String> = mapOf(
        "≈ nice dinner for two" to "≈ buena cena para dos",
        "≈ taxi long ride" to "≈ taxi (trayecto largo)",
        "≈ coffee + sandwich" to "≈ café + sándwich",
        "Common daily use" to "Uso diario",
        "≈ quick coffee" to "≈ café rápido",
        "≈ bottle of water" to "≈ botella de agua",
        "≈ small snack" to "≈ snack pequeño",
        "≈ small tip" to "≈ propina pequeña",
        "Small change" to "Cambio pequeño",
        "Very high value" to "Valor muy alto",
        "High value" to "Valor alto",
        "Common for shopping" to "Común para compras",
        "Everyday payments" to "Pagos diarios",
        "Daily use" to "Uso diario",
        "Small note" to "Billete pequeño",
        "Small purchases" to "Compras pequeñas",
        "Common coin" to "Moneda común",
        "Coins / change" to "Monedas / cambio",
        "Exact change" to "Cambio exacto",
        "Rounding" to "Redondeo",
        "Change" to "Cambio",
        "Not always accepted" to "No siempre aceptado",
        "Rare note" to "Billete raro",
        "Common note" to "Billete común",
        "A rare banknote. Not commonly used in daily transactions." to "Billete raro, poco usado en transacciones diarias.",
        "The most common banknote. Used everywhere for small purchases." to "El billete más común, usado en todas partes para compras pequeñas.",
        "The smallest coin (penny). Used for exact change." to "La moneda más pequeña (centavo). Usada para cambio exacto.",
        "The smallest coin. Used for exact change." to "La moneda más pequeña. Usada para cambio exacto.",
        "Smallest change" to "Cambio más pequeño",
        "Rarely seen" to "Raramente visto",
        "One of the most used notes. Great for daily expenses and shopping." to "Uno de los billetes más usados. Ideal para gastos diarios y compras.",
        "A very common banknote for everyday spending." to "Billete muy común para gastos diarios.",
        "Common for shopping and travel expenses. Some small places may prefer smaller notes." to "Común para compras y gastos de viaje. Algunos lugares pequeños pueden preferir billetes más pequeños.",
        "A very high-value note. Rare in daily use. Many places may refuse it." to "Billete de muy alto valor, raro en uso diario. Muchos lugares pueden rechazarlo.",
        "High-value note. Not always accepted in small shops." to "Billete de alto valor, no siempre aceptado en tiendas pequeñas.",
        "Very common for quick purchases and small expenses." to "Muy común para compras rápidas y gastos pequeños.",
        "Great for small purchases and change." to "Ideal para compras pequeñas y cambio.",
        "A common coin used everywhere." to "Moneda común usada en todas partes.",
        "Used for exact change and small purchases." to "Usada para cambio exacto y compras pequeñas.",
        "Used for exact change." to "Usada para cambio exacto.",
        "A high-value banknote. Often used for meals, shopping, or larger payments." to "Billete de alto valor, a menudo usado para comidas, compras o pagos grandes.",
        "Very common for daily spending and taxis. Many shops can break this note." to "Muy común para gastos diarios y taxis. Muchas tiendas pueden cambiar este billete.",
        "A popular everyday note. Great for cafés, snacks, and small purchases." to "Billete popular de uso diario, ideal para cafés, snacks y compras pequeñas.",
        "One of the most used notes. Handy for quick purchases and short trips." to "Uno de los billetes más usados, práctico para compras rápidas y viajes cortos.",
        "A very common coin. Used everywhere for small items and change." to "Moneda muy común, usada en todas partes para artículos pequeños y cambio.",
        "Common for small purchases and getting the right change." to "Común para compras pequeñas y obtener el cambio correcto.",
        "Useful for tiny purchases and as change." to "Útil para compras muy pequeñas y como cambio.",
        "Small coin used mainly for change." to "Moneda pequeña usada principalmente para cambio.",
        "Small coin used for exact change." to "Moneda pequeña usada para cambio exacto."
    )

    private val PT: Map<String, String> = mapOf(
        "≈ nice dinner for two" to "≈ bom jantar para dois",
        "≈ taxi long ride" to "≈ táxi (viagem longa)",
        "≈ coffee + sandwich" to "≈ café + sanduíche",
        "Common daily use" to "Uso diário",
        "≈ quick coffee" to "≈ café rápido",
        "≈ bottle of water" to "≈ garrafa de água",
        "≈ small snack" to "≈ snack pequeno",
        "≈ small tip" to "≈ gorjeta pequena",
        "Small change" to "Troco pequeno",
        "Very high value" to "Valor muito alto",
        "High value" to "Valor alto",
        "Common for shopping" to "Comum para compras",
        "Everyday payments" to "Pagamentos diários",
        "Daily use" to "Uso diário",
        "Small note" to "Nota pequena",
        "Small purchases" to "Compras pequenas",
        "Common coin" to "Moeda comum",
        "Coins / change" to "Moedas / troco",
        "Exact change" to "Troco exato",
        "Rounding" to "Arredondamento",
        "Change" to "Troco",
        "Not always accepted" to "Nem sempre aceito",
        "Rare note" to "Nota rara",
        "Common note" to "Nota comum",
        "A rare banknote. Not commonly used in daily transactions." to "Nota rara, pouco usada em transações diárias.",
        "The most common banknote. Used everywhere for small purchases." to "A nota mais comum, usada em todo o lado para compras pequenas.",
        "The smallest coin (penny). Used for exact change." to "A moeda mais pequena (cêntimo). Usada para troco exato.",
        "The smallest coin. Used for exact change." to "A moeda mais pequena. Usada para troco exato.",
        "Smallest change" to "Troco mais pequeno",
        "Rarely seen" to "Raramente visto",
        "One of the most used notes. Great for daily expenses and shopping." to "Uma das notas mais usadas. Ideal para despesas diárias e compras.",
        "A very common banknote for everyday spending." to "Nota muito comum para despesas diárias.",
        "Common for shopping and travel expenses. Some small places may prefer smaller notes." to "Comum para compras e despesas de viagem. Alguns lugares pequenos podem preferir notas menores.",
        "A very high-value note. Rare in daily use. Many places may refuse it." to "Nota de valor muito alto, rara no uso diário. Muitos lugares podem recusá-la.",
        "High-value note. Not always accepted in small shops." to "Nota de alto valor, nem sempre aceite em lojas pequenas.",
        "Very common for quick purchases and small expenses." to "Muito comum para compras rápidas e despesas pequenas.",
        "Great for small purchases and change." to "Ideal para compras pequenas e troco.",
        "A common coin used everywhere." to "Moeda comum usada em todo o lado.",
        "Used for exact change and small purchases." to "Usada para troco exato e compras pequenas.",
        "Used for exact change." to "Usada para troco exato.",
        "A high-value banknote. Often used for meals, shopping, or larger payments." to "Nota de alto valor, frequentemente usada para refeições, compras ou pagamentos maiores.",
        "Very common for daily spending and taxis. Many shops can break this note." to "Muito comum para despesas diárias e táxis. Muitas lojas podem trocar esta nota.",
        "A popular everyday note. Great for cafés, snacks, and small purchases." to "Nota popular de uso diário, ideal para cafés, snacks e compras pequenas.",
        "One of the most used notes. Handy for quick purchases and short trips." to "Uma das notas mais usadas, prática para compras rápidas e viagens curtas.",
        "A very common coin. Used everywhere for small items and change." to "Moeda muito comum, usada em todo o lado para artigos pequenos e troco.",
        "Common for small purchases and getting the right change." to "Comum para compras pequenas e obter o troco correto.",
        "Useful for tiny purchases and as change." to "Útil para compras muito pequenas e como troco.",
        "Small coin used mainly for change." to "Moeda pequena usada principalmente para troco.",
        "Small coin used for exact change." to "Moeda pequena usada para troco exato."
    )
}





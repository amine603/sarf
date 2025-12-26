package com.tajir.sarf.domain

import com.tajir.sarf.data.Denominations

/**
 * Domain layer: Change calculation logic using greedy algorithm
 * 
 * Greedy Algorithm Explanation:
 * - Start with the largest denomination
 * - Use as many of the largest denomination as possible
 * - Move to the next smaller denomination
 * - Repeat until change is fully broken down
 * 
 * Example: Change = 129 MAD
 * - 129 / 200 = 0 (can't use 200)
 * - 129 / 100 = 1 (use one 100, remainder = 29)
 * - 29 / 50 = 0 (can't use 50)
 * - 29 / 20 = 1 (use one 20, remainder = 9)
 * - 9 / 10 = 0 (can't use 10)
 * - 9 / 5 = 1 (use one 5, remainder = 4)
 * - 4 / 2 = 2 (use two 2s, remainder = 0)
 * - Result: 100×1, 20×1, 5×1, 2×2
 */
object ChangeCalculator {
    /**
     * Calculates the change breakdown using greedy algorithm
     *
     * All parameters are in **cents** (MAD * 100).
     *
     * @param priceCents The price to pay (in cents)
     * @param paidCents The amount paid by customer (in cents)
     * @param excludedDenominationsCents Denominations (in cents) that are not available. They will not be used.
     * @return ChangeResult.Success with breakdown or ChangeResult.Error
     */
    fun calculateChangeBreakdown(
        priceCents: Int,
        paidCents: Int,
        excludedDenominationsCents: Set<Int> = emptySet(),
        denominations: List<Denominations.DenominationSpec> = Denominations.ALL
    ): ChangeResult {
        if (priceCents < 0) return ChangeResult.Error("Price cannot be negative")
        if (paidCents < 0) return ChangeResult.Error("Paid amount cannot be negative")
        if (paidCents < priceCents) return ChangeResult.Error("Customer paid less than the price.")

        // Calculate change
        val changeCents = paidCents - priceCents

        if (changeCents == 0) {
            return ChangeResult.Success(
                totalChangeCents = 0,
                breakdown = emptyList()
            )
        }

        // Greedy algorithm: break down change into denominations
        val breakdown = mutableListOf<DenominationItem>()
        var remainingChange = changeCents

        // Iterate through denominations from largest to smallest
        for (spec in denominations) {
            if (spec.valueCents in excludedDenominationsCents) continue
            if (remainingChange >= spec.valueCents) {
                val count = remainingChange / spec.valueCents
                remainingChange %= spec.valueCents
                breakdown.add(
                    DenominationItem(
                        valueCents = spec.valueCents,
                        count = count,
                        assetPath = spec.assetPath,
                        displayLabel = spec.displayLabel,
                        isCoin = spec.isCoin
                    )
                )
            }
        }

        if (remainingChange != 0) {
            return ChangeResult.Error("Cannot make exact change with selected denominations.")
        }

        return ChangeResult.Success(
            totalChangeCents = changeCents,
            breakdown = breakdown
        )
    }
}


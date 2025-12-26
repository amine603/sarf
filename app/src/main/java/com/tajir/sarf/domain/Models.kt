package com.tajir.sarf.domain

/**
 * Data class representing a denomination item in the change breakdown.
 *
 * All money values are in **cents** (MAD * 100).
 */
data class DenominationItem(
    val valueCents: Int,
    val count: Int,
    val assetPath: String,       // e.g. "sarfpic/100dh.jpg"
    val displayLabel: String,    // e.g. "100 MAD" or "0.50 MAD"
    val isCoin: Boolean
)

/**
 * Sealed class representing the result of change calculation
 */
sealed class ChangeResult {
    /**
     * Success case with total change and breakdown list
     */
    data class Success(
        val totalChangeCents: Int,
        val breakdown: List<DenominationItem>
    ) : ChangeResult()

    /**
     * Error case with error message
     */
    data class Error(val message: String) : ChangeResult()
}


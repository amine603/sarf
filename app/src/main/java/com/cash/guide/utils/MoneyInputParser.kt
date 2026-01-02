package com.cash.guide.utils

/**
 * Parses user-entered money strings into cents (MAD * 100).
 *
 * Supports:
 * - "71"
 * - "71.5"  -> 71.50
 * - "71,5"  -> 71.50
 * - "71.05" -> 71.05
 */
object MoneyInputParser {
    private val allowedPattern = Regex("""^\s*\d+(?:[.,]\d{0,2})?\s*$""")

    fun parseToCents(input: String): Result<Int> {
        val raw = input.trim()
        if (raw.isEmpty()) return Result.failure(IllegalArgumentException("Amount cannot be empty"))
        if (!allowedPattern.matches(raw)) return Result.failure(IllegalArgumentException("Amount must be a valid number"))

        val normalized = raw.replace(',', '.')
        val parts = normalized.split('.', limit = 2)

        val whole = parts[0].toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Amount must be a valid number"))

        val fracStr = parts.getOrNull(1).orEmpty()
        val cents = when (fracStr.length) {
            0 -> 0
            1 -> (fracStr[0].digitToInt()) * 10
            2 -> (fracStr[0].digitToInt() * 10) + fracStr[1].digitToInt()
            else -> return Result.failure(IllegalArgumentException("Amount must have at most 2 decimals"))
        }

        if (whole < 0) return Result.failure(IllegalArgumentException("Amount cannot be negative"))

        return runCatching {
            Math.addExact(Math.multiplyExact(whole, 100), cents)
        }
    }

    /**
     * Filters a typed string to a safe money format:
     * digits + optional single '.' or ',' + up to 2 fractional digits.
     */
    fun sanitizeForTyping(input: String): String {
        val trimmed = input.trim()
        val sb = StringBuilder()
        var seenSep = false
        var fracCount = 0

        for (ch in trimmed) {
            when {
                ch.isDigit() -> {
                    if (!seenSep) {
                        sb.append(ch)
                    } else if (fracCount < 2) {
                        sb.append(ch)
                        fracCount++
                    }
                }
                (ch == '.' || ch == ',') && !seenSep -> {
                    // allow separator only if we already have some digits
                    if (sb.isNotEmpty()) {
                        sb.append(ch)
                        seenSep = true
                    }
                }
            }
        }

        return sb.toString()
    }

    /**
     * Formats cents (MAD * 100) into a plain input string like "71.50" (no currency suffix).
     */
    fun formatCentsForInput(cents: Int): String {
        val abs = kotlin.math.abs(cents)
        val mad = abs / 100
        val cent = abs % 100
        val sign = if (cents < 0) "-" else ""
        return "$sign$mad.${cent.toString().padStart(2, '0')}"
    }

    /**
     * Like [formatCentsForInput] but returns "77" instead of "77.00" when cents are 0.
     */
    fun formatCentsForInputCompact(cents: Int): String {
        val abs = kotlin.math.abs(cents)
        val mad = abs / 100
        val cent = abs % 100
        val sign = if (cents < 0) "-" else ""
        return if (cent == 0) "$sign$mad" else "$sign$mad.${cent.toString().padStart(2, '0')}"
    }
}



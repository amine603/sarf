package com.cash.guide.utils

/**
 * Utility class for validating user inputs
 */
object InputValidator {
    /**
     * Validates price input
     * @return null if valid, error message if invalid
     */
    fun validatePrice(price: String): String? {
        if (price.isBlank()) {
            return "Price cannot be empty"
        }
        val priceInt = price.toIntOrNull()
        return when {
            priceInt == null -> "Price must be a valid number"
            priceInt < 0 -> "Price cannot be negative"
            else -> null
        }
    }

    /**
     * Validates customer paid input
     * @return null if valid, error message if invalid
     */
    fun validatePaid(paid: String): String? {
        if (paid.isBlank()) {
            return "Paid amount cannot be empty"
        }
        val paidInt = paid.toIntOrNull()
        return when {
            paidInt == null -> "Paid amount must be a valid number"
            paidInt < 0 -> "Paid amount cannot be negative"
            else -> null
        }
    }

    /**
     * Validates that paid amount is sufficient
     * @return null if valid, error message if invalid
     */
    fun validateSufficientPayment(price: Int, paid: Int): String? {
        return if (paid < price) {
            "Customer paid less than the price."
        } else {
            null
        }
    }
}


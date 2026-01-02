package com.cash.guide.utils

/**
 * Force Latin digits for display.
 *
 * Arabic locales can format numbers/dates using Arabic-Indic digits (٠١٢٣٤٥٦٧٨٩) or
 * Eastern Arabic-Indic digits (۰۱۲۳۴۵۶۷۸۹). The app prefers Latin digits everywhere.
 */
fun String.toLatinDigits(): String {
    if (this.isEmpty()) return this
    val sb = StringBuilder(this.length)
    for (ch in this) {
        val mapped = when (ch) {
            // Arabic-Indic digits
            '٠' -> '0'
            '١' -> '1'
            '٢' -> '2'
            '٣' -> '3'
            '٤' -> '4'
            '٥' -> '5'
            '٦' -> '6'
            '٧' -> '7'
            '٨' -> '8'
            '٩' -> '9'

            // Eastern Arabic-Indic digits
            '۰' -> '0'
            '۱' -> '1'
            '۲' -> '2'
            '۳' -> '3'
            '۴' -> '4'
            '۵' -> '5'
            '۶' -> '6'
            '۷' -> '7'
            '۸' -> '8'
            '۹' -> '9'

            else -> ch
        }
        sb.append(mapped)
    }
    return sb.toString()
}



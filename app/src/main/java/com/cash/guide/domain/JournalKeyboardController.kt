package com.cash.guide.domain

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

enum class JournalKeyboardMode {
    NONE,
    TEXT,
    NUMBER
}

enum class JournalShiftMode {
    OFF,
    ONE_SHOT,
    CAPS_LOCK
}

object JournalKeyboardController {

    private val accentMap = mapOf(
        'e' to listOf("é", "è", "ê", "ë"),
        'E' to listOf("É", "È", "Ê", "Ë"),
        'a' to listOf("à", "â", "ä"),
        'A' to listOf("À", "Â", "Ä"),
        'c' to listOf("ç"),
        'C' to listOf("Ç"),
        'u' to listOf("ù", "û", "ü"),
        'U' to listOf("Ù", "Û", "Ü"),
        'i' to listOf("î", "ï"),
        'I' to listOf("Î", "Ï"),
        'o' to listOf("ô", "ö"),
        'O' to listOf("Ô", "Ö")
    )

    fun getAccents(char: Char): List<String> {
        return accentMap[char] ?: emptyList()
    }

    /**
     * Inserts text at the current selection/cursor position and advances the cursor.
     * Replaces any highlighted selection range.
     */
    fun insertText(current: TextFieldValue, textToInsert: String): TextFieldValue {
        val selStart = current.selection.min
        val selEnd = current.selection.max
        val currentText = current.text

        val newText = if (selStart != selEnd) {
            currentText.replaceRange(selStart, selEnd, textToInsert)
        } else {
            val validStart = selStart.coerceIn(0, currentText.length)
            currentText.substring(0, validStart) + textToInsert + currentText.substring(validStart)
        }

        val newCursor = (selStart + textToInsert.length).coerceIn(0, newText.length)
        return TextFieldValue(text = newText, selection = TextRange(newCursor))
    }

    /**
     * Deletes the selection or the character/code point immediately preceding the cursor.
     * Correctly handles Unicode surrogate pairs (e.g. accented characters or emojis).
     */
    fun deleteBackward(current: TextFieldValue): TextFieldValue {
        val selStart = current.selection.min
        val selEnd = current.selection.max
        val currentText = current.text

        if (selStart != selEnd) {
            val newText = currentText.removeRange(selStart, selEnd)
            val newCursor = selStart.coerceIn(0, newText.length)
            return TextFieldValue(text = newText, selection = TextRange(newCursor))
        }

        if (selStart <= 0) {
            return current
        }

        // Account for surrogate pairs when deleting
        val deleteCount = if (selStart > 1 && Character.isSurrogatePair(currentText[selStart - 2], currentText[selStart - 1])) {
            2
        } else {
            1
        }

        val deleteFrom = (selStart - deleteCount).coerceAtLeast(0)
        val newText = currentText.removeRange(deleteFrom, selStart)
        val newCursor = deleteFrom.coerceIn(0, newText.length)
        return TextFieldValue(text = newText, selection = TextRange(newCursor))
    }

    /**
     * Applies a key press to numeric amount input:
     * - Restricts to digits 0-9 and at most one decimal dot.
     * - Limits length to 18 digits.
     */
    fun applyNumericKey(current: TextFieldValue, key: String): TextFieldValue {
        val selStart = current.selection.min
        val selEnd = current.selection.max
        val currentText = current.text

        return when (key) {
            "⌫" -> deleteBackward(current)
            "." -> {
                val textWithoutSelection = if (selStart != selEnd) {
                    currentText.removeRange(selStart, selEnd)
                } else {
                    currentText
                }
                if (!textWithoutSelection.contains('.')) {
                    insertText(current, ".")
                } else {
                    current
                }
            }
            else -> {
                // Digits 0-9
                if (key.all { it.isDigit() } && currentText.length < 18) {
                    insertText(current, key)
                } else {
                    current
                }
            }
        }
    }
}

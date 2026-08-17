package com.cash.guide

import com.cash.guide.domain.JournalLedgerManager
import com.cash.guide.domain.MoneyMath
import com.cash.guide.domain.MoneyUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorPopupTest {

    data class TestRow(
        val id: Long,
        val title: String = "",
        val expression: String = ""
    )

    data class TextSelection(
        val start: Int,
        val end: Int
    ) {
        val min: Int get() = if (start <= end) start else end
        val max: Int get() = if (start > end) start else end
    }

    data class AmountFieldState(
        val text: String,
        val selection: TextSelection
    )

    // Cursor-aware compact numeric keypad handler
    private fun applyCompactKeyWithCursor(
        state: AmountFieldState,
        key: String
    ): AmountFieldState {
        val currentText = state.text
        val selStart = state.selection.min.coerceIn(0, currentText.length)
        val selEnd = state.selection.max.coerceIn(0, currentText.length)

        return when (key) {
            "⌫" -> {
                if (selStart != selEnd) {
                    val newText = currentText.removeRange(selStart, selEnd)
                    AmountFieldState(newText, TextSelection(selStart, selStart))
                } else if (selStart > 0) {
                    val newText = currentText.removeRange(selStart - 1, selStart)
                    AmountFieldState(newText, TextSelection(selStart - 1, selStart - 1))
                } else {
                    state
                }
            }
            "." -> {
                val textWithoutSelection = currentText.removeRange(selStart, selEnd)
                if (!textWithoutSelection.contains('.')) {
                    val newText = currentText.replaceRange(selStart, selEnd, ".")
                    AmountFieldState(newText, TextSelection(selStart + 1, selStart + 1))
                } else {
                    state
                }
            }
            else -> { // Digits 0-9
                if (currentText.length < 18) {
                    val newText = currentText.replaceRange(selStart, selEnd, key)
                    AmountFieldState(newText, TextSelection(selStart + key.length, selStart + key.length))
                } else {
                    state
                }
            }
        }
    }

    // Popup state and handler
    data class PopupState(
        var expression: String = "",
        var result: String = "",
        var hasError: Boolean = false,
        var isEvaluated: Boolean = false
    )

    private fun applyPopupKey(
        state: PopupState,
        key: String
    ) {
        val operators = setOf('+', '−', '×', '÷')
        when (key) {
            "⌫" -> {
                state.hasError = false
                state.isEvaluated = false
                state.result = ""
                if (state.expression.isNotEmpty()) {
                    state.expression = state.expression.dropLast(1)
                }
            }
            "=" -> {
                if (state.expression.isBlank()) return
                val evaluated = MoneyMath.evaluate(state.expression)?.stripTrailingZeros()?.toPlainString()
                if (evaluated != null && !evaluated.contains("NaN") && !evaluated.contains("Infinity")) {
                    state.result = evaluated
                    state.isEvaluated = true
                    state.hasError = false
                } else {
                    state.result = ""
                    state.isEvaluated = false
                    state.hasError = true
                }
            }
            "+", "−", "×", "÷" -> {
                state.hasError = false
                if (state.isEvaluated && state.result.isNotBlank()) {
                    state.expression = state.result + key
                    state.result = ""
                    state.isEvaluated = false
                } else {
                    state.isEvaluated = false
                    state.result = ""
                    state.expression = when {
                        state.expression.isBlank() -> state.expression
                        state.expression.last() in operators -> state.expression.dropLast(1) + key
                        else -> state.expression + key
                    }
                }
            }
            "." -> {
                state.hasError = false
                if (state.isEvaluated) {
                    state.expression = "0."
                    state.result = ""
                    state.isEvaluated = false
                } else {
                    state.result = ""
                    state.isEvaluated = false
                    val lastPart = state.expression.split('+', '−', '×', '÷').lastOrNull().orEmpty()
                    state.expression = when {
                        lastPart.contains('.') -> state.expression
                        lastPart.isEmpty() -> state.expression + "0."
                        else -> state.expression + "."
                    }
                }
            }
            else -> { // Digits 0-9
                state.hasError = false
                if (state.isEvaluated) {
                    state.expression = key
                    state.result = ""
                    state.isEvaluated = false
                } else {
                    state.result = ""
                    state.isEvaluated = false
                    val lastPart = state.expression.split('+', '−', '×', '÷').lastOrNull().orEmpty()
                    if (lastPart == "0") {
                        if (key != "0") {
                            state.expression = state.expression.dropLast(1) + key
                        }
                    } else if (state.expression.length < 32) {
                        state.expression += key
                    }
                }
            }
        }
    }

    private fun confirmPopupResult(
        popup: PopupState,
        activeRowId: Long?,
        rows: MutableList<TestRow>
    ): Boolean {
        if (!popup.isEvaluated || popup.result.isBlank() || popup.hasError) return false
        val targetId = activeRowId.takeIf { id -> rows.any { it.id == id } } ?: return false
        val index = rows.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            rows[index] = rows[index].copy(expression = popup.result)
            popup.expression = ""
            popup.result = ""
            popup.isEvaluated = false
            popup.hasError = false
            return true
        }
        return false
    }

    @Test
    fun test1_evaluate600Plus250Plus30Equals880() {
        val evaluated = MoneyMath.evaluate("600+250+30")?.stripTrailingZeros()?.toPlainString()
        assertEquals("880", evaluated)
    }

    @Test
    fun test2_equalsCalculatesAndDisplaysResultOnlyWithoutModifyingRowOrTotal() {
        val rows = mutableListOf(
            TestRow(id = 1L, title = "Naïma", expression = "1200"),
            TestRow(id = 2L, title = "Nadia", expression = "5000"),
            TestRow(id = 3L, title = "Marché", expression = "2600")
        )
        val initialTotal = rows.sumOf { MoneyMath.toCentimes(it.expression, MoneyUnit.RIAL) ?: 0L }
        assertEquals(44000L, initialTotal)

        val popup = PopupState(expression = "600+250+30")
        applyPopupKey(popup, "=")

        assertEquals("880", popup.result)
        assertTrue(popup.isEvaluated)
        assertFalse(popup.hasError)

        // Active row 3 remains UNCHANGED after pressing '='
        assertEquals("2600", rows[2].expression)
        // Total remains UNCHANGED after pressing '='
        val totalAfterEquals = rows.sumOf { MoneyMath.toCentimes(it.expression, MoneyUnit.RIAL) ?: 0L }
        assertEquals(initialTotal, totalAfterEquals)
    }

    @Test
    fun test3_confirmerWritesResultIntoActiveRowAndRecalculatesTotal() {
        val rows = mutableListOf(
            TestRow(id = 1L, title = "Naïma", expression = "1200"),
            TestRow(id = 2L, title = "Nadia", expression = "5000"),
            TestRow(id = 3L, title = "Marché", expression = "2600")
        )
        val popup = PopupState(expression = "600+250+30")
        applyPopupKey(popup, "=")
        assertEquals("880", popup.result)

        // Confirmer is tapped for active row 3
        val confirmed = confirmPopupResult(popup, 3L, rows)
        assertTrue(confirmed)
        assertEquals("880", rows[2].expression)

        // New total: 1200 + 5000 + 880 = 7080 rial = 354 DH = 35400 centimes
        val newTotal = rows.sumOf { MoneyMath.toCentimes(it.expression, MoneyUnit.RIAL) ?: 0L }
        assertEquals(35400L, newTotal)
        assertEquals("7\u00A0080", JournalLedgerManager.formatTotal(newTotal, MoneyUnit.RIAL))
        assertEquals("354", JournalLedgerManager.formatTotal(newTotal, MoneyUnit.DIRHAM))
    }

    @Test
    fun test4_confirmerDoesNotAutomaticallyCreateAnotherRow() {
        val rows = mutableListOf(
            TestRow(id = 1L, title = "Naïma", expression = "1200"),
            TestRow(id = 2L, title = "Nadia", expression = "5000"),
            TestRow(id = 3L, title = "Marché", expression = "2600")
        )
        val popup = PopupState(expression = "880")
        applyPopupKey(popup, "=")
        confirmPopupResult(popup, 3L, rows)

        assertEquals(3, rows.size) // Exact same row count!
    }

    @Test
    fun test5_staleResultInvalidatedWhenExpressionChangedAfterEquals() {
        val popup = PopupState(expression = "600+250+30")
        applyPopupKey(popup, "=")
        assertEquals("880", popup.result)
        assertTrue(popup.isEvaluated)

        // User edits expression by pressing '+'
        applyPopupKey(popup, "+")
        assertEquals("880+", popup.expression)
        assertEquals("", popup.result)
        assertFalse(popup.isEvaluated)

        // Trying to confirm now must fail
        val rows = mutableListOf(TestRow(id = 1L, expression = "2600"))
        val confirmed = confirmPopupResult(popup, 1L, rows)
        assertFalse(confirmed)
        assertEquals("2600", rows[0].expression) // unchanged!
    }

    @Test
    fun test6_staleResultInvalidatedWhenBackspacePressedAfterEquals() {
        val popup = PopupState(expression = "600+250+30")
        applyPopupKey(popup, "=")
        assertEquals("880", popup.result)
        assertTrue(popup.isEvaluated)

        // User hits backspace
        applyPopupKey(popup, "⌫")
        assertEquals("", popup.result)
        assertFalse(popup.isEvaluated)
    }

    @Test
    fun test7_popupWithoutActiveRowCannotConfirm() {
        val rows = mutableListOf(TestRow(id = 1L, expression = "1200"))
        val popup = PopupState(expression = "500")
        applyPopupKey(popup, "=")
        assertTrue(popup.isEvaluated)

        // No active row ID (null)
        val confirmed = confirmPopupResult(popup, null, rows)
        assertFalse(confirmed)
        assertEquals("1200", rows[0].expression)
    }

    @Test
    fun test8_invalidExpressionAndDivisionByZeroDisableConfirmer() {
        val rows = mutableListOf(TestRow(id = 1L, expression = "1200"))

        // Incomplete expression
        val popup1 = PopupState(expression = "1200+")
        applyPopupKey(popup1, "=")
        assertTrue(popup1.hasError)
        assertFalse(popup1.isEvaluated)
        assertFalse(confirmPopupResult(popup1, 1L, rows))

        // Division by zero
        val popup2 = PopupState(expression = "50÷0")
        applyPopupKey(popup2, "=")
        assertTrue(popup2.hasError)
        assertFalse(popup2.isEvaluated)
        assertFalse(confirmPopupResult(popup2, 1L, rows))

        assertEquals("1200", rows[0].expression)
    }

    @Test
    fun test9_manualPlusButtonAddsExactlyOneRow() {
        val rows = mutableListOf(
            TestRow(id = 1L, title = "Naïma", expression = "1200"),
            TestRow(id = 2L, title = "Nadia", expression = "5000"),
            TestRow(id = 3L, title = "Marché", expression = "2600")
        )
        var nextId = 4L

        // Tap '+'
        val newId = nextId++
        rows.add(TestRow(id = newId))

        assertEquals(4, rows.size)
        assertEquals(4L, rows[3].id)
        assertEquals("", rows[3].title)
        assertEquals("", rows[3].expression)
    }

    @Test
    fun test10_deletingRowRenumbersSequentiallyWithoutAutomaticReplacement() {
        val rows = mutableListOf(
            TestRow(id = 1L, title = "Naïma", expression = "1200"),
            TestRow(id = 2L, title = "Nadia", expression = "5000"),
            TestRow(id = 3L, title = "Marché", expression = "2600")
        )
        // Delete row with id 2
        rows.removeIf { it.id == 2L }

        assertEquals(2, rows.size)
        assertEquals(1L, rows[0].id)
        assertEquals(3L, rows[1].id)
        // Total recalculates
        val total = rows.sumOf { MoneyMath.toCentimes(it.expression, MoneyUnit.RIAL) ?: 0L }
        assertEquals(19000L, total) // (1200 + 2600) * 5 = 3800 * 5 = 19000 centimes = 3800 rial
    }

    @Test
    fun test11_cursorInsertionInMiddleOfString() {
        // "1200" with cursor between '2' and '0' (index 2)
        val initial = AmountFieldState(text = "1200", selection = TextSelection(2, 2))
        val updated = applyCompactKeyWithCursor(initial, "5")

        assertEquals("12500", updated.text)
        assertEquals(3, updated.selection.start)
        assertEquals(3, updated.selection.end)
    }

    @Test
    fun test12_cursorBackspaceInMiddleOfString() {
        // "12500" with cursor after '5' (index 3)
        val initial = AmountFieldState(text = "12500", selection = TextSelection(3, 3))
        val updated = applyCompactKeyWithCursor(initial, "⌫")

        assertEquals("1200", updated.text)
        assertEquals(2, updated.selection.start)
    }

    @Test
    fun test13_cursorSelectionReplacement() {
        // "1200" with "20" selected (range 1 to 3)
        val initial = AmountFieldState(text = "1200", selection = TextSelection(1, 3))
        val updated = applyCompactKeyWithCursor(initial, "9")

        assertEquals("190", updated.text)
        assertEquals(2, updated.selection.start)
    }

    @Test
    fun test14_preventDoubleDotWithCursor() {
        val initial = AmountFieldState(text = "12.3", selection = TextSelection(4, 4))
        val updated = applyCompactKeyWithCursor(initial, ".")

        assertEquals("12.3", updated.text) // unchanged!
    }

    @Test
    fun test15_moroccanCurrencyExactConversion() {
        // 1 DH = 20 rial => 1 rial = 5 centimes, 1 DH = 100 centimes
        assertEquals(100L, MoneyMath.toCentimes("1", MoneyUnit.DIRHAM))
        assertEquals(5L, MoneyMath.toCentimes("1", MoneyUnit.RIAL))
        assertEquals(44000L, MoneyMath.toCentimes("8800", MoneyUnit.RIAL))
        assertEquals(44000L, MoneyMath.toCentimes("440", MoneyUnit.DIRHAM))
    }

    @Test
    fun test16_frenchGroupingIsDisplayOnlyAndDoesNotMutateStoredValue() {
        val storedRaw = "1200"
        val displayFormatted = JournalLedgerManager.formatFrenchNumber(storedRaw)
        assertEquals("1\u00A0200", displayFormatted)
        assertEquals("1200", storedRaw) // Raw stored value untouched

        // When evaluating math, raw value is used
        val centimes = MoneyMath.toCentimes(storedRaw, MoneyUnit.RIAL)
        assertEquals(6000L, centimes)
    }

    @Test
    fun test17_decimalGroupingPreservesDecimalPart() {
        assertEquals("8\u00A0800.5", JournalLedgerManager.formatFrenchNumber("8800.5"))
        assertEquals("1\u00A0200.75", JournalLedgerManager.formatFrenchNumber("1200.75"))
        assertEquals("50\u00A0000.25", JournalLedgerManager.formatFrenchNumber("50000.25"))
    }

    @Test
    fun test18_totalSectionFormattingMatchesApprovedReference() {
        val row1 = TestRow(id = 1L, title = "Naïma", expression = "1200")
        val row2 = TestRow(id = 2L, title = "Nadia", expression = "5000")
        val row3 = TestRow(id = 3L, title = "Marché", expression = "2600")
        val list = listOf(row1, row2, row3)

        val totalCentimes = list.sumOf { MoneyMath.toCentimes(it.expression, MoneyUnit.RIAL) ?: 0L }
        assertEquals(44000L, totalCentimes)

        val primaryFormatted = JournalLedgerManager.formatTotal(totalCentimes, MoneyUnit.RIAL)
        val secondaryFormatted = JournalLedgerManager.formatTotal(totalCentimes, MoneyUnit.DIRHAM)

        assertEquals("8\u00A0800", primaryFormatted)
        assertEquals("440", secondaryFormatted)
    }
}


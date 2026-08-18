package com.cash.guide

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.cash.guide.domain.JournalKeyboardController
import com.cash.guide.domain.JournalKeyboardMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalKeyboardTest {

    @Test
    fun `insertText inserts single character at cursor position`() {
        val initial = TextFieldValue("Nadia", TextRange(5))
        val result = JournalKeyboardController.insertText(initial, " ")
        assertEquals("Nadia ", result.text)
        assertEquals(TextRange(6), result.selection)

        val result2 = JournalKeyboardController.insertText(result, "K")
        assertEquals("Nadia K", result2.text)
        assertEquals(TextRange(7), result2.selection)
    }

    @Test
    fun `insertText replaces selected range and positions cursor at end of insertion`() {
        // Highlight "dia" in "Nadia" -> [2, 5]
        val initial = TextFieldValue("Nadia", TextRange(2, 5))
        val result = JournalKeyboardController.insertText(initial, "ïma")
        assertEquals("Naïma", result.text)
        assertEquals(TextRange(5), result.selection)
    }

    @Test
    fun `deleteBackward removes single character before cursor`() {
        val initial = TextFieldValue("Marché", TextRange(6))
        val result = JournalKeyboardController.deleteBackward(initial)
        assertEquals("March", result.text)
        assertEquals(TextRange(5), result.selection)
    }

    @Test
    fun `deleteBackward deletes entire selection when range is active`() {
        val initial = TextFieldValue("Marché aux fleurs", TextRange(6, 17))
        val result = JournalKeyboardController.deleteBackward(initial)
        assertEquals("Marché", result.text)
        assertEquals(TextRange(6), result.selection)
    }

    @Test
    fun `deleteBackward at start of text does nothing`() {
        val initial = TextFieldValue("Naïma", TextRange(0))
        val result = JournalKeyboardController.deleteBackward(initial)
        assertEquals("Naïma", result.text)
        assertEquals(TextRange(0), result.selection)
    }

    @Test
    fun `accent mappings return correct French accented letters`() {
        val eLower = JournalKeyboardController.getAccents('e')
        assertEquals(listOf("é", "è", "ê", "ë"), eLower)

        val eUpper = JournalKeyboardController.getAccents('E')
        assertEquals(listOf("É", "È", "Ê", "Ë"), eUpper)

        val aLower = JournalKeyboardController.getAccents('a')
        assertEquals(listOf("à", "â", "ä"), aLower)

        val cLower = JournalKeyboardController.getAccents('c')
        assertEquals(listOf("ç"), cLower)

        val uLower = JournalKeyboardController.getAccents('u')
        assertEquals(listOf("ù", "û", "ü"), uLower)

        val iLower = JournalKeyboardController.getAccents('i')
        assertEquals(listOf("î", "ï"), iLower)

        val oLower = JournalKeyboardController.getAccents('o')
        assertEquals(listOf("ô", "ö"), oLower)
    }

    @Test
    fun `numeric keypad limits to single decimal dot and accepts digits`() {
        var value = TextFieldValue("", TextRange(0))
        value = JournalKeyboardController.applyNumericKey(value, "2")
        value = JournalKeyboardController.applyNumericKey(value, "6")
        value = JournalKeyboardController.applyNumericKey(value, "0")
        value = JournalKeyboardController.applyNumericKey(value, "0")
        assertEquals("2600", value.text)

        value = JournalKeyboardController.applyNumericKey(value, ".")
        assertEquals("2600.", value.text)

        value = JournalKeyboardController.applyNumericKey(value, "5")
        assertEquals("2600.5", value.text)

        // Attempt second dot -> should be rejected
        val beforeSecondDot = value
        value = JournalKeyboardController.applyNumericKey(value, ".")
        assertEquals("2600.5", value.text)
        assertEquals(beforeSecondDot, value)
    }

    @Test
    fun `numeric keypad backspace deletes digit`() {
        val value = TextFieldValue("2600", TextRange(4))
        val afterBksp = JournalKeyboardController.applyNumericKey(value, "⌫")
        assertEquals("260", afterBksp.text)
        assertEquals(TextRange(3), afterBksp.selection)
    }

    @Test
    fun `numeric keypad ignores non-digit inputs`() {
        val value = TextFieldValue("5000", TextRange(4))
        val result = JournalKeyboardController.applyNumericKey(value, "abc")
        assertEquals("5000", result.text)
    }

    data class KeyboardSessionState(
        var activeRowId: Long? = null,
        var activeField: String = "NONE",
        var keyboardMode: JournalKeyboardMode = JournalKeyboardMode.NONE,
        var keyboardExpanded: Boolean = false,
        var isShiftActive: Boolean = false
    ) {
        fun onTitleFocused(rowId: Long, currentTitle: String) {
            activeRowId = rowId
            activeField = "TITLE"
            keyboardMode = JournalKeyboardMode.TEXT
            keyboardExpanded = true
            isShiftActive = currentTitle.isEmpty()
        }

        fun onAmountFocused(rowId: Long) {
            activeRowId = rowId
            activeField = "AMOUNT"
            keyboardMode = JournalKeyboardMode.NUMBER
            keyboardExpanded = true
        }

        fun switchToTextMode(currentTitle: String) {
            activeField = "TITLE"
            keyboardMode = JournalKeyboardMode.TEXT
            keyboardExpanded = true
            if (currentTitle.isEmpty()) {
                isShiftActive = true
            }
        }

        fun switchToNumericMode() {
            activeField = "AMOUNT"
            keyboardMode = JournalKeyboardMode.NUMBER
            keyboardExpanded = true
        }

        fun onConfirm() {
            activeRowId = null
            activeField = "NONE"
            keyboardMode = JournalKeyboardMode.NONE
            keyboardExpanded = false
            isShiftActive = false
        }

        fun onBackPress(): String {
            return if (keyboardExpanded) {
                keyboardExpanded = false
                "COLLAPSED_KEYBOARD"
            } else {
                "NAVIGATE_OR_RESET"
            }
        }
    }

    @Test
    fun `tapping title opens AZERTY text keyboard with one-shot shift for empty row`() {
        val session = KeyboardSessionState()
        session.onTitleFocused(1L, "")
        assertEquals(1L, session.activeRowId)
        assertEquals("TITLE", session.activeField)
        assertEquals(JournalKeyboardMode.TEXT, session.keyboardMode)
        assertTrue(session.keyboardExpanded)
        assertTrue(session.isShiftActive)
    }

    @Test
    fun `tapping title for populated row keeps shift off`() {
        val session = KeyboardSessionState()
        session.onTitleFocused(2L, "Nadia")
        assertEquals(2L, session.activeRowId)
        assertEquals("TITLE", session.activeField)
        assertEquals(JournalKeyboardMode.TEXT, session.keyboardMode)
        assertTrue(session.keyboardExpanded)
        assertFalse(session.isShiftActive)
    }

    @Test
    fun `mode switch 123 transitions from text to numeric keyboard`() {
        val session = KeyboardSessionState()
        session.onTitleFocused(3L, "Marché")
        assertEquals(JournalKeyboardMode.TEXT, session.keyboardMode)

        session.switchToNumericMode()
        assertEquals(3L, session.activeRowId)
        assertEquals("AMOUNT", session.activeField)
        assertEquals(JournalKeyboardMode.NUMBER, session.keyboardMode)
        assertTrue(session.keyboardExpanded)
    }

    @Test
    fun `mode switch ABC transitions from numeric to text keyboard`() {
        val session = KeyboardSessionState()
        session.onAmountFocused(3L)
        assertEquals(JournalKeyboardMode.NUMBER, session.keyboardMode)

        session.switchToTextMode("Marché")
        assertEquals(3L, session.activeRowId)
        assertEquals("TITLE", session.activeField)
        assertEquals(JournalKeyboardMode.TEXT, session.keyboardMode)
        assertTrue(session.keyboardExpanded)
        assertFalse(session.isShiftActive)
    }

    @Test
    fun `confirming row edit collapses keyboard and clears active field`() {
        val session = KeyboardSessionState(
            activeRowId = 1L,
            activeField = "TITLE",
            keyboardMode = JournalKeyboardMode.TEXT,
            keyboardExpanded = true
        )
        session.onConfirm()
        org.junit.Assert.assertNull(session.activeRowId)
        assertEquals("NONE", session.activeField)
        assertEquals(JournalKeyboardMode.NONE, session.keyboardMode)
        assertFalse(session.keyboardExpanded)
    }

    @Test
    fun `back press collapses keyboard first before leaving screen`() {
        val session = KeyboardSessionState(
            activeRowId = 1L,
            activeField = "AMOUNT",
            keyboardMode = JournalKeyboardMode.NUMBER,
            keyboardExpanded = true
        )
        val firstBackAction = session.onBackPress()
        assertEquals("COLLAPSED_KEYBOARD", firstBackAction)
        assertFalse(session.keyboardExpanded)

        val secondBackAction = session.onBackPress()
        assertEquals("NAVIGATE_OR_RESET", secondBackAction)
    }
}

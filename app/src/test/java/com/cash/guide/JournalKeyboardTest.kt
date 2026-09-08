package com.cash.guide

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.cash.guide.domain.BackspaceRepeatController
import com.cash.guide.domain.JournalKeyboardController
import com.cash.guide.domain.JournalKeyboardLanguage
import com.cash.guide.domain.JournalKeyboardMode
import com.cash.guide.domain.JournalShiftMode
import com.cash.guide.domain.JournalShiftState
import com.cash.guide.domain.JvmGraphemeSegmenter
import com.cash.guide.domain.ShiftAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalKeyboardTest {

    private val segmenter = JvmGraphemeSegmenter()

    // ==========================================
    // 1. Shift & Caps Lock State Machine Reducer Tests
    // ==========================================

    @Test
    fun testAutoSetOneShot_doesNotSetLastPhysicalTapTimestamp() {
        val initial = JournalShiftState(mode = JournalShiftMode.OFF)
        val state = JournalKeyboardController.reduceShift(
            state = initial,
            action = ShiftAction.AutoSetOneShot,
            monotonicNow = { 1000L }
        )
        assertEquals(JournalShiftMode.ONE_SHOT, state.mode)
        assertTrue(state.isAutoOneShot)
        assertEquals(0L, state.lastPhysicalTapTimestamp)
    }

    @Test
    fun testFirstPhysicalTapOnAutoOneShot_switchesToOffAndRecordsTap() {
        val autoOneShot = JournalShiftState(
            mode = JournalShiftMode.ONE_SHOT,
            lastPhysicalTapTimestamp = 0L,
            isAutoOneShot = true
        )
        val state = JournalKeyboardController.reduceShift(
            state = autoOneShot,
            action = ShiftAction.UserTapShift(1000L),
            monotonicNow = { 1000L }
        )
        assertEquals(JournalShiftMode.OFF, state.mode)
        assertFalse(state.isAutoOneShot)
        assertEquals(1000L, state.lastPhysicalTapTimestamp)
    }

    @Test
    fun testDoubleTapFromOff_enablesCapsLock() {
        var state = JournalShiftState(mode = JournalShiftMode.OFF)

        // First physical tap at t = 1000ms
        state = JournalKeyboardController.reduceShift(
            state = state,
            action = ShiftAction.UserTapShift(1000L),
            monotonicNow = { 1000L }
        )
        assertEquals(JournalShiftMode.ONE_SHOT, state.mode)
        assertEquals(1000L, state.lastPhysicalTapTimestamp)

        // Second physical tap at t = 1200ms (within 300ms window)
        state = JournalKeyboardController.reduceShift(
            state = state,
            action = ShiftAction.UserTapShift(1200L),
            monotonicNow = { 1200L }
        )
        assertEquals(JournalShiftMode.CAPS_LOCK, state.mode)
    }

    @Test
    fun testTapAfterDoubleTapTimeout_revertsToOff() {
        var state = JournalShiftState(mode = JournalShiftMode.OFF)

        // First physical tap at t = 1000ms
        state = JournalKeyboardController.reduceShift(
            state = state,
            action = ShiftAction.UserTapShift(1000L),
            monotonicNow = { 1000L }
        )
        assertEquals(JournalShiftMode.ONE_SHOT, state.mode)

        // Second physical tap at t = 1450ms (> 300ms elapsed)
        state = JournalKeyboardController.reduceShift(
            state = state,
            action = ShiftAction.UserTapShift(1450L),
            monotonicNow = { 1450L }
        )
        assertEquals(JournalShiftMode.OFF, state.mode)
    }

    @Test
    fun testOneShotConsumedByTypingLetter() {
        var state = JournalShiftState(mode = JournalShiftMode.ONE_SHOT)
        state = JournalKeyboardController.reduceShift(
            state = state,
            action = ShiftAction.UserTypedText("A"),
            monotonicNow = { 1000L }
        )
        assertEquals(JournalShiftMode.OFF, state.mode)
    }

    @Test
    fun testCapsLockPersistsAcrossMultipleTypedLetters() {
        var state = JournalShiftState(mode = JournalShiftMode.CAPS_LOCK)
        state = JournalKeyboardController.reduceShift(
            state = state,
            action = ShiftAction.UserTypedText("H"),
            monotonicNow = { 1000L }
        )
        assertEquals(JournalShiftMode.CAPS_LOCK, state.mode)

        state = JournalKeyboardController.reduceShift(
            state = state,
            action = ShiftAction.UserTypedText("E"),
            monotonicNow = { 1010L }
        )
        assertEquals(JournalShiftMode.CAPS_LOCK, state.mode)

        state = JournalKeyboardController.reduceShift(
            state = state,
            action = ShiftAction.UserTypedText("L"),
            monotonicNow = { 1020L }
        )
        assertEquals(JournalShiftMode.CAPS_LOCK, state.mode)
    }

    @Test
    fun testSingleTapOnCapsLock_revertsToOff() {
        var state = JournalShiftState(mode = JournalShiftMode.CAPS_LOCK)
        state = JournalKeyboardController.reduceShift(
            state = state,
            action = ShiftAction.UserTapShift(2000L),
            monotonicNow = { 2000L }
        )
        assertEquals(JournalShiftMode.OFF, state.mode)
    }

    // ==========================================
    // 2. Grapheme Safe Text Insertion & Deletion Tests
    // ==========================================

    @Test
    fun testInsertText_atCursor() {
        val initial = TextFieldValue(text = "Hello", selection = TextRange(5, 5))
        val result = JournalKeyboardController.insertText(initial, " World")
        assertEquals("Hello World", result.text)
        assertEquals(TextRange(11, 11), result.selection)
    }

    @Test
    fun testInsertText_replacesSelection() {
        val initial = TextFieldValue(text = "Hello World", selection = TextRange(6, 11))
        val result = JournalKeyboardController.insertText(initial, "Friend")
        assertEquals("Hello Friend", result.text)
        assertEquals(TextRange(12, 12), result.selection)
    }

    @Test
    fun testDeleteBackward_singleAsciiCharacter() {
        val initial = TextFieldValue(text = "Marché", selection = TextRange(6, 6))
        val result = JournalKeyboardController.deleteBackward(initial, segmenter)
        assertEquals("March", result.text)
        assertEquals(TextRange(5, 5), result.selection)
    }

    @Test
    fun testDeleteBackward_selectionSpan() {
        val initial = TextFieldValue(text = "Flous l9or3a", selection = TextRange(6, 12))
        val result = JournalKeyboardController.deleteBackward(initial, segmenter)
        assertEquals("Flous ", result.text)
        assertEquals(TextRange(6, 6), result.selection)
    }

    @Test
    fun testDeleteBackward_surrogatePairEmoji() {
        val emojiText = "Fruit \uD83C\uDF4E" // Apple emoji (surrogate pair)
        val initial = TextFieldValue(text = emojiText, selection = TextRange(emojiText.length, emojiText.length))
        val result = JournalKeyboardController.deleteBackward(initial, segmenter)
        assertEquals("Fruit ", result.text)
    }

    @Test
    fun testDeleteBackward_arabicTextWithLamAlif() {
        val text = "حسابي"
        val initial = TextFieldValue(text = text, selection = TextRange(text.length, text.length))
        val result = JournalKeyboardController.deleteBackward(initial, segmenter)
        assertEquals("حساب", result.text)
    }

    // ==========================================
    // 3. Language Layout & Cycle Tests
    // ==========================================

    @Test
    fun testFrenchAzertyLayout_structure() {
        val rowsLower = JournalKeyboardController.getFrenchRows(JournalShiftMode.OFF)
        assertEquals(5, rowsLower.size)

        // Row 0: Top Number Row (1 2 3 4 5 6 7 8 9 0)
        assertEquals(10, rowsLower[0].size)
        assertEquals("1", rowsLower[0][0].label)
        assertEquals("0", rowsLower[0][9].label)

        // Row 1: Letter Row 1 (a z e r t y u i o p)
        assertEquals("a", rowsLower[1][0].label)
        assertEquals("z", rowsLower[1][1].label)
        assertEquals("e", rowsLower[1][2].label)
        assertTrue(rowsLower[1][2].alternatives.contains("é"))

        // Row 4: Utility Row (123 / 😊 / espace / . / OK ✓)
        assertTrue(rowsLower[4][1].isEmojiSwitch)
        assertEquals("espace", rowsLower[4][2].label)

        val rowsUpper = JournalKeyboardController.getFrenchRows(JournalShiftMode.ONE_SHOT)
        assertEquals(5, rowsUpper.size)
        assertEquals("1", rowsUpper[0][0].label)
        assertEquals("A", rowsUpper[1][0].label)
        assertEquals("Z", rowsUpper[1][1].label)
        assertEquals("E", rowsUpper[1][2].label)
        assertTrue(rowsUpper[1][2].alternatives.contains("É"))
    }

    @Test
    fun testEnglishQwertyLayout_structure() {
        val rows = JournalKeyboardController.getEnglishRows(JournalShiftMode.OFF)
        assertEquals(5, rows.size)

        // Row 0: Top Number Row (1 2 3 4 5 6 7 8 9 0)
        assertEquals(10, rows[0].size)
        assertEquals("1", rows[0][0].label)
        assertEquals("0", rows[0][9].label)

        // Row 1: Letter Row 1 (q w e r t y u i o p)
        assertEquals("q", rows[1][0].label)
        assertEquals("w", rows[1][1].label)
        assertEquals("e", rows[1][2].label)

        // Row 4: Utility Row (123 / 😊 / space / . / OK ✓)
        assertTrue(rows[4][1].isEmojiSwitch)
        assertEquals("space", rows[4][2].label)
    }

    @Test
    fun testArabicLayout_structure() {
        val rows = JournalKeyboardController.getArabicRows()
        assertEquals(5, rows.size)

        // Row 0: Top Number Row (1 2 3 4 5 6 7 8 9 0)
        assertEquals(10, rows[0].size)
        assertEquals("1", rows[0][0].label)
        assertEquals("0", rows[0][9].label)
        assertTrue(rows[0][0].alternatives.contains("١"))
        assertTrue(rows[0][9].alternatives.contains("٠"))

        // Row 1: 11 keys (ض ص ث ق ف غ ع ه خ ح ج)
        assertEquals(11, rows[1].size)
        assertEquals("ض", rows[1][0].label)
        assertEquals("ج", rows[1][10].label)

        // Row 2: 11 keys (ش س ي ب ل ا ت ن م ك ط)
        assertEquals(11, rows[2].size)
        assertEquals("ش", rows[2][0].label)
        assertEquals("ط", rows[2][10].label)

        // Row 3: 11 keys (ذ ء ؤ ر ى ة و ز ظ د ⌫)
        assertEquals(11, rows[3].size)
        assertEquals("ذ", rows[3][0].label)
        assertEquals("د", rows[3][9].label)
        assertTrue(rows[3][10].isBackspace)

        // Row 4: Utility row (123 / 😊 / مسافة / ، / OK ✓)
        assertEquals(5, rows[4].size)
        assertTrue(rows[4][0].isModeSwitch)
        assertTrue(rows[4][1].isEmojiSwitch)
        assertEquals("مسافة", rows[4][2].label)
        assertEquals("،", rows[4][3].label)
        assertTrue(rows[4][4].isConfirm)
    }

    @Test
    fun testMoroccanEmojiPack_structureAndContent() {
        val categories = com.cash.guide.ui.notebook.JournalEmojiData.categories
        assertEquals(8, categories.size)
        val totalEmojis = categories.sumOf { it.emojis.size }
        assertTrue("Total emojis should be at least 180, found $totalEmojis", totalEmojis >= 180)

        // Check specific Moroccan business categories
        val names = categories.map { it.nameFr }
        assertTrue(names.any { it.contains("Commerce") })
        assertTrue(names.any { it.contains("Chantier") })
        assertTrue(names.any { it.contains("Café") })
        assertTrue(names.any { it.contains("Transport") })
        assertTrue(names.any { it.contains("Maison") })
        assertTrue(names.any { it.contains("Flous") })
        assertTrue(names.any { it.contains("Équipe") })
        assertTrue(names.any { it.contains("Symboles") })

        // Check quick emojis
        val quick = com.cash.guide.ui.notebook.JournalEmojiData.quickEmojis
        assertTrue("Quick emojis should have at least 10 items", quick.size >= 10)
        assertTrue(quick.contains("🛒"))
        assertTrue(quick.contains("🏗️"))
        assertTrue(quick.contains("💰"))
    }

    @Test
    fun testCompleteArabicAlphabetCoverage() {
        val rows = JournalKeyboardController.getArabicRows()
        val primaryKeys = rows.flatMap { r -> r.map { it.output } }.toSet()
        val allAvailableOutputs = rows.flatMap { r ->
            r.flatMap { listOf(it.output) + it.alternatives }
        }.toSet()

        val alphabet = JournalKeyboardController.ALL_ARABIC_ALPHABET_LETTERS
        assertEquals(28, alphabet.length)

        for (letter in alphabet) {
            val letterStr = letter.toString()
            assertTrue(
                "Letter '$letterStr' must be directly accessible as a primary key or alternative",
                allAvailableOutputs.contains(letterStr)
            )
            assertTrue(
                "Letter '$letterStr' must be present as a primary key",
                primaryKeys.contains(letterStr)
            )
        }

        // Verify key alternatives
        val alifKey = rows[2].first { it.label == "ا" }
        assertTrue(alifKey.alternatives.containsAll(listOf("أ", "إ", "آ")))

        val laamKey = rows[2].first { it.label == "ل" }
        assertTrue(laamKey.alternatives.containsAll(listOf("لا", "لأ", "لإ", "لآ")))

        val dalKey = rows[3].first { it.label == "د" }
        assertTrue(dalKey.alternatives.contains("ذ"))

        val dhalKey = rows[3].first { it.label == "ذ" }
        assertEquals("ذ", dhalKey.output)
    }

    @Test
    fun testLanguageCycle() {
        assertEquals(JournalKeyboardLanguage.ENGLISH, JournalKeyboardController.nextLanguage(JournalKeyboardLanguage.FRENCH))
        assertEquals(JournalKeyboardLanguage.ARABIC, JournalKeyboardController.nextLanguage(JournalKeyboardLanguage.ENGLISH))
        assertEquals(JournalKeyboardLanguage.FRENCH, JournalKeyboardController.nextLanguage(JournalKeyboardLanguage.ARABIC))
    }

    // ==========================================
    // 4. Anchored Popup Positioning Calculations
    // ==========================================

    @Test
    fun testCalculateAnchoredPopupPosition_centerKey() {
        val anchor = IntRect(left = 500, top = 1600, right = 580, bottom = 1680)
        val windowSize = IntSize(width = 1080, height = 2340)
        val popupSize = IntSize(width = 200, height = 60)

        val pos = JournalKeyboardController.calculateAnchoredPopupPosition(
            anchorBounds = anchor,
            windowSize = windowSize,
            popupContentSize = popupSize
        )

        // Horizontally centered: 500 + (80 - 200) / 2 = 440
        assertEquals(440, pos.x)
        // Positioned above key: 1600 - 60 - 8 = 1532
        assertEquals(1532, pos.y)
    }

    @Test
    fun testCalculateAnchoredPopupPosition_leftEdgeKeyClamping() {
        val anchor = IntRect(left = 0, top = 1600, right = 80, bottom = 1680)
        val windowSize = IntSize(width = 1080, height = 2340)
        val popupSize = IntSize(width = 200, height = 60)

        val pos = JournalKeyboardController.calculateAnchoredPopupPosition(
            anchorBounds = anchor,
            windowSize = windowSize,
            popupContentSize = popupSize,
            safePaddingPx = 16
        )

        // Clamped to left safe margin
        assertEquals(16, pos.x)
    }

    @Test
    fun testCalculateAnchoredPopupPosition_rightEdgeKeyClamping() {
        val anchor = IntRect(left = 1000, top = 1600, right = 1080, bottom = 1680)
        val windowSize = IntSize(width = 1080, height = 2340)
        val popupSize = IntSize(width = 200, height = 60)

        val pos = JournalKeyboardController.calculateAnchoredPopupPosition(
            anchorBounds = anchor,
            windowSize = windowSize,
            popupContentSize = popupSize,
            safePaddingPx = 16
        )

        // Clamped to right safe margin: 1080 - 200 - 16 = 864
        assertEquals(864, pos.x)
    }

    @Test
    fun testCalculateAnchoredPopupPosition_insufficientSpaceAboveFlipsBelow() {
        val anchor = IntRect(left = 500, top = 20, right = 580, bottom = 100)
        val windowSize = IntSize(width = 1080, height = 2340)
        val popupSize = IntSize(width = 200, height = 60)

        val pos = JournalKeyboardController.calculateAnchoredPopupPosition(
            anchorBounds = anchor,
            windowSize = windowSize,
            popupContentSize = popupSize,
            safePaddingPx = 16
        )

        // Space above is 20px (< 60 + 16), so flips below: bottom (100) + 8 = 108
        assertEquals(108, pos.y)
        assertTrue(pos.y >= 0)
    }

    // ==========================================
    // 5. Backspace Repeat Controller Coroutine Timing Tests
    // ==========================================

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testBackspaceRepeatController_tapAndHoldAcceleration() {
        val testScope = TestScope()
        var deleteCount = 0

        val controller = BackspaceRepeatController(
            scope = testScope,
            onDelete = { deleteCount++ },
            initialDelayMs = 400L,
            initialRepeatRateMs = 90L,
            acceleratedRepeatRateMs = 45L,
            repeatsBeforeAcceleration = 5
        )

        // Pointer down triggers immediate single tap delete
        controller.onPointerDown()
        testScope.runCurrent()
        assertEquals(1, deleteCount)

        // Advance 399ms (before initial hold delay)
        testScope.advanceTimeBy(399L)
        testScope.runCurrent()
        assertEquals(1, deleteCount)

        // Reach initial hold delay (400ms)
        testScope.advanceTimeBy(1L)
        testScope.runCurrent()
        assertEquals(2, deleteCount)

        // 4 repeats at standard rate (90ms each -> 360ms)
        testScope.advanceTimeBy(360L)
        testScope.runCurrent()
        assertEquals(6, deleteCount)

        // Now accelerated rate takes over (45ms per repeat)
        testScope.advanceTimeBy(45L)
        testScope.runCurrent()
        assertEquals(7, deleteCount)

        // Pointer up cancels immediately
        controller.onPointerUp()
        testScope.advanceTimeBy(500L)
        testScope.runCurrent()
        assertEquals(7, deleteCount)
    }
}

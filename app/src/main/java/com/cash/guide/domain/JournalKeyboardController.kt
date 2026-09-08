package com.cash.guide.domain

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Active Dock Modes:
 * - NONE: Keyboard hidden
 * - TEXT: Custom text keyboard (FR/EN/AR)
 * - NUMBER: Custom compact numeric keypad
 */
enum class JournalKeyboardMode {
    NONE,
    TEXT,
    NUMBER
}

/**
 * Supported Keyboard Languages (Exactly 3):
 * 1. FRENCH  — AZERTY
 * 2. ENGLISH — QWERTY
 * 3. ARABIC  — Standard Arabic Mobile Layout with RTL row direction
 */
enum class JournalKeyboardLanguage(val label: String, val displayName: String) {
    FRENCH("FR", "Français"),
    ENGLISH("EN", "English"),
    ARABIC("ع", "العربية")
}

/**
 * Latin Shift Modes:
 * - OFF: Lowercase letters
 * - ONE_SHOT: Uppercase for the next typed letter only
 * - CAPS_LOCK: Persistent uppercase until tapped again
 */
enum class JournalShiftMode {
    OFF,
    ONE_SHOT,
    CAPS_LOCK
}

/**
 * Durable Shift state & transient tap tracking
 */
data class JournalShiftState(
    val mode: JournalShiftMode = JournalShiftMode.OFF,
    val lastPhysicalTapTimestamp: Long = 0L,
    val isAutoOneShot: Boolean = false
)

/**
 * Explicit Shift Reducer Actions
 */
sealed interface ShiftAction {
    /** Fired when an empty title field receives focus (does NOT record tap timestamp) */
    data object AutoSetOneShot : ShiftAction

    /** Physical user tap on Shift key with monotonic timestamp */
    data class UserTapShift(val timestampMs: Long) : ShiftAction

    /** Fired when any text is typed */
    data class UserTypedText(val text: String) : ShiftAction

    /** Explicitly reset shift state */
    data object Reset : ShiftAction
}

/**
 * Specification for an individual keyboard key.
 * Uses String output to support multi-character glyphs like "لا" or composed diacritics.
 */
data class JournalKeySpec(
    val label: String,
    val output: String = label,
    val contentDescription: String = label,
    val alternatives: List<String> = emptyList(),
    val isShift: Boolean = false,
    val isBackspace: Boolean = false,
    val isSpace: Boolean = false,
    val isModeSwitch: Boolean = false,
    val isEmojiSwitch: Boolean = false,
    val isConfirm: Boolean = false,
    val flexWeight: Float = 1.0f
)

/**
 * Grapheme segmentation interface to safely delete Unicode clusters
 * (surrogate pairs, combining accents, emoji ZWJ, and Arabic harakat)
 * without coupling unit tests to Android ICU.
 */
interface GraphemeSegmenter {
    fun getPreviousGraphemeOffset(text: String, cursorOffset: Int): Int
}

/**
 * Production Android ICU Grapheme Segmenter (minSdk 24)
 */
class AndroidIcuGraphemeSegmenter : GraphemeSegmenter {
    override fun getPreviousGraphemeOffset(text: String, cursorOffset: Int): Int {
        if (cursorOffset <= 0 || text.isEmpty()) return 0
        val clampedOffset = cursorOffset.coerceIn(0, text.length)
        val iterator = android.icu.text.BreakIterator.getCharacterInstance()
        iterator.setText(text)
        val boundary = iterator.preceding(clampedOffset)
        return if (boundary == android.icu.text.BreakIterator.DONE) 0 else boundary
    }
}

/**
 * JVM-compatible Grapheme Segmenter for local unit tests
 */
class JvmGraphemeSegmenter : GraphemeSegmenter {
    override fun getPreviousGraphemeOffset(text: String, cursorOffset: Int): Int {
        if (cursorOffset <= 0 || text.isEmpty()) return 0
        val clampedOffset = cursorOffset.coerceIn(0, text.length)
        val iterator = java.text.BreakIterator.getCharacterInstance()
        iterator.setText(text)
        val boundary = iterator.preceding(clampedOffset)
        return if (boundary == java.text.BreakIterator.DONE) {
            if (clampedOffset > 1 && Character.isSurrogatePair(text[clampedOffset - 2], text[clampedOffset - 1])) {
                clampedOffset - 2
            } else {
                (clampedOffset - 1).coerceAtLeast(0)
            }
        } else {
            boundary
        }
    }
}

object JournalKeyboardController {

    private const val DOUBLE_TAP_WINDOW_MS = 300L

    /**
     * Pure Shift Reducer
     */
    fun reduceShift(
        state: JournalShiftState,
        action: ShiftAction,
        monotonicNow: () -> Long
    ): JournalShiftState {
        return when (action) {
            is ShiftAction.AutoSetOneShot -> {
                JournalShiftState(
                    mode = JournalShiftMode.ONE_SHOT,
                    lastPhysicalTapTimestamp = 0L,
                    isAutoOneShot = true
                )
            }

            is ShiftAction.UserTapShift -> {
                val now = action.timestampMs
                when (state.mode) {
                    JournalShiftMode.OFF -> {
                        JournalShiftState(
                            mode = JournalShiftMode.ONE_SHOT,
                            lastPhysicalTapTimestamp = now,
                            isAutoOneShot = false
                        )
                    }

                    JournalShiftMode.ONE_SHOT -> {
                        if (state.isAutoOneShot) {
                            // First physical tap on automatic one-shot switches to OFF
                            JournalShiftState(
                                mode = JournalShiftMode.OFF,
                                lastPhysicalTapTimestamp = now,
                                isAutoOneShot = false
                            )
                        } else {
                            // User previously tapped Shift
                            val elapsed = now - state.lastPhysicalTapTimestamp
                            if (elapsed in 0..DOUBLE_TAP_WINDOW_MS) {
                                // Double tap enabled Caps Lock
                                JournalShiftState(
                                    mode = JournalShiftMode.CAPS_LOCK,
                                    lastPhysicalTapTimestamp = 0L,
                                    isAutoOneShot = false
                                )
                            } else {
                                // Single tap after timeout toggles to OFF
                                JournalShiftState(
                                    mode = JournalShiftMode.OFF,
                                    lastPhysicalTapTimestamp = now,
                                    isAutoOneShot = false
                                )
                            }
                        }
                    }

                    JournalShiftMode.CAPS_LOCK -> {
                        // Tapping Caps Lock reverts to OFF
                        JournalShiftState(
                            mode = JournalShiftMode.OFF,
                            lastPhysicalTapTimestamp = now,
                            isAutoOneShot = false
                        )
                    }
                }
            }

            is ShiftAction.UserTypedText -> {
                if (state.mode == JournalShiftMode.ONE_SHOT) {
                    // Consumes one-shot capitalization
                    JournalShiftState(
                        mode = JournalShiftMode.OFF,
                        lastPhysicalTapTimestamp = 0L,
                        isAutoOneShot = false
                    )
                } else {
                    state
                }
            }

            is ShiftAction.Reset -> {
                JournalShiftState(
                    mode = JournalShiftMode.OFF,
                    lastPhysicalTapTimestamp = 0L,
                    isAutoOneShot = false
                )
            }
        }
    }

    /**
     * Inserts text at the current cursor or replaces the selection range.
     */
    fun insertText(current: TextFieldValue, textToInsert: String): TextFieldValue {
        val sel = current.selection
        val min = sel.min.coerceIn(0, current.text.length)
        val max = sel.max.coerceIn(0, current.text.length)

        val before = current.text.substring(0, min)
        val after = current.text.substring(max)
        val newText = before + textToInsert + after
        val newCursor = min + textToInsert.length

        return current.copy(
            text = newText,
            selection = TextRange(newCursor, newCursor)
        )
    }

    /**
     * Safely deletes text backwards using grapheme segmentation without splitting multi-byte UTF-16 surrogates,
     * combining marks, or emoji sequences. Deletes non-empty selection first.
     */
    fun deleteBackward(
        current: TextFieldValue,
        segmenter: GraphemeSegmenter
    ): TextFieldValue {
        val sel = current.selection
        val text = current.text
        if (text.isEmpty()) return current

        val min = sel.min.coerceIn(0, text.length)
        val max = sel.max.coerceIn(0, text.length)

        if (min != max) {
            // Delete selection span
            val before = text.substring(0, min)
            val after = text.substring(max)
            return current.copy(
                text = before + after,
                selection = TextRange(min, min)
            )
        }

        if (min == 0) return current

        val prevGraphemeOffset = segmenter.getPreviousGraphemeOffset(text, min)
        val deleteStart = prevGraphemeOffset.coerceIn(0, min)
        val before = text.substring(0, deleteStart)
        val after = text.substring(min)

        return current.copy(
            text = before + after,
            selection = TextRange(deleteStart, deleteStart)
        )
    }

    /**
     * Returns the next language in cyclic order: FR -> EN -> AR -> FR
     */
    fun nextLanguage(current: JournalKeyboardLanguage): JournalKeyboardLanguage {
        return when (current) {
            JournalKeyboardLanguage.FRENCH -> JournalKeyboardLanguage.ENGLISH
            JournalKeyboardLanguage.ENGLISH -> JournalKeyboardLanguage.ARABIC
            JournalKeyboardLanguage.ARABIC -> JournalKeyboardLanguage.FRENCH
        }
    }

    // ==========================================
    // Layout Definitions (Rows 0 to 4)
    // Row 0: Top Number Row (1 2 3 4 5 6 7 8 9 0)
    // Rows 1-3: Letter Rows
    // Row 4: Utility Row (123, 😊, Space, Punctuation, OK ✓)
    // ==========================================

    fun createNumberRow(isArabic: Boolean = false): List<JournalKeySpec> {
        val digits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val arabicAlts = listOf(
            listOf("١"), listOf("٢"), listOf("٣"), listOf("٤"), listOf("٥"),
            listOf("٦"), listOf("٧"), listOf("٨"), listOf("٩"), listOf("٠")
        )
        val latinAlts = listOf(
            listOf("¹", "½"),
            listOf("²", "⅓"),
            listOf("³", "¼"),
            listOf("⁴"),
            listOf("⁵"),
            listOf("⁶"),
            listOf("⁷"),
            listOf("⁸"),
            listOf("⁹"),
            listOf("⁰", "°")
        )
        return digits.mapIndexed { index, digit ->
            JournalKeySpec(
                label = digit,
                output = digit,
                contentDescription = digit,
                alternatives = if (isArabic) arabicAlts[index] else latinAlts[index],
                flexWeight = 1.0f
            )
        }
    }

    fun getFrenchRows(shiftMode: JournalShiftMode): List<List<JournalKeySpec>> {
        val isUpper = shiftMode != JournalShiftMode.OFF
        fun charKey(c: String, alts: List<String> = emptyList()): JournalKeySpec {
            val label = if (isUpper) c.uppercase() else c.lowercase()
            val output = label
            val mappedAlts = if (isUpper) alts.map { it.uppercase() } else alts.map { it.lowercase() }
            return JournalKeySpec(label = label, output = output, contentDescription = label, alternatives = mappedAlts)
        }

        val eAlts = listOf("é", "è", "ê", "ë")
        val aAlts = listOf("à", "â", "ä")
        val cAlts = listOf("ç")
        val uAlts = listOf("ù", "û", "ü")
        val iAlts = listOf("î", "ï")
        val oAlts = listOf("ô", "ö")

        val r0 = createNumberRow(isArabic = false)
        val r1 = listOf(
            charKey("a", aAlts), charKey("z"), charKey("e", eAlts), charKey("r"), charKey("t"),
            charKey("y"), charKey("u", uAlts), charKey("i", iAlts), charKey("o", oAlts), charKey("p")
        )
        val r2 = listOf(
            charKey("q"), charKey("s"), charKey("d"), charKey("f"), charKey("g"),
            charKey("h"), charKey("j"), charKey("k"), charKey("l"), charKey("m")
        )
        val r3 = listOf(
            JournalKeySpec(label = "⇧", contentDescription = "Majuscule", isShift = true, flexWeight = 1.3f),
            charKey("w"), charKey("x"), charKey("c", cAlts), charKey("v"), charKey("b"), charKey("n"),
            JournalKeySpec(label = "'", output = "'", contentDescription = "Apostrophe", alternatives = listOf("\"", "-", "«", "»")),
            JournalKeySpec(label = "⌫", contentDescription = "Effacer", isBackspace = true, flexWeight = 1.3f)
        )
        val r4 = listOf(
            JournalKeySpec(label = "123", contentDescription = "Chiffres", isModeSwitch = true, flexWeight = 1.3f),
            JournalKeySpec(label = "😊", contentDescription = "Émojis", isEmojiSwitch = true, flexWeight = 1.1f),
            JournalKeySpec(label = "espace", output = " ", contentDescription = "Espace", isSpace = true, flexWeight = 3.6f),
            JournalKeySpec(label = ".", output = ".", contentDescription = "Point", alternatives = listOf(",", "?", "!", ":", ";"), flexWeight = 1.1f),
            JournalKeySpec(label = "OK ✓", contentDescription = "Valider", isConfirm = true, flexWeight = 1.7f)
        )

        return listOf(r0, r1, r2, r3, r4)
    }

    fun getEnglishRows(shiftMode: JournalShiftMode): List<List<JournalKeySpec>> {
        val isUpper = shiftMode != JournalShiftMode.OFF
        fun charKey(c: String): JournalKeySpec {
            val label = if (isUpper) c.uppercase() else c.lowercase()
            return JournalKeySpec(label = label, output = label, contentDescription = label)
        }

        val r0 = createNumberRow(isArabic = false)
        val r1 = listOf(
            charKey("q"), charKey("w"), charKey("e"), charKey("r"), charKey("t"),
            charKey("y"), charKey("u"), charKey("i"), charKey("o"), charKey("p")
        )
        val r2 = listOf(
            charKey("a"), charKey("s"), charKey("d"), charKey("f"), charKey("g"),
            charKey("h"), charKey("j"), charKey("k"), charKey("l")
        )
        val r3 = listOf(
            JournalKeySpec(label = "⇧", contentDescription = "Shift", isShift = true, flexWeight = 1.3f),
            charKey("z"), charKey("x"), charKey("c"), charKey("v"), charKey("b"), charKey("n"), charKey("m"),
            JournalKeySpec(label = "'", output = "'", contentDescription = "Apostrophe", alternatives = listOf("\"", "-", "'", "`")),
            JournalKeySpec(label = "⌫", contentDescription = "Backspace", isBackspace = true, flexWeight = 1.3f)
        )
        val r4 = listOf(
            JournalKeySpec(label = "123", contentDescription = "Numbers", isModeSwitch = true, flexWeight = 1.3f),
            JournalKeySpec(label = "😊", contentDescription = "Emojis", isEmojiSwitch = true, flexWeight = 1.1f),
            JournalKeySpec(label = "space", output = " ", contentDescription = "Space", isSpace = true, flexWeight = 3.6f),
            JournalKeySpec(label = ".", output = ".", contentDescription = "Period", alternatives = listOf(",", "?", "!", ":", ";"), flexWeight = 1.1f),
            JournalKeySpec(label = "OK ✓", contentDescription = "Confirm", isConfirm = true, flexWeight = 1.7f)
        )

        return listOf(r0, r1, r2, r3, r4)
    }

    const val ALL_ARABIC_ALPHABET_LETTERS = "ابتثجحخدذرزسشصضطظعغفقكلمنهوي"

    /**
     * Arabic mobile layout stored in left-to-right visual order matching Samsung keyboard:
     * Row 0: 1 2 3 4 5 6 7 8 9 0 (Number Row)
     * Row 1: ض ص ث ق ف غ ع ه خ ح ج (11 keys)
     * Row 2: ش س ي ب ل ا ت ن م ك ط (11 keys)
     * Row 3: ذ ء ؤ ر ى ة و ز ظ د ⌫ (10 letters + Backspace = 11 keys)
     * Row 4: 123 😊 مسافة ، OK ✓ (Utility Row)
     */
    fun getArabicRows(): List<List<JournalKeySpec>> {
        fun arKey(c: String, alts: List<String> = emptyList(), flexWeight: Float = 1.0f): JournalKeySpec {
            return JournalKeySpec(label = c, output = c, contentDescription = c, alternatives = alts, flexWeight = flexWeight)
        }

        val alifAlts = listOf("أ", "إ", "آ", "ٱ")
        val laamAlts = listOf("لا", "لأ", "لإ", "لآ")
        val yaaAlts = listOf("ئ") // "ى" is a dedicated key in Row 3
        val faAlts = listOf("ڤ") // Moroccan Ve (e.g. villa, virage)
        val qafAlts = listOf("ڨ") // Moroccan Gaf (e.g. Guelmim, ga3)
        val kafAlts = listOf("ڨ")
        val baaAlts = listOf("پ") // Moroccan Pe (e.g. prix, parking)
        val haaAlts = listOf("ـ") // Tatweel ("ة" is a dedicated key in Row 3)

        val r0 = createNumberRow(isArabic = true)
        val r1 = listOf(
            arKey("ض"), arKey("ص"), arKey("ث"), arKey("ق", qafAlts), arKey("ف", faAlts),
            arKey("غ"), arKey("ع"), arKey("ه", haaAlts), arKey("خ"), arKey("ح"), arKey("ج")
        )
        val r2 = listOf(
            arKey("ش"), arKey("س"), arKey("ي", yaaAlts), arKey("ب", baaAlts), arKey("ل", laamAlts),
            arKey("ا", alifAlts), arKey("ت"), arKey("ن"), arKey("م"), arKey("ك", kafAlts), arKey("ط")
        )
        val r3 = listOf(
            arKey("ذ"), arKey("ء"), arKey("ؤ"), arKey("ر"), arKey("ى"),
            arKey("ة"), arKey("و"), arKey("ز"), arKey("ظ"), arKey("د"),
            JournalKeySpec(label = "⌫", contentDescription = "حذف", isBackspace = true, flexWeight = 1.15f)
        )
        val r4 = listOf(
            JournalKeySpec(label = "123", contentDescription = "أرقام", isModeSwitch = true, flexWeight = 1.3f),
            JournalKeySpec(label = "😊", contentDescription = "رموز تعبيرية", isEmojiSwitch = true, flexWeight = 1.1f),
            JournalKeySpec(label = "مسافة", output = " ", contentDescription = "مسافة", isSpace = true, flexWeight = 3.6f),
            JournalKeySpec(label = "،", output = "،", contentDescription = "فاصلة", alternatives = listOf("؟", "!", "."), flexWeight = 1.1f),
            JournalKeySpec(label = "OK ✓", contentDescription = "تأكيد", isConfirm = true, flexWeight = 1.7f)
        )

        return listOf(r0, r1, r2, r3, r4)
    }

    /**
     * Pure function to calculate anchored popup coordinates with full edge clamping.
     */
    fun calculateAnchoredPopupPosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        popupContentSize: IntSize,
        safePaddingPx: Int = 16
    ): IntOffset {
        // Horizontal centering above key with window bounds clamping
        val preferredX = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        val clampedX = preferredX.coerceIn(
            safePaddingPx,
            (windowSize.width - popupContentSize.width - safePaddingPx).coerceAtLeast(safePaddingPx)
        )

        // Vertical placement: above key if space permits, else below key
        val spaceAbove = anchorBounds.top
        val preferredY = if (spaceAbove >= popupContentSize.height + safePaddingPx) {
            anchorBounds.top - popupContentSize.height - 8
        } else {
            anchorBounds.bottom + 8
        }
        val clampedY = preferredY.coerceIn(
            safePaddingPx,
            (windowSize.height - popupContentSize.height - safePaddingPx).coerceAtLeast(safePaddingPx)
        )

        return IntOffset(clampedX, clampedY)
    }
}

/**
 * Controller managing Backspace tap, hold, repeat, and acceleration with strict lifecycle cancellation.
 */
class BackspaceRepeatController(
    private val scope: CoroutineScope,
    private val onDelete: () -> Unit,
    private val initialDelayMs: Long = 400L,
    private val initialRepeatRateMs: Long = 90L,
    private val acceleratedRepeatRateMs: Long = 45L,
    private val repeatsBeforeAcceleration: Int = 10
) {
    private var repeatJob: Job? = null

    fun onPointerDown() {
        cancel()
        // Single tap deletion immediately
        onDelete()

        // Start repeating coroutine job
        repeatJob = scope.launch {
            delay(initialDelayMs)
            var count = 0
            while (isActive) {
                onDelete()
                count++
                val rate = if (count >= repeatsBeforeAcceleration) {
                    acceleratedRepeatRateMs
                } else {
                    initialRepeatRateMs
                }
                delay(rate)
            }
        }
    }

    fun onPointerUp() {
        cancel()
    }

    fun cancel() {
        repeatJob?.cancel()
        repeatJob = null
    }
}

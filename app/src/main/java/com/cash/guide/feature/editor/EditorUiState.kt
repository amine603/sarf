package com.cash.guide.feature.editor

import androidx.compose.ui.text.input.TextFieldValue
import com.cash.guide.domain.JournalKeyboardLanguage
import com.cash.guide.domain.JournalKeyboardMode
import com.cash.guide.domain.JournalShiftMode
import com.cash.guide.domain.JournalShiftState
import com.cash.guide.domain.MoneyMath
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.ActiveField

enum class EditorMode {
    NEW,
    EXISTING
}

data class EditorRowUiState(
    val id: Long,
    val title: TextFieldValue = TextFieldValue(""),
    val amount: TextFieldValue = TextFieldValue(""),
    val rawExpression: String = ""
) {
    val isEmpty: Boolean get() = title.text.isBlank() && amount.text.isBlank()
    val isPopulated: Boolean get() = !isEmpty
}

data class CalculatorPopupState(
    val isVisible: Boolean = false,
    val targetRowId: Long? = null,
    val expression: String = "",
    val result: String = "",
    val hasError: Boolean = false,
    val isEvaluated: Boolean = false
)

data class EditorUiState(
    val calculationId: String? = null,
    val editingSavedId: String? = null,
    val mode: EditorMode = EditorMode.NEW,
    val title: TextFieldValue = TextFieldValue(""),
    val currency: MoneyUnit = MoneyUnit.DIRHAM,
    val rows: List<EditorRowUiState> = listOf(EditorRowUiState(id = 1L)),
    val activeRowId: Long? = 1L,
    val activeField: ActiveField = ActiveField.TITLE,
    val keyboardMode: JournalKeyboardMode = JournalKeyboardMode.TEXT,
    val keyboardLanguage: JournalKeyboardLanguage = JournalKeyboardLanguage.FRENCH,
    val shiftState: JournalShiftState = JournalShiftState(),
    val shiftMode: JournalShiftMode = JournalShiftMode.OFF,
    val keyboardExpanded: Boolean = true,
    val calculator: CalculatorPopupState = CalculatorPopupState(),
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val showUnsavedDialog: Boolean = false,
    val showBreakdownSheet: Boolean = false,
    val recoveredDraft: Boolean = false,
    val validationError: String? = null,
    val pendingFocusRowId: Long? = 1L,
    val createdAtEpochMs: Long? = null,
    val canUndo: Boolean = false,
    val groupId: String? = null
) {
    val hasInvalidRows: Boolean
        get() = rows.any { it.amount.text.isNotBlank() && !MoneyMath.isValidExpression(it.amount.text) }

    val totalCentimes: Long
        get() = if (hasInvalidRows) {
            0L
        } else {
            rows.sumOf { row ->
                MoneyMath.toCentimes(row.amount.text, currency) ?: 0L
            }
        }
}

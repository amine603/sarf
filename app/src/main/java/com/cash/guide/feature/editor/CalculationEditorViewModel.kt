package com.cash.guide.feature.editor

import android.os.SystemClock
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.SettingsRepository
import com.cash.guide.data.db.CalculationEntity
import com.cash.guide.data.db.CalculationItemEntity
import com.cash.guide.domain.AndroidIcuGraphemeSegmenter
import com.cash.guide.domain.GraphemeSegmenter
import com.cash.guide.domain.JournalKeyboardController
import com.cash.guide.domain.JournalKeyboardLanguage
import com.cash.guide.domain.JournalKeyboardMode
import com.cash.guide.domain.JournalShiftMode
import com.cash.guide.domain.JournalShiftState
import com.cash.guide.domain.MoneyMath
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.domain.ShiftAction
import com.cash.guide.ui.ActiveField
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CalculationEditorViewModel(
    private val calculationRepository: CalculationRepository,
    private val settingsRepository: SettingsRepository? = null,
    private val graphemeSegmenter: GraphemeSegmenter = AndroidIcuGraphemeSegmenter(),
    private val monotonicClock: () -> Long = { SystemClock.uptimeMillis() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var nextRowId = 2L
    private var lastLatinShiftMode = JournalShiftMode.OFF
    private var draftSaveJob: Job? = null

    fun loadCalculation(id: String?) {
        viewModelScope.launch {
            val defaultCurrency = settingsRepository?.defaultCurrency?.first() ?: MoneyUnit.DIRHAM
            val defaultLanguage = when (settingsRepository?.appLanguage?.first()) {
                "ar" -> JournalKeyboardLanguage.ARABIC
                else -> JournalKeyboardLanguage.FRENCH
            }

            if (id == null) {
                // Check if an uncommitted new draft exists
                val draft = calculationRepository.getRecoverableDraft(null)
                if (draft != null && (draft.calculation.title.isNotBlank() || draft.items.isNotEmpty())) {
                    val restoredRows = if (draft.items.isEmpty()) {
                        listOf(EditorRowUiState(id = 1L))
                    } else {
                        draft.items.mapIndexed { idx, item ->
                            EditorRowUiState(
                                id = (idx + 1).toLong(),
                                title = TextFieldValue(item.label, TextRange(item.label.length)),
                                amount = TextFieldValue(
                                    MoneyMath.fromCentimes(item.amountCentimes, MoneyUnit.valueOf(draft.calculation.currency)),
                                    TextRange(MoneyMath.fromCentimes(item.amountCentimes, MoneyUnit.valueOf(draft.calculation.currency)).length)
                                ),
                                rawExpression = item.rawExpression ?: ""
                            )
                        }
                    }
                    nextRowId = (restoredRows.maxOfOrNull { it.id } ?: 1L) + 1
                    _uiState.update {
                        it.copy(
                            calculationId = draft.calculation.id,
                            editingSavedId = null,
                            mode = EditorMode.NEW,
                            title = TextFieldValue(draft.calculation.title, TextRange(draft.calculation.title.length)),
                            currency = MoneyUnit.valueOf(draft.calculation.currency),
                            rows = restoredRows,
                            activeRowId = restoredRows.firstOrNull()?.id,
                            activeField = ActiveField.TITLE,
                            keyboardMode = JournalKeyboardMode.TEXT,
                            keyboardLanguage = defaultLanguage,
                            keyboardExpanded = true,
                            isDirty = true,
                            recoveredDraft = true
                        )
                    }
                } else {
                    val initialId = UUID.randomUUID().toString()
                    _uiState.update {
                        it.copy(
                            calculationId = initialId,
                            editingSavedId = null,
                            mode = EditorMode.NEW,
                            title = TextFieldValue(""),
                            currency = defaultCurrency,
                            rows = listOf(EditorRowUiState(id = 1L)),
                            activeRowId = 1L,
                            activeField = ActiveField.TITLE,
                            keyboardMode = JournalKeyboardMode.TEXT,
                            keyboardLanguage = defaultLanguage,
                            keyboardExpanded = true,
                            isDirty = false
                        )
                    }
                }
            } else {
                // Check if draft for this saved calculation exists
                val draft = calculationRepository.getRecoverableDraft(id)
                if (draft != null) {
                    val restoredRows = draft.items.mapIndexed { idx, item ->
                        EditorRowUiState(
                            id = (idx + 1).toLong(),
                            title = TextFieldValue(item.label, TextRange(item.label.length)),
                            amount = TextFieldValue(
                                MoneyMath.fromCentimes(item.amountCentimes, MoneyUnit.valueOf(draft.calculation.currency)),
                                TextRange(MoneyMath.fromCentimes(item.amountCentimes, MoneyUnit.valueOf(draft.calculation.currency)).length)
                            ),
                            rawExpression = item.rawExpression ?: ""
                        )
                    }
                    nextRowId = (restoredRows.maxOfOrNull { it.id } ?: 1L) + 1
                    _uiState.update {
                        it.copy(
                            calculationId = draft.calculation.id,
                            editingSavedId = id,
                            mode = EditorMode.EXISTING,
                            title = TextFieldValue(draft.calculation.title, TextRange(draft.calculation.title.length)),
                            currency = MoneyUnit.valueOf(draft.calculation.currency),
                            rows = restoredRows.ifEmpty { listOf(EditorRowUiState(id = 1L)) },
                            activeRowId = null,
                            activeField = ActiveField.NONE,
                            keyboardMode = JournalKeyboardMode.NONE,
                            keyboardExpanded = false,
                            isDirty = true,
                            recoveredDraft = true
                        )
                    }
                } else {
                    val saved = calculationRepository.getCalculation(id)
                    if (saved != null) {
                        val loadedCurrency = runCatching { MoneyUnit.valueOf(saved.calculation.currency) }.getOrDefault(defaultCurrency)
                        val loadedRows = saved.items.sortedBy { it.position }.mapIndexed { idx, item ->
                            val amountStr = MoneyMath.fromCentimes(item.amountCentimes, loadedCurrency)
                            EditorRowUiState(
                                id = (idx + 1).toLong(),
                                title = TextFieldValue(item.label, TextRange(item.label.length)),
                                amount = TextFieldValue(amountStr, TextRange(amountStr.length)),
                                rawExpression = item.rawExpression ?: ""
                            )
                        }
                        val rows = loadedRows.ifEmpty { listOf(EditorRowUiState(id = 1L)) }
                        nextRowId = (rows.maxOfOrNull { it.id } ?: 1L) + 1
                        _uiState.update {
                            it.copy(
                                calculationId = saved.calculation.id,
                                editingSavedId = id,
                                mode = EditorMode.EXISTING,
                                title = TextFieldValue(saved.calculation.title, TextRange(saved.calculation.title.length)),
                                currency = loadedCurrency,
                                rows = rows,
                                activeRowId = null,
                                activeField = ActiveField.NONE,
                                keyboardMode = JournalKeyboardMode.NONE,
                                keyboardExpanded = false,
                                isDirty = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateTitle(value: TextFieldValue) {
        _uiState.update { it.copy(title = value, isDirty = true) }
        scheduleDraftSave()
    }

    fun updateRowTitle(id: Long, value: TextFieldValue) {
        _uiState.update { state ->
            val updated = state.rows.map { row ->
                if (row.id == id) row.copy(title = value) else row
            }
            state.copy(rows = updated, isDirty = true)
        }
        scheduleDraftSave()
    }

    fun updateRowAmount(id: Long, value: TextFieldValue) {
        val filteredText = value.text.filter { it.isDigit() || it == '.' }
        val sanitizedText = if (filteredText.count { it == '.' } > 1) {
            val firstDot = filteredText.indexOf('.')
            filteredText.filterIndexed { idx, ch -> ch != '.' || idx == firstDot }
        } else {
            filteredText
        }
        val sanitizedValue = if (sanitizedText != value.text) {
            TextFieldValue(text = sanitizedText, selection = TextRange(sanitizedText.length.coerceAtMost(value.selection.start)))
        } else {
            value
        }

        _uiState.update { state ->
            val updated = state.rows.map { row ->
                if (row.id == id) row.copy(amount = sanitizedValue) else row
            }
            state.copy(rows = updated, isDirty = true)
        }
        scheduleDraftSave()
    }

    fun addNewRow() {
        val newId = nextRowId++
        val newRow = EditorRowUiState(id = newId)
        _uiState.update { state ->
            val updatedRows = state.rows + newRow
            var newShiftState = state.shiftState
            var newShiftMode = state.shiftMode
            if (state.keyboardLanguage != JournalKeyboardLanguage.ARABIC) {
                newShiftState = JournalKeyboardController.reduceShift(
                    state = state.shiftState,
                    action = ShiftAction.AutoSetOneShot,
                    monotonicNow = monotonicClock
                )
                newShiftMode = newShiftState.mode
                lastLatinShiftMode = newShiftState.mode
            }
            state.copy(
                rows = updatedRows,
                activeRowId = newId,
                activeField = ActiveField.TITLE,
                keyboardMode = JournalKeyboardMode.TEXT,
                keyboardExpanded = true,
                shiftState = newShiftState,
                shiftMode = newShiftMode,
                pendingFocusRowId = newId,
                isDirty = true
            )
        }
        scheduleDraftSave()
    }

    fun removeRow(id: Long) {
        _uiState.update { state ->
            val filtered = state.rows.filter { it.id != id }
            val updatedRows = filtered.ifEmpty {
                val freshId = nextRowId++
                listOf(EditorRowUiState(id = freshId))
            }
            val newActiveRowId = if (state.activeRowId == id) null else state.activeRowId
            val newActiveField = if (state.activeRowId == id) ActiveField.NONE else state.activeField
            val newKeyboardMode = if (state.activeRowId == id) JournalKeyboardMode.NONE else state.keyboardMode
            val newKeyboardExpanded = if (state.activeRowId == id) false else state.keyboardExpanded
            state.copy(
                rows = updatedRows,
                activeRowId = newActiveRowId,
                activeField = newActiveField,
                keyboardMode = newKeyboardMode,
                keyboardExpanded = newKeyboardExpanded,
                isDirty = true
            )
        }
        scheduleDraftSave()
    }

    fun confirmRowEdit(id: Long) {
        _uiState.update { state ->
            state.copy(
                activeRowId = null,
                activeField = ActiveField.NONE,
                keyboardMode = JournalKeyboardMode.NONE,
                keyboardExpanded = false,
                shiftState = JournalShiftState(mode = JournalShiftMode.OFF),
                shiftMode = JournalShiftMode.OFF
            )
        }
    }

    fun selectRowField(rowId: Long, field: ActiveField) {
        _uiState.update { state ->
            when (field) {
                ActiveField.TITLE -> {
                    var newShiftState = state.shiftState
                    var newShiftMode = state.shiftMode
                    val row = state.rows.firstOrNull { it.id == rowId }
                    if (row != null && row.title.text.isEmpty() && state.keyboardLanguage != JournalKeyboardLanguage.ARABIC) {
                        newShiftState = JournalKeyboardController.reduceShift(
                            state = state.shiftState,
                            action = ShiftAction.AutoSetOneShot,
                            monotonicNow = monotonicClock
                        )
                        newShiftMode = newShiftState.mode
                        lastLatinShiftMode = newShiftState.mode
                    }
                    state.copy(
                        activeRowId = rowId,
                        activeField = ActiveField.TITLE,
                        keyboardMode = JournalKeyboardMode.TEXT,
                        keyboardExpanded = true,
                        shiftState = newShiftState,
                        shiftMode = newShiftMode,
                        pendingFocusRowId = rowId
                    )
                }
                ActiveField.AMOUNT -> {
                    state.copy(
                        activeRowId = rowId,
                        activeField = ActiveField.AMOUNT,
                        keyboardMode = JournalKeyboardMode.NUMBER,
                        keyboardExpanded = true,
                        pendingFocusRowId = rowId
                    )
                }
                ActiveField.NONE -> {
                    state.copy(
                        activeRowId = null,
                        activeField = ActiveField.NONE,
                        keyboardMode = JournalKeyboardMode.NONE,
                        keyboardExpanded = false
                    )
                }
            }
        }
    }

    fun selectUnit(unit: MoneyUnit) {
        _uiState.update { state ->
            if (unit == state.currency) return@update state
            val convertedRows = state.rows.map { row ->
                val converted = MoneyMath.convertExpression(row.amount.text, state.currency, unit)
                row.copy(
                    amount = TextFieldValue(text = converted, selection = TextRange(converted.length))
                )
            }
            state.copy(
                currency = unit,
                rows = convertedRows,
                isDirty = true
            )
        }
        scheduleDraftSave()
    }

    fun applyTextKey(text: String) {
        val state = _uiState.value
        val targetId = state.activeRowId ?: return
        val row = state.rows.firstOrNull { it.id == targetId } ?: return
        val newVal = JournalKeyboardController.insertText(row.title, text)

        var newShiftState = state.shiftState
        var newShiftMode = state.shiftMode
        if (state.keyboardLanguage != JournalKeyboardLanguage.ARABIC) {
            newShiftState = JournalKeyboardController.reduceShift(
                state = state.shiftState,
                action = ShiftAction.UserTypedText(text),
                monotonicNow = monotonicClock
            )
            newShiftMode = newShiftState.mode
            lastLatinShiftMode = newShiftState.mode
        }

        _uiState.update { s ->
            val updated = s.rows.map { if (it.id == targetId) it.copy(title = newVal) else it }
            s.copy(rows = updated, shiftState = newShiftState, shiftMode = newShiftMode, isDirty = true)
        }
        scheduleDraftSave()
    }

    fun applyTextBackspace() {
        val state = _uiState.value
        val targetId = state.activeRowId ?: return
        val row = state.rows.firstOrNull { it.id == targetId } ?: return
        val newVal = JournalKeyboardController.deleteBackward(row.title, graphemeSegmenter)
        _uiState.update { s ->
            val updated = s.rows.map { if (it.id == targetId) it.copy(title = newVal) else it }
            s.copy(rows = updated, isDirty = true)
        }
        scheduleDraftSave()
    }

    fun applyCompactKey(key: String) {
        val state = _uiState.value
        val targetId = state.activeRowId ?: return
        val row = state.rows.firstOrNull { it.id == targetId } ?: return
        val curVal = row.amount
        val newVal = if (key == "⌫") {
            JournalKeyboardController.deleteBackward(curVal, graphemeSegmenter)
        } else {
            val curText = curVal.text
            if (key == "." && curText.contains('.')) {
                curVal
            } else {
                JournalKeyboardController.insertText(curVal, key)
            }
        }
        _uiState.update { s ->
            val updated = s.rows.map { if (it.id == targetId) it.copy(amount = newVal) else it }
            s.copy(rows = updated, isDirty = true)
        }
        scheduleDraftSave()
    }

    fun cycleLanguage() {
        _uiState.update { state ->
            val nextLang = JournalKeyboardController.nextLanguage(state.keyboardLanguage)
            var newShiftState = state.shiftState
            var newShiftMode = state.shiftMode
            if (state.keyboardLanguage == JournalKeyboardLanguage.ARABIC && nextLang != JournalKeyboardLanguage.ARABIC) {
                newShiftState = newShiftState.copy(mode = lastLatinShiftMode)
                newShiftMode = lastLatinShiftMode
            } else if (state.keyboardLanguage != JournalKeyboardLanguage.ARABIC && nextLang == JournalKeyboardLanguage.ARABIC) {
                lastLatinShiftMode = newShiftState.mode
                newShiftMode = JournalShiftMode.OFF
            }
            state.copy(keyboardLanguage = nextLang, shiftState = newShiftState, shiftMode = newShiftMode)
        }
    }

    fun selectLanguage(lang: JournalKeyboardLanguage) {
        _uiState.update { state ->
            var newShiftState = state.shiftState
            var newShiftMode = state.shiftMode
            if (state.keyboardLanguage != JournalKeyboardLanguage.ARABIC && lang == JournalKeyboardLanguage.ARABIC) {
                lastLatinShiftMode = newShiftState.mode
                newShiftMode = JournalShiftMode.OFF
            } else if (state.keyboardLanguage == JournalKeyboardLanguage.ARABIC && lang != JournalKeyboardLanguage.ARABIC) {
                newShiftState = newShiftState.copy(mode = lastLatinShiftMode)
                newShiftMode = lastLatinShiftMode
            }
            state.copy(keyboardLanguage = lang, shiftState = newShiftState, shiftMode = newShiftMode)
        }
    }

    fun toggleShift() {
        _uiState.update { state ->
            if (state.keyboardLanguage != JournalKeyboardLanguage.ARABIC) {
                val newShiftState = JournalKeyboardController.reduceShift(
                    state = state.shiftState,
                    action = ShiftAction.UserTapShift(monotonicClock()),
                    monotonicNow = monotonicClock
                )
                lastLatinShiftMode = newShiftState.mode
                state.copy(shiftState = newShiftState, shiftMode = newShiftState.mode)
            } else {
                state
            }
        }
    }

    fun switchToTextMode() {
        val state = _uiState.value
        val targetId = state.activeRowId ?: state.rows.firstOrNull()?.id ?: return
        selectRowField(targetId, ActiveField.TITLE)
    }

    fun switchToNumericMode() {
        val state = _uiState.value
        val targetId = state.activeRowId ?: state.rows.firstOrNull()?.id ?: return
        selectRowField(targetId, ActiveField.AMOUNT)
    }

    fun openCalculatorPopup(rowId: Long? = null) {
        _uiState.update { state ->
            val target = rowId ?: state.activeRowId ?: state.rows.firstOrNull()?.id
            state.copy(
                calculator = CalculatorPopupState(
                    isVisible = true,
                    targetRowId = target,
                    expression = "",
                    result = "",
                    hasError = false,
                    isEvaluated = false
                )
            )
        }
    }

    fun closeCalculatorPopup() {
        _uiState.update { state ->
            state.copy(calculator = CalculatorPopupState(isVisible = false))
        }
    }

    fun applyPopupKey(key: String) {
        _uiState.update { state ->
            val calc = state.calculator
            val operators = setOf('+', '−', '×', '÷')
            var expr = calc.expression
            var res = calc.result
            var err = false
            var evaluated = false

            when (key) {
                "⌫" -> {
                    if (expr.isNotEmpty()) expr = expr.dropLast(1)
                    res = ""
                }
                "=" -> {
                    if (expr.isNotBlank()) {
                        val eval = MoneyMath.evaluate(expr)?.stripTrailingZeros()?.toPlainString()
                        if (eval != null && !eval.contains("NaN") && !eval.contains("Infinity")) {
                            res = eval
                            evaluated = true
                        } else {
                            err = true
                        }
                    }
                }
                "+", "−", "×", "÷" -> {
                    if (calc.isEvaluated && calc.result.isNotBlank()) {
                        expr = calc.result + key
                        res = ""
                    } else {
                        expr = when {
                            expr.isBlank() -> expr
                            expr.last() in operators -> expr.dropLast(1) + key
                            else -> expr + key
                        }
                        res = ""
                    }
                }
                "." -> {
                    if (calc.isEvaluated) {
                        expr = "0."
                        res = ""
                    } else {
                        res = ""
                        val lastPart = expr.split('+', '−', '×', '÷').lastOrNull().orEmpty()
                        expr = when {
                            lastPart.contains('.') -> expr
                            lastPart.isEmpty() -> expr + "0."
                            else -> expr + "."
                        }
                    }
                }
                else -> { // Digits 0-9
                    if (calc.isEvaluated) {
                        expr = key
                        res = ""
                    } else {
                        res = ""
                        val lastPart = expr.split('+', '−', '×', '÷').lastOrNull().orEmpty()
                        if (lastPart == "0") {
                            if (key != "0") expr = expr.dropLast(1) + key
                        } else if (expr.length < 32) {
                            expr += key
                        }
                    }
                }
            }

            state.copy(
                calculator = calc.copy(
                    expression = expr,
                    result = res,
                    hasError = err,
                    isEvaluated = evaluated
                )
            )
        }
    }

    fun confirmPopupResult() {
        val state = _uiState.value
        val calc = state.calculator
        if (!calc.isEvaluated || calc.result.isBlank() || calc.hasError) return
        val targetId = calc.targetRowId ?: return
        updateRowAmount(targetId, TextFieldValue(text = calc.result, selection = TextRange(calc.result.length)))
        _uiState.update { s ->
            s.copy(
                activeRowId = targetId,
                activeField = ActiveField.AMOUNT,
                calculator = CalculatorPopupState(isVisible = false)
            )
        }
    }

    fun toggleBreakdownSheet(show: Boolean) {
        _uiState.update { it.copy(showBreakdownSheet = show) }
    }

    fun dismissUnsavedDialog() {
        _uiState.update { it.copy(showUnsavedDialog = false) }
    }

    fun handleBackPress(onNavigateBack: () -> Unit) {
        val state = _uiState.value
        if (state.calculator.isVisible) {
            closeCalculatorPopup()
            return
        }
        if (state.showBreakdownSheet) {
            toggleBreakdownSheet(false)
            return
        }
        if (state.isDirty) {
            _uiState.update { it.copy(showUnsavedDialog = true) }
        } else {
            viewModelScope.launch {
                // Untouched new draft can be deleted safely
                if (state.mode == EditorMode.NEW && state.title.text.isBlank() && state.rows.all { it.isEmpty }) {
                    state.calculationId?.let { calculationRepository.deleteDraft(it) }
                }
                onNavigateBack()
            }
        }
    }

    fun discardChanges(onNavigateBack: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(showUnsavedDialog = false, isDirty = false) }
            val draftId = if (state.mode == EditorMode.EXISTING) {
                "draft_" + (state.editingSavedId ?: "")
            } else {
                state.calculationId ?: ""
            }
            if (draftId.isNotBlank()) {
                calculationRepository.deleteDraft(draftId)
            }
            onNavigateBack()
        }
    }

    fun saveCalculation(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isSaving = true) }

            val titleText = state.title.text.trim().ifEmpty { "Calcul" }
            val targetId = state.editingSavedId ?: state.calculationId ?: UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val populatedRows = state.rows.filter { it.isPopulated }
            val itemEntities = populatedRows.mapIndexed { idx, row ->
                val centimes = MoneyMath.toCentimes(row.amount.text, state.currency) ?: 0L
                CalculationItemEntity(
                    id = UUID.randomUUID().toString(),
                    calculationId = targetId,
                    label = row.title.text.trim(),
                    amountCentimes = centimes,
                    rawExpression = row.amount.text.trim(),
                    position = idx,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now
                )
            }

            val calculationEntity = CalculationEntity(
                id = targetId,
                title = titleText,
                currency = state.currency.name,
                createdAtEpochMs = now,
                updatedAtEpochMs = now,
                status = "SAVED",
                editingCalculationId = null
            )

            calculationRepository.saveCalculation(calculationEntity, itemEntities)

            // Cancel any pending draft saves
            draftSaveJob?.cancel()

            _uiState.update {
                it.copy(
                    calculationId = targetId,
                    editingSavedId = targetId,
                    mode = EditorMode.EXISTING,
                    isSaving = false,
                    isDirty = false,
                    isSaved = true
                )
            }
            onSuccess?.invoke()
        }
    }

    private fun scheduleDraftSave() {
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            delay(600)
            val state = _uiState.value
            if (!state.isDirty) return@launch

            val now = System.currentTimeMillis()
            val draftId: String
            val editingSavedId: String?

            if (state.mode == EditorMode.EXISTING && state.editingSavedId != null) {
                draftId = "draft_" + state.editingSavedId
                editingSavedId = state.editingSavedId
            } else {
                draftId = state.calculationId ?: UUID.randomUUID().toString()
                editingSavedId = null
            }

            val itemEntities = state.rows.mapIndexed { idx, row ->
                val centimes = MoneyMath.toCentimes(row.amount.text, state.currency) ?: 0L
                CalculationItemEntity(
                    id = UUID.randomUUID().toString(),
                    calculationId = draftId,
                    label = row.title.text,
                    amountCentimes = centimes,
                    rawExpression = row.amount.text,
                    position = idx,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now
                )
            }

            val draftEntity = CalculationEntity(
                id = draftId,
                title = state.title.text,
                currency = state.currency.name,
                createdAtEpochMs = now,
                updatedAtEpochMs = now,
                status = "DRAFT",
                editingCalculationId = editingSavedId
            )

            calculationRepository.saveDraft(draftEntity, itemEntities)
        }
    }
}

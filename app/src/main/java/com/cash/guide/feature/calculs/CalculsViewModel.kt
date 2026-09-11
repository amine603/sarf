package com.cash.guide.feature.calculs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.R
import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.SettingsRepository
import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.CalculationDateGroup
import com.cash.guide.domain.DateGroupHelper
import com.cash.guide.domain.reminder.CreditReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

data class CalculsUiState(
    val searchQuery: String = "",
    val selectedDateEpoch: Long? = null,
    val selectedYear: Int? = null,
    val selectedMonth: Int? = null,
    val allDateGroups: List<CalculationDateGroup> = emptyList(),
    val filteredDateGroups: List<CalculationDateGroup> = emptyList(),
    val pinnedCalculationIds: Set<String> = emptySet(),
    val selectedCalculationForAction: CalculationWithItems? = null,
    val calculationToDelete: CalculationWithItems? = null,
    val isLoading: Boolean = true
) {
    val isFiltering: Boolean
        get() = searchQuery.isNotBlank() || selectedDateEpoch != null || selectedYear != null

    val displayDateGroups: List<CalculationDateGroup>
        get() = if (isFiltering) filteredDateGroups else allDateGroups

    val isEmpty: Boolean
        get() = displayDateGroups.isEmpty() || displayDateGroups.all { it.calculations.isEmpty() }
}

class CalculsViewModel(
    val repository: CalculationRepository,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculsUiState())
    val uiState: StateFlow<CalculsUiState> = _uiState.asStateFlow()

    private var allItems: List<CalculationWithItems> = emptyList()
    private var lastContext: Context? = null

    init {
        if (settingsRepository != null) {
            viewModelScope.launch {
                settingsRepository.pinnedCalculationIds.collect { pinned ->
                    _uiState.update { it.copy(pinnedCalculationIds = pinned) }
                }
            }
        }
    }

    fun loadCalculations(context: Context) {
        lastContext = context
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeAllSaved().collect { items ->
                allItems = items
                applyFilters(context)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        lastContext?.let { applyFilters(it) }
    }

    fun filterByDate(epochMs: Long?) {
        _uiState.update { it.copy(selectedDateEpoch = epochMs, selectedYear = null, selectedMonth = null) }
        lastContext?.let { applyFilters(it) }
    }

    fun filterByMonth(year: Int, month: Int) {
        _uiState.update { it.copy(selectedYear = year, selectedMonth = month, selectedDateEpoch = null) }
        lastContext?.let { applyFilters(it) }
    }

    fun clearDateFilter() {
        _uiState.update { it.copy(selectedDateEpoch = null, selectedYear = null, selectedMonth = null) }
        lastContext?.let { applyFilters(it) }
    }

    fun togglePin(calculationId: String) {
        viewModelScope.launch {
            settingsRepository?.togglePinCalculation(calculationId)
        }
    }

    fun togglePaymentStatus(calculationId: String, currentStatus: String) {
        val nextStatus = if (currentStatus == "PAID") "UNPAID" else "PAID"
        viewModelScope.launch {
            repository.updatePaymentStatus(calculationId, nextStatus)
            if (nextStatus == "PAID") {
                lastContext?.let { CreditReminderScheduler.cancelReminder(it, calculationId) }
            }
        }
    }

    fun toggleCalcType(calculationId: String, currentType: String) {
        val nextType = if (currentType == "CREDIT") "PERSONNEL" else "CREDIT"
        val nextStatus = if (nextType == "CREDIT") "UNPAID" else "PAID"
        viewModelScope.launch {
            repository.updateCalcType(calculationId, nextType)
            repository.updatePaymentStatus(calculationId, nextStatus)
            if (nextType == "PERSONNEL") {
                lastContext?.let { CreditReminderScheduler.cancelReminder(it, calculationId) }
            }
        }
    }

    fun selectCalculationForAction(calc: CalculationWithItems?) {
        _uiState.update { it.copy(selectedCalculationForAction = calc) }
    }

    fun requestDelete(calc: CalculationWithItems) {
        _uiState.update {
            it.copy(
                selectedCalculationForAction = null,
                calculationToDelete = calc
            )
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(calculationToDelete = null) }
    }

    fun confirmDelete() {
        val toDelete = _uiState.value.calculationToDelete ?: return
        viewModelScope.launch {
            repository.deleteCalculation(toDelete.calculation.id)
            _uiState.update { it.copy(calculationToDelete = null) }
        }
    }

    fun duplicateCalculation(calc: CalculationWithItems, onDuplicated: (String) -> Unit) {
        viewModelScope.launch {
            val duplicated = repository.duplicateCalculation(calc.calculation.id)
            if (duplicated != null) {
                onDuplicated(duplicated.calculation.id)
            }
        }
    }


    private fun applyFilters(context: Context) {
        val query = _uiState.value.searchQuery.trim()
        val selectedDate = _uiState.value.selectedDateEpoch
        val selYear = _uiState.value.selectedYear
        val selMonth = _uiState.value.selectedMonth

        val allGroups = DateGroupHelper.groupByDate(
            items = allItems,
            todayString = context.getString(R.string.date_today),
            yesterdayString = context.getString(R.string.date_yesterday),
            thisWeekString = context.getString(R.string.date_this_week),
            locale = context.resources.configuration.locales[0]
        )

        if (query.isBlank() && selectedDate == null && selYear == null) {
            _uiState.update {
                it.copy(
                    allDateGroups = allGroups,
                    filteredDateGroups = emptyList(),
                    isLoading = false
                )
            }
            return
        }

        var filtered = allItems

        if (query.isNotBlank()) {
            val q = query.lowercase()
            filtered = filtered.filter { item ->
                item.calculation.title.lowercase().contains(q) ||
                item.items.any { it.label.lowercase().contains(q) }
            }
        }

        if (selectedDate != null) {
            val zone = ZoneId.systemDefault()
            val targetLocalDate = Instant.ofEpochMilli(selectedDate).atZone(zone).toLocalDate()
            filtered = filtered.filter { item ->
                val itemLocalDate = Instant.ofEpochMilli(item.calculation.updatedAtEpochMs).atZone(zone).toLocalDate()
                itemLocalDate.isEqual(targetLocalDate)
            }
        } else if (selYear != null && selMonth != null) {
            val zone = ZoneId.systemDefault()
            filtered = filtered.filter { item ->
                val itemLocalDate = Instant.ofEpochMilli(item.calculation.updatedAtEpochMs).atZone(zone).toLocalDate()
                itemLocalDate.year == selYear && itemLocalDate.monthValue == selMonth
            }
        }

        val filteredGroups = DateGroupHelper.groupByDate(
            items = filtered,
            todayString = context.getString(R.string.date_today),
            yesterdayString = context.getString(R.string.date_yesterday),
            thisWeekString = context.getString(R.string.date_this_week),
            locale = context.resources.configuration.locales[0]
        )

        _uiState.update {
            it.copy(
                allDateGroups = allGroups,
                filteredDateGroups = filteredGroups,
                isLoading = false
            )
        }
    }
}

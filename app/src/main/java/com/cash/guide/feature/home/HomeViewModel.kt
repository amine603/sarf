package com.cash.guide.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.R
import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.DateGroupHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: CalculationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allItems: List<CalculationWithItems> = emptyList()
    private var lastContext: Context? = null

    fun loadRecent(context: Context) {
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
        _uiState.update { it.copy(selectedDateEpoch = epochMs) }
        lastContext?.let { applyFilters(it) }
    }

    fun clearDateFilter() {
        filterByDate(null)
    }

    private fun applyFilters(context: Context) {
        val query = _uiState.value.searchQuery.trim()
        val selectedDate = _uiState.value.selectedDateEpoch

        val recentGroups = DateGroupHelper.groupByDate(
            items = allItems.take(10),
            todayString = context.getString(R.string.date_today),
            yesterdayString = context.getString(R.string.date_yesterday),
            thisWeekString = context.getString(R.string.date_this_week),
            locale = context.resources.configuration.locales[0]
        )

        if (query.isBlank() && selectedDate == null) {
            _uiState.update {
                it.copy(
                    recentDateGroups = recentGroups,
                    filteredDateGroups = emptyList(),
                    isLoading = false
                )
            }
        } else {
            val filtered = allItems.filter { calc ->
                val matchesQuery = if (query.isBlank()) true else {
                    calc.calculation.title.contains(query, ignoreCase = true) ||
                    calc.items.any { it.label.contains(query, ignoreCase = true) }
                }
                val matchesDate = if (selectedDate == null) true else {
                    isSameDay(calc.calculation.updatedAtEpochMs, selectedDate)
                }
                matchesQuery && matchesDate
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
                    recentDateGroups = recentGroups,
                    filteredDateGroups = filteredGroups,
                    isLoading = false
                )
            }
        }
    }

    private fun isSameDay(epoch1: Long, epoch2: Long): Boolean {
        val c1 = java.util.Calendar.getInstance().apply { timeInMillis = epoch1 }
        val c2 = java.util.Calendar.getInstance().apply { timeInMillis = epoch2 }
        return c1.get(java.util.Calendar.YEAR) == c2.get(java.util.Calendar.YEAR) &&
               c1.get(java.util.Calendar.DAY_OF_YEAR) == c2.get(java.util.Calendar.DAY_OF_YEAR)
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
}

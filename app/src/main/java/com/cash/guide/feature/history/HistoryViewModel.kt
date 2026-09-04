package com.cash.guide.feature.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.R
import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.DateGroupHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: CalculationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun loadAll(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeAllSaved().collect { items ->
                val groups = DateGroupHelper.groupByDate(
                    items = items,
                    todayString = context.getString(R.string.date_today),
                    yesterdayString = context.getString(R.string.date_yesterday),
                    thisWeekString = context.getString(R.string.date_this_week),
                    locale = context.resources.configuration.locales[0]
                )
                _uiState.update { it.copy(allDateGroups = groups, isLoading = false) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearching = query.isNotBlank()) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
        } else {
            searchJob = viewModelScope.launch {
                repository.searchSaved(query).collect { results ->
                    _uiState.update { it.copy(searchResults = results) }
                }
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
}

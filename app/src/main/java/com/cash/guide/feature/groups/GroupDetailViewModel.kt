package com.cash.guide.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.db.CalculationGroupWithCalculations
import com.cash.guide.data.db.CalculationWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val groupWithCalculations: CalculationGroupWithCalculations? = null,
    val isLoading: Boolean = true,
    val selectedCalculationForAction: CalculationWithItems? = null,
    val calculationToDelete: CalculationWithItems? = null
)

class GroupDetailViewModel(
    val groupId: String,
    private val calculationRepository: CalculationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            calculationRepository.observeGroupWithCalculations(groupId).collectLatest { data ->
                _uiState.value = _uiState.value.copy(
                    groupWithCalculations = data,
                    isLoading = false
                )
            }
        }
    }

    fun selectCalculationForAction(calc: CalculationWithItems?) {
        _uiState.value = _uiState.value.copy(selectedCalculationForAction = calc)
    }

    fun promptDeleteCalculation(calc: CalculationWithItems) {
        _uiState.value = _uiState.value.copy(
            selectedCalculationForAction = null,
            calculationToDelete = calc
        )
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(calculationToDelete = null)
    }

    fun confirmDeleteCalculation() {
        val calc = _uiState.value.calculationToDelete ?: return
        viewModelScope.launch {
            calculationRepository.deleteCalculation(calc.calculation.id)
            dismissDeleteDialog()
        }
    }

    fun removeCalculationFromGroup(calcId: String) {
        viewModelScope.launch {
            calculationRepository.assignCalculationToGroup(calcId, null)
            _uiState.value = _uiState.value.copy(selectedCalculationForAction = null)
        }
    }

    fun duplicateCalculation(calcId: String) {
        viewModelScope.launch {
            calculationRepository.duplicateCalculation(calcId)
            _uiState.value = _uiState.value.copy(selectedCalculationForAction = null)
        }
    }
}

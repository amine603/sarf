package com.cash.guide.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.db.CalculationGroupEntity
import com.cash.guide.data.db.CalculationGroupWithCalculations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class GroupsUiState(
    val groups: List<CalculationGroupWithCalculations> = emptyList(),
    val isLoading: Boolean = true,
    val isCreateOrEditDialogOpen: Boolean = false,
    val editingGroup: CalculationGroupEntity? = null,
    val groupNameInput: String = "",
    val selectedColorHex: String = "#F4D66D",
    val groupToDelete: CalculationGroupEntity? = null
)

class GroupsViewModel(
    private val calculationRepository: CalculationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            calculationRepository.observeAllGroupsWithCalculations().collectLatest { list ->
                _uiState.value = _uiState.value.copy(
                    groups = list,
                    isLoading = false
                )
            }
        }
    }

    fun openCreateDialog() {
        _uiState.value = _uiState.value.copy(
            isCreateOrEditDialogOpen = true,
            editingGroup = null,
            groupNameInput = "",
            selectedColorHex = "#F4D66D"
        )
    }

    fun openEditDialog(group: CalculationGroupEntity) {
        _uiState.value = _uiState.value.copy(
            isCreateOrEditDialogOpen = true,
            editingGroup = group,
            groupNameInput = group.name,
            selectedColorHex = group.colorHex
        )
    }

    fun updateGroupNameInput(name: String) {
        _uiState.value = _uiState.value.copy(groupNameInput = name)
    }

    fun selectColorHex(hex: String) {
        _uiState.value = _uiState.value.copy(selectedColorHex = hex)
    }

    fun dismissCreateOrEditDialog() {
        _uiState.value = _uiState.value.copy(
            isCreateOrEditDialogOpen = false,
            editingGroup = null,
            groupNameInput = ""
        )
    }

    fun saveGroup() {
        val name = _uiState.value.groupNameInput.trim()
        if (name.isBlank()) return

        val editing = _uiState.value.editingGroup
        val colorHex = _uiState.value.selectedColorHex

        viewModelScope.launch {
            if (editing != null) {
                calculationRepository.updateGroup(editing.copy(name = name, colorHex = colorHex))
            } else {
                calculationRepository.createGroup(name, colorHex)
            }
            dismissCreateOrEditDialog()
        }
    }

    fun promptDeleteGroup(group: CalculationGroupEntity) {
        _uiState.value = _uiState.value.copy(groupToDelete = group)
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(groupToDelete = null)
    }

    fun confirmDeleteGroup() {
        val group = _uiState.value.groupToDelete ?: return
        viewModelScope.launch {
            calculationRepository.deleteGroup(group.id)
            dismissDeleteDialog()
        }
    }
}

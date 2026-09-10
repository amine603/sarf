package com.cash.guide.feature.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.data.ChecklistRepository
import com.cash.guide.data.db.ChecklistWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChecklistsOverviewUiState(
    val showCreateDialog: Boolean = false,
    val newChecklistTitle: String = "",
    val checklistToDelete: ChecklistWithItems? = null
)

class ChecklistsOverviewViewModel(
    private val checklistRepository: ChecklistRepository
) : ViewModel() {

    val checklists: StateFlow<List<ChecklistWithItems>> = checklistRepository.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(ChecklistsOverviewUiState())
    val uiState: StateFlow<ChecklistsOverviewUiState> = _uiState.asStateFlow()

    fun openCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true, newChecklistTitle = "") }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false, newChecklistTitle = "") }
    }

    fun setNewChecklistTitle(title: String) {
        _uiState.update { it.copy(newChecklistTitle = title) }
    }

    fun confirmCreateChecklist(onCreated: (String) -> Unit) {
        val title = _uiState.value.newChecklistTitle.trim().ifBlank { "Checklist" }
        viewModelScope.launch {
            val newId = checklistRepository.createChecklist(title)
            _uiState.update { it.copy(showCreateDialog = false, newChecklistTitle = "") }
            onCreated(newId)
        }
    }

    fun promptDeleteChecklist(checklist: ChecklistWithItems) {
        _uiState.update { it.copy(checklistToDelete = checklist) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(checklistToDelete = null) }
    }

    fun confirmDeleteChecklist() {
        val target = _uiState.value.checklistToDelete ?: return
        viewModelScope.launch {
            checklistRepository.deleteChecklist(target.checklist.id)
            _uiState.update { it.copy(checklistToDelete = null) }
        }
    }
}

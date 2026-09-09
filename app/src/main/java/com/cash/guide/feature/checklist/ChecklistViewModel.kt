package com.cash.guide.feature.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.data.ChecklistRepository
import com.cash.guide.data.db.ChecklistWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChecklistUiState(
    val allChecklists: List<ChecklistWithItems> = emptyList(),
    val currentChecklist: ChecklistWithItems? = null,
    val inputText: String = "",
    val isTitleEditing: Boolean = false,
    val titleInput: String = "",
    val isLoading: Boolean = true,
    val showChecklistListDialog: Boolean = false,
    val showCreateNewDialog: Boolean = false,
    val newChecklistTitle: String = ""
)

class ChecklistViewModel(
    private val checklistRepository: ChecklistRepository,
    initialChecklistId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()

    private var activeId: String? = initialChecklistId

    init {
        viewModelScope.launch {
            checklistRepository.observeAll().collectLatest { all ->
                val target = if (activeId != null) {
                    all.find { it.checklist.id == activeId } ?: all.firstOrNull()
                } else {
                    all.firstOrNull()
                }

                if (target == null && all.isEmpty()) {
                    // Automatically create default initial checklist if none exists
                    val newId = checklistRepository.createChecklist("Checklist")
                    activeId = newId
                } else {
                    activeId = target?.checklist?.id
                    _uiState.value = _uiState.value.copy(
                        allChecklists = all,
                        currentChecklist = target,
                        titleInput = target?.checklist?.title ?: "Checklist",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectChecklist(id: String) {
        activeId = id
        val target = _uiState.value.allChecklists.find { it.checklist.id == id }
        _uiState.value = _uiState.value.copy(
            currentChecklist = target,
            titleInput = target?.checklist?.title ?: "Checklist",
            showChecklistListDialog = false
        )
    }

    fun setInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun addItem() {
        val text = _uiState.value.inputText.trim()
        val current = _uiState.value.currentChecklist ?: return
        if (text.isEmpty()) return
        viewModelScope.launch {
            checklistRepository.addItem(current.checklist.id, text)
            _uiState.value = _uiState.value.copy(inputText = "")
        }
    }

    fun toggleItem(itemId: String, isChecked: Boolean) {
        viewModelScope.launch {
            checklistRepository.toggleItem(itemId, isChecked)
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            checklistRepository.deleteItem(itemId)
        }
    }

    fun setAllItemsChecked(isChecked: Boolean) {
        val current = _uiState.value.currentChecklist ?: return
        viewModelScope.launch {
            checklistRepository.setAllItemsChecked(current.checklist.id, isChecked)
        }
    }

    fun deleteCompletedItems() {
        val current = _uiState.value.currentChecklist ?: return
        viewModelScope.launch {
            checklistRepository.deleteCompletedItems(current.checklist.id)
        }
    }

    fun startEditingTitle() {
        _uiState.value = _uiState.value.copy(
            isTitleEditing = true,
            titleInput = _uiState.value.currentChecklist?.checklist?.title ?: "Checklist"
        )
    }

    fun setTitleInput(text: String) {
        _uiState.value = _uiState.value.copy(titleInput = text)
    }

    fun saveTitle() {
        val current = _uiState.value.currentChecklist ?: return
        val newTitle = _uiState.value.titleInput.trim().ifBlank { "Checklist" }
        viewModelScope.launch {
            checklistRepository.updateTitle(current.checklist.id, newTitle)
            _uiState.value = _uiState.value.copy(isTitleEditing = false)
        }
    }

    fun openCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateNewDialog = true, newChecklistTitle = "")
    }

    fun setNewChecklistTitle(title: String) {
        _uiState.value = _uiState.value.copy(newChecklistTitle = title)
    }

    fun confirmCreateChecklist() {
        val title = _uiState.value.newChecklistTitle.trim().ifBlank { "Checklist" }
        viewModelScope.launch {
            val newId = checklistRepository.createChecklist(title)
            activeId = newId
            _uiState.value = _uiState.value.copy(showCreateNewDialog = false)
        }
    }

    fun dismissCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateNewDialog = false)
    }

    fun showListsDialog() {
        _uiState.value = _uiState.value.copy(showChecklistListDialog = true)
    }

    fun dismissListsDialog() {
        _uiState.value = _uiState.value.copy(showChecklistListDialog = false)
    }

    fun deleteCurrentChecklist() {
        val current = _uiState.value.currentChecklist ?: return
        viewModelScope.launch {
            checklistRepository.deleteChecklist(current.checklist.id)
            activeId = null
        }
    }

    fun importFromLink(title: String, items: List<Pair<String, Boolean>>) {
        viewModelScope.launch {
            val newId = checklistRepository.importChecklist(title, items)
            activeId = newId
        }
    }
}

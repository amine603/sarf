package com.cash.guide.feature.note

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.data.NoteRepository
import com.cash.guide.data.db.NoteEntity
import com.cash.guide.domain.speech.SpeechRecognitionState
import com.cash.guide.domain.speech.SpeechRecognizerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val colorTag: String = "DEFAULT",
    val isPinned: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val isListening: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = true
)

class NoteViewModel(
    private val noteRepository: NoteRepository,
    private val noteId: String?,
    context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private val speechHelper = SpeechRecognizerHelper(context)

    init {
        speechHelper.onSpeechResult = { text ->
            if (text.isNotBlank()) {
                appendDictatedText(text)
            }
        }

        viewModelScope.launch {
            speechHelper.state.collect { st ->
                _uiState.value = _uiState.value.copy(
                    isListening = (st == SpeechRecognitionState.LISTENING || st == SpeechRecognitionState.PROCESSING)
                )
            }
        }

        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            if (!noteId.isNullOrBlank()) {
                val existing = noteRepository.getNote(noteId)
                if (existing != null) {
                    _uiState.value = NoteEditorUiState(
                        id = existing.id,
                        title = existing.title,
                        content = existing.content,
                        colorTag = existing.colorTag,
                        isPinned = existing.isPinned,
                        createdAtEpochMs = existing.createdAtEpochMs,
                        isLoading = false
                    )
                    return@launch
                }
            }

            // If new note
            val newId = noteId ?: java.util.UUID.randomUUID().toString()
            _uiState.value = NoteEditorUiState(
                id = newId,
                isLoading = false
            )
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
        saveChanges()
    }

    fun updateContent(newContent: String) {
        _uiState.value = _uiState.value.copy(content = newContent)
        saveChanges()
    }

    fun setColorTag(colorTag: String) {
        _uiState.value = _uiState.value.copy(colorTag = colorTag)
        saveChanges()
    }

    fun togglePin() {
        val newPinned = !_uiState.value.isPinned
        _uiState.value = _uiState.value.copy(isPinned = newPinned)
        saveChanges()
    }

    fun startListening() {
        speechHelper.startListening()
    }

    fun stopListening() {
        speechHelper.stopListening()
    }

    private fun appendDictatedText(text: String) {
        val current = _uiState.value.content
        val updated = if (current.isBlank()) {
            text.trim()
        } else {
            current.trimEnd() + " " + text.trim()
        }
        _uiState.value = _uiState.value.copy(content = updated)
        saveChanges()
    }

    fun saveChanges() {
        val current = _uiState.value
        if (current.id.isBlank()) return
        viewModelScope.launch {
            val existing = noteRepository.getNote(current.id)
            if (existing != null) {
                noteRepository.updateNote(
                    id = current.id,
                    title = current.title,
                    content = current.content,
                    colorTag = current.colorTag,
                    isPinned = current.isPinned
                )
            } else {
                noteRepository.createNote(
                    title = current.title,
                    content = current.content,
                    colorTag = current.colorTag
                )
            }
        }
    }

    fun deleteNote(onDeleted: () -> Unit) {
        val id = _uiState.value.id
        if (id.isNotBlank()) {
            viewModelScope.launch {
                noteRepository.deleteNote(id)
                onDeleted()
            }
        } else {
            onDeleted()
        }
    }

    fun getNoteEntity(): NoteEntity {
        val s = _uiState.value
        return NoteEntity(
            id = s.id,
            title = s.title,
            content = s.content,
            colorTag = s.colorTag,
            isPinned = s.isPinned,
            createdAtEpochMs = s.createdAtEpochMs,
            updatedAtEpochMs = System.currentTimeMillis()
        )
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper.destroy()
    }
}

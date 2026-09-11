package com.cash.guide.feature.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.data.NoteRepository
import com.cash.guide.data.db.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NoteMonthGroup(
    val monthYearKey: String,
    val displayTitle: String,
    val notes: List<NoteEntity>
)

data class NotesOverviewUiState(
    val searchQuery: String = "",
    val monthGroups: List<NoteMonthGroup> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

class NotesOverviewViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val uiState: StateFlow<NotesOverviewUiState> = combine(
        noteRepository.observeAll(),
        _searchQuery
    ) { allNotes, query ->
        val filtered = if (query.isBlank()) {
            allNotes
        } else {
            val q = query.trim().lowercase()
            allNotes.filter { note ->
                note.title.lowercase().contains(q) || note.content.lowercase().contains(q)
            }
        }

        // Group by Month and Year (e.g. "2026-03")
        val monthKeyFormatter = SimpleDateFormat("yyyy-MM", Locale.US)
        val monthDisplayFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        val groupedMap = filtered.groupBy { note ->
            monthKeyFormatter.format(Date(note.createdAtEpochMs))
        }

        val groups = groupedMap.map { (key, notesInMonth) ->
            val firstNoteDate = Date(notesInMonth.first().createdAtEpochMs)
            val rawTitle = monthDisplayFormatter.format(firstNoteDate)
            val displayTitle = rawTitle.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            NoteMonthGroup(
                monthYearKey = key,
                displayTitle = displayTitle,
                notes = notesInMonth
            )
        }

        NotesOverviewUiState(
            searchQuery = query,
            monthGroups = groups,
            totalCount = filtered.size,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesOverviewUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(id)
        }
    }

    fun togglePin(id: String, isPinned: Boolean) {
        viewModelScope.launch {
            noteRepository.togglePin(id, isPinned)
        }
    }

    suspend fun createNewNote(): String {
        return noteRepository.createNote()
    }
}

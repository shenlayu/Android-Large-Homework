package com.example.simplenote.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.NoteType
import com.example.simplenote.data.NotebookWithNotes
import com.example.simplenote.data.NotebooksRepository
import com.example.simplenote.data.NotesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// 主界面ViewModel, 提供所有notebook可供预览的列表
class DirectoryViewModel_ (
    notebooksRepository: NotebooksRepository,
    noteRepository: NotesRepository
): ViewModel() {
    val homeUiState: StateFlow<HomeUiState> =
        notebooksRepository.getAllNotebooksWithNotes().map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    // 指定notebook, note后获得信息
    fun getNoteType(notebookId: Int, noteId: Int): NoteType? {
        return homeUiState.value.notebookList[notebookId].notes[noteId].type
    }
    fun getNoteContent(notebookId: Int, noteId: Int): String {
        return homeUiState.value.notebookList[notebookId].notes[noteId].content
    }
}

data class HomeUiState(
    val notebookList: List<NotebookWithNotes> = listOf()
)
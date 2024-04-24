package com.example.simplenote.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.NotebookWithNotes
import com.example.simplenote.data.NotebooksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// 主界面ViewModel, 提供所有notebook可供预览的列表
class HomeViewModel (notebooksRepository: NotebooksRepository): ViewModel() {
    val homeUiState: StateFlow<HomeUiState> =
        notebooksRepository.getNotebooksWithNotes().map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class HomeUiState(
    val notebookList: List<NotebookWithNotes> = listOf()
)
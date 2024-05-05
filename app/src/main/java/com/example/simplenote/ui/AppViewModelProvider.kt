package com.example.simplenote.ui

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.simplenote.NoteApplication
import com.example.simplenote.ui.note.NoteViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for [NoteViewModel]
        initializer {
            NoteViewModel(NoteApplication().container.notebooksRepository)
        }
    }
}
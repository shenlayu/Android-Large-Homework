package com.example.simplenote.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.simplenote.NoteApplication
import com.example.simplenote.ui.note.NoteViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for [NoteViewModel]
        initializer {
            NoteViewModel(
                noteApplication().container.notebooksRepository,
                noteApplication().container.notesRepository
                )
        }
    }
}

fun CreationExtras.noteApplication(): NoteApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NoteApplication)
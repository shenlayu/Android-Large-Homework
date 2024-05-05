package com.example.simplenote.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.simplenote.NoteApplication
import com.example.simplenote.ui.note.DirectoryViewModel
import com.example.simplenote.ui.note.NoteViewModel
import com.example.simplenote.ui.note.UserViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for [NoteViewModel]
//        initializer {
//            NoteViewModel(
//                noteApplication().container.notebooksRepository,
//                noteApplication().container.notesRepository
//                )
//        }
//        // Initializer for [HomeViewModel]
//        initializer {
//            DirectoryViewModel(
//                noteApplication().container.notebooksRepository,
//                noteApplication().container.notesRepository
//            )
//        }
        initializer {
            UserViewModel(
                noteApplication().container.usersRepository,
            )
        }
        initializer {
            DirectoryViewModel(
                noteApplication().container.usersRepository,
                noteApplication().container.directoriesRepository
            )
        }
    }
}

fun CreationExtras.noteApplication(): NoteApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NoteApplication)
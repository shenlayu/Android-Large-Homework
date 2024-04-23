package com.example.simplenote.data

import android.content.Context

interface AppContainer {
    val notesRepository: NotesRepository
    val notebooksRepository: NotebooksRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineItemsRepository]
 */
// 将Dao赋给OfflineRepository的构造函数
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [NotesRepository]
     */
    override val notesRepository: NotesRepository by lazy {
        OfflineNotesRepository(NoteDatabase.getDatabase(context).noteDao())
    }
    override val notebooksRepository: NotebooksRepository by lazy {
        OfflineNotebooksRepository(NoteDatabase.getDatabase(context).notebookDao())
    }
}
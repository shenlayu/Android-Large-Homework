package com.example.simplenote.data

import android.content.Context

interface AppContainer {
    val usersRepository: UsersRepository
    val directoriesRepository: DirectoriesRepository
    val notebooksRepository: NotebooksRepository
    val notesRepository: NotesRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineItemsRepository]
 */
// 将Dao赋给OfflineRepository的构造函数
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [NotesRepository]
     */
    override val usersRepository: UsersRepository by lazy {
        OfflineUsersRepository(NoteDatabase.getDatabase(context).userDao())
    }
    override val directoriesRepository: DirectoriesRepository by lazy {
        OfflineDirectoriesRepository(NoteDatabase.getDatabase(context).directoryDao())
    }
    override val notebooksRepository: NotebooksRepository by lazy {
        OfflineNotebooksRepository(NoteDatabase.getDatabase(context).notebookDao())
    }
    override val notesRepository: NotesRepository by lazy {
        OfflineNotesRepository(NoteDatabase.getDatabase(context).noteDao())
    }
}
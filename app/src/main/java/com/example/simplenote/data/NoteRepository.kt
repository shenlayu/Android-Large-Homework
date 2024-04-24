package com.example.simplenote.data

import kotlinx.coroutines.flow.Flow

// 映射Dao的接口，将对database对操作封装为对用户的接口
interface NotebooksRepository {
    fun getAllNotebooksStream(): Flow<List<Notebook>>

    fun getNotebookStream(id: Int): Flow<Notebook?>

    fun getNotebooksWithNotes(): Flow<List<NotebookWithNotes>>

    suspend fun insertNotebook(notebook: Notebook)

    suspend fun deleteNotebook(notebook: Notebook)

    suspend fun updateNotebook(notebook: Notebook)
}

interface NotesRepository {
    fun getAllNotesStream(): Flow<List<Note>>

    fun getNoteStream(id: Int): Flow<Note?>

    suspend fun insertNote(note: Note)

    suspend fun deleteNote(note: Note)

    suspend fun updateNote(note: Note)
}
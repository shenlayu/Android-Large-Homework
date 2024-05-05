package com.example.simplenote.data

import androidx.room.Delete
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// 映射Dao的接口，将对database对操作封装为对用户的接口
interface UsersRepository {
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
    fun searchUserExisting(username: String): Flow<User>
}

interface DirectoriesRepository {
    suspend fun insertDirectory(directory: Directory)

    suspend fun updateDirectory(directory: Directory)

    suspend fun deleteDirectory(directory: Directory)
}

interface NotebooksRepository {
    fun getAllNotebooksStream(): Flow<List<Notebook>>

    fun getNotebookStream(id: Int): Flow<Notebook?>

    fun getNotebookWithNotes(id: Int): Flow<NotebookWithNotes>

    fun getAllNotebooksWithNotes(): Flow<List<NotebookWithNotes>>

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
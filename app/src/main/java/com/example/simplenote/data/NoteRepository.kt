package com.example.simplenote.data

import kotlinx.coroutines.flow.Flow

// 映射Dao的接口，将对database对操作封装为对用户的接口
interface UsersRepository {
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
    fun searchUser(username: String): Flow<User>
    fun searchUserById(id: Int): Flow<User>
    fun getUserWithDirectories(id : Int): Flow<UserWithDirectories>
}

interface DirectoriesRepository {
    suspend fun insertDirectory(directory: Directory)
    suspend fun updateDirectory(directory: Directory)
    suspend fun deleteDirectory(directory: Directory)
    fun getDirectory(id: Int): Flow<Directory>
    fun getDirectoryWithNotebooks(id: Int): Flow<DirectoryWithNotebooks>
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
    fun searchNote(content: String): Flow<List<Note>>
}

interface LoggedUserRepository {
    suspend fun insertLoggedUser(loggedUser: LoggedUser)
    suspend fun updateLoggedUser(loggedUser: LoggedUser)
    suspend fun deleteLoggedUser(loggedUser: LoggedUser)
    fun getLoggedUser(): Flow<List<LoggedUser>>
}
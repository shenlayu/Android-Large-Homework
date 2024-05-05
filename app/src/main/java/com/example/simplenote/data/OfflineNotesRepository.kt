package com.example.simplenote.data

import kotlinx.coroutines.flow.Flow

// 将Dao的接口赋值给暴露给用户的接口
class OfflineUsersRepository(private val userDao: UserDao) : UsersRepository {
    override suspend fun insertUser(user: User) = userDao.insert(user)
    override suspend fun deleteUser(user: User) = userDao.delete(user)
    override suspend fun updateUser(user: User) = userDao.update(user)
    override fun searchUser(username: String) = userDao.searchUser(username)
    override fun getNotebookWithNotes(id : Int) = userDao.getNotebookWithNotes(id)
}

class OfflineDirectoriesRepository(private val directoryDao: DirectoryDao) : DirectoriesRepository {
    override suspend fun insertDirectory(directory: Directory) = directoryDao.insert(directory)
    override suspend fun deleteDirectory(directory: Directory) = directoryDao.delete(directory)
    override suspend fun updateDirectory(directory: Directory) = directoryDao.update(directory)
    override fun getDirectory(id: Int) = directoryDao.getDirectory(id)
}

class OfflineNotebooksRepository(private val notebookDao: NotebookDao) : NotebooksRepository {
    override fun getAllNotebooksStream() = notebookDao.getAllItems()

    override fun getNotebookStream(id: Int) = notebookDao.getItem(id)

    override fun getNotebookWithNotes(id: Int) = notebookDao.getNotebookWithNotes(id)

    override fun getAllNotebooksWithNotes() = notebookDao.getAllNotebooksWithNotes()

    override suspend fun insertNotebook(notebook: Notebook) = notebookDao.insert(notebook)

    override suspend fun deleteNotebook(notebook: Notebook) = notebookDao.delete(notebook)

    override suspend fun updateNotebook(notebook: Notebook) = notebookDao.update(notebook)
}

class OfflineNotesRepository(private val noteDao: NoteDao) : NotesRepository {
    override fun getAllNotesStream() = noteDao.getAllNotes()

    override fun getNoteStream(id: Int) = noteDao.getNote(id)

    override suspend fun insertNote(note: Note) = noteDao.insert(note)

    override suspend fun deleteNote(note: Note) = noteDao.delete(note)

    override suspend fun updateNote(note: Note) = noteDao.update(note)
}
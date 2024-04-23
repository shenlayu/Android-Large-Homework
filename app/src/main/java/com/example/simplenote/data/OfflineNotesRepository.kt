package com.example.simplenote.data

import kotlinx.coroutines.flow.Flow

// 将Dao的接口赋值给暴露给用户的接口
class OfflineNotesRepository(private val noteDao: NoteDao) : NotesRepository {
    override fun getAllItemsStream(): Flow<List<Note>> = noteDao.getAllItems()

    override fun getItemStream(id: Int): Flow<Note?> = noteDao.getItem(id)

    override suspend fun insertItem(note: Note) = noteDao.insert(note)

    override suspend fun deleteItem(note: Note) = noteDao.delete(note)

    override suspend fun updateItem(note: Note) = noteDao.update(note)
}

class OfflineNotebooksRepository(private val notebookDao: NotebookDao) : NotebooksRepository {
    override fun getAllItemsStream(): Flow<List<Notebook>> = notebookDao.getAllItems()

    override fun getItemStream(id: Int): Flow<Notebook?> = notebookDao.getItem(id)

    override suspend fun insertItem(notebook: Notebook) = notebookDao.insert(notebook)

    override suspend fun deleteItem(notebook: Notebook) = notebookDao.delete(notebook)

    override suspend fun updateItem(notebook: Notebook) = notebookDao.update(notebook)
}
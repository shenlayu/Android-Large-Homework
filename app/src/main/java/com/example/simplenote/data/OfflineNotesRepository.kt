package com.example.simplenote.data

import kotlinx.coroutines.flow.Flow

// 将Dao的接口赋值给暴露给用户的接口
class OfflineNotesRepository(private val noteDao: NoteDao) : NotesRepository {
    override fun getAllNotesStream(): Flow<List<Note>> = noteDao.getAllNotes()

    override fun getNoteStream(id: Int): Flow<Note?> = noteDao.getNote(id)

    override suspend fun insertNote(note: Note) = noteDao.insert(note)

    override suspend fun deleteNote(note: Note) = noteDao.delete(note)

    override suspend fun updateNote(note: Note) = noteDao.update(note)
}

class OfflineNotebooksRepository(private val notebookDao: NotebookDao) : NotebooksRepository {
    override fun getAllNotebooksStream(): Flow<List<Notebook>> = notebookDao.getAllItems()

    override fun getNotebookStream(id: Int): Flow<Notebook?> = notebookDao.getItem(id)

    override suspend fun insertNotebook(notebook: Notebook) = notebookDao.insert(notebook)

    override suspend fun deleteNotebook(notebook: Notebook) = notebookDao.delete(notebook)

    override suspend fun updateNotebook(notebook: Notebook) = notebookDao.update(notebook)
}
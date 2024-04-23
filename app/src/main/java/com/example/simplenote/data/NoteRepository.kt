package com.example.simplenote.data

import kotlinx.coroutines.flow.Flow

// 映射Dao的接口，将对database对操作封装为对用户的接口
interface NotesRepository {
    fun getAllItemsStream(): Flow<List<Note>>

    fun getItemStream(id: Int): Flow<Note?>

    suspend fun insertItem(note: Note)

    suspend fun deleteItem(note: Note)

    suspend fun updateItem(note: Note)
}

interface NotebooksRepository {
    fun getAllItemsStream(): Flow<List<Notebook>>

    fun getItemStream(id: Int): Flow<Notebook?>

    suspend fun insertItem(notebook: Notebook)

    suspend fun deleteItem(notebook: Notebook)

    suspend fun updateItem(notebook: Notebook)
}
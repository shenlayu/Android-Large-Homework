package com.example.simplenote.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// 提供对数据库操作的方法接口 Data Access Object
@Dao
interface NotebookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // 如果确定不会发生冲突，那么可以无视冲突
    suspend fun insert(notebook: Notebook) // suspend可以使操作在单独的线程上运行

    @Update
    suspend fun update(notebook: Notebook)

    @Delete
    suspend fun delete(notebook: Notebook)

    @Query("SELECT * from notebooks WHERE id = :id") // 由于返回值是flow, 会在后台运行该查询。无需suspend
    fun getItem(id: Int): Flow<Notebook>

    @Query("SELECT * from notebooks ORDER BY id ASC")
    fun getAllItems(): Flow<List<Notebook>>

    @Query("SELECT * FROM notebooks")
    fun getNotebooksWithNotes(): Flow<List<NotebookWithNotes>>
}

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * from notes WHERE id = :id")
    fun getNote(id: Int): Flow<Note>

    @Query("SELECT * from notes ORDER BY id ASC")
    fun getAllNotes(): Flow<List<Note>>
}
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
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // 如果确定不会发生冲突，那么可以无视冲突
    suspend fun insert(user: User) // suspend可以使操作在单独的线程上运行
    @Update
    suspend fun update(user: User)
    @Delete
    suspend fun delete(user: User)
    @Query("SELECT * from users WHERE username = :username")
    fun searchUser(username: String): Flow<User> // 检查user
    @Query("SELECT * from users WHERE id = :id")
    fun searchUserById(id: Int): Flow<User>
    @Query("SELECT * FROM users WHERE id = :id")
    fun getNotebookWithNotes(id : Int): Flow<UserWithDirectories>
}

@Dao
interface DirectoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(directory: Directory)
    @Update
    suspend fun update(directory: Directory)
    @Delete
    suspend fun delete(directory: Directory)
    @Query("SELECT * from directories WHERE id = :id")
    fun getDirectory(id: Int): Flow<Directory>
    @Query("SELECT * FROM directories WHERE id = :id")
    fun getDirectoryWithNotebooks(id: Int): Flow<DirectoryWithNotebooks>
}

@Dao
interface NotebookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notebook: Notebook)
    @Update
    suspend fun update(notebook: Notebook)
    @Delete
    suspend fun delete(notebook: Notebook)
    @Query("SELECT * from notebooks WHERE id = :id") // 由于返回值是flow, 会在后台运行该查询。无需suspend
    fun getItem(id: Int): Flow<Notebook>
    @Query("SELECT * from notebooks ORDER BY id ASC")
    fun getAllItems(): Flow<List<Notebook>>
    @Query("SELECT * FROM notebooks WHERE id = :id")
    fun getNotebookWithNotes(id : Int): Flow<NotebookWithNotes>
    @Query("SELECT * FROM notebooks")
    fun getAllNotebooksWithNotes(): Flow<List<NotebookWithNotes>>
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
    @Query("SELECT * from notes WHERE content LIKE :content AND type = :text")
    fun search(content: String, text: String = "Text"): Flow<List<Note>>
}

@Dao
interface LoggedUserDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(loggedUser: LoggedUser)
    @Update
    suspend fun update(loggedUser: LoggedUser)
    @Delete
    suspend fun delete(loggedUser: LoggedUser)
    @Query("SELECT * from loggedUser ORDER BY id ASC")
    fun getLoggedUser(): Flow<List<LoggedUser>>
}
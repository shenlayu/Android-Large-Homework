package com.example.simplenote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 创建一个数据库
@Database(entities = [Notebook::class, Note::class], version = 1, exportSchema = false)
abstract class NoteDatabase: RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun noteDao(): NoteDao

    // 仅当数据库不存在时创建数据库，否则返回现有数据库
    companion object {
        @Volatile
        private var Instance: NoteDatabase? = null // 初始化为null
        fun getDatabase(context: Context): NoteDatabase {
            return Instance ?: synchronized(this) { // 第一次调用才创建
                Room.databaseBuilder(context, NoteDatabase::class.java, "item_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
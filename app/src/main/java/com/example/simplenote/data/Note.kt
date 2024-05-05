package com.example.simplenote.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

// 实体，对应数据库中一张表
// 用户
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val directoryNum: Int,
    val username: String,
    val password: String,
    val photo: String
)

// 文件夹
@Entity(tableName = "directories")
data class Directory(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val notebookNum: Int,
    val userId: Int
)

// 笔记本
@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey(autoGenerate = true) // 主键，递增ID以保证ID唯一
    val id: Int,
    val name: String,
    val noteNum: Int, // 包含的note条目数量
    val directoryId: Int // 所在的文件夹编号
)

// 笔记条目，对应一段文字/一张图片 等等
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) // 主键
    val id: Int,
    val content: String,
    val type: NoteType?,
    val notebookId: Int
)

// 用户与文件夹之间的对应
data class UserWithDirectories(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val directories: List<Directory>
)
// 文件夹与笔记本之间的对应
data class DirectoryWithNotebooks(
    @Embedded val directory: Directory,
    @Relation(
        parentColumn = "id",
        entityColumn = "directoryId"
    )
    val notebooks: List<Notebook>
)
// 笔记本和笔记之间的对应
data class NotebookWithNotes(
    @Embedded val notebook: Notebook,
    @Relation(
        parentColumn = "id",
        entityColumn = "notebookId"
    )
    val notes: List<Note>
)

// 规定一个Note条目的类型
enum class NoteType {
    Text,
    Photo
}
package com.example.simplenote.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.simplenote.ui.note.SortType
import java.security.Signature

// 实体，对应数据库中一张表
// 用户
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val username: String,
    val password: String,
    val nickname: String,
    val avatar: String,
    val signature: String
)

// 文件夹
@Entity(tableName = "directories")
data class Directory(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val userId: Int,
    val time: String, // 创建时间
)

// 笔记本
@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey(autoGenerate = true) // 主键，递增ID以保证ID唯一
    val id: Int,
    val name: String,
    val directoryId: Int, // 所在的文件夹编号
    val createTime: String, // 创建时间
    val changeTime: String // 修改时间
)

// 笔记条目，对应一段文字/一张图片 等等
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) // 主键
    val id: Int,
    val content: String,
    val type: NoteType?,
    val notebookId: Int,
    val order: Int,
    val isTitle: Boolean
)

@Entity(tableName = "loggedUser")
data class LoggedUser(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val userId: Int
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
    Photo,
    Audio,
    Video
}
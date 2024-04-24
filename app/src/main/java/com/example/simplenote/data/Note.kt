package com.example.simplenote.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

// 实体，对应数据库中一张表
// 笔记本
@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey(autoGenerate = true) // 主键，递增ID以保证ID唯一
    val id: Long,
    val name: String,
    val noteNum: Int // 包含的note条目数量
) {
//    @Relation(
//        parentColumn = "id",
//        entityColumn = "notebookId"
//    )
//    var notes: List<Note>? = null // 笔记列表，通过关联查询获取
}

// 笔记条目，对应一段文字/一张图片 等等
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) // 主键
    val id: Long,
    val notebookId: Long,
    val content: String,
    val type: NoteType?
)

// 规定一个Note条目的类型
enum class NoteType {
    Text,
    Photo
}
package com.example.simplenote.ui.note

import androidx.lifecycle.ViewModel
import com.example.simplenote.data.Note
import com.example.simplenote.data.NoteType
import com.example.simplenote.data.NotesRepository

// 传递封装好的接口
class NoteViewModel(private val notesRepository: NotesRepository) : ViewModel() {

}



// notebook界面
data class NotebookUiState(
    val notebookDetails: NotebookDetails = NotebookDetails(),
    val isEntryValid: Boolean = false
)

// note界面
data class NoteUiState(
    val noteDetails: NoteDetails = NoteDetails(),
    val isEntryValid: Boolean = false
)

// notebook细节
data class NotebookDetails(
    val id: Long = 0,
    val name: String = ""
)

// note细节
data class NoteDetails(
    val id: Long = 0,
    val notebookId: Long = 0,
    val content: String = "",
    val type: NoteType? = null
)

// 将NoteDetails转换为Note entity，相当于写
fun NoteDetails.toItem(): Note = Note(
    id = id,
    notebookId = notebookId,
    content = content,
    type = type
)

// 将Note entity转换为NoteUiState
fun Note.toNoteUiState(isEntryValid: Boolean = false): NoteUiState = NoteUiState(
    noteDetails = this.toNoteDetails(),
    isEntryValid = isEntryValid
)

// 将Note entity转换为NoteDetails，相当于读
fun Note.toNoteDetails(): NoteDetails = NoteDetails(
    id = id,
    notebookId = notebookId,
    content = content,
    type = type
)
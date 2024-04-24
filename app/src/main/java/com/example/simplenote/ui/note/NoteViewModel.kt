package com.example.simplenote.ui.note

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.simplenote.data.Note
import com.example.simplenote.data.NoteType
import com.example.simplenote.data.Notebook
import com.example.simplenote.data.NotebooksRepository

// 传递封装好的接口
class NoteViewModel(private val notebooksRepository: NotebooksRepository) : ViewModel() {
    // 保存notebook状态
    var notebookUiState by mutableStateOf(NotebookUiState())
        private set

    // 更新正在修改的notebook
    fun updateUiState(itemDetails: NotebookDetails) {
        notebookUiState =
            NotebookUiState(notebookDetails = itemDetails, isEntryValid = validateInput(itemDetails))
    }

    // 判断一个notebook是否valid
    private fun validateInput(uiState: NotebookDetails = notebookUiState.notebookDetails): Boolean {
        return with(uiState) {
            noteNum != 0 // 若没有条目，则为无效
        }
    }
    suspend fun saveNotebook() {
        if (validateInput()) {
            notebooksRepository.insertNotebook(notebookUiState.notebookDetails.toNotebook())
        }
    }


    // 新建一个Text类型的Note并将其添加到Notebook的List<Note>中
    fun CreatAndSaveText(text: String = "") {
        var noteUiState by mutableStateOf(NotebookUiState())
    }
    fun CreatAndSavePhoto() {

    }
}

// notebook界面
data class NotebookUiState(
    val notebookDetails: NotebookDetails = NotebookDetails(),
    val isEntryValid: Boolean = false
)
// notebook细节
data class NotebookDetails(
    val id: Long = 0,
    val name: String = "",
    val noteNum: Int = 0
)
// note细节
data class NoteDetails(
    val id: Long = 0,
    val notebookId: Long = 0,
    val content: String = "",
    val type: NoteType? = null
)
fun NotebookDetails.toNotebook(): Notebook = Notebook(
    id = id,
    name = name,
    noteNum = noteNum
)
fun Notebook.toNotebookUiState(isEntryValid: Boolean = false): NotebookUiState = NotebookUiState(
    notebookDetails = this.toNotebookDetails(),
    isEntryValid = isEntryValid
)
fun Notebook.toNotebookDetails(): NotebookDetails = NotebookDetails(
    id = id,
    name = name,
    noteNum = noteNum
)
// 将NoteDetails转换为Note entity，相当于写
fun NoteDetails.toNote(): Note = Note(
    id = id,
    notebookId = notebookId,
    content = content,
    type = type
)
// 将Note entity转换为NoteDetails，相当于读
fun Note.toNoteDetails(): NoteDetails = NoteDetails(
    id = id,
    notebookId = notebookId,
    content = content,
    type = type
)
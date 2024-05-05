package com.example.simplenote.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.Directory
import com.example.simplenote.data.Note
import com.example.simplenote.data.NoteType
import com.example.simplenote.data.Notebook
import com.example.simplenote.data.NotebookWithNotes
import com.example.simplenote.data.NotebooksRepository
import com.example.simplenote.data.NotesRepository
import com.example.simplenote.data.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// 传递封装好的接口
class NoteViewModel_(
    private val notebooksRepository: NotebooksRepository,
    private val notesRepository: NotesRepository

) : ViewModel() {
    // 正在编辑的笔记本编号，应该有更合适的组织方法
    var notebookId: Int = 0

    // 暂存一个noteDetails列表，内容先存放在这个列表中
    // 在save notebook时，不仅需要在database中存放新的notebook, 还需要存放这些noteDetails
    var noteList = mutableListOf<NoteDetails>()
    val notebookState: StateFlow<NotebookState> =
        notebooksRepository.getNotebookWithNotes(notebookId).map { NotebookState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = NotebookState()
            )
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
    var editedNotebook = NotebookDetails(
        name = "",
        noteNum = 0
    )

    fun init(notebookId: Int = 0, name: String = "") {
        // 指定notebookId
        changeNotebookId(notebookId)
        changeNotebookName(name)
        // 通过database给noteList赋值
        notebookState.value.notebookWithNotes?.notes?.forEach {
            noteList.add(it.toNoteDetails())
        }
        // 清理noteList
        noteList.clear()
    }

    // 从数据库将notebook现有对应的notes传到临时list中
    fun getNotesFromDatabase() {
        var a = notebooksRepository.getNotebookWithNotes(notebookId)
    }
    // 与暂存note有关的接口
    fun addText(content: String = "") {
        noteList.add(
            NoteDetails(
                notebookId = notebookId,
                content = content,
                type = NoteType.Text
            )
        )
    }
    fun addPhoto(path: String = "") {
        noteList.add(
            NoteDetails(
                notebookId = notebookId,
                content = path,
                type = NoteType.Text
            )
        )
    }
    // 修改editedNotebook名称
    fun changeNotebookName(name: String = "") {
        editedNotebook.name = name
    }
    fun changeNotebookId(id: Int = 0) {
        editedNotebook.id = id
    }

    // 保存notebook和note
    suspend fun saveNotebook() {
        if(noteList.size > 0) {
            editedNotebook.noteNum = noteList.size
            notebookState.value.notebookWithNotes?.notes?.forEach {
                notesRepository.deleteNote(it) // 将原本数据删除
            }
            notebooksRepository.insertNotebook(editedNotebook.toNotebook()) // 存储notebook
            noteList.forEach {
                notesRepository.insertNote(it.toNote()) // 存进新的临时数据
            }
        }
    }
}

data class NotebookState(
    val notebookWithNotes: NotebookWithNotes? = null
)


// notebook细节
data class NotebookDetails(
    var id: Int = 0,
    var name: String = "",
    var noteNum: Int = 0,
    var directoryId: Int = 0
)
// note细节
data class NoteDetails(
    val id: Int = 0,
    val notebookId: Int = 0,
    val content: String = "",
    val type: NoteType? = null
)



fun NotebookDetails.toNotebook(): Notebook = Notebook(
    id = id,
    name = name,
    noteNum = noteNum,
    directoryId = directoryId
)
fun Notebook.toNotebookDetails(): NotebookDetails = NotebookDetails(
    id = id,
    name = name,
    noteNum = noteNum,
    directoryId = directoryId
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

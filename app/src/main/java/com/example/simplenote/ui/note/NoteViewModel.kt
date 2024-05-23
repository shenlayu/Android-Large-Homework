package com.example.simplenote.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.DirectoryWithNotebooks
import com.example.simplenote.data.Note
import com.example.simplenote.data.NoteType
import com.example.simplenote.data.NotebookWithNotes
import com.example.simplenote.data.NotebooksRepository
import com.example.simplenote.data.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(
    private val notebooksRepository: NotebooksRepository,
    private val notesRepository: NotesRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow((NoteUiState()))
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
//    private var notebookState: StateFlow<NotebookState>? = null
//    var noteList = mutableListOf<NoteDetails>()
//    private var notebookID: Int? = null
//    private var deletedNoteList = mutableListOf<NoteDetails>()

//    fun init(notebookID_: Int? = null) { // 如果是更新已有notebook, 传入其id; 如果是创建新notebook, 传入null
//        notebookID_?.let {nid ->
//            _uiState.value = _uiState.value.copy(
//                notebookState =
//                notebooksRepository.getNotebookWithNotes(nid)
//                    .map { NotebookState(it) }
//                    .stateIn(
//                        scope = viewModelScope,
//                        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
//                        initialValue = NotebookState()
//                    )
//            )
//        }
//        val newNoteList = emptyList<NoteDetails>().toMutableList()
//        _uiState.value.notebookState?.value?.notebookWithNotes?.notes?.forEach { // 打进临时
//            newNoteList.add(it.toNoteDetails())
//        }
//        _uiState.value = _uiState.value.copy(noteList = newNoteList, notebookID = notebookID_)
//    }

    fun init(notebookId: Int? = null) {
        viewModelScope.launch {
            notebookId?.let { nid ->
                val notebookWithNotes: NotebookWithNotes = notebooksRepository.getNotebookWithNotes(nid).firstOrNull()!!
                val newNoteList = _uiState.value.noteList.toMutableList()
                notebookWithNotes.notes.forEach {
                    newNoteList.add(it.toNoteDetails())
                }
                _uiState.value = _uiState.value.copy(noteList = newNoteList, notebookID = notebookId)
            } ?: run {
                _uiState.value = _uiState.value.copy(noteList = emptyList(), notebookID = notebookId)
            }
        }
    }

    fun insertNote(listID: Int, content: String, type: NoteType) {
        val noteDetails: NoteDetails = NoteDetails(
            notebookId = _uiState.value.notebookID!!,
            content = content,
            type = type
        )
        val newNoteList = _uiState.value.noteList.toMutableList().apply { add(listID, noteDetails) }
        _uiState.value = _uiState.value.copy(noteList = newNoteList)
    }
    fun deleteNote(listID: Int) {
        val newNoteList = _uiState.value.noteList.toMutableList().apply { removeAt(listID) }
        val newDeletedList = _uiState.value.deletedNoteList.toMutableList().apply { add(_uiState.value.noteList[listID]) }
    }
    fun changeNote(listID: Int, content: String, type: NoteType) {
        val currentNoteDetails = _uiState.value.noteList[listID]
        val newNoteDetails: NoteDetails = NoteDetails(
            id = currentNoteDetails.id,
            notebookId = currentNoteDetails.notebookId,
            content = content,
            type = type
        )
        val newNoteList = _uiState.value.noteList.toMutableList()
        newNoteList[listID] = newNoteDetails
        _uiState.value = _uiState.value.copy(noteList = newNoteList)
    }
    fun saveNotes() {
        viewModelScope.launch {
            _uiState.value.noteList.forEach {
                val checkNote: Note? = notesRepository.getNoteStream(it.id).firstOrNull()
                if(checkNote != null) { // 已经存在这条内容
                    notesRepository.updateNote(it.toNote())
                } else {
                    notesRepository.insertNote(it.toNote())
                }
            }
            _uiState.value.deletedNoteList.forEach {
                notesRepository.deleteNote(it.toNote())
            }
        }
    }
}

data class NotebookState(
    val notebookWithNotes: NotebookWithNotes? = null
)

// note细节
data class NoteDetails(
    val id: Int = 0,
    val notebookId: Int = 0,
    val content: String = "",
    val type: NoteType? = null,
    var order: Int = 0
)
// 将NoteDetails转换为Note entity，相当于写
fun NoteDetails.toNote(): Note = Note(
    id = id,
    notebookId = notebookId,
    content = content,
    type = type,
    order = order
)
// 将Note entity转换为NoteDetails，相当于读
fun Note.toNoteDetails(): NoteDetails = NoteDetails(
    id = id,
    notebookId = notebookId,
    content = content,
    type = type,
    order = order
)

data class NoteUiState(
    val notebookState: StateFlow<NotebookState>? = null,
    val noteList: List<NoteDetails> = listOf<NoteDetails>(),
    val notebookID: Int? = null,
    val deletedNoteList: List<NoteDetails> = listOf<NoteDetails>(),
)
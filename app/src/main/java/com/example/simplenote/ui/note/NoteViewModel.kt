package com.example.simplenote.ui.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.DirectoryWithNotebooks
import com.example.simplenote.data.Note
import com.example.simplenote.data.NoteType
import com.example.simplenote.data.NotebookWithNotes
import com.example.simplenote.data.NotebooksRepository
import com.example.simplenote.data.NotesRepository
import com.example.simplenote.ui.ContentItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

//    fun init(notebookId: Int? = null) {
//        viewModelScope.launch {
//            notebookId?.let { nid ->
//                val notebookWithNotes: NotebookWithNotes = notebooksRepository.getNotebookWithNotes(nid).firstOrNull()!!
//                val newNoteList = _uiState.value.noteList.toMutableList()
//                notebookWithNotes.notes.forEach {
//                    newNoteList.add(it.toNoteDetails())
//                }
//                _uiState.value = _uiState.value.copy(noteList = newNoteList, notebookID = notebookId)
//            } ?: run {
//                _uiState.value = _uiState.value.copy(noteList = emptyList(), notebookID = notebookId)
//            }
//        }
//    }
//
//    fun insertNote(listID: Int, content: String, type: NoteType) {
//        val noteDetails: NoteDetails = NoteDetails(
//            notebookId = _uiState.value.notebookID!!,
//            content = content,
//            type = type
//        )
//        val newNoteList = _uiState.value.noteList.toMutableList().apply { add(listID, noteDetails) }
//        _uiState.value = _uiState.value.copy(noteList = newNoteList)
//    }
//    fun deleteNote(listID: Int) {
//        val newNoteList = _uiState.value.noteList.toMutableList().apply { removeAt(listID) }
//        val newDeletedList = _uiState.value.deletedNoteList.toMutableList().apply { add(_uiState.value.noteList[listID]) }
//    }
//    fun changeNote(listID: Int, content: String, type: NoteType) {
//        val currentNoteDetails = _uiState.value.noteList[listID]
//        val newNoteDetails: NoteDetails = NoteDetails(
//            id = currentNoteDetails.id,
//            notebookId = currentNoteDetails.notebookId,
//            content = content,
//            type = type
//        )
//        val newNoteList = _uiState.value.noteList.toMutableList()
//        newNoteList[listID] = newNoteDetails
//        _uiState.value = _uiState.value.copy(noteList = newNoteList)
//    }
//    fun saveNotes() {
//        viewModelScope.launch {
//            _uiState.value.noteList.forEach {
//                val checkNote: Note? = notesRepository.getNoteStream(it.id).firstOrNull()
//                if(checkNote != null) { // 已经存在这条内容
//                    notesRepository.updateNote(it.toNote())
//                } else {
//                    notesRepository.insertNote(it.toNote())
//                }
//            }
//            _uiState.value.deletedNoteList.forEach {
//                notesRepository.deleteNote(it.toNote())
//            }
//        }
//    }

    fun init(notebookID: Int) {
        runBlocking {
            val notebookWithNotes: NotebookWithNotes = notebooksRepository.getNotebookWithNotes(notebookID).firstOrNull()!!
            val newNoteList = emptyList<NoteDetails>().toMutableList()
            notebookWithNotes.notes.forEach {
                newNoteList.add(it.toNoteDetails())
            }
            _uiState.value = _uiState.value.copy(noteList = newNoteList, notebookId = notebookID)
        }
    }
    fun initFirst(notebookID: Int) { // 新建时的init
        val newNoteList = emptyList<NoteDetails>().toMutableList()
        newNoteList.add(
            NoteDetails(
                notebookId = notebookID,
                content = "新建笔记本",
                type = NoteType.Text,
                order = 0,
                isTitle = true
            )
        )
        newNoteList.add(
            NoteDetails(
                notebookId = notebookID,
                content = "内容",
                type = NoteType.Text,
                order = 1,
                isTitle = false
            )
        )
        runBlocking {
            newNoteList.forEach {
                notesRepository.insertNote(it.toNote())
            }
        }
        runBlocking {
            init(notebookID)
        }
//        Log.d("add1", "in ${_uiState.value.noteList.size}")
    }

    fun convertContentItemToNoteDetails(contentItem: ContentItem): NoteDetails {
        var noteDetails: NoteDetails = NoteDetails()
        when (contentItem) {
            is ContentItem.TextItem -> {
                noteDetails = noteDetails.copy(content = contentItem.text.value.text, isTitle = contentItem.isTitle, type = NoteType.Text)
            }
            is ContentItem.ImageItem -> {
                noteDetails = noteDetails.copy(content = contentItem.imageUri.path!!, type = NoteType.Photo)
            }
            is ContentItem.AudioItem -> {
                noteDetails = noteDetails.copy(content = contentItem.audioUri.path!!, type = NoteType.Audio)
            }
            is ContentItem.VideoItem -> {
                noteDetails = noteDetails.copy(content = contentItem.videoUri.path!!, type = NoteType.Video)
            }
        }
        return noteDetails
    }
    fun convertToNoteDetailsList(contentItems: List<ContentItem>, notebookId: Int): List<NoteDetails> {
        val noteDetailsList: MutableList<NoteDetails> = emptyList<NoteDetails>().toMutableList()
        for(idx in 0..<contentItems.size) {
            var noteDetails = convertContentItemToNoteDetails(contentItems[idx])
            noteDetails = noteDetails.copy(order = idx, notebookId = notebookId)
            noteDetailsList.add(noteDetails)
        }
        return noteDetailsList.toList()
    }

    fun saveNotes(contentItems: List<ContentItem>) {
        // 先将当前viewmodel中list中内容在database中清空，
        // 然后convert to noteList并保存到viewmodel中list
        // 最后将viewmodel中新的list内容存到database
        runBlocking {
            _uiState.value.noteList.forEach {
                notesRepository.deleteNote(it.toNote())
            }
        }

        val newNoteList = convertToNoteDetailsList(
            contentItems = contentItems,
            notebookId = _uiState.value.notebookId!!
        )
        _uiState.value = _uiState.value.copy(noteList = newNoteList)

        runBlocking {
            _uiState.value.noteList.forEach {
                val checkNote: Note? = notesRepository.getNoteStream(it.id).firstOrNull()
                if(checkNote != null) { // 已经存在这条内容
                    notesRepository.updateNote(it.toNote())
                } else {
                    notesRepository.insertNote(it.toNote())
                }
            } // 需要先清空
            _uiState.value.deletedNoteList.forEach {
                notesRepository.deleteNote(it.toNote())
            }
        }

        runBlocking {
            val notebookWithNotes: NotebookWithNotes = notebooksRepository.getNotebookWithNotes(_uiState.value.notebookId!!).firstOrNull()!!
            val newNoteListWithId = emptyList<NoteDetails>().toMutableList()
            notebookWithNotes.notes.forEach {
                newNoteListWithId.add(it.toNoteDetails())
            }
            _uiState.value = _uiState.value.copy(noteList = newNoteListWithId)
        }
    }

    fun searchNote(content: String): List<NoteDetails> {
        val noteDetailsList: MutableList<NoteDetails> = emptyList<NoteDetails>().toMutableList()
        runBlocking {
            val notes: List<Note> = notesRepository.searchNote(content).first()
            notes.forEach {
                noteDetailsList.add(it.toNoteDetails())
            }
        }
        return noteDetailsList.toList()
    }
}

data class NoteUiState(
    val notebookState: StateFlow<NotebookState>? = null,
    val noteList: List<NoteDetails> = listOf<NoteDetails>(),
    val notebookId: Int? = null,
    val deletedNoteList: List<NoteDetails> = listOf<NoteDetails>(),
)

data class NotebookState(
    val notebookWithNotes: NotebookWithNotes? = null
)

// note细节
data class NoteDetails(
    val id: Int = 0,
    val notebookId: Int = 0,
    val content: String = "",
    val type: NoteType? = null,
    var order: Int = 0,
    val isTitle: Boolean = false
)
// 将NoteDetails转换为Note entity，相当于写
fun NoteDetails.toNote(): Note = Note(
    id = id,
    notebookId = notebookId,
    content = content,
    type = type,
    order = order,
    isTitle = isTitle
)
// 将Note entity转换为NoteDetails，相当于读
fun Note.toNoteDetails(): NoteDetails = NoteDetails(
    id = id,
    notebookId = notebookId,
    content = content,
    type = type,
    order = order,
    isTitle = isTitle
)
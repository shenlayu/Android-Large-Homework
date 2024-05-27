package com.example.simplenote.ui.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.DirectoriesRepository
import com.example.simplenote.data.DirectoryWithNotebooks
import com.example.simplenote.data.Notebook
import com.example.simplenote.data.NotebooksRepository
import com.example.simplenote.data.NotesRepository
import com.example.simplenote.data.UserWithDirectories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotebookViewModel(
    private val directoriesRepository: DirectoriesRepository,
    private val notebookRepository: NotebooksRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow((NotebookUiState()))
    val uiState: StateFlow<NotebookUiState> = _uiState.asStateFlow()
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
//    var directoryState: StateFlow<DirectoryState>? = null
//    var notebookList = mutableListOf<NotebookDetails>()
//    var directoryID: Int? = null
//    var sortType: SortType = SortType.Time

//    fun init(directoryID_: Int? = null) { // 更改页面时手动调用该函数
//        directoryID_?.let {did ->
//            _uiState.value = _uiState.value.copy(
//                directoryState =
//                directoriesRepository.getDirectoryWithNotebooks(did)
//                    .map { DirectoryState(it) }
//                    .stateIn(
//                        scope = viewModelScope,
//                        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
//                        initialValue = DirectoryState()
//                    )
//            )
//        }
//        val newNotebookList = emptyList<NotebookDetails>().toMutableList()
//        _uiState.value.directoryState?.value?.directoryWithNotebooks?.notebooks?.forEach { // 打进临时
//            newNotebookList.add(it.toNotebookDetails())
//        }
//        _uiState.value = _uiState.value.copy(directoryID = directoryID_, notebookList = newNotebookList)
//    }
    fun init(directoryID: Int? = null) {
        viewModelScope.launch {
            directoryID?.let { did ->
                val directoryWithNotebooks: DirectoryWithNotebooks = directoriesRepository.getDirectoryWithNotebooks(did).firstOrNull()!!
                val newNotebookList = emptyList<NotebookDetails>().toMutableList()
                directoryWithNotebooks.notebooks.forEach {
                    newNotebookList.add(it.toNotebookDetails())
                }
                _uiState.value = _uiState.value.copy(notebookList = newNotebookList, directoryID = directoryID)
            } ?: run {
                _uiState.value = _uiState.value.copy(notebookList = emptyList(), directoryID = directoryID)
            }
        }
    }


    fun sortBySortType() {
        if(_uiState.value.sortType == SortType.Time) {
            val newNotebookList = _uiState.value.notebookList.toMutableList()
            newNotebookList.sortBy { it.time }
            _uiState.value = _uiState.value.copy(notebookList = newNotebookList.toList())
        }
        if(_uiState.value.sortType == SortType.Name) {
            val newNotebookList = _uiState.value.notebookList.toMutableList()
            newNotebookList.sortBy { it.name }
            _uiState.value = _uiState.value.copy(notebookList = newNotebookList.toList())
        }
    }
    fun changeSortType(sortTypeTo: SortType) {
        _uiState.value = _uiState.value.copy(sortType = sortTypeTo)
    }
    fun insertNotebook(name: String) {
//        Log.d("add1", "directoryId ${_uiState.value.directoryID}")
        val notebookDetails: NotebookDetails = NotebookDetails(
            name = name,
            directoryId = _uiState.value.directoryID!!,
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        val newNotebookList = _uiState.value.notebookList.toMutableList().apply{ add(notebookDetails) }
        _uiState.value = _uiState.value.copy(notebookList = newNotebookList)
//        Log.d("add1", "list size ${_uiState.value.notebookList.size}")
        runBlocking {
            notebookRepository.insertNotebook(notebookDetails.toNotebook())
        }
        runBlocking {
            val directoryWithNotebooks: DirectoryWithNotebooks = directoriesRepository.getDirectoryWithNotebooks(_uiState.value.directoryID!!).firstOrNull()!!
            val newNotebookListWithId = emptyList<NotebookDetails>().toMutableList()
            directoryWithNotebooks.notebooks.forEach {
                newNotebookListWithId.add(it.toNotebookDetails())
            }
            _uiState.value = _uiState.value.copy(notebookList = newNotebookListWithId)
        }
    }
    fun deleteNotebook(listID: Int) {
        if(listID < _uiState.value.notebookList.size) {
            val newNotebookList = _uiState.value.notebookList.toMutableList().apply{ removeAt(listID) }
            val notebook = newNotebookList[listID].toNotebook()
            _uiState.value = _uiState.value.copy(notebookList = newNotebookList)
            viewModelScope.launch {
                notebookRepository.deleteNotebook(notebook)
            }
        }
        else {
            println("deleteDirectory ERROR: No user found")
        }
    }
    fun changeNotebookName(listID: Int, name: String) {
        if(listID < _uiState.value.notebookList.size) {
            val newNotebookList = _uiState.value.notebookList.toMutableList()
            newNotebookList[listID].name = name
            val notebook = newNotebookList[listID].toNotebook()
            viewModelScope.launch {
                notebookRepository.updateNotebook(notebook)
            }
        }
        else {
            println("changeDirectoryName ERROR: No user found")
        }
    }
    fun changeNotebookTime(listID: Int) {
        // TODO
    }
    fun getTitleNote(notebookID: Int): NoteDetails? {
        val notes = runBlocking {
            notebookRepository.getNotebookWithNotes(notebookID).firstOrNull()?.notes
        }
        var noteDetails: NoteDetails? = null
        notes?.forEach {
            if(it.isTitle == true) {
                noteDetails = it.toNoteDetails()
            }
        }
        return noteDetails
    }
    fun getFirstNote(notebookID: Int): NoteDetails? {
        val notes = runBlocking {
            notebookRepository.getNotebookWithNotes(notebookID).firstOrNull()?.notes
        }

        if (notes != null) {
            for(note in notes) {
                if(!note.isTitle)
                    return note.toNoteDetails()
            }
        }

        return null
    }
    fun getSecondNote(notebookID: Int): NoteDetails? {
        val notes = runBlocking {
            notebookRepository.getNotebookWithNotes(notebookID).firstOrNull()?.notes
        }

        var jump = true
        if (notes != null) {
            for(note in notes) {
                if(!note.isTitle) {
                    if(jump) {
                        jump = false
                        continue
                    }
                    return note.toNoteDetails()
                }
            }
        }

        return null
    }
}

data class NotebookUiState (
//    val directoryState: StateFlow<DirectoryState>? = null,
    val notebookList: List<NotebookDetails> = listOf<NotebookDetails>(),
    val directoryID: Int? = null,
    val sortType: SortType = SortType.Time
)

data class DirectoryState(
    val directoryWithNotebooks: DirectoryWithNotebooks? = null
)

// notebook细节
data class NotebookDetails(
    var id: Int = 0,
    var name: String = "",
    var directoryId: Int = 0,
    var time: String = ""
)
fun NotebookDetails.toNotebook(): Notebook = Notebook(
    id = id,
    name = name,
    directoryId = directoryId,
    time = time
)
fun Notebook.toNotebookDetails(): NotebookDetails = NotebookDetails(
    id = id,
    name = name,
    directoryId = directoryId,
    time = time
)
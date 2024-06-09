package com.example.simplenote.ui.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.DirectoriesRepository
import com.example.simplenote.data.DirectoryWithNotebooks
import com.example.simplenote.data.NoteType
import com.example.simplenote.data.Notebook
import com.example.simplenote.data.NotebooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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
    fun init(directoryID: Int? = null, directoryList: List<DirectoryDetails>? = null) {
        runBlocking {
            directoryID?.let { did ->
                if(directoryList != null) { // 全部笔记
                    val newNotebookList: MutableList<NotebookDetails> = emptyList<NotebookDetails>().toMutableList()
                    directoryList.forEach {directoryDetails ->
                        val directoryWithNotebooks: DirectoryWithNotebooks = directoriesRepository.getDirectoryWithNotebooks(directoryDetails.id).firstOrNull()!!
                        directoryWithNotebooks.notebooks.forEach {notebook ->
                            newNotebookList.add(notebook.toNotebookDetails())
                        }
                    }
                    _uiState.value = _uiState.value.copy(notebookList = newNotebookList, directoryID = directoryID)
                }
                else {
                    val directoryWithNotebooks: DirectoryWithNotebooks = directoriesRepository.getDirectoryWithNotebooks(did).firstOrNull()!!
                    val newNotebookList = emptyList<NotebookDetails>().toMutableList()
                    directoryWithNotebooks.notebooks.forEach {
                        newNotebookList.add(it.toNotebookDetails())
                    }
                    _uiState.value = _uiState.value.copy(notebookList = newNotebookList, directoryID = directoryID)
                }
            } ?: run {
                _uiState.value = _uiState.value.copy(notebookList = emptyList(), directoryID = directoryID)
            }
            sortBySortType()
        }
    }

    fun sortBySortType() {
        if(_uiState.value.sortType == SortType.CreateTime) {
            val newNotebookList = _uiState.value.notebookList.toMutableList()
            newNotebookList.sortBy { it.createTime }
            _uiState.value = _uiState.value.copy(notebookList = newNotebookList.toList())
        }
        if(_uiState.value.sortType == SortType.ChangeTime) {
            val newNotebookList = _uiState.value.notebookList.toMutableList()
            newNotebookList.sortBy { it.changeTime }
            _uiState.value = _uiState.value.copy(notebookList = newNotebookList.toList())
        }
    }
    fun changeSortType(sortTypeTo: SortType) {
        _uiState.value = _uiState.value.copy(sortType = sortTypeTo)
    }
    fun insertNotebook(name: String, directoryID: Int? = null, directoryList: List<DirectoryDetails>? = null) {
        // todo 在全部笔记中插入要插到未分类
        Log.d("add1", "directoryID $directoryID")
        val notebookDetails: NotebookDetails = NotebookDetails(
            name = name,
            directoryId = directoryID ?: _uiState.value.directoryID!!,
            createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            changeTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        runBlocking {
            notebookRepository.insertNotebook(notebookDetails.toNotebook())
        }
        runBlocking {
            if(directoryID == null) {
                val directoryWithNotebooks: DirectoryWithNotebooks = directoriesRepository.getDirectoryWithNotebooks(_uiState.value.directoryID!!).firstOrNull()!!
                val newNotebookListWithId = emptyList<NotebookDetails>().toMutableList()
                directoryWithNotebooks.notebooks.forEach {
                    newNotebookListWithId.add(it.toNotebookDetails())
                }
                _uiState.value = _uiState.value.copy(notebookList = newNotebookListWithId)
            }
            else {
                val newNotebookList: MutableList<NotebookDetails> = emptyList<NotebookDetails>().toMutableList()
                directoryList!!.forEach {directoryDetails ->
                    val directoryWithNotebooks: DirectoryWithNotebooks = directoriesRepository.getDirectoryWithNotebooks(directoryDetails.id).firstOrNull()!!
                    directoryWithNotebooks.notebooks.forEach {notebook ->
                        newNotebookList.add(notebook.toNotebookDetails())
                    }
                }
                _uiState.value = _uiState.value.copy(notebookList = newNotebookList)
            }
        }
    }
    fun deleteNotebook(listID: Int) {
        if(listID < _uiState.value.notebookList.size) {
            val notebook = _uiState.value.notebookList[listID].toNotebook()
            runBlocking {
                notebookRepository.deleteNotebook(notebook)
            }
            val newNotebookList = _uiState.value.notebookList.toMutableList().apply{ removeAt(listID) }
            _uiState.value = _uiState.value.copy(notebookList = newNotebookList)
        }
    }
    fun changeNotebookDirectory(listID: Int, directoryID: Int) {
        if(listID < _uiState.value.notebookList.size) {
            val notebookDetails = _uiState.value.notebookList[listID]
            notebookDetails.directoryId = directoryID
            val notebook = notebookDetails.toNotebook()
            runBlocking {
                notebookRepository.updateNotebook(notebook)
            }
            val newNotebookList = _uiState.value.notebookList.toMutableList().apply{ removeAt(listID) }
            _uiState.value = _uiState.value.copy(notebookList = newNotebookList)
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
    }
    fun updateNotebookChangeTime() {
        val newNotebookDetailsList = _uiState.value.notebookList.toMutableList()
        _uiState.value.notebookList.forEachIndexed {idx, notebookDetails ->
            var newNotebookDetails: NotebookDetails
            runBlocking {
                newNotebookDetails = notebookRepository.getNotebookStream(notebookDetails.id).first()!!.toNotebookDetails()
            }
            newNotebookDetailsList[idx] = newNotebookDetails
        }
        _uiState.value = _uiState.value.copy(notebookList = newNotebookDetailsList)
    }
//    fun changeNotebookChangeTime(id: Int) {
//        var listID: Int = 0
//        _uiState.value.notebookList.forEachIndexed() {idx, notebookDetails ->
//            if(notebookDetails.id == id) {
//                listID = idx
//            }
//        }
//        if(listID < _uiState.value.notebookList.size) {
//            val newNotebookList = _uiState.value.notebookList.toMutableList()
//            newNotebookList[listID].changeTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//            val notebook = newNotebookList[listID].toNotebook()
//            viewModelScope.launch {
//                notebookRepository.updateNotebook(notebook)
//            }
//        }
//    }

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
    fun getAllTextNotes(notebookID: Int): List<NoteDetails> {
        val notes = runBlocking {
            notebookRepository.getNotebookWithNotes(notebookID).firstOrNull()?.notes
        }

        val textNotes = mutableListOf<NoteDetails>()
        notes?.forEach { note ->
            if (note.type == NoteType.Text) {
                textNotes.add(note.toNoteDetails())
            }
        }

        return textNotes
    }

    fun getNotebook(notebookID: Int): NotebookDetails? {
        var notebook: NotebookDetails?
        runBlocking {
            notebook = notebookRepository.getNotebookStream(notebookID).firstOrNull()?.toNotebookDetails()
        }
        return notebook
    }
}

data class NotebookUiState (
//    val directoryState: StateFlow<DirectoryState>? = null,
    val notebookList: List<NotebookDetails> = listOf<NotebookDetails>(),
    val directoryID: Int? = null,
    val sortType: SortType = SortType.CreateTime,
)

data class DirectoryState(
    val directoryWithNotebooks: DirectoryWithNotebooks? = null
)

// notebook细节
data class NotebookDetails(
    var id: Int = 0,
    var name: String = "",
    var directoryId: Int = 0,
    var createTime: String = "",
    var changeTime: String = ""
)
fun NotebookDetails.toNotebook(): Notebook = Notebook(
    id = id,
    name = name,
    directoryId = directoryId,
    createTime = createTime,
    changeTime = changeTime
)
fun Notebook.toNotebookDetails(): NotebookDetails = NotebookDetails(
    id = id,
    name = name,
    directoryId = directoryId,
    createTime = createTime,
    changeTime = changeTime
)
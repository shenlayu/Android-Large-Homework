package com.example.simplenote.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.DirectoriesRepository
import com.example.simplenote.data.DirectoryWithNotebooks
import com.example.simplenote.data.Notebook
import com.example.simplenote.data.NotebooksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotebookViewModel(
    private val directoriesRepository: DirectoriesRepository,
    private val notebookRepository: NotebooksRepository,
): ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
    var directoryState: StateFlow<DirectoryState>? = null
    var notebookList = mutableListOf<NotebookDetails>()
    var directoryID: Int? = null
    fun init(directoryID_: Int? = null) { // 更改页面时手动调用该函数
        directoryID_?.let {did ->
            directoryState =
                directoriesRepository.getDirectoryWithNotebooks(did)
                    .map { DirectoryState(it) }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                        initialValue = DirectoryState()
                    )
        }
        notebookList.clear()
        directoryState?.value?.directoryWithNotebooks?.notebooks?.forEach { // 打进临时
            notebookList.add(it.toNotebookDetails())
        }
        directoryID = directoryID_
    }
    fun getNotebookList(list: MutableList<NotebookDetails>) {
        list.clear()
        list.addAll(notebookList)
    }
    var sortType: SortType = SortType.Time

    private fun sortBySortType() {
        if(sortType == SortType.Time) {
            notebookList.sortBy { it.time }
        }
        else if(sortType == SortType.Name) {
            notebookList.sortBy { it.name }
        }
    }
    fun changeSortType(sortTypeTo: SortType) {
        sortType = sortTypeTo
    }
    suspend fun insertNotebook(name: String) {
        val notebookDetails: NotebookDetails = NotebookDetails(
            name = name,
            directoryId = directoryID!!,
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        notebookRepository.insertNotebook(notebookDetails.toNotebook())
        notebookList.add(notebookDetails)
        sortBySortType()
    }
    suspend fun deleteNotebook(listID: Int) {
        if(listID < notebookList.size) {
            val notebook = notebookList[listID].toNotebook()
            notebookList.removeAt(listID) // 不确定要不要删
            notebookRepository.deleteNotebook(notebook)
        }
        else {
            println("deleteDirectory ERROR: No user found")
        }
    }
    suspend fun changeNotebookName(listID: Int, name: String) {
        if(listID < notebookList.size) {
            notebookList[listID].name = name
            val notebook = notebookList[listID].toNotebook()
            notebookRepository.updateNotebook(notebook)
        }
        else {
            println("changeDirectoryName ERROR: No user found")
        }
    }
    suspend fun changeNotebookTime(listID: Int) {
        // TODO
    }
    suspend fun getPreviewNote(listID: Int) {
        // TODO
    }
}

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
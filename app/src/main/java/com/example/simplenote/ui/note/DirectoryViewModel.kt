package com.example.simplenote.ui.note

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.DirectoriesRepository
import com.example.simplenote.data.Directory
import com.example.simplenote.data.UserWithDirectories
import com.example.simplenote.data.UsersRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DirectoryViewModel(
    private val usersRepository: UsersRepository,
    private val directoriesRepository: DirectoriesRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow((DirectoryUiState()))
    val uiState: StateFlow<DirectoryUiState> = _uiState.asStateFlow()

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

//    fun init(userID_: Int? = null) {
//        userID_?.let {uid ->
//            userState =
//            usersRepository.getNotebookWithNotes(uid)
//                .map { UserState(it) }
//                .stateIn(
//                    scope = viewModelScope,
//                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
//                    initialValue = UserState()
//                )
//        }
//        directoryList.clear()
//        userState?.value?.userWithDirectories?.directories?.forEach { // 打进临时
//            directoryList.add(it.toDirectoryDetails())
//        }
//        userID = userID_
//    }
    fun init(userID_: Int? = null) {
        userID_?.let {uid ->
            _uiState.value = _uiState.value.copy(
                userState =
                usersRepository.getNotebookWithNotes(uid)
                    .map { UserState(it) }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                        initialValue = UserState()
                    )
            )
        } ?: run {
            _uiState.value = _uiState.value.copy(userState = null)
        }
        val newDirectoryList = emptyList<DirectoryDetails>().toMutableList()

        _uiState.value.userState?.value?.userWithDirectories?.directories?.forEach { // 打进临时
            newDirectoryList.add(it.toDirectoryDetails())
        }
        _uiState.value = _uiState.value.copy(directoryList = newDirectoryList, userID = userID_)
    }
//    fun getDirectoryList(list: MutableList<DirectoryDetails>) {
//        list.clear()
//        list.addAll(directoryList)
//    }
//    fun getDirectoryList(list: MutableList<DirectoryDetails>) {
//        list.clear()
//        list.addAll(directoryList)
//    }

    private fun sortBySortType() {
        if(_uiState.value.sortType == SortType.Time) {
            val newDirectoryList = _uiState.value.directoryList.toMutableList()
            newDirectoryList.sortBy { it.time }
            _uiState.value = _uiState.value.copy(directoryList = newDirectoryList.toList())
        }
        else if(_uiState.value.sortType == SortType.Name) {
            val newDirectoryList = _uiState.value.directoryList.toMutableList()
            newDirectoryList.sortBy { it.name }
            _uiState.value = _uiState.value.copy(directoryList = newDirectoryList.toList())
        }
    }
    fun changeSortType(sortTypeTo: SortType) {
        _uiState.value = _uiState.value.copy(sortType = sortTypeTo)
    }
    fun insertDirectory(name: String) {
        val directoryDetails: DirectoryDetails = DirectoryDetails(
            name = name,
//            userId = _uiState.value.userID!!,
            userId = 0,
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        val newDirectoryList = _uiState.value.directoryList.toMutableList().apply { add(directoryDetails) }
        _uiState.value = _uiState.value.copy(directoryList = newDirectoryList.toList())
        sortBySortType()
        viewModelScope.launch {
            try {
                directoriesRepository.insertDirectory(directoryDetails.toDirectory())
            } catch (e: Exception) {
                Log.e("DirectoryViewModel", "Error inserting directory", e)
            }
        }
    }
    fun deleteDirectory(listID: Int) {
        if (listID < _uiState.value.directoryList.size) {
            val newDirectoryList = _uiState.value.directoryList.toMutableList().apply { removeAt(listID) }
            val directory = newDirectoryList[listID].toDirectory()
            _uiState.value = _uiState.value.copy(directoryList = newDirectoryList.toList())
            viewModelScope.launch {
                directoriesRepository.deleteDirectory(directory)
            }
        } else {
            println("deleteDirectory ERROR: No user found")
        }
    }
    fun changeDirectoryName(listID: Int, name: String) {
        if(listID < _uiState.value.directoryList.size) {
            val newDirectoryList = _uiState.value.directoryList.toMutableList()
            newDirectoryList[listID].name = name
            _uiState.value = _uiState.value.copy(directoryList = newDirectoryList.toList())
            viewModelScope.launch {
                val directory = newDirectoryList[listID].toDirectory()
                directoriesRepository.updateDirectory(directory)
            }
        }
        else {
            println("changeDirectoryName ERROR: No user found")
        }
    }

    fun backdoor() {
        _uiState.value = _uiState.value.copy(userID = 0)
    }
//    fun Updatetext(text: String) {
//        _uiState.value = _uiState.value.copy(SavedText = text)
//    }
}

data class DirectoryUiState(
    val userState: StateFlow<UserState>? = null,
    val userID: Int? = null,
    val directoryList: List<DirectoryDetails> = listOf<DirectoryDetails>(),
    val sortType: SortType = SortType.Time,
//    val SavedText: String = ""
)

data class UserState(
    val userWithDirectories: UserWithDirectories? = null
)

// directory细节
data class DirectoryDetails(
    var id: Int = 0,
    var name: String = "",
    var userId: Int = 0,
    var time: String = ""
)
fun DirectoryDetails.toDirectory(): Directory = Directory(
    id = id,
    name = name,
    userId = userId,
    time = time
)
fun Directory.toDirectoryDetails(): DirectoryDetails = DirectoryDetails(
    id = id,
    name = name,
    userId = userId,
    time = time
)

enum class SortType {
    Name,
    Time
}
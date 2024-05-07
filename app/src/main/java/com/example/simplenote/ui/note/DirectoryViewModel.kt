package com.example.simplenote.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.DirectoriesRepository
import com.example.simplenote.data.Directory
import com.example.simplenote.data.UserWithDirectories
import com.example.simplenote.data.UsersRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DirectoryViewModel(
    private val usersRepository: UsersRepository,
    private val directoriesRepository: DirectoriesRepository,
): ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
    var userState: StateFlow<UserState>? = null
    var directoryList = mutableListOf<DirectoryDetails>()
    var userID: Int? = null
    fun init(userID_: Int? = null) {
        userID_?.let {uid ->
            userState =
            usersRepository.getNotebookWithNotes(uid)
                .map { UserState(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = UserState()
                )
        }
        directoryList.clear()
        userState?.value?.userWithDirectories?.directories?.forEach { // 打进临时
            directoryList.add(it.toDirectoryDetails())
        }
        userID = userID_
    }
    var sortType: SortType = SortType.Time

    private fun sortBySortType() {
        if(sortType == SortType.Time) {
            directoryList.sortBy { it.time }
        }
        else if(sortType == SortType.Name) {
            directoryList.sortBy { it.name }
        }
    }
    fun changeSortType(sortTypeTo: SortType) {
        sortType = sortTypeTo
    }
    suspend fun insertDirectory(name: String) {
        val directoryDetails: DirectoryDetails = DirectoryDetails(
            name = name,
            userId = userID!!,
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        directoriesRepository.insertDirectory(directoryDetails.toDirectory())
        directoryList.add(directoryDetails)
        sortBySortType()
    }
    suspend fun deleteDirectory(listID: Int) {
        if (listID < directoryList.size) {
            val directory = directoryList[listID].toDirectory()
            directoryList.removeAt(listID) // 不确定要不要删
            directoriesRepository.deleteDirectory(directory)
        } else {
            println("deleteDirectory ERROR: No user found")
        }
    }
    suspend fun changeDirectoryName(listID: Int, name: String) {
        if(listID < directoryList.size) {
            directoryList[listID].name = name
            val directory = directoryList[listID].toDirectory()
            directoriesRepository.updateDirectory(directory)
        }
        else {
            println("changeDirectoryName ERROR: No user found")
        }
    }
}

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
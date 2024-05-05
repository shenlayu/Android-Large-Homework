package com.example.simplenote.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.DirectoriesRepository
import com.example.simplenote.data.Directory
import com.example.simplenote.data.NotebookWithNotes
import com.example.simplenote.data.User
import com.example.simplenote.data.UserWithDirectories
import com.example.simplenote.data.UsersRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DirectoryViewModel(
    private val directoriesRepository: DirectoriesRepository,
    private val usersRepository: UsersRepository,
    val userID: Int
): ViewModel() {
    val userState: StateFlow<UserState> =
        usersRepository.getNotebookWithNotes(userID).map { UserState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = UserState()
            )
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
    var directoryList = mutableListOf<DirectoryDetails>()

    fun init() {
        directoryList.clear()
        userState.value.userWithDirectories?.directories?.forEach {
            directoryList.add(it.toDirectoryDetails())
        }
        sortDirectoryByName()
    }
    fun sortDirectoryByName() {
        directoryList.sortBy { it.name }
    }
    suspend fun insertDirectory(name: String) {
        val directoryDetails: DirectoryDetails = DirectoryDetails(
            name = name
        )
        directoriesRepository.insertDirectory(directoryDetails.toDirectory())
        sortDirectoryByName()
    }
    suspend fun deleteDirectory(listID: Int) {
        if(listID < directoryList.size) {
            val directory = directoryList[listID].toDirectory()
            directoriesRepository.deleteDirectory(directory)
        }
        else {
            println("deleteDirectory ERROR: No user found")
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
    var notebookNum: Int = 0,
    var userId: Int = 0
)
fun DirectoryDetails.toDirectory(): Directory = Directory(
    id = id,
    name = name,
    notebookNum = notebookNum,
    userId = userId
)
fun Directory.toDirectoryDetails(): DirectoryDetails = DirectoryDetails(
    id = id,
    name = name,
    notebookNum = notebookNum,
    userId = userId
)
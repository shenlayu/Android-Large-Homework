package com.example.simplenote.ui.note

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.simplenote.data.User
import com.example.simplenote.data.UsersRepository
import kotlinx.coroutines.flow.firstOrNull

class UserViewModel(
    private val usersRepository: UsersRepository,
): ViewModel() {
    suspend fun insertUser(username: String, password: String) {
        val userDetails = UserDetails(
            username = username,
            password = password
        )
        usersRepository.insertUser(userDetails.toUser())
    }
    suspend fun deleteUser(username: String) {
        val user: User? = usersRepository.searchUser(username).firstOrNull()
        user?.let {
            usersRepository.deleteUser(it)
        } ?: run {
            println("deleteUse ERROR: No user found")
        }
    }
    suspend fun checkUser(username: String, password: String): Boolean {
        val user: User? = usersRepository.searchUser(username).firstOrNull()
        user?.let {
            if (user.password == password) {
                return true
            }
            else {
                return false
            }
        } ?: run {
            println("checkUser ERROR: No user found")
        }
        return false
    }
    suspend fun setPassword(username: String, password: String) {
        val userDetails: UserDetails? = usersRepository.searchUser(username).firstOrNull()?.toUserDetails()
        userDetails?.let {
            userDetails.password = password
            usersRepository.updateUser(userDetails.toUser())
        } ?: run {
            println("setPassword ERROR: No user found")
        }
    }
}

data class UserDetails(
    val id: Int = 0,
    val directoryNum: Int = 0,
    val username: String = "",
    var password: String = "",
    val photo: String = ""
)
fun UserDetails.toUser(): User = User(
    id = id,
    directoryNum = directoryNum,
    username = username,
    password = password,
    photo = photo
)
fun User.toUserDetails(): UserDetails = UserDetails(
    id = id,
    directoryNum = directoryNum,
    username = username,
    password = password,
    photo = photo
)
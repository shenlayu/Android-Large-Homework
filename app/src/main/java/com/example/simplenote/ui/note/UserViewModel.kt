package com.example.simplenote.ui.note

import com.example.simplenote.data.User
import com.example.simplenote.data.UsersRepository

class UserViewModel(
    private val usersRepository: UsersRepository,
) {
    fun insertUser(username: String, password: String) {

    }
    fun deleteUser(username: String) {

    }
    suspend fun checkUser(username: String, password: String) {

    }
}

data class UserDetails(
    val id: Int = 0,
    val directoryNum: Int = 0,
    val username: String = "",
    val password: String = "",
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
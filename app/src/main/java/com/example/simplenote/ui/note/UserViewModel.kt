package com.example.simplenote.ui.note

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.simplenote.data.LoggedUser
import com.example.simplenote.data.LoggedUserRepository
import com.example.simplenote.data.User
import com.example.simplenote.data.UsersRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.math.log

class UserViewModel(
    private val usersRepository: UsersRepository,
    private val loggedUserRRepository: LoggedUserRepository
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
    suspend fun login(username: String) {
        // 将要登录user
        val user: User? = usersRepository.searchUser(username).firstOrNull()
        // 目前登录loggedUser
        val loggedUser: LoggedUser? = loggedUserRRepository.getLoggedUser()
            .map { list: List<LoggedUser> ->
                list.firstOrNull()
            }
            .firstOrNull()
        loggedUser?.let { // 目前有登录用户
            user?.let {
                loggedUserRRepository.updateLoggedUser(
                    LoggedUserDetails(
                        id = loggedUser.id,
                        userId = user.id
                    ).toLoggedUser()
                )
            } ?. run {
                println("login ERROR: No user found")
            }
        } ?. run { // 目前没有登录用户
            user?.let {
                loggedUserRRepository.insertLoggedUser(
                    LoggedUserDetails( userId = user.id ).toLoggedUser()
                )
            } ?. run {
                println("login ERROR: No user found")
            }
        }
    }
    suspend fun logout() {
        val loggedUser: LoggedUser? = loggedUserRRepository.getLoggedUser()
            .map { list: List<LoggedUser> ->
                list.firstOrNull()
            }
            .firstOrNull()
        loggedUser?.let { // 目前有登录用户
            loggedUserRRepository.deleteLoggedUser(loggedUser)
        } ?. run { // 目前没有登录用户
            println("logout ERROR: No user found")
        }
    }
}

data class UserDetails(
    val id: Int = 0,
    val username: String = "",
    var password: String = "",
    val photo: String = ""
)
fun UserDetails.toUser(): User = User(
    id = id,
    username = username,
    password = password,
    photo = photo
)
fun User.toUserDetails(): UserDetails = UserDetails(
    id = id,
    username = username,
    password = password,
    photo = photo
)
data class LoggedUserDetails(
    val id: Int = 0,
    val userId: Int = 0
)
fun LoggedUserDetails.toLoggedUser(): LoggedUser = LoggedUser(
    id = id,
    userId = userId
)
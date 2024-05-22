package com.example.simplenote.ui.note

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.LoggedUser
import com.example.simplenote.data.LoggedUserRepository
import com.example.simplenote.data.User
import com.example.simplenote.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.log

class UserViewModel(
    private val usersRepository: UsersRepository,
    private val loggedUserRRepository: LoggedUserRepository
): ViewModel() {
    private val _uiState = MutableStateFlow((LoggedUserUiState()))
    val uiState: StateFlow<LoggedUserUiState> = _uiState.asStateFlow()

    fun init() {
        // TODO: 将database中存储的loggedUser读到本地，这样碗外面就不再需要loggedUserRRepository了
    }
    fun insertUser(username: String, password: String) {
        val userDetails = UserDetails(
            username = username,
            password = password
        )
        viewModelScope.launch {
            usersRepository.insertUser(userDetails.toUser())
        }
    }
    fun deleteUser(username: String) {
        viewModelScope.launch {
            val user: User? = usersRepository.searchUser(username).firstOrNull()
            user?.let {
                usersRepository.deleteUser(it)
            } ?: run {
                println("deleteUse ERROR: No user found")
            }
        }
    }
    fun checkUser(username: String, password: String): Boolean {
        val user: User? = runBlocking {
            usersRepository.searchUser(username).firstOrNull()
        }
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
    fun setPassword(username: String, password: String) {
        viewModelScope.launch {
            val userDetails: UserDetails? =
                usersRepository.searchUser(username).firstOrNull()?.toUserDetails()
            userDetails?.let {
                userDetails.password = password
                usersRepository.updateUser(userDetails.toUser())
            } ?: run {
                println("setPassword ERROR: No user found")
            }
        }
    }
    fun login(username: String) {
        Log.d("add1", "inViewModel")
//        viewModelScope.launch {
        viewModelScope.launch {
            // 将要登录user
            val user: User? = usersRepository.searchUser(username).firstOrNull()
            // 目前登录loggedUser
            Log.d("add1", "here?")
            _uiState.value.loggedUserDetails?.let { // 目前有登录用户
                Log.d("add1", "why")
                user?.let {
                    Log.d("add1", "userExist")
                    loggedUserRRepository.updateLoggedUser(
                        LoggedUserDetails(
                            id = _uiState.value.loggedUserDetails!!.id,
                            userId = user.id
                        ).toLoggedUser()
                    )
                } ?:run {
                    Log.d("add1", "userNotExist")
                    println("login ERROR: No user found")
                }
            } ?:run { // 目前没有登录用户
                Log.d("add1", "what")
                user?.let {
                    Log.d("add1", "userExist2")
                    loggedUserRRepository.insertLoggedUser(
                        LoggedUserDetails(userId = user.id).toLoggedUser()
                    )
                } ?:run {
                    Log.d("add1", "userNotExist2")
                    println("login ERROR: No user found")
                }
            }
        }
    }
    fun logout() {
//        val loggedUser: LoggedUser? = loggedUserRRepository.getLoggedUser()
//            .map { list: List<LoggedUser> ->
//                list.firstOrNull()
//            }
//            .firstOrNull()
        viewModelScope.launch {
            _uiState.value.loggedUserDetails?.let { // 目前有登录用户
                loggedUserRRepository.deleteLoggedUser(it.toLoggedUser())
            }?:run { // 目前没有登录用户
                println("logout ERROR: No user found")
            }
        }
    }
    fun checkUserExist(username: String): Boolean {
        // TODO
        return false
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
data class LoggedUserUiState (
    val loggedUserDetails: LoggedUserDetails? = null
)
data class LoggedUserDetails(
    val id: Int = 0,
    val userId: Int = 0
)
fun LoggedUserDetails.toLoggedUser(): LoggedUser = LoggedUser(
    id = id,
    userId = userId
)
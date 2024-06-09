package com.example.simplenote.ui.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.DirectoriesRepository
import com.example.simplenote.data.LoggedUser
import com.example.simplenote.data.LoggedUserRepository
import com.example.simplenote.data.User
import com.example.simplenote.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserViewModel(
    private val usersRepository: UsersRepository,
    private val loggedUserRepository: LoggedUserRepository,
    private val directoryRepository: DirectoriesRepository
): ViewModel() {
    private val _uiState = MutableStateFlow((LoggedUserUiState()))
    val uiState: StateFlow<LoggedUserUiState> = _uiState.asStateFlow()

    init {
        // TODO: 将database中存储的loggedUser读到本地，这样碗外面就不再需要loggedUserRRepository了
        viewModelScope.launch {
            val loggedUserDetails: LoggedUserDetails? = loggedUserRepository.getLoggedUser().firstOrNull()?.firstOrNull()?.toLoggedUserDetails()
            _uiState.value = _uiState.value.copy(loggedUserDetails = loggedUserDetails)
        }
    }
    fun insertUser(username: String, password: String) {
        val userDetails = UserDetails(
            username = username,
            password = password
        )
        viewModelScope.launch {
            usersRepository.insertUser(userDetails.toUser())
            val id = usersRepository.searchUser(username = userDetails.username).firstOrNull()!!.id
            val directoryDetails = DirectoryDetails(
                name = "全部笔记",
                userId = id,
                time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
            directoryRepository.insertDirectory(directoryDetails.toDirectory())
            directoryDetails.name = "未分类"
            directoryRepository.insertDirectory(directoryDetails.toDirectory())
            // 默认设置一个头像，在显示图片的时候判断一下是不是default
            setAvatar(username, "default")
            setNickname(username, "未设置昵称")
            setSignature(username, "未设置签名")
        }
    }
    fun deleteUser(username: String) {
        viewModelScope.launch {
            val user: User? = usersRepository.searchUser(username).firstOrNull()
            user?.let {
                usersRepository.deleteUser(it)
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
        }
        return false
    }
    fun setPassword(username: String, password: String) {
        runBlocking {
            val userDetails: UserDetails? =
                usersRepository.searchUser(username).firstOrNull()?.toUserDetails()
            userDetails?.let {
                userDetails.password = password
                usersRepository.updateUser(userDetails.toUser())
            }
        }
    }
    fun setNickname(username: String, nickname: String) {
        runBlocking {
            val userDetails: UserDetails? =
                usersRepository.searchUser(username).firstOrNull()?.toUserDetails()
            userDetails?.let {
                userDetails.nickname = nickname
                usersRepository.updateUser(userDetails.toUser())
            }
        }
    }
    fun setAvatar(username: String, avatar: String) {
        runBlocking {
            val userDetails: UserDetails? =
                usersRepository.searchUser(username).firstOrNull()?.toUserDetails()
            userDetails?.let {
                userDetails.avatar = avatar
                usersRepository.updateUser(userDetails.toUser())
            }
        }
    }
    fun setSignature(username: String, signature: String) {
        runBlocking {
            val userDetails: UserDetails? =
                usersRepository.searchUser(username).firstOrNull()?.toUserDetails()
            userDetails?.let {
                userDetails.signature = signature
                usersRepository.updateUser(userDetails.toUser())
            }
        }
    }
    fun getUser(userID: Int): UserDetails? {
        val user = runBlocking {
            usersRepository.searchUserById(userID).firstOrNull()
        }
        return user?.toUserDetails()
    }
    fun login(username: String) {
//        Log.d("add1", "inViewModel")
//        viewModelScope.launch {

        runBlocking {
            // 将要登录user
            val user: User? = usersRepository.searchUser(username).firstOrNull()
            val currentLoggedUser: LoggedUser? = loggedUserRepository.getLoggedUser().firstOrNull()?.firstOrNull()
//            val currentLoggedUserDetails = _uiState.value.loggedUserDetails
            user?.let {
                _uiState.value = _uiState.value.copy(
                    loggedUserDetails = LoggedUserDetails(userId = it.id)
                )
            }

            // 目前登录loggedUser
//            Log.d("add1", "here?")
            currentLoggedUser?.let { // 目前有登录用户
                Log.d("add1", "why")
                user?.let {
                    loggedUserRepository.updateLoggedUser(
                        LoggedUser(
                            id = currentLoggedUser.id,
                            userId = user.id
                        )
                    )
                }
            } ?:run { // 目前没有登录用户
                user?.let {
//                    Log.d("add1", "userExist2")
                    loggedUserRepository.insertLoggedUser(
                        LoggedUserDetails(userId = user.id).toLoggedUser()
                    )
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
        runBlocking {
            val currentLoggedUser: LoggedUser? = loggedUserRepository.getLoggedUser().firstOrNull()?.firstOrNull()
            currentLoggedUser?.let { // 目前有登录用户
                loggedUserRepository.deleteLoggedUser(it)
                _uiState.value = _uiState.value.copy(loggedUserDetails = null)
            }
        }
    }
    fun checkUserExist(username: String): Boolean {
        // TODO
        var returnVal: Boolean = false
        runBlocking {
            val userDetails: UserDetails? =
                usersRepository.searchUser(username).firstOrNull()?.toUserDetails()
            userDetails?.let {
                returnVal = true
            } ?: run {
                returnVal = false
            }
        }
        return returnVal
    }
    fun getUserAvatar(userID: Int): String {
        var avater: String = ""
        runBlocking {
            val userDetails: UserDetails? =
                usersRepository.searchUserById(userID).firstOrNull()?.toUserDetails()
            userDetails?.let {
                avater = userDetails.avatar
            }
        }
        return avater
    }
    fun getUserNickname(userID: Int): String {
        var nickname: String = ""
        runBlocking {
            val userDetails: UserDetails? =
                usersRepository.searchUserById(userID).firstOrNull()?.toUserDetails()
            userDetails?.let {
                nickname = userDetails.nickname
            }
        }
        return nickname
    }
    fun getUserSignature(userID: Int): String {
        var signature: String = ""
        runBlocking {
            val userDetails: UserDetails? =
                usersRepository.searchUserById(userID).firstOrNull()?.toUserDetails()
            userDetails?.let {
                signature = userDetails.signature
            }
        }
        return signature
    }
}

data class UserDetails(
    val id: Int = 0,
    var username: String = "",
    var password: String = "",
    var nickname: String = "",
    var avatar: String = "",
    var signature: String = ""
)
fun UserDetails.toUser(): User = User(
    id = id,
    username = username,
    password = password,
    nickname = nickname,
    avatar = avatar,
    signature = signature
)
fun User.toUserDetails(): UserDetails = UserDetails(
    id = id,
    username = username,
    password = password,
    nickname = nickname,
    avatar = avatar,
    signature = signature
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
fun LoggedUser.toLoggedUserDetails(): LoggedUserDetails = LoggedUserDetails(
    id = id,
    userId = userId
)
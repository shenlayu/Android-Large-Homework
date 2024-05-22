package com.example.simplenote.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.simplenote.ui.MainScreen
import com.example.simplenote.R
import com.example.simplenote.data.LoggedUser
import com.example.simplenote.data.LoggedUserRepository
import com.example.simplenote.ui.AppViewModelProvider
import com.example.simplenote.ui.EditorScreen
import com.example.simplenote.ui.LoginScreen
import com.example.simplenote.ui.MeScreen
import com.example.simplenote.ui.contentItems
import com.example.simplenote.ui.note.DirectoryViewModel
import com.example.simplenote.ui.note.NoteViewModel
import com.example.simplenote.ui.note.NotebookViewModel
import com.example.simplenote.ui.note.UserViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

enum class pages(@StringRes val title: Int) {
    Main(title = R.string.Main),
    Edit(title = R.string.Edit),
    Me(title = R.string.Me)
}

@Composable
fun NoteNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
//    val userViewModel: UserViewModel =  viewModel(factory = AppViewModelProvider.Factory)
//    val directoryViewModel: DirectoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
//    val notebookViewModel: NotebookViewModel = viewModel(factory = AppViewModelProvider.Factory)
//    val noteViewModel: NoteViewModel = viewModel(factory = AppViewModelProvider.Factory)

    NavHost(
        navController = navController,
        startDestination = pages.Me.name,
    ) {
        // 预览笔记界面
        composable(route = pages.Main.name) {
            MainScreen(
//                directoryViewModel = directoryViewModel,
//                notebookViewModel = notebookViewModel,
//                noteViewModel = noteViewModel,
                navigateToEdit = { navController.navigate(pages.Edit.name) }
            )
        }
        // 编辑笔记界面
        composable(route = pages.Edit.name) {
            EditorScreen(
                contentItems,
//                noteViewModel = noteViewModel,
                navigateToMain = { navController.navigate(pages.Main.name) },
                navigateBack = { navController.navigateUp() }
            )
        }
        // 登录注册界面
        composable(route = pages.Me.name) {
            LoginScreen(
//                userViewModel = userViewModel
                navToMain = { navController.navigate(pages.Main.name) }
            )
        }
    }









//    NavHost(
//        navController = navController,
//        startDestination = pages.Main.name,
//    ) {
//
//        // 预览笔记界面
//        composable(route = pages.Main.name) {
//            var loggedUser: LoggedUser? = null
//            runBlocking {
//                loggedUser = loggedUserRepository.getLoggedUser()
//                    .map { list: List<LoggedUser> ->
//                        list.firstOrNull()
//                    }
//                    .firstOrNull()
//            } // 获得登录用户
//            loggedUser?.let { // 已登录
//                if(loggedUser?.userId == savingLoggedUserID) { // 登录用户没有变化，不需要init
//                    val defaultDirectory = directoryViewModel.directoryList.firstOrNull()
//                    if(defaultDirectory != null) { // 该用户有directory
//                        MainScreen(
//                            directoryViewModel = directoryViewModel,
//                            notebookViewModel = notebookViewModel,
//                            noteViewModel = noteViewModel,
//                            logged = true,
//                            havingDirectory = true,
//                            navigateToEdit = { navController.navigate(pages.Edit.name) }
//                        )
//                    } else { // 该用户没有directory
//                        MainScreen(
//                            directoryViewModel = directoryViewModel,
//                            notebookViewModel = notebookViewModel,
//                            noteViewModel = noteViewModel,
//                            logged = true,
//                            havingDirectory = false,
//                            navigateToEdit = { navController.navigate(pages.Edit.name) }
//                        )
//                    }
//                } else { // 登录用户变了，需要init
//                    savingLoggedUserID = loggedUser?.userId
//                    directoryViewModel.init(loggedUser!!.userId)
//                    val defaultDirectory = directoryViewModel.directoryList.firstOrNull()
//                    if(defaultDirectory != null) { // 该用户有directory
//                        notebookViewModel.init(defaultDirectory.id)
//                        MainScreen(
//                            directoryViewModel = directoryViewModel,
//                            notebookViewModel = notebookViewModel,
//                            noteViewModel = noteViewModel,
//                            logged = true,
//                            havingDirectory = true,
//                            navigateToEdit = { navController.navigate(pages.Edit.name) }
//                        )
//                    } else { // 该用户没有directory
//                        MainScreen(
//                            directoryViewModel = directoryViewModel,
//                            notebookViewModel = notebookViewModel,
//                            noteViewModel = noteViewModel,
//                            logged = true,
//                            havingDirectory = false,
//                            navigateToEdit = { navController.navigate(pages.Edit.name) }
//                        )
//                    }
//                }
//            } ?: run { // 未登录
//                MainScreen(logged = false)
//            }
//        }
//        // 编辑笔记界面
//        composable(route = pages.Edit.name) {
//            EditorScreen(
//                contentItems,
//                noteViewModel = noteViewModel,
//                navigateToMain = { navController.navigate(pages.Main.name) },
//                navigateBack = { navController.navigateUp() }
//            )
//        }
//        // 登录注册界面
//        composable(route = pages.Me.name) {
//            MeScreen(userViewModel)
//        }
//    }
}
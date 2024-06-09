package com.example.simplenote.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.simplenote.R
import com.example.simplenote.ui.AppViewModelProvider
import com.example.simplenote.ui.EditorScreen
import com.example.simplenote.ui.LoginScreen
import com.example.simplenote.ui.MainScreen
import com.example.simplenote.ui.MeScreen
import com.example.simplenote.ui.PasswordScreen
import com.example.simplenote.ui.RegisterScreen
import com.example.simplenote.ui.WelcomeScreen
import com.example.simplenote.ui.contentItems
import com.example.simplenote.ui.note.NoteViewModel
import com.example.simplenote.ui.note.NotebookViewModel
import com.example.simplenote.ui.note.UserViewModel

enum class pages(@StringRes val title: Int) {
    Main(title = R.string.Main),
    Edit(title = R.string.Edit),
    Me(title = R.string.Me),
    Login(title = R.string.Login),
    Register(title = R.string.Register),
    Welcome(title = R.string.Welcome),
    Password(title = R.string.Password)
}

@Composable
fun NoteNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val userViewModel: UserViewModel =  viewModel(factory = AppViewModelProvider.Factory)
//    val directoryViewModel: DirectoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val notebookViewModel: NotebookViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val noteViewModel: NoteViewModel = viewModel(factory = AppViewModelProvider.Factory)

    NavHost(
        navController = navController,
        startDestination = pages.Welcome.name,
    ) {
        // 预览笔记界面
        composable(route = pages.Main.name) {
            MainScreen(
//                directoryViewModel = directoryViewModel,
                userViewModel = userViewModel,
                notebookViewModel = notebookViewModel,
                noteViewModel = noteViewModel,
                navigateToEdit = { navController.navigate(pages.Edit.name) },
                navigateToMe = {navController.navigate(pages.Me.name)},
                navigateToMain = { navController.navigate(pages.Main.name) }
            )
        }
        // 编辑笔记界面
        composable(route = pages.Edit.name) {
            EditorScreen(
                contentItems,
                notebookViewModel = notebookViewModel,
                noteViewModel = noteViewModel,
                navigateToMain = { navController.navigate(pages.Main.name) },
                navigateBack = { navController.navigateUp() },
            )
        }
        // 登录注册界面
        composable(route = pages.Login.name) {
            LoginScreen(
                userViewModel = userViewModel,
                navigateToMain = { navController.navigate(pages.Main.name) },
                navigateToWelcome = { navController.navigate(pages.Welcome.name) },
            )
        }
        composable(route = pages.Me.name) {
            MeScreen(
                userViewModel = userViewModel,
                navigateToPassword = { navController.navigate(pages.Password.name) },
                navigateToMain = { navController.navigate(pages.Main.name) },
                navigateToWelcome = { navController.navigate(pages.Welcome.name) }
            )
        }
        composable(route = pages.Register.name) {
            RegisterScreen(
                userViewModel = userViewModel,
                navigateToLogin = { navController.navigate(pages.Login.name) },
                navigateToWelcome = { navController.navigate(pages.Welcome.name) },
            )
        }
        composable(route = pages.Welcome.name) {
            WelcomeScreen(
                userViewModel = userViewModel,
                navigateToLogin = { navController.navigate(pages.Login.name) },
                navigateToRegister = { navController.navigate(pages.Register.name) },
                navigateToMain = { navController.navigate(pages.Main.name) }
            )
        }
        composable(route = pages.Password.name) {
            PasswordScreen(
                userViewModel = userViewModel,
                navigateToMe = { navController.navigate(pages.Me.name) }
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
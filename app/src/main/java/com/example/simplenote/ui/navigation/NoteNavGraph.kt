package com.example.simplenote.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.simplenote.MainScreen
import com.example.simplenote.R

enum class pages(@StringRes val title: Int) {
    Main(title = R.string.Main),
    Edit(title = R.string.Edit)
}

@Composable
fun NoteNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    NavHost(
        navController = navController,
        startDestination = pages.Main.name,
    ) {
        composable(route = pages.Edit.name) {
            MainScreen(

            )
        }
        composable(route = pages.Edit.name) {

        }
    }
}
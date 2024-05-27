package com.example.simplenote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.ui.note.UserViewModel

@Composable
fun WelcomeScreen(
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToLogin: () -> Unit = {},
    navigateToRegister: () -> Unit = {},
    navigateToMain: () -> Unit = {}
    ) {
    val localUserUiState by userViewModel.uiState.collectAsState()
    if(localUserUiState.loggedUserDetails != null) {
        navigateToMain()
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = navigateToLogin,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("登录")
        }

        Button(
            onClick = navigateToRegister,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("注册")
        }
    }
}
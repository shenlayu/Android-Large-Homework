package com.example.simplenote.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.R
import com.example.simplenote.ui.note.UserViewModel

@Composable
fun WelcomeScreen(
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToLogin: () -> Unit = {},
    navigateToRegister: () -> Unit = {},
    navigateToMain: () -> Unit = {}
) {
    val localUserUiState by userViewModel.uiState.collectAsState()
    if (localUserUiState.loggedUserDetails != null) {
        navigateToMain()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.bijiben),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
//        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
//                .weight(1f)
        ) {
            Button(
                onClick = navigateToLogin,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text("登录")
            }
            Button(
                onClick = navigateToRegister,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text("注册")
            }
        }

    }
}
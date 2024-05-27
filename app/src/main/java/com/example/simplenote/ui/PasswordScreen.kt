package com.example.simplenote.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.ui.note.UserDetails
import com.example.simplenote.ui.note.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToMe: () -> Unit = {}
) {
    val localUserUiState by userViewModel.uiState.collectAsState()

    val localUserDetails: UserDetails? =
        if(localUserUiState.loggedUserDetails != null)
            userViewModel.getUser(localUserUiState.loggedUserDetails!!.userId)
        else
            null
    val username = localUserDetails?.username

    var password by rememberSaveable { mutableStateOf("") }
    var confirm_password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
        )
        TextField(
            value = confirm_password,
            onValueChange = { confirm_password = it },
            label = { Text("确认密码") },
            modifier = Modifier
                .fillMaxWidth()
        )
        Button(
            onClick = {
                username?.let {
                    if(password == confirm_password) {
                        userViewModel.setPassword(username, password)
                        navigateToMe()
                    }
                    else {
                        // todo
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("确定")
        }
    }
}
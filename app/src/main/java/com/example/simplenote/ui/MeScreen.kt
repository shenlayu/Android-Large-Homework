package com.example.simplenote.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.R
import com.example.simplenote.data.User
import com.example.simplenote.ui.note.UserDetails
import com.example.simplenote.ui.note.UserViewModel

@Composable
fun MeScreen(
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToMain: () -> Unit = {},
    navigateToPassword: () -> Unit = {},
    navigateToWelcome: () -> Unit = {}
) {
//    Log.d("add1", "in")
    val localUserUiState by userViewModel.uiState.collectAsState()
//    Log.d("add1", "in2 ${localUserUiState.loggedUserDetails}")

//    Log.d("add1", "in3")

    var localUserDetails: UserDetails? = null
    localUserUiState.loggedUserDetails?.let {
        localUserDetails = userViewModel.getUser(localUserUiState.loggedUserDetails!!.userId)
    }

    var nickname by remember { mutableStateOf(TextFieldValue(localUserDetails?.nickname ?: ""))}
    var isEditingNickname by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
        Image(
            painter = painterResource(id = R.drawable.avatar), // Placeholder avatar
            contentDescription = "Avatar",
            modifier = Modifier
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        if (isEditingNickname) {
            TextField(
                value = nickname,
                onValueChange = { nickname = it },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                localUserDetails?.let {
                    userViewModel.setNickname(localUserDetails!!.username, nickname.text)
                }
                isEditingNickname = false
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.avatar), // Check icon resource
                    contentDescription = "Save"
                )
            }
        } else {
            Text(
                text = nickname.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { isEditingNickname = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.avatar), // Edit icon resource
                    contentDescription = "Edit"
                )
            }
        }
    }

        Divider(color = Color.Gray, thickness = 1.dp)

        Button(
            onClick = navigateToPassword,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("设置密码")
        }

        Button(
            onClick = {
                userViewModel.logout()
                navigateToWelcome()
                      },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("登出")
        }
        Button(
            onClick = navigateToMain,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("去预览")
        }
    }
}
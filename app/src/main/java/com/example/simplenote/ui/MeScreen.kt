package com.example.simplenote.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.R
import com.example.simplenote.ui.note.UserViewModel

@Composable
fun MeScreen(
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToPassword: () -> Unit = {},
    navigateToMain: () -> Unit = {},
    navigateToWelcome: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(1) }
    val localUserUiScale by userViewModel.uiState.collectAsState()
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "笔记") },
                    label = { Text("笔记") },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navigateToMain()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "我的") },
                    label = { Text("我的") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.avatar), // Placeholder avatar
                contentDescription = "头像",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(

                text = userViewModel.getUserNickname(localUserUiScale.loggedUserDetails!!.userId),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold

            )

            Spacer(modifier = Modifier.padding(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    modifier = Modifier.padding(end = 16.dp),
                    onClick = navigateToPassword
                ) {
                    Text("重置密码")
                }
                Button(
                    onClick = {
                        userViewModel.logout()
                        navigateToWelcome()
                    },
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text("退出登录")
                }
            }
        }
    }
}

package com.example.simplenote.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.ui.note.UserViewModel

@Composable
fun MeScreen(
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {

}
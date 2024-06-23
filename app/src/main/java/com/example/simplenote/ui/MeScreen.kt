package com.example.simplenote.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.simplenote.R
import com.example.simplenote.ui.note.UserViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeScreen(
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToPassword: () -> Unit = {},
    navigateToMain: () -> Unit = {},
    navigateToWelcome: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(1) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showSignatureDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    val localUserUiScale by userViewModel.uiState.collectAsState()
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var avatarUri_c by remember { mutableStateOf<Uri?>(null) }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    val context = LocalContext.current
//    Log.d("iii", userViewModel.getUserAvatar(localUserUiScale.loggedUserDetails!!.userId))

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val newUri = copyUriToInternalStorage(context, it, timestamp)
                if (newUri != null) {
                    avatarUri = newUri
                    userViewModel.setAvatar(userViewModel.getUser(localUserUiScale.loggedUserDetails!!.userId)!!.username, newUri.toString())
                }
            }
        }
    )

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success && avatarUri_c != null) {
                // 更新本地的 avatarUri 以触发即时更新
                avatarUri = avatarUri_c
                userViewModel.setAvatar(userViewModel.getUser(localUserUiScale.loggedUserDetails!!.userId)!!.username, avatarUri_c.toString())
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createImageUri(context)
                if (uri != null) {
                    avatarUri_c = uri
                    takePhotoLauncher.launch(uri)
                }
            } else {
                // Handle permission denied case
                Log.d("MeScreen", "Camera permission denied")
            }
        }
    )

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
            var avatar: String = ""
            if(avatarUri?.toString() != null) {
                avatar = avatarUri?.toString()!!
            }
            else {
                localUserUiScale.loggedUserDetails ?. let {
                    avatar = userViewModel.getUserAvatar(localUserUiScale.loggedUserDetails!!.userId)
                }
            }
//            val avatar = avatarUri?.toString() ?: userViewModel.getUserAvatar(localUserUiScale.loggedUserDetails!!.userId)
//            val avatar = userViewModel.getUserAvatar(localUserUiScale.loggedUserDetails!!.userId)
//            Log.d("add1", "avatar me ${userViewModel.getUserAvatar(localUserUiScale.loggedUserDetails!!.id)}")
//            Log.d("add1", "avatar me $avatar}")
            Image(
                painter = if (avatar == "default") {
                    painterResource(id = R.drawable.avatar)
                } else {
                    rememberAsyncImagePainter(model = Uri.parse(avatar))
                },
                contentDescription = "头像",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable {
                        showAvatarDialog = true
                    }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = if(localUserUiScale.loggedUserDetails != null) userViewModel.getUserNickname(localUserUiScale.loggedUserDetails!!.userId) else "",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { showNicknameDialog = true }
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = if(localUserUiScale.loggedUserDetails != null) userViewModel.getUserSignature(localUserUiScale.loggedUserDetails!!.userId) else "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = androidx.compose.ui.graphics.Color.Gray,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { showSignatureDialog = true }
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

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onDismiss = { showAvatarDialog = false },
            onTakePhoto = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    val uri = createImageUri(context)
                    if (uri != null) {
                        avatarUri_c = uri
                        takePhotoLauncher.launch(uri)
                    }
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                showAvatarDialog = false
            },
            onSelectFromGallery = {
                pickImageLauncher.launch("image/*")
                showAvatarDialog = false
            }
        )
    }

    if (showNicknameDialog) {
        EditNicknameDialog(
            currentNickname = userViewModel.getUserNickname(localUserUiScale.loggedUserDetails!!.userId),
            onDismiss = { showNicknameDialog = false },
            onSave = { newNickname ->
                userViewModel.setNickname(userViewModel.getUser(localUserUiScale.loggedUserDetails!!.userId)!!.username, newNickname)
                showNicknameDialog = false
            }
        )
    }

    if (showSignatureDialog) {
        EditSignatureDialog(
            currentSignature = userViewModel.getUserSignature(localUserUiScale.loggedUserDetails!!.userId),
            onDismiss = { showSignatureDialog = false },
            onSave = { newSignature ->
                userViewModel.setSignature(userViewModel.getUser(localUserUiScale.loggedUserDetails!!.userId)!!.username, newSignature)
                showSignatureDialog = false
            }
        )
    }

    LaunchedEffect(avatarUri) {
        if (avatarUri != null) {
            userViewModel.setAvatar(userViewModel.getUser(localUserUiScale.loggedUserDetails!!.userId)!!.username, avatarUri.toString())
        }
    }
}

@Composable
fun AvatarSelectionDialog(onDismiss: () -> Unit, onTakePhoto: () -> Unit, onSelectFromGallery: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择头像") },
        text = {
            Column {
                Button(onClick = onTakePhoto) {
                    Text("拍照导入")
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Button(onClick = onSelectFromGallery) {
                    Text("本机导入")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun EditNicknameDialog(currentNickname: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var newNickname by remember { mutableStateOf(currentNickname) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑昵称") },
        text = {
            Column {
                TextField(
                    value = newNickname,
                    onValueChange = { newNickname = it },
                    label = { Text("昵称") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(newNickname) }) {
                Text("保存")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun EditSignatureDialog(currentSignature: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var newSignature by remember { mutableStateOf(currentSignature) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑签名") },
        text = {
            Column {
                TextField(
                    value = newSignature,
                    onValueChange = { newSignature = it },
                    label = { Text("签名") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(newSignature) }) {
                Text("保存")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

fun createImageUri(context: Context): Uri? {
    return try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        val imageFile = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun copyUriToInternalStorage(context: Context, uri: Uri, filename: String): Uri? {
    return try {
        val destinationFile = File(context.filesDir, filename)
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream: OutputStream = FileOutputStream(destinationFile)

        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()

        Uri.fromFile(destinationFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

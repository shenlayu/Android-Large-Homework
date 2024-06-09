package com.example.simplenote.ui

import android.content.ClipData
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import com.example.simplenote.R
import com.example.simplenote.data.NoteType
import com.example.simplenote.ui.note.DirectoryViewModel
import com.example.simplenote.ui.note.NoteDetails
import com.example.simplenote.ui.note.NoteViewModel
import com.example.simplenote.ui.note.NotebookViewModel
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 为浏览编辑界面临时创建的数据类，文字类、图片类、音频类

val undoStack: MutableList<List<ContentItem>> = mutableListOf()
// 被撤销内容项列表的栈，用于重做操作
val redoStack: MutableList<List<ContentItem>> = mutableListOf()

sealed class ContentItem {
    abstract fun copy(): ContentItem

    data class TextItem(
        var text: MutableState<TextFieldValue>,
        val isTitle: Boolean = false,
        val isFocused: MutableState<Boolean> = mutableStateOf(false)
    ) : ContentItem() {
        override fun copy(): ContentItem {
            // 创建新的 MutableState 实例，复制原始值
            return TextItem(
                text = mutableStateOf(text.value.copy()),
                isTitle = isTitle,
                isFocused = mutableStateOf(isFocused.value)
            )
        }
    }

    data class ImageItem(val imageUri: Uri) : ContentItem() {
        override fun copy(): ContentItem {
            // URI 是不可变的，可以直接复用
            return ImageItem(imageUri)
        }
    }

    data class AudioItem(val audioUri: Uri) : ContentItem() {
        override fun copy(): ContentItem {
            // URI 是不可变的，可以直接复用
            return AudioItem(audioUri)
        }
    }

    data class VideoItem(val videoUri: Uri) : ContentItem() {
        override fun copy(): ContentItem {
            // URI 是不可变的，可以直接复用
            return VideoItem(videoUri)
        }
    }
}

@Composable
fun covertNoteDetailsToContentItem(noteDetails: NoteDetails): ContentItem {
    var contentItem: ContentItem? = null
    if (noteDetails.type == NoteType.Text) {
        contentItem = ContentItem.TextItem(
            text = remember { mutableStateOf(TextFieldValue(noteDetails.content)) },
            isTitle = noteDetails.isTitle
        )
    } else if (noteDetails.type == NoteType.Photo) {
        contentItem = ContentItem.ImageItem(Uri.parse(noteDetails.content))
    } else if (noteDetails.type == NoteType.Audio) {
        contentItem = ContentItem.AudioItem(Uri.parse(noteDetails.content))
    } else if (noteDetails.type == NoteType.Video) {
        contentItem = ContentItem.VideoItem(Uri.parse(noteDetails.content))
    }
    return contentItem!!
}

@Composable
fun convertToContentItemList(noteDetailsList: List<NoteDetails>): List<ContentItem> {
    val contentItemList: MutableList<ContentItem> = emptyList<ContentItem>().toMutableList()
    noteDetailsList.forEach {
        contentItemList.add(covertNoteDetailsToContentItem(it))
    }
    return contentItemList
}

// 现在修改 saveState、undo 和 redo 方法，使用 copy 方法
fun saveState(contentItems: List<ContentItem>) {
    if (undoStack.size >= 10) {
        undoStack.removeAt(0)
    }
    undoStack.add(contentItems.map { it.copy() })  // 使用 copy 方法深拷贝
}

fun undo(contentItems: MutableState<MutableList<ContentItem>>) {
    if (undoStack.isNotEmpty()) {
        redoStack.add(contentItems.value.map { it.copy() })  // 使用 copy 方法深拷贝
        contentItems.value = undoStack.removeAt(undoStack.lastIndex).toMutableList()
    }
}

fun redo(contentItems: MutableState<MutableList<ContentItem>>) {
    if (redoStack.isNotEmpty()) {
        undoStack.add(contentItems.value.map { it.copy() })  // 使用 copy 方法深拷贝
        contentItems.value = redoStack.removeAt(redoStack.lastIndex).toMutableList()
    }
}

// 清空重做栈，当用户进行了新的编辑操作时调用
fun clearRedoStack() {
    redoStack.clear()
}

val sampleTitleItem = ContentItem.TextItem(mutableStateOf(TextFieldValue("标题")), isTitle = true)
val sampleTextItem = ContentItem.TextItem(mutableStateOf(TextFieldValue("正文")))

val contentItems = mutableStateOf(mutableListOf<ContentItem>(sampleTitleItem, sampleTextItem))

@Composable
fun EditorScreen(
    contentItems: MutableState<MutableList<ContentItem>>,
    noteViewModel: NoteViewModel = viewModel(factory = AppViewModelProvider.Factory),
    notebookViewModel: NotebookViewModel = viewModel(factory = AppViewModelProvider.Factory),
    directoryViewModel: DirectoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToMain: () -> Unit = {},
    navigateBack: () -> Unit = {},
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val currentDate = remember { dateFormat.format(Date()) }
    val isAIDialogOpen = remember { mutableStateOf(false) }
    val isSearchDialogOpen = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    val aiSummary = remember { mutableStateOf("") }
    val localNoteUiState by noteViewModel.uiState.collectAsState()

    var canLaunch by rememberSaveable { mutableStateOf(true) }
    if (canLaunch && localNoteUiState.noteList.isNotEmpty()) {
        contentItems.value = convertToContentItemList(localNoteUiState.noteList).toMutableList()
        canLaunch = false
    }

    val totalCharacters = contentItems.value.sumOf {
        when (it) {
            is ContentItem.TextItem -> it.text.value.text.length
            else -> 0
        }
    }
    val directoryName = localNoteUiState.notebookId
        ?.let { notebookViewModel.getNotebook(it) }
        ?.let { notebook -> directoryViewModel.getDirectory(notebook.directoryId)?.name }
        ?: ""

    val searchTerm = remember { mutableStateOf("") }
    val matches = remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    val currentMatchIndex = remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            EditorTopBar(
                onBack = navigateToMain,
                onUndo = {
                    undo(contentItems = contentItems)
                    matches.value = searchAndHighlight(contentItems, searchTerm.value)
                },
                onRedo = {
                    redo(contentItems = contentItems)
                    matches.value = searchAndHighlight(contentItems, searchTerm.value)
                },
                onSearch = {
                    isSearchDialogOpen.value = true
                },
                onDone = {
                    noteViewModel.saveNotes(contentItems.value)
                    notebookViewModel.sortBySortType()
                    canLaunch = true
                    notebookViewModel.updateNotebookChangeTime()
                }
            )
        },
        bottomBar = {
            ControlPanel(contentItems, LocalContext.current, onAIClick = { isAIDialogOpen.value = true })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column {
                InfoBar(currentDate, totalCharacters, directoryName)
                Divider(color = Color.LightGray, thickness = 1.dp)
                LazyColumn {
                    itemsIndexed(contentItems.value) { index, item ->
                        when (item) {
                            is ContentItem.TextItem -> EditTextItem(
                                textItem = item,
                                searchTerm = searchTerm.value,
                                matches = matches.value,
                                currentMatchIndex = currentMatchIndex.value,
                                index = index,
                                onTextChange = { idx, newValue ->
                                    matches.value = searchAndHighlight(contentItems, searchTerm.value)
                                }
                            )
                            is ContentItem.ImageItem -> DisplayImageItem(item, contentItems, index)
                            is ContentItem.AudioItem -> DisplayAudioItem(item, LocalContext.current, contentItems, index)
                            is ContentItem.VideoItem -> DisplayVideoItem(item, LocalContext.current, contentItems, index)
                        }
                    }
                }
            }
            SearchNavigation(
                searchTerm = searchTerm,
                matches = matches,
                currentMatchIndex = currentMatchIndex,
                contentItems = contentItems,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    AIDialog(isDialogOpen = isAIDialogOpen, contentItems, isLoading, aiSummary)
    SearchDialog(isDialogOpen = isSearchDialogOpen) { searchQuery ->
        searchTerm.value = searchQuery
        matches.value = searchAndHighlight(contentItems, searchTerm.value)
        currentMatchIndex.value = 0
    }
    LoadingDialog(isLoading = isLoading)
    AISummaryDialog(aiSummary = aiSummary)
}

@Composable
fun EditTextItem(
    textItem: ContentItem.TextItem,
    searchTerm: String,
    matches: List<Pair<Int, Int>>,
    currentMatchIndex: Int,
    index: Int,
    onTextChange: (Int, TextFieldValue) -> Unit
) {
    val annotatedText = buildAnnotatedString {
        append(textItem.text.value.text)
        matches.filter { it.first == index }.forEachIndexed { matchIndex, range ->
            addStyle(
                style = SpanStyle(
                    background = if (matchIndex == currentMatchIndex) Color.Yellow else Color.LightGray
                ),
                start = range.second,
                end = range.second + searchTerm.length
            )
        }
    }
    val textFieldValue = textItem.text.value.copy(annotatedString = annotatedText)

    TextField(
        value = textFieldValue,
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .onFocusChanged { focusState ->
                textItem.isFocused.value = focusState.isFocused
            },
        onValueChange = { newValue ->
            saveState(contentItems.value)
            clearRedoStack()
            textItem.text.value = newValue
            onTextChange(index, newValue)
        },
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = if (textItem.isTitle) 20.sp else 14.sp, // 标题使用更大的字体
            color = Color.Black
        ),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        colors = TextFieldDefaults.colors(
            cursorColor = Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        )
    )
}






// 在topbar和底下的编辑区之间加一行小字，小字显示分三栏，分别显示当前日期、该笔记总字数、该笔记所属笔记本名称
@Composable
fun InfoBar(currentDate: String, totalCharacters: Int, directoryName: String) {

    Row(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = currentDate,
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.Bottom)
        )
        Divider(
            color = Color.LightGray,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .align(Alignment.Bottom)
                .height(16.dp)
                .width(1.dp)
        )
        Text(
            text = "字数: $totalCharacters",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.Bottom)
        )
        Divider(
            color = Color.LightGray,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .align(Alignment.Bottom)
                .height(16.dp)
                .width(1.dp)
        )
        Text(
            text = directoryName,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.Bottom)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopBar(
    onBack: () -> Unit = {},
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSearch: () -> Unit,
    onDone: () -> Unit
) {
    val isUndoEnabled = remember { mutableStateOf(undoStack.isNotEmpty()) }
    val isRedoEnabled = remember { mutableStateOf(redoStack.isNotEmpty()) }

    LaunchedEffect(undoStack.size, redoStack.size) {
        isUndoEnabled.value = undoStack.isNotEmpty()
        isRedoEnabled.value = redoStack.isNotEmpty()
    }

    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(painter = rememberAsyncImagePainter(R.drawable.baseline_arrow_back_24), contentDescription = "返回")
            }
        },
        actions = {
            // Undo button
            IconButton(onClick = onUndo, enabled = isUndoEnabled.value) {
                Icon(
                    painter = rememberAsyncImagePainter(R.drawable.baseline_undo_24),
                    contentDescription = "撤回",
                    tint = if (isUndoEnabled.value) Color.Unspecified else Color.Gray
                )
            }
            // Redo button
            IconButton(onClick = onRedo, enabled = isRedoEnabled.value) {
                Icon(
                    painter = rememberAsyncImagePainter(R.drawable.baseline_redo_24),
                    contentDescription = "重做",
                    tint = if (isRedoEnabled.value) Color.Unspecified else Color.Gray
                )
            }
            // Search button
            IconButton(onClick = onSearch) {
                Icon(painter = rememberAsyncImagePainter(R.drawable.baseline_search_24), contentDescription = "搜索")
            }
            // Done button
            IconButton(onClick = onDone) {
                Icon(painter = rememberAsyncImagePainter(R.drawable.baseline_save_24), contentDescription = "保存")
            }
        }
    )
}

@Composable
fun ControlPanel(contentItems: MutableState<MutableList<ContentItem>>, context: Context, onAIClick: () -> Unit) {

    val imageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uri ->
                val newUri = copyUriToInternalStorage(context, uri, "copied_image_${System.currentTimeMillis()}.jpg")
                newUri?.let {
                    saveState(contentItems.value)
                    clearRedoStack()
                    val items = contentItems.value.toMutableList()

                    val index = items.indexOfFirst { it is ContentItem.TextItem && it.isFocused.value }
                    if (index != -1) {
                        val currentItem = items[index] as ContentItem.TextItem
                        val cursorPosition = currentItem.text.value.selection.start
                        val newTextItem = ContentItem.TextItem(mutableStateOf(TextFieldValue("")))
                        when (cursorPosition) {
                            currentItem.text.value.text.length -> {
                                // 光标在末尾
                                items.add(index + 1, ContentItem.ImageItem(it))
                                items.add(index + 2, newTextItem)
                            }
                            0 -> {
                                // 光标在开头
                                newTextItem.isFocused.value = true
                                items.add(index, newTextItem)
                                items.add(index + 1, ContentItem.ImageItem(it))
                            }
                            else -> {
                                // 光标在中间
                                val textBefore = currentItem.text.value.text.substring(0, cursorPosition)
                                val textAfter = currentItem.text.value.text.substring(cursorPosition)
                                val firstTextItem = ContentItem.TextItem(mutableStateOf(TextFieldValue(textBefore)))
                                firstTextItem.isFocused.value = true
                                val secondTextItem = ContentItem.TextItem(mutableStateOf(TextFieldValue(textAfter)))
                                items[index] = firstTextItem
                                items.add(index + 1, ContentItem.ImageItem(it))
                                items.add(index + 2, secondTextItem)
                            }
                        }
                    }
                    contentItems.value = items
                }
            }
        }

    val audioLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uri ->
                val newUri = copyUriToInternalStorage(context, uri, "copied_audio_${System.currentTimeMillis()}.mp3")
                newUri?.let {
                    saveState(contentItems.value)
                    clearRedoStack()
                    val items = contentItems.value.toMutableList()

                    val index = items.indexOfFirst { it is ContentItem.TextItem && it.isFocused.value }
                    if (index != -1) {
                        val currentItem = items[index] as ContentItem.TextItem
                        val cursorPosition = currentItem.text.value.selection.start
                        val newTextItem = ContentItem.TextItem(mutableStateOf(TextFieldValue("")))
                        when (cursorPosition) {
                            currentItem.text.value.text.length -> {
                                // 光标在末尾
                                items.add(index + 1, ContentItem.AudioItem(it))
                                items.add(index + 2, newTextItem)
                            }
                            0 -> {
                                // 光标在开头
                                newTextItem.isFocused.value = true
                                items.add(index, newTextItem)
                                items.add(index + 1, ContentItem.AudioItem(it))
                            }
                            else -> {
                                // 光标在中间
                                val textBefore = currentItem.text.value.text.substring(0, cursorPosition)
                                val textAfter = currentItem.text.value.text.substring(cursorPosition)
                                val firstTextItem = ContentItem.TextItem(mutableStateOf(TextFieldValue(textBefore)))
                                firstTextItem.isFocused.value = true
                                val secondTextItem = ContentItem.TextItem(mutableStateOf(TextFieldValue(textAfter)))
                                items[index] = firstTextItem
                                items.add(index + 1, ContentItem.AudioItem(it))
                                items.add(index + 2, secondTextItem)
                            }
                        }
                    }
                    contentItems.value = items
                }
            }
        }

    val videoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uri ->
                val newUri = copyUriToInternalStorage(context, uri, "copied_video_${System.currentTimeMillis()}.mp4")
                newUri?.let {
                    saveState(contentItems.value)
                    clearRedoStack()
                    val items = contentItems.value.toMutableList()

                    val index = items.indexOfFirst { it is ContentItem.TextItem && it.isFocused.value }
                    if (index != -1) {
                        val currentItem = items[index] as ContentItem.TextItem
                        val cursorPosition = currentItem.text.value.selection.start
                        val newTextItem = ContentItem.TextItem(mutableStateOf(TextFieldValue("")))
                        when (cursorPosition) {
                            currentItem.text.value.text.length -> {
                                // 光标在末尾
                                items.add(index + 1, ContentItem.VideoItem(it))
                                items.add(index + 2, newTextItem)
                            }
                            0 -> {
                                // 光标在开头
                                newTextItem.isFocused.value = true
                                items.add(index, newTextItem)
                                items.add(index + 1, ContentItem.VideoItem(it))
                            }
                            else -> {
                                // 光标在中间
                                val textBefore = currentItem.text.value.text.substring(0, cursorPosition)
                                val textAfter = currentItem.text.value.text.substring(cursorPosition)
                                val firstTextItem = ContentItem.TextItem(mutableStateOf(TextFieldValue(textBefore)))
                                firstTextItem.isFocused.value = true
                                val secondTextItem = ContentItem.TextItem(mutableStateOf(TextFieldValue(textAfter)))
                                items[index] = firstTextItem
                                items.add(index + 1, ContentItem.VideoItem(it))
                                items.add(index + 2, secondTextItem)
                            }
                        }
                    }
                    contentItems.value = items
                }
            }
        }

    val currentFocusIsTitle =
        contentItems.value.any { it is ContentItem.TextItem && it.isFocused.value && it.isTitle }

    BottomAppBar(modifier = Modifier.imePadding()) {
        // Image Button
        IconButton(
            onClick = { if (!currentFocusIsTitle) imageLauncher.launch("image/*") },
            enabled = !currentFocusIsTitle
        ) {
            Icon(
                painter = rememberAsyncImagePainter(R.drawable.ic_add_photo),
                contentDescription = "Add Image"
            )
        }

        Spacer(Modifier.weight(1f, true))

        // AI Button
        IconButton(onClick = onAIClick) {
            Icon(
                painter = painterResource(R.drawable.ic_ai_24),
                contentDescription = "AI Summary",
                tint = Color.Unspecified
            )
        }

        Spacer(Modifier.weight(1f, true))
        // Audio Button
        IconButton(
            onClick = { if (!currentFocusIsTitle) audioLauncher.launch("audio/*") },
            enabled = !currentFocusIsTitle
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_audio),
                contentDescription = "Add Audio"
            )
        }

        Spacer(Modifier.weight(1f, true))
        // Video Button
        IconButton(
            onClick = { if (!currentFocusIsTitle) videoLauncher.launch("video/*") },
            enabled = !currentFocusIsTitle
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_video_library_24),
                contentDescription = "Add Video"
            )
        }
    }
}

@Composable
fun SearchDialog(isDialogOpen: MutableState<Boolean>, onSearch: (String) -> Unit) {
    if (isDialogOpen.value) {
        val searchQuery = remember { mutableStateOf("") }
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {
                isDialogOpen.value = false
                onSearch("")
            },
            title = { Text("搜索") },
            text = {
                Column {
                    TextField(
                        value = searchQuery.value,
                        onValueChange = { searchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(128.dp)),
                        placeholder = { Text("请输入搜索内容") },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
                        colors = TextFieldDefaults.colors(
                            cursorColor = Color.Black,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDialogOpen.value = false
                        onSearch(searchQuery.value)
                    }
                ) {
                    Text("搜索", fontSize = 14.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogOpen.value = false }) {
                    Text("取消", fontSize = 14.sp)
                }
            }
        )
    }
}


@Composable
fun DisplayImageItem(
    imageItem: ContentItem.ImageItem,
    contentItems: MutableState<MutableList<ContentItem>>,
    index: Int
) {
    val showOptions = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scale = animateFloatAsState(targetValue = if (showOptions.value) 0.95f else 1f)

    // 震动效果实现
    fun triggerVibration() {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator

        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    // 使用Box包裹原有的Column，以便于处理点击外部区域隐藏ItemOptionsBar的逻辑
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // 如果ItemOptionsBar显示，尝试将其隐藏
                        if (showOptions.value) {
                            showOptions.value = false
                            // 必须调用awaitRelease以确认事件不是在ItemOptionsBar上触发的
                            awaitRelease()
                        }
                    }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Log.d("add1", "imaging ${contentItems.value[index]}")
            Image(
                painter = rememberAsyncImagePainter(imageItem.imageUri),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(300.dp)
                    .align(Alignment.CenterHorizontally)
                    .scale(scale.value)  // 应用缩放动画
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            triggerVibration()
                            showOptions.value = !showOptions.value
                        })
                    }
            )

            if (showOptions.value) {
                Popup(
                    alignment = Alignment.TopCenter,
                    offset = IntOffset(0, -150)  // 固定位置在图片正上方
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                color = Color.Gray.copy(alpha = 1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        TextButton(onClick = {
                            saveState(contentItems.value)
                            clearRedoStack()
                            showOptions.value = false
                            val items = contentItems.value.toMutableList()
                            if (index in items.indices) {
                                val currentItem = items[index]
                                val prevIndex = index - 1
                                val nextIndex = index + 1

                                when (currentItem) {
                                    is ContentItem.ImageItem, is ContentItem.AudioItem -> {
                                        val prevItem = items.getOrNull(prevIndex) as? ContentItem.TextItem
                                        val nextItem = items.getOrNull(nextIndex) as? ContentItem.TextItem

                                        if (prevItem != null && nextItem != null) {
                                            // 将下一个文本合并到前一个文本中
                                            prevItem.text.value = TextFieldValue(prevItem.text.value.text + nextItem.text.value.text)
                                            items.removeAt(nextIndex)
                                        } else if (nextItem != null) {
                                            // 如果没有前一个文本项目，将下一个项目上移
                                            items[prevIndex + 1] = nextItem
                                        }
                                        items.removeAt(index)
                                    } else -> return@TextButton
                                }

                                // 确保总是至少有一个TextItem
                                if (items.none { it is ContentItem.TextItem }) {
                                    items.add(ContentItem.TextItem(mutableStateOf(TextFieldValue(""))))
                                }

                                contentItems.value = items
                            }
                        }) {
                            Text(text = "Delete", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DisplayAudioItem(
    audioItem: ContentItem.AudioItem,
    context: Context,
    contentItems: MutableState<MutableList<ContentItem>>,
    index: Int
) {
    val showOptions = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scale = animateFloatAsState(targetValue = if (showOptions.value) 0.95f else 1f)

    // 震动效果实现
    fun triggerVibration() {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator

        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    // 使用Box包裹原有的Column，以便于处理点击外部区域隐藏ItemOptionsBar的逻辑
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // 如果ItemOptionsBar显示，尝试将其隐藏
                        if (showOptions.value) {
                            showOptions.value = false
                            // 必须调用awaitRelease以确认事件不是在ItemOptionsBar上触发的
                            awaitRelease()
                        }
                    }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AudioPlayerUI(audioItem, context,
                Modifier
                    .scale(scale.value)
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            triggerVibration()
                            showOptions.value = !showOptions.value
                        })
                    })

            if (showOptions.value) {
                Popup(
                    alignment = Alignment.TopCenter,
                    offset = IntOffset(0, -150)  // 固定位置在音频控件正上方
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                color = Color.Gray.copy(alpha = 1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        TextButton(onClick = {
                            saveState(contentItems.value)
                            clearRedoStack()
                            showOptions.value = false
                            val items = contentItems.value.toMutableList()
                            if (index in items.indices) {
                                val currentItem = items[index]
                                val prevIndex = index - 1
                                val nextIndex = index + 1

                                when (currentItem) {
                                    is ContentItem.ImageItem, is ContentItem.AudioItem -> {
                                        val prevItem = items.getOrNull(prevIndex) as? ContentItem.TextItem
                                        val nextItem = items.getOrNull(nextIndex) as? ContentItem.TextItem

                                        if (prevItem != null && nextItem != null) {
                                            // 将下一个文本合并到前一个文本中
                                            prevItem.text.value = TextFieldValue(prevItem.text.value.text + nextItem.text.value.text)
                                            items.removeAt(nextIndex)
                                        } else if (nextItem != null) {
                                            // 如果没有前一个文本项目，将下一个项目上移
                                            items[prevIndex + 1] = nextItem
                                        }
                                        items.removeAt(index)
                                    } else -> return@TextButton
                                }

                                // 确保总是至少有一个TextItem
                                if (items.none { it is ContentItem.TextItem }) {
                                    items.add(ContentItem.TextItem(mutableStateOf(TextFieldValue(""))))
                                }

                                contentItems.value = items
                            }
                        }) {
                            Text(text = "Delete", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun AudioPlayerUI(audioItem: ContentItem.AudioItem, context: Context, modifier: Modifier) {
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(audioItem.audioUri))
            prepare()
        }
    }

    val progress = remember { mutableStateOf(0f) }
    val isPlaying = remember { mutableStateOf(false) }
    val duration = remember { mutableStateOf(0L) }

    // To update the slider and time labels based on player progress
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(1000) // Update every second
            val currentPosition = exoPlayer.currentPosition.toFloat()
            val totalDuration = exoPlayer.duration.takeIf { it > 0 } ?: 1 // avoid division by zero
            progress.value = currentPosition / totalDuration
            duration.value = totalDuration
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release() // Release player when no longer needed
        }
    }

    exoPlayer.addListener(object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                duration.value = exoPlayer.duration
            }
        }
    })

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                isPlaying.value = !isPlaying.value
                if (isPlaying.value) exoPlayer.play() else exoPlayer.pause()
            }) {
                Icon(
                    painter = if (isPlaying.value) painterResource(id = R.drawable.pause) else painterResource(id = R.drawable.baseline_play_arrow_24),
                    contentDescription = if (isPlaying.value) "Pause" else "Play"
                )
            }
            Text(text = "${exoPlayer.currentPosition.msToTime()} / ${duration.value.msToTime()}")
            Slider(
                value = progress.value,
                onValueChange = { newProgress ->
                    exoPlayer.seekTo((newProgress * duration.value).toLong())
                    progress.value = newProgress
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            Text(text = duration.value.msToTime())
        }
    }
}

fun Long.msToTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun DisplayVideoItem(
    videoItem: ContentItem.VideoItem,
    context: Context,
    contentItems: MutableState<MutableList<ContentItem>>,
    index: Int
) {
    // 是否显示视频播放界面
    var showVideoPlayer by remember { mutableStateOf(false) }

    // 获取视频缩略图
    val thumbnail = remember(videoItem.videoUri) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoItem.videoUri)
        val bitmap = retriever.frameAtTime
        retriever.release()
        bitmap
    }

    if (showVideoPlayer) {
        // 为视频初始化 ExoPlayer，并确保只在 videoUri 改变时重新构建
        val exoPlayer = remember(videoItem.videoUri) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoItem.videoUri))
                prepare()
            }
        }.also {
            DisposableEffect(Unit) {
                onDispose {
                    it.release()  // 确保在 Composable 移除时释放资源
                }
            }
        }

        // 其他状态变量
        val aspectRatio = remember { mutableStateOf(16 / 9f) }

        // 更新宽高比
        LaunchedEffect(videoItem.videoUri) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, videoItem.videoUri)
                val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toFloat() ?: 16f
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toFloat() ?: 9f
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toFloat() ?: 0f
                aspectRatio.value = if (rotation == 90f || rotation == 270f) {
                    height / width
                } else {
                    width / height
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                retriever.release()
            }
        }

        // 主 UI 组件
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio.value)
                ) {
                    // 视频播放器视图
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                player = exoPlayer
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio.value)
                    )

                    // 删除按钮
                    IconButton(
                        onClick = {
                            saveState(contentItems.value)
                            clearRedoStack()
                            val items = contentItems.value.toMutableList()
                            if (index in items.indices) {
                                val currentItem = items[index]
                                val prevIndex = index - 1
                                val nextIndex = index + 1

                                when (currentItem) {
                                    is ContentItem.ImageItem, is ContentItem.AudioItem, is ContentItem.VideoItem -> {
                                        val prevItem = items.getOrNull(prevIndex) as? ContentItem.TextItem
                                        val nextItem = items.getOrNull(nextIndex) as? ContentItem.TextItem

                                        if (prevItem != null && nextItem != null) {
                                            // 将下一个文本合并到前一个文本中
                                            prevItem.text.value = TextFieldValue(prevItem.text.value.text + nextItem.text.value.text)
                                            items.removeAt(nextIndex)
                                        } else if (nextItem != null) {
                                            // 如果没有前一个文本项目，将下一个项目上移
                                            items[prevIndex + 1] = nextItem
                                        }
                                        items.removeAt(index)
                                    } else -> return@IconButton
                                }

                                // 确保总是至少有一个 TextItem
                                if (items.none { it is ContentItem.TextItem }) {
                                    items.add(ContentItem.TextItem(mutableStateOf(TextFieldValue(""))))
                                }

                                contentItems.value = items
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Video",
                            tint = Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    } else {
        // 显示视频缩略图
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent)
                .clickable { showVideoPlayer = true }
        ) {
            thumbnail?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 删除按钮
            IconButton(
                onClick = {
                    saveState(contentItems.value)
                    clearRedoStack()
                    val items = contentItems.value.toMutableList()
                    if (index in items.indices) {
                        val currentItem = items[index]
                        val prevIndex = index + 1
                        val nextIndex = index - 1

                        when (currentItem) {
                            is ContentItem.ImageItem, is ContentItem.AudioItem, is ContentItem.VideoItem -> {
                                val prevItem = items.getOrNull(prevIndex) as? ContentItem.TextItem
                                val nextItem = items.getOrNull(nextIndex) as? ContentItem.TextItem

                                if (prevItem != null && nextItem != null) {
                                    // 将下一个文本合并到前一个文本中
                                    prevItem.text.value = TextFieldValue(prevItem.text.value.text + nextItem.text.value.text)
                                    items.removeAt(nextIndex)
                                } else if (nextItem != null) {
                                    // 如果没有前一个文本项目，将下一个项目上移
                                    items[prevIndex + 1] = nextItem
                                }
                                items.removeAt(index)
                            } else -> return@IconButton
                        }

                        // 确保总是至少有一个 TextItem
                        if (items.none { it is ContentItem.TextItem }) {
                            items.add(ContentItem.TextItem(mutableStateOf(TextFieldValue(""))))
                        }

                        contentItems.value = items
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Video",
                    tint = Color.Gray.copy(alpha = 0.6f)
                )
            }
        }
    }
}



@Composable
fun AISummaryDialog(aiSummary: MutableState<String>) {
    if (aiSummary.value.isNotEmpty()) {
        val clipboardManager = LocalClipboardManager.current

        AlertDialog(
            onDismissRequest = { aiSummary.value = "" },
            title = { Text("AI总结") },
            text = { Text(aiSummary.value) },
            confirmButton = {
                Button(onClick = { aiSummary.value = "" }) {
                    Text("关闭")
                }
            },
            dismissButton = {
                Button(onClick = {
                    val clip = ClipData.newPlainText("AI Summary", aiSummary.value)
                    clipboardManager.setText(AnnotatedString(aiSummary.value))
                }) {
                    Text("复制")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog(isLoading: MutableState<Boolean>) {
    if (isLoading.value) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("生成中...") },
            text = { Text("正在使用智谱清言AI生成总结，请稍候...") },
            confirmButton = {}
        )
    }
}

@Composable
fun AIDialog(
    isDialogOpen: MutableState<Boolean>,
    contentItems: MutableState<MutableList<ContentItem>>,
    isLoading: MutableState<Boolean>,
    aiSummary: MutableState<String>
) {
    val coroutineScope = rememberCoroutineScope()

    if (isDialogOpen.value) {
        AlertDialog(
            onDismissRequest = { isDialogOpen.value = false },
            title = { Text("AI总结") },
            text = { Text("使用AI为这篇笔记的文字部分生成总结") },
            confirmButton = {
                Button(onClick = {
                    isDialogOpen.value = false
                    isLoading.value = true
                    coroutineScope.launch {
                        val content = generateSummary(contentItems)
                        isLoading.value = false
                        aiSummary.value = content
                    }
                }) {
                    Text("生成")
                }
            },
            dismissButton = {
                Button(onClick = { isDialogOpen.value = false }) {
                    Text("取消")
                }
            }
        )
    }
}

suspend fun generateSummary(contentItems: MutableState<MutableList<ContentItem>>): String {
    val allText: String = contentItems.value.filterIsInstance<ContentItem.TextItem>().joinToString(" ") { it.text.value.text }

    val url = "http://51.13.55.234:8080/process_string"
    val jsonObject = JSONObject()
    jsonObject.put("input_string", allText)
    val jsonString = jsonObject.toString()
    Log.d("AI Summary", jsonString)

    var returnVal = "FAIL"
    withContext(Dispatchers.IO) {
        try {
            val (request, response, result) = url.httpPost()
                .header("Content-Type", "application/json")
                .body(jsonString)
                .responseString()

            when (result) {
                is Result.Success -> {
                    val data = result.get()
                    val gson = Gson()
                    val jsonObject = gson.fromJson(data, JsonObject::class.java)
                    val outputString = jsonObject.get("output_string").asString
                    Log.d("AI Summary", "success $outputString")
                    returnVal = outputString
                }
                is Result.Failure -> {
                    val ex = result.getException()
                    Log.d("AI Summary", "false $ex")
                }
            }
        } catch (e: Exception) {
            Log.d("AI Summary", "Exception: ${e.message}")
        }
    }
    return returnVal
}

fun searchAndHighlight(contentItems: MutableState<MutableList<ContentItem>>, searchTerm: String): List<Pair<Int, Int>> {
    val matches = mutableListOf<Pair<Int, Int>>()
    contentItems.value.forEachIndexed { index, contentItem ->
        if (contentItem is ContentItem.TextItem) {
            val regex = Regex(searchTerm, RegexOption.IGNORE_CASE)
            regex.findAll(contentItem.text.value.text).forEach { matchResult ->
                matches.add(Pair(index, matchResult.range.first))
            }
        }
    }
    return matches
}

@Composable
fun SearchNavigation(
    searchTerm: MutableState<String>,
    matches: MutableState<List<Pair<Int, Int>>>,
    currentMatchIndex: MutableState<Int>,
    contentItems: MutableState<MutableList<ContentItem>>,
    modifier: Modifier = Modifier
) {
    if (searchTerm.value.isNotEmpty() && matches.value.isNotEmpty()) {
        Box(
            modifier = modifier
                .padding(16.dp)
                .background(Color.LightGray.copy(alpha = 0.9f), shape = RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${currentMatchIndex.value + 1}/${matches.value.size}")
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = {
                    if (currentMatchIndex.value > 0) {
                        currentMatchIndex.value -= 1
                        focusOnMatch(matches.value[currentMatchIndex.value], contentItems)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous Match"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if (currentMatchIndex.value < matches.value.size - 1) {
                        currentMatchIndex.value += 1
                        focusOnMatch(matches.value[currentMatchIndex.value], contentItems)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Match"
                    )
                }
            }
        }
    }
}

fun focusOnMatch(match: Pair<Int, Int>, contentItems: MutableState<MutableList<ContentItem>>) {
    val (index, position) = match
    val contentItem = contentItems.value[index]
    if (contentItem is ContentItem.TextItem) {
        contentItem.text.value = contentItem.text.value.copy(selection = TextRange(position, position + 1))
    }
}




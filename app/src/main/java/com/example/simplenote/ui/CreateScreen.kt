package com.example.simplenote.ui

import android.content.Context
import android.net.Uri
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.rememberAsyncImagePainter
import com.example.simplenote.R
import com.example.simplenote.data.NoteType
import com.example.simplenote.ui.note.NoteDetails
import com.example.simplenote.ui.note.NoteViewModel
import kotlinx.coroutines.delay
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
fun covertNoteToContentItem (note: NoteDetails): ContentItem {
    var contentItem: ContentItem? = null
    if(note.type == NoteType.Text) {
        contentItem = ContentItem.TextItem(
            text = remember { mutableStateOf(TextFieldValue(note.content)) }
        )
    }
    else if(note.type == NoteType.Photo) {
        contentItem = ContentItem.ImageItem(Uri.parse(note.content))
    }
    else if(note.type == NoteType.Audio) {
        contentItem = ContentItem.AudioItem(Uri.parse(note.content))
    }
    return contentItem!!
}


// 现在修改 saveState、undo 和 redo 方法，使用 copy 方法
fun saveState(contentItems: List<ContentItem>) {
    if (undoStack.size >= 5) {
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


// 预览编辑界面
@Preview
@Composable
fun PreviewEditorScreen() {

//    val sampleTitleItem = rememberSaveable {
//        ContentItem.TextItem(mutableStateOf(TextFieldValue("标题")), isTitle = true)
//    }
//    val sampleTextItem = rememberSaveable {
//        ContentItem.TextItem(mutableStateOf(TextFieldValue("正文")))
//    }
//
//    val contentItems = rememberSaveable { mutableStateOf(mutableListOf<ContentItem>(
//        sampleTitleItem, sampleTextItem
//    )) }
//
    EditorScreen(contentItems = contentItems)
}


@Composable
fun EditorScreen(
    contentItems: MutableState<MutableList<ContentItem>>,
    noteViewModel: NoteViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToMain: () -> Unit = {},
    navigateBack: () -> Unit = {}

) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val currentDate = remember { dateFormat.format(Date()) }
    val isAIDialogOpen = remember { mutableStateOf(false) }
    val isSearchDialogOpen = remember { mutableStateOf(false) }

    val noteList = remember {
        mutableListOf<NoteDetails>()
    }

//    noteViewModel.insertNote(-1, "标题", type = NoteType.Text)



    // 改为直接计算字符串的长度，适用于中文字符的统计
    val totalCharacters = contentItems.value.sumOf {
        when (it) {
            is ContentItem.TextItem -> it.text.value.text.length
            else -> 0
        }
    }
    val notebookName = "我的笔记本" // 假设笔记本名称是固定的，实际使用中可能来自外部数据源
    Scaffold(
        topBar = { EditorTopBar(
            onUndo = { undo(contentItems = contentItems)},
            onRedo = { redo(contentItems)},
            onSearch = {isSearchDialogOpen.value = true}
        ) },
        bottomBar = {
            ControlPanel(contentItems, LocalContext.current, onAIClick = {isAIDialogOpen.value = true})
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            InfoBar(currentDate, totalCharacters, notebookName)
            Divider(color = Color.LightGray, thickness = 1.dp) // 添加分割线
            LazyColumn {
                itemsIndexed(contentItems.value) { index, item ->
                    when (item) {
                        is ContentItem.TextItem -> EditTextItem(item)
                        is ContentItem.ImageItem -> DisplayImageItem(item, contentItems, index)
                        is ContentItem.AudioItem -> DisplayAudioItem(item, LocalContext.current, contentItems, index)
                        is ContentItem.VideoItem -> DisplayVideoItem(item, LocalContext.current, contentItems, index)

                    }
                }
            }

        }
    }
    AIDialog(isAIDialogOpen, contentItems)
    SearchDialog(isDialogOpen = isSearchDialogOpen) {

    }

}

// 在topbar和底下的编辑区之间加一行小字，小字显示分三栏，分别显示当前日期、该笔记总字数、该笔记所属笔记本名称
@Composable
fun InfoBar(currentDate: String, totalCharacters: Int, notebookName: String) {

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
            text = "字符数: $totalCharacters",
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
            text = notebookName,
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
fun EditorTopBar(onUndo: () -> Unit, onRedo: () -> Unit, onSearch: () -> Unit) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = {
                // Handle back navigation
            }) {
                Icon(painter = rememberAsyncImagePainter(R.drawable.baseline_arrow_back_24), contentDescription = "Back")
            }
        },
        actions = {
            // Undo button
            IconButton(onClick = onUndo) {
                Icon(painter = rememberAsyncImagePainter(R.drawable.baseline_undo_24), contentDescription = "Undo")
            }
            // Redo button
            IconButton(onClick = onRedo
            ) {
                Icon(painter = rememberAsyncImagePainter(R.drawable.baseline_redo_24), contentDescription = "Redo")
            }
            // Search button
            IconButton(onClick = onSearch) {
                Icon(painter = rememberAsyncImagePainter(R.drawable.baseline_search_24), contentDescription = "Search")
            }
            // Done button
            IconButton(onClick = {
                // Handle done action
            }) {
                Icon(painter = rememberAsyncImagePainter(R.drawable.baseline_check_24), contentDescription = "Done")
            }
        }
    )
}


@Composable
fun ControlPanel(contentItems: MutableState<MutableList<ContentItem>>, context: Context, onAIClick: () -> Unit) {
//    val focusRequester = remember {
//        FocusRequester()
//    }

    val imageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uri ->
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
                            items.add(index + 1, ContentItem.ImageItem(uri))
                            items.add(index + 2, newTextItem)
                        }

                        0 -> {
                            // 光标在开头
                            newTextItem.isFocused.value = true
//                            newTextItem.focusRequester.requestFocus()
                            items.add(index, newTextItem)
                            items.add(index + 1, ContentItem.ImageItem(uri))

                        }

                        else -> {
                            // 光标在中间
                            val textBefore =
                                currentItem.text.value.text.substring(0, cursorPosition)
                            val textAfter = currentItem.text.value.text.substring(cursorPosition)
                            val firstTextItem =
                                ContentItem.TextItem(mutableStateOf(TextFieldValue(textBefore)))
                            firstTextItem.isFocused.value = true
                            val secondTextItem =
                                ContentItem.TextItem(mutableStateOf(TextFieldValue(textAfter)))
                            items[index] = firstTextItem
                            items.add(index + 1, ContentItem.ImageItem(uri))
                            items.add(index + 2, secondTextItem)
//                            newTextItem.focusRequester.requestFocus()
                        }
                    }
                }
                contentItems.value = items
            }
        }

    val audioLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uri ->
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
                            items.add(index + 1, ContentItem.AudioItem(uri))
                            items.add(index + 2, newTextItem)
                        }

                        0 -> {
                            // 光标在开头
                            newTextItem.isFocused.value = true
//                            newTextItem.focusRequester.requestFocus()
                            items.add(index, newTextItem)
                            items.add(index + 1, ContentItem.AudioItem(uri))

                        }

                        else -> {
                            // 光标在中间
                            val textBefore =
                                currentItem.text.value.text.substring(0, cursorPosition)
                            val textAfter = currentItem.text.value.text.substring(cursorPosition)
                            val firstTextItem =
                                ContentItem.TextItem(mutableStateOf(TextFieldValue(textBefore)))
                            firstTextItem.isFocused.value = true
                            val secondTextItem =
                                ContentItem.TextItem(mutableStateOf(TextFieldValue(textAfter)))
                            items[index] = firstTextItem
                            items.add(index + 1, ContentItem.AudioItem(uri))
                            items.add(index + 2, secondTextItem)
//                            newTextItem.focusRequester.requestFocus()
                        }
                    }
                }
                contentItems.value = items
            }


        }

    val videoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uri ->
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
                            items.add(index + 1, ContentItem.VideoItem(uri))
                            items.add(index + 2, newTextItem)
                        }

                        0 -> {
                            // 光标在开头
                            newTextItem.isFocused.value = true
//                            newTextItem.focusRequester.requestFocus()
                            items.add(index, newTextItem)
                            items.add(index + 1, ContentItem.VideoItem(uri))

                        }

                        else -> {
                            // 光标在中间
                            val textBefore =
                                currentItem.text.value.text.substring(0, cursorPosition)
                            val textAfter = currentItem.text.value.text.substring(cursorPosition)
                            val firstTextItem =
                                ContentItem.TextItem(mutableStateOf(TextFieldValue(textBefore)))
                            firstTextItem.isFocused.value = true
                            val secondTextItem =
                                ContentItem.TextItem(mutableStateOf(TextFieldValue(textAfter)))
                            items[index] = firstTextItem
                            items.add(index + 1, ContentItem.VideoItem(uri))
                            items.add(index + 2, secondTextItem)
//                            newTextItem.focusRequester.requestFocus()
                        }
                    }
                }
                contentItems.value = items
            }
        }


    val currentFocusIsTitle =
        contentItems.value.any { it is ContentItem.TextItem && it.isFocused.value && it.isTitle }

    BottomAppBar {
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
        // Audio Button
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
//    Row(modifier = Modifier.padding(8.dp)) {
//        Button(onClick = {
//            // 打开图库选择图片
//            imageLauncher.launch("image/*")
//        }) {
//            Text("Add Image")
//        }
//        Spacer(Modifier.width(8.dp))
//        Button(onClick = {
//            // 打开音频选择
//            audioLauncher.launch("audio/*")
//        }) {
//            Text("Add Audio")
//        }
//    }


@Composable
fun AIDialog(isDialogOpen: MutableState<Boolean>, contentItems: MutableState<MutableList<ContentItem>>) {
    if (isDialogOpen.value) {
        AlertDialog(
            onDismissRequest = { isDialogOpen.value = false },
            title = { Text("AI总结") },
            text = { Text("使用AI为这篇笔记的文字部分生成总结") },
            confirmButton = {
                Button(onClick = {
                    isDialogOpen.value = false
                    generateSummary(contentItems)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(isDialogOpen: MutableState<Boolean>, onSearch: (String) -> Unit) {
    if (isDialogOpen.value) {
        val searchQuery = remember { mutableStateOf("") }
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = { isDialogOpen.value = false },
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
//                            disabledIndicatorColor = Color.Transparent,
//                            focusedContainerColor = Color.Transparent,
//                            unfocusedContainerColor = Color.Transparent,
//                            disabledContainerColor = Color.Transparent
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

fun generateSummary(contentItems: MutableState<MutableList<ContentItem>>) {
    val allText = contentItems.value.filterIsInstance<ContentItem.TextItem>().joinToString(" ") { it.text.value.text }
    // Here, use `allText` to call your AI API for generating the summary
    Log.d("AI Summary", "Generated summary for text: $allText")
}


@Composable
fun deleteContentItem(contentItems: MutableState<MutableList<ContentItem>>, index: Int) {
    val items = contentItems.value.toMutableList()
    if (index in items.indices) {
        val currentItem = items[index]
        val prevIndex = index - 1
        val nextIndex = index + 1

        // Check if there are text items around the image or audio to merge them
        val prevItem = items.getOrNull(prevIndex) as? ContentItem.TextItem
        val nextItem = items.getOrNull(nextIndex) as? ContentItem.TextItem

        if (prevItem != null && nextItem != null) {
            // Merge the next text into the previous one and remove the current and next items
            val mergedText = prevItem.text.value.text + nextItem.text.value.text
            prevItem.text.value = TextFieldValue(mergedText)
            items.removeAt(nextIndex) // Remove the next text item
            items.removeAt(index) // Remove the current item (now at the original index)
        } else if (prevItem != null && nextItem == null) {
            // If there is no next text item, just remove the current item
            items.removeAt(index)
        } else if (prevItem == null && nextItem != null) {
            // If there is no previous text item, replace the current item with the next text item
            items[index] = nextItem
            items.removeAt(nextIndex)
        } else {
            // If there are no surrounding text items, just remove the current item
            items.removeAt(index)
        }

        contentItems.value = items
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
    Box(modifier = Modifier
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
                        TextButton(onClick = { /* Implement cut logic */ }) {
                            Text(text = "Cut", color = Color.White)
                        }
                        TextButton(onClick = { /* Implement cut logic */ }) {
                            Text(text = "Copy", color = Color.White)
                        }
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
                                    }
                                    else -> return@TextButton
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
fun AudioPlayerUI(audioItem: ContentItem.AudioItem, context: Context, modifier: Modifier) {
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(audioItem.audioUri))
            prepare()
        }
    }

    val progress = remember { mutableStateOf(0f) }
    val isPlaying = remember { mutableStateOf(false) }

    // To update the slider and time labels based on player progress
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(1000) // Update every second
            val currentPosition = exoPlayer.currentPosition.toFloat()
            val totalDuration = exoPlayer.duration.toFloat()
            if (totalDuration > 0) {
                progress.value = currentPosition / totalDuration
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release() // Release player when no longer needed
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
//        elevation = 4.dp,
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
                    if (isPlaying.value) (R.drawable.pause as ImageVector) else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying.value) "Pause" else "Play"
                )
            }
            Text(text = "${exoPlayer.currentPosition.msToTime()} / ${exoPlayer.duration.msToTime()}")
            Slider(
                value = progress.value,
                onValueChange = { newProgress ->
                    exoPlayer.seekTo((newProgress * exoPlayer.duration).toLong())
                    progress.value = newProgress
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            Text(text = exoPlayer.duration.msToTime())
        }
    }
}

// Helper function to convert milliseconds to a time formatted string
fun Long.msToTime(): String {
    val seconds = this / 1000 % 60
    val minutes = this / (1000 * 60) % 60
    return String.format("%02d:%02d", minutes, seconds)
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
    Box(modifier = Modifier
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
            AudioPlayerUI(audioItem, context, Modifier.scale(scale.value).pointerInput(Unit) {
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
                        TextButton(onClick = { /* Implement cut logic */ }) {
                            Text(text = "Cut", color = Color.White)
                        }
                        TextButton(onClick = { /* Implement cut logic */ }) {
                            Text(text = "Copy", color = Color.White)
                        }
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
                                    }
                                    else -> return@TextButton
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
fun DisplayVideoItem(videoItem: ContentItem.VideoItem, context: Context, contentItems: MutableState<MutableList<ContentItem>>, index: Int) {
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoItem.videoUri))
            prepare()
        }
    }
    val isPlaying = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        VideoPlayer(exoPlayer, isPlaying)
    }
}

@Composable
fun VideoPlayer(exoPlayer: ExoPlayer, isPlaying: MutableState<Boolean>) {
    IconButton(onClick = {
        isPlaying.value = !isPlaying.value
        if (isPlaying.value) exoPlayer.play() else exoPlayer.pause()
    }) {
        Icon(painter = rememberAsyncImagePainter(if (isPlaying.value) R.drawable.pause else Icons.Filled.PlayArrow), contentDescription = if (isPlaying.value) "Pause" else "Play")
    }
}

@Composable
fun EditTextItem(textItem: ContentItem.TextItem) {
//    val focusRequester = remember { FocusRequester() }

    TextField(
        value = textItem.text.value,
        onValueChange = { newValue ->
            saveState(contentItems.value)
            clearRedoStack()
            textItem.text.value = newValue
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
//            .focusRequester(textItem.focusRequester)
            .onFocusChanged { focusState ->
                textItem.isFocused.value = focusState.isFocused
                Log.d("haha", "fuck")
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

@Composable
fun ItemOptionsBar(onCut: () -> Unit, onCopy: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 40.dp)
            .padding(4.dp)
//            .clip(RoundedCornerShape(8.dp))
            .shadow(8.dp, shape = RoundedCornerShape(8.dp), clip = true)
            .background(Color.White)
    ) {
        TextButton(
            onClick = onCut,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cut")
        }
        TextButton(
            onClick = onCopy,
            modifier = Modifier.weight(1f)
        ) {
            Text("Copy")
        }
        TextButton(
            onClick = onDelete,
            modifier = Modifier.weight(1f)
        ) {
            Text("Delete")
        }
    }
}
fun focusOnNearestTextItem(contentItems: MutableState<MutableList<ContentItem>>, index: Int) {
    val items = contentItems.value
    (items.getOrNull(index - 1) ?: items.getOrNull(index))?.let { item ->
        if (item is ContentItem.TextItem) {
//            item.focusRequester.requestFocus()
        }
    }
}

@Preview
@Composable
fun PreviewDisplayImageItem() {
    val imageUri = remember { Uri.parse(R.drawable.avatar.toString()) } // Replace URI with your image URI
    val imageItem = remember { ContentItem.ImageItem(imageUri) }
    val sampleTextItem = remember {
        ContentItem.TextItem(mutableStateOf(TextFieldValue("Sample")))
    }

    val contentItems = remember { mutableStateOf(mutableListOf<ContentItem>(
        sampleTextItem
    )) }

    // Preview the DisplayImageItem
    DisplayImageItem(imageItem = imageItem, contentItems = contentItems, index = 0)
}

package com.example.simplenote.ui

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.TextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.simplenote.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 为浏览编辑界面临时创建的数据类，文字类、图片类、音频类

sealed class ContentItem {
    data class TextItem(
        var text: MutableState<TextFieldValue>,
        val isTitle: Boolean = false, // 新增标识是否为标题
        val isFocused: MutableState<Boolean> = mutableStateOf(false)
    ) : ContentItem()

    data class ImageItem(val imageUri: Uri) : ContentItem()
    data class AudioItem(val audioUri: Uri) : ContentItem()
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
fun EditorScreen(contentItems: MutableState<MutableList<ContentItem>>) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val currentDate = remember { dateFormat.format(Date()) }
    // 改为直接计算字符串的长度，适用于中文字符的统计
    val totalCharacters = contentItems.value.sumOf {
        when (it) {
            is ContentItem.TextItem -> it.text.value.text.length
            else -> 0
        }
    }
    val notebookName = "我的笔记本" // 假设笔记本名称是固定的，实际使用中可能来自外部数据源
    Scaffold(
        topBar = { EditorTopBar() },
        bottomBar = {
            ControlPanel(contentItems, LocalContext.current)
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
                    }
                }
            }

        }
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
fun EditorTopBar() {
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
            IconButton(onClick = {
                // Handle undo action
            }) {
                Icon(painter = rememberAsyncImagePainter(R.drawable.baseline_undo_24), contentDescription = "Undo")
            }
            // Redo button
            IconButton(onClick = {
                // Handle redo action
            }) {
                Icon(painter = rememberAsyncImagePainter(R.drawable.baseline_redo_24), contentDescription = "Redo")
            }
            // Search button
            IconButton(onClick = {
                // Handle search action
            }) {
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
fun ControlPanel(contentItems: MutableState<MutableList<ContentItem>>, context: Context) {
//    val focusRequester = remember {
//        FocusRequester()
//    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uri ->
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
                            val textBefore = currentItem.text.value.text.substring(0, cursorPosition)
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

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uri ->
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
                        val textBefore = currentItem.text.value.text.substring(0, cursorPosition)
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
    val currentFocusIsTitle = contentItems.value.any { it is ContentItem.TextItem && it.isFocused.value && it.isTitle }

    BottomAppBar {
        // Image Button
        IconButton(onClick = { if (!currentFocusIsTitle) imageLauncher.launch("image/*") },
            enabled = !currentFocusIsTitle
        ) {
            Icon(painter = rememberAsyncImagePainter(R.drawable.ic_add_photo), contentDescription = "Add Image")
        }

        Spacer(Modifier.weight(1f, true))

        // Audio Button
        IconButton(onClick = { if (!currentFocusIsTitle) audioLauncher.launch("audio/*") },
            enabled = !currentFocusIsTitle
        ) {
            Icon(painter = painterResource(R.drawable.ic_add_audio), contentDescription = "Add Audio")
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
fun DisplayImageItem(imageItem: ContentItem.ImageItem, contentItems: MutableState<MutableList<ContentItem>>, index: Int) {
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
                onPress = { // 检测到按压事件
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
            if (showOptions.value) {
                ItemOptionsBar(
                    onCut = { /* Implement cut logic */ },
                    onCopy = { /* Implement copy logic */ },
                    onDelete = {
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
                                else -> return@ItemOptionsBar
                            }

                            // 确保总是至少有一个TextItem
                            if (items.none { it is ContentItem.TextItem }) {
                                items.add(ContentItem.TextItem(mutableStateOf(TextFieldValue(""))))
                            }

                            contentItems.value = items
                        }
                    }
                )
            }
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
                            showOptions.value = !showOptions.value })
                    }
            )
        }
    }
}


@Composable
fun AudioPlayerUI(audioItem: ContentItem.AudioItem, context: Context) {
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
        modifier = Modifier.padding(8.dp)
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
fun DisplayAudioItem(audioItem: ContentItem.AudioItem, context: Context, contentItems: MutableState<MutableList<ContentItem>>, index: Int) {
    val showOptions = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { showOptions.value = !showOptions.value }
            )
        }
    ) {
        if (showOptions.value) {
            ItemOptionsBar(
                onCut = { /* Implement cut logic */ },
                onCopy = { /* Implement copy logic */ },
                onDelete = {
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
                            else -> return@ItemOptionsBar
                        }

                        // 确保总是至少有一个TextItem
                        if (items.none { it is ContentItem.TextItem }) {
                            items.add(ContentItem.TextItem(mutableStateOf(TextFieldValue(""))))
                        }

                        contentItems.value = items
                    }
                }
            )
        }
        AudioPlayerUI(audioItem, context)

        // Detecting long press on the whole component to show options
        Modifier.pointerInput(Unit) {
            detectTapGestures(onLongPress = { showOptions.value = true })
        }
    }
}



@Composable
fun EditTextItem(textItem: ContentItem.TextItem) {
//    val focusRequester = remember { FocusRequester() }

    TextField(
        value = textItem.text.value,
        onValueChange = { newValue ->
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

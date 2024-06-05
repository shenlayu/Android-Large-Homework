package com.example.simplenote.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.simplenote.R
import com.example.simplenote.ui.note.DirectoryDetails
import com.example.simplenote.ui.note.DirectoryViewModel
import com.example.simplenote.ui.note.NoteViewModel
import com.example.simplenote.ui.note.NotebookViewModel
import com.example.simplenote.ui.note.SortType
import com.example.simplenote.ui.note.UserViewModel
import com.example.simplenote.ui.note.toNotebook
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainScreen(
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory),
    directoryViewModel: DirectoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    notebookViewModel: NotebookViewModel = viewModel(factory = AppViewModelProvider.Factory),
    noteViewModel: NoteViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToEdit: ()->Unit = {},
    navigateToMe: () -> Unit = {},
    navigateToMain: () -> Unit = {}
) {
    val id = rememberSaveable {
        mutableIntStateOf(0)
    }
    val localDirectoryUiState by directoryViewModel.uiState.collectAsState()
    val localNotebookUiState by notebookViewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSortMenu by rememberSaveable { mutableStateOf(false) }

    var showBottomSheet by rememberSaveable { mutableStateOf(false) } // 用于控制底部动作条的状态
    val (selectedTab, setSelectedTab) = rememberSaveable { mutableStateOf(0) }

    var isSelecting by rememberSaveable { mutableStateOf(false) }
    var selectedItems by rememberSaveable { mutableStateOf(setOf<Int>()) }
    val isSearchDialogOpen = remember { mutableStateOf(false) }

    var isCreatingDirectory by rememberSaveable { mutableStateOf(false) }

    val isFirstLaunch = rememberSaveable { mutableStateOf(true) }
    if(localDirectoryUiState.directoryList.isNotEmpty()) {
        if(isFirstLaunch.value) {
            isFirstLaunch.value = false
            if(localNotebookUiState.directoryID != null && localNotebookUiState.directoryID != localDirectoryUiState.directoryList[0].id) {
                notebookViewModel.init(localNotebookUiState.directoryID)
                id.intValue = localNotebookUiState.directoryID!!
            }
            else {
                notebookViewModel.init(localDirectoryUiState.directoryList[0].id, localDirectoryUiState.directoryList)
                id.intValue = localDirectoryUiState.directoryList[0].id
            }
        }
    }

    fun clearSelection() {
        isSelecting = false
        selectedItems = setOf()
    }

    fun handleItemSelect(index: Int) {
        selectedItems = if (selectedItems.contains(index)) {
            selectedItems - index
        } else {
            selectedItems + index
        }
    }

    fun handleLongPress(index: Int) {
        isSelecting = true
        handleItemSelect(index)
    }

    Scaffold(
        topBar = {
            if (isSelecting) {
                TopAppBar(
//                    scrollBehavior = scrollBehavior,
                    title = { Text(if (selectedItems.isEmpty()) "请选择项目" else "已选择${selectedItems.size}项") },
                    navigationIcon = {
                        TextButton(onClick = { clearSelection() }) {
                            Text("取消")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            selectedItems = (0 until localNotebookUiState.notebookList.size).toSet()
                            isSelecting = true
                        }) {
                            Text("全选")
                        }

                    }
                )
            } else {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showBottomSheet = !showBottomSheet }
                        ) {
                            var listID: Int = 0
                            for(idx in 0 until localDirectoryUiState.directoryList.size) {
                                if(localDirectoryUiState.directoryList[idx].id == id.intValue) {
                                    listID = idx
                                    break
                                }
                            }
                            if(localDirectoryUiState.directoryList.isNotEmpty()) {
                                Text(localDirectoryUiState.directoryList[listID].name)
                            } else {
                                Text("LOADING")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (showBottomSheet) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "展开"
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = navigateToMe, modifier = Modifier.padding(start = 8.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.avatar),
                                contentDescription = "Avatar",
                                modifier = Modifier.size(48.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSortMenu = !showSortMenu }) {
                            Icon(
                                rememberAsyncImagePainter(model = R.drawable.baseline_sort_24),
                                contentDescription = "排序"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("按修改时间排序") },
                                onClick = {
                                    notebookViewModel.changeSortType(SortType.ChangeTime)
                                    notebookViewModel.sortBySortType()
                                    showSortMenu = false
//                                    sortValue = true
                                  },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("按创建时间排序") },
                                onClick = {
                                    notebookViewModel.changeSortType(SortType.CreateTime)
                                    notebookViewModel.sortBySortType()
                                    showSortMenu = false
//                                    sortValue = true
                                  },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }

                        IconButton(onClick = { isSearchDialogOpen.value = true }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "搜索"
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelecting) {
                FloatingActionButton(
                    onClick = {
//                        scope.launch {
//                            val index = notebookList.size
//                            notebookViewModel.insertNotebook(name = "new")
//                            //notebookViewModel.getNotebookList(notebookList)
//                            id.intValue = notebookList[index].id
//                            noteViewModel.init(id.intValue)
////                            navigateToEdit()
//                        }
                        // 处理全部笔记
                        if(localNotebookUiState.directoryID == localDirectoryUiState.directoryList[0].id) {
                            Log.d("add1", "localNotebookUiState.directoryID ${localNotebookUiState.directoryID}")
                            Log.d("add1", "localDirectoryUiState.directoryList[0].id ${localDirectoryUiState.directoryList[0].id}")
                            Log.d("add1", "localDirectoryUiState.directoryList[1].id ${localDirectoryUiState.directoryList[1].id}")
                            notebookViewModel.insertNotebook("new", localDirectoryUiState.directoryList[1].id, localDirectoryUiState.directoryList)
                        }
                        else {
                            notebookViewModel.insertNotebook("new")
                        }
//                        Log.d("add1", "local notebookList size ${localNotebookUiState.notebookList.size}")
                        val newNotebookId = notebookViewModel.uiState.value.notebookList.last().id
                        noteViewModel.initFirst(newNotebookId)
                        navigateToEdit()
//                        sortValue = true
                    },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "新建",
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                        isCreatingDirectory = false
                    },
                    sheetState = sheetState
                ) {
                    if (isCreatingDirectory) {
                        CreateDirectorySheet(
                            directories = localDirectoryUiState.directoryList,
                            onCancel = {
                                isCreatingDirectory = false
                            },
                            onSave = { name ->
                                directoryViewModel.insertDirectory(name)
                                isCreatingDirectory = false
                            }
                        )
                    } else {
                        BottomSheetContent(
                            onClose = {
                                scope.launch { sheetState.hide() }
                                    .invokeOnCompletion { showBottomSheet = false }
                            },
                            onCreateDirectory = { isCreatingDirectory = true },
                            onDirectoryClick = {
                                id.intValue = it.id

                                if(id.intValue == localDirectoryUiState.directoryList[0].id) {
                                    notebookViewModel.init(id.intValue, localDirectoryUiState.directoryList)
                                }
                                else {
                                    notebookViewModel.init(id.intValue)
                                }
                                Log.d("add1", "id ${localNotebookUiState.directoryID}")
                                //notebookViewModel.getNotebookList(notebookList)
                            },
                            directories = localDirectoryUiState.directoryList,
                        )
                    }
                }

            } else {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    isSelecting = isSelecting,
                    onMove = {
                        // todo: 处理移动操作的逻辑
                    },
                    onDelete = {
                        // todo: 处理删除操作的逻辑
                        val sortedList = selectedItems.sortedDescending()
                        sortedList.forEach {
                            notebookViewModel.deleteNotebook(it)
                            localNotebookUiState.notebookList.forEach {
                            }
                        }

                        // 清除选中的项目
                        selectedItems = setOf()
                        isSelecting = false
                    },
                    onNoteClick = {},
                    onMyClick = navigateToMe
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(padding)
        ) {
//            TextField(
//                value = localUiState.SavedText,
//                onValueChange = {
//                    directoryViewModel.Updatetext((it))
//                },
//                singleLine = false,
//                maxLines = 4,
//                modifier = Modifier
//            )
//            Button(modifier = Modifier.fillMaxWidth(),
//                onClick = {
//                    directoryViewModel.init(0)
//                }
//                ) {}
//            repeat(99) { index ->  // 保持99个项目以达到100个
//                CustomListItem(
//                    index = index,
//                    text = "$index. 主要标题",
//                    subText1 = if (index % 2 == 0) "次要信息" else null,
//                    subText2 = "附加信息",
//                    isSelecting = isSelecting,
//                    isSelected = selectedItems.contains(index),
//                    onSelect = { handleItemSelect(index) },
//                    onLongPress = { handleLongPress(index) },
//                    enterEditScreen = {
//                    //todo: 完成进入编辑界面的逻辑
//                    }
//                )
//            }
            localNotebookUiState.notebookList.reversed().forEachIndexed() { idx, notebookDetails ->
                // 保持99个项目以达到100个
                val noteTitle = notebookViewModel.getTitleNote(notebookDetails.id)
                val noteFirst = notebookViewModel.getFirstNote(notebookDetails.id)
                val noteSecond = notebookViewModel.getSecondNote(notebookDetails.id)
//                val noteTitle: NoteDetails? = null
//                val noteFirst: NoteDetails? = null
//                val noteSecond: NoteDetails? = null
                item {
                    CustomListItem(
                        text = noteTitle?.content ?: "",
                        subText1 = noteFirst?.content ?: "",
                        subText2 = noteSecond?.content ?: "",
                        isSelecting = isSelecting,
                        isSelected = selectedItems.contains(idx),
                        onSelect = { handleItemSelect(idx) },
                        onLongPress = { handleLongPress(idx) },
                        enterEditScreen = {
                            //todo: 完成进入编辑界面的逻辑
                            noteViewModel.init(notebookDetails.id)

                            navigateToEdit()
//                        sortValue = true
                        }
                    )
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    MainSearchDialog(isDialogOpen = isSearchDialogOpen, onSearch = { /* Handle search */ })
}




@Composable
fun MainSearchDialog(isDialogOpen: MutableState<Boolean>, onSearch: (String) -> Unit) {
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
                        textStyle = TextStyle(fontSize = 16.sp),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomListItem(
    text: String,
    subText1: String?,
    subText2: String,
    isSelecting: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onLongPress: () -> Unit,
    enterEditScreen: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .alpha(0.95f)
            .combinedClickable(
                onClick = if (isSelecting) onSelect else enterEditScreen,
                onLongClick = onLongPress
            )
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                if (subText1 != null) {
                    Text(
                        text = subText1,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = subText2,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.weight(1f)) // This pushes the checkbox to the right
            if (isSelecting) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() }
                )
            }
        }
    }
}


@Composable
fun BottomNavigationBar(selectedTab: Int, isSelecting: Boolean, onMove: () -> Unit, onDelete: () -> Unit, onNoteClick: () -> Unit,
                        onMyClick: () -> Unit) {
    NavigationBar {
        if (isSelecting) {
            // 在选择模式下显示移动和删除按钮
            NavigationBarItem(
                icon = { Icon(rememberAsyncImagePainter(model = R.drawable.baseline_drive_file_move_rtl_24), contentDescription = "移动") },
                label = { Text("移动") },
                selected = false,
                onClick = onMove
            )
            NavigationBarItem(
                icon = { Icon(rememberAsyncImagePainter(model = R.drawable.baseline_delete_forever_24), contentDescription = "删除") },
                label = { Text("删除") },
                selected = false,
                onClick = onDelete
            )
        } else {
            // 非选择模式下显示正常的导航项
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = "笔记") },
                label = { Text("笔记") },
                selected = selectedTab == 0,
                onClick = onNoteClick
            )
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Person, contentDescription = "我的") },
                label = { Text("我的") },
                selected = selectedTab == 1,
                onClick = onMyClick
            )
        }
    }
}
@Composable
fun BottomSheetContent(
    onClose: () -> Unit,
    onCreateDirectory: () -> Unit,
    directories: List<DirectoryDetails>,
    onDirectoryClick: (DirectoryDetails) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                Text("完成")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "笔记本",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onCreateDirectory, modifier = Modifier.weight(1f)) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "新建笔记本")
            }
        }
        Divider(color = Color.LightGray, thickness = 1.dp)
        LazyColumn {
            items(directories) { directory ->
                Box(
                    modifier = Modifier
                        .clickable { onDirectoryClick(directory) }
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(directory.name)
                }
                Divider(color = Color.LightGray, thickness = 1.dp)
            }
        }
    }
}



@Composable
fun CreateDirectorySheet(
    directories: List<DirectoryDetails>, // 已存在的笔记本名称列表
    onCancel: () -> Unit,
    onSave: (String) -> Unit // 保存笔记本的函数，参数为笔记本名称和颜色
) {
    var directoryName by rememberSaveable { mutableStateOf("") }
    val isNameValid = directoryName.isNotBlank() && directories.none { it.name == directoryName }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("完成")
            }
            Spacer(modifier = Modifier.weight(1f))  // 使用权重推动文本到中间
            Text(
                "新建笔记本",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterVertically)  // 这里给文本加大权重，确保它能在中间显示
            )
            Spacer(modifier = Modifier.weight(1f))  // 使用权重推动文本到中间
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = { if (isNameValid) onSave(directoryName) },
                enabled = isNameValid
            ) {
                Text("保存", color = if (isNameValid) MaterialTheme.colorScheme.onBackground else Color.Gray)
            }
        }

        TextField(
            value = directoryName,
            onValueChange = { directoryName = it },
            label = { Text("笔记本名称") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
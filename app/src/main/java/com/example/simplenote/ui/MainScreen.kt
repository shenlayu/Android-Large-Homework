package com.example.simplenote.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.semantics.Role.Companion.Button
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
import com.example.simplenote.ui.note.NoteDetails
import com.example.simplenote.ui.note.NoteViewModel
import com.example.simplenote.ui.note.NotebookDetails
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
//    logged: Boolean = false,
//    havingDirectory: Boolean = false,
    navigateToEdit: ()->Unit = {},
    navigateToMe: () -> Unit = {}
) {
//    if(!logged) {
//
//    }
//    else if(!havingDirectory) {
//
//    }
  
    // 本地维护一个directoryList
//    val notebookList = remember {
//        mutableStateListOf<NotebookDetails>()
//    }

    val id = rememberSaveable {
        mutableIntStateOf(0)
    }
    val localDirectoryUiState by directoryViewModel.uiState.collectAsState()
    val localNotebookUiState by notebookViewModel.uiState.collectAsState()
    val localUserUiState by userViewModel.uiState.collectAsState()
//    Log.d("add1", "launched notebook size ${localNotebookUiState.notebookList.size}")


    var currentDirectory by rememberSaveable { mutableStateOf<DirectoryDetails?>(null) }


    val scrollBehavior = exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showSyncCard by rememberSaveable { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSortMenu by rememberSaveable { mutableStateOf(false) }

    var showBottomSheet by rememberSaveable { mutableStateOf(false) } // 用于控制底部动作条的状态
    val (selectedTab, setSelectedTab) = rememberSaveable { mutableStateOf(0) }
    var isSelecting by rememberSaveable { mutableStateOf(false) }
    var selectedItems by rememberSaveable { mutableStateOf(setOf<Int>()) }
    val isSearchDialogOpen = remember { mutableStateOf(false) }

    var isCreatingDirectory by rememberSaveable { mutableStateOf(false) }

    val isFirstLaunch = rememberSaveable { mutableStateOf(true) } // 添加这个状态变量
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
                LargeTopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = { Text(if (selectedItems.isEmpty()) "请选择项目" else "已选择${selectedItems.size}项") },
                    navigationIcon = {
                        TextButton(onClick = { clearSelection() }) {
                            Text("取消")
                        }
                    },
                    actions = {
                        TextButton(onClick = { selectedItems = (0..99).toSet(); isSelecting = true }) {
                            Text("全选")
//                            todo: 需要修改全选的逻辑
                        }
                    }
                )
            } else {
                LargeTopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showBottomSheet = !showBottomSheet } // 使标题可点击
                        ) {
//                            Text(localUiState.directoryList[id.intValue].name)
//                            try {
//                                val a: String = localUiState.directoryList[0].name
//                            }  catch (e: Exception) {
//                                Log.e("directoryList", "Error inserting directory", e)
//                            }
//                            Log.d("add1", "after cache${localDirectoryUiState.directoryList.size}")
                            var listID: Int = 0
                            for(idx in 0..<localDirectoryUiState.directoryList.size) {
                                if(localDirectoryUiState.directoryList[idx].id == id.intValue) {
                                    listID = idx
                                    break
                                }
                            }
                            if(localDirectoryUiState.directoryList.size != 0) {
                                Text(localDirectoryUiState.directoryList[listID].name)
                            }
                            else {
                                Text("LOADING")
                            }

//                            Text("全部笔记")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (showBottomSheet) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "展开"
                            )
                        }
                    },


                    navigationIcon = {
                        IconButton(onClick = { /* Handle avatar click */ }, modifier = Modifier.padding(start = 8.dp)) {
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
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "更多"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(text = { Text("第一个菜单项") }, onClick = { /*TODO*/ })
                            DropdownMenuItem(text = { Text("第二个菜单项") }, onClick = { /*TODO*/ })
                            DropdownMenuItem(text = { Text("第三个菜单项") }, onClick = { /*TODO*/ })
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
//                                scope.launch {
                                directoryViewModel.insertDirectory(name)
                                isCreatingDirectory = false
//                                }
                                // todo:可以在这里添加逻辑，比如更新数据库或状态
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
                            // todo:示例笔记本列表，根据实际需要进行调整
                            directoryViewModel = directoryViewModel
                        )
                    }
                }

            } else {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    setSelectedTab = setSelectedTab,
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
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
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

            if (showSyncCard) {
                SpecialSyncCard(
                    onIgnore = { showSyncCard = false },
                    onEnable = { /* Handle enable action */ }
                )
            }
            Button(onClick = navigateToMe) {
                Text("去我的")
            }
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
                CustomListItem(
                    index = idx,
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
            Spacer(Modifier.height(16.dp))
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialSyncCard(onIgnore: () -> Unit, onEnable: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
//        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .alpha(0.95f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "开启便签云同步",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "保障便签数据安全",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onIgnore,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("忽略")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onEnable,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("开启")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomListItem(
    index: Int,
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
fun BottomNavigationBar(selectedTab: Int, setSelectedTab: (Int) -> Unit, isSelecting: Boolean, onMove: () -> Unit, onDelete: () -> Unit) {
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
                onClick = { setSelectedTab(0) }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Person, contentDescription = "我的") },
                label = { Text("我的") },
                selected = selectedTab == 1,
                onClick = { setSelectedTab(1)

                }
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
    directoryViewModel: DirectoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    ) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically // 添加此行来确保垂直居中对齐
        ) {
            TextButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                Text("完成")
            }
            Spacer(modifier = Modifier.weight(1.5f))  // 使用权重推动文本到中间
            Text(
                "笔记本",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterVertically)  // 这里给文本加大权重，确保它能在中间显示
            )
            Spacer(modifier = Modifier.weight(1.5f))  // 使用权重推动文本到中间
            IconButton(onClick = onCreateDirectory, modifier = Modifier.weight(1f)) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "新建笔记本")
            }
        }
        Divider(color = Color.LightGray, thickness = 1.dp)
        LazyColumn {
            items(directories) { directory ->
                Text(
                    directory.name,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable { onDirectoryClick(directory) }
                )
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
            Spacer(modifier = Modifier.weight(1.5f))  // 使用权重推动文本到中间
            Text(
                "新建笔记本",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterVertically)  // 这里给文本加大权重，确保它能在中间显示
            )
            Spacer(modifier = Modifier.weight(1.5f))  // 使用权重推动文本到中间
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


//@Composable
//fun BottomSheetContent() {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("底部动作条内容", style = MaterialTheme.typography.bodySmall)
//        Button(onClick = { /* Handle action */ }) {
//            Text("点击我")
//        }
//    }
//}
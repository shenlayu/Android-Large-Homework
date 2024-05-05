package com.example.simplenote

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.ui.AppViewModelProvider
import com.example.simplenote.ui.note.NoteViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainScreen(
    viewModel: NoteViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showSyncCard by rememberSaveable { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSortMenu by rememberSaveable { mutableStateOf(false) }
    var sortOrder by rememberSaveable { mutableStateOf("按修改时间排序") } // 默认排序方式
    var showBottomSheet by rememberSaveable { mutableStateOf(false) } // 用于控制底部动作条的状态
    val (selectedTab, setSelectedTab) = rememberSaveable { mutableStateOf(0) }
    Scaffold(
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showBottomSheet = !showBottomSheet } // 使标题可点击
                    ) {
                        Text("全部笔记")
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
                            imageVector = Icons.Filled.Build,
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
                            onClick = { sortOrder = "按修改时间排序"; showSortMenu = false },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("按修改时间排序") },
                            onClick = { sortOrder = "按创建时间排序"; showSortMenu = false },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }

                    IconButton(onClick = { /* Handle search action */ }) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Handle create new note action */ },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "新建"
                )
            }
        },
        bottomBar = {
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState
                ) {
                    // Sheet content
                    Button(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }) {
                        Text("Hide bottom sheet")
                    }
                }

            } else {
                BottomNavigationBar(selectedTab, setSelectedTab)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            Row() {
                val coroutineScope = rememberCoroutineScope()
                var idx: Int = 1
                Button(onClick = {
                    viewModel.init(1, "name")
                }) {
                }
                Button(onClick = {
                    viewModel.addText("halo${idx++}")
                }) {
                }
                Button(onClick = {
                    coroutineScope.launch {
                        viewModel.saveNotebook()
                    }
                }) {
                }
            }
            if (showSyncCard) {
                SpecialSyncCard(
                    onIgnore = { showSyncCard = false },
                    onEnable = { /* Handle enable action */ }
                )
            }
            repeat(99) { index ->  // 保持99个项目以达到100个
                CustomListItem(
                    text = "$index. 主要标题",
                    subText1 = if (index % 2 == 0) "次要信息" else null,
                    subText2 = "附加信息"
                )
            }
            Spacer(Modifier.height(16.dp))
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomListItem(text: String, subText1: String?, subText2: String) {
    Card(
        shape = RoundedCornerShape(8.dp), // 设置圆角
//        elevation = 0, // 设置阴影
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {})
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .alpha(0.95f) // 设置不透明度
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = text,
                color = Color.White, // 设置文本颜色为白色
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), // 加粗
                modifier = Modifier.padding(bottom = 4.dp)
            )
            if (subText1 != null) { // 第二个文本可以不显示
                Text(
                    text = subText1,
                    color = Color.Gray, // 设置文本颜色为灰色
                    style = MaterialTheme.typography.bodySmall, // 设置文本较小
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = subText2,
                color = Color.Gray, // 设置文本颜色为灰色
                style = MaterialTheme.typography.bodySmall // 设置文本较小
            )
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, setSelectedTab: (Int) -> Unit) {
    NavigationBar {
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
            onClick = { setSelectedTab(1) }
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
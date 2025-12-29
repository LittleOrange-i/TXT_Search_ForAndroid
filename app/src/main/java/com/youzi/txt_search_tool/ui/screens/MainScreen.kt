package com.youzi.txt_search_tool.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.youzi.txt_search_tool.ui.components.*
import com.youzi.txt_search_tool.ui.theme.*
import com.youzi.txt_search_tool.ui.viewmodel.MainViewModel

/**
 * 主界面
 * @param viewModel 主ViewModel
 * @param onNavigateToResults 导航到搜索结果页面的回调
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToResults: () -> Unit
) {
    val context = LocalContext.current
    val fileName by viewModel.fileName.collectAsState()
    val fileSize by viewModel.fileSize.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val replaceText by viewModel.replaceText.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchProgress by viewModel.searchProgress.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val replaceHistory by viewModel.replaceHistory.collectAsState()
    
    // 无搜索结果弹窗状态
    var showNoResultDialog by remember { mutableStateOf(false) }
    
    // 内容映射弹窗状态
    var showHistoryDialog by remember { mutableStateOf(false) }
    
    // 记录上一次的搜索状态，用于检测搜索完成时刻
    var previousSearching by remember { mutableStateOf(false) }

    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // 获取持久化权限
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                viewModel.setFileUri(uri)
                // 加载文件内容
                viewModel.loadCurrentContent()
            }
        }
    }

    // 另存为文件选择器
    val saveAsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // 获取持久化权限
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                viewModel.saveFileAs(uri)
            }
        }
    }

    // 监听搜索完成（只在搜索状态从true变为false时触发）
    LaunchedEffect(isSearching) {
        if (previousSearching && !isSearching) {
            // 搜索刚完成
            if (searchResults.isNotEmpty()) {
                // 有搜索结果，跳转到结果页面
                onNavigateToResults()
            } else if (searchQuery.isNotEmpty() && errorMessage == null && successMessage == null) {
                // 搜索完成但无结果，且不是替换操作后的自动搜索，显示弹窗
                showNoResultDialog = true
            }
        }
        // 更新上一次的状态
        previousSearching = isSearching
    }

    Scaffold(
        // 浮动搜索按钮 - 方便单手快速触发搜索
        floatingActionButton = {
            if (fileName.isNotEmpty() && searchQuery.isNotEmpty() && !isSearching) {
                FloatingActionButton(
                    onClick = { viewModel.performSearch() },
                    containerColor = NeumorphicPrimary,
                    contentColor = NeumorphicBackground
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "快速搜索"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeumorphicBackground)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 顶部标题栏 - 紧凑设计
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = NeumorphicBackground,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "TXT 搜索替换",
                        fontSize = 20.sp,
                        color = NeumorphicText,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // 文件信息区 - 紧凑显示
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 文件图标
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "文件",
                                tint = NeumorphicPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // 文件信息
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                if (fileName.isNotEmpty()) {
                                    Text(
                                        text = fileName,
                                        fontSize = 14.sp,
                                        color = NeumorphicText,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = formatFileSize(fileSize),
                                        fontSize = 12.sp,
                                        color = NeumorphicTextSecondary
                                    )
                                } else {
                                    Text(
                                        text = "未选择文件",
                                        fontSize = 14.sp,
                                        color = NeumorphicTextSecondary
                                    )
                                }
                            }
                            
                            // 打开文件按钮 - 小型图标按钮
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "text/plain"
                                    }
                                    filePickerLauncher.launch(intent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "选择文件",
                                    tint = NeumorphicPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 搜索和替换输入区 - 合并为一张卡片
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // 搜索输入
                            NeumorphicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = "搜索关键字",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 替换输入
                            NeumorphicTextField(
                                value = replaceText,
                                onValueChange = { viewModel.updateReplaceText(it) },
                                placeholder = "替换文本",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 内容映射按钮
                            NeumorphicButton(
                                text = "内容映射",
                                onClick = { showHistoryDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = replaceHistory.isNotEmpty()
                            )

                            // 搜索进度条
                            if (isSearching) {
                                Spacer(modifier = Modifier.height(8.dp))
                                NeumorphicProgressBar(
                                    progress = searchProgress,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 操作按钮组 - 横向排列，减少垂直空间
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // 第一行：搜索和替换
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                NeumorphicButton(
                                    text = if (isSearching) "停止" else "搜索",
                                    onClick = {
                                        if (isSearching) {
                                            viewModel.stopSearch()
                                        } else {
                                            viewModel.performSearch()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = fileName.isNotEmpty() && searchQuery.isNotEmpty()
                                )

                                NeumorphicButton(
                                    text = "替换全部",
                                    onClick = { viewModel.performReplaceAll() },
                                    modifier = Modifier.weight(1f),
                                    enabled = fileName.isNotEmpty() && searchQuery.isNotEmpty()
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 第二行：保存操作
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                NeumorphicButton(
                                    text = "保存",
                                    onClick = { viewModel.saveFile() },
                                    modifier = Modifier.weight(1f),
                                    enabled = fileName.isNotEmpty()
                                )

                                NeumorphicButton(
                                    text = "另存为",
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                            addCategory(Intent.CATEGORY_OPENABLE)
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TITLE, fileName.ifEmpty { "新文件.txt" })
                                        }
                                        saveAsLauncher.launch(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = fileName.isNotEmpty()
                                )
                            }
                        }
                    }

                    // 提示消息区域
                    successMessage?.let { message ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = NeumorphicPrimary.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = message,
                                color = NeumorphicPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        LaunchedEffect(message) {
                            kotlinx.coroutines.delay(3000)
                            viewModel.clearSuccess()
                        }
                    }

                    errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = NeumorphicError.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = message,
                                color = NeumorphicError,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    
                    // 底部留白，确保内容不被遮挡
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // 无搜索结果弹窗
    if (showNoResultDialog) {
        NoResultDialog(
            searchQuery = searchQuery,
            onDismiss = { 
                showNoResultDialog = false
                viewModel.clearError()
            }
        )
    }
    
    // 内容映射历史弹窗
    if (showHistoryDialog) {
        ReplaceHistoryDialog(
            historyList = replaceHistory,
            onSelectHistory = { history ->
                viewModel.updateSearchQuery(history.searchQuery)
                viewModel.updateReplaceText(history.replaceText)
                showHistoryDialog = false
            },
            onDismiss = { showHistoryDialog = false }
        )
    }
}

/**
 * 格式化文件大小
 * @param size 文件大小（字节）
 * @return 格式化后的字符串
 */
private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}

/**
 * 无搜索结果弹窗
 * @param searchQuery 搜索关键字
 * @param onDismiss 关闭回调
 */
@Composable
fun NoResultDialog(
    searchQuery: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                // 图标
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "提示",
                    tint = NeumorphicPrimary,
                    modifier = Modifier.size(56.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标题
                Text(
                    text = "未找到匹配结果",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicText
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 说明文字
                Text(
                    text = "关键字 \"$searchQuery\" 在文件中未找到任何匹配内容",
                    fontSize = 14.sp,
                    color = NeumorphicTextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 确定按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeumorphicPrimary
                    )
                ) {
                    Text(
                        text = "确定",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 替换历史记录弹窗 - 显示替换操作的历史记录
 * @param historyList 历史记录列表
 * @param onSelectHistory 选择历史记录回调
 * @param onDismiss 关闭回调
 */
@Composable
fun ReplaceHistoryDialog(
    historyList: List<com.youzi.txt_search_tool.data.model.ReplaceHistory>,
    onSelectHistory: (com.youzi.txt_search_tool.data.model.ReplaceHistory) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "历史记录",
                            tint = NeumorphicPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "内容映射历史",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicText
                        )
                    }
                    
                    Text(
                        text = "${historyList.size} 条记录",
                        fontSize = 13.sp,
                        color = NeumorphicTextSecondary
                    )
                }
                
                Divider(color = NeumorphicTextSecondary.copy(alpha = 0.2f), thickness = 1.dp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 历史记录列表
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    historyList.forEachIndexed { index, history ->
                        HistoryItem(
                            history = history,
                            onClick = { onSelectHistory(history) }
                        )
                        
                        if (index < historyList.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeumorphicTextSecondary
                    )
                ) {
                    Text(
                        text = "关闭",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 历史记录项 - 单个历史记录的显示组件
 * @param history 历史记录
 * @param onClick 点击回调
 */
@Composable
fun HistoryItem(
    history: com.youzi.txt_search_tool.data.model.ReplaceHistory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = NeumorphicBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 关键字
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "关键字:",
                    fontSize = 12.sp,
                    color = NeumorphicTextSecondary,
                    modifier = Modifier.width(60.dp)
                )
                Text(
                    text = history.searchQuery,
                    fontSize = 14.sp,
                    color = NeumorphicText,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // 替换文本
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "替换为:",
                    fontSize = 12.sp,
                    color = NeumorphicTextSecondary,
                    modifier = Modifier.width(60.dp)
                )
                Text(
                    text = if (history.replaceText.isEmpty()) "(空)" else history.replaceText,
                    fontSize = 14.sp,
                    color = if (history.replaceText.isEmpty()) NeumorphicTextSecondary else NeumorphicText,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // 替换次数和时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "共替换 ${history.count} 处",
                    fontSize = 12.sp,
                    color = NeumorphicPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = formatTimestamp(history.timestamp),
                    fontSize = 11.sp,
                    color = NeumorphicTextSecondary
                )
            }
        }
    }
}

/**
 * 格式化时间戳
 * @param timestamp 时间戳（毫秒）
 * @return 格式化后的时间字符串
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚" // 小于1分钟
        diff < 3600000 -> "${diff / 60000} 分钟前" // 小于1小时
        diff < 86400000 -> "${diff / 3600000} 小时前" // 小于1天
        diff < 604800000 -> "${diff / 86400000} 天前" // 小于7天
        else -> {
            // 超过7天显示具体日期
            val date = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(timestamp))
            date
        }
    }
}



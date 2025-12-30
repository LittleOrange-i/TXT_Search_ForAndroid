package com.youzi.txt_search_tool.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.youzi.txt_search_tool.data.model.SearchResult
import com.youzi.txt_search_tool.ui.components.NeumorphicCard
import com.youzi.txt_search_tool.ui.theme.*
import com.youzi.txt_search_tool.ui.viewmodel.MainViewModel

/**
 * 搜索结果界面
 * @param viewModel 主ViewModel
 * @param onNavigateBack 返回主界面的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val fileName by viewModel.fileName.collectAsState()
    val ignoredResults by viewModel.ignoredResults.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val quickPhrases by viewModel.quickPhrases.collectAsState()
    
    var showQuickPhraseDialog by remember { mutableStateOf(false) }
    var newQuickPhrase by remember { mutableStateOf("") }
    var showBatchIgnoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "搜索结果",
                        color = NeumorphicText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = NeumorphicText
                        )
                    }
                },
                actions = {
                    // 二次过滤搜索按钮
                    IconButton(onClick = { showBatchIgnoreDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "批量忽略",
                            tint = NeumorphicPrimary
                        )
                    }
                    // 快捷字段按钮
                    IconButton(onClick = { showQuickPhraseDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "快捷字段",
                            tint = NeumorphicPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeumorphicBackground
                )
            )
        },
        containerColor = NeumorphicBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 替换成功提示横幅（持久显示，不自动消失）
            successMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NeumorphicSuccess.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "成功",
                            tint = NeumorphicSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            color = NeumorphicSuccess,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // 搜索信息卡片
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "文件: $fileName",
                        fontSize = 14.sp,
                        color = NeumorphicTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "搜索: \"$searchQuery\"",
                        fontSize = 14.sp,
                        color = NeumorphicTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // 计算未处理的数量（排除已忽略的结果）
                    val remainingCount = searchResults.count { result ->
                        !result.lineNumbers.any { ignoredResults.contains(it) }
                    }
                    Text(
                        text = "找到 ${searchResults.size} 处匹配，还剩 $remainingCount 处未处理",
                        fontSize = 14.sp,
                        color = NeumorphicPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 搜索结果列表
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "未找到匹配结果",
                        fontSize = 16.sp,
                        color = NeumorphicTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(searchResults) { result ->
                        if (!result.lineNumbers.any { ignoredResults.contains(it) }) {
                            SearchResultItem(
                                result = result,
                                searchQuery = searchQuery,
                                quickPhrases = quickPhrases,
                                onReplace = { replaceText ->
                                    viewModel.replaceSingleResult(result, replaceText)
                                },
                                onIgnore = {
                                    viewModel.ignoreResult(result.lineNumbers, result.displayText)
                                },
                                onAddQuickPhrase = { phrase ->
                                    viewModel.addQuickPhrase(phrase)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        
        // 快捷字段管理弹窗
        if (showQuickPhraseDialog) {
            QuickPhraseDialog(
                quickPhrases = quickPhrases,
                onDismiss = { showQuickPhraseDialog = false },
                onAddPhrase = { phrase ->
                    viewModel.addQuickPhrase(phrase)
                },
                onRemovePhrase = { phrase ->
                    viewModel.removeQuickPhrase(phrase)
                }
            )
        }
        
        // 批量忽略弹窗
        if (showBatchIgnoreDialog) {
            BatchIgnoreDialog(
                searchResults = searchResults,
                ignoredResults = ignoredResults,
                onDismiss = { showBatchIgnoreDialog = false },
                onBatchIgnore = { keyword ->
                    // 遍历所有搜索结果，找出包含关键字的结果并批量忽略
                    searchResults.forEach { result ->
                        if (result.displayText.contains(keyword, ignoreCase = true) &&
                            !result.lineNumbers.any { ignoredResults.contains(it) }) {
                            viewModel.ignoreResult(result.lineNumbers, result.displayText)
                        }
                    }
                }
            )
        }
    }
}

/**
 * 搜索结果项组件
 * @param result 搜索结果
 * @param searchQuery 搜索关键字
 * @param quickPhrases 快捷字段列表
 * @param onReplace 替换回调
 * @param onIgnore 忽略回调
 * @param onAddQuickPhrase 添加快捷字段回调
 */
@Composable
fun SearchResultItem(
    result: SearchResult,
    searchQuery: String,
    quickPhrases: List<String>,
    onReplace: (String) -> Unit,
    onIgnore: () -> Unit,
    onAddQuickPhrase: (String) -> Unit
) {
    var showDetailDialog by remember { mutableStateOf(false) }
    // 使用 result 的唯一标识作为 key，确保每个结果项都有独立的输入框状态
    var replaceText by remember(result.primaryLineNumber, result.lineNumbers) { mutableStateOf("") }
    var showQuickPhraseMenu by remember { mutableStateOf(false) }

    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // 行号信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (result.matchCount > 1) {
                        "第 ${result.lineNumbers.joinToString(", ")} 行"
                    } else {
                        "第 ${result.primaryLineNumber} 行"
                    },
                    fontSize = 12.sp,
                    color = NeumorphicPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                if (result.matchCount > 1) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeumorphicPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${result.matchCount} 处",
                            fontSize = 11.sp,
                            color = NeumorphicPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 显示搜索结果（前3字+关键字+后3字，高亮关键字）
            Text(
                text = buildHighlightedText(result.displayText, searchQuery),
                fontSize = 14.sp,
                color = NeumorphicText,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 快捷字段横向滚动列表
            if (quickPhrases.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPhrases.size) { index ->
                        val phrase = quickPhrases[index]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeumorphicPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.clickable {
                                replaceText = phrase
                            }
                        ) {
                            Text(
                                text = phrase,
                                fontSize = 12.sp,
                                color = NeumorphicPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 替换输入框
            OutlinedTextField(
                value = replaceText,
                onValueChange = { replaceText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "输入替换内容",
                        color = NeumorphicTextSecondary.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NeumorphicSurface,
                    unfocusedContainerColor = NeumorphicSurface,
                    focusedBorderColor = NeumorphicPrimary,
                    unfocusedBorderColor = NeumorphicDark.copy(alpha = 0.3f),
                    focusedTextColor = NeumorphicText,
                    unfocusedTextColor = NeumorphicText
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 操作按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 段落详情按钮
                OutlinedButton(
                    onClick = { showDetailDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeumorphicPrimary
                    ),
                    border = BorderStroke(1.dp, NeumorphicPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "详情",
                        fontSize = 13.sp
                    )
                }

                // 添加到快捷字段按钮
                OutlinedButton(
                    onClick = { 
                        if (replaceText.isNotBlank()) {
                            onAddQuickPhrase(replaceText)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeumorphicPrimary
                    ),
                    border = BorderStroke(1.dp, NeumorphicPrimary.copy(alpha = 0.5f)),
                    enabled = replaceText.isNotBlank()
                ) {
                    Text(
                        text = "添加",
                        fontSize = 13.sp
                    )
                }

                // 替换按钮
                Button(
                    onClick = { 
                        onReplace(replaceText)
                        // 替换成功后清空输入框
                        replaceText = ""
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeumorphicSuccess,
                        contentColor = NeumorphicLight
                    ),
                    enabled = replaceText.isNotEmpty()
                ) {
                    Text(
                        text = "替换",
                        fontSize = 13.sp
                    )
                }

                // 忽略按钮
                OutlinedButton(
                    onClick = onIgnore,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeumorphicTextSecondary
                    ),
                    border = BorderStroke(1.dp, NeumorphicDark.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "忽略",
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // 段落详情弹窗
    if (showDetailDialog) {
        ParagraphDetailDialog(
            result = result,
            searchQuery = searchQuery,
            onDismiss = { showDetailDialog = false }
        )
    }
}

/**
 * 段落详情弹窗（显示每个匹配位置的前一行、当前行、后一行）
 * @param result 搜索结果
 * @param searchQuery 搜索关键字
 * @param onDismiss 关闭回调
 */
@Composable
fun ParagraphDetailDialog(
    result: SearchResult,
    searchQuery: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "段落详情 (${result.matchCount} 处匹配)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicText
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = NeumorphicTextSecondary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = NeumorphicDark.copy(alpha = 0.3f)
                )

                // 滚动内容区域
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 遍历每个匹配位置
                    result.lineNumbers.forEachIndexed { index, lineNumber ->
                        ParagraphContextItem(
                            lineNumber = lineNumber,
                            previousLine = result.previousLines.getOrNull(index) ?: "",
                            currentLine = result.matchLines.getOrNull(index) ?: "",
                            nextLine = result.nextLines.getOrNull(index) ?: "",
                            searchQuery = searchQuery,
                            showDivider = index < result.lineNumbers.size - 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单个段落上下文项
 * @param lineNumber 行号
 * @param previousLine 前一行
 * @param currentLine 当前行（包含关键字）
 * @param nextLine 后一行
 * @param searchQuery 搜索关键字
 * @param showDivider 是否显示分隔线
 */
@Composable
fun ParagraphContextItem(
    lineNumber: Int,
    previousLine: String,
    currentLine: String,
    nextLine: String,
    searchQuery: String,
    showDivider: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 行号标题
        Text(
            text = "第 $lineNumber 行",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NeumorphicPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 前一行
        if (previousLine.isNotEmpty()) {
            LineSection(
                title = "前一行",
                content = previousLine,
                searchQuery = searchQuery,
                backgroundColor = NeumorphicSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 当前行（包含关键字）
        LineSection(
            title = "当前行（包含关键字）",
            content = currentLine,
            searchQuery = searchQuery,
            backgroundColor = NeumorphicPrimary.copy(alpha = 0.1f),
            isCurrentLine = true
        )

        // 后一行
        if (nextLine.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LineSection(
                title = "后一行",
                content = nextLine,
                searchQuery = searchQuery,
                backgroundColor = NeumorphicSurface.copy(alpha = 0.5f)
            )
        }

        // 分隔线
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 24.dp),
                color = NeumorphicDark.copy(alpha = 0.2f),
                thickness = 2.dp
            )
        }
    }
}


/**
 * 行区域组件
 * @param title 标题
 * @param content 内容
 * @param searchQuery 搜索关键字
 * @param backgroundColor 背景色
 * @param isCurrentLine 是否为当前行
 */
@Composable
fun LineSection(
    title: String,
    content: String,
    searchQuery: String,
    backgroundColor: Color,
    isCurrentLine: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrentLine) NeumorphicPrimary else NeumorphicTextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = buildHighlightedText(content, searchQuery),
            fontSize = 14.sp,
            color = NeumorphicText,
            lineHeight = 20.sp
        )
    }
}

/**
 * 构建高亮文本（将关键字标记为红色）
 * @param text 原始文本
 * @param searchQuery 搜索关键字
 * @return 带高亮的AnnotatedString
 */
@Composable
fun buildHighlightedText(text: String, searchQuery: String) = buildAnnotatedString {
    var startIndex = 0
    var index = text.indexOf(searchQuery, startIndex, ignoreCase = true)

    while (index != -1) {
        // 添加匹配前的文本
        if (index > startIndex) {
            append(text.substring(startIndex, index))
        }

        // 高亮匹配的文本（红色）
        withStyle(
            style = SpanStyle(
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                background = Color.Red.copy(alpha = 0.15f)
            )
        ) {
            append(text.substring(index, index + searchQuery.length))
        }

        startIndex = index + searchQuery.length
        index = text.indexOf(searchQuery, startIndex, ignoreCase = true)
    }

    // 添加剩余文本
    if (startIndex < text.length) {
        append(text.substring(startIndex))
    }
}

/**
 * 快捷字段管理弹窗
 * @param quickPhrases 快捷字段列表
 * @param onDismiss 关闭回调
 * @param onAddPhrase 添加快捷字段回调
 * @param onRemovePhrase 删除快捷字段回调
 */
@Composable
fun QuickPhraseDialog(
    quickPhrases: List<String>,
    onDismiss: () -> Unit,
    onAddPhrase: (String) -> Unit,
    onRemovePhrase: (String) -> Unit
) {
    var newPhrase by remember { mutableStateOf("") }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏 - 使用渐变背景和更现代的设计
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = NeumorphicPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 装饰性图标
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = NeumorphicPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = NeumorphicPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Column {
                                Text(
                                    text = "快捷字段管理",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeumorphicText
                                )
                                Text(
                                    text = "管理常用替换内容",
                                    fontSize = 12.sp,
                                    color = NeumorphicTextSecondary.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeumorphicSurface,
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = NeumorphicTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = NeumorphicDark.copy(alpha = 0.3f)
                )

                // 添加新快捷字段输入框
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPhrase,
                        onValueChange = { newPhrase = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "输入新快捷字段",
                                color = NeumorphicTextSecondary.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NeumorphicSurface,
                            unfocusedContainerColor = NeumorphicSurface,
                            focusedBorderColor = NeumorphicPrimary,
                            unfocusedBorderColor = NeumorphicDark.copy(alpha = 0.3f),
                            focusedTextColor = NeumorphicText,
                            unfocusedTextColor = NeumorphicText
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                    )
                    
                    Button(
                        onClick = {
                            if (newPhrase.isNotBlank()) {
                                onAddPhrase(newPhrase)
                                newPhrase = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeumorphicPrimary,
                            contentColor = NeumorphicLight
                        ),
                        enabled = newPhrase.isNotBlank()
                    ) {
                        Text(text = "添加", fontSize = 14.sp)
                    }
                }

                // 快捷字段列表
                if (quickPhrases.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无快捷字段",
                            fontSize = 14.sp,
                            color = NeumorphicTextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickPhrases.size) { index ->
                            val phrase = quickPhrases[index]
                            QuickPhraseItem(
                                phrase = phrase,
                                onRemove = { onRemovePhrase(phrase) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 快捷字段项
 * @param phrase 快捷字段内容
 * @param onRemove 删除回调
 */
@Composable
fun QuickPhraseItem(
    phrase: String,
    onRemove: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = phrase,
                fontSize = 14.sp,
                color = NeumorphicText,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    tint = NeumorphicTextSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 批量忽略弹窗
 * @param searchResults 搜索结果列表
 * @param ignoredResults 已忽略的结果集合
 * @param onDismiss 关闭回调
 * @param onBatchIgnore 批量忽略回调
 */
@Composable
fun BatchIgnoreDialog(
    searchResults: List<SearchResult>,
    ignoredResults: Set<Int>,
    onDismiss: () -> Unit,
    onBatchIgnore: (String) -> Unit
) {
    var filterKeyword by remember { mutableStateOf("") }
    
    // 计算匹配的结果数量（排除已忽略的）
    val matchCount = remember(filterKeyword, searchResults, ignoredResults) {
        if (filterKeyword.isBlank()) {
            0
        } else {
            searchResults.count { result ->
                result.displayText.contains(filterKeyword, ignoreCase = true) &&
                !result.lineNumbers.any { ignoredResults.contains(it) }
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "批量忽略",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicText
                        )
                        Text(
                            text = "二次过滤搜索结果",
                            fontSize = 12.sp,
                            color = NeumorphicTextSecondary.copy(alpha = 0.7f)
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = NeumorphicTextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = NeumorphicDark.copy(alpha = 0.3f)
                )
                
                // 说明文字
                Text(
                    text = "输入关键字，匹配到的搜索结果将被批量忽略",
                    fontSize = 13.sp,
                    color = NeumorphicTextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // 输入框
                OutlinedTextField(
                    value = filterKeyword,
                    onValueChange = { filterKeyword = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "输入过滤关键字",
                            color = NeumorphicTextSecondary.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NeumorphicSurface,
                        unfocusedContainerColor = NeumorphicSurface,
                        focusedBorderColor = NeumorphicPrimary,
                        unfocusedBorderColor = NeumorphicDark.copy(alpha = 0.3f),
                        focusedTextColor = NeumorphicText,
                        unfocusedTextColor = NeumorphicText
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                )
                
                // 匹配数量提示
                if (filterKeyword.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "将忽略 $matchCount 条匹配结果",
                        fontSize = 13.sp,
                        color = if (matchCount > 0) NeumorphicPrimary else NeumorphicTextSecondary,
                        fontWeight = if (matchCount > 0) FontWeight.Medium else FontWeight.Normal
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NeumorphicTextSecondary
                        ),
                        border = BorderStroke(1.dp, NeumorphicDark.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "取消",
                            fontSize = 14.sp
                        )
                    }
                    
                    // 确认忽略按钮
                    Button(
                        onClick = {
                            if (filterKeyword.isNotBlank() && matchCount > 0) {
                                onBatchIgnore(filterKeyword)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeumorphicPrimary,
                            contentColor = NeumorphicLight
                        ),
                        enabled = filterKeyword.isNotBlank() && matchCount > 0
                    ) {
                        Text(
                            text = "批量忽略",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

package com.youzi.txt_search_tool

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.youzi.txt_search_tool.ui.screens.MainScreen
import com.youzi.txt_search_tool.ui.screens.SearchResultsScreen
import com.youzi.txt_search_tool.ui.theme.TXT_Search_ToolTheme
import com.youzi.txt_search_tool.ui.viewmodel.MainViewModel

/**
 * 主Activity
 * 应用入口
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TXT_Search_ToolTheme {
                TXTSearchApp()
            }
        }
    }
}

/**
 * 应用主组件
 * 管理导航和状态
 */
@Composable
fun TXTSearchApp() {
    val context = LocalContext.current
    val viewModel = remember { MainViewModel(context) }
    
    // 导航状态：true表示在搜索结果页面，false表示在主页面
    var showResults by remember { mutableStateOf(false) }
    
    // 获取是否有未保存的替换操作
    val hasUnsavedReplacements by viewModel.hasUnsavedReplacements.collectAsState()
    
    // 是否显示退出确认对话框
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    
    // 处理搜索结果页面的系统返回键
    BackHandler(enabled = showResults) {
        showResults = false
        // 返回主页时清空搜索结果，避免重复触发导航
        viewModel.clearSearchResults()
    }
    
    // 处理主页面的系统返回键 - 检测未保存的替换操作
    BackHandler(enabled = !showResults && hasUnsavedReplacements) {
        showExitConfirmDialog = true
    }

    if (showResults) {
        SearchResultsScreen(
            viewModel = viewModel,
            onNavigateBack = { 
                showResults = false
                // 返回主页时清空搜索结果，避免重复触发导航
                viewModel.clearSearchResults()
            }
        )
    } else {
        MainScreen(
            viewModel = viewModel,
            onNavigateToResults = { 
                showResults = true
            }
        )
        
        // 退出确认对话框
        if (showExitConfirmDialog) {
            ExitConfirmDialog(
                onSave = {
                    viewModel.saveFile()
                    showExitConfirmDialog = false
                    // 保存后退出应用
                    (context as? Activity)?.finish()
                },
                onDismiss = {
                    viewModel.discardUnsavedReplacements()
                    showExitConfirmDialog = false
                    // 不保存直接退出应用
                    (context as? Activity)?.finish()
                },
                onCancel = {
                    showExitConfirmDialog = false
                }
            )
        }
    }
}

/**
 * 退出确认对话框
 * @param onSave 保存回调
 * @param onDismiss 不保存回调
 * @param onCancel 取消回调
 */
@Composable
fun ExitConfirmDialog(
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onCancel) {
        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 警告图标
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Info,
                    contentDescription = "提示",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标题
                Text(
                    text = "有未保存的替换操作",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 说明文字
                Text(
                    text = "你有未保存的替换内容，是否保存？",
                    fontSize = 18.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮组
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 不保存按钮
                    androidx.compose.material3.OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "不保存", fontSize = 14.sp)
                    }
                    
                    // 取消按钮
                    androidx.compose.material3.OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "取消", fontSize = 14.sp)
                    }
                    
                   
                }
            }
        }
    }
}

package com.youzi.txt_search_tool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
    
    // 处理系统返回键
    BackHandler(enabled = showResults) {
        showResults = false
        // 返回主页时清空搜索结果，避免重复触发导航
        viewModel.clearSearchResults()
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
    }
}

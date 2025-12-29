package com.youzi.txt_search_tool.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youzi.txt_search_tool.data.model.ReplaceHistory
import com.youzi.txt_search_tool.data.model.SearchResult
import com.youzi.txt_search_tool.data.repository.FileRepository
import com.youzi.txt_search_tool.data.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch

/**
 * 主ViewModel
 * 管理应用状态和业务逻辑
 */
class MainViewModel(context: Context) : ViewModel() {

    private val fileRepository = FileRepository(context)
    private val searchRepository = SearchRepository()

    // 当前选择的文件URI
    private val _fileUri = MutableStateFlow<Uri?>(null)
    val fileUri: StateFlow<Uri?> = _fileUri.asStateFlow()

    // 文件名
    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    // 文件大小
    private val _fileSize = MutableStateFlow(0L)
    val fileSize: StateFlow<Long> = _fileSize.asStateFlow()

    // 搜索关键字
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 替换文本
    private val _replaceText = MutableStateFlow("")
    val replaceText: StateFlow<String> = _replaceText.asStateFlow()

    // 搜索结果列表
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    // 已忽略的搜索结果索引集合
    private val _ignoredResults = MutableStateFlow<Set<Int>>(emptySet())
    val ignoredResults: StateFlow<Set<Int>> = _ignoredResults.asStateFlow()

    // 搜索进度 (0.0 - 1.0)
    private val _searchProgress = MutableStateFlow(0f)
    val searchProgress: StateFlow<Float> = _searchProgress.asStateFlow()

    // 是否正在搜索
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // 是否正在替换
    private val _isReplacing = MutableStateFlow(false)
    val isReplacing: StateFlow<Boolean> = _isReplacing.asStateFlow()

    // 替换历史记录
    private val _replaceHistory = MutableStateFlow<List<ReplaceHistory>>(emptyList())
    val replaceHistory: StateFlow<List<ReplaceHistory>> = _replaceHistory.asStateFlow()

    // 错误信息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 成功信息
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // 当前文件内容（用于保存功能）
    private val _currentContent = MutableStateFlow("")
    val currentContent: StateFlow<String> = _currentContent.asStateFlow()

    // 是否有未保存的替换操作
    private val _hasUnsavedReplacements = MutableStateFlow(false)
    val hasUnsavedReplacements: StateFlow<Boolean> = _hasUnsavedReplacements.asStateFlow()

    // 搜索任务
    private var searchJob: Job? = null

    /**
     * 设置文件URI
     * @param uri 文件URI
     */
    fun setFileUri(uri: Uri) {
        _fileUri.value = uri
        viewModelScope.launch {
            try {
                _fileName.value = fileRepository.getFileName(uri)
                _fileSize.value = fileRepository.getFileSize(uri)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "无法读取文件信息: ${e.message}"
            }
        }
    }

    /**
     * 更新搜索关键字
     * @param query 搜索关键字
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * 更新替换文本
     * @param text 替换文本
     */
    fun updateReplaceText(text: String) {
        _replaceText.value = text
    }

    /**
     * 执行搜索
     * @param showNoResultError 是否在无结果时显示错误消息，默认为 true
     */
    fun performSearch(showNoResultError: Boolean = true) {
        val uri = _fileUri.value ?: run {
            _errorMessage.value = "请先选择文件"
            return
        }

        val query = _searchQuery.value
        if (query.isEmpty()) {
            _errorMessage.value = "请输入搜索关键字"
            return
        }

        // 取消之前的搜索任务
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            try {
                _isSearching.value = true
                _searchResults.value = emptyList()
                _searchProgress.value = 0f
                _errorMessage.value = null

                // 计算总行数用于进度显示
                val totalLines = fileRepository.countLines(uri)
                var processedLines = 0

                // 执行搜索 - 从当前内容或文件读取
                val searchContent = if (_currentContent.value.isNotEmpty()) {
                    // 如果已有内容（已做过替换），从内存中搜索
                    _currentContent.value.lineSequence().asFlow()
                } else {
                    // 否则从文件读取
                    fileRepository.readLargeFile(uri)
                }

                searchRepository.searchWithContext(
                    content = searchContent,
                    query = query,
                    contextSize = 2
                ).collect { result ->
                    // 添加搜索结果
                    _searchResults.value = _searchResults.value + result

                    // 更新进度
                    processedLines++
                    _searchProgress.value = if (totalLines > 0) {
                        processedLines.toFloat() / totalLines
                    } else {
                        1f
                    }
                }

                // 搜索完成
                _searchProgress.value = 1f
                
                // 如果没有搜索结果，根据参数决定是否显示提示
                if (_searchResults.value.isEmpty() && showNoResultError) {
                    _errorMessage.value = "未找到匹配结果"
                }
            } catch (e: Exception) {
                _errorMessage.value = "搜索失败: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * 停止搜索
     */
    fun stopSearch() {
        searchJob?.cancel()
        searchJob = null
        _isSearching.value = false
    }

    /**
     * 执行全文替换（只更新内存，不写入文件）
     */
    fun performReplaceAll() {
        val uri = _fileUri.value ?: run {
            _errorMessage.value = "请先选择文件"
            return
        }

        val query = _searchQuery.value
        val replace = _replaceText.value

        if (query.isEmpty()) {
            _errorMessage.value = "请输入搜索关键字"
            return
        }

        viewModelScope.launch {
            try {
                _isReplacing.value = true
                _errorMessage.value = null

                val resultLines = mutableListOf<String>()
                var totalReplacements = 0

                // 从当前内容或文件读取
                val sourceContent = if (_currentContent.value.isNotEmpty()) {
                    // 如果已有内容（已做过替换），从内存中读取
                    _currentContent.value.lineSequence().asFlow()
                } else {
                    // 否则从文件读取
                    fileRepository.readLargeFile(uri)
                }

                // 执行替换（只在内存中）
                searchRepository.replaceContent(
                    content = sourceContent,
                    searchQuery = query,
                    replaceText = replace
                ).collect { (line, replacements) ->
                    resultLines.add(line)
                    totalReplacements = replacements
                }

                // 更新内存中的内容，不写入文件
                _currentContent.value = resultLines.joinToString("\n")

                // 标记有未保存的替换操作
                _hasUnsavedReplacements.value = true

                // 添加到历史记录
                val history = ReplaceHistory(
                    searchQuery = query,
                    replaceText = replace,
                    count = totalReplacements,
                    timestamp = System.currentTimeMillis()
                )
                _replaceHistory.value = listOf(history) + _replaceHistory.value

                // 清空搜索结果并重新搜索（不显示无结果错误）
                _searchResults.value = emptyList()
                performSearch(showNoResultError = false)

                _successMessage.value = "替换完成，共替换 $totalReplacements 处，请返回主页面保存"
            } catch (e: Exception) {
                _errorMessage.value = "替换失败: ${e.message}"
            } finally {
                _isReplacing.value = false
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 清除成功信息
     */
    fun clearSuccess() {
        _successMessage.value = null
    }

    /**
     * 清空搜索结果
     */
    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _searchProgress.value = 0f
        _ignoredResults.value = emptySet()
    }

    /**
     * 忽略某个搜索结果（忽略所有匹配的行）
     * @param lineNumbers 行号列表
     */
    fun ignoreResult(lineNumbers: List<Int>) {
        _ignoredResults.value = _ignoredResults.value + lineNumbers.toSet()
    }

    /**
     * 替换单个搜索结果（只更新内存，不写入文件）
     * @param result 搜索结果
     * @param replaceText 替换文本
     */
    fun replaceSingleResult(result: SearchResult, replaceText: String) {
        val uri = _fileUri.value ?: run {
            _errorMessage.value = "请先选择文件"
            return
        }

        val query = _searchQuery.value
        if (query.isEmpty()) {
            _errorMessage.value = "搜索关键字为空"
            return
        }

        viewModelScope.launch {
            try {
                _isReplacing.value = true
                _errorMessage.value = null

                // 从当前内容或文件读取所有行
                val allLines = mutableListOf<String>()
                val sourceContent = if (_currentContent.value.isNotEmpty()) {
                    // 如果已有内容（已做过替换），从内存中读取
                    _currentContent.value.lineSequence().asFlow()
                } else {
                    // 否则从文件读取
                    fileRepository.readLargeFile(uri)
                }
                
                sourceContent.collect { line ->
                    allLines.add(line)
                }

                // 替换所有匹配的行
                var replacedCount = 0
                result.lineNumbers.forEach { lineNumber ->
                    if (lineNumber > 0 && lineNumber <= allLines.size) {
                        val lineIndex = lineNumber - 1
                        allLines[lineIndex] = allLines[lineIndex].replace(query, replaceText, ignoreCase = true)
                        replacedCount++
                    }
                }

                // 更新内存中的内容，不写入文件
                _currentContent.value = allLines.joinToString("\n")

                // 标记有未保存的替换操作
                _hasUnsavedReplacements.value = true

                // 从搜索结果中移除这些项
                _searchResults.value = _searchResults.value.filter { 
                    it.lineNumbers.none { lineNumber -> result.lineNumbers.contains(lineNumber) }
                }

                _successMessage.value = "替换成功，共替换 $replacedCount 行，请返回主页面保存"
            } catch (e: Exception) {
                _errorMessage.value = "替换失败: ${e.message}"
            } finally {
                _isReplacing.value = false
            }
        }
    }

    /**
     * 加载当前文件内容
     */
    fun loadCurrentContent() {
        val uri = _fileUri.value ?: run {
            _errorMessage.value = "请先选择文件"
            return
        }

        viewModelScope.launch {
            try {
                val lines = mutableListOf<String>()
                fileRepository.readLargeFile(uri).collect { line ->
                    lines.add(line)
                }
                _currentContent.value = lines.joinToString("\n")
            } catch (e: Exception) {
                _errorMessage.value = "读取文件失败: ${e.message}"
            }
        }
    }

    /**
     * 保存文件（覆盖原文件）
     */
    fun saveFile() {
        val uri = _fileUri.value ?: run {
            _errorMessage.value = "请先选择文件"
            return
        }

        val content = _currentContent.value
        if (content.isEmpty()) {
            _errorMessage.value = "文件内容为空"
            return
        }

        viewModelScope.launch {
            try {
                _errorMessage.value = null
                fileRepository.writeFile(uri, content)
                _successMessage.value = "保存成功"
                // 保存成功后清除未保存标记
                _hasUnsavedReplacements.value = false
            } catch (e: Exception) {
                _errorMessage.value = "保存失败: ${e.message}"
            }
        }
    }

    /**
     * 另存为文件
     * @param targetUri 目标文件URI
     */
    fun saveFileAs(targetUri: Uri) {
        val content = _currentContent.value
        if (content.isEmpty()) {
            _errorMessage.value = "文件内容为空"
            return
        }

        viewModelScope.launch {
            try {
                _errorMessage.value = null
                fileRepository.writeFile(targetUri, content)
                _successMessage.value = "另存为成功"
                // 另存为成功后清除未保存标记
                _hasUnsavedReplacements.value = false
            } catch (e: Exception) {
                _errorMessage.value = "另存为失败: ${e.message}"
            }
        }
    }

    /**
     * 更新当前文件内容
     * @param content 新内容
     */
    fun updateCurrentContent(content: String) {
        _currentContent.value = content
    }

    /**
     * 丢弃未保存的替换操作
     */
    fun discardUnsavedReplacements() {
        _hasUnsavedReplacements.value = false
        _currentContent.value = ""
        _replaceHistory.value = emptyList()
    }
}

package com.youzi.txt_search_tool.data.model

/**
 * 搜索结果数据模型
 * @param lineNumbers 所有匹配的行号列表（合并相同搜索结果）
 * @param displayText 显示文本（前3字+关键字+后3字）
 * @param matchLines 所有匹配行的完整内容
 * @param previousLines 前一行内容列表
 * @param nextLines 后一行内容列表
 * @param matchPosition 关键字在完整行中的位置
 */
data class SearchResult(
    val lineNumbers: List<Int>,
    val displayText: String,
    val matchLines: List<String>,
    val previousLines: List<String>,
    val nextLines: List<String>,
    val matchPosition: Int
) {
    // 主要行号（第一个匹配的行号）
    val primaryLineNumber: Int get() = lineNumbers.firstOrNull() ?: 0
    
    // 匹配次数
    val matchCount: Int get() = lineNumbers.size
}

/**
 * 替换历史记录
 * @param searchQuery 搜索关键字
 * @param replaceText 替换文本
 * @param count 替换次数
 * @param timestamp 时间戳
 */
data class ReplaceHistory(
    val searchQuery: String,
    val replaceText: String,
    val count: Int,
    val timestamp: Long
)

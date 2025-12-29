package com.youzi.txt_search_tool.data.repository

import com.youzi.txt_search_tool.data.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * 搜索操作仓库
 * 负责文件搜索和替换功能
 */
class SearchRepository {

    /**
     * 搜索文件内容，带上下文
     * @param content 文件内容流
     * @param query 搜索关键字
     * @param contextSize 上下文字符数（默认3个字符）
     * @param ignoredTexts 已忽略的搜索结果文本集合
     * @return Flow<SearchResult> 搜索结果流（已合并相同结果）
     */
    fun searchWithContext(
        content: Flow<String>,
        query: String,
        contextSize: Int = 3,
        ignoredTexts: Set<String> = emptySet()
    ): Flow<SearchResult> = flow {
        if (query.isEmpty()) return@flow

        val allLines = mutableListOf<String>()
        
        // 收集所有行
        content.collect { line ->
            allLines.add(line)
        }

        // 临时存储：displayText -> SearchResultBuilder
        val resultMap = mutableMapOf<String, SearchResultBuilder>()

        // 遍历所有行进行搜索
        allLines.forEachIndexed { index, line ->
            val lineNumber = index + 1

            // 查找所有关键字出现的位置
            var searchIndex = 0
            while (true) {
                val matchIndex = line.indexOf(query, searchIndex, ignoreCase = true)
                if (matchIndex == -1) break

                // 提取前3字+关键字+后3字
                val startPos = maxOf(0, matchIndex - contextSize)
                val endPos = minOf(line.length, matchIndex + query.length + contextSize)
                val displayText = line.substring(startPos, endPos)

                // 如果displayText在忽略列表中，跳过此结果
                if (ignoredTexts.contains(displayText)) {
                    searchIndex = matchIndex + query.length
                    continue
                }

                // 获取前一行和后一行
                val previousLine = if (index > 0) allLines[index - 1] else ""
                val nextLine = if (index < allLines.size - 1) allLines[index + 1] else ""

                // 合并相同的displayText
                val builder = resultMap.getOrPut(displayText) {
                    SearchResultBuilder(
                        displayText = displayText,
                        matchPosition = matchIndex
                    )
                }
                
                builder.lineNumbers.add(lineNumber)
                builder.matchLines.add(line)
                builder.previousLines.add(previousLine)
                builder.nextLines.add(nextLine)

                searchIndex = matchIndex + query.length
            }
        }

        // 输出合并后的结果
        resultMap.values.forEach { builder ->
            emit(builder.build())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 搜索结果构建器（用于合并相同结果）
     */
    private data class SearchResultBuilder(
        val displayText: String,
        val matchPosition: Int,
        val lineNumbers: MutableList<Int> = mutableListOf(),
        val matchLines: MutableList<String> = mutableListOf(),
        val previousLines: MutableList<String> = mutableListOf(),
        val nextLines: MutableList<String> = mutableListOf()
    ) {
        fun build() = SearchResult(
            lineNumbers = lineNumbers.toList(),
            displayText = displayText,
            matchLines = matchLines.toList(),
            previousLines = previousLines.toList(),
            nextLines = nextLines.toList(),
            matchPosition = matchPosition
        )
    }

    /**
     * 替换文件内容
     * @param content 文件内容流
     * @param searchQuery 搜索关键字
     * @param replaceText 替换文本
     * @return Flow<Pair<String, Int>> 替换后的行和替换次数
     */
    fun replaceContent(
        content: Flow<String>,
        searchQuery: String,
        replaceText: String
    ): Flow<Pair<String, Int>> = flow {
        var totalReplacements = 0

        content.collect { line ->
            val replacedLine = line.replace(searchQuery, replaceText, ignoreCase = true)
            val replacements = countOccurrences(line, searchQuery)
            totalReplacements += replacements

            emit(Pair(replacedLine, totalReplacements))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 计算关键字在字符串中出现的次数
     * @param text 文本
     * @param query 搜索关键字
     * @return 出现次数
     */
    private fun countOccurrences(text: String, query: String): Int {
        if (query.isEmpty()) return 0
        var count = 0
        var index = 0
        while (text.indexOf(query, index, ignoreCase = true).also { index = it } != -1) {
            count++
            index += query.length
        }
        return count
    }
}

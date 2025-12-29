package com.youzi.txt_search_tool.util

/**
 * 文件工具类
 * 提供文件相关的辅助函数
 */
object FileUtils {

    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    /**
     * 根据文件大小确定线程数
     * @param fileSize 文件大小（字节）
     * @return 建议的线程数
     */
    fun getThreadCount(fileSize: Long): Int {
        return when {
            fileSize < 100 * 1024 -> 1
            fileSize < 1 * 1024 * 1024 -> 2
            fileSize < 5 * 1024 * 1024 -> 4
            else -> 8
        }
    }
}

package com.youzi.txt_search_tool.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 文件操作仓库
 * 负责文件的读取、写入等操作
 */
class FileRepository(private val context: Context) {

    /**
     * 流式读取大文件，避免内存溢出
     * @param uri 文件URI
     * @return Flow<String> 每行内容的流
     */
    fun readLargeFile(uri: Uri): Flow<String> = flow {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    emit(line!!)
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 计算文件总行数
     * @param uri 文件URI
     * @return 总行数
     */
    suspend fun countLines(uri: Uri): Int = withContext(Dispatchers.IO) {
        var count = 0
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                while (reader.readLine() != null) {
                    count++
                }
            }
        }
        count
    }

    /**
     * 写入文件
     * @param uri 文件URI
     * @param content 文件内容
     */
    suspend fun writeFile(uri: Uri, content: String) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
            outputStream.write(content.toByteArray(Charsets.UTF_8))
        }
    }

    /**
     * 获取文件名
     * @param uri 文件URI
     * @return 文件名
     */
    suspend fun getFileName(uri: Uri): String = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return@withContext it.getString(nameIndex)
                }
            }
        }
        "未知文件"
    }

    /**
     * 获取文件大小
     * @param uri 文件URI
     * @return 文件大小（字节）
     */
    suspend fun getFileSize(uri: Uri): Long = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex >= 0) {
                    return@withContext it.getLong(sizeIndex)
                }
            }
        }
        0L
    }
}

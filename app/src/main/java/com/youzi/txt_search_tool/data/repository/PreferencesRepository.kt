package com.youzi.txt_search_tool.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 偏好设置数据仓库
 * 用于持久化存储应用配置数据
 */
class PreferencesRepository(context: Context) {

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "txt_search_tool_prefs"
        private const val KEY_QUICK_PHRASES = "quick_phrases"
        private const val KEY_LAST_FILE_URI = "last_file_uri" // 保存上次打开的文件URI
        private const val SEPARATOR = "||" // 使用不常见的分隔符
    }

    /**
     * 保存快捷字段列表
     * @param phrases 快捷字段列表
     */
    suspend fun saveQuickPhrases(phrases: List<String>) = withContext(Dispatchers.IO) {
        val joined = phrases.joinToString(SEPARATOR)
        sharedPreferences.edit()
            .putString(KEY_QUICK_PHRASES, joined)
            .apply()
    }

    /**
     * 加载快捷字段列表
     * @return 快捷字段列表
     */
    suspend fun loadQuickPhrases(): List<String> = withContext(Dispatchers.IO) {
        val joined = sharedPreferences.getString(KEY_QUICK_PHRASES, "") ?: ""
        if (joined.isEmpty()) {
            emptyList()
        } else {
            joined.split(SEPARATOR).filter { it.isNotBlank() }
        }
    }

    /**
     * 清空快捷字段列表
     */
    suspend fun clearQuickPhrases() = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .remove(KEY_QUICK_PHRASES)
            .apply()
    }

    /**
     * 保存上次打开的文件URI
     * @param uri 文件URI字符串
     */
    suspend fun saveLastFileUri(uri: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putString(KEY_LAST_FILE_URI, uri)
            .apply()
    }

    /**
     * 加载上次打开的文件URI
     * @return 文件URI字符串，如果没有则返回null
     */
    suspend fun loadLastFileUri(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_LAST_FILE_URI, null)
    }

    /**
     * 清除上次打开的文件URI
     */
    suspend fun clearLastFileUri() = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .remove(KEY_LAST_FILE_URI)
            .apply()
    }
}

package com.youzi.txt_search_tool.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 拟态化浅色主题
private val NeumorphicLightColorScheme = lightColorScheme(
    primary = NeumorphicPrimary,
    secondary = NeumorphicSecondary,
    tertiary = NeumorphicSecondary,
    background = NeumorphicBackground,
    surface = NeumorphicSurface,
    error = NeumorphicError,
    onPrimary = NeumorphicLight,
    onSecondary = NeumorphicLight,
    onTertiary = NeumorphicLight,
    onBackground = NeumorphicText,
    onSurface = NeumorphicText,
    onError = NeumorphicLight
)

@Composable
fun TXT_Search_ToolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> NeumorphicLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
package com.youzi.txt_search_tool.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youzi.txt_search_tool.ui.theme.*

/**
 * 拟态化卡片组件
 * @param modifier 修饰符
 * @param elevation 阴影高度
 * @param content 卡片内容
 */
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    elevation: Int = 8,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .shadow(elevation.dp, RoundedCornerShape(16.dp), ambientColor = NeumorphicDark, spotColor = NeumorphicLight),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = NeumorphicSurface
        )
    ) {
        Box(
            modifier = Modifier
                .background(NeumorphicSurface)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * 拟态化按钮组件
 * @param text 按钮文本
 * @param onClick 点击事件
 * @param modifier 修饰符
 * @param enabled 是否启用
 */
@Composable
fun NeumorphicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) NeumorphicPrimary else NeumorphicDark.copy(alpha = 0.5f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = NeumorphicLight,
            fontSize = 16.sp,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * 拟态化文本输入框组件
 * @param value 输入值
 * @param onValueChange 值变化回调
 * @param modifier 修饰符
 * @param placeholder 占位符文本
 * @param singleLine 是否单行
 */
@Composable
fun NeumorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = NeumorphicTextSecondary.copy(alpha = 0.6f)
            )
        },
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = NeumorphicSurface,
            unfocusedContainerColor = NeumorphicSurface,
            focusedBorderColor = NeumorphicPrimary,
            unfocusedBorderColor = NeumorphicDark.copy(alpha = 0.3f),
            focusedTextColor = NeumorphicText,
            unfocusedTextColor = NeumorphicText
        )
    )
}

/**
 * 拟态化进度条组件
 * @param progress 进度值 (0.0 - 1.0)
 * @param modifier 修饰符
 */
@Composable
fun NeumorphicProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = NeumorphicPrimary,
            trackColor = NeumorphicDark.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(progress * 100).toInt()}%",
            color = NeumorphicTextSecondary,
            fontSize = 12.sp
        )
    }
}

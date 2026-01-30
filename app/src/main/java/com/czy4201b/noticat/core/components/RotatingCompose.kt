package com.czy4201b.noticat.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun RotatingIcon(
    icon: ImageVector, // 要旋转的图标
    modifier: Modifier = Modifier,
    durationMillis: Int = 1000 // 旋转一周的时间（毫秒）
) {
    // 1. 创建一个无限循环的动画控制器
    val infiniteTransition = rememberInfiniteTransition(label = "icon_rotation_transition")

    // 2. 定义一个从 0 度到 360 度重复的浮点数动画
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing), // 动画时长和匀速
            repeatMode = RepeatMode.Restart // 动画结束后重新开始
        ),
        label = "icon_rotation"
    )

    // 3. 将旋转角度应用到图标的 graphicsLayer 上
    Icon(
        imageVector = icon,
        contentDescription = "Rotating Icon",
        modifier = modifier.graphicsLayer {
            rotationZ = rotation // 沿 Z 轴旋转
        }
    )
}

@Composable
fun RotatingIcon(
    painter: Painter, // 要旋转的图标
    modifier: Modifier = Modifier,
    durationMillis: Int = 1000, // 旋转一周的时间（毫秒）
    tint: Color = LocalContentColor.current
) {
    // 1. 创建一个无限循环的动画控制器
    val infiniteTransition = rememberInfiniteTransition(label = "icon_rotation_transition")

    // 2. 定义一个从 0 度到 360 度重复的浮点数动画
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing), // 动画时长和匀速
            repeatMode = RepeatMode.Restart // 动画结束后重新开始
        ),
        label = "icon_rotation"
    )

    // 3. 将旋转角度应用到图标的 graphicsLayer 上
    Icon(
        painter = painter,
        contentDescription = "Rotating Icon",
        modifier = modifier.graphicsLayer {
            rotationZ = rotation // 沿 Z 轴旋转
        },
        tint = tint
    )
}
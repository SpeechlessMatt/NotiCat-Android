package com.czy4201b.noticat.core.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.czy4201b.noticat.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun SwipeBox(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenWidthPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val dismissThreshold = with(density) { -80.dp.toPx() }   // 往左滑 80dp 触发删除
    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // 高度由内容决定
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp),
            contentAlignment = Alignment.CenterEnd // 图标靠右
        ) {
            val progress = (offsetX.value / dismissThreshold).coerceIn(0f, 1f)
            val iconColor =
                lerp(MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.error, progress)
            Icon(
                painter = painterResource(R.drawable.delete),
                contentDescription = "Delete",
                tint = iconColor,
                modifier = Modifier.graphicsLayer {
                    alpha = progress
                    scaleX = 0.5f + (progress * 0.5f)
                    scaleY = 0.5f + (progress * 0.5f)
                }
            )
        }

        Box(
            modifier = modifier
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value <= dismissThreshold) {
                                    offsetX.animateTo(offsetX.value - screenWidthPx)
                                    onDelete()
                                }
                                offsetX.animateTo(0f)   // 弹回
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newX = (offsetX.value + dragAmount).coerceAtMost(0f)
                                offsetX.snapTo(newX)

                                // 计算当前进度
                                val currentProgress = (newX / dismissThreshold).coerceIn(0f, 1f)

                                // 核心判断逻辑：
                                if (currentProgress >= 1f && !hasTriggeredHaptic) {
                                    // 刚好滑过阈值，且之前没震动过
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    hasTriggeredHaptic = true
                                } else if (currentProgress < 1f && hasTriggeredHaptic) {
                                    // 如果用户又滑回去了，重置状态，下次滑过来还能震
                                    hasTriggeredHaptic = false
                                }
                            }
                        }
                    )
                }
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        ) {
            content()
        }
    }
}

@Composable
fun SwipeLazyColumn(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    onSwipe: () -> Unit,
    swipeTint: String = "继续上拉",
    finishTint: String = "✅ 已添加成功",
    content: LazyListScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current // 加入震动反馈
    val threshold = 400f // 触发拉动的距离阈值

    var totalDelta by remember { mutableFloatStateOf(0f) }
    // 2. 锁定开关：防止单次操作重复触发
    var hasTriggered by remember { mutableStateOf(false) }

    val message = when {
        hasTriggered -> finishTint
        else -> swipeTint
    }

    val animatedDelta by animateFloatAsState(
        targetValue = totalDelta,
        animationSpec = if (totalDelta != 0f) {
            // 用户还在拉，不需要动画，直接跟随（或者用极高的刚度）
            snap()
        } else {
            spring(
                // 阻尼比：关键！决定“晃动”程度。
                // DampingRatioMediumBounce 会有一点点轻微的回弹晃动，很有动感
                // 如果想要更稳重，可以用 DampingRatioLowBounce
                dampingRatio = Spring.DampingRatioMediumBouncy,

                // 刚度：决定回弹的“速度”。
                // StiffnessLow 让它慢慢弹回去，StiffnessVeryLow 会更优雅
                stiffness = Spring.StiffnessLow
            )
        },
        label = "pull_delta"
    )

    // 3. 关键：监听滚动是否停止。当用户松手且列表静止时，重置触发状态和位移
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            totalDelta = 0f
            hasTriggered = false
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (hasTriggered) return available
                if (source == NestedScrollSource.UserInput && available.y < 0) {
                    if (available.y < 0) { // 上拉
                        totalDelta += available.y.absoluteValue
                        if (totalDelta > threshold) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSwipe()
                            hasTriggered = true
                        }
                    } else if (consumed.y > 0) {
                        totalDelta = 0f
                    }
                } else if (source == NestedScrollSource.SideEffect) {
                    // 3. 如果是惯性滑动（Fling）：哪怕滑到了底，也强制清空进度
                    // 这样可以彻底杜绝快速滑动时的误操作
                    totalDelta = 0f
                }
                return available
            }
        }
    }

    Box(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        ) {
            val alpha = remember(totalDelta, hasTriggered) {
                if (hasTriggered) {
                    1f
                } else {
                    val startThreshold = 150f // 延迟 150 像素再开始显现
                    if (totalDelta < startThreshold) {
                        0f
                    } else {
                        // 线性映射：从 startThreshold 到 threshold 之间完成 0 到 1 的淡入
                        ((totalDelta - startThreshold) / (threshold - startThreshold)).coerceIn(
                            0f,
                            1f
                        )
                    }
                }
            }
            Text(
                text = message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                modifier = Modifier.graphicsLayer {
                    // 随着拉动，文字从下方 20px 的位置慢慢漂浮到原位
                    translationY = (1f - alpha) * 20f
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .wrapContentHeight()
                .graphicsLayer {
                    // 核心视觉效果：用户往上拽多少，列表就往上移动多少
                    // 我们给位移加个阻尼感 (* 0.4f)，让拉动显得更有张力
                    translationY = -animatedDelta * 0.4f
                },
            state = listState,
            verticalArrangement = verticalArrangement,
        ) {
            content()
        }
    }
}
package com.czy4201b.noticat.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.czy4201b.noticat.R

@Composable
fun ModernFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = if (enabled) 2.dp else 0.dp,
                shape = RoundedCornerShape(8.dp)
            ),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        content = content
    )
}

@Composable
fun ModernOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        content = content,
        border = if (enabled) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ) else BorderStroke(width = 1.dp, color = Color(0xFFEEEEEE))
    )
}

@Immutable
data class ModernSwitchColors(
    val checkedThumb: Color,
    val checkedTrack: Color,
    val uncheckedThumb: Color,
    val uncheckedTrack: Color
)

object ModernSwitchDefaults {
    @Composable
    fun colors(
        checkedThumb: Color = Color.White,
        checkedTrack: Color = MaterialTheme.colorScheme.primary,
        uncheckedThumb: Color = Color.White,
        uncheckedTrack: Color = MaterialTheme.colorScheme.outline
    ) = ModernSwitchColors(checkedThumb, checkedTrack, uncheckedThumb, uncheckedTrack)
}

@Composable
fun ModernSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ModernSwitchColors = ModernSwitchDefaults.colors()
) {
    val density = LocalDensity.current
    // 尺寸全部按 dp 转 px
    val trackWidth = with(density) { 44.dp.toPx() }
    val trackHeight = with(density) { 24.dp.toPx() }
    val thumbDiameter = with(density) { 20.dp.toPx() }
    val thumbRadius = thumbDiameter / 2
    val padding = with(density) { 2.dp.toPx() }
    val focusStroke = with(density) { 1.dp.toPx() }
    val focusGap = with(density) { 4.dp.toPx() }

    val trackColor by animateColorAsState(
        targetValue = if (checked) colors.checkedTrack else colors.uncheckedTrack,
        label = "trackColor"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) colors.checkedThumb else colors.uncheckedThumb,
        label = "thumbColor"
    )

    // 动画
    val transition = updateTransition(checked, label = "switch")
    val thumbOffset by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 200, easing = FastOutSlowInEasing) },
        label = "offset"
    ) { if (it) trackWidth - thumbDiameter - padding else padding }

    // 按压放大
    var pressed by remember { mutableStateOf(false) }
    val thumbDp by animateFloatAsState(
        targetValue = if (pressed) 22.dp.value else 20.dp.value,
        animationSpec = tween(150), label = "size"
    )

    val semantics = Modifier.semantics {
        role = Role.Switch
        stateDescription = if (checked) "开启" else "关闭"
        if (!enabled) disabled()
    }

    val onClickable = Modifier.pointerInput(checked) {
        if (enabled) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                },
                onTap = {
                    onCheckedChange(!checked)
                }
            )
        }
    }

    Canvas(
        modifier
            .size(44.dp, 24.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .then(onClickable)
            .then(semantics)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 聚焦外框
        if (enabled) {
            drawRoundRect(
                color = Color.Black,
                size = Size(canvasWidth + focusGap * 2, canvasHeight + focusGap * 2),
                cornerRadius = CornerRadius(trackHeight / 2 + focusGap),
                style = Stroke(width = focusStroke),
                topLeft = Offset(-focusGap, -focusGap)
            )
        }

        drawRoundRect(
            color = trackColor,
            size = Size(canvasWidth, canvasHeight),
            cornerRadius = CornerRadius(trackHeight / 2)
        )

        drawCircle(
            color = thumbColor,
            radius = thumbDp * density.density / 2,
            center = Offset(
                thumbOffset + thumbRadius,
                canvasHeight / 2
            ),
        )
    }
}

@Composable
fun ModernDefaultFilledButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable (RowScope.() -> Unit)
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        content = content
    )
}

@Composable
fun ModernDefaultOutlinedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable (RowScope.() -> Unit)
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        content = content
    )
}

@Composable
fun ModernCheckBox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Icon(
        modifier = modifier
            .clickable(
                onClick = {
                    onCheckedChange(!checked)
                },
                indication = null,
                interactionSource = null
            ),
        painter = if (checked)
            painterResource(R.drawable.check_box_checked)
        else
            painterResource(R.drawable.check_box_unchecked),
        contentDescription = null
    )
}

@Preview
@Composable
fun ModernPreview() {
    ModernSwitch(true, {})
}
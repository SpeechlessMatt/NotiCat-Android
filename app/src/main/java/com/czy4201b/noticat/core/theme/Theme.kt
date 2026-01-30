package com.czy4201b.noticat.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    // 主色调 - 深蓝灰系列
    primary = BlueCharcoal,
    onPrimary = PearlWhite,

    // 次要色调 - 暖色点缀
    secondary = BronzeGlow,
    onSecondary = PearlWhite,

    // 第三色调 - 中性灰
    tertiary = Graphite,
    onTertiary = PearlWhite,

    // 背景与表面
    background = PearlWhite,
    onBackground = BlueCharcoal,

    surface = Porcelain,
    onSurface = Graphite,

    surfaceVariant = SilverMist,
    onSurfaceVariant = SteelGray,

    // 容器颜色（用于卡片、按钮等）
    primaryContainer = PrussianBlue.copy(alpha = 0.1f),
    onPrimaryContainer = PrussianBlue,

    secondaryContainer = BronzeGlow.copy(alpha = 0.1f),
    onSecondaryContainer = BronzeGlow,

    // 功能色
    error = SunsetCoral,
    onError = PearlWhite,

    // 边框与轮廓
    outline = Platinum,
    outlineVariant = Platinum.copy(alpha = 0.5f),

    // 交互状态
    inversePrimary = PrussianBlue,
    inverseSurface = DeepSlate,
    inverseOnSurface = SilverFrost,

    // 浮动操作按钮等突出元素
    tertiaryContainer = GoldenSand.copy(alpha = 0.15f),
    onTertiaryContainer = WarmTaupe
)

private val DarkColorScheme = darkColorScheme(
    // 主色调 - 深蓝色系
    primary = Moonstone,
    onPrimary = MidnightNavy,

    // 次要色调 - 温暖的金属色
    secondary = CopperEmber,
    onSecondary = MidnightNavy,

    // 第三色调
    tertiary = SilverFrost,
    onTertiary = DeepSpace,

    // 背景与表面
    background = MidnightNavy,
    onBackground = Moonstone,

    surface = DeepSpace,
    onSurface = SilverFrost,

    surfaceVariant = TwilightBlue,
    onSurfaceVariant = SlateShadow,

    // 容器颜色
    primaryContainer = BlueSteel.copy(alpha = 0.3f),
    onPrimaryContainer = Moonstone,

    secondaryContainer = DesertRose.copy(alpha = 0.2f),
    onSecondaryContainer = CopperEmber,

    // 功能色
    error = RubyRed,
    onError = MidnightNavy,

    // 边框与轮廓
    outline = GraphiteNight,
    outlineVariant = GraphiteNight.copy(alpha = 0.7f),

    // 交互状态
    inversePrimary = PrussianBlue,
    inverseSurface = Moonstone,
    inverseOnSurface = BlueCharcoal,

    // 特殊容器
    tertiaryContainer = MutedGold.copy(alpha = 0.15f),
    onTertiaryContainer = MutedGold
)

// 扩展颜色，用于特殊场景
object ExtendedColors {
    val gradientStart = Color(0xFF667EEA)
    val gradientEnd = Color(0xFF764BA2)

    val cardShadowLight = Color(0x1A000000)  // 浅色卡片阴影
    val cardShadowDark = Color(0x2A000000)   // 深色卡片阴影

    val shimmerBase = Color(0xFFE0E0E0)
    val shimmerHighlight = Color(0xFFF5F5F5)

    val premiumAccent = Color(0xFFFFD700)    // 高级功能金色强调

    val warningLight = GoldenSand
    val warningDark = MutedGold

    val onWarningLight = BlueCharcoal
    val onWarningDark = MidnightNavy

    val successLight = ForestMist
    val successDark = EmeraldGlow

    val onSuccessLight = PearlWhite
    val onSuccessDark = MidnightNavy

    val infoLight = OceanTeal
    val infoDark = CyanGlow

    val onInfoLight = PearlWhite
    val onInfoDark = MidnightNavy
}

// 自定义主题配置
@Immutable
data class CustomColorScheme(
    val gradientStart: Color,
    val gradientEnd: Color,
    val cardShadow: Color,
    val shimmerBase: Color,
    val shimmerHighlight: Color,
    val premiumAccent: Color,
    val warning: Color,
    val onWarning: Color,
    val success : Color,
    val onSuccess :Color,
    val info: Color,
    val onInfo: Color,
)

val LightCustomColors = CustomColorScheme(
    gradientStart = ExtendedColors.gradientStart,
    gradientEnd = ExtendedColors.gradientEnd,
    cardShadow = ExtendedColors.cardShadowLight,
    shimmerBase = ExtendedColors.shimmerBase,
    shimmerHighlight = ExtendedColors.shimmerHighlight,
    premiumAccent = ExtendedColors.premiumAccent,
    warning = ExtendedColors.warningLight,
    onWarning = ExtendedColors.onWarningLight,
    success = ExtendedColors.successLight,
    onSuccess = ExtendedColors.onSuccessLight,
    info = ExtendedColors.infoLight,
    onInfo = ExtendedColors.onInfoLight,
)

val DarkCustomColors = CustomColorScheme(
    gradientStart = ExtendedColors.gradientEnd,
    gradientEnd = ExtendedColors.gradientStart,
    cardShadow = ExtendedColors.cardShadowDark,
    shimmerBase = GraphiteNight,
    shimmerHighlight = SlateShadow,
    premiumAccent = MutedGold,
    warning = ExtendedColors.warningDark,
    onWarning = ExtendedColors.onWarningDark,
    success = ExtendedColors.successDark,
    onSuccess = ExtendedColors.onSuccessDark,
    info = ExtendedColors.infoDark,
    onInfo = ExtendedColors.onInfoDark,
)

val LocalCustomColors = staticCompositionLocalOf {
    LightCustomColors
}

object AppTheme {
    val customColors: CustomColorScheme
        @Composable
        get() = LocalCustomColors.current
}

@Composable
fun NotiCatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        DarkCustomColors
    } else {
        LightCustomColors
    }

    CompositionLocalProvider(LocalCustomColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
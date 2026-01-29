package com.example.myapplication.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * 淺色主題配色方案
 */
private val LightColorScheme = lightColorScheme(
    // Primary - 主要互動元素（按鈕、FAB、連結）
    primary = Orange40,
    onPrimary = Color.White,
    primaryContainer = Orange90,
    onPrimaryContainer = Orange10,

    // Secondary - 次要元素（篩選器、標籤）
    secondary = Brown40,
    onSecondary = Color.White,
    secondaryContainer = Brown90,
    onSecondaryContainer = Brown10,

    // Tertiary - 強調元素（特殊狀態、亮點）
    tertiary = Blue40,
    onTertiary = Color.White,
    tertiaryContainer = Blue90,
    onTertiaryContainer = Blue10,

    // Error - 錯誤狀態
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red40,

    // Background & Surface
    background = Gray99,
    onBackground = Gray10,
    surface = Color.White,
    onSurface = Gray10,
    surfaceVariant = Gray95,
    onSurfaceVariant = Gray20,

    // Outline
    outline = Gray80,
    outlineVariant = Gray90,

    // Inverse (用於 Snackbar 等)
    inverseSurface = Gray20,
    inverseOnSurface = Gray90,
    inversePrimary = Orange80
)

/**
 * 深色主題配色方案
 */
private val DarkColorScheme = darkColorScheme(
    // Primary
    primary = Orange80,
    onPrimary = Orange10,
    primaryContainer = Orange40,
    onPrimaryContainer = Orange90,

    // Secondary
    secondary = Brown80,
    onSecondary = Brown10,
    secondaryContainer = Brown40,
    onSecondaryContainer = Brown90,

    // Tertiary
    tertiary = Blue80,
    onTertiary = Blue10,
    tertiaryContainer = Blue40,
    onTertiaryContainer = Blue90,

    // Error
    error = Red80,
    onError = Red40,
    errorContainer = Red40,
    onErrorContainer = Red90,

    // Background & Surface
    background = Gray10,
    onBackground = Gray90,
    surface = Gray20,
    onSurface = Gray90,
    surfaceVariant = Gray20,
    onSurfaceVariant = Gray80,

    // Outline
    outline = Gray80,
    outlineVariant = Gray20,

    // Inverse
    inverseSurface = Gray90,
    inverseOnSurface = Gray20,
    inversePrimary = Orange40
)

/**
 * 旅遊記錄 App 主題
 *
 * @param darkTheme 是否使用深色模式，預設跟隨系統設定
 * @param content 主題內容
 */
@Composable
fun TravelRecordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalAppDimensions provides AppDimensions()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

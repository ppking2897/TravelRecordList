package io.github.ppking2897.travelrecord.core.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * 淺色主題配色方案（手帳風：米白底、白卡片、墨色字與框線）
 */
private val LightColorScheme = lightColorScheme(
    // Primary - 主要互動元素（按鈕、連結、選中狀態）；色塊放在 container，上面一律墨色字
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = Butter,
    onPrimaryContainer = Ink,

    // Secondary - 次要元素（篩選器、標籤）
    secondary = Brown40,
    onSecondary = Color.White,
    secondaryContainer = Sand,
    onSecondaryContainer = Ink,

    // Tertiary - 強調元素（特殊狀態、亮點）；tertiaryContainer 是新增按鈕與選中狀態的珊瑚紅
    tertiary = Blue40,
    onTertiary = Color.White,
    tertiaryContainer = Coral,
    onTertiaryContainer = Ink,

    // Error - 錯誤狀態
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red40,

    // Background & Surface
    background = Cream,
    onBackground = Ink,
    // 標題列、日期軸等直接放在頁面上的元件；白色卡片用 surfaceContainerLowest
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = InkMuted,

    // 卡片、底部導覽列、選單、對話框的底色；不設的話會用 Material 預設的灰紫色
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,

    // Outline - 卡片框線也用這個顏色
    outline = Ink,
    outlineVariant = Gray90,

    // Inverse (用於 Snackbar 等)
    inverseSurface = Gray20,
    inverseOnSurface = Gray90,
    inversePrimary = Coral
)

/**
 * 旅遊記錄 App 主題
 *
 * 只有淺色配色：手機開深色模式時也用同一套顏色（狀態列圖示顏色在 Android 的 MainActivity 固定）。
 *
 * @param content 主題內容
 */
@Composable
fun TravelRecordTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppDimensions provides AppDimensions()
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            content = content
        )
    }
}

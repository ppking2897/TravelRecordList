package io.github.ppking2897.travelrecord.core.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

/**
 * 探險橙色主題 - 旅遊記錄 App 配色
 *
 * 設計理念：橙色傳達活力、冒險精神，適合戶外/探索風格的旅遊 App
 */

// Primary - 探險橙
val Orange40 = Color(0xFFE65100) // Dark theme primary
val Orange80 = Color(0xFFFFB74D) // Light theme primary
val Orange90 = Color(0xFFFFE0B2) // Light theme primary container

// Secondary - 暖棕色（大地色系）
val Brown40 = Color(0xFF5D4037)

// Tertiary - 深藍色（對比色，用於強調）
val Blue40 = Color(0xFF1565C0)

// Success - 完成狀態綠色
val Green40 = Color(0xFF2E7D32) // Dark theme success
val Green80 = Color(0xFF81C784) // Light theme success
val Green90 = Color(0xFFC8E6C9) // Light theme success container
val Green10 = Color(0xFF1B5E20) // Dark theme on success

// Error
val Red40 = Color(0xFFB71C1C)
val Red90 = Color(0xFFFFEBEE)

// Neutral - 中性色
val Gray20 = Color(0xFF2D2D2D)
val Gray90 = Color(0xFFE8E8E8)

// 手帳風：米白底、墨色字與框線，色塊只當底色用，上面一律放墨色字
// Cream 也是 Android 的啟動底色（androidApp 的 res/values/themes.xml），改了要一起改那裡
val Cream = Color(0xFFFAF6EC)  // 頁面底色
val Ink = Color(0xFF2B2A28)    // 主要文字、圖示、卡片框線
val InkMuted = Color(0xFF6B665E) // 次要文字
val Sand = Color(0xFFF1E7D6)   // 標籤、次要色塊
val Butter = Color(0xFFF8D57E) // 摘要卡
val Coral = Color(0xFFF07060)  // 強調色塊（新增按鈕、選中狀態、進行中）
val Mint = Color(0xFFCDEBC0)   // 已完成的卡片底色
val Leaf = Color(0xFF7CC38A)   // 已完成的圓點與標記
val Sky = Color(0xFFBFDDF2)    // 地點標籤

// Surface variants
val OrangeSurface = Color(0xFFFFF8E1) // 淡橙色背景
val OrangeSurfaceDark = Color(0xFF2D2418) // 深色模式背景

// ============================================================================
// 漸層色系統 (Gradients)
// ============================================================================

/**
 * App 漸層色集合
 */
object AppGradients {
    /** 主題色漸層 - 水平 */
    val primaryGradient: Brush
        get() = Brush.linearGradient(colors = listOf(Orange40, Orange80))

    /** 主題色漸層 - 垂直 */
    val primaryGradientVertical: Brush
        get() = Brush.verticalGradient(colors = listOf(Orange40, Orange80))

    /** 成功色漸層 - 水平 */
    val successGradient: Brush
        get() = Brush.linearGradient(colors = listOf(Green40, Green80))

    /** 成功色漸層 - 垂直 */
    val successGradientVertical: Brush
        get() = Brush.verticalGradient(colors = listOf(Green40, Green80))

    /** 時間軸漸層 - 進行中 */
    val timelineGradient: Brush
        get() = Brush.verticalGradient(colors = listOf(Orange90, Orange80, Orange40))

    /** 時間軸漸層 - 已完成 */
    val timelineCompletedGradient: Brush
        get() = Brush.verticalGradient(colors = listOf(Green90, Green80, Green40))

    /** 卡片覆蓋漸層（用於封面照片底部文字遮罩） */
    val cardOverlayGradient: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.3f),
                Color.Black.copy(alpha = 0.7f),
            ),
        )

    /** 頁面背景（手帳風用單色米白，不做漸層） */
    val warmBackgroundGradient: Brush
        get() = SolidColor(Cream)
}

// ============================================================================
// 完成狀態色彩 (Completed State Colors)
// ============================================================================

/**
 * 完成狀態專用色彩
 * 用於區分已完成/未完成的行程項目
 */
object CompletedStateColors {
    /** 已完成的卡片底色 */
    val background = Mint
    /** 時間軸圓點、日期軸節點、「已完成」標記的底色 */
    val indicator = Leaf
    /** 放在 indicator 上的勾勾與文字 */
    val onIndicator = Ink
    /** 綠色文字（日期軸上已完成那天的地點與進度） */
    val content = Green40
    /** 已完成項目的時間文字 */
    val text = Green10
}

// ============================================================================
// 地點標籤色彩 (Location Tag Colors)
// ============================================================================

/**
 * 地點標籤專用色彩，跟一般標籤（Sand）區分
 */
object LocationTagColors {
    val container = Sky
    val content = Ink
}

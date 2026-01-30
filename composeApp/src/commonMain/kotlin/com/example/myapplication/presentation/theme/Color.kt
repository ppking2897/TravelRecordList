package com.example.myapplication.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 探險橙色主題 - 旅遊記錄 App 配色
 *
 * 設計理念：橙色傳達活力、冒險精神，適合戶外/探索風格的旅遊 App
 */

// Primary - 探險橙
val Orange40 = Color(0xFFE65100) // Dark theme primary
val Orange80 = Color(0xFFFFB74D) // Light theme primary
val Orange90 = Color(0xFFFFE0B2) // Light theme primary container
val Orange10 = Color(0xFF3E2723) // Dark theme on primary

// Secondary - 暖棕色（大地色系）
val Brown40 = Color(0xFF5D4037)
val Brown80 = Color(0xFFBCAAA4)
val Brown90 = Color(0xFFEFEBE9)
val Brown10 = Color(0xFF1B1210)

// Tertiary - 深藍色（對比色，用於強調）
val Blue40 = Color(0xFF1565C0)
val Blue80 = Color(0xFF90CAF9)
val Blue90 = Color(0xFFBBDEFB)
val Blue10 = Color(0xFF0D1B2A)

// Success - 完成狀態綠色
val Green40 = Color(0xFF2E7D32) // Dark theme success
val Green80 = Color(0xFF81C784) // Light theme success
val Green90 = Color(0xFFC8E6C9) // Light theme success container
val Green10 = Color(0xFF1B5E20) // Dark theme on success

// Error
val Red40 = Color(0xFFB71C1C)
val Red80 = Color(0xFFEF9A9A)
val Red90 = Color(0xFFFFEBEE)

// Neutral - 中性色
val Gray10 = Color(0xFF1A1A1A)
val Gray20 = Color(0xFF2D2D2D)
val Gray80 = Color(0xFFC9C9C9)
val Gray90 = Color(0xFFE8E8E8)
val Gray95 = Color(0xFFF5F5F5)
val Gray99 = Color(0xFFFFFBFE)

// Surface variants
val OrangeSurface = Color(0xFFFFF8E1) // 淡橙色背景
val OrangeSurfaceDark = Color(0xFF2D2418) // 深色模式背景

// ============================================================================
// Surface 層次系統 (Surface Levels)
// ============================================================================

/**
 * Light Theme Surface 層次
 * 用於創建視覺深度和層次感
 */
val SurfaceLevel0 = Color(0xFFFFFBFE) // 最底層，純白背景
val SurfaceLevel1 = Color(0xFFFFF8F5) // 第一層，輕微橙色調
val SurfaceLevel2 = Color(0xFFFFF5EE) // 第二層，更明顯的橙色調
val SurfaceLevel3 = Color(0xFFFFF0E5) // 第三層，最高層次

/**
 * Dark Theme Surface 層次
 * 深色模式下的層次感
 */
val SurfaceDarkLevel0 = Color(0xFF1A1A1A) // 最底層，深色背景
val SurfaceDarkLevel1 = Color(0xFF242420) // 第一層，輕微暖色調
val SurfaceDarkLevel2 = Color(0xFF2D2824) // 第二層，更明顯的暖色調
val SurfaceDarkLevel3 = Color(0xFF363028) // 第三層，最高層次

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

    /** 暖色背景漸層 - Light Theme */
    val warmBackgroundGradient: Brush
        get() = Brush.verticalGradient(
            colors = listOf(SurfaceLevel0, SurfaceLevel1, SurfaceLevel2),
        )

    /** 暖色背景漸層 - Dark Theme */
    val darkBackgroundGradient: Brush
        get() = Brush.verticalGradient(
            colors = listOf(SurfaceDarkLevel0, SurfaceDarkLevel1, SurfaceDarkLevel2),
        )
}

// ============================================================================
// 完成狀態色彩 (Completed State Colors)
// ============================================================================

/**
 * 完成狀態專用色彩
 * 用於區分已完成/未完成的行程項目
 */
object CompletedStateColors {
    // Light Theme
    val lightBackground = Green90
    val lightContent = Green40
    val lightBorder = Green80
    val lightIcon = Green40
    val lightText = Green10

    // Dark Theme
    val darkBackground = Green10.copy(alpha = 0.3f)
    val darkContent = Green80
    val darkBorder = Green40
    val darkIcon = Green80
    val darkText = Green90
}

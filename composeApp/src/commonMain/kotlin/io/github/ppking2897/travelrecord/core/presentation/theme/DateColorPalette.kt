package com.example.myapplication.core.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * 為多日行程的每一日分配穩定顏色，讓地圖標記、列表、時間軸可以用
 * 一致的色彩區分不同日子。顏色循環（超過 palette 長度的日期會重複）。
 */
object DateColorPalette {

    /** Material 3 風格的柔和色盤，對 Light/Dark theme 都有可接受的對比。 */
    private val palette: List<Color> = listOf(
        Color(0xFFE57373), // red
        Color(0xFFFFB74D), // orange
        Color(0xFFFFD54F), // amber
        Color(0xFF81C784), // green
        Color(0xFF4DD0E1), // cyan
        Color(0xFF64B5F6), // blue
        Color(0xFF9575CD)  // purple
    )

    /**
     * 依「第幾天」取顏色（Day 1 / Day 2 / ...），0-based。
     */
    fun colorForDayIndex(dayIndex: Int): Color {
        if (palette.isEmpty()) return Color.Unspecified
        val normalized = ((dayIndex % palette.size) + palette.size) % palette.size
        return palette[normalized]
    }
}

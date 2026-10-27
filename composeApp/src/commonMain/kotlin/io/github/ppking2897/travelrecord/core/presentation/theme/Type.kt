package io.github.ppking2897.travelrecord.core.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

/**
 * 手帳風字型：標題、時間、日期、按鈕字用粗體，內文維持一般粗細。
 * 字級與行高沿用 Material 預設，只改粗細。
 */
internal val AppTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontWeight = FontWeight.ExtraBold),
        displayMedium = displayMedium.copy(fontWeight = FontWeight.ExtraBold),
        displaySmall = displaySmall.copy(fontWeight = FontWeight.ExtraBold),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.ExtraBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.ExtraBold),
        titleSmall = titleSmall.copy(fontWeight = FontWeight.ExtraBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.ExtraBold),
        labelMedium = labelMedium.copy(fontWeight = FontWeight.Bold),
        labelSmall = labelSmall.copy(fontWeight = FontWeight.Bold)
    )
}

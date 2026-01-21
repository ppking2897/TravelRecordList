package com.example.myapplication.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design Tokens - 設計令牌系統
 *
 * 統一定義所有尺寸、間距、圓角等設計規範
 * 確保整個 App 的視覺一致性
 */

// ============================================================================
// 間距系統 (Spacing)
// ============================================================================

object Spacing {
    /** 4.dp - 極小間距，用於緊密排列的元素 */
    val xs: Dp = 4.dp

    /** 8.dp - 小間距，用於相關元素之間 */
    val sm: Dp = 8.dp

    /** 12.dp - 中小間距，用於卡片內部元素 */
    val md: Dp = 12.dp

    /** 16.dp - 標準間距，用於區塊之間 */
    val lg: Dp = 16.dp

    /** 24.dp - 大間距，用於主要區塊分隔 */
    val xl: Dp = 24.dp

    /** 32.dp - 超大間距，用於頁面邊距或重要分隔 */
    val xxl: Dp = 32.dp

    /** 48.dp - 極大間距，用於空狀態等特殊場景 */
    val xxxl: Dp = 48.dp
}

// ============================================================================
// 圖標尺寸 (Icon Sizes)
// ============================================================================

object IconSize {
    /** 16.dp - 極小圖標，用於標籤內 */
    val xs: Dp = 16.dp

    /** 20.dp - 小圖標，用於列表項目 */
    val sm: Dp = 20.dp

    /** 24.dp - 標準圖標，用於大多數場景 */
    val md: Dp = 24.dp

    /** 32.dp - 大圖標，用於按鈕或強調 */
    val lg: Dp = 32.dp

    /** 48.dp - 超大圖標，用於空狀態 */
    val xl: Dp = 48.dp

    /** 64.dp - 極大圖標，用於空狀態主圖 */
    val xxl: Dp = 64.dp
}

// ============================================================================
// 圓角半徑 (Corner Radius)
// ============================================================================

object CornerRadius {
    /** 4.dp - 極小圓角，用於標籤 */
    val xs: Dp = 4.dp

    /** 8.dp - 小圓角，用於按鈕、輸入框 */
    val sm: Dp = 8.dp

    /** 12.dp - 標準圓角，用於卡片 */
    val md: Dp = 12.dp

    /** 16.dp - 大圓角，用於底部彈窗 */
    val lg: Dp = 16.dp

    /** 24.dp - 超大圓角，用於圓形按鈕 */
    val xl: Dp = 24.dp

    /** 完全圓形 */
    val full: Dp = 1000.dp
}

// ============================================================================
// 高度/陰影 (Elevation)
// ============================================================================

object Elevation {
    /** 0.dp - 無陰影，用於平面元素 */
    val none: Dp = 0.dp

    /** 1.dp - 極低陰影，用於微小區分 */
    val xs: Dp = 1.dp

    /** 2.dp - 低陰影，用於卡片 */
    val sm: Dp = 2.dp

    /** 4.dp - 標準陰影，用於懸浮卡片 */
    val md: Dp = 4.dp

    /** 8.dp - 高陰影，用於對話框 */
    val lg: Dp = 8.dp

    /** 12.dp - 超高陰影，用於模態窗口 */
    val xl: Dp = 12.dp
}

// ============================================================================
// 元件尺寸 (Component Sizes)
// ============================================================================

object ComponentSize {
    /** 按鈕最小高度 */
    val buttonHeight: Dp = 48.dp

    /** 小按鈕高度 */
    val buttonHeightSmall: Dp = 36.dp

    /** 輸入框高度 */
    val textFieldHeight: Dp = 56.dp

    /** 多行輸入框高度 */
    val textAreaHeight: Dp = 120.dp

    /** TopAppBar 高度 */
    val topBarHeight: Dp = 64.dp

    /** BottomBar 高度 */
    val bottomBarHeight: Dp = 80.dp

    /** FAB 尺寸 */
    val fabSize: Dp = 56.dp

    /** 小 FAB 尺寸 */
    val fabSizeSmall: Dp = 40.dp

    /** 頭像尺寸 */
    val avatarSize: Dp = 40.dp

    /** 縮圖尺寸 */
    val thumbnailSize: Dp = 80.dp

    /** 照片預覽尺寸 */
    val photoPreviewSize: Dp = 100.dp

    /** 封面照片高度 */
    val coverPhotoHeight: Dp = 150.dp

    /** 觸控最小目標尺寸 (無障礙要求) */
    val minTouchTarget: Dp = 48.dp
}

// ============================================================================
// 卡片樣式 (Card Styles)
// ============================================================================

object CardStyle {
    /** 列表卡片 - 低陰影 */
    val listCardElevation: Dp = Elevation.sm

    /** 詳情卡片 - 標準陰影 */
    val detailCardElevation: Dp = Elevation.md

    /** 對話框卡片 - 高陰影 */
    val dialogCardElevation: Dp = Elevation.lg

    /** 卡片內部間距 */
    val contentPadding: Dp = Spacing.lg

    /** 緊湊卡片內部間距 */
    val compactContentPadding: Dp = Spacing.md

    /** 卡片圓角 */
    val cornerRadius: Dp = CornerRadius.md
}

// ============================================================================
// 列表樣式 (List Styles)
// ============================================================================

object ListStyle {
    /** 列表項目間距 */
    val itemSpacing: Dp = Spacing.sm

    /** 列表內容間距 */
    val contentPadding: Dp = Spacing.lg

    /** 網格項目間距 */
    val gridSpacing: Dp = Spacing.md

    /** 分組標題上方間距 */
    val sectionHeaderTopPadding: Dp = Spacing.lg

    /** 分組標題下方間距 */
    val sectionHeaderBottomPadding: Dp = Spacing.sm
}

// ============================================================================
// Composition Local (供 Theme 使用)
// ============================================================================

data class AppDimensions(
    val spacing: Spacing = Spacing,
    val iconSize: IconSize = IconSize,
    val cornerRadius: CornerRadius = CornerRadius,
    val elevation: Elevation = Elevation,
    val componentSize: ComponentSize = ComponentSize,
    val cardStyle: CardStyle = CardStyle,
    val listStyle: ListStyle = ListStyle
)

val LocalAppDimensions = staticCompositionLocalOf { AppDimensions() }

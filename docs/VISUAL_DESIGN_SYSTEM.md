# 視覺設計系統規格

## 概述

定義 App 的視覺設計 tokens，包含色彩、漸層、尺寸等，確保整體視覺一致性。

## 色彩系統

### 主題色 (Primary - 探險橙)

| Token | 色碼 | 用途 |
|-------|------|------|
| Orange40 | #E65100 | Dark theme primary |
| Orange80 | #FFB74D | Light theme primary |
| Orange90 | #FFE0B2 | Light theme primary container |
| Orange10 | #3E2723 | Dark theme on primary |

### 完成狀態色 (Success - 綠色)

| Token | 色碼 | 用途 |
|-------|------|------|
| Green40 | #2E7D32 | Dark theme success |
| Green80 | #81C784 | Light theme success |
| Green90 | #C8E6C9 | Light theme success container |
| Green10 | #1B5E20 | Dark theme on success |

### Surface 層次

#### Light Theme

| Token | 色碼 | 說明 |
|-------|------|------|
| SurfaceLevel0 | #FFFBFE | 最底層，純白背景 |
| SurfaceLevel1 | #FFF8F5 | 第一層，輕微橙色調 |
| SurfaceLevel2 | #FFF5EE | 第二層，更明顯橙色調 |
| SurfaceLevel3 | #FFF0E5 | 第三層，最高層次 |

#### Dark Theme

| Token | 色碼 | 說明 |
|-------|------|------|
| SurfaceDarkLevel0 | #1A1A1A | 最底層，深色背景 |
| SurfaceDarkLevel1 | #242420 | 第一層，輕微暖色調 |
| SurfaceDarkLevel2 | #2D2824 | 第二層，更明顯暖色調 |
| SurfaceDarkLevel3 | #363028 | 第三層，最高層次 |

## 漸層系統 (AppGradients)

### 可用漸層

| 名稱 | 方向 | 用途 |
|------|------|------|
| primaryGradient | 水平 | 主題色強調 |
| primaryGradientVertical | 垂直 | 主題色強調 |
| successGradient | 水平 | 成功狀態 |
| successGradientVertical | 垂直 | 成功狀態 |
| timelineGradient | 垂直 | 時間軸進行中 |
| timelineCompletedGradient | 垂直 | 時間軸已完成 |
| cardOverlayGradient | 垂直 | 卡片照片遮罩 |
| warmBackgroundGradient | 垂直 | Light theme 背景 |
| darkBackgroundGradient | 垂直 | Dark theme 背景 |

### cardOverlayGradient 詳細

```kotlin
Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.3f),
        Color.Black.copy(alpha = 0.7f),
    ),
)
```

## 完成狀態色彩 (CompletedStateColors)

### Light Theme

| Token | 用途 |
|-------|------|
| lightBackground | 卡片背景 (Green90) |
| lightContent | 內容色 (Green40) |
| lightBorder | 邊框色 (Green80) |
| lightIcon | 圖標色 (Green40) |
| lightText | 文字色 (Green10) |

### Dark Theme

| Token | 用途 |
|-------|------|
| darkBackground | 卡片背景 (Green10 @ 30%) |
| darkContent | 內容色 (Green80) |
| darkBorder | 邊框色 (Green40) |
| darkIcon | 圖標色 (Green80) |
| darkText | 文字色 (Green90) |

## 時間軸尺寸 (TimelineDimensions)

| Token | 值 | 用途 |
|-------|-----|------|
| nodeSize | 16dp | 時間軸節點大小 |
| nodeSelectedSize | 20dp | 選中節點大小 |
| lineWidth | 2dp | 連接線寬度 |
| nodeSpacing | 48dp | 節點間距 |
| horizontalPadding | 16dp | 水平內距 |
| verticalPadding | 8dp | 垂直內距 |
| labelSpacing | 4dp | 標籤與節點間距 |

## 整合位置

### ItemCard.kt

- `CompletedStateColors` - 區分已完成/未完成項目
- `AppGradients.cardOverlayGradient` - 封面照片遮罩
- `TimelineDimensions` - 時間軸尺寸

### ItineraryDetailScreen.kt

- `warmBackgroundGradient` / `darkBackgroundGradient` - 背景漸層

## 相關檔案

| 檔案 | 說明 |
|------|------|
| `presentation/theme/Color.kt` | 色彩定義 |
| `presentation/theme/Dimensions.kt` | 尺寸定義 |
| `presentation/components/ItemCard.kt` | 使用位置 |
| `presentation/itinerary_detail/ItineraryDetailScreen.kt` | 使用位置 |

## 完成日期

2026-01-30

# TimelineNavigator 水平導覽元件規格

## 概述

TimelineNavigator 是一個水平時間軸導覽元件，用於在 ItineraryDetailScreen 頂部提供日期快速切換功能。

## 功能

- 水平可滑動的日期節點 (LazyRow)
- 點擊節點跳轉到該日期
- 當前日期高亮顯示（帶動畫效果）
- 顯示每日主要地點名稱

## 視覺設計

```
┌────────────────────────────────────────────────────────────────┐
│ Day 1 ──●── Day 2 ──●── Day 3 ──●── Day 4 ──●── Day 5        │
│          淺草寺      新宿御苑     秋葉原      澀谷              │
└────────────────────────────────────────────────────────────────┘
```

### 節點狀態

| 狀態 | 節點大小 | 顏色 |
|------|---------|------|
| 未選中 | 16dp | surfaceVariant |
| 選中 | 20dp | primary (動畫過渡) |

## API

### TimelineNavigator

```kotlin
@Composable
fun TimelineNavigator(
    dateRange: ClosedRange<LocalDate>,
    selectedDate: LocalDate?,
    groupedItems: List<ItemsByDate>,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
)
```

#### 參數

| 參數 | 類型 | 說明 |
|------|------|------|
| dateRange | ClosedRange<LocalDate> | 行程日期範圍 |
| selectedDate | LocalDate? | 當前選中的日期 |
| groupedItems | List<ItemsByDate> | 按日期分組的項目，用於提取地點名稱 |
| onDateSelected | (LocalDate?) -> Unit | 日期選擇回調 |
| modifier | Modifier | Compose Modifier |

### TimelineNode (Data Class)

```kotlin
data class TimelineNode(
    val date: LocalDate,
    val dayNumber: Int,
    val locationName: String?
)
```

## 相關檔案

| 檔案 | 說明 |
|------|------|
| `presentation/components/TimelineNavigator.kt` | 元件實作 |
| `presentation/itinerary_detail/ItineraryDetailScreen.kt` | 使用位置 |
| `presentation/theme/Dimensions.kt` | TimelineDimensions 尺寸定義 |

## 使用的設計 Tokens

- `TimelineDimensions.nodeSize` - 節點大小 (16dp)
- `TimelineDimensions.nodeSelectedSize` - 選中節點大小 (20dp)
- `TimelineDimensions.lineWidth` - 連接線寬度 (2dp)
- `TimelineDimensions.nodeSpacing` - 節點間距 (48dp)
- `TimelineDimensions.labelSpacing` - 標籤間距 (4dp)

## 完成日期

2026-01-30

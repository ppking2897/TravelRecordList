# Design Document - 行程 UI 增強

## 概述

本設計文件描述行程 UI 增強功能的技術實作方案，主要包括：
1. Itinerary model 增加日期範圍欄位
2. 日期 tabs 水平滾動 UI component
3. 項目按日期分組和篩選邏輯
4. FAB button 整合
5. Dropdown menu 日期選擇器

## 架構

### Component 層級結構

```
ItineraryListScreen
├── FAB (新增行程)
└── ItineraryList

ItineraryDetailScreen
├── DateTabsRow (水平滾動)
├── ItemsListByDate (分組顯示)
└── FAB (新增項目)

AddEditItineraryScreen
├── DateRangePicker (開始/結束日期)
└── Save Button

AddEditItemScreen
├── DateDropdown (從行程日期範圍選擇)
├── TimeInput
├── LocationInput
└── Save Button
```

### Data Flow

```
ViewModel → State → UI
    ↓
Repository → Storage
```

## Components and Interfaces

### 1. 更新 Itinerary Model

```kotlin
@Serializable
data class Itinerary(
    val id: String,
    val title: String,
    val description: String = "",
    val startDate: LocalDate?,  // 新增：開始日期
    val endDate: LocalDate?,    // 新增：結束日期
    @Contextual val createdAt: Instant,
    @Contextual val modifiedAt: Instant
)
```

### 2. DateTabsRow Component

```kotlin
@Composable
fun DateTabsRow(
    dateRange: ClosedRange<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
)
```

**功能：**
- 水平滾動顯示日期 tabs
- 支援「全部」tab 和個別日期 tabs
- 高亮選中的 tab

### 3. ItemCard Component

```kotlin
@Composable
fun ItemCard(
    item: ItineraryItem,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

**顯示內容：**
- 活動名稱
- 地點
- 時間
- 完成狀態 checkbox
- 刪除按鈕

### 4. DateDropdown Component

```kotlin
@Composable
fun DateDropdown(
    dateRange: ClosedRange<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
)
```

**功能：**
- 顯示日期範圍內的所有日期
- 格式化顯示（例如："2024-01-15 (週一)"）
- 支援搜尋/篩選

## Data Models

### DateRange Extension

```kotlin
fun ClosedRange<LocalDate>.toDateList(): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var current = start
    while (current <= endInclusive) {
        dates.add(current)
        current = current.plus(1, DateTimeUnit.DAY)
    }
    return dates
}
```

### Grouped Items

```kotlin
data class ItemsByDate(
    val date: LocalDate,
    val items: List<ItineraryItem>
)

fun List<ItineraryItem>.groupByDate(): List<ItemsByDate> {
    return this.groupBy { it.date }
        .map { (date, items) ->
            ItemsByDate(
                date = date,
                items = items.sortedWith(
                    compareBy<ItineraryItem> { it.time ?: LocalTime.MAX }
                )
            )
        }
        .sortedBy { it.date }
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. 
Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: 日期範圍驗證
*For any* Itinerary with startDate and endDate, if endDate is before startDate, then validation should fail
**Validates: 需求 1.3**

### Property 2: 日期範圍生成完整性
*For any* valid date range (start to end), the generated date list should contain all dates from start to end inclusive with no gaps
**Validates: 需求 2.2, 5.2**

### Property 3: 日期篩選正確性
*For any* list of items and selected date, filtering by that date should return only items with matching date
**Validates: 需求 2.3, 6.1**

### Property 4: 項目分組保持順序
*For any* list of items, grouping by date should preserve the time-based ordering within each date group
**Validates: 需求 3.3, 3.4**

### Property 5: 日期格式化一致性
*For any* LocalDate, the formatted string should always include the date and day of week in consistent format
**Validates: 需求 5.5**

### Property 6: 空狀態轉換
*For any* itinerary, when all items are removed, the UI state should transition to empty state
**Validates: 需求 7.5**

## Error Handling

### Validation Errors

1. **日期範圍無效**
   - 錯誤：結束日期早於開始日期
   - 處理：顯示錯誤訊息，阻止儲存

2. **缺少必填日期**
   - 錯誤：新增項目時未選擇日期
   - 處理：顯示錯誤訊息，高亮欄位

3. **日期超出範圍**
   - 錯誤：選擇的日期不在行程日期範圍內
   - 處理：自動調整或顯示警告

### UI Error States

1. **載入失敗**
   - 顯示錯誤訊息和重試按鈕

2. **空狀態**
   - 顯示友善的空狀態圖示和提示訊息

3. **網路錯誤**
   - 顯示離線模式提示

## Testing Strategy

### Unit Tests

1. **日期範圍驗證測試**
   - 測試有效和無效的日期範圍組合
   - 測試邊界情況（同一天、跨年等）

2. **日期列表生成測試**
   - 測試單日範圍
   - 測試多日範圍
   - 測試跨月、跨年範圍

3. **項目分組測試**
   - 測試空列表
   - 測試單一日期
   - 測試多個日期
   - 測試有/無時間的項目混合

4. **篩選邏輯測試**
   - 測試按日期篩選
   - 測試「全部」篩選
   - 測試空結果

### Property-Based Tests

使用 **Kotest Property Testing** 框架進行 property-based testing。

每個 property test 應該：
- 運行至少 100 次迭代
- 使用隨機生成的測試資料
- 明確標註對應的 correctness property

### Integration Tests

1. **完整流程測試**
   - 建立行程 → 新增項目 → 篩選顯示 → 完成項目

2. **UI 互動測試**
   - 點擊 FAB → 填寫表單 → 儲存
   - 點擊日期 tab → 驗證篩選結果

### UI Tests

1. **Component 渲染測試**
   - DateTabsRow 正確顯示
   - ItemCard 正確顯示
   - FAB button 正確定位

2. **互動測試**
   - Tab 選擇
   - Dropdown 選擇
   - FAB 點擊

## Implementation Notes

### Performance Considerations

1. **Lazy Loading**
   - 使用 `LazyColumn` 顯示項目列表
   - 使用 `LazyRow` 顯示日期 tabs

2. **State Management**
   - 使用 `remember` 快取分組資料
   - 使用 `derivedStateOf` 計算篩選結果

3. **Recomposition Optimization**
   - 使用 `key` 優化列表項目
   - 避免不必要的 lambda 重建

### Accessibility

1. **語義標籤**
   - 為所有互動元件添加 `contentDescription`
   - 使用 `semantics` modifier 提供額外資訊

2. **鍵盤導航**
   - 支援 tab 鍵導航
   - 支援方向鍵選擇

3. **對比度**
   - 確保文字和背景有足夠對比度
   - 選中狀態有明顯視覺差異

### Platform-Specific Considerations

1. **Android**
   - 使用 Material 3 design system
   - 支援返回鍵導航

2. **iOS**
   - 使用 Cupertino-style date picker
   - 支援手勢導航

3. **Web**
   - 響應式佈局
   - 支援滑鼠和觸控操作

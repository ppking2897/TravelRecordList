# 拖曳排序優化規格

## 目標

優化現有的拖曳排序功能，提升使用體驗。

## 需修改的檔案

```
composeApp/src/commonMain/kotlin/com/example/myapplication/presentation/itinerary_detail/ItineraryDetailScreen.kt
```

## 優化項目

### 1. 動態計算 item 高度

**現況**：使用固定值 `120f` 計算拖曳目標位置

```kotlin
// 目前的程式碼（約第 450 行附近）
val itemHeight = 120f // 預估 item 高度
```

**改進**：使用 `onGloballyPositioned` 取得實際高度

```kotlin
// 追蹤每個 item 的高度
var itemHeights by remember { mutableStateOf(mapOf<String, Int>()) }

// 在 ItemCard 外層加入
Modifier.onGloballyPositioned { coordinates ->
    itemHeights = itemHeights + (item.id to coordinates.size.height)
}

// 計算目標位置時使用實際高度
val averageHeight = itemHeights.values.average().toFloat()
```

### 2. 其他 item 讓位動畫

**現況**：拖曳時其他 item 不會移動

**改進**：加入 `animateItemPlacement`

```kotlin
LazyColumn {
    itemsIndexed(
        items = group.items,
        key = { _, item -> item.id }
    ) { index, item ->
        Box(
            modifier = Modifier.animateItemPlacement() // 加入這行
        ) {
            // ...
        }
    }
}
```

### 3. 觸覺回饋（可選）

**目標**：長按開始拖曳時震動提示

```kotlin
// 在 onDragStart 中
val haptic = LocalHapticFeedback.current
haptic.performHapticFeedback(HapticFeedbackType.LongPress)
```

需要加入 import：
```kotlin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
```

## 驗證步驟

1. 執行編譯：`./gradlew :composeApp:compileDebugKotlinAndroid`
2. 確認無錯誤
3. Git commit：`feat: 優化拖曳排序體驗`

## 注意事項

- `animateItemPlacement` 需要 item 有 key
- 觸覺回饋在 Desktop 平台可能不支援，需要加 expect/actual 或 try-catch

# ItineraryItem Time Field Migration - 完成報告

## 變更說明

將 `ItineraryItem` 的單一 `time` 欄位改為 `arrivalTime` 和 `departureTime` 兩個欄位，以支援更精確的時間管理。

## 資料模型變更

```kotlin
// 舊版
data class ItineraryItem(
    val time: LocalTime? = null,
    // ...
)

// 新版
data class ItineraryItem(
    val arrivalTime: LocalTime? = null,
    val departureTime: LocalTime? = null,
    // ...
) {
    /**
     * 取得主要時間（用於排序和顯示）
     * 優先使用到達時間，如果沒有則使用離開時間
     */
    fun primaryTime(): LocalTime? = arrivalTime ?: departureTime
    
    /**
     * 計算停留時間
     * 如果有到達和離開時間，計算兩者之間的時間差
     */
    fun stayDuration(): kotlin.time.Duration? {
        return if (arrivalTime != null && departureTime != null) {
            val arrivalSeconds = arrivalTime.toSecondOfDay()
            val departureSeconds = departureTime.toSecondOfDay()
            kotlin.time.Duration.Companion.parse("PT${departureSeconds - arrivalSeconds}S")
        } else {
            null
        }
    }
}
```

## ✅ 遷移完成

### 1. Data Model 更新
- ✅ ItineraryItem.kt - 資料模型更新完成
  - 新增 `arrivalTime` 和 `departureTime` 欄位
  - 新增 `primaryTime()` 輔助函數
  - 新增 `stayDuration()` 函數

### 2. Use Cases 更新
- ✅ AddItineraryItemUseCase.kt - 參數更新為 `arrivalTime` 和 `departureTime`
- ✅ UpdateItineraryItemUseCase.kt - 新增時間邏輯驗證
- ✅ GetTravelHistoryUseCase.kt - 排序改用 `primaryTime()`
- ✅ GroupItemsByDateUseCase.kt - 排序改用 `primaryTime()`

### 3. Repositories 更新
- ✅ ItineraryItemRepositoryImpl.kt - 所有排序邏輯改用 `primaryTime()`
- ✅ RouteRepositoryImpl.kt - 路線生成時的排序改用 `primaryTime()`

### 4. ViewModels 更新
- ✅ AddEditItemViewModel.kt (MVI) - 使用 `arrivalTime`
- ✅ EditItemViewModel.kt (MVI) - 使用 `arrivalTime` 並保留 `departureTime`
- ✅ TravelHistoryViewModel.kt (MVI) - 排序改用 `primaryTime()`
- ✅ TravelHistoryViewModel.kt (舊版) - 排序改用 `primaryTime()`

### 5. UI Screens 更新
- ✅ ItemCard.kt - 顯示改用 `primaryTime()`
- ✅ EditItemScreen.kt - 使用 `arrivalTime` 並保留 `departureTime`
- ✅ AddEditItemScreen.kt - 使用 `arrivalTime` 參數
- ✅ ItineraryDetailScreen.kt - Preview 資料更新
- ✅ TravelHistoryScreen.kt - 顯示改用 `primaryTime()`，Preview 資料更新

### 6. 測試更新
- ✅ GroupItemsByDateUseCasePropertyTest.kt - 更新測試資料生成器
- ✅ BaseViewModelTest.kt - 修復協程測試
- ✅ DateExtensionsPropertyTest.kt - 修復 import

## 編譯與測試結果

### 編譯成功 ✅
```bash
.\gradlew.bat :composeApp:compileDebugKotlinAndroid
# BUILD SUCCESSFUL
```

### 測試通過 ✅
```bash
.\gradlew.bat :composeApp:test
# BUILD SUCCESSFUL
# 所有測試通過
```

## 技術細節

### primaryTime() 函數
```kotlin
fun primaryTime(): LocalTime? = arrivalTime ?: departureTime
```
- 優先返回到達時間
- 如果沒有到達時間，返回離開時間
- 用於排序和顯示

### stayDuration() 函數
```kotlin
fun stayDuration(): kotlin.time.Duration? {
    return if (arrivalTime != null && departureTime != null) {
        val arrivalSeconds = arrivalTime.toSecondOfDay()
        val departureSeconds = departureTime.toSecondOfDay()
        kotlin.time.Duration.Companion.parse("PT${departureSeconds - arrivalSeconds}S")
    } else {
        null
    }
}
```
- 計算到達和離開之間的時間差
- 僅在兩個時間都存在時返回值

### 時間驗證
在 `UpdateItineraryItemUseCase` 中新增驗證：
```kotlin
if (item.arrivalTime != null && item.departureTime != null && 
    item.departureTime < item.arrivalTime) {
    return Result.failure(Exception("Departure time must be after arrival time"))
}
```

## 遷移規則

1. **讀取舊資料**: `item.time` → `item.primaryTime()` 或 `item.arrivalTime`
2. **排序**: 使用 `item.primaryTime()` 進行排序
3. **顯示**: 使用 `item.primaryTime()` 顯示時間
4. **新增/編輯**: 目前僅支援編輯到達時間，離開時間設為 null

## 向後相容性

- ✅ 所有 `time` 欄位引用已完全移除
- ✅ 所有排序邏輯已更新為使用 `primaryTime()`
- ✅ UI 目前僅支援編輯到達時間（未來可擴展）
- ⚠️ 如果有舊版本的持久化資料，需要手動遷移

## 未來改進建議

1. **UI 增強**
   - 支援同時編輯到達和離開時間
   - 顯示停留時間（使用 `stayDuration()`）
   - 時間範圍選擇器

2. **資料遷移**
   - 如果需要從舊版本升級，建立資料遷移工具
   - 將舊的 `time` 值自動遷移到 `arrivalTime`

3. **驗證增強**
   - 更多時間邏輯驗證
   - 跨日期的時間處理

## 完成日期
2024-11-28

## 總結
✅ Time Field Migration 已成功完成！所有相關檔案已更新，編譯和測試都通過。系統現在支援更精確的時間管理，為未來的功能擴展奠定了基礎。

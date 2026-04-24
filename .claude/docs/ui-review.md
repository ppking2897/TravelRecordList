# TravelApp UI 審查報告

> 審查日期：2026-04-09
> 審查範圍：所有 Screen.kt、Components、Theme 檔案

---

## 一、整體評價

| 面向 | 評分 | 說明 |
|------|------|------|
| Theme 系統完整度 | A | Color、Dimensions、Typography 架構完善，有完整的 Design Token 系統 |
| 間距一致性 | B- | 部分畫面使用 Spacing 常數，部分硬寫 `.dp` |
| 顏色一致性 | A | Screen 中未發現硬寫 `Color(0xFF...)`，全部走 MaterialTheme |
| 元件重複度 | C | AddEditItemScreen 與 EditItemScreen 大量重複 |
| 狀態處理 | B | 大部分有 Loading/Error/Empty，但處理方式不統一 |
| 無障礙 | C+ | 48 處 `contentDescription = null`，裝飾性圖標可接受但仍有改善空間 |
| Typography | A | 未發現硬寫 `fontSize`，全部使用 `MaterialTheme.typography` |

---

## 二、嚴重問題（High）

### H-1: AddEditItemScreen 與 EditItemScreen 程式碼高度重複

**影響**：維護成本加倍，修改一處忘記同步另一處
**檔案**：
- `feature/itinerary_item/presentation/AddEditItemScreen.kt`
- `feature/itinerary_item/presentation/EditItemScreen.kt`

兩個檔案的 `ScreenContent` 有超過 90% 的相同程式碼（表單欄位、照片區段、時間選擇器、底部按鈕），僅差異在標題文字「新增項目」vs「編輯項目」及 Intent 類型不同。

**建議**：抽出共用的 `ItemFormContent` composable，以參數區分 add/edit 模式。同時 `AddEditItemContract.kt` 和 `EditItemContract.kt` 的 State 也幾乎相同，可考慮合併。

### H-2: ItineraryAddEditScreen 內有兩份幾乎相同的 Scaffold

**影響**：同一個檔案中存在兩個 Scaffold（一個在 `ItineraryAddEditScreen` 函數，一個在 `ItineraryAddEditScreenContent`），都包含完整表單，但 `ItineraryAddEditScreenContent` 少了封面照片功能。
**檔案**：`feature/itinerary_add_edit/presentation/ItineraryAddEditScreen.kt`

實際使用的是 `ItineraryAddEditScreen`（非 ContentView），`ItineraryAddEditScreenContent` 只用於 Preview，但內含完整的重複佈局，造成維護混亂。

**建議**：將 `ItineraryAddEditScreen` 的表單抽出為真正的 ContentView，Preview 使用同一個 ContentView。

### H-3: 大量硬寫 `.dp` 值未使用 Design Token

**影響**：間距不一致，違反設計系統
**檔案與統計**：

| 檔案 | 硬寫 dp 數量 | 主要問題 |
|------|-------------|----------|
| `RouteViewScreen.kt` | ~25 處 | `16.dp`, `12.dp`, `8.dp`, `4.dp`, `40.dp`, `64.dp` 等全部硬寫 |
| `AddEditItemScreen.kt` | ~15 處 | `16.dp`, `8.dp`, `100.dp`, `120.dp`, `24.dp` 等 |
| `EditItemScreen.kt` | ~15 處 | 與 AddEditItemScreen 相同問題 |
| `ItineraryAddEditScreen.kt` | ~8 處 | `16.dp`, `120.dp`, `24.dp` |
| `ItineraryMapScreen.kt` | ~10 處 | `48.dp`, `16.dp`, `32.dp`, `8.dp` |
| `TravelHistoryScreen.kt` | ~8 處 | `14.dp`, `16.dp`, `48.dp`, `4.dp` |

**對比**：`ItineraryListScreen.kt` 和 `ItineraryDetailScreen.kt` 正確使用了 `Spacing.lg`、`IconSize.xxl` 等 Design Token。

**建議**：統一替換為 `Spacing.*`、`IconSize.*`、`CornerRadius.*`、`ComponentSize.*` 等已定義的 Token。

---

## 三、中等問題（Medium）

### M-1: 狀態處理方式不統一

已有 `CommonComponents.kt` 提供 `LoadingOverlay`、`LoadingContent`、`ErrorContent`、`EmptyStateEnhanced` 等共用元件，但大多數畫面自行實作 Loading/Error/Empty：

| 畫面 | Loading | Error | Empty | 使用共用元件？ |
|------|---------|-------|-------|---------------|
| ItineraryListScreen | 無明確處理 | inline Text | 自定義 EmptyStateGrid | 否 |
| ItineraryDetailScreen | 自訂 CircularProgressIndicator | inline Text | 使用 EmptyState | 部分 |
| ItineraryAddEditScreen | Button 內 spinner | inline Text | N/A | 否 |
| AddEditItemScreen | Button 內 spinner | inline Text | N/A | 否 |
| TravelHistoryScreen | 自訂 CircularProgressIndicator | inline Text | 自定義 EmptyHistoryState | 否 |
| RouteViewScreen | 自訂 CircularProgressIndicator | 自定義空狀態 | 包含在錯誤處理中 | 否 |
| ItineraryMapScreen | 自訂 CircularProgressIndicator | 自定義錯誤 | 自定義無標記狀態 | 否 |

**建議**：統一使用 `LoadingContent`、`ErrorContent`、`EmptyStateEnhanced`，只在必要時自定義。

### M-2: ItineraryListScreen 缺少 Loading 狀態顯示

**檔案**：`feature/itinerary_list/presentation/ItineraryListScreen.kt`
**問題**：State 有 `isLoading` 欄位，但 ScreenContent 中沒有任何 Loading UI（沒有 CircularProgressIndicator 也沒有使用 LoadingOverlay）。初次載入時畫面會是空白或直接顯示空狀態。

**建議**：在 `itineraries.isEmpty() && !isLoading` 判斷前，先處理 `isLoading` 狀態。

### M-3: 照片刪除按鈕觸控區域過小（24.dp）

**檔案**：`AddEditItemScreen.kt` (第 263 行)、`EditItemScreen.kt` (第 268 行)
**問題**：照片右上角的刪除 IconButton 只有 `24.dp`，低於 Material3 要求的最小觸控目標 48dp（App 的 `ComponentSize.minTouchTarget` 也定義為 48.dp）。

**建議**：使用 `Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` 包裹，或使用 `minimumInteractiveComponentSize()`。

### M-4: EmptyState 元件硬寫 dp 值

**檔案**：`core/presentation/components/EmptyState.kt`
**問題**：使用 `32.dp`、`64.dp`、`16.dp` 硬寫值，但同檔案的 `EmptyStateEnhanced`（在 CommonComponents.kt）正確使用了 Design Token。
**建議**：改用 `Spacing.xxl`、`IconSize.xxl`、`Spacing.lg`。

### M-5: Preview 未使用 TravelRecordTheme

**檔案**：
- `AddEditItemScreen.kt` Preview 使用 `MaterialTheme { ... }`
- `EditItemScreen.kt` Preview 使用 `MaterialTheme { ... }`
- `RouteViewScreen.kt` 所有 Preview 使用 `MaterialTheme { ... }`
- `ItineraryAddEditScreen.kt` Preview 使用 `MaterialTheme { ... }`

**對比**：`ItineraryListScreen.kt` 和 `ItineraryDetailScreen.kt` 正確使用 `TravelRecordTheme { ... }`

**建議**：統一使用 `TravelRecordTheme`，確保 Preview 能正確反映自定義主題配色。

---

## 四、輕微問題（Low）

### L-1: contentDescription = null 數量偏多

全專案共有 48 處 `contentDescription = null`。大部分是裝飾性圖標（如列表項旁的 Place icon、Divider 旁的 icon），可接受但建議逐步補上有意義的描述，特別是：
- 操作類圖標（刪除、編輯、篩選）中的 leadingIcon
- 時間圖標（Schedule icon）
- 照片圖標（Photo icon）

### L-2: RoundedCornerShape 硬寫

**檔案**：
- `AddEditItemScreen.kt` / `EditItemScreen.kt`：`RoundedCornerShape(8.dp)`、`RoundedCornerShape(50)`
- `TravelHistoryScreen.kt`：`RoundedCornerShape(4.dp)`
- `RouteViewScreen.kt`：無（使用 `MaterialTheme.shapes`）

**建議**：改用 `RoundedCornerShape(CornerRadius.sm)` 或 `MaterialTheme.shapes.small`。

### L-3: formatTime 函數重複定義

`formatTime` 在以下三個檔案中各自定義了一次：
- `ItemCard.kt` (第 521 行)
- `TravelHistoryScreen.kt` (第 736 行)
- `ItineraryListScreen.kt` 中的 `formatCreatedDate`、`toDisplayString` 也有類似邏輯

**建議**：抽到 `core/presentation/util/DateTimeFormatter.kt` 共用。

### L-4: ItineraryDetailScreen 使用 wildcard import

**檔案**：`ItineraryDetailScreen.kt`
```kotlin
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
```

**建議**：改為具名 import 以避免衝突和提升可讀性。

### L-5: TravelHistoryScreen 使用 AlertDialog 做日期範圍篩選

**檔案**：`TravelHistoryScreen.kt`
**問題**：用 AlertDialog 包含兩個 TextButton 做日期範圍選擇，使用者需要分兩步選擇開始和結束日期，且無法直覺地看到日期範圍。

**建議**：考慮使用 Material3 的 `DateRangePicker` 提供更好的體驗。

---

## 五、Theme 系統評價

### 優勢
- Color Token 完整：Primary/Secondary/Tertiary/Error/Surface 全定義
- 有 Light/Dark 兩套配色
- Dimensions 系統全面：Spacing、IconSize、CornerRadius、Elevation、ComponentSize、CardStyle、ListStyle、TimelineDimensions
- 有 CompositionLocal (`LocalAppDimensions`) 可在 Theme 中提供
- 有漸層色系統 (AppGradients)
- 有完成狀態專用色彩 (CompletedStateColors)

### 待改善
- `LocalAppDimensions` 在 Theme.kt 中有 provide，但所有 Screen 都直接引用 `Spacing`、`IconSize` 等 object，未透過 `LocalAppDimensions.current` 使用（影響不大，因為值是固定的）
- Theme.kt 未定義自訂 Typography（使用 Material3 預設）
- Theme.kt 未定義自訂 Shapes（使用 Material3 預設）

---

## 六、摘要

| 優先級 | 問題數 | 關鍵項目 |
|--------|--------|----------|
| High | 3 | 元件重複（H-1、H-2）、硬寫 dp（H-3） |
| Medium | 5 | 狀態處理不統一（M-1）、缺 Loading（M-2）、觸控區域（M-3）、EmptyState dp（M-4）、Preview Theme（M-5） |
| Low | 5 | contentDescription（L-1）、RoundedCornerShape（L-2）、formatTime 重複（L-3）、wildcard import（L-4）、DateRangePicker（L-5） |

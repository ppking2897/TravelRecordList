# TravelApp UI/UX 問題修復計畫

> 建立日期：2026-04-24
> 範圍：`ui-review.md` + `ux-improvements.md` 中尚未完成的項目
> 目標：消除技術債、補完流程 UX、提升品質感

---

## 一、既有問題盤點（已驗證現況）

### 1.1 已完成的項目（不再處理）

| 項目 | 現況 |
|------|------|
| P0-1 刪除行程 / 產生路線空 lambda | ItineraryDetail 已重構，已消失 ✅ |
| P0-2 RouteView 匯出只有 println | 已改為 `shareLauncher.share(...)` ✅ |
| P0-3 表單未儲存離開無確認 | 有 `isDirty` + `DiscardDialog` ✅ |
| P1-1 新增行程改為 FAB | 已用 `FloatingActionButton` ✅ |
| P1-2 操作回饋 Snackbar | 10 個 Screen 使用 `LocalSnackbarHostState` ✅ |
| P2-1 Pull-to-refresh | 已完成 ✅ |
| H-3 RouteView 硬寫 dp | 從 ~25 處降到 7 處，大幅改善 ✅ |
| L-2 `RoundedCornerShape(8.dp)` / `(50)` / `(4.dp)` | Screen 內已無 ✅ |

### 1.2 本次處理範圍（仍存在的問題）

**技術債（ui-review.md）：**
| 編號 | 問題 | 驗證結果 |
|------|------|----------|
| H-1 | AddEditItemScreen (452行) vs EditItemScreen (390行) 90% 重複 | 仍存在 |
| H-2 | ItineraryAddEditScreen 兩份 Scaffold | 仍存在 |
| M-1 | Loading/Error/Empty 處理不統一 | 仍存在 |
| M-2 | ItineraryListScreen 無 Loading UI | 仍存在 |
| M-3 | 照片刪除按鈕觸控區 24dp（< 48dp Material 最小值） | 4 處仍存在 |
| M-5 | Preview 使用 MaterialTheme 而非 TravelRecordTheme | 部分 Screen 仍存在 |
| L-1 | 48 處 `contentDescription = null` | 待逐步補 |
| L-3 | `formatTime` 在 3 個檔案重複定義 | 仍存在 |
| L-4 | wildcard import | 仍存在 |
| L-5 | TravelHistory 日期篩選用 AlertDialog | 仍存在 |

**UX 流程（ux-improvements.md + memory 筆記）：**
| 編號 | 問題 |
|------|------|
| P2-2 | ItineraryDetail 同日項目拖拽排序 |
| P2-3 | ItineraryMap 按日期上色 + 同日虛線連接 |
| P2-4 | 搜尋只能文字（缺日期/地點/狀態篩選） |
| P2-5 | 旅遊歷史無統計摘要 |
| P3-1 | 空狀態使用 Icon 而非插圖 |
| P3-2 | 深色模式判斷散落在 Screen |
| P3-3 | 無左滑刪除 / 右滑編輯 |
| P3-4 | 載入動畫只有 CircularProgressIndicator |

**延伸 UX（PM 視角補充）：**
| 編號 | 問題 |
|------|------|
| U1 | 新增項目需填 8 欄位，無快速模式 |
| U2 | 行程未設日期範圍時項目日期 disabled 無引導 |
| U3 | 行程列表卡片只顯示標題 / 日期 / 建立時間 |
| U4 | TimelineNavigator 日期節點無項目數 badge |
| U5 | TravelHistory 日期篩選用 AlertDialog 不直覺 |
| U6 | 行程搜尋只能文字 |

---

## 二、處理順序（三階段）

### Phase 1 — 技術債消除（本週，高 ROI / 低風險）

目標：重構重複程式碼、補齊基礎元件使用，消除雙改風險。

| # | 任務 | 涵蓋問題 | 規模 |
|---|------|---------|------|
| 1.1 | 抽 `ItemFormContent` composable + 合併 Contract | H-1 | 中（2 天） |
| 1.2 | 修照片刪除觸控區 24→48dp | M-3 | 小（0.5 天） |
| 1.3 | 修 ItineraryAddEditScreen 雙 Scaffold | H-2 | 小（0.5 天） |
| 1.4 | ItineraryListScreen 補 Loading UI（Shimmer）| M-2、P3-4 部分 | 小（0.5 天） |
| 1.5 | 統一使用 `LoadingContent` / `ErrorContent` / `EmptyStateEnhanced` | M-1 | 中（1 天） |
| 1.6 | Preview 統一 `TravelRecordTheme` | M-5 | 小（0.5 天） |
| 1.7 | `formatTime` 抽共用工具到 `core/presentation/util/DateTimeFormatter.kt` | L-3 | 小（0.3 天） |
| 1.8 | 移除 wildcard import | L-4 | 小（0.2 天） |

**完成條件**：
- `./gradlew compileDebugKotlin` 無錯
- `/arch-check` 通過
- 所有 Screen 的 Preview 使用 `TravelRecordTheme`
- AddEdit 和 Edit 兩個 Screen 的欄位邏輯走同一 `ItemFormContent`

**預計時程**：5-6 天

---

### Phase 2 — UX 流程補完（下個迭代）

目標：提升操作流暢度、補齊使用者顯著能感知到的流程問題。

| # | 任務 | 涵蓋問題 | 規模 |
|---|------|---------|------|
| 2.1 | ItineraryDetail 同日拖拽排序（LazyColumn reorder） | P2-2 | 中-高（3 天） |
| 2.2 | ItineraryMap 按日期上色 + 同日虛線連接 | P2-3 | 中（2 天） |
| 2.3 | 快速新增項目模式（活動 + 地點即可存檔） | U1 | 小-中（1.5 天） |
| 2.4 | 日期欄位 disabled 時加「前往設定日期範圍」引導按鈕 | U2、P1-4 | 小（0.5 天） |
| 2.5 | 行程列表卡片擴充（項目數 / 完成進度 / 主要地點） | U3、P1-5 | 小-中（1 天） |
| 2.6 | TimelineNavigator 日期節點加項目數 badge | U4、P2-1 | 小（0.5 天） |
| 2.7 | TravelHistory 改用 Material3 `DateRangePicker` | U5、L-5 | 小（0.5 天） |

**預計時程**：8-9 天

---

### Phase 3 — 品質感打磨（再下個迭代）

目標：視覺與互動細節精煉。

| # | 任務 | 涵蓋問題 | 規模 |
|---|------|---------|------|
| 3.1 | 空狀態插圖（Composable 繪製或 SVG 資源） | P3-1 | 中（2 天） |
| 3.2 | 深色模式判斷收到 Theme 層（CompositionLocal） | P3-2 | 小-中（1 天） |
| 3.3 | 行程卡片 / 項目左滑刪除、右滑編輯 | P3-3 | 中（2 天） |
| 3.4 | Shimmer/Skeleton 擴展到各畫面 | P3-4 | 中（1.5 天） |
| 3.5 | 旅遊歷史統計摘要卡片（去過 X 地點、Y 次旅行） | P2-5 | 小-中（1 天） |
| 3.6 | 搜尋進階篩選（日期 / 地點 / 狀態） | P2-4、U6 | 中（2 天） |
| 3.7 | `contentDescription` 補齊（48 處逐步） | L-1 | 小-中，隨任務補 |

**預計時程**：9-10 天

---

## 三、Phase 1 詳細規劃（立即執行）

### 1.1 抽 `ItemFormContent` composable

**現況分析**：

| 檔案 | 行數 | 內容 |
|------|------|------|
| `AddEditItemScreen.kt` | 452 | Scaffold、TopAppBar、表單、照片、時間選擇器 |
| `EditItemScreen.kt` | 390 | 幾乎同上，只差 title 和 Intent 類型 |

**重構方案**：

```
feature/itinerary_item/presentation/
├── ItemFormContent.kt           # 新增：共用表單 composable（活動、地點、日期、時間、照片、備註）
├── AddEditItemScreen.kt         # 簡化：Scaffold + ItemFormContent（mode = Add）
├── EditItemScreen.kt            # 簡化：Scaffold + ItemFormContent（mode = Edit）
├── AddEditItemContract.kt       # 保留
└── EditItemContract.kt          # 保留
```

**`ItemFormContent` 參數設計**：
```kotlin
@Composable
fun ItemFormContent(
    activity: String,
    location: Location?,
    date: LocalDate?,
    arrivalTime: LocalTime?,
    departureTime: LocalTime?,
    photos: List<Photo>,
    notes: String,
    hashtags: List<String>,
    availableDates: List<LocalDate>,
    onActivityChange: (String) -> Unit,
    onLocationChange: (Location?) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onArrivalTimeChange: (LocalTime?) -> Unit,
    onDepartureTimeChange: (LocalTime?) -> Unit,
    onAddPhoto: (Photo) -> Unit,
    onDeletePhoto: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onNavigateToEditItinerary: (() -> Unit)? = null,  // 僅 Add 模式用
    modifier: Modifier = Modifier
)
```

**Contract 合併考量**：
- AddEditItemState 和 EditItemState 欄位大部分重疊，可抽 `ItemFormState` 當共用子 State
- Intent 不合併（Add 有 `SaveNewItem`、Edit 有 `UpdateItem`，語意不同）
- 短期：先抽 Composable、State 保留獨立
- 長期（Phase 3+）：再看是否值得合併 Contract

**風險**：
- `AddEditItemScreen` 有 `defaultDate` 參數（QuickAddItem 用），`EditItemScreen` 沒有
- `AddEditItemScreen` 有 `onNavigateToEditItinerary`，`EditItemScreen` 沒有
- → 透過參數可選處理

**驗證**：
- 編譯通過
- Preview 正常顯示
- 手動測試：新增項目 / QuickAdd 項目 / 編輯項目 三個流程都正常

### 1.2 照片刪除觸控區 24→48dp

**位置**：
- `AddEditItemScreen.kt:331`、`:380`
- `EditItemScreen.kt:270`、`:319`

**修改方式**：
```kotlin
// Before:
modifier = Modifier.size(24.dp)

// After:
// IconButton 本身已有 48dp 觸控區（Material3 minimumInteractiveComponentSize）
// 將 24dp 改為 Icon 內部 size
```

由於抽完 `ItemFormContent` 後只剩一處，這題會順手完成。

### 1.3 `ItineraryAddEditScreen` 雙 Scaffold

**現況**：`ItineraryAddEditScreen` 和 `ItineraryAddEditScreenContent` 都有完整 Scaffold，ContentView 只用於 Preview 卻少了封面照片功能。

**修改方式**：
- `ItineraryAddEditScreen` 的表單主體抽成 `ItineraryFormContent`
- `ItineraryAddEditScreen` 用 Scaffold 包 `ItineraryFormContent`
- Preview 直接用 `ItineraryFormContent` + 假資料

### 1.4 ItineraryListScreen 補 Loading UI

**現況**：State 有 `isLoading` 但 Screen 沒處理。

**修改方式**：
```kotlin
when {
    state.isLoading -> ItineraryListSkeleton()  // 新增 Shimmer placeholder
    state.itineraries.isEmpty() -> EmptyStateEnhanced(...)
    else -> LazyColumn { ... }
}
```

**依賴**：需在 `core/presentation/components/` 新增 `ShimmerPlaceholder.kt`（Brush + infiniteTransition）

### 1.5 統一狀態元件使用

**掃描 + 替換對象**：
- `ItineraryDetailScreen`（部分）
- `TravelHistoryScreen`
- `RouteViewScreen`
- `ItineraryMapScreen`

**原則**：
- 載入中 → `LoadingContent`（或 `LoadingOverlay`）
- 錯誤 → `ErrorContent`（含 retry 按鈕）
- 空狀態 → `EmptyStateEnhanced`（含 primary / secondary action）

### 1.6 Preview 統一 `TravelRecordTheme`

**掃描目標**：
```bash
grep -r "MaterialTheme {" --include="*.kt" | grep Preview
```

**替換**：`MaterialTheme { ... }` → `TravelRecordTheme { ... }`

### 1.7 `formatTime` 抽共用工具

**建立**：`core/presentation/util/DateTimeFormatter.kt`

```kotlin
object DateTimeFormatter {
    fun formatTime(time: LocalTime?): String = ...
    fun formatDate(date: LocalDate?, style: Style = Style.Short): String = ...
    fun formatCreatedDate(instant: Instant): String = ...
    fun formatDateRange(start: LocalDate?, end: LocalDate?): String = ...
}
```

**替換**：
- `ItemCard.kt:521`
- `TravelHistoryScreen.kt:736`
- `ItineraryListScreen.kt`（相關 format 邏輯）

### 1.8 移除 wildcard import

**檔案**：`ItineraryDetailScreen.kt`
```kotlin
// Before:
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*

// After: 只 import 實際使用到的
```

---

## 四、Phase 1 執行檢查清單

每個任務完成後必須執行：

```bash
# 1. 編譯檢查
./gradlew compileDebugKotlin

# 2. 架構檢查
/arch-check

# 3. 手動驗證路徑（至少涵蓋）
# - 行程列表初次載入（確認 Shimmer 顯示）
# - 新增項目（從 FAB）
# - QuickAdd 項目（從 TimelineNavigator 日期節點）
# - 編輯項目
# - 編輯行程
```

每個任務 commit 訊息格式：
```
refactor(ui): 抽取 ItemFormContent 消除 AddEdit/Edit 重複
fix(ui): 照片刪除按鈕觸控區域修正為 48dp
...
```

---

## 五、執行狀態（截至 2026-04-24）

### Phase 1 — 技術債 ✅ 全部完成（8 個子任務 commit）
Android + iOS assemble 通過、模擬器驗證通過。

### Phase 2 — UX 流程補完 ✅ 大部分完成
| # | 任務 | 狀態 |
|---|------|------|
| 2.1 | 同日拖拽排序 | ✅ 完成後依使用者回饋**移除**（保留 `order` 欄位資料層，改時間即可變順序）|
| 2.2 | 地圖按日期上色 | ✅ 完成（虛線連接待真實地圖就位後再做，目前是列表版） |
| 2.3 | 快速新增項目 | ⏸ 既有 QuickAddItem 路徑已覆蓋類似能力，未獨立做 |
| 2.4 | 日期欄位引導 | ✅ 於 Phase 1.1 抽 ItemFormContent 時順手完成 |
| 2.5 | 行程列表卡片擴充 | ✅ 現況已具備 |
| 2.6 | TimelineNavigator 進度 badge | ✅ 現況已有 + 本輪修正多項跑版 / 空日期虛線圓 |
| 2.7 | TravelHistory 日期篩選 DateRangePicker | ✅ 完成 |

### Phase 3 — 品質感打磨 ⏸ **尚未開始**
| # | 任務 | 狀態 |
|---|------|------|
| 3.1 | 空狀態插圖取代 Icon | ⏸ 待做 |
| 3.2 | 深色模式判斷收到 Theme 層 | ⏸ 待做 |
| 3.3 | 行程卡片 / 項目左右滑手勢（SwipeToDismiss）| ⏸ 待做 |
| 3.4 | Shimmer/Skeleton 擴展到其他畫面 | ⏸ 僅列表頁完成，其他畫面待做 |
| 3.5 | 旅遊歷史統計摘要卡片 | ⏸ 待做 |
| 3.6 | 搜尋進階篩選（日期 / 地點 / 狀態）| ⏸ 待做 |
| 3.7 | `contentDescription` 補齊（48 處）| ⏸ 隨任務補，不急 |

### 額外完成（計畫外但實際做了）
- Preview 遷移到 Compose Multiplatform 1.10 新 artifact（消除 28 個 deprecation warning）
- `ClassNotFoundException: ComposeViewAdapter` 修正（`ui-tooling` runtime）
- 封面照片解析度差修正（改讀原圖 + 預設縮圖 200→400）
- PhotoGallery MoreVert 半透明圓形背景避免遮擋縮圖
- TimelineNavigator 連續修正：今天 halo 撐大、圓點浮動（Top 對齊）、中文字 fallback line height、AllTab 佔位對稱
- 快速新增項目日期偏 1 天 bug 修正
- 新增項目有照片時第一張自動設為封面
- TimelineNavigator 0/0 節點改虛線空心圓（待規劃狀態）
- 每日進度計數 bug（State 分離 `groupedItems` / `allGroupedItems`）
- LocalTime 顯示毫秒 bug（抽 `DateTimeFormatter`）

---

## 六、變更紀錄

- 2026-04-24：建立計畫，完成 Phase 1 + Phase 2 大部分；Phase 3 待後續處理。

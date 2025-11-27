# Design Document

## Overview

旅遊流程記事應用程式採用 Kotlin Multiplatform 和 Compose Multiplatform 技術，實現跨平台的旅遊規劃與記錄系統。應用程式採用 MVVM (Model-View-ViewModel) 架構模式，結合 Repository pattern 進行資料管理，確保程式碼的可維護性和可測試性。

核心設計原則：
- **平台無關的 business logic**：所有核心功能實現在 commonMain 中
- **Single source of truth**：使用 Repository pattern 統一管理資料存取
- **Reactive UI**：利用 Compose 的 state management 實現自動 UI 更新
- **Offline-first**：以 local storage 為主，支援離線操作
- **Type safety**：使用 Kotlin 的 type system 確保資料完整性

## Architecture

### 分層架構

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  (Compose UI + ViewModels)          │
└─────────────────────────────────────┘
              ↓ ↑
┌─────────────────────────────────────┐
│         Domain Layer                │
│  (Use Cases + Business Logic)       │
└─────────────────────────────────────┘
              ↓ ↑
┌─────────────────────────────────────┐
│         Data Layer                  │
│  (Repositories + Data Sources)      │
└─────────────────────────────────────┘
              ↓ ↑
┌─────────────────────────────────────┐
│      Platform-Specific Storage      │
│  (Local Storage Implementation)     │
└─────────────────────────────────────┘
```

### 模組結構

- **data**: 資料模型、Repository 實作、local storage
- **domain**: Business logic、Use Cases
- **ui**: Compose UI 元件、ViewModels、導航
- **util**: 工具類別、extension functions

## Components and Interfaces

### Data Layer

#### Models

```kotlin
// 旅遊行程
data class Itinerary(
    val id: String,
    val title: String,
    val description: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val items: List<ItineraryItem>,
    val createdAt: Instant,
    val modifiedAt: Instant
)

// 行程項目
data class ItineraryItem(
    val id: String,
    val itineraryId: String,
    val date: LocalDate,
    val time: LocalTime?,
    val location: Location,
    val activity: String,
    val notes: String,
    val isCompleted: Boolean,
    val completedAt: Instant?,
    val photoReferences: List<String>,
    val createdAt: Instant,
    val modifiedAt: Instant
)

// 地點
data class Location(
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
    val address: String?
)

// 可分享的路線
data class Route(
    val id: String,
    val title: String,
    val locations: List<RouteLocation>,
    val createdFrom: String // itinerary id
)

data class RouteLocation(
    val location: Location,
    val order: Int,
    val recommendedDuration: Duration?,
    val notes: String
)
```

#### Repository Interfaces

```kotlin
interface ItineraryRepository {
    suspend fun createItinerary(itinerary: Itinerary): Result<Itinerary>
    suspend fun getItinerary(id: String): Result<Itinerary?>
    suspend fun getAllItineraries(): Result<List<Itinerary>>
    suspend fun updateItinerary(itinerary: Itinerary): Result<Itinerary>
    suspend fun deleteItinerary(id: String): Result<Unit>
    suspend fun searchItineraries(query: String): Result<List<Itinerary>>
}

interface ItineraryItemRepository {
    suspend fun addItem(item: ItineraryItem): Result<ItineraryItem>
    suspend fun updateItem(item: ItineraryItem): Result<ItineraryItem>
    suspend fun deleteItem(id: String): Result<Unit>
    suspend fun getItemsByItinerary(itineraryId: String): Result<List<ItineraryItem>>
    suspend fun getItemsByLocation(locationName: String): Result<List<ItineraryItem>>
    suspend fun getItemsByDateRange(start: LocalDate, end: LocalDate): Result<List<ItineraryItem>>
}

interface RouteRepository {
    suspend fun createRoute(route: Route): Result<Route>
    suspend fun getRoute(id: String): Result<Route?>
    suspend fun exportRoute(id: String): Result<String> // JSON format
}

interface StorageService {
    suspend fun save(key: String, data: String): Result<Unit>
    suspend fun load(key: String): Result<String?>
    suspend fun delete(key: String): Result<Unit>
    suspend fun getAllKeys(): Result<List<String>>
}
```

### Domain Layer

#### Use Cases

```kotlin
class CreateItineraryUseCase(
    private val repository: ItineraryRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Result<Itinerary>
}

class AddItineraryItemUseCase(
    private val itemRepository: ItineraryItemRepository,
    private val itineraryRepository: ItineraryRepository
) {
    suspend operator fun invoke(
        itineraryId: String,
        date: LocalDate,
        time: LocalTime?,
        location: Location,
        activity: String,
        notes: String
    ): Result<ItineraryItem>
}

class GetTravelHistoryUseCase(
    private val itemRepository: ItineraryItemRepository
) {
    suspend operator fun invoke(): Result<Map<Location, List<ItineraryItem>>>
}

class CreateRouteFromItineraryUseCase(
    private val itineraryRepository: ItineraryRepository,
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(itineraryId: String): Result<Route>
}

class SearchItinerariesUseCase(
    private val itineraryRepository: ItineraryRepository,
    private val itemRepository: ItineraryItemRepository
) {
    suspend operator fun invoke(query: String): Result<List<Itinerary>>
}
```

### Presentation Layer

#### ViewModels

```kotlin
class ItineraryListViewModel(
    private val getAllItinerariesUseCase: GetAllItinerariesUseCase,
    private val searchUseCase: SearchItinerariesUseCase
) : ViewModel() {
    val itineraries: StateFlow<List<Itinerary>>
    val searchQuery: StateFlow<String>
    val isLoading: StateFlow<Boolean>
    
    fun search(query: String)
    fun refresh()
}

class ItineraryDetailViewModel(
    private val getItineraryUseCase: GetItineraryUseCase,
    private val addItemUseCase: AddItineraryItemUseCase,
    private val updateItemUseCase: UpdateItineraryItemUseCase,
    private val deleteItemUseCase: DeleteItineraryItemUseCase
) : ViewModel() {
    val itinerary: StateFlow<Itinerary?>
    val items: StateFlow<List<ItineraryItem>>
    val progress: StateFlow<Float>
    
    fun addItem(...)
    fun updateItem(item: ItineraryItem)
    fun deleteItem(itemId: String)
    fun toggleItemCompletion(itemId: String)
}

class TravelHistoryViewModel(
    private val getTravelHistoryUseCase: GetTravelHistoryUseCase
) : ViewModel() {
    val historyByLocation: StateFlow<Map<Location, List<ItineraryItem>>>
    val dateFilter: StateFlow<DateRange?>
    
    fun filterByDateRange(start: LocalDate, end: LocalDate)
    fun clearFilter()
}
```

#### UI Screens

- **ItineraryListScreen**: 顯示所有行程列表，支援搜尋
- **ItineraryDetailScreen**: 顯示單一行程的詳細資訊和項目列表
- **AddEditItineraryScreen**: 建立或編輯行程
- **AddEditItemScreen**: 新增或編輯行程項目
- **TravelHistoryScreen**: 顯示按地點分組的旅遊歷史
- **RouteViewScreen**: 顯示可分享的路線資訊

## Data Models

### 資料驗證規則

1. **Itinerary**
   - `title`: 不可為空，至少包含一個非空白字元
   - `endDate`: 如果設定，必須不早於 `startDate`
   - `id`: UUID 格式

2. **ItineraryItem**
   - `location.name`: 不可為空
   - `activity`: 不可為空
   - `date`: 必須為有效日期
   - `completedAt`: 只有當 `isCompleted` 為 true 時才能設定

3. **Location**
   - `name`: 不可為空
   - `latitude`: 如果設定，範圍 -90 到 90
   - `longitude`: 如果設定，範圍 -180 到 180

### 資料排序規則

- **Itinerary 列表**: 預設按 `createdAt` 降序排列
- **ItineraryItem 列表**: 按 `date` 升序，相同日期按 `time` 升序
- **Travel History**: 按地點名稱字母順序，每個地點內的項目按日期升序

### Local Storage 格式

使用 JSON serialization 儲存資料：
- Key pattern: `itinerary:{id}` 用於 itineraries
- Key pattern: `item:{id}` 用於 items
- Key pattern: `route:{id}` 用於 routes
- Index key: `itinerary:index` 用於所有 itinerary IDs 的列表

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Core Data Properties

Property 1: Itinerary 建立完整性
*For any* 有效的 title、description 和 date range，建立 itinerary 應該回傳一個具有 unique ID、所有提供的欄位和 creation timestamp 的物件。
**Validates: Requirements 1.1**

Property 2: Title 驗證
*For any* string input，系統應該只在包含至少一個非空白字元時接受它作為 title。
**Validates: Requirements 1.2**

Property 3: 新 itinerary 初始化
*For any* 新建立的 itinerary，其 items list 應該是空的。
**Validates: Requirements 1.3**

Property 4: Itinerary persistence round-trip
*For any* 建立的 itinerary，從 storage 取回應該回傳相同資料的等價 itinerary。
**Validates: Requirements 1.4**

Property 5: Date range 驗證
*For any* start 和 end date pair，如果 end date 早於 start date，系統應該拒絕該 itinerary。
**Validates: Requirements 1.5**

### Item Management Properties

Property 6: Item 建立完整性
*For any* 包含 date、time、location、activity 和 notes 的 itinerary item，儲存該 item 應該保留所有欄位。
**Validates: Requirements 2.1**

Property 7: Location name 驗證
*For any* location input，如果 name 為空或只包含空白字元，系統應該拒絕它。
**Validates: Requirements 2.2**

Property 8: 時間順序排序不變性
*For any* itinerary，無論 items 以何種順序加入，items list 應該始終按 date 和 time 時間順序排序。
**Validates: Requirements 2.3, 4.3**

Property 9: 唯一 item identifiers
*For any* 在 itinerary 中建立的 items 集合，所有 item IDs 應該是唯一的。
**Validates: Requirements 2.5**

Property 10: Modification timestamp 更新
*For any* item modification，modifiedAt timestamp 應該更新為大於先前 timestamp 的值。
**Validates: Requirements 3.1**

Property 11: Item deletion round-trip
*For any* 已刪除的 item，嘗試從 storage 取回應該回傳 null 或 error。
**Validates: Requirements 3.2**

Property 12: 刪除保持排序
*For any* 包含 items 的 itinerary，刪除任何 item 應該讓剩餘的 items 保持時間順序。
**Validates: Requirements 3.3**

Property 13: Modification persistence
*For any* item modification，從 storage 取回該 item 應該回傳更新後的值。
**Validates: Requirements 3.5**

### Display and Query Properties

Property 14: Itinerary list 排序
*For any* itineraries 集合，取回所有 itineraries 應該回傳按 creation date 降序排列的結果。
**Validates: Requirements 4.1**

Property 15: Display format 完整性
*For any* itinerary 或 item，display representation 應該包含所有必要欄位（title、dates、location、activity 等）。
**Validates: Requirements 4.2, 4.4**

### Completion Tracking Properties

Property 16: Completion status 更新
*For any* 標記為完成的 item，該 item 應該將 isCompleted 設為 true 且 completedAt 設為有效的 timestamp。
**Validates: Requirements 5.1**

Property 17: 已完成的 items 保留在 itinerary 中
*For any* 標記為完成的 item，它應該仍然存在於 itinerary 的 items list 中。
**Validates: Requirements 5.2**

Property 18: Completion toggle persistence
*For any* 切換完成狀態的 item，取回該 item 應該反映新的狀態。
**Validates: Requirements 5.4**

Property 19: Progress 計算準確性
*For any* itinerary，progress percentage 應該等於 (completed items count / total items count) * 100。
**Validates: Requirements 5.5**

### Travel History Properties

Property 20: History 按 location 分組
*For any* 已完成 items 的集合，travel history 應該按 location name 分組，相同 location 的所有 items 在一起。
**Validates: Requirements 6.1**

Property 21: Date range 過濾
*For any* 套用到 history 的 date range filter，只有日期在範圍內（含）的 items 應該被回傳。
**Validates: Requirements 6.3**

Property 22: 基於 location 的 item 取回
*For any* location，取回該 location 的 items 應該回傳所有且僅有該確切 location name 的 items。
**Validates: Requirements 6.5**

### Route Generation Properties

Property 23: 從 itinerary 生成 route
*For any* 至少有兩個 items 的 itinerary，生成 route 應該產生一個包含所有唯一 locations 按時間順序排列的 route。
**Validates: Requirements 7.1**

Property 24: Route data 完整性
*For any* 生成的 route，每個 route location 應該包含 name、coordinates（如果有）和 recommended duration。
**Validates: Requirements 7.2**

Property 25: Route generation 驗證
*For any* 少於兩個唯一 locations 的 itinerary，嘗試生成 route 應該失敗並回傳 validation error。
**Validates: Requirements 7.3**

Property 26: Route unique identifier
*For any* 建立的 route，它應該有一個與所有其他 routes 不同的 unique ID。
**Validates: Requirements 7.4**

Property 27: Route serialization round-trip
*For any* route，將其 serialize 為 JSON 然後 deserialize 應該產生具有相同資料的等價 route。
**Validates: Requirements 7.5**

### Search Properties

Property 28: Multi-field search
*For any* search query，結果應該包含所有 query 符合 title、任何 location name 或任何 activity description 的 itineraries（不區分大小寫）。
**Validates: Requirements 8.1**

Property 29: 每個結果單一 itinerary
*For any* 對 search query 有多個匹配的 itinerary，該 itinerary 應該在結果中只出現一次。
**Validates: Requirements 8.4**

Property 30: 基於日期的 search
*For any* date range search，結果應該包含所有日期在指定範圍內的 itineraries 和 items。
**Validates: Requirements 8.5**

### Offline and Sync Properties

Property 31: Offline data modification
*For any* 在離線時進行的資料修改，該變更應該儲存在 local storage 中並可立即取回。
**Validates: Requirements 9.2**

Property 32: 基於 timestamp 的衝突解決
*For any* 同一 item 的兩個衝突版本，應該保留 modifiedAt timestamp 較新的版本。
**Validates: Requirements 9.4**

### Photo Management Properties

Property 33: Photo reference storage
*For any* 加入到 item 的 photo，該 item 的 photoReferences list 應該包含該 photo reference。
**Validates: Requirements 10.1**

Property 34: Photo reference 清理
*For any* 有 photo references 的已刪除 item，所有相關的 photo references 應該從 storage 中移除。
**Validates: Requirements 10.4**

## Error Handling

### Error Types

```kotlin
sealed class TravelAppError {
    data class ValidationError(val field: String, val message: String) : TravelAppError()
    data class NotFoundError(val entityType: String, val id: String) : TravelAppError()
    data class StorageError(val message: String, val cause: Throwable?) : TravelAppError()
    data class NetworkError(val message: String, val cause: Throwable?) : TravelAppError()
    data class ConflictError(val message: String) : TravelAppError()
}
```

### Error Handling Strategy

1. **Validation Errors**: 在 Use Case 層級進行驗證，回傳具體的驗證錯誤訊息
2. **Storage Errors**: 使用 Result type 包裝所有儲存操作，捕獲並轉換 exceptions
3. **Network Errors**: 實現重試機制，最多重試 3 次，使用 exponential backoff
4. **Conflict Errors**: 在同步時檢測衝突，使用 timestamp 解決
5. **Not Found Errors**: 當查詢不存在的 entity 時回傳明確的錯誤

### 錯誤恢復機制

- **Local storage 失敗**: 記錄錯誤日誌，向使用者顯示錯誤訊息，保留記憶體中的資料
- **Network sync 失敗**: 將變更標記為待同步，在下次網路可用時重試
- **Data conflicts**: 自動使用最新 timestamp 的版本，記錄衝突日誌供除錯

## Testing Strategy

### Unit Testing

使用 Kotlin Test framework 進行單元測試，重點測試：

1. **資料驗證邏輯**
   - 測試各種無效輸入（空字串、無效日期範圍等）
   - 驗證錯誤訊息的正確性

2. **Business Logic**
   - Use Case 的核心邏輯
   - 排序和過濾演算法
   - 進度計算

3. **資料轉換**
   - Model 到 UI State 的轉換
   - JSON serialization/deserialization

4. **邊緣情況**
   - 空列表處理
   - 單一項目的行程
   - 相同日期和時間的項目

### Property-Based Testing

使用 **Kotest Property Testing** framework 進行屬性測試。每個屬性測試應該：

- 運行至少 **100 次迭代**以確保覆蓋各種輸入
- 使用智能生成器生成有效的測試資料
- 明確標記對應的設計文件屬性

**測試標記格式**: `// Feature: travel-itinerary, Property {number}: {property_text}`

**需要實現的屬性測試**:

1. **Property 1-5**: Itinerary 建立和驗證
2. **Property 6-13**: Item 管理和持久化
3. **Property 14-15**: 顯示和查詢
4. **Property 16-19**: 完成狀態追蹤
5. **Property 20-22**: 旅遊歷史
6. **Property 23-27**: Route 生成
7. **Property 28-30**: 搜尋功能
8. **Property 31-32**: 離線和同步
9. **Property 33-34**: 照片管理

### 測試資料生成器

為屬性測試創建智能生成器：

```kotlin
// 生成有效的 itinerary
fun Arb.Companion.validItinerary(): Arb<Itinerary>

// 生成有效的 item
fun Arb.Companion.validItineraryItem(): Arb<ItineraryItem>

// 生成有效的 location
fun Arb.Companion.validLocation(): Arb<Location>

// 生成無效的 title（空白或純空格）
fun Arb.Companion.invalidTitle(): Arb<String>

// 生成無效的 date range（結束早於開始）
fun Arb.Companion.invalidDateRange(): Arb<Pair<LocalDate, LocalDate>>
```

### 整合測試

測試多個元件的協作：

1. **Repository + Storage**: 驗證資料持久化的完整流程
2. **Use Case + Repository**: 驗證 business logic 與 data layer 的整合
3. **ViewModel + Use Case**: 驗證 UI state management

### UI 測試

使用 Compose Testing framework：

1. **導航流程**: 驗證畫面間的導航
2. **使用者互動**: 驗證按鈕點擊、輸入等
3. **狀態顯示**: 驗證 UI 正確反映 ViewModel 狀態

### 測試覆蓋率目標

- **Unit Tests**: 核心 business logic 90%+
- **Property Tests**: 所有 34 個正確性屬性
- **Integration Tests**: 主要使用流程
- **UI Tests**: 關鍵使用者路徑

## Performance Considerations

### 資料載入優化

- **Lazy Loading**: 只在需要時載入項目詳情
- **Pagination**: 對於大量行程，實現分頁載入
- **Caching**: 在記憶體中快取最近存取的資料

### 儲存優化

- **Batch Operations**: 將多個儲存操作批次處理
- **Indexing**: 維護 itinerary ID 索引以加快查詢
- **Compression**: 對照片進行壓縮以節省空間

### UI 效能

- **Virtualized Lists**: 使用 LazyColumn 處理長列表
- **State Hoisting**: 避免不必要的 recomposition
- **Memoization**: 使用 remember 和 derivedStateOf 優化計算

## Platform-Specific Implementations

### Storage Service

每個平台需要實現 `StorageService` interface：

- **Android**: 使用 SharedPreferences 或 DataStore
- **iOS**: 使用 UserDefaults 或 Core Data
- **JS/Web**: 使用 LocalStorage
- **Desktop**: 使用檔案系統

### Photo Storage

- **Android**: 使用 MediaStore API
- **iOS**: 使用 Photos Framework
- **Web**: 使用 IndexedDB 或 File API
- **Desktop**: 使用檔案系統

### Date/Time Handling

使用 `kotlinx-datetime` library 確保跨平台一致性。

## Future Enhancements

1. **Cloud Sync**: 實現完整的雲端備份和多裝置同步
2. **Map Integration**: 在地圖上顯示路線和地點
3. **Community Sharing**: 允許使用者公開分享行程
4. **Budget Tracking**: 為每個項目添加費用記錄
5. **Weather Info**: 整合天氣 API 顯示目的地天氣
6. **Collaboration**: 允許多人共同編輯行程
7. **Export**: 匯出為 PDF 或其他格式
8. **Template System**: 提供常見旅遊路線範本

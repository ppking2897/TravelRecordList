# Design Document - 行程 CRUD 增強

## 概述 (Overview)

本設計文件描述如何實作行程和項目的完整 CRUD 操作，包括編輯、刪除確認和草稿暫存功能。設計遵循現有的 Clean Architecture 架構，並使用 Compose Multiplatform 實作 UI。

## 架構 (Architecture)

### 分層架構

```
UI Layer (Compose)
    ├── Screens (AddEditItineraryScreen, ItineraryListScreen, ItineraryDetailScreen)
    ├── Components (DeleteConfirmDialog, DraftIndicator)
    └── ViewModels (ItineraryListViewModel, ItineraryDetailViewModel, AddEditItineraryViewModel)
         ↓
Domain Layer
    ├── Use Cases (UpdateItineraryUseCase, DeleteItineraryUseCase, SaveDraftUseCase)
    └── Models (Itinerary, ItineraryItem, Draft)
         ↓
Data Layer
    ├── Repositories (ItineraryRepository, DraftRepository)
    └── Storage (StorageService)
```

## 元件和介面 (Components and Interfaces)

### 1. UI Components

#### DeleteConfirmDialog
```kotlin
@Composable
fun DeleteConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
)
```

用於顯示刪除確認對話框的可重用元件。

#### EditItemScreen
```kotlin
@Composable
fun EditItemScreen(
    item: ItineraryItem,
    itinerary: Itinerary,
    updateItemUseCase: UpdateItineraryItemUseCase,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
)
```

編輯現有項目的畫面。

### 2. ViewModels

#### ItineraryListViewModel 擴充
```kotlin
class ItineraryListViewModel {
    // 新增方法
    fun deleteItinerary(id: String)
    fun getItinerary(id: String): Itinerary?
}
```

#### AddEditItineraryViewModel (新增)
```kotlin
class AddEditItineraryViewModel(
    private val createUseCase: CreateItineraryUseCase,
    private val updateUseCase: UpdateItineraryUseCase,
    private val draftRepository: DraftRepository
) : ViewModel() {
    val title: StateFlow<String>
    val description: StateFlow<String>
    val startDate: StateFlow<LocalDate?>
    val endDate: StateFlow<LocalDate?>
    val isEditMode: StateFlow<Boolean>
    
    fun loadItinerary(id: String?)
    fun saveDraft()
    fun loadDraft()
    fun clearDraft()
    fun save()
}
```

### 3. Use Cases

#### UpdateItineraryUseCase
```kotlin
class UpdateItineraryUseCase(
    private val repository: ItineraryRepository
) {
    suspend operator fun invoke(
        itinerary: Itinerary,
        currentTimestamp: Instant
    ): Result<Itinerary>
}
```

#### DeleteItineraryUseCase
```kotlin
class DeleteItineraryUseCase(
    private val itineraryRepository: ItineraryRepository,
    private val itemRepository: ItineraryItemRepository
) {
    suspend operator fun invoke(id: String): Result<Unit>
}
```

#### SaveDraftUseCase
```kotlin
class SaveDraftUseCase(
    private val draftRepository: DraftRepository
) {
    suspend operator fun invoke(
        type: DraftType,
        data: Map<String, Any>
    ): Result<Unit>
}
```

#### LoadDraftUseCase
```kotlin
class LoadDraftUseCase(
    private val draftRepository: DraftRepository
) {
    suspend operator fun invoke(
        type: DraftType
    ): Result<Map<String, Any>?>
}
```

### 4. Data Models

#### Draft
```kotlin
@Serializable
data class Draft(
    val id: String,
    val type: DraftType,
    val data: Map<String, String>,
    @Contextual val createdAt: Instant,
    @Contextual val modifiedAt: Instant
)

enum class DraftType {
    ITINERARY,
    ITEM
}
```

### 5. Repositories

#### DraftRepository
```kotlin
interface DraftRepository {
    suspend fun saveDraft(draft: Draft): Result<Unit>
    suspend fun getDraft(type: DraftType): Result<Draft?>
    suspend fun deleteDraft(type: DraftType): Result<Unit>
    suspend fun deleteExpiredDrafts(): Result<Unit>
}
```

## 資料模型 (Data Models)

### Itinerary (現有，無需修改)
```kotlin
data class Itinerary(
    val id: String,
    val title: String,
    val description: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val items: List<ItineraryItem> = emptyList(),
    val createdAt: Instant,
    val modifiedAt: Instant
)
```

### Draft (新增)
```kotlin
data class Draft(
    val id: String,
    val type: DraftType,
    val data: Map<String, String>,
    val createdAt: Instant,
    val modifiedAt: Instant
)
```

## 正確性屬性 (Correctness Properties)

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: 刪除行程同時刪除項目
*For any* 行程，當刪除該行程時，該行程的所有項目也應該被刪除
**Validates: Requirements 8.1**

### Property 2: 編輯後資料一致性
*For any* 行程，編輯並儲存後，重新讀取應該得到相同的資料
**Validates: Requirements 1.3**

### Property 3: 草稿暫存和恢復
*For any* 草稿資料，儲存後再讀取應該得到相同的資料
**Validates: Requirements 4.1, 4.2**

### Property 4: 刪除確認防止誤刪
*For any* 刪除操作，在使用者確認前不應該執行實際刪除
**Validates: Requirements 2.3, 3.3**

### Property 5: 日期範圍驗證
*For any* 行程編輯，如果新日期範圍不包含現有項目的日期，應該顯示警告
**Validates: Requirements 8.2, 8.3**

## 錯誤處理 (Error Handling)

### 刪除錯誤
- 行程不存在：顯示 "行程不存在" 錯誤
- 網路錯誤：顯示 "無法連接，請稍後再試"
- 權限錯誤：顯示 "沒有權限執行此操作"

### 編輯錯誤
- 驗證失敗：顯示具體的驗證錯誤訊息
- 衝突錯誤：顯示 "資料已被其他使用者修改，請重新載入"
- 儲存失敗：顯示 "儲存失敗，請重試"

### 草稿錯誤
- 儲存失敗：靜默失敗，不影響使用者操作
- 讀取失敗：使用空白表單

## 測試策略 (Testing Strategy)

### Unit Tests
- 測試 ViewModel 的狀態管理
- 測試 Use Case 的業務邏輯
- 測試 Repository 的資料操作

### Property-Based Tests
使用 Kotest property testing framework，每個測試執行 100 次迭代。

- Property 1: 刪除行程級聯刪除項目
- Property 2: 編輯後資料一致性
- Property 3: 草稿暫存和恢復
- Property 4: 刪除確認流程
- Property 5: 日期範圍驗證

### Integration Tests
- 測試完整的編輯流程
- 測試完整的刪除流程
- 測試草稿暫存和恢復流程

## UI/UX 考量

### 刪除確認 Dialog
- 使用 Material 3 AlertDialog
- 標題：「確認刪除」
- 內容：顯示要刪除的項目名稱和警告
- 按鈕：「取消」（TextButton）和「刪除」（TextButton，error color）

### 編輯畫面
- 重用 AddEditItineraryScreen，根據是否有 itineraryId 判斷新增或編輯模式
- Top bar 標題：新增時顯示「新增行程」，編輯時顯示「編輯行程」
- 預填現有資料

### 草稿指示器
- 在輸入欄位下方顯示小字「已自動儲存草稿」
- 使用淡入淡出動畫
- 3 秒後自動消失

## 效能考量

### 草稿自動儲存
- 使用 debounce 機制，輸入停止 500ms 後才儲存
- 避免頻繁的儲存操作

### 刪除操作
- 使用樂觀更新：先更新 UI，再執行實際刪除
- 如果刪除失敗，回滾 UI 變更

### 快取策略
- ViewModel 快取行程資料
- 避免重複載入相同資料

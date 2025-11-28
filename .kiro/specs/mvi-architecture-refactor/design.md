# Design Document - MVI 架構重構

## Overview

本設計文件描述如何將現有的 MVVM 架構重構為 MVI (Model-View-Intent) 架構。MVI 提供單向資料流、不可變狀態和清晰的意圖處理，使 UI 狀態更可預測和易於測試。

## Architecture

### MVI 架構圖

```
┌─────────────────────────────────────────────────────────┐
│                         View                             │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Compose Screen                                   │  │
│  │  - 觀察 State (StateFlow)                        │  │
│  │  - 收集 Event (SharedFlow)                       │  │
│  │  - 發送 Intent                                    │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                    │                    ▲
                    │ Intent             │ State/Event
                    ▼                    │
┌─────────────────────────────────────────────────────────┐
│                      ViewModel                           │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Intent Handler                                   │  │
│  │  - 接收 Intent                                    │  │
│  │  - 呼叫 Use Case                                  │  │
│  │  - 更新 State                                     │  │
│  │  - 發送 Event                                     │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │  State: StateFlow<UiState>                       │  │
│  │  Event: SharedFlow<UiEvent>                      │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                    │
                    │ Repository Call
                    ▼
┌─────────────────────────────────────────────────────────┐
│                   Domain Layer                           │
│  - Use Cases                                             │
│  - Repositories                                          │
└─────────────────────────────────────────────────────────┘
```

### 資料流向

1. **View → ViewModel**: 用戶操作觸發 Intent
2. **ViewModel → Domain**: 處理 Intent，呼叫 Use Case
3. **Domain → ViewModel**: 返回結果
4. **ViewModel → View**: 更新 State 或發送 Event
5. **View**: 根據新的 State 重新渲染 UI

## Components and Interfaces

### 1. 基礎介面

#### UiState
```kotlin
/**
 * 標記介面，所有 UI State 都應實作此介面
 */
interface UiState

/**
 * 通用的載入狀態包裝
 */
sealed class LoadingState<out T> {
    object Idle : LoadingState<Nothing>()
    object Loading : LoadingState<Nothing>()
    data class Success<T>(val data: T) : LoadingState<T>()
    data class Error(val message: String) : LoadingState<Nothing>()
}
```

#### UiIntent
```kotlin
/**
 * 標記介面，所有 UI Intent 都應實作此介面
 */
interface UiIntent
```

#### UiEvent
```kotlin
/**
 * 標記介面，所有 UI Event 都應實作此介面
 * Event 是單次性的，不應該保存在 State 中
 */
interface UiEvent
```

### 2. BaseViewModel

```kotlin
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEvent>(
    initialState: S
) : ViewModel() {
    
    // State: 使用 StateFlow 管理 UI 狀態
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()
    
    // Event: 使用 SharedFlow 處理單次事件
    private val _event = MutableSharedFlow<E>()
    val event: SharedFlow<E> = _event.asSharedFlow()
    
    // Intent 處理
    fun handleIntent(intent: I) {
        viewModelScope.launch {
            processIntent(intent)
        }
    }
    
    // 子類實作此方法處理具體的 Intent
    protected abstract suspend fun processIntent(intent: I)
    
    // 更新 State
    protected fun updateState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }
    
    // 發送 Event
    protected suspend fun sendEvent(event: E) {
        _event.emit(event)
    }
    
    // 當前 State
    protected val currentState: S
        get() = _state.value
}
```

### 3. 畫面特定實作

#### ItineraryListScreen

**State**:
```kotlin
data class ItineraryListState(
    val itineraries: List<Itinerary> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState
```

**Intent**:
```kotlin
sealed class ItineraryListIntent : UiIntent {
    object LoadItineraries : ItineraryListIntent()
    object Refresh : ItineraryListIntent()
    data class Search(val query: String) : ItineraryListIntent()
    data class DeleteItinerary(val id: String) : ItineraryListIntent()
}
```

**Event**:
```kotlin
sealed class ItineraryListEvent : UiEvent {
    data class NavigateToDetail(val id: String) : ItineraryListEvent()
    data class NavigateToEdit(val id: String) : ItineraryListEvent()
    object NavigateToAdd : ItineraryListEvent()
    data class ShowDeleteConfirm(val itinerary: Itinerary) : ItineraryListEvent()
    data class ShowError(val message: String) : ItineraryListEvent()
}
```

**ViewModel**:
```kotlin
class ItineraryListViewModel(
    private val itineraryRepository: ItineraryRepository,
    private val searchUseCase: SearchItinerariesUseCase,
    private val deleteUseCase: DeleteItineraryUseCase
) : BaseViewModel<ItineraryListState, ItineraryListIntent, ItineraryListEvent>(
    initialState = ItineraryListState()
) {
    
    init {
        handleIntent(ItineraryListIntent.LoadItineraries)
    }
    
    override suspend fun processIntent(intent: ItineraryListIntent) {
        when (intent) {
            is ItineraryListIntent.LoadItineraries -> loadItineraries()
            is ItineraryListIntent.Refresh -> refresh()
            is ItineraryListIntent.Search -> search(intent.query)
            is ItineraryListIntent.DeleteItinerary -> deleteItinerary(intent.id)
        }
    }
    
    private suspend fun loadItineraries() {
        updateState { copy(isLoading = true, error = null) }
        
        itineraryRepository.getAllItineraries()
            .onSuccess { itineraries ->
                updateState { 
                    copy(
                        itineraries = itineraries,
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                updateState { 
                    copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            }
    }
    
    private suspend fun search(query: String) {
        updateState { copy(searchQuery = query) }
        
        searchUseCase(query)
            .onSuccess { results ->
                updateState { copy(itineraries = results) }
            }
            .onFailure { exception ->
                sendEvent(ItineraryListEvent.ShowError(exception.message ?: "搜尋失敗"))
            }
    }
    
    private suspend fun deleteItinerary(id: String) {
        deleteUseCase(id)
            .onSuccess {
                handleIntent(ItineraryListIntent.Refresh)
            }
            .onFailure { exception ->
                sendEvent(ItineraryListEvent.ShowError(exception.message ?: "刪除失敗"))
            }
    }
    
    private suspend fun refresh() {
        loadItineraries()
    }
}
```

**Screen**:
```kotlin
@Composable
fun ItineraryListScreen(
    viewModel: ItineraryListViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // 收集 Event
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ItineraryListEvent.NavigateToDetail -> onNavigateToDetail(event.id)
                is ItineraryListEvent.NavigateToEdit -> onNavigateToEdit(event.id)
                is ItineraryListEvent.NavigateToAdd -> onNavigateToAdd()
                is ItineraryListEvent.ShowDeleteConfirm -> {
                    // 顯示刪除確認 Dialog
                }
                is ItineraryListEvent.ShowError -> {
                    // 顯示錯誤訊息
                }
            }
        }
    }
    
    ItineraryListScreenContent(
        state = state,
        onIntent = viewModel::handleIntent
    )
}

@Composable
private fun ItineraryListScreenContent(
    state: ItineraryListState,
    onIntent: (ItineraryListIntent) -> Unit
) {
    // UI 實作
}
```

#### ItineraryDetailScreen

**State**:
```kotlin
data class ItineraryDetailState(
    val itinerary: Itinerary? = null,
    val groupedItems: List<ItemsByDate> = emptyList(),
    val selectedDate: LocalDate? = null,
    val dateRange: ClosedRange<LocalDate>? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState
```

**Intent**:
```kotlin
sealed class ItineraryDetailIntent : UiIntent {
    data class LoadItinerary(val id: String) : ItineraryDetailIntent()
    data class SelectDate(val date: LocalDate?) : ItineraryDetailIntent()
    data class ToggleItemCompletion(val itemId: String) : ItineraryDetailIntent()
    data class DeleteItem(val itemId: String) : ItineraryDetailIntent()
    object GenerateRoute : ItineraryDetailIntent()
}
```

**Event**:
```kotlin
sealed class ItineraryDetailEvent : UiEvent {
    object NavigateBack : ItineraryDetailEvent()
    object NavigateToAddItem : ItineraryDetailEvent()
    data class NavigateToEditItem(val itemId: String) : ItineraryDetailEvent()
    object NavigateToEditItinerary : ItineraryDetailEvent()
    data class ShowDeleteItemConfirm(val item: ItineraryItem) : ItineraryDetailEvent()
    data class ShowDeleteItineraryConfirm(val itinerary: Itinerary) : ItineraryDetailEvent()
    data class NavigateToRoute(val routeId: String) : ItineraryDetailEvent()
    data class ShowError(val message: String) : ItineraryDetailEvent()
}
```

#### AddEditItineraryScreen

**State**:
```kotlin
data class AddEditItineraryState(
    val isEditMode: Boolean = false,
    val itineraryId: String? = null,
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val titleError: String? = null,
    val dateError: String? = null,
    val isLoading: Boolean = false,
    val showDraftSaved: Boolean = false,
    val error: String? = null
) : UiState
```

**Intent**:
```kotlin
sealed class AddEditItineraryIntent : UiIntent {
    data class LoadItinerary(val id: String) : AddEditItineraryIntent()
    object LoadDraft : AddEditItineraryIntent()
    data class UpdateTitle(val title: String) : AddEditItineraryIntent()
    data class UpdateDescription(val description: String) : AddEditItineraryIntent()
    data class UpdateStartDate(val date: LocalDate?) : AddEditItineraryIntent()
    data class UpdateEndDate(val date: LocalDate?) : AddEditItineraryIntent()
    object Save : AddEditItineraryIntent()
    object SaveDraft : AddEditItineraryIntent()
}
```

**Event**:
```kotlin
sealed class AddEditItineraryEvent : UiEvent {
    object NavigateBack : AddEditItineraryEvent()
    data class SaveSuccess(val id: String) : AddEditItineraryEvent()
    object ShowStartDatePicker : AddEditItineraryEvent()
    object ShowEndDatePicker : AddEditItineraryEvent()
    data class ShowError(val message: String) : AddEditItineraryEvent()
}
```

## Data Models

### 狀態管理模式

所有 State 都應該：
1. 是 `data class`，確保不可變性
2. 實作 `UiState` 介面
3. 包含所有 UI 需要的資料
4. 使用 `copy()` 方法更新

### Event 處理模式

Event 應該：
1. 是 `sealed class`，確保類型安全
2. 實作 `UiEvent` 介面
3. 只用於單次性操作（導航、顯示 Toast、Dialog 等）
4. 不應該包含在 State 中

### Intent 處理模式

Intent 應該：
1. 是 `sealed class`，確保類型安全
2. 實作 `UiIntent` 介面
3. 代表用戶的意圖或動作
4. 包含執行動作所需的參數

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: State 不可變性
*For any* State 更新操作，原始 State 物件應保持不變，只產生新的 State 實例
**Validates: Requirements 1.4**

### Property 2: Event 單次性
*For any* Event 發送，每個訂閱者應只接收一次該 Event
**Validates: Requirements 1.3**

### Property 3: Intent 處理順序
*For any* 連續發送的 Intent 序列，ViewModel 應按照發送順序處理
**Validates: Requirements 1.2**

### Property 4: State 一致性
*For any* 時間點，ViewModel 應只有一個有效的 State
**Validates: Requirements 1.2**

### Property 5: 錯誤狀態清除
*For any* 用戶修正輸入後，相關的錯誤訊息應從 State 中清除
**Validates: Requirements 10.3**

### Property 6: 載入狀態管理
*For any* 非同步操作，isLoading 應在操作開始時設為 true，結束時設為 false
**Validates: Requirements 2.4, 3.4**

### Property 7: 搜尋狀態同步
*For any* 搜尋操作，searchQuery 應與顯示的結果保持一致
**Validates: Requirements 2.4**

### Property 8: 日期選擇一致性
*For any* 日期選擇操作，selectedDate 應與 groupedItems 的過濾結果一致
**Validates: Requirements 3.4**

### Property 9: 表單驗證一致性
*For any* 表單提交，所有驗證錯誤應在 State 中正確反映
**Validates: Requirements 4.4, 5.4**

### Property 10: 配置變更恢復
*For any* 配置變更（如螢幕旋轉），State 應保持不變
**Validates: Requirements 9.1**

## Error Handling

### 錯誤類型

1. **驗證錯誤**: 儲存在 State 的特定欄位錯誤中
2. **網路錯誤**: 儲存在 State 的 error 欄位中
3. **致命錯誤**: 透過 Event 發送，顯示 Dialog 或 Toast

### 錯誤處理流程

```kotlin
private suspend fun handleOperation() {
    updateState { copy(isLoading = true, error = null) }
    
    repository.operation()
        .onSuccess { result ->
            updateState { 
                copy(
                    data = result,
                    isLoading = false
                )
            }
        }
        .onFailure { exception ->
            when (exception) {
                is ValidationException -> {
                    updateState { 
                        copy(
                            fieldError = exception.message,
                            isLoading = false
                        )
                    }
                }
                else -> {
                    updateState { copy(isLoading = false) }
                    sendEvent(ShowError(exception.message ?: "操作失敗"))
                }
            }
        }
}
```

## Testing Strategy

### Unit Testing

**ViewModel 測試**:
```kotlin
class ItineraryListViewModelTest {
    
    @Test
    fun `when LoadItineraries intent, should update state with itineraries`() = runTest {
        // Given
        val mockRepository = mockk<ItineraryRepository>()
        val testItineraries = listOf(/* test data */)
        coEvery { mockRepository.getAllItineraries() } returns Result.success(testItineraries)
        
        val viewModel = ItineraryListViewModel(mockRepository, ...)
        
        // When
        viewModel.handleIntent(ItineraryListIntent.LoadItineraries)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.state.value
        assertEquals(testItineraries, state.itineraries)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `when DeleteItinerary intent succeeds, should emit refresh intent`() = runTest {
        // Given
        val mockDeleteUseCase = mockk<DeleteItineraryUseCase>()
        coEvery { mockDeleteUseCase(any()) } returns Result.success(Unit)
        
        val viewModel = ItineraryListViewModel(..., mockDeleteUseCase)
        
        // When
        viewModel.handleIntent(ItineraryListIntent.DeleteItinerary("id"))
        advanceUntilIdle()
        
        // Then
        coVerify { mockDeleteUseCase("id") }
        // Verify refresh was called
    }
}
```

### Property-Based Testing

使用 Kotest 進行 property-based testing：

```kotlin
class ItineraryListViewModelPropertyTest : StringSpec({
    
    "State should always be immutable" {
        checkAll<String, String> { title, description ->
            val viewModel = ItineraryListViewModel(...)
            val stateBefore = viewModel.state.value
            
            viewModel.handleIntent(ItineraryListIntent.Search(title))
            
            // Original state should not change
            stateBefore shouldBe stateBefore
        }
    }
    
    "Loading state should always be consistent" {
        checkAll<List<Itinerary>> { itineraries ->
            val viewModel = ItineraryListViewModel(...)
            
            viewModel.handleIntent(ItineraryListIntent.LoadItineraries)
            
            // During loading
            viewModel.state.value.isLoading shouldBe true
            
            // After completion
            advanceUntilIdle()
            viewModel.state.value.isLoading shouldBe false
        }
    }
})
```

### UI Testing

使用 Preview 進行 UI 測試：

```kotlin
@Preview
@Composable
private fun ItineraryListScreenPreview_Loading() {
    MaterialTheme {
        Surface {
            ItineraryListScreenContent(
                state = ItineraryListState(isLoading = true),
                onIntent = {}
            )
        }
    }
}

@Preview
@Composable
private fun ItineraryListScreenPreview_Error() {
    MaterialTheme {
        Surface {
            ItineraryListScreenContent(
                state = ItineraryListState(error = "載入失敗"),
                onIntent = {}
            )
        }
    }
}
```

## Migration Strategy

### 階段性遷移

1. **Phase 1**: 建立 MVI 基礎架構
   - 建立 BaseViewModel
   - 定義 UiState, UiIntent, UiEvent 介面
   - 建立測試工具

2. **Phase 2**: 遷移簡單畫面
   - RouteViewScreen (最簡單)
   - TravelHistoryScreen

3. **Phase 3**: 遷移列表畫面
   - ItineraryListScreen

4. **Phase 4**: 遷移詳情畫面
   - ItineraryDetailScreen

5. **Phase 5**: 遷移表單畫面
   - AddEditItineraryScreen
   - AddEditItemScreen
   - EditItemScreen

6. **Phase 6**: 清理和優化
   - 移除舊的 ViewModel
   - 統一錯誤處理
   - 完善測試覆蓋

### 共存策略

在遷移期間：
- 新舊 ViewModel 可以共存
- 使用不同的命名（如 `ItineraryListViewModelMVI`）
- 在 Koin 中註冊兩個版本
- 逐步切換 Screen 使用的 ViewModel

## Performance Considerations

### State 更新優化

1. 使用 `StateFlow` 的 `update` 方法確保原子性
2. 避免頻繁的 State 更新
3. 使用 `distinctUntilChanged` 避免重複渲染

### Event 處理優化

1. 使用 `SharedFlow` 的 `replay = 0` 避免重複處理
2. 使用 `MutableSharedFlow` 的 `extraBufferCapacity` 處理背壓

### 記憶體管理

1. 在 ViewModel 中使用 `viewModelScope`
2. 確保所有協程在 ViewModel 清除時取消
3. 避免在 State 中儲存大型物件

## Dependencies

- Kotlin Coroutines
- Kotlin Flow
- Lifecycle ViewModel
- Koin (依賴注入)
- Kotest (測試)

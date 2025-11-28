# Requirements Document - MVI 架構重構

## Introduction

將現有的 MVVM 架構重構為 MVI (Model-View-Intent) 架構，以提供更清晰的狀態管理、更好的可測試性和更可預測的 UI 行為。

## Glossary

- **MVI**: Model-View-Intent 架構模式
- **State**: 不可變的 UI 狀態，代表畫面在某個時間點的完整狀態
- **Intent**: 用戶意圖或動作，代表用戶想要執行的操作
- **Event**: 單次性事件，如導航、顯示 Toast、顯示 Dialog
- **ViewModel**: 負責處理 Intent 並產生新的 State
- **Screen**: Compose UI 畫面，觀察 State 並發送 Intent

## Requirements

### Requirement 1: MVI 核心架構

**User Story:** 作為開發者，我想要建立 MVI 核心架構基礎，以便所有畫面都能遵循統一的模式。

#### Acceptance Criteria

1. WHEN 定義 MVI 基礎類別 THEN 系統應提供 BaseViewModel、UiState、UiIntent、UiEvent 等基礎介面
2. WHEN ViewModel 處理 Intent THEN 系統應使用單一 StateFlow 管理 UI 狀態
3. WHEN 產生單次事件 THEN 系統應使用 Channel 或 SharedFlow 處理 Event
4. WHEN State 更新 THEN 系統應確保 State 是不可變的 data class
5. WHEN 處理非同步操作 THEN 系統應在 ViewModel 中使用 viewModelScope

### Requirement 2: ItineraryListScreen MVI 重構

**User Story:** 作為用戶，我想要行程列表畫面使用 MVI 架構，以獲得更穩定的 UI 體驗。

#### Acceptance Criteria

1. WHEN 定義 ItineraryListState THEN 系統應包含 itineraries、searchQuery、isLoading、error 等狀態
2. WHEN 定義 ItineraryListIntent THEN 系統應包含 LoadItineraries、Search、DeleteItinerary、Refresh 等意圖
3. WHEN 定義 ItineraryListEvent THEN 系統應包含 NavigateToDetail、NavigateToEdit、ShowDeleteConfirm 等事件
4. WHEN 用戶執行搜尋 THEN ViewModel 應處理 Search Intent 並更新 State
5. WHEN 刪除行程成功 THEN 系統應發送 Event 並重新載入列表

### Requirement 3: ItineraryDetailScreen MVI 重構

**User Story:** 作為用戶，我想要行程詳情畫面使用 MVI 架構，以便更清楚地管理複雜的狀態。

#### Acceptance Criteria

1. WHEN 定義 ItineraryDetailState THEN 系統應包含 itinerary、groupedItems、selectedDate、dateRange、isLoading、error 等狀態
2. WHEN 定義 ItineraryDetailIntent THEN 系統應包含 LoadItinerary、SelectDate、ToggleItemCompletion、DeleteItem 等意圖
3. WHEN 定義 ItineraryDetailEvent THEN 系統應包含 NavigateBack、NavigateToAddItem、NavigateToEditItem、ShowDeleteDialog 等事件
4. WHEN 用戶選擇日期 THEN ViewModel 應處理 SelectDate Intent 並更新 groupedItems
5. WHEN 切換項目完成狀態 THEN 系統應更新 State 並重新載入項目

### Requirement 4: AddEditItineraryScreen MVI 重構

**User Story:** 作為用戶，我想要新增/編輯行程畫面使用 MVI 架構，以便更好地管理表單狀態。

#### Acceptance Criteria

1. WHEN 定義 AddEditItineraryState THEN 系統應包含 title、description、startDate、endDate、errors、isLoading、showDraftSaved 等狀態
2. WHEN 定義 AddEditItineraryIntent THEN 系統應包含 UpdateTitle、UpdateDescription、UpdateStartDate、UpdateEndDate、Save、LoadDraft 等意圖
3. WHEN 定義 AddEditItineraryEvent THEN 系統應包含 NavigateBack、SaveSuccess、ShowDatePicker 等事件
4. WHEN 用戶輸入標題 THEN ViewModel 應處理 UpdateTitle Intent 並更新 State
5. WHEN 儲存成功 THEN 系統應發送 SaveSuccess Event 並導航回上一頁

### Requirement 5: AddEditItemScreen MVI 重構

**User Story:** 作為用戶，我想要新增/編輯項目畫面使用 MVI 架構，以便更好地管理複雜的表單驗證。

#### Acceptance Criteria

1. WHEN 定義 AddEditItemState THEN 系統應包含 activity、locationName、locationAddress、notes、selectedDate、selectedTime、errors、isLoading 等狀態
2. WHEN 定義 AddEditItemIntent THEN 系統應包含 UpdateActivity、UpdateLocation、UpdateDate、UpdateTime、Save 等意圖
3. WHEN 定義 AddEditItemEvent THEN 系統應包含 NavigateBack、SaveSuccess、ShowTimePicker 等事件
4. WHEN 用戶輸入活動名稱 THEN ViewModel 應處理 UpdateActivity Intent 並清除相關錯誤
5. WHEN 表單驗證失敗 THEN 系統應更新 State 中的 errors 而不發送 Event

### Requirement 6: TravelHistoryScreen MVI 重構

**User Story:** 作為用戶，我想要旅遊歷史畫面使用 MVI 架構，以便更好地管理過濾狀態。

#### Acceptance Criteria

1. WHEN 定義 TravelHistoryState THEN 系統應包含 historyByLocation、dateFilter、isLoading、error 等狀態
2. WHEN 定義 TravelHistoryIntent THEN 系統應包含 LoadHistory、FilterByDateRange、ClearFilter 等意圖
3. WHEN 定義 TravelHistoryEvent THEN 系統應包含 NavigateBack、ShowFilterDialog 等事件
4. WHEN 用戶套用日期過濾 THEN ViewModel 應處理 FilterByDateRange Intent 並更新 State
5. WHEN 清除過濾 THEN 系統應重置 dateFilter 並重新載入歷史

### Requirement 7: RouteViewScreen MVI 重構

**User Story:** 作為用戶，我想要路線檢視畫面使用 MVI 架構，以便更清楚地管理載入和匯出狀態。

#### Acceptance Criteria

1. WHEN 定義 RouteViewState THEN 系統應包含 route、isLoading、error、isExporting 等狀態
2. WHEN 定義 RouteViewIntent THEN 系統應包含 LoadRoute、ExportRoute 等意圖
3. WHEN 定義 RouteViewEvent THEN 系統應包含 NavigateBack、ExportSuccess、ShowExportDialog 等事件
4. WHEN 用戶匯出路線 THEN ViewModel 應處理 ExportRoute Intent 並更新 isExporting 狀態
5. WHEN 匯出成功 THEN 系統應發送 ExportSuccess Event 並傳遞匯出的 JSON

### Requirement 8: EditItemScreen MVI 重構

**User Story:** 作為用戶，我想要編輯項目畫面使用 MVI 架構，以便與新增項目畫面保持一致的架構。

#### Acceptance Criteria

1. WHEN 定義 EditItemState THEN 系統應包含與 AddEditItemState 相似的欄位，並額外包含 originalItem
2. WHEN 定義 EditItemIntent THEN 系統應包含 LoadItem、UpdateActivity、UpdateLocation、Save 等意圖
3. WHEN 定義 EditItemEvent THEN 系統應包含 NavigateBack、SaveSuccess、ShowTimePicker 等事件
4. WHEN 載入項目 THEN ViewModel 應處理 LoadItem Intent 並填充 State
5. WHEN 儲存成功 THEN 系統應發送 SaveSuccess Event 並導航回上一頁

### Requirement 9: 狀態持久化

**User Story:** 作為用戶，我想要在配置變更時保留 UI 狀態，以避免資料遺失。

#### Acceptance Criteria

1. WHEN 螢幕旋轉或配置變更 THEN 系統應保留當前的 State
2. WHEN ViewModel 被銷毀 THEN 系統應取消所有進行中的協程
3. WHEN 恢復 State THEN 系統應從 SavedStateHandle 讀取保存的狀態
4. WHEN State 包含大型物件 THEN 系統應只保存必要的識別資訊
5. WHEN 重新建立 ViewModel THEN 系統應使用保存的狀態初始化

### Requirement 10: 錯誤處理

**User Story:** 作為用戶，我想要清楚的錯誤訊息和恢復機制，以便在發生錯誤時知道如何處理。

#### Acceptance Criteria

1. WHEN 發生網路錯誤 THEN 系統應在 State 中設定 error 訊息
2. WHEN 發生驗證錯誤 THEN 系統應在 State 中設定欄位特定的錯誤
3. WHEN 用戶修正錯誤 THEN 系統應清除相關的錯誤訊息
4. WHEN 發生致命錯誤 THEN 系統應發送 ShowError Event
5. WHEN 錯誤可重試 THEN 系統應提供重試的 Intent

### Requirement 11: 測試支援

**User Story:** 作為開發者，我想要 MVI 架構易於測試，以確保程式碼品質。

#### Acceptance Criteria

1. WHEN 測試 ViewModel THEN 系統應允許注入假的 Repository
2. WHEN 測試 Intent 處理 THEN 系統應能驗證 State 的變化
3. WHEN 測試 Event 發送 THEN 系統應能收集和驗證 Event
4. WHEN 測試非同步操作 THEN 系統應使用 TestDispatcher
5. WHEN 測試 UI THEN 系統應能使用假的 State 進行 Preview

### Requirement 12: 向後相容

**User Story:** 作為開發者，我想要重構過程中保持應用程式可運行，以便逐步遷移。

#### Acceptance Criteria

1. WHEN 重構單一畫面 THEN 系統應不影響其他畫面的運作
2. WHEN 新舊架構共存 THEN 系統應能正常編譯和運行
3. WHEN 導航在新舊畫面間 THEN 系統應正常工作
4. WHEN 共享 Repository THEN 新舊 ViewModel 應能同時使用
5. WHEN 完成所有重構 THEN 系統應移除舊的 ViewModel 實作

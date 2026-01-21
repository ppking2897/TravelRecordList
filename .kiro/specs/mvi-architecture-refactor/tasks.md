# Implementation Plan - MVI 架構重構

## Phase 1: MVI 基礎架構

- [x] 1. 建立 MVI 核心介面和基礎類別


  - 建立 `ui/mvi/` 套件
  - 定義 UiState、UiIntent、UiEvent 標記介面
  - 實作 BaseViewModel 抽象類別
  - 實作 LoadingState sealed class
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ]* 1.1 撰寫 BaseViewModel 的 unit tests
  - 測試 State 更新機制
  - 測試 Event 發送機制
  - 測試 Intent 處理流程
  - _Requirements: 11.1, 11.2, 11.3_

- [ ]* 1.2 撰寫 Property 1: State 不可變性的 property test
  - **Property 1: State 不可變性**
  - **Validates: Requirements 1.4**

- [ ]* 1.3 撰寫 Property 2: Event 單次性的 property test
  - **Property 2: Event 單次性**
  - **Validates: Requirements 1.3**

- [ ]* 1.4 撰寫 Property 3: Intent 處理順序的 property test
  - **Property 3: Intent 處理順序**
  - **Validates: Requirements 1.2**

- [ ]* 1.5 撰寫 Property 4: State 一致性的 property test
  - **Property 4: State 一致性**
  - **Validates: Requirements 1.2**





- [ ] 2. 建立測試工具和輔助函數
  - 建立 TestDispatcher 設定
  - 建立 ViewModel 測試基礎類別
  - 建立假的 Repository 實作
  - 建立 State/Event 收集工具
  - _Requirements: 11.1, 11.4_


## Phase 2: RouteViewScreen MVI 重構



- [ ] 3. 實作 RouteViewScreen MVI 架構
  - 建立 RouteViewState data class


  - 建立 RouteViewIntent sealed class
  - 建立 RouteViewEvent sealed class
  - _Requirements: 7.1, 7.2, 7.3_

- [x] 3.1 實作 RouteViewViewModel

  - 繼承 BaseViewModel
  - 實作 processIntent 方法


  - 實作 LoadRoute Intent 處理
  - 實作 ExportRoute Intent 處理
  - _Requirements: 7.4, 7.5_

- [x]* 3.2 撰寫 RouteViewViewModel 的 unit tests


  - 測試 LoadRoute Intent
  - 測試 ExportRoute Intent
  - 測試錯誤處理

  - _Requirements: 11.1, 11.2_

- [x] 3.3 重構 RouteViewScreen UI

  - 更新 Screen 使用新的 ViewModel
  - 實作 Event 收集邏輯
  - 更新 Content 函數接收 State 和 onIntent

  - 更新 Preview 使用新的 State
  - _Requirements: 7.1, 7.2, 7.3_

- [x] 3.4 在 Koin 中註冊新的 ViewModel

  - 更新 AppModule.kt


  - 註冊 RouteViewViewModel
  - _Requirements: 12.2_

- [ ] 4. Checkpoint - 驗證 RouteViewScreen 重構
  - 確保編譯通過
  - 確保所有測試通過
  - 手動測試 UI 功能

## Phase 3: TravelHistoryScreen MVI 重構

- [x] 5. 實作 TravelHistoryScreen MVI 架構


  - 建立 TravelHistoryState data class
  - 建立 TravelHistoryIntent sealed class
  - 建立 TravelHistoryEvent sealed class
  - _Requirements: 6.1, 6.2, 6.3_



- [x] 5.1 實作 TravelHistoryViewModel

  - 繼承 BaseViewModel
  - 實作 LoadHistory Intent 處理
  - 實作 FilterByDateRange Intent 處理
  - 實作 ClearFilter Intent 處理
  - _Requirements: 6.4, 6.5_



- [ ]* 5.2 撰寫 TravelHistoryViewModel 的 unit tests
  - 測試 LoadHistory Intent
  - 測試 FilterByDateRange Intent
  - 測試 ClearFilter Intent

  - _Requirements: 11.1, 11.2_

- [ ] 5.3 重構 TravelHistoryScreen UI
  - 更新 Screen 使用新的 ViewModel
  - 實作 Event 收集邏輯

  - 更新 Content 函數
  - 更新 Preview
  - _Requirements: 6.1, 6.2, 6.3_

- [x] 5.4 在 Koin 中註冊 TravelHistoryViewModel


  - 更新 AppModule.kt
  - _Requirements: 12.2_

- [ ]* 5.5 撰寫 Property 7: 搜尋狀態同步的 property test
  - **Property 7: 搜尋狀態同步**
  - **Validates: Requirements 2.4**

- [ ] 6. Checkpoint - 驗證 TravelHistoryScreen 重構
  - 確保編譯通過
  - 確保所有測試通過
  - 手動測試 UI 功能

## Phase 4: ItineraryListScreen MVI 重構



- [ ] 7. 實作 ItineraryListScreen MVI 架構
  - 建立 ItineraryListState data class
  - 建立 ItineraryListIntent sealed class
  - 建立 ItineraryListEvent sealed class


  - _Requirements: 2.1, 2.2, 2.3_

- [x] 7.1 實作 ItineraryListViewModel


  - 繼承 BaseViewModel
  - 實作 LoadItineraries Intent 處理
  - 實作 Search Intent 處理
  - 實作 DeleteItinerary Intent 處理
  - 實作 Refresh Intent 處理
  - _Requirements: 2.4, 2.5_

- [ ]* 7.2 撰寫 ItineraryListViewModel 的 unit tests
  - 測試 LoadItineraries Intent
  - 測試 Search Intent
  - 測試 DeleteItinerary Intent
  - 測試錯誤處理
  - _Requirements: 11.1, 11.2_

- [ ]* 7.3 撰寫 Property 6: 載入狀態管理的 property test
  - **Property 6: 載入狀態管理**
  - **Validates: Requirements 2.4, 3.4**

- [ ] 7.4 重構 ItineraryListScreen UI
  - 更新 Screen 使用新的 ViewModel
  - 實作 Event 收集邏輯（導航、刪除確認）
  - 更新 Content 函數
  - 更新 Preview
  - _Requirements: 2.1, 2.2, 2.3_

- [ ] 7.5 在 Koin 中註冊 ItineraryListViewModel
  - 更新 AppModule.kt
  - 保留舊的 ViewModel（向後相容）
  - _Requirements: 12.1, 12.2_

- [ ] 8. Checkpoint - 驗證 ItineraryListScreen 重構
  - 確保編譯通過
  - 確保所有測試通過
  - 手動測試搜尋、刪除功能

## Phase 5: ItineraryDetailScreen MVI 重構

- [-] 9. 實作 ItineraryDetailScreen MVI 架構

  - 建立 ItineraryDetailState data class
  - 建立 ItineraryDetailIntent sealed class
  - 建立 ItineraryDetailEvent sealed class
  - _Requirements: 3.1, 3.2, 3.3_



- [ ] 9.1 實作 ItineraryDetailViewModel
  - 繼承 BaseViewModel
  - 實作 LoadItinerary Intent 處理
  - 實作 SelectDate Intent 處理
  - 實作 ToggleItemCompletion Intent 處理
  - 實作 DeleteItem Intent 處理
  - 實作 GenerateRoute Intent 處理
  - _Requirements: 3.4, 3.5_

- [ ]* 9.2 撰寫 ItineraryDetailViewModel 的 unit tests
  - 測試 LoadItinerary Intent
  - 測試 SelectDate Intent
  - 測試 ToggleItemCompletion Intent
  - 測試 DeleteItem Intent
  - _Requirements: 11.1, 11.2_

- [ ]* 9.3 撰寫 Property 8: 日期選擇一致性的 property test
  - **Property 8: 日期選擇一致性**
  - **Validates: Requirements 3.4**

- [ ] 9.4 重構 ItineraryDetailScreen UI
  - 更新 Screen 使用新的 ViewModel
  - 實作 Event 收集邏輯
  - 更新 Content 函數
  - 更新 Preview
  - _Requirements: 3.1, 3.2, 3.3_

- [ ] 9.5 在 Koin 中註冊 ItineraryDetailViewModel
  - 更新 AppModule.kt
  - _Requirements: 12.2_

- [ ] 10. Checkpoint - 驗證 ItineraryDetailScreen 重構
  - 確保編譯通過
  - 確保所有測試通過
  - 手動測試日期選擇、項目完成切換

## Phase 6: AddEditItineraryScreen MVI 重構

- [-] 11. 實作 AddEditItineraryScreen MVI 架構

  - 建立 AddEditItineraryState data class
  - 建立 AddEditItineraryIntent sealed class
  - 建立 AddEditItineraryEvent sealed class
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 11.1 實作 AddEditItineraryViewModel


  - 繼承 BaseViewModel
  - 實作 LoadItinerary Intent 處理（編輯模式）
  - 實作 LoadDraft Intent 處理
  - 實作 UpdateTitle/Description/Dates Intent 處理
  - 實作 Save Intent 處理（含驗證）
  - 實作 SaveDraft Intent 處理
  - _Requirements: 4.4, 4.5_

- [ ]* 11.2 撰寫 AddEditItineraryViewModel 的 unit tests
  - 測試表單更新 Intent
  - 測試驗證邏輯
  - 測試 Save Intent
  - 測試草稿功能
  - _Requirements: 11.1, 11.2_

- [ ]* 11.3 撰寫 Property 5: 錯誤狀態清除的 property test
  - **Property 5: 錯誤狀態清除**
  - **Validates: Requirements 10.3**

- [ ]* 11.4 撰寫 Property 9: 表單驗證一致性的 property test
  - **Property 9: 表單驗證一致性**
  - **Validates: Requirements 4.4, 5.4**

- [ ] 11.5 重構 AddEditItineraryScreen UI
  - 更新 Screen 使用新的 ViewModel
  - 實作 Event 收集邏輯
  - 更新 Content 函數
  - 更新 Preview（多個狀態變體）
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 11.6 在 Koin 中註冊 AddEditItineraryViewModel


  - 更新 AppModule.kt
  - _Requirements: 12.2_

- [ ] 12. Checkpoint - 驗證 AddEditItineraryScreen 重構
  - 確保編譯通過
  - 確保所有測試通過
  - 手動測試新增、編輯、驗證、草稿功能

## Phase 7: AddEditItemScreen MVI 重構

- [-] 13. 實作 AddEditItemScreen MVI 架構

  - 建立 AddEditItemState data class
  - 建立 AddEditItemIntent sealed class
  - 建立 AddEditItemEvent sealed class
  - _Requirements: 5.1, 5.2, 5.3_

- [x] 13.1 實作 AddEditItemViewModel


  - 繼承 BaseViewModel
  - 實作表單欄位更新 Intent 處理
  - 實作 Save Intent 處理（含驗證）
  - 實作錯誤清除邏輯
  - _Requirements: 5.4, 5.5_

- [ ]* 13.2 撰寫 AddEditItemViewModel 的 unit tests
  - 測試表單更新 Intent
  - 測試驗證邏輯
  - 測試 Save Intent
  - _Requirements: 11.1, 11.2_

- [x] 13.3 重構 AddEditItemScreen UI

  - 更新 Screen 使用新的 ViewModel
  - 實作 Event 收集邏輯
  - 更新 Content 函數
  - 更新 Preview
  - _Requirements: 5.1, 5.2, 5.3_

- [x] 13.4 在 Koin 中註冊 AddEditItemViewModel

  - 更新 AppModule.kt
  - _Requirements: 12.2_

## Phase 8: EditItemScreen MVI 重構

- [-] 14. 實作 EditItemScreen MVI 架構

  - 建立 EditItemState data class
  - 建立 EditItemIntent sealed class
  - 建立 EditItemEvent sealed class
  - _Requirements: 8.1, 8.2, 8.3_

- [x] 14.1 實作 EditItemViewModel


  - 繼承 BaseViewModel
  - 實作 LoadItem Intent 處理
  - 實作表單欄位更新 Intent 處理
  - 實作 Save Intent 處理
  - _Requirements: 8.4, 8.5_

- [ ]* 14.2 撰寫 EditItemViewModel 的 unit tests
  - 測試 LoadItem Intent
  - 測試表單更新 Intent
  - 測試 Save Intent
  - _Requirements: 11.1, 11.2_

- [ ] 14.3 重構 EditItemScreen UI
  - 更新 Screen 使用新的 ViewModel
  - 實作 Event 收集邏輯
  - 更新 Content 函數
  - 更新 Preview
  - _Requirements: 8.1, 8.2, 8.3_

- [ ] 14.4 在 Koin 中註冊 EditItemViewModel


  - 更新 AppModule.kt
  - _Requirements: 12.2_

- [ ] 15. Checkpoint - 驗證所有表單畫面重構
  - 確保編譯通過
  - 確保所有測試通過
  - 手動測試所有表單功能

## Phase 9: 狀態持久化和配置變更處理

- [ ] 16. 實作 SavedStateHandle 支援
  - 更新 BaseViewModel 支援 SavedStateHandle
  - 為需要的 ViewModel 添加狀態保存邏輯
  - 實作狀態恢復邏輯
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ]* 16.1 撰寫 Property 10: 配置變更恢復的 property test
  - **Property 10: 配置變更恢復**
  - **Validates: Requirements 9.1**

- [ ]* 16.2 撰寫配置變更的 unit tests
  - 測試 State 保存
  - 測試 State 恢復
  - 測試協程取消
  - _Requirements: 11.1, 11.4_

## Phase 10: 清理和優化

- [ ] 17. 移除舊的 ViewModel 實作
  - 刪除舊的 ItineraryListViewModel
  - 刪除舊的 ItineraryDetailViewModel
  - 刪除舊的 TravelHistoryViewModel
  - 更新 Koin 配置移除舊的註冊
  - _Requirements: 12.5_

- [ ] 17.1 重新命名新的 ViewModel
  - 移除 MVI 後綴（如果有）
  - 確保所有引用都已更新
  - _Requirements: 12.5_

- [ ] 18. 統一錯誤處理
  - 建立統一的錯誤處理擴充函數
  - 更新所有 ViewModel 使用統一的錯誤處理
  - 建立錯誤訊息資源
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 19. 效能優化
  - 檢查並優化 State 更新頻率
  - 添加 distinctUntilChanged 到必要的 Flow
  - 優化 Event 處理的背壓
  - 檢查記憶體洩漏
  - _Requirements: 1.5_

- [ ]* 20. 完善測試覆蓋
  - 確保所有 ViewModel 都有 unit tests
  - 確保所有 Property tests 都已實作
  - 添加整合測試
  - 檢查測試覆蓋率
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 21. 更新文件
  - 更新 tech.md 添加 MVI 架構說明
  - 更新 structure.md 添加 MVI 套件結構
  - 建立 MVI 最佳實踐文件
  - _Requirements: All_

- [ ] 22. Final Checkpoint - 完整驗證
  - 確保所有編譯通過
  - 確保所有測試通過
  - 手動測試所有功能
  - 效能測試
  - 記憶體測試

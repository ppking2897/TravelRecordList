# Implementation Plan

- [x] 1. 設定專案結構和核心 interfaces





  - 建立 data、domain、ui、util 模組的目錄結構
  - 定義 Repository interfaces 和 StorageService interface
  - 設定 kotlinx-datetime 和 kotlinx-serialization dependencies
  - _Requirements: 1.1, 2.1_

- [ ]* 1.1 設定 Kotest Property Testing framework
  - 添加 Kotest dependencies 到 build.gradle.kts
  - 配置 property testing 運行至少 100 次迭代
  - _Requirements: All_

- [x] 2. 實作 data models 和 validation





  - 建立 Itinerary、ItineraryItem、Location、Route data classes
  - 實作 validation functions 用於 title、date range、location name
  - 實作 TravelAppError sealed class
  - _Requirements: 1.2, 1.5, 2.2_

- [ ]* 2.1 撰寫 Property 1: Itinerary 建立完整性的 property test
  - **Property 1: Itinerary 建立完整性**
  - **Validates: Requirements 1.1**

- [ ]* 2.2 撰寫 Property 2: Title 驗證的 property test
  - **Property 2: Title 驗證**
  - **Validates: Requirements 1.2**

- [ ]* 2.3 撰寫 Property 3: 新 itinerary 初始化的 property test
  - **Property 3: 新 itinerary 初始化**
  - **Validates: Requirements 1.3**

- [ ]* 2.4 撰寫 Property 5: Date range 驗證的 property test
  - **Property 5: Date range 驗證**
  - **Validates: Requirements 1.5**

- [ ]* 2.5 撰寫 Property 7: Location name 驗證的 property test
  - **Property 7: Location name 驗證**
  - **Validates: Requirements 2.2**

- [x] 3. 實作 platform-specific StorageService





  - 為 Android 實作 StorageService（使用 DataStore）
  - 為 iOS 實作 StorageService（使用 UserDefaults）
  - 為 JS/Web 實作 StorageService（使用 LocalStorage）
  - 實作 JSON serialization/deserialization
  - _Requirements: 1.4, 3.5_

- [ ]* 3.1 撰寫 Property 4: Itinerary persistence round-trip 的 property test
  - **Property 4: Itinerary persistence round-trip**
  - **Validates: Requirements 1.4**

- [ ]* 3.2 撰寫 Property 27: Route serialization round-trip 的 property test
  - **Property 27: Route serialization round-trip**
  - **Validates: Requirements 7.5**

- [x] 4. 實作 ItineraryRepository



  - 實作 createItinerary、getItinerary、getAllItineraries functions
  - 實作 updateItinerary、deleteItinerary functions
  - 實作 searchItineraries function（multi-field search）
  - 實作 itinerary 按 createdAt 排序邏輯
  - _Requirements: 1.1, 1.4, 4.1, 8.1_

- [ ]* 4.1 撰寫 Property 14: Itinerary list 排序的 property test
  - **Property 14: Itinerary list 排序**
  - **Validates: Requirements 4.1**

- [ ]* 4.2 撰寫 Property 28: Multi-field search 的 property test
  - **Property 28: Multi-field search**
  - **Validates: Requirements 8.1**

- [ ]* 4.3 撰寫 Property 29: 每個結果單一 itinerary 的 property test
  - **Property 29: 每個結果單一 itinerary**
  - **Validates: Requirements 8.4**

- [x] 5. 實作 ItineraryItemRepository



  - 實作 addItem、updateItem、deleteItem functions
  - 實作 getItemsByItinerary function 並按時間順序排序
  - 實作 getItemsByLocation function
  - 實作 getItemsByDateRange function
  - _Requirements: 2.1, 2.3, 3.1, 3.2, 6.3, 6.5_

- [ ]* 5.1 撰寫 Property 6: Item 建立完整性的 property test
  - **Property 6: Item 建立完整性**
  - **Validates: Requirements 2.1**

- [ ]* 5.2 撰寫 Property 8: 時間順序排序不變性的 property test
  - **Property 8: 時間順序排序不變性**
  - **Validates: Requirements 2.3, 4.3**

- [ ]* 5.3 撰寫 Property 9: 唯一 item identifiers 的 property test
  - **Property 9: 唯一 item identifiers**
  - **Validates: Requirements 2.5**

- [ ]* 5.4 撰寫 Property 10: Modification timestamp 更新的 property test
  - **Property 10: Modification timestamp 更新**
  - **Validates: Requirements 3.1**

- [ ]* 5.5 撰寫 Property 11: Item deletion round-trip 的 property test
  - **Property 11: Item deletion round-trip**
  - **Validates: Requirements 3.2**

- [ ]* 5.6 撰寫 Property 12: 刪除保持排序的 property test
  - **Property 12: 刪除保持排序**
  - **Validates: Requirements 3.3**

- [ ]* 5.7 撰寫 Property 13: Modification persistence 的 property test
  - **Property 13: Modification persistence**
  - **Validates: Requirements 3.5**

- [ ]* 5.8 撰寫 Property 21: Date range 過濾的 property test
  - **Property 21: Date range 過濾**
  - **Validates: Requirements 6.3**

- [ ]* 5.9 撰寫 Property 22: 基於 location 的 item 取回的 property test
  - **Property 22: 基於 location 的 item 取回**
  - **Validates: Requirements 6.5**

- [ ]* 5.10 撰寫 Property 30: 基於日期的 search 的 property test
  - **Property 30: 基於日期的 search**
  - **Validates: Requirements 8.5**

- [x] 6. 實作 completion tracking 功能


  - 在 ItineraryItemRepository 中實作 toggleCompletion function
  - 實作 progress calculation logic
  - 確保 completedAt timestamp 正確設定
  - _Requirements: 5.1, 5.2, 5.4, 5.5_

- [ ]* 6.1 撰寫 Property 16: Completion status 更新的 property test
  - **Property 16: Completion status 更新**
  - **Validates: Requirements 5.1**

- [ ]* 6.2 撰寫 Property 17: 已完成的 items 保留在 itinerary 中的 property test
  - **Property 17: 已完成的 items 保留在 itinerary 中**
  - **Validates: Requirements 5.2**

- [ ]* 6.3 撰寫 Property 18: Completion toggle persistence 的 property test
  - **Property 18: Completion toggle persistence**
  - **Validates: Requirements 5.4**

- [ ]* 6.4 撰寫 Property 19: Progress 計算準確性的 property test
  - **Property 19: Progress 計算準確性**
  - **Validates: Requirements 5.5**



- [x] 7. 實作 RouteRepository 和 route generation



  - 實作 createRoute function（從 itinerary 生成 route）
  - 實作 route validation（至少兩個 unique locations）
  - 實作 getRoute 和 exportRoute functions
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ]* 7.1 撰寫 Property 23: 從 itinerary 生成 route 的 property test
  - **Property 23: 從 itinerary 生成 route**
  - **Validates: Requirements 7.1**

- [ ]* 7.2 撰寫 Property 24: Route data 完整性的 property test
  - **Property 24: Route data 完整性**
  - **Validates: Requirements 7.2**

- [ ]* 7.3 撰寫 Property 25: Route generation 驗證的 property test
  - **Property 25: Route generation 驗證**
  - **Validates: Requirements 7.3**

- [ ]* 7.4 撰寫 Property 26: Route unique identifier 的 property test
  - **Property 26: Route unique identifier**



  - **Validates: Requirements 7.4**

- [x] 8. 實作 Use Cases



  - 實作 CreateItineraryUseCase
  - 實作 AddItineraryItemUseCase
  - 實作 UpdateItineraryItemUseCase 和 DeleteItineraryItemUseCase
  - 實作 GetTravelHistoryUseCase（按 location 分組）
  - 實作 CreateRouteFromItineraryUseCase
  - 實作 SearchItinerariesUseCase
  - _Requirements: 1.1, 2.1, 3.1, 3.2, 6.1, 7.1, 8.1_

- [ ]* 8.1 撰寫 Property 20: History 按 location 分組的 property test
  - **Property 20: History 按 location 分組**
  - **Validates: Requirements 6.1**

- [x] 9. 實作 photo management


  - 在 ItineraryItem 中實作 photo reference 管理
  - 實作 addPhotoReference 和 removePhotoReference functions
  - 實作刪除 item 時清理 photo references 的邏輯
  - _Requirements: 10.1, 10.4_

- [ ]* 9.1 撰寫 Property 33: Photo reference storage 的 property test
  - **Property 33: Photo reference storage**
  - **Validates: Requirements 10.1**

- [ ]* 9.2 撰寫 Property 34: Photo reference 清理的 property test
  - **Property 34: Photo reference 清理**
  - **Validates: Requirements 10.4**

- [x] 10. 實作 offline support



  - 實作 offline data modification 邏輯
  - 實作 conflict resolution（基於 timestamp）
  - 實作 sync markers 和 pending changes tracking
  - _Requirements: 9.1, 9.2, 9.4_

- [ ]* 10.1 撰寫 Property 31: Offline data modification 的 property test
  - **Property 31: Offline data modification**
  - **Validates: Requirements 9.2**

- [ ]* 10.2 撰寫 Property 32: 基於 timestamp 的衝突解決的 property test
  - **Property 32: 基於 timestamp 的衝突解決**
  - **Validates: Requirements 9.4**

- [ ] 11. Checkpoint - 確保所有測試通過
  - 確保所有測試通過，如有問題請詢問使用者

- [x] 12. 實作 ViewModels



  - 實作 ItineraryListViewModel（包含 search 功能）
  - 實作 ItineraryDetailViewModel（包含 progress tracking）
  - 實作 TravelHistoryViewModel（包含 date filter）
  - 使用 StateFlow 管理 UI state
  - _Requirements: 4.1, 4.2, 5.3, 5.5, 6.1, 8.1_

- [ ]* 12.1 撰寫 ViewModels 的 unit tests
  - 測試 state updates
  - 測試 error handling
  - 測試 loading states

- [x] 13. 實作 ItineraryListScreen UI


  - 建立 itinerary list 顯示（使用 LazyColumn）
  - 實作 search bar
  - 實作 empty state UI
  - 實作導航到 detail screen
  - _Requirements: 4.1, 4.2, 4.5, 8.1_

- [ ]* 13.1 撰寫 Property 15: Display format 完整性的 property test
  - **Property 15: Display format 完整性**
  - **Validates: Requirements 4.2, 4.4**

- [x] 14. 實作 AddEditItineraryScreen UI


  - 建立 form fields（title、description、dates）
  - 實作 date pickers
  - 實作 validation 和 error display
  - 實作 save 和 cancel actions
  - _Requirements: 1.1, 1.2, 1.5_

- [x] 15. 實作 ItineraryDetailScreen UI


  - 顯示 itinerary 資訊和 progress bar
  - 顯示 items list（按時間順序）
  - 實作 completion toggle
  - 實作導航到 add/edit item screen
  - 實作 route generation button
  - _Requirements: 4.3, 4.4, 5.3, 5.5, 7.1_

- [x] 16. 實作 AddEditItemScreen UI


  - 建立 form fields（date、time、location、activity、notes）
  - 實作 date 和 time pickers
  - 實作 photo attachment UI
  - 實作 validation 和 error display
  - _Requirements: 2.1, 2.2, 10.1_

- [x] 17. 實作 TravelHistoryScreen UI



  - 顯示按 location 分組的 items
  - 實作 date range filter
  - 實作 location selection 和 detail view
  - _Requirements: 6.1, 6.2, 6.3, 6.5_

- [x] 18. 實作 RouteViewScreen UI



  - 顯示 route 資訊和 locations list
  - 實作 export route 功能
  - 顯示每個 location 的 recommended duration
  - _Requirements: 7.1, 7.2, 7.5_

- [x] 19. 實作 navigation



  - 設定 Compose Navigation
  - 定義所有 screen routes
  - 實作 screen transitions
  - 處理 back navigation

- [ ] 20. 實作 platform-specific photo storage
  - 為 Android 實作 photo storage（使用 MediaStore）
  - 為 iOS 實作 photo storage（使用 Photos Framework）
  - 為 Web 實作 photo storage（使用 IndexedDB）
  - 實作 photo compression
  - _Requirements: 10.1, 10.5_

- [ ] 21. 效能優化
  - 實作 lazy loading 用於 item details
  - 實作 caching 機制
  - 優化 list rendering（LazyColumn）
  - 實作 batch operations 用於 storage
  - _Requirements: All_

- [ ] 22. Final Checkpoint - 確保所有測試通過
  - 確保所有測試通過，如有問題請詢問使用者

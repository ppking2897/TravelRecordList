# Implementation Plan - 行程 CRUD 增強

- [ ] 1. 建立刪除確認 Dialog 元件
- [x] 1.1 建立 DeleteConfirmDialog composable


  - 實作 AlertDialog 包含標題、訊息、確認和取消按鈕
  - 使用 Material 3 設計規範
  - 刪除按鈕使用 error color
  - _Requirements: 2.1, 2.2, 3.1, 3.2_



- [ ] 2. 更新行程列表畫面
- [ ] 2.1 在 ItineraryCard 添加編輯和刪除按鈕
  - 在卡片右上角添加 IconButton (Edit 和 Delete)
  - 編輯按鈕使用 primary color
  - 刪除按鈕使用 error color


  - 確保按鈕不觸發卡片點擊事件
  - _Requirements: 5.1, 5.2, 5.3, 5.5_

- [ ] 2.2 實作刪除確認流程
  - 添加 showDeleteDialog 狀態

  - 添加 itineraryToDelete 狀態
  - 點擊刪除按鈕時顯示 DeleteConfirmDialog
  - 確認後調用 ViewModel 的 deleteItinerary 方法
  - _Requirements: 2.1, 2.3, 2.4_



- [ ] 2.3 更新 ItineraryListScreen 參數
  - 添加 onEditClick: (String) -> Unit 參數
  - 傳遞給 ItineraryCard
  - _Requirements: 5.1_

- [ ] 3. 更新 ItineraryListViewModel
- [ ] 3.1 添加 deleteItinerary 方法
  - 調用 repository.deleteItinerary
  - 成功後重新載入列表


  - 失敗時設定錯誤訊息
  - _Requirements: 2.3, 2.5_

- [ ]* 3.2 撰寫 property test 驗證刪除後列表更新
  - **Property 1: 刪除行程同時刪除項目**
  - **Validates: Requirements 8.1**

- [ ] 4. 建立 UpdateItineraryUseCase
- [x] 4.1 實作 UpdateItineraryUseCase


  - 驗證輸入資料
  - 更新 modifiedAt 時間戳記
  - 調用 repository.updateItinerary
  - _Requirements: 1.3_

- [ ]* 4.2 撰寫 property test 驗證更新後資料一致性
  - **Property 2: 編輯後資料一致性**
  - **Validates: Requirements 1.3**



- [ ] 5. 建立 DeleteItineraryUseCase
- [ ] 5.1 實作 DeleteItineraryUseCase
  - 刪除行程
  - 級聯刪除所有相關項目


  - 處理錯誤情況
  - _Requirements: 8.1_



- [ ]* 5.2 撰寫 property test 驗證級聯刪除
  - **Property 1: 刪除行程同時刪除項目**
  - **Validates: Requirements 8.1**



- [ ] 6. 更新 AddEditItineraryScreen 支援編輯模式
- [ ] 6.1 添加 itineraryId 參數支援編輯模式
  - 添加 itineraryId: String? 參數


  - 添加 updateItineraryUseCase: UpdateItineraryUseCase? 參數
  - 根據 itineraryId 是否為 null 判斷新增或編輯模式
  - _Requirements: 1.1_


- [ ] 6.2 實作資料預填邏輯
  - 使用 LaunchedEffect 載入現有行程資料
  - 預填 title, description, startDate, endDate
  - _Requirements: 1.1_



- [ ] 6.3 更新儲存邏輯
  - 編輯模式使用 updateItineraryUseCase
  - 新增模式使用 createItineraryUseCase

  - _Requirements: 1.3_

- [ ] 6.4 更新 Top Bar 標題
  - 新增模式顯示「新增行程」

  - 編輯模式顯示「編輯行程」
  - _Requirements: 1.1_

- [ ] 7. 更新導航邏輯
- [ ] 7.1 在 App.kt 添加編輯行程路由
  - 添加 EditItinerary composable 路由


  - 傳遞 itineraryId 參數
  - 注入 updateItineraryUseCase
  - _Requirements: 1.1_

- [x] 7.2 更新 ItineraryListScreen 導航

  - 實作 onEditClick 導航到編輯畫面
  - _Requirements: 5.1_

- [ ] 8. 建立 EditItemScreen
- [x] 8.1 建立 EditItemScreen composable


  - 接收 item 和 itinerary 參數
  - 預填現有項目資料
  - 使用 UpdateItineraryItemUseCase 儲存

  - _Requirements: 7.1, 7.2_

- [ ] 8.2 實作驗證邏輯
  - 驗證必填欄位


  - 驗證日期在行程範圍內
  - _Requirements: 7.3, 7.4_

- [x] 8.3 在 App.kt 添加編輯項目路由


  - 添加 EditItem composable 路由
  - 傳遞 itemId 參數
  - 載入 item 和 itinerary 資料
  - _Requirements: 7.1_



- [ ] 9. 更新 ItineraryDetailScreen 添加刪除確認
- [x] 9.1 在 ItemCard 添加刪除確認


  - 添加 showDeleteDialog 狀態
  - 添加 itemToDelete 狀態
  - 點擊刪除按鈕時顯示 DeleteConfirmDialog


  - 確認後調用 ViewModel 的 deleteItem 方法
  - _Requirements: 3.1, 3.3, 3.4_

- [ ] 9.2 添加行程操作選單
  - 在 Top Bar 添加 MoreVert 圖示按鈕


  - 點擊顯示 DropdownMenu
  - 選單包含「編輯行程」和「刪除行程」選項
  - _Requirements: 6.1, 6.2_


- [ ] 9.3 實作行程刪除確認
  - 點擊「刪除行程」顯示 DeleteConfirmDialog
  - 確認後刪除行程並返回列表

  - _Requirements: 6.4, 6.5_

- [x] 9.4 實作行程編輯導航

  - 點擊「編輯行程」導航到編輯畫面
  - _Requirements: 6.3_

- [ ] 10. 建立草稿功能 (Draft)
- [ ] 10.1 建立 Draft data model
  - 定義 Draft data class
  - 定義 DraftType enum
  - 添加序列化支援
  - _Requirements: 4.1_

- [ ] 10.2 建立 DraftRepository
  - 定義 DraftRepository interface
  - 實作 DraftRepositoryImpl
  - 使用 StorageService 儲存草稿
  - _Requirements: 4.1, 4.2_

- [ ] 10.3 建立 SaveDraftUseCase
  - 實作草稿儲存邏輯


  - 序列化資料為 Map


  - _Requirements: 4.1_

- [ ] 10.4 建立 LoadDraftUseCase
  - 實作草稿載入邏輯
  - 反序列化 Map 為資料
  - _Requirements: 4.2_

- [ ]* 10.5 撰寫 property test 驗證草稿暫存和恢復
  - **Property 3: 草稿暫存和恢復**
  - **Validates: Requirements 4.1, 4.2**



- [x] 11. 整合草稿功能到 AddEditItineraryScreen

- [ ] 11.1 添加自動儲存邏輯
  - 使用 LaunchedEffect 監聽輸入變更
  - 使用 debounce 延遲 500ms 後儲存
  - _Requirements: 4.1_


- [ ] 11.2 添加草稿載入邏輯
  - 在畫面初始化時載入草稿
  - 僅在新增模式載入草稿

  - _Requirements: 4.2_

- [ ] 11.3 添加草稿清除邏輯
  - 成功儲存後清除草稿
  - _Requirements: 4.3_


- [ ] 11.4 添加草稿指示器 UI
  - 顯示「已自動儲存草稿」訊息



  - 使用淡入淡出動畫
  - 3 秒後自動消失
  - _Requirements: 4.1_

- [ ] 12. 更新 Koin 依賴注入
- [ ] 12.1 註冊新的 Use Cases
  - UpdateItineraryUseCase
  - DeleteItineraryUseCase
  - SaveDraftUseCase
  - LoadDraftUseCase
  - _Requirements: All_

- [ ] 12.2 註冊 DraftRepository
  - DraftRepository 和 DraftRepositoryImpl
  - _Requirements: 4.1, 4.2_

- [ ] 13. 測試和驗證
- [ ] 13.1 執行所有 property tests
  - 確保所有測試通過
  - 修正發現的問題

- [ ] 13.2 手動測試編輯流程
  - 測試行程編輯
  - 測試項目編輯
  - 驗證資料正確更新

- [ ] 13.3 手動測試刪除流程
  - 測試行程刪除確認
  - 測試項目刪除確認
  - 驗證級聯刪除

- [ ] 13.4 手動測試草稿功能
  - 測試自動儲存
  - 測試草稿恢復
  - 測試草稿清除

- [ ] 14. 文檔更新
- [ ] 14.1 更新程式碼註解
  - 添加中文註解說明新功能
  - 更新相關文件

- [ ] 14.2 建立完成報告
  - 記錄實作的功能
  - 記錄測試結果

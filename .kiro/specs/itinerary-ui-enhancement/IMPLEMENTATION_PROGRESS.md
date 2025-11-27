# 行程 UI 增強 - 實作進度報告

## 已完成的任務

### 1. Data Models 和 Extensions (✅ 完成)

#### 1.1 Itinerary Model 更新
- ✅ 已在 `Itinerary.kt` 添加 `startDate: LocalDate?` 和 `endDate: LocalDate?` 欄位
- ✅ 序列化邏輯已支援新欄位

#### 1.2 Property Tests - 日期範圍驗證
- ✅ 創建 `ItineraryPropertyTest.kt`
- ✅ 實作 Property 1: 日期範圍驗證
- ✅ 驗證 endDate 不能早於 startDate
- **Validates: 需求 1.3**

#### 1.3 日期範圍 Extension Functions
- ✅ 已在 `DateExtensions.kt` 實作
- ✅ `ClosedRange<LocalDate>.toDateList()` - 生成日期列表
- ✅ `LocalDate.toFriendlyString()` - 格式化顯示（例如："2024-01-15 (週一)"）
- ✅ `LocalDate.toShortString()` - 簡短格式
- ✅ `LocalDate.isInRange()` - 檢查日期是否在範圍內
- **Validates: 需求 2.2, 5.2, 5.5**

#### 1.4 Property Tests - 日期範圍生成
- ✅ 創建 `DateExtensionsPropertyTest.kt`
- ✅ 實作 Property 2: 日期範圍生成完整性
- ✅ 實作 Property 5: 日期格式化一致性
- ✅ 驗證日期列表包含所有日期且無間隙
- **Validates: 需求 2.2, 5.2, 5.5**

### 2. Use Cases (✅ 完成)

#### 2.1 CreateItineraryUseCase
- ✅ 已支援 startDate 和 endDate 參數
- ✅ 使用 `Validation.validateDateRange()` 進行驗證
- **Validates: 需求 1.3, 1.4**

#### 2.2 GroupItemsByDateUseCase
- ✅ 創建 `GroupItemsByDateUseCase.kt`
- ✅ 實作項目按日期分組邏輯
- ✅ 每組內按時間排序（無時間的項目排在最後）
- ✅ 創建 `ItemsByDate` 資料類別
- **Validates: 需求 3.3, 3.4**

#### 2.3 FilterItemsByDateUseCase
- ✅ 創建 `FilterItemsByDateUseCase.kt`
- ✅ 實作按日期篩選邏輯
- ✅ 支援 null 日期（顯示所有項目）
- **Validates: 需求 2.3, 6.1**

#### 2.4 Property Tests - 項目分組
- ✅ 創建 `GroupItemsByDateUseCasePropertyTest.kt`
- ✅ 實作 Property 4: 項目分組保持順序
- ✅ 驗證時間順序、日期順序、項目完整性
- **Validates: 需求 3.3, 3.4**

#### 2.5 Property Tests - 日期篩選
- ✅ 創建 `FilterItemsByDateUseCasePropertyTest.kt`
- ✅ 實作 Property 3: 日期篩選正確性
- ✅ 驗證篩選結果只包含匹配的日期
- **Validates: 需求 2.3, 6.1**

### 3. ViewModels (✅ 完成)

#### 3.1 ItineraryDetailViewModel 更新
- ✅ 添加 `selectedDate: StateFlow<LocalDate?>` 狀態
- ✅ 添加 `dateRange: StateFlow<ClosedRange<LocalDate>?>` 狀態
- ✅ 添加 `groupedItems: StateFlow<List<ItemsByDate>>` 狀態
- ✅ 添加 `filteredItems: StateFlow<List<ItineraryItem>>` 狀態
- ✅ 實作 `selectDate(date: LocalDate?)` 方法
- ✅ 實作 `updateGroupedAndFilteredItems()` 邏輯
- ✅ 注入 `GroupItemsByDateUseCase` 和 `FilterItemsByDateUseCase`
- **Validates: 需求 2.3, 3.3, 6.1**

### 4. Dependency Injection (✅ 完成)

#### 4.1 AppModule 更新
- ✅ 註冊 `GroupItemsByDateUseCase`
- ✅ 註冊 `FilterItemsByDateUseCase`
- ✅ 更新 `ItineraryDetailViewModel` 的依賴注入

## 待完成的任務

### 4. UI Components (⏳ 待實作)

#### 4.1 DateTabsRow Component
- [ ] 實作水平滾動的日期 tabs
- [ ] 添加「全部」tab
- [ ] 實作 tab 選擇和高亮邏輯
- **需求: 2.1, 2.2, 2.3, 6.2**

#### 4.2 ItemCard Component
- [ ] 顯示活動名稱、地點、時間
- [ ] 添加完成狀態 checkbox
- [ ] 添加刪除按鈕
- **需求: 3.1, 3.2**

#### 4.3 DateDropdown Component
- [ ] 實作下拉選單顯示日期範圍
- [ ] 實作日期格式化顯示
- [ ] 添加日期選擇邏輯
- **需求: 5.1, 5.2, 5.3, 5.5**

#### 4.4 Property Tests - 日期格式化
- [ ] 驗證日期格式化一致性
- **Validates: 需求 5.5**

#### 4.5 EmptyState Component
- [ ] 顯示空狀態圖示和訊息
- [ ] 添加新增提示
- **需求: 7.1, 7.2**

### 5. Screen 更新 (⏳ 待實作)

#### 5.1 AddEditItineraryScreen
- [ ] 添加開始日期 date picker
- [ ] 添加結束日期 date picker
- [ ] 實作日期範圍驗證
- [ ] 更新儲存邏輯包含日期範圍
- **需求: 1.1, 1.2, 1.3, 1.5**

#### 5.2 AddEditItemScreen
- [ ] 將日期輸入改為 DateDropdown
- [ ] 從 itinerary 獲取日期範圍
- [ ] 實作日期必填驗證
- **需求: 5.1, 5.2, 5.3, 5.4**

#### 5.3 ItineraryDetailScreen
- [ ] 添加 DateTabsRow 到頂部
- [ ] 更新項目列表使用 ItemCard
- [ ] 實作按日期分組顯示
- [ ] 添加 FAB button
- [ ] 實作空狀態顯示
- **需求: 2.1, 3.1, 4.2, 7.1**

#### 5.4 ItineraryListScreen
- [ ] 添加 FAB button
- [ ] 更新導航邏輯
- **需求: 4.1, 4.3**

### 6. 日期篩選功能 (⏳ 待實作)

#### 6.1 連接 DateTabsRow 和 ViewModel
- [ ] 實作 tab 點擊事件處理
- [ ] 更新 ViewModel 狀態
- [ ] 觸發項目列表重新篩選
- **需求: 6.1, 6.2**

#### 6.2 實作篩選後的顯示邏輯
- [ ] 顯示選中日期的項目
- [ ] 顯示空狀態（如果該日期無項目）
- [ ] 顯示所有項目（如果選擇「全部」）
- **需求: 6.3, 6.4, 6.5**

### 7. 狀態管理 (⏳ 待實作)

#### 7.1 實作空狀態檢測
- [ ] 檢測行程是否有項目
- [ ] 切換顯示內容或空狀態
- **需求: 7.1, 7.4, 7.5**

#### 7.2 Property Tests - 空狀態轉換
- [ ] 驗證空狀態轉換邏輯
- **Validates: 需求 7.5**

#### 7.3 實作螢幕方向改變時的狀態保存
- [ ] 保存選中的日期
- [ ] 保存滾動位置
- **需求: 8.4**

### 8. 效能優化 (⏳ 待實作)

#### 8.1 實作 lazy loading
- [ ] 使用 `LazyColumn` 顯示項目
- [ ] 使用 `LazyRow` 顯示日期 tabs
- **需求: 8.1**

#### 8.2 實作分組資料快取
- [ ] 使用 `remember` 快取分組結果
- [ ] 使用 `derivedStateOf` 計算篩選結果
- **需求: 8.3**

### 9. 測試和修正 (⏳ 待實作)

#### 9.1 執行所有 unit tests
- [ ] 確保所有測試通過
- [ ] 修正發現的問題

#### 9.2 執行 UI 測試
- [ ] 測試所有畫面的 UI 互動
- [ ] 驗證導航流程

#### 9.3 手動測試完整流程
- [ ] 建立行程並設定日期範圍
- [ ] 新增多個項目到不同日期
- [ ] 測試日期 tabs 篩選
- [ ] 測試項目完成和刪除
- [ ] 測試空狀態顯示

### 10. 文檔和清理 (⏳ 待實作)

#### 10.1 更新 README
- [ ] 記錄新功能
- [ ] 更新使用說明

#### 10.2 代碼清理
- [ ] 移除未使用的代碼
- [ ] 優化 imports
- [ ] 添加必要的註解

## 檔案清單

### 已創建的檔案

#### Models & Extensions
- ✅ `data/model/Itinerary.kt` (已更新)
- ✅ `util/DateExtensions.kt` (已存在)

#### Use Cases
- ✅ `domain/usecase/CreateItineraryUseCase.kt` (已更新)
- ✅ `domain/usecase/GroupItemsByDateUseCase.kt` (新建)
- ✅ `domain/usecase/FilterItemsByDateUseCase.kt` (新建)

#### ViewModels
- ✅ `ui/viewmodel/ItineraryDetailViewModel.kt` (已更新)

#### Dependency Injection
- ✅ `di/AppModule.kt` (已更新)

#### Tests
- ✅ `commonTest/data/model/ItineraryPropertyTest.kt` (新建)
- ✅ `commonTest/util/DateExtensionsPropertyTest.kt` (新建)
- ✅ `commonTest/domain/usecase/GroupItemsByDateUseCasePropertyTest.kt` (新建)
- ✅ `commonTest/domain/usecase/FilterItemsByDateUseCasePropertyTest.kt` (新建)

### 待創建的檔案

#### UI Components
- [ ] `ui/component/DateTabsRow.kt`
- [ ] `ui/component/ItemCard.kt`
- [ ] `ui/component/DateDropdown.kt`
- [ ] `ui/component/EmptyState.kt`

#### Screens (待更新)
- [ ] `ui/screen/AddEditItineraryScreen.kt`
- [ ] `ui/screen/AddEditItemScreen.kt`
- [ ] `ui/screen/ItineraryDetailScreen.kt`
- [ ] `ui/screen/ItineraryListScreen.kt`

## 下一步行動

1. **創建 UI Components** (優先級: 高)
   - DateTabsRow - 日期 tabs 水平滾動
   - ItemCard - 項目卡片顯示
   - DateDropdown - 日期下拉選單
   - EmptyState - 空狀態顯示

2. **更新 Screens** (優先級: 高)
   - 整合新的 UI components
   - 連接 ViewModel 狀態
   - 實作互動邏輯

3. **執行測試** (優先級: 中)
   - 修正 Gradle 配置問題
   - 執行所有 property tests
   - 驗證功能正確性

4. **效能優化** (優先級: 低)
   - 實作 lazy loading
   - 添加快取機制

## 技術債務

1. **Gradle 配置問題**
   - 需要修正 Android Gradle Plugin 版本問題
   - 可能需要清理 Gradle 快取

2. **測試執行環境**
   - 需要確保測試環境正確配置
   - 驗證 Kotest 依賴正確載入

## 備註

- 所有 property tests 都使用 Kotest 框架
- 每個 property test 運行 100 次迭代
- 使用隨機生成的測試資料確保覆蓋各種情況
- ViewModel 已備份為 `.bak` 檔案以防需要回滾

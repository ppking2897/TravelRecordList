# Requirements Document - 行程 UI 增強

## 簡介

本規格描述對現有旅遊行程應用的 UI 增強，主要改進包括：
1. 新增行程時設定日期範圍
2. 行程詳情頁面使用日期 tabs 水平滾動
3. 新增項目時使用下拉選單選擇日期
4. 項目以 card 形式按日期分組顯示
5. 使用 FAB 按鈕進行新增操作

## 術語表

- **System**: 旅遊行程記事應用程式
- **Itinerary**: 行程，包含開始和結束日期
- **Item**: 行程項目（單一活動或地點）
- **FAB**: Floating Action Button，浮動操作按鈕
- **Date Tab**: 日期標籤，用於切換顯示不同日期的項目
- **Date Range**: 日期範圍，從開始日期到結束日期的所有日期
- **Item Card**: 項目卡片，顯示單一行程項目的 UI 元件

## 需求

### 需求 1: 行程日期範圍設定

**使用者故事：** 作為使用者，我想在建立行程時設定開始和結束日期，以便系統知道我的旅行日期範圍。

#### 驗收標準

1. WHEN 建立新行程 THEN System SHALL 要求使用者輸入開始日期
2. WHEN 建立新行程 THEN System SHALL 要求使用者輸入結束日期
3. WHEN 結束日期早於開始日期 THEN System SHALL 顯示驗證錯誤並阻止儲存
4. WHEN 兩個日期都有效 THEN System SHALL 以指定的日期範圍儲存行程
5. WHEN 日期欄位獲得焦點 THEN System SHALL 顯示 date picker 介面

### 需求 2: 日期 Tabs 水平滾動顯示

**使用者故事：** 作為使用者，我想在水平滾動列表中看到日期 tabs，以便快速瀏覽行程的不同日期。

#### 驗收標準

1. WHEN 查看行程詳情頁面 THEN System SHALL 在頂部顯示水平滾動的日期 tabs 列表
2. WHEN 顯示日期 tabs THEN System SHALL 按時間順序顯示從行程開始日期到結束日期的所有日期
3. WHEN 點擊日期 tab THEN System SHALL 高亮選中的 tab 並篩選顯示該日期的項目
4. WHEN 沒有選中日期 tab THEN System SHALL 顯示所有項目並按日期分組
5. WHEN 使用者滾動日期 tab 列表 THEN System SHALL 保持流暢的水平滾動

### 需求 3: 項目卡片顯示

**使用者故事：** 作為使用者，我想以 card 形式查看項目，以便輕鬆閱讀和區分不同的活動。

#### 驗收標準

1. WHEN 顯示行程項目 THEN System SHALL 將每個項目渲染為 card component
2. WHEN 顯示項目 card THEN System SHALL 顯示活動名稱、地點、時間和完成狀態
3. WHEN 顯示項目 THEN System SHALL 按日期分組並顯示日期標題
4. WHEN 顯示日期組內的項目 THEN System SHALL 按時間排序（最早的在前）
5. WHEN 項目沒有指定時間 THEN System SHALL 將其顯示在同日期組有時間項目之後

### 需求 4: FAB 按鈕新增功能

**使用者故事：** 作為使用者，我想使用 floating action button 新增項目，以便從畫面任何位置快速存取新增功能。

#### 驗收標準

1. WHEN 查看行程列表畫面 THEN System SHALL 顯示用於新增行程的 FAB button
2. WHEN 查看行程詳情畫面 THEN System SHALL 顯示用於新增項目的 FAB button
3. WHEN 使用者點擊 FAB button THEN System SHALL 導航到相應的新增畫面
4. WHEN 滾動內容 THEN System SHALL 保持 FAB button 可見且可存取
5. WHEN 顯示 FAB button THEN System SHALL 將其定位在右下角並保持適當間距

### 需求 5: 項目日期下拉選單

**使用者故事：** 作為使用者，我想在新增項目時從下拉選單選擇日期，以便輕鬆從行程的日期範圍中選擇。

#### 驗收標準

1. WHEN 新增項目 THEN System SHALL 顯示日期選擇的 dropdown menu
2. WHEN 開啟 dropdown THEN System SHALL 顯示從行程開始日期到結束日期的所有日期
3. WHEN 從 dropdown 選擇日期 THEN System SHALL 將選中的日期填入日期欄位
4. WHEN 使用者嘗試在未選擇日期時儲存 THEN System SHALL 顯示錯誤訊息並阻止儲存
5. WHEN 在 dropdown 中顯示日期 THEN System SHALL 以使用者友善的格式顯示（例如："2024-01-15 (週一)"）

### 需求 6: 日期 Tab 篩選功能

**使用者故事：** 作為使用者，我想點擊日期 tabs 來篩選項目，以便專注於特定日期的行程。

#### 驗收標準

1. WHEN 點擊日期 tab THEN System SHALL 篩選項目列表僅顯示該日期的項目
2. WHEN 選中日期 tab THEN System SHALL 應用視覺高亮以指示選中狀態
3. WHEN 選中沒有項目的日期 tab THEN System SHALL 顯示該日期的空狀態訊息
4. WHEN 點擊「全部」tab THEN System SHALL 顯示所有項目並按日期分組
5. WHEN 按日期篩選項目 THEN System SHALL 在該日期內保持基於時間的排序

### Requirement 6: 日期標籤互動

**User Story:** As a user, I want to tap on date tabs to filter items, so that I can focus on specific days of my itinerary.

#### Acceptance Criteria

1. WHEN a date tab is tapped THEN the System SHALL scroll the items list to show that date's items
2. WHEN a date tab is selected THEN the System SHALL apply a visual highlight to indicate selection
3. WHEN the items list is scrolled manually THEN the System SHALL update the selected date tab to match the visible items
4. WHEN only one date exists THEN the System SHALL still display the date tab but disable scrolling
5. WHEN the selected date has no items THEN the System SHALL display a message indicating no items for that date

### Requirement 7: 空狀態處理

**User Story:** As a user, I want clear feedback when there are no items, so that I understand the current state and know how to add items.

#### Acceptance Criteria

1. WHEN an itinerary has no items THEN the System SHALL display an empty state illustration and message
2. WHEN the empty state is displayed THEN the System SHALL show a prompt to add the first item
3. WHEN the FAB button is visible in empty state THEN the System SHALL emphasize it with an animation or pulse effect
4. WHEN items are added to an empty itinerary THEN the System SHALL immediately show the date tabs and grouped items
5. WHEN all items are deleted THEN the System SHALL return to the empty state display

### Requirement 8: 效能優化

**User Story:** As a system administrator, I want the UI to perform smoothly, so that users have a responsive experience even with many items.

#### Acceptance Criteria

1. WHEN displaying items THEN the System SHALL use lazy loading for the items list
2. WHEN scrolling the date tabs THEN the System SHALL maintain 60 FPS scrolling performance
3. WHEN grouping items by date THEN the System SHALL cache the grouped data to avoid recalculation
4. WHEN the screen orientation changes THEN the System SHALL preserve the scroll position and selected date
5. WHEN loading a large itinerary THEN the System SHALL display items progressively without blocking the UI


### 需求 7: 空狀態處理

**使用者故事：** 作為使用者，我想在沒有項目時獲得清晰的回饋，以便了解當前狀態並知道如何新增項目。

#### 驗收標準

1. WHEN 行程沒有項目 THEN System SHALL 顯示空狀態圖示和訊息
2. WHEN 顯示空狀態 THEN System SHALL 顯示提示以新增第一個項目
3. WHEN FAB button 在空狀態下可見 THEN System SHALL 以動畫或脈衝效果強調它
4. WHEN 向空行程新增項目 THEN System SHALL 立即顯示日期 tabs 和分組項目
5. WHEN 刪除所有項目 THEN System SHALL 返回空狀態顯示

### 需求 8: 效能優化

**使用者故事：** 作為系統管理員，我想要 UI 流暢運行，以便使用者即使在有許多項目時也能獲得響應式體驗。

#### 驗收標準

1. WHEN 顯示項目 THEN System SHALL 對項目列表使用 lazy loading
2. WHEN 滾動日期 tabs THEN System SHALL 保持 60 FPS 的滾動效能
3. WHEN 按日期分組項目 THEN System SHALL 快取分組資料以避免重新計算
4. WHEN 螢幕方向改變 THEN System SHALL 保留滾動位置和選中的日期
5. WHEN 載入大型行程 THEN System SHALL 漸進式顯示項目而不阻塞 UI

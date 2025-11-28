# Requirements Document - 行程 CRUD 增強

## 簡介 (Introduction)

本規格描述對現有旅遊行程應用的 CRUD 操作增強，主要改進包括：
1. 行程編輯功能
2. 行程刪除功能（帶確認 dialog）
3. 項目刪除確認 dialog
4. 草稿暫存功能

## 術語表 (Glossary)

- **System**: 旅遊行程記事應用程式
- **Itinerary**: 行程，包含標題、描述、日期範圍和項目
- **Item**: 行程項目（單一活動或地點）
- **Draft**: 草稿，未完成的行程或項目資料
- **Dialog**: 對話框，用於確認操作的彈出視窗
- **CRUD**: Create, Read, Update, Delete 操作

## 需求 (Requirements)

### 需求 1: 行程編輯功能

**使用者故事：** 作為使用者，我想編輯現有行程的資訊，以便更新標題、描述或日期範圍。

#### 驗收標準 (Acceptance Criteria)

1. WHEN 使用者在行程列表點擊編輯按鈕 THEN System SHALL 導航到編輯畫面並預填現有資料
2. WHEN 使用者在編輯畫面修改資料 THEN System SHALL 即時更新輸入欄位
3. WHEN 使用者儲存編輯後的行程 THEN System SHALL 驗證資料並更新行程
4. WHEN 驗證失敗 THEN System SHALL 顯示錯誤訊息並阻止儲存
5. WHEN 成功儲存 THEN System SHALL 返回行程列表並顯示更新後的資料

### 需求 2: 行程刪除確認

**使用者故事：** 作為使用者，我想在刪除行程前看到確認對話框，以便避免誤刪重要資料。

#### 驗收標準 (Acceptance Criteria)

1. WHEN 使用者在行程列表點擊刪除按鈕 THEN System SHALL 顯示確認 dialog
2. WHEN 顯示確認 dialog THEN System SHALL 顯示行程標題和警告訊息
3. WHEN 使用者在 dialog 點擊確認 THEN System SHALL 刪除行程並更新列表
4. WHEN 使用者在 dialog 點擊取消 THEN System SHALL 關閉 dialog 且不刪除行程
5. WHEN 刪除失敗 THEN System SHALL 顯示錯誤訊息

### 需求 3: 項目刪除確認

**使用者故事：** 作為使用者，我想在刪除項目前看到確認對話框，以便避免誤刪重要活動。

#### 驗收標準 (Acceptance Criteria)

1. WHEN 使用者在項目卡片點擊刪除按鈕 THEN System SHALL 顯示確認 dialog
2. WHEN 顯示確認 dialog THEN System SHALL 顯示項目活動名稱和警告訊息
3. WHEN 使用者在 dialog 點擊確認 THEN System SHALL 刪除項目並更新列表
4. WHEN 使用者在 dialog 點擊取消 THEN System SHALL 關閉 dialog 且不刪除項目
5. WHEN 刪除項目後該日期無其他項目 THEN System SHALL 顯示該日期的空狀態

### 需求 4: 草稿暫存功能

**使用者故事：** 作為使用者，我想在新增或編輯行程時暫存草稿，以便稍後繼續編輯而不丟失資料。

#### 驗收標準 (Acceptance Criteria)

1. WHEN 使用者在新增/編輯畫面輸入資料 THEN System SHALL 自動暫存草稿到本地儲存
2. WHEN 使用者離開畫面後返回 THEN System SHALL 恢復之前的草稿資料
3. WHEN 使用者成功儲存行程 THEN System SHALL 清除該草稿
4. WHEN 使用者取消編輯 THEN System SHALL 保留草稿供下次使用
5. WHEN 草稿超過 7 天 THEN System SHALL 自動清除過期草稿

### 需求 5: 行程列表操作按鈕

**使用者故事：** 作為使用者，我想在行程列表直接看到編輯和刪除按鈕，以便快速執行操作。

#### 驗收標準 (Acceptance Criteria)

1. WHEN 顯示行程卡片 THEN System SHALL 在右上角顯示編輯和刪除圖示按鈕
2. WHEN 顯示編輯按鈕 THEN System SHALL 使用主題色彩（primary color）
3. WHEN 顯示刪除按鈕 THEN System SHALL 使用錯誤色彩（error color）
4. WHEN 點擊行程標題或內容區域 THEN System SHALL 導航到行程詳情
5. WHEN 點擊編輯或刪除按鈕 THEN System SHALL 不觸發行程詳情導航

### 需求 6: 行程詳情操作選項

**使用者故事：** 作為使用者，我想在行程詳情頁面編輯或刪除行程，以便在查看詳情時執行操作。

#### 驗收標準 (Acceptance Criteria)

1. WHEN 查看行程詳情 THEN System SHALL 在 top bar 顯示更多選項按鈕
2. WHEN 點擊更多選項按鈕 THEN System SHALL 顯示下拉選單包含編輯和刪除選項
3. WHEN 選擇編輯選項 THEN System SHALL 導航到編輯畫面
4. WHEN 選擇刪除選項 THEN System SHALL 顯示確認 dialog
5. WHEN 刪除成功 THEN System SHALL 返回行程列表

### 需求 7: 編輯項目功能

**使用者故事：** 作為使用者，我想編輯現有項目的資訊，以便更新活動、地點或時間。

#### 驗收標準 (Acceptance Criteria)

1. WHEN 使用者在項目卡片點擊編輯按鈕 THEN System SHALL 導航到編輯畫面並預填現有資料
2. WHEN 使用者在編輯畫面修改資料 THEN System SHALL 即時更新輸入欄位
3. WHEN 使用者儲存編輯後的項目 THEN System SHALL 驗證資料並更新項目
4. WHEN 驗證失敗 THEN System SHALL 顯示錯誤訊息並阻止儲存
5. WHEN 成功儲存 THEN System SHALL 返回行程詳情並顯示更新後的項目

### 需求 8: 資料一致性

**使用者故事：** 作為系統管理員，我想確保刪除和編輯操作保持資料一致性，以便系統資料始終正確。

#### 驗收標準 (Acceptance Criteria)

1. WHEN 刪除行程 THEN System SHALL 同時刪除該行程的所有項目
2. WHEN 編輯行程日期範圍 THEN System SHALL 驗證現有項目是否在新日期範圍內
3. WHEN 項目日期超出新日期範圍 THEN System SHALL 顯示警告並要求使用者確認
4. WHEN 更新失敗 THEN System SHALL 回滾所有變更並顯示錯誤訊息
5. WHEN 多個操作同時進行 THEN System SHALL 使用樂觀鎖定防止衝突

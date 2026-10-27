# 規格調整

> **目的**：把[產品規格書](product-spec.md)「待確認事項」在 2026-10-27 做的決定落實到程式。14 件改動依功能各寫成一條需求，分六組實作；每組動工時才補齊那一組的情境。
>
> 決定的理由見產品規格書「待確認事項」與[改善計畫](improvement-plan.md)「規格調整」。各畫面原本的行為寫在 [ItineraryList.md](ItineraryList.md)、[ItineraryDetail.md](ItineraryDetail.md)、[Today.md](Today.md)；每組做完，在同一筆 commit 裡改掉那幾份文件中跟新行為衝突的地方。

---

## 行為規格

**第 1 組：用語與小畫面**

### Requirement: 介面用語統一 <!-- ADDED 2026-10-27 -->

App 的用語 SHALL 統一如下：

- 行程階段在首頁的摘要卡與區塊都叫「規劃中」，跟篩選選項一致，不再用「即將出發」。
- 行程裡的第幾天一律寫「第 N 天」，不用「Day N」。
- 有年份的完整日期一律寫成 2026/10/20；「10/14」「10/14（三）」這類短日期維持原樣。
- 回憶故事裡，中文字跟地名之間、句子跟句子之間都不加空格；數字前後的空格維持，跟 App 其他地方的「3 個地點」一致。

#### Scenario: 首頁寫「規劃中」
- GIVEN 今天是 10/16，有 5 趟規劃中的行程
- WHEN 使用者打開行程列表
- THEN 摘要卡的數字 5 下面寫「規劃中」
- AND 區塊標題寫「規劃中」，整頁不再出現「即將出發」

#### Scenario: 行程詳情寫「第 N 天」
- GIVEN 行程是 10/14–10/18
- WHEN 使用者在行程詳情選第三天
- THEN 時間軸上這一天寫「第 3 天」
- AND 當天的摘要卡寫「第 3 天」，下面是「10/16（五）」
- AND 「總覽」與每一天之間的連接線接成一條直線，沒有高低落差

#### Scenario: 行程詳情的日期標題寫完整日期
- GIVEN 行程 10/20 有項目
- WHEN 使用者在行程詳情選「總覽」
- THEN 那一組項目上方的日期寫「2026/10/20」

#### Scenario: 地圖頁寫「第 N 天」與完整日期
- GIVEN 行程從 10/14 開始，第一天有 2 個項目
- WHEN 使用者打開這趟行程的地圖頁
- THEN 第一天的標題寫「第 1 天 · 2026/10/14」
- AND 依日期篩選的選單、點選地點後的資訊卡，日期都寫成 2026/10/14 的格式

#### Scenario: 新增項目的日期選單寫完整日期
- GIVEN 行程是 10/20–10/22
- WHEN 使用者在新增項目打開日期選單
- THEN 第一個選項寫「2026/10/20（二）」，星期的寫法跟「10/14（三）」一致

#### Scenario: 新增行程的日期欄寫完整日期
- WHEN 使用者在新增行程選開始日期 11/10
- THEN 開始日期欄寫「2026/11/10」，不是「2026-11-10」

#### Scenario: 回憶故事沒有多餘空格
- GIVEN 已結束的行程從 10/14 開始，共 3 天、5 個項目，地點依序是伏見稻荷、京都、京都、金閣寺、嵐山，都已打卡，共 2 張照片
- WHEN 產生這趟行程的回憶故事
- THEN 標題是「秋天的京都」
- AND 摘要是「10 月的京都之旅，走了 3 天、5 個地方。」
- AND 敘述是「10 月 14 日，旅程從伏見稻荷展開。這 3 天裡走過 5 個地方，每一處都留下了打卡紀錄。最後一站嵐山成了旅程的句點——相簿裡留下 2 張照片，剩下的就放在心裡。」

### Requirement: 「今日」分頁換成代表今天的圖示 <!-- ADDED 2026-10-27 -->

底部「今日」分頁 SHALL 用日曆上標一個點的「今天」圖示，不用「＋」，以免被當成「新增」。

#### Scenario: 底部分頁的今日圖示
- WHEN 使用者看底部分頁
- THEN 「今日」上方是日曆上標一個點的圖示，不是「＋」

### Requirement: 首頁拿掉最近編輯區塊與足跡按鈕 <!-- ADDED 2026-10-27 -->

首頁 SHALL 不再有「最近編輯」區塊；標題列右上 SHALL 不再有切到足跡的按鈕，足跡改從底部分頁進入。

#### Scenario: 首頁沒有最近編輯
- GIVEN 有旅行中、規劃中、已結束的行程各一趟
- WHEN 使用者打開行程列表
- THEN 區塊只有「現在旅行中」「規劃中」「全部行程」
- AND 沒有「最近編輯」區塊

#### Scenario: 標題列沒有足跡按鈕
- WHEN 使用者打開行程列表
- THEN 標題列右上沒有地點圖示的按鈕

### Requirement: 沒有資料時有「去看行程」按鈕 <!-- ADDED 2026-10-27 -->

足跡、回憶沒有資料時，說明文字下方 SHALL 有「去看行程」按鈕；按下 SHALL 切到「行程」分頁，跟點底部的「行程」分頁一樣，停在行程分頁上次所在的頁面。今日頁沒有旅行中行程時的「回行程規劃」做的是同一件事，SHALL 一併改名為「去看行程」。

#### Scenario: 今日頁的按鈕改名
- GIVEN 今天沒有旅行中的行程
- WHEN 使用者打開今日分頁
- THEN 說明文字下方的按鈕寫「去看行程」，按下切到行程分頁

#### Scenario: 從足跡的空狀態去看行程
- GIVEN 沒有已打卡、也沒有日期已過的項目
- WHEN 使用者在足跡分頁按「去看行程」
- THEN 切到行程分頁，底部分頁選中「行程」

#### Scenario: 從回憶的空狀態去看行程
- GIVEN 沒有已結束的行程
- WHEN 使用者在回憶分頁按「去看行程」
- THEN 切到行程分頁，底部分頁選中「行程」

### Requirement: 時間軸的地點名稱完整顯示 <!-- ADDED 2026-10-27 -->

行程詳情時間軸上每天的地點名稱 SHALL 在一行內完整顯示：最長的名稱放不下時，所有格子一起加寬到放得下，格子仍一樣寬；加寬有上限，約 7 個中文字，超過上限的名稱才顯示「…」。（2026-10-27 實機檢查時決定）

#### Scenario: 地點名稱放不下時加寬格子
- GIVEN 行程 10/14–10/18 共 5 天，第 1 天的地點是「伏見稻荷大社」
- WHEN 使用者打開行程詳情
- THEN 時間軸上第 1 天寫完整的「伏見稻荷大社」，不是「伏見稻荷…」
- AND 每一格一樣寬

**第 2 組：排序與範例資料**

### Requirement: 沒排時間的項目排在當天最後 <!-- ADDED 2026-10-27 -->

行程詳情、今日、地圖、路線 SHALL 用同一套排序：同一天裡，有時間的項目依時間排（有抵達時間用抵達，否則用出發），沒排時間的排在當天最後。（產品規格書待確認 #6）

#### Scenario: TODO：沒排時間的項目排在當天最後（第 2 組動工時補）

### Requirement: 範例行程只在第一次安裝時放一次 <!-- ADDED 2026-10-27 -->

範例行程 SHALL 只在第一次安裝時放一次；使用者把行程全部刪掉後重開 App，範例 SHALL 不會再出現。從舊版升級、本來就有行程的人，SHALL 視為已經放過。（待確認 #7）

#### Scenario: TODO：範例行程只在第一次安裝時放一次（第 2 組動工時補）

**第 3 組：存檔與防呆**

### Requirement: 今日頁讀取失敗時可以重試 <!-- ADDED 2026-10-27 -->

今日頁讀取行程失敗時 SHALL 顯示錯誤訊息與「重試」按鈕，不顯示成「今天沒有旅行中的行程」。（待確認 #8）

#### Scenario: TODO：今日頁讀取失敗時可以重試（第 3 組動工時補）

### Requirement: 快速紀錄存檔成功才關閉 <!-- ADDED 2026-10-27 -->

快速紀錄按儲存後 SHALL 等存檔結果：存檔中按鈕停用；成功才關閉面板；失敗時留在面板並顯示錯誤，已填的內容不消失。（待確認 #9）

#### Scenario: TODO：快速紀錄存檔成功才關閉（第 3 組動工時補）

### Requirement: 編輯沒存就離開會提醒 <!-- ADDED 2026-10-27 -->

編輯行程、編輯項目有修改時，沒存就離開 SHALL 跳「捨棄變更？」，跟新增時一樣；沒有修改時直接離開。（待確認 #10）

#### Scenario: TODO：編輯沒存就離開會提醒（第 3 組動工時補）

### Requirement: 刪照片一律先確認 <!-- ADDED 2026-10-27 -->

從縮圖選單或照片預覽刪除照片，SHALL 都先跳確認。（待確認 #11）

#### Scenario: TODO：刪照片一律先確認（第 3 組動工時補）

**第 4 組：日期必填**

### Requirement: 行程一定要有日期 <!-- ADDED 2026-10-27 -->

新建行程時，開始與結束日期 SHALL 必填；既有沒有日期的行程，詳情頁 SHALL 提示補上日期。（待確認 #4）

#### Scenario: TODO：行程一定要有日期（第 4 組動工時補）

**第 5 組：路線**

### Requirement: 路線分天，分享可讀的文字 <!-- ADDED 2026-10-27 -->

路線 SHALL 依天分段；分享內容 SHALL 是「第 1 天：伏見稻荷 → 金閣寺 → 嵐山」這種可讀的文字，不是程式格式。地圖頁與路線頁合成一頁，留到做真地圖時。（待確認 #5）

#### Scenario: TODO：路線分天，分享可讀的文字（第 5 組動工時補）

**第 6 組：標籤與手勢**

### Requirement: 點標籤篩選同標籤的項目 <!-- ADDED 2026-10-27 -->

在行程詳情點項目上的標籤 SHALL 只列出這趟行程裡有同一個標籤的項目；再點一次 SHALL 取消篩選。（待確認 #14）

#### Scenario: TODO：點標籤篩選同標籤的項目（第 6 組動工時補）

### Requirement: 卡片可以左右滑 <!-- ADDED 2026-10-27 -->

行程卡片與項目卡片 SHALL 可以左滑刪除（先確認）、右滑編輯；原本的選單 SHALL 照樣能用，不會滑的人也能操作。（改善計畫「UI 改善」#8）

#### Scenario: TODO：卡片可以左右滑（第 6 組動工時補）

---

## 技術設計

### 第 1 組

**用語**

| 用語 | 改的位置 |
|---|---|
| 即將出發 → 規劃中 | `ItineraryListScreen` 的摘要卡數字標籤、區塊標題 |
| Day N → 第 N 天 | `TimelineNavigator` 的日期節點、`ItineraryDetailScreen` 的當天摘要卡、Android 地圖頁 `ItineraryMapView.android.kt` 的每日標題 |
| 完整日期 → 2026/10/20 | `ItineraryDetailScreen` 的日期分組標題、`ItineraryMapScreen` 的日期篩選選單、`MapInfoCard`、`ItineraryMapView.android.kt` 的每日標題、`toFriendlyString()`（新增與編輯項目的日期選單 `DateDropdown`）、`ItineraryFormContent` 的開始與結束日期欄、`ItemFormContent` 在行程沒設日期時的日期欄 |
| 故事的空格 | `LocalRuleBasedMemoryStoryGenerator` 的模板字串 |

- 完整日期一律交給既有的 `DateTimeFormatter.formatDate`（已經是 `yyyy/MM/dd`），不再用 `LocalDate.toString()`。
- `toFriendlyString()` 的日期改成 `2026/10/20`，星期從「(週二)」改成跟 `toShortStringWithWeekday()` 一樣的「（二）」。

**圖示**：`App.kt` 的 `TravelBottomNavigation` 把今日分頁的 `Icons.Default.Add` 換成 `Icons.Default.Today`。足跡、回憶頁已經在用 `Explore`、`AutoStories` 這類延伸圖示，推測不用新增依賴；實作時以編譯結果確認。

**首頁**
- `ItineraryDashboard` 拿掉 `recentTrips`，`ItineraryListScreen` 拿掉「最近編輯」區塊。
- 標題列的足跡按鈕（`onHistoryClick`）拿掉，`App.kt` 的 `PlanNavHost` 一併拿掉 `onSwitchToFootprints`。

**時間軸**（`TimelineNavigator`）
- 每格寬度原本是「平分整排，最少 64dp」，再加一個下限：最長地點名稱的寬度（用粗體量，左右各加 `Spacing.xs`），上限 `TimelineDimensions.maxItemWidth`（96dp，約 7 個中文字）。名稱用 `rememberTextMeasurer` 量。
- 「總覽」那格對應「第 N 天」的佔位，原本是一個空白字元，改成 alpha 0 的「第 1 天」。空白是英文字型，比中文那一行矮，「第 N 天」上線後總覽的節點比每一天高一兩個像素，連接線在總覽旁錯開。
- 選中那一格的標記（`SelectedNode`）從手畫的行李箱改成珊瑚紅圓角框裡一架朝右的飛機（`Icons.Default.Flight` 轉 90 度），框的大小、底色、墨色外框不變。跟手帳風示意圖的行李箱不同，是 2026-10-27 實機檢查後使用者的決定。

**空狀態**：`FootprintScreen`、`MemoryScreen` 新增 `onGoToPlan: () -> Unit`，不給預設值。`App.kt` 傳 `{ selectedTab = AppTab.Plan }`，跟今日頁的 `onGoToPlan` 同一個寫法。`TodayScreen` 的「回行程規劃」改成「去看行程」。

**因規格改變而要改的既有測試**：下面兩條是為了跟新決定一致，不是為了讓測試通過。

| 測試 | 改法 |
|---|---|
| `ItineraryDashboardTest#只有一筆行程時不顯示最近編輯` | 刪除，對應的情境在 ItineraryList.md 標成 REMOVED |
| `DateExtensionsPropertyTest` 的「日期格式化一致性」 | 預期從「含 `2026-10-20` 與『週』」改成「含 `2026/10/20` 與全形括號的星期」 |

**同一筆 commit 要更新的既有文件**
- ItineraryList.md：區塊表拿掉「最近編輯」；「即將出發」改成「規劃中」；「只有一筆行程時不顯示最近編輯」標 REMOVED；右上地點圖示那句拿掉。
- ItineraryDetail.md：「Day N」改成「第 N 天」；時間軸的地點名稱完整顯示。
- Today.md：待辦的「換今日分頁圖示」改成已完成；情境「從沒有行程的狀態回到行程規劃」的按鈕名稱改成「去看行程」（MODIFIED）。
- product-spec.md：首頁、行程詳情、回憶等章節改寫成新行為，待確認 #1、#12、#13、#15、#16 標成已完成。

---

## 涵蓋範圍

這份文件本來就跨好幾個畫面，以下依組列出；畫面原本的識別字仍由各畫面文件涵蓋。

- 第 1 組識別字：`buildItineraryDashboard`、`ItineraryDashboard`、`LocalRuleBasedMemoryStoryGenerator`、`toFriendlyString`、`TravelBottomNavigation`、`DayGroupHeader`、`DayPlanningSummary`
- 第 1 組路徑：
  - `feature/itinerary_list/presentation/**`
  - `feature/itinerary_detail/presentation/ItineraryDetailScreen.kt`
  - `feature/itinerary_map/presentation/ItineraryMapScreen.kt`
  - `feature/itinerary_add_edit/presentation/ItineraryFormContent.kt`
  - `feature/itinerary_item/presentation/ItemFormContent.kt`
  - `feature/footprint/presentation/FootprintScreen.kt`
  - `feature/memory/presentation/MemoryScreen.kt`
  - `feature/today/presentation/TodayScreen.kt`
  - `feature/memory/data/service/LocalRuleBasedMemoryStoryGenerator.kt`
  - `core/presentation/components/TimelineNavigator.kt`
  - `core/presentation/theme/Dimensions.kt`（`TimelineDimensions.maxItemWidth`）
  - `core/presentation/components/MapInfoCard.kt`
  - `androidMain/.../ItineraryMapView.android.kt`
  - `util/DateExtensions.kt`
  - `App.kt`
- 第 2～6 組：動工時補。

---

## 實作計畫 + 驗證紀錄

> 「邏輯」情境先寫測試、跑紅再實作；「UI」情境在手機上確認。改到換頁或返回的組是高風險組，做完要跑 `/device-check`。

### Slice 1 — 第 1 組：用語與小畫面
- **對應 Requirement**：介面用語統一、「今日」分頁換成代表今天的圖示、首頁拿掉最近編輯區塊與足跡按鈕、沒有資料時有「去看行程」按鈕、時間軸的地點名稱完整顯示
- **commit**：未 commit（程式已改完，測試 243 條全過）
- **整體狀態**：✅ 邏輯情境由單元測試驗證，UI 情境 2026-10-27 實機走查通過
- **高風險組**：是（「去看行程」會切換分頁）

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 首頁寫「規劃中」 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：有一趟規劃中時，摘要卡數字下寫「規劃中」，區塊標題也是「規劃中」 |
| 行程詳情寫「第 N 天」 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：時間軸與摘要卡寫「第 3 天」，下面是「10/16（五）」；總覽旁的連接線一直線（第一次走查時錯開一兩個像素，已修正） |
| 行程詳情的日期標題寫完整日期 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：總覽的分組標題寫「2026/10/14」 |
| 地圖頁寫「第 N 天」與完整日期 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：標題寫「第 1 天 · 2026/10/14」，日期篩選選單與資訊卡也是 2026/10/14 |
| 新增項目的日期選單寫完整日期 | 邏輯 | ✅ | 2026-10-27 | unit test: DateExtensionsTest#新增項目的日期選單寫完整日期 、 DateExtensionsPropertyTest 的「日期格式化一致性」；先紅後綠；突變（mutation）：星期改回半形「(週二)」、日期改回 2026-10-20 → 紅 |
| 新增行程的日期欄寫完整日期 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：第一次走查還是「2026-11-10」，修正後選 10/30 顯示「2026/10/30」 |
| 回憶故事沒有多餘空格 | 邏輯 | ✅ | 2026-10-27 | unit test: LocalRuleBasedMemoryStoryGeneratorTest#回憶故事沒有多餘空格；先紅後綠；突變：標題「的」前後、摘要地名前後、敘事句子之間、最後一站地名前後加回空格 → 紅 |
| 底部分頁的今日圖示 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：「今日」是日曆上標一個點的圖示 |
| 首頁沒有最近編輯 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：旅行中、規劃中、已結束各一趟時，區塊只有「現在旅行中」「規劃中」「全部行程」 |
| 標題列沒有足跡按鈕 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：標題列只有「行程規劃」，右上沒有按鈕 |
| 從足跡的空狀態去看行程 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：沒有行程項目時按「去看行程」，切到行程分頁，底部選中「行程」 |
| 從回憶的空狀態去看行程 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：沒有已結束的行程時按「去看行程」，切到行程分頁，底部選中「行程」 |
| 今日頁的按鈕改名 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：沒有旅行中的行程時，按鈕寫「去看行程」，按下切到行程分頁 |
| 地點名稱放不下時加寬格子 | UI | ✅ | 2026-10-27 | 實機（SM-S9110）：京都大阪 5 日遊第 1 天完整顯示「伏見稲荷大社」，每格一樣寬；東京 3 日遊名稱短，維持平分整排 |

### Slice 2 — 第 2 組：排序與範例資料
- **對應 Requirement**：沒排時間的項目排在當天最後、範例行程只在第一次安裝時放一次
- **commit**：未開始
- **整體狀態**：⏸️ blocked: Scenario 未定義

### Slice 3 — 第 3 組：存檔與防呆
- **對應 Requirement**：今日頁讀取失敗時可以重試、快速紀錄存檔成功才關閉、編輯沒存就離開會提醒、刪照片一律先確認
- **commit**：未開始
- **整體狀態**：⏸️ blocked: Scenario 未定義

### Slice 4 — 第 4 組：日期必填
- **對應 Requirement**：行程一定要有日期
- **commit**：未開始
- **整體狀態**：⏸️ blocked: Scenario 未定義

### Slice 5 — 第 5 組：路線
- **對應 Requirement**：路線分天，分享可讀的文字
- **commit**：未開始
- **整體狀態**：⏸️ blocked: Scenario 未定義

### Slice 6 — 第 6 組：標籤與手勢
- **對應 Requirement**：點標籤篩選同標籤的項目、卡片可以左右滑
- **commit**：未開始
- **整體狀態**：⏸️ blocked: Scenario 未定義

---

## 待辦 / 跟外部對齊

| 項目 | 屬於誰 | 阻擋上線？ |
|------|-------|----------|
| 項目卡片的資訊順序：先盤點卡片上現有的欄位，再決定順序（改善計畫「UI 改善」#10） | App | 否 |
| 地圖頁與路線頁合成一頁：做真地圖時一起做（待確認 #5 的後半） | App | 否 |

---

## 變更紀錄

- **2026-10-27** ADDED: Requirement「介面用語統一」「「今日」分頁換成代表今天的圖示」「首頁拿掉最近編輯區塊與足跡按鈕」「沒有資料時有「去看行程」按鈕」（第 1 組，含情境）
- **2026-10-27** ADDED: Requirement「沒排時間的項目排在當天最後」「範例行程只在第一次安裝時放一次」「今日頁讀取失敗時可以重試」「快速紀錄存檔成功才關閉」「編輯沒存就離開會提醒」「刪照片一律先確認」「行程一定要有日期」「路線分天，分享可讀的文字」「點標籤篩選同標籤的項目」「卡片可以左右滑」（第 2～6 組，情境動工時補）
- **2026-10-27** 技術設計：時間軸選中那格的行李箱換成飛機（實機檢查後決定，示意圖仍是行李箱）

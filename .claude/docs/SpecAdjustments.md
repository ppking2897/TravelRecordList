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

行程詳情、今日、地圖、路線、回憶故事 SHALL 用同一套排序：同一天裡，有時間的項目依時間排（有抵達時間用抵達，否則用出發），沒排時間的排在當天最後；時間相同、或都沒排時間的，先建立的在前。（產品規格書待確認 #6）

#### Scenario: 同一天的排序規則
- GIVEN 10/14 依建立先後有：「祇園晚餐」沒排時間、「清水寺」抵達 14:30、「錦市場」抵達 14:30、「伏見稻荷」只填出發 09:00、「八坂神社」沒排時間
- WHEN 排出 10/14 的順序
- THEN 依序是伏見稻荷、清水寺、錦市場、祇園晚餐、八坂神社

#### Scenario: 今日頁沒排時間的排最後
- GIVEN 今天是 10/15，進行中的行程今天有跟上一條一樣的五個項目
- WHEN 使用者打開今日分頁
- THEN 清單依序是伏見稻荷、清水寺、錦市場、祇園晚餐、八坂神社
- AND 祇園晚餐、八坂神社的時間欄寫「未排時間」

#### Scenario: 地圖頁沒排時間的排在當天最後
- GIVEN 行程 10/14–10/15，項目都有座標：10/14 有沒排時間的「祇園」、09:00 的「伏見稻荷」，10/15 有 10:00 的「金閣寺」
- WHEN 使用者打開地圖頁
- THEN 地點依序是伏見稻荷（10/14 第 1 站）、祇園（10/14 第 2 站）、金閣寺（10/15 第 1 站）

#### Scenario: 路線沒排時間的排在當天最後
- GIVEN 同上一條的行程
- WHEN 使用者生成路線
- THEN 路線依序是伏見稻荷 → 祇園 → 金閣寺

#### Scenario: 回憶故事的第一站與最後一站
- GIVEN 已結束的行程 10/14–10/15，全部已打卡：10/14 有沒排時間的「錦市場」、09:00 的「伏見稻荷」，10/15 有 10:00 的「金閣寺」、沒排時間的「關西機場」
- WHEN 產生這趟行程的回憶故事
- THEN 敘述開頭是「10 月 14 日，旅程從伏見稻荷展開。」
- AND 敘述裡寫「最後一站關西機場成了旅程的句點」

### Requirement: 範例行程只在第一次安裝時放一次 <!-- ADDED 2026-10-27 -->

範例行程 SHALL 只在第一次安裝時放一次；使用者把行程全部刪掉後重開 App，範例 SHALL 不會再出現。從舊版升級、本來就有行程的人，SHALL 視為已經放過。（待確認 #7）

#### Scenario: 第一次安裝放入範例行程
- GIVEN 全新安裝，手機上沒有任何行程
- WHEN 使用者第一次打開 App
- THEN 行程列表有「京都大阪 5 日遊」與「東京 3 日遊」兩趟範例

#### Scenario: 範例刪光後重開不會回來
- GIVEN 範例已經放過，使用者把行程全部刪掉
- WHEN 使用者重開 App
- THEN 行程列表是空的，顯示「還沒有行程」

#### Scenario: 從舊版升級、本來就有行程
- GIVEN 舊版使用者手機上有 2 趟行程，沒有「範例已放過」的紀錄
- WHEN 升級後第一次打開 App
- THEN 不加範例，行程維持原本的 2 趟
- AND 之後把行程全部刪掉再重開，範例也不會出現

#### Scenario: 從舊版升級、行程已經刪光
- GIVEN 舊版使用者已經把行程全部刪掉，沒有「範例已放過」的紀錄
- WHEN 升級後第一次打開 App
- THEN 放入兩趟範例（這時跟全新安裝分不出來）
- AND 之後把行程全部刪掉再重開，範例不會再出現

**第 3 組：存檔與防呆**

### Requirement: 今日頁讀取失敗時可以重試 <!-- ADDED 2026-10-27 -->

今日頁讀取行程失敗時 SHALL 顯示錯誤訊息與「重試」按鈕，不顯示成「今天沒有旅行中的行程」。讀取今天的項目失敗時同樣 SHALL 顯示錯誤與「重試」，不顯示成「今天還沒有行程」。（待確認 #8；項目那半是 2026-10-28 補的決定）

#### Scenario: 讀取行程失敗時顯示錯誤與重試
- GIVEN 讀取行程時發生錯誤「磁碟已滿」
- WHEN 使用者打開今日分頁
- THEN 顯示「讀取失敗：磁碟已滿」與「重試」按鈕
- AND 不顯示「今天沒有旅行中的行程」與「去看行程」

#### Scenario: 讀取今天的項目失敗時顯示錯誤與重試
- GIVEN 進行中的行程是「京都大阪 5 日遊」，讀取它的項目時發生錯誤「磁碟已滿」
- WHEN 使用者打開今日分頁
- THEN 行程頁首照常顯示，項目區顯示「讀取失敗：磁碟已滿」與「重試」按鈕
- AND 不顯示「今天還沒有行程」與「新增第一筆」

#### Scenario: 按重試後重新讀取
- GIVEN 剛才讀取失敗，畫面顯示「重試」；現在讀取恢復正常，進行中的行程是「京都大阪 5 日遊」，今天有「伏見稻荷」
- WHEN 使用者按「重試」
- THEN 錯誤訊息消失，列出「伏見稻荷」

### Requirement: 快速紀錄存檔成功才關閉 <!-- ADDED 2026-10-27 -->

快速紀錄按儲存後 SHALL 等存檔結果：存檔中按鈕停用；成功才關閉面板；失敗時留在面板並顯示錯誤，已填的內容不消失。（待確認 #9）

#### Scenario: 存檔成功才關閉面板
- GIVEN 快速紀錄填了活動「喝抹茶」、地點「中村藤吉」
- WHEN 使用者按「儲存到今日」，存檔成功
- THEN 面板關閉，表單清空
- AND 底部提示「已加入今日紀錄」，今天的清單出現「喝抹茶」

#### Scenario: 存檔中不能再按
- GIVEN 已按下「儲存到今日」，存檔還沒完成
- THEN 按鈕停用並顯示轉圈，面板還開著
- AND 這時再按一次儲存，也只會新增一筆

#### Scenario: 存檔失敗時留在面板
- GIVEN 快速紀錄填了活動「喝抹茶」、地點「中村藤吉」、備註「排隊 20 分鐘」，加了 1 張照片
- WHEN 使用者按「儲存到今日」，存檔時發生錯誤「磁碟已滿」
- THEN 面板不關閉，面板裡顯示「儲存失敗：磁碟已滿」
- AND 活動、地點、備註、照片都還在，按鈕恢復可以按
- AND 再按一次「儲存到今日」時，錯誤訊息先消失

#### Scenario: 沒填活動時不關閉面板
- GIVEN 快速紀錄只填了地點「中村藤吉」
- WHEN 使用者按「儲存到今日」
- THEN 面板不關閉，活動欄顯示「請輸入活動」，不存檔

### Requirement: 編輯沒存就離開會提醒 <!-- ADDED 2026-10-27 -->

編輯行程、編輯項目有修改時，沒存就離開 SHALL 跳「捨棄變更？」，跟新增時一樣；沒有修改時直接離開。（待確認 #10）

#### Scenario: 編輯行程沒改就離開
- GIVEN 打開「京都大阪 5 日遊」的編輯行程，什麼都沒改
- WHEN 使用者按返回
- THEN 直接回到上一頁，不跳提醒

#### Scenario: 編輯行程有修改時離開會提醒
- GIVEN 打開「京都大阪 5 日遊」的編輯行程，把標題改成「京都 5 日遊」
- WHEN 使用者按左上返回，或用系統返回手勢
- THEN 跳出「捨棄變更？」「您有未儲存的變更，確定要離開嗎？」
- AND 按「捨棄」回到上一頁，行程標題仍是「京都大阪 5 日遊」；按「繼續編輯」留在表單，標題仍是「京都 5 日遊」

#### Scenario: 改回原樣不算修改
- GIVEN 編輯行程時把標題改成「京都 5 日遊」，又改回「京都大阪 5 日遊」
- WHEN 使用者按返回
- THEN 直接回到上一頁，不跳提醒

#### Scenario: 行程的每一欄都算
- GIVEN 打開「京都大阪 5 日遊」的編輯行程
- WHEN 只改標題、描述、開始日期、結束日期、封面其中一欄
- THEN 每一種都算有修改

#### Scenario: 編輯項目沒改就離開
- GIVEN 打開「清水寺」的編輯項目，什麼都沒改
- WHEN 使用者按返回
- THEN 直接回到上一頁，不跳提醒

#### Scenario: 編輯項目有修改時離開會提醒
- GIVEN 打開「清水寺」的編輯項目，在備註加上「記得買御守」
- WHEN 使用者按左上返回，或用系統返回手勢
- THEN 跳出「捨棄變更？」「您有未儲存的變更，確定要離開嗎？」
- AND 按「捨棄」回到上一頁，備註沒有存；按「繼續編輯」留在表單，備註還在

#### Scenario: 項目的每一欄都算
- GIVEN 打開「清水寺」的編輯項目
- WHEN 只改活動、地點、地址、備註、日期、到達時間、離開時間、照片其中一項
- THEN 每一種都算有修改

#### Scenario: 新增時照舊
- GIVEN 打開新增行程或新增項目
- WHEN 填了任何一欄後按返回
- THEN 跟現在一樣跳出「捨棄變更？」；什麼都沒填就直接離開

### Requirement: 刪照片一律先確認 <!-- ADDED 2026-10-27 -->

從縮圖選單或照片預覽刪除照片，SHALL 都先跳確認。（待確認 #11）

#### Scenario: 從縮圖選單刪照片要先確認
- GIVEN 行程詳情展開「清水寺」，它有 2 張照片
- WHEN 使用者點第一張縮圖右上的「⋮」，選「刪除」
- THEN 跳出「刪除照片」「確定要刪除這張照片嗎？此操作無法復原。」
- AND 按「刪除」後剩 1 張；按「取消」還是 2 張

#### Scenario: 從照片預覽刪照片照舊先確認
- GIVEN 使用者點縮圖打開照片預覽
- WHEN 按右上的刪除
- THEN 跳出同一個確認；按「刪除」後預覽關閉，照片少一張

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

### 第 2 組

**排序**
- 在 domain 放一個共用的比較規則（`ItineraryItem` 旁）：日期 → 有時間的在前 → 時間（`primaryTime()`）。時間相同時不另外比，靠穩定排序維持資料層讀出的順序（先建立的在前）。
- 改用這個規則的地方：
  - 行程詳情的 `GroupItemsByDateUseCase`：拿掉「沒時間當成 23:59:59」的寫法，免得 23:59:59 的項目跟沒排時間的分不出先後。時間軸每天顯示的地點取當天第一個項目，跟著一起對。
  - 今日的 `TodayViewModel`：原本是沒時間的排最前，時間相同再比建立時間。
  - 地圖的 `GetMapMarkersUseCase`：原本沒時間的排最前，當天第幾站跟著變。
  - 路線的 `RouteRepositoryImpl`：原本沒時間的排最前。
  - 回憶故事的 `LocalRuleBasedMemoryStoryGenerator`：第一站、最後一站跟著變。
- 資料層讀出的順序也改成同一規則：`ItineraryItemDao` 的 `ORDER BY` 在時間前加 `COALESCE(arrivalTime, departureTime) IS NULL`。只改查詢，不動資料表，不用升資料庫版本。同一份合約綁著的另外兩個測試用實作也跟著改：記憶體版（`InMemoryRepositories`）與舊版 JSON 版（`legacy/LegacyJsonItineraryItemRepository`），兩者都直接用 `itemDayOrder`。

**範例資料**
- `InitializeSampleDataUseCase` 改成看「範例已放過」的紀錄，不再看「有沒有行程」：
  - 有紀錄：不放。
  - 沒紀錄、已經有行程（舊版升級）：補寫紀錄，不放。
  - 沒紀錄、沒有行程：放範例，成功後寫紀錄。
- 紀錄存在鍵值儲存，跟搬移標記 `migration:room:v1` 同一處。domain 加一個小介面讀寫這筆紀錄，data 層用 `StorageService` 實作，use case 不直接碰儲存。
- 寫紀錄失敗也不會重複放：下次啟動時已經有行程，會走「補寫紀錄」那條。

**因規格改變而要改的既有測試**

| 測試 | 改法 |
|---|---|
| `ItineraryItemRepositoryContract#項目依日期再依抵達或離開時間排序，沒有時間的排前面，相同時保留新增順序` | 預期改成沒有時間的排在當天最後，測試名稱改成「…沒有時間的排在當天最後…」 |
| `TodayViewModelTest#沒排時間的項目排在最前面` | 換成 `沒排時間的項目排在最後面`，對應 Today.md 的情境改成 MODIFIED |

**同一筆 commit 要更新的既有文件**
- Today.md：Requirement「只列出進行中行程裡日期是今天的項目，並依時間排序」與情境「沒排時間的項目排在最前面」改成排在最後（MODIFIED）。
- ItineraryDetail.md：待辦「沒排時間的項目在這頁排最後、在今日頁排最前」拿掉。
- ItineraryList.md：Requirement「啟動時先準備本機資料，再載入行程」裡「一筆行程都沒有時放入範例行程」改成只放一次（MODIFIED）。
- product-spec.md：今日、地圖、路線的排序描述與範例行程的說明改成新行為，待確認 #6、#7 標成已完成。
- improvement-plan.md：規格調整第 2 組的兩項打勾。

### 第 3 組

**今日頁讀取失敗**
- ViewModel 讀取行程失敗時，本來就會留下錯誤訊息、清掉行程；讀取項目失敗時，留下錯誤訊息、保留行程。重試直接送既有的 `TodayIntent.Refresh`，它一開始就會清掉錯誤。所以這部分的 ViewModel 不用改。
- `TodayScreenContent` 的 `when`：
  - 在「沒有進行中的行程」前面加一個分支：`error != null && currentTrip == null` 時，顯示錯誤與「重試」。
  - 有行程的那一支：`error != null` 時，項目區換成錯誤與「重試」，不再同時顯示紅字和「今天還沒有行程」。
- 頁面的 `error` 之後只代表讀取失敗。快速紀錄存檔失敗不再寫進這裡（見下一段）。

**快速紀錄**
- 面板開關仍由畫面管理。ViewModel 存檔成功時多送一個 `TodayEvent.QuickRecordSaved`，畫面收到才關面板。原本「按下儲存、活動與地點都有填就關」的判斷拿掉。
- 存檔失敗的訊息放進 `QuickRecordState.saveError`，顯示在面板裡；不再跳底部提示，因為面板開著時會擋住它。下一次按儲存時清掉。
- `saveQuickRecord` 一開始先看是不是已經在存檔中，是就直接返回。按鈕停用要等畫面重畫才生效，連點兩下仍可能送出兩次，這裡再擋一次。
- 畫面收事件時，底部提示另開 coroutine 顯示。`showSnackbar` 要等提示消失才返回，原本會讓後面的事件排隊：剛加完照片跳出「已加入照片」就按儲存，存檔成功了面板也要等那個提示結束才關。（實作時補的）

**捨棄變更**
- 「有沒有修改」從畫面移到 State 的唯讀屬性 `hasUnsavedChanges`，才能用單元測試驗：
  - 行程（`ItineraryAddEditState`）：新增模式維持原本規則，任一欄有填就算。編輯模式跟讀進來的 `itinerary` 比標題、描述、開始日、結束日、封面；還沒讀到行程時不算。
  - 項目（`EditItemState`）：跟讀進來的 `item` 比活動、地點名稱、地址、座標、備註、日期、到達、離開、照片；還沒讀到項目時不算。
- `EditItemScreen` 加上跟新增項目一樣的返回攔截（左上返回與系統返回）和「捨棄變更？」對話框。新增項目（`AddEditItemScreen`）不動。
- 不在這組：編輯時挑了新封面或新照片又按「捨棄」，挑的檔案仍留在手機。產品規格書「刪除的連帶效果」已經記錄這件事。

**刪照片**：`PhotoGridItem` 選單的「刪除」改成先跳 `DeleteConfirmDialog`，文字跟照片預覽的一樣。照片預覽不動。

**同一筆 commit 要更新的既有文件**
- Today.md：
  - Requirement「讀取失敗時的顯示」與它的兩個情境改成顯示錯誤與重試（MODIFIED）。
  - Requirement「快速紀錄新增一筆今天的項目」：情境「按儲存後面板立刻關閉，不等存檔結果」改成存檔成功才關（MODIFIED）。
  - 待辦的兩列（讀取失敗看不到錯誤、面板不等存檔結果）拿掉。
- ItineraryDetail.md：待辦「縮圖選單刪照片不確認」拿掉，Requirement「管理項目照片」補上縮圖選單也要先確認（MODIFIED）。
- product-spec.md：
  - 建立與編輯行程、新增與編輯項目兩節的離開提醒改成新行為。
  - 行程詳情的已知問題拿掉刪照片那句。
  - 待確認 #8～#11 標成已完成。
- improvement-plan.md：規格調整第 2 批的四項打勾。

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
- 第 2 組識別字：`itemDayOrder`、`GroupItemsByDateUseCase`、`GetMapMarkersUseCase`、`RouteRepositoryImpl`、`InitializeSampleDataUseCase`、`SampleDataRecordRepository`、`SampleDataRecordRepositoryImpl`、`EnsureLocalDataReadyUseCase`、`ItineraryItemDao`
- 第 2 組路徑：
  - `feature/itinerary_item/domain/**`、`feature/itinerary_item/data/local/ItineraryItemDao.kt`
  - `feature/today/presentation/TodayViewModel.kt`
  - `feature/route/domain/usecase/GetMapMarkersUseCase.kt`、`feature/route/data/repository/RouteRepositoryImpl.kt`
  - `feature/memory/data/service/LocalRuleBasedMemoryStoryGenerator.kt`
  - `feature/itinerary/domain/usecase/InitializeSampleDataUseCase.kt`
  - `feature/itinerary/domain/repository/SampleDataRecordRepository.kt`、`feature/itinerary/data/repository/SampleDataRecordRepositoryImpl.kt`、`feature/itinerary/di/ItineraryModule.kt`
- 第 3 組識別字：`TodayViewModel`、`QuickRecordState`、`TodayEvent`、`ItineraryAddEditState`、`EditItemState`、`EditItemScreen`、`PhotoGridItem`
- 第 3 組路徑：
  - `feature/today/presentation/**`
  - `feature/itinerary_add_edit/presentation/ItineraryAddEditContract.kt`、`ItineraryAddEditScreen.kt`
  - `feature/itinerary_item/presentation/EditItemContract.kt`、`EditItemScreen.kt`
  - `core/presentation/components/PhotoGallery.kt`
- 第 4～6 組：動工時補。

---

## 實作計畫 + 驗證紀錄

> 「邏輯」情境先寫測試、跑紅再實作；「UI」情境在手機上確認。改到換頁或返回的組是高風險組，做完要跑 `/device-check`。

### Slice 1 — 第 1 組：用語與小畫面
- **對應 Requirement**：介面用語統一、「今日」分頁換成代表今天的圖示、首頁拿掉最近編輯區塊與足跡按鈕、沒有資料時有「去看行程」按鈕、時間軸的地點名稱完整顯示
- **commit**：`2234536`
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
- **commit**：`02aa8c1`
- **整體狀態**：🟡 九條邏輯情境都有單元測試、先紅後綠並做過突變；今日頁那條的突變只驗到共用規則這一層（見表）。2026-10-28 實機走查：6 條通過，回憶故事只驗了最後一站，地圖無法驗，「行程已經刪光」的升級沒走（交付資料夾 `device-check.md` 第 2 組）
- **高風險組**：是（改到存起來的資料：新增「範例已放過」的紀錄、資料庫讀出的順序）。實作時誤判成否，2026-10-28 補做實機走查

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 同一天的排序規則 | 邏輯 | ✅ | 2026-10-28 | unit test: ItemDayOrderTest#同一天的排序規則 、 ItemDayOrderTest#行程詳情的分組照同一套排序 、 ItineraryDetailViewModelTest#組內依時間排序，沒排時間的排最後；先紅後綠（行程詳情原本就把沒時間的排最後，那條一開始就綠）；突變：共用規則拿掉「有時間的在前」→ 三條都紅；時間相同改成依 id 倒序 → 前兩條紅；實機 2026-10-28：行程詳情把沒排時間的排在當天最後 |
| 今日頁沒排時間的排最後 | 邏輯 | 🟡 | 2026-10-28 | unit test: TodayViewModelTest#沒排時間的項目排在最後面；先紅後綠；突變：共用規則拿掉「有時間的在前」→ 紅。只拿掉今日頁自己那行排序不會紅，因為資料層讀出時已照同一規則排好；實機 2026-10-28 通過，「未排時間」字樣也確認了 |
| 地圖頁沒排時間的排在當天最後 | 邏輯 | ✅ | 2026-10-28 | unit test: GetMapMarkersUseCaseTest#地圖頁沒排時間的排在當天最後；測試直接餵沒排序的項目，不靠資料層；先紅後綠；突變：共用規則拿掉「有時間的在前」→ 紅；實機無法驗：沒設地點搜尋金鑰，新項目沒有座標 |
| 路線沒排時間的排在當天最後 | 邏輯 | ✅ | 2026-10-28 | unit test: RouteRepositoryImplTest#路線沒排時間的排在當天最後；測試直接餵沒排序的項目；先紅後綠；突變同上 → 紅；實機 2026-10-28 通過 |
| 回憶故事的第一站與最後一站 | 邏輯 | ✅ | 2026-10-28 | unit test: LocalRuleBasedMemoryStoryGeneratorTest#回憶故事的第一站與最後一站；先紅後綠；突變同上 → 紅；實機 2026-10-28 驗了最後一站 |
| 第一次安裝放入範例行程 | 邏輯 | ✅ | 2026-10-28 | unit test: InitializeSampleDataUseCaseTest#第一次安裝放入範例行程；原本就會放，一開始就綠；突變：不放範例 → 紅；實機 2026-10-28 通過 |
| 範例刪光後重開不會回來 | 邏輯 | ✅ | 2026-10-28 | unit test: InitializeSampleDataUseCaseTest#範例刪光後重開不會回來；先紅後綠；突變：放完範例不記紀錄 → 紅；實機 2026-10-28 通過 |
| 從舊版升級、本來就有行程 | 邏輯 | ✅ | 2026-10-28 | unit test: InitializeSampleDataUseCaseTest#從舊版升級、本來就有行程；先紅後綠；突變：已有行程時不記紀錄 → 紅；實機 2026-10-28 通過（新版直接蓋在第 1 組版本的資料上） |
| 從舊版升級、行程已經刪光 | 邏輯 | ✅ | 2026-10-28 | unit test: InitializeSampleDataUseCaseTest#從舊版升級、行程已經刪光；先紅後綠；突變：放完範例不記紀錄、不放範例 → 都紅；實機沒走（要舊版 build 加上刪光的資料） |

資料層讀出的順序（技術設計「排序」最後一點）由 `ItineraryItemRepositoryContract#項目依日期再依抵達或離開時間排序，沒有時間的排在當天最後，相同時保留新增順序` 驗證，資料庫版、記憶體版、舊版 JSON 版三種都跑；先紅後綠；突變：資料庫查詢拿掉 `IS NULL` 那段 → 資料庫版紅，共用規則拿掉「有時間的在前」→ 記憶體版、舊版 JSON 版紅；「相同時保留新增順序」也改壞過：資料庫查詢改成 `rowid DESC`、共用規則加上依 id 倒序 → 三種都紅。

### Slice 3 — 第 3 組：存檔與防呆
- **對應 Requirement**：今日頁讀取失敗時可以重試、快速紀錄存檔成功才關閉、編輯沒存就離開會提醒、刪照片一律先確認
- **commit**：尚未 commit
- **整體狀態**：✅ 實作完成（2026-10-28）。11 條邏輯情境都有單元測試，先紅後綠（4 條在骨架上就綠，見表），每條都做過突變；2026-10-28 實機走查 17 條，沒有失敗，「每一欄都算」兩條只在手機上走了一欄，其餘欄靠單元測試（交付資料夾 `device-check.md` 第 3 組）
- **高風險組**：是（改到返回與面板關閉）

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 讀取行程失敗時顯示錯誤與重試 | UI | ✅ | 2026-10-28 | 實機（SM-S9110，臨時改成第 1 次讀行程失敗的版本，驗完裝回正式版、沒有 commit）：「讀取失敗：磁碟已滿」與「重試」，沒有「今天沒有旅行中的行程」 |
| 讀取今天的項目失敗時顯示錯誤與重試 | UI | ✅ | 2026-10-28 | 實機（同上，第 2 次讀項目失敗）：行程頁首照常，項目區顯示錯誤與「重試」，沒有「今天還沒有行程」與「新增第一筆」 |
| 按重試後重新讀取 | 邏輯 | ✅ | 2026-10-28 | unit test: TodayViewModelTest#按重試後重新讀取；既有程式本來就支援，一開始就綠；突變：重新讀取時不清錯誤 → 紅；實機 2026-10-28：按重試後錯誤消失、列出項目 |
| 存檔成功才關閉面板 | 邏輯 | ✅ | 2026-10-28 | unit test: TodayViewModelTest#存檔成功才關閉面板；先紅後綠；突變：成功時不送關閉面板、不清空表單、不跳提示、不重新讀取 → 紅；反向：先跳提示、最後才送關閉面板 → 維持綠；實機 2026-10-28：面板關閉、再打開是空白、出現「已加入今日紀錄」、清單出現新項目 |
| 存檔中不能再按 | 邏輯 | ✅ | 2026-10-28 | unit test: TodayViewModelTest#存檔中不能再按；先紅後綠（實作前連按兩次會存出兩筆）；突變：存檔中不標成存檔中、拿掉防重複 → 紅；實機 2026-10-28（存檔等 3 秒的臨時版）：按鈕停用並轉圈、面板開著，再按沒有反應 |
| 存檔失敗時留在面板 | 邏輯 | ✅ | 2026-10-28 | unit test: TodayViewModelTest#存檔失敗時留在面板；先紅後綠；突變：失敗也關面板、訊息少了「儲存失敗：」、清掉已填內容、按鈕不恢復、再按時不清錯誤、錯誤寫進頁面的 error → 紅；反向：驗證通過、開始存檔時才清錯誤 → 維持綠。「頁面的 error 保持空白」不在情境裡，依技術設計加，2026-10-28 審查時決定保留；實機 2026-10-28（臨時版）：面板裡顯示「儲存失敗：磁碟已滿」、內容都在、再按時錯誤先消失（沒加照片） |
| 沒填活動時不關閉面板 | 邏輯 | ✅ | 2026-10-28 | unit test: TodayViewModelTest#沒填活動時不關閉面板；既有程式本來就不存，一開始就綠；突變：沒填活動也照存、提示改字 → 紅；實機 2026-10-28 通過 |
| 編輯行程沒改就離開 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryAddEditViewModelTest#編輯行程沒改就離開；骨架一律回「沒改」，一開始就綠；突變：編輯模式誤用新增的規則（有填就算）→ 紅；實機 2026-10-28 通過 |
| 編輯行程有修改時離開會提醒 | UI | ✅ | 2026-10-28 | 實機（SM-S9110）：改標題後左上返回、系統返回都跳「捨棄變更？」；「繼續編輯」留在表單、改過的標題還在；「捨棄」回到詳情、標題沒存 |
| 改回原樣不算修改 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryAddEditViewModelTest#改回原樣不算修改；一開始就綠，原因同上；突變同上 → 紅；實機 2026-10-28 通過 |
| 行程的每一欄都算 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryAddEditViewModelTest#行程的每一欄都算；先紅後綠；突變：標題、描述、開始日、結束日、封面各拿掉一欄不比 → 都紅 |
| 編輯項目沒改就離開 | 邏輯 | ✅ | 2026-10-28 | unit test: EditItemViewModelTest#編輯項目沒改就離開；一開始就綠，原因同編輯行程；突變：地址的空字串跟沒填當成不一樣、讀到項目就一律算有修改 → 紅；實機 2026-10-28 通過（項目沒填地址） |
| 編輯項目有修改時離開會提醒 | UI | ✅ | 2026-10-28 | 實機（SM-S9110）：加備註後左上返回、系統返回都跳「捨棄變更？」；「繼續編輯」留在表單、備註還在；「捨棄」後重新打開，備註沒存 |
| 項目的每一欄都算 | 邏輯 | ✅ | 2026-10-28 | unit test: EditItemViewModelTest#項目的每一欄都算；先紅後綠；突變：活動、地點名稱、地址、備註、日期、到達、離開、照片各拿掉一欄不比 → 都紅。座標也有比，但情境沒列、沒有測試 |
| 新增時照舊 | 邏輯 | ✅ | 2026-10-28 | 新增行程：unit test: ItineraryAddEditViewModelTest#新增時照舊；先紅後綠；突變：新增時一律算有修改、不看標題、不看封面 → 紅。新增項目沒改程式，實機 2026-10-28 抽查：沒填直接離開、填了活動會跳提醒 |
| 從縮圖選單刪照片要先確認 | UI | ✅ | 2026-10-28 | 實機（SM-S9110，東京 3 日遊「林蔭參道散步」的 3 張範例照片）：跳「刪除照片」與情境原句；「取消」還是 3 張，「刪除」剩 2 張 |
| 從照片預覽刪照片照舊先確認 | UI | ✅ | 2026-10-28 | 實機（同上）：跳同一個確認；「刪除」後預覽關閉、少一張 |

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
- **2026-10-28** ADDED: 第 2 組的 9 條情境與技術設計；Requirement「沒排時間的項目排在當天最後」補上回憶故事與「時間相同時先建立的在前」。實作完成，單元測試先紅後綠並做過突變
- **2026-10-28** ADDED: 第 3 組的 19 條情境與技術設計；Requirement「今日頁讀取失敗時可以重試」補上讀取今天的項目失敗也要能重試。第 2 組改標成高風險組（改到存起來的資料）
- **2026-10-28** 第 3 組實作完成，邏輯情境先紅後綠並做過突變，實機走查 17 條沒有失敗。技術設計補一條：今日頁的底部提示另開 coroutine 顯示，不讓關面板等提示消失

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

#### Scenario: 紀錄寫不進去也照常啟動 <!-- ADDED 2026-10-29 -->
- GIVEN 手機上有 1 趟「北海道」，沒有「範例已放過」的紀錄，而且這筆紀錄寫不進去（例如儲存空間滿了）
- WHEN 使用者打開 App
- THEN 照常啟動，行程列表列出「北海道」，不顯示「資料準備失敗」
- AND 全新安裝時紀錄寫不進去，也照常列出兩趟範例；下次啟動已經有行程，不會再放一次

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
- AND 這時往下滑、點面板外面或按返回，面板都不會關（2026-10-29 審查補：存檔中關掉面板，失敗時會看不到錯誤）

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

#### Scenario: 畫面重建後修改還在 <!-- ADDED 2026-10-29 -->
- GIVEN 編輯「清水寺」時在備註加上「記得買御守」；或編輯「京都大阪 5 日遊」時把標題改成「京都 5 日遊」；或新增行程時標題打了「東京 3 日遊」
- WHEN 畫面重建（轉螢幕、切換深色模式、改字體大小等）
- THEN 剛才的修改都還在，不會被存檔或草稿裡的舊內容蓋掉
- AND 這時按返回照樣跳「捨棄變更？」

#### Scenario: 連按儲存只存一次 <!-- ADDED 2026-10-29 -->
- GIVEN 新增行程填好「東京 3 日遊」與日期；或編輯「清水寺」改了備註；或新增項目填好「清水寺」（複查補）
- WHEN 使用者很快地連按兩下「儲存」
- THEN 只建立一趟「東京 3 日遊」；編輯項目只回到上一頁一次；新增項目只新增一筆「清水寺」
- AND 「捨棄變更？」按「捨棄」時，對話框先關掉再離開，連點兩下也只退一頁

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

細節（2026-10-28）：新增與編輯行程儲存時都要兩個日期都有填；錯誤一次只顯示一個，先標題、再日期。行程沒填日期、或只填了一個，詳情頁 SHALL 在時間軸的位置顯示提示與「設定日期」按鈕，按下打開這趟行程的編輯；補完回到詳情，提示換成時間軸。

#### Scenario: 新增行程沒選日期不能儲存
- GIVEN 新增行程填了標題「東京 3 日遊」，開始、結束日期都沒選
- WHEN 使用者按「儲存」
- THEN 不建立行程，結束日期欄下方顯示「請選擇開始與結束日期」

#### Scenario: 只選了開始日期也不能儲存
- GIVEN 新增行程填了標題「東京 3 日遊」，開始日期選 2026/11/10，結束日期沒選
- WHEN 使用者按「儲存」
- THEN 不建立行程，顯示「請選擇開始與結束日期」

#### Scenario: 補上日期後就能儲存
- GIVEN 剛才因為沒選日期存不了，畫面顯示「請選擇開始與結束日期」
- WHEN 使用者把結束日期選成 2026/11/12
- THEN 錯誤訊息消失
- AND 再按「儲存」，建立「東京 3 日遊」，日期 2026/11/10–2026/11/12

#### Scenario: 標題和日期都沒填時先提示標題
- GIVEN 新增行程什麼都沒填
- WHEN 使用者按「儲存」
- THEN 標題欄顯示「標題不可為空」，日期欄不顯示錯誤，不建立行程

#### Scenario: 編輯沒日期的舊行程要先補日期
- GIVEN 舊行程「北海道」沒有日期
- WHEN 使用者打開編輯行程，只把標題改成「北海道 5 日遊」就按「儲存」
- THEN 不儲存，顯示「請選擇開始與結束日期」
- AND 選了 2026/12/01–2026/12/05 再按「儲存」，標題與日期都存進去

#### Scenario: 編輯有日期的行程照舊
- GIVEN 「京都大阪 5 日遊」的日期是 2026/10/01–2026/10/05
- WHEN 使用者把標題改成「京都 5 日遊」後按「儲存」
- THEN 照常儲存，日期不變

#### Scenario: 沒日期的行程在詳情頁提示補日期
- GIVEN 舊行程「北海道」沒有日期
- WHEN 使用者打開它的行程詳情
- THEN 原本時間軸的位置顯示「這趟行程的日期還沒設定完整」「設定日期後才能新增項目，也才會出現在今日與回憶。」與「設定日期」按鈕
- AND 按「設定日期」打開「北海道」的編輯行程

#### Scenario: 只填了一個日期的行程也提示
- GIVEN 舊行程「北海道」只填了開始日期 2026/12/01
- WHEN 使用者打開它的行程詳情
- THEN 一樣顯示「這趟行程的日期還沒設定完整」與「設定日期」

#### Scenario: 補完日期回到詳情後提示消失
- GIVEN 從提示打開「北海道」的編輯行程，選了 2026/12/01–2026/12/05 並儲存
- WHEN 回到行程詳情
- THEN 提示不見，改顯示時間軸「總覽」與「第 1 天」到「第 5 天」

#### Scenario: 有日期的行程不顯示提示
- GIVEN 「京都大阪 5 日遊」的日期是 2026/10/01–2026/10/05
- WHEN 使用者打開它的行程詳情
- THEN 不顯示「這趟行程的日期還沒設定完整」，照常顯示時間軸

**第 5 組：路線**

### Requirement: 路線分天，分享可讀的文字 <!-- ADDED 2026-10-27 -->

路線 SHALL 依天分段；分享內容 SHALL 是「第 1 天：伏見稻荷 → 金閣寺 → 嵐山」這種可讀的文字，不是程式格式。地圖頁與路線頁合成一頁，留到做真地圖時。（待確認 #5）

細節（2026-10-28）：每天照當天項目的順序列出地點，編號每天從 1 開始；同一天連續在同一個地點只列一次，離開再回來、或不同天再去，都照實列出。「第 N 天」依行程開始日算，跟行程詳情的時間軸一樣；沒有項目的天不列；行程日期範圍外的項目（縮短範圍後留下的）用日期代替「第 N 天」。分享的文字第一行是行程標題，之後每天一行。資訊卡的「N 個地點」算不同地點的數量。「至少要有兩個不同的地點」照舊。

#### Scenario: 路線依天分段
- GIVEN 「京都大阪 5 日遊」是 2026/10/14–2026/10/18，10/14 有 09:00 伏見稻荷、18:00 祇園，10/15 有 10:00 金閣寺
- WHEN 使用者打開路線頁
- THEN 先是「第 1 天 · 2026/10/14」，下面依序是 1 伏見稻荷、2 祇園
- AND 接著是「第 2 天 · 2026/10/15」，下面是 1 金閣寺

#### Scenario: 沒有項目的天不列
- GIVEN 同一趟行程只有 10/14 的伏見稻荷與 10/16 的嵐山
- WHEN 使用者打開路線頁
- THEN 只有「第 1 天」與「第 3 天」兩段，沒有「第 2 天」

#### Scenario: 不同天去同一個地點都會列出
- GIVEN 10/14 有 08:00 飯店、10:00 伏見稻荷，10/17 有 10:00 金閣寺、20:00 飯店
- WHEN 使用者打開路線頁
- THEN 第 1 天是飯店、伏見稻荷；第 4 天是金閣寺、飯店

#### Scenario: 同一天連續在同一個地點只列一次
- GIVEN 10/15 有 10:00 在金閣寺參觀、11:00 在金閣寺拍照、14:00 嵐山
- WHEN 使用者打開路線頁
- THEN 第 2 天是 1 金閣寺、2 嵐山

#### Scenario: 同一天離開再回來會列兩次
- GIVEN 10/14 有 08:00 飯店、10:00 伏見稻荷、20:00 飯店
- WHEN 使用者打開路線頁
- THEN 第 1 天是 1 飯店、2 伏見稻荷、3 飯店

#### Scenario: 分享可讀的文字
- GIVEN 10/14 有伏見稻荷、祇園，10/15 有金閣寺、嵐山，10/16 只有清水寺
- WHEN 使用者在路線頁按分享
- THEN 分享出去的文字是四行：「京都大阪 5 日遊」「第 1 天：伏見稻荷 → 祇園」「第 2 天：金閣寺 → 嵐山」「第 3 天：清水寺」
- AND 文字裡沒有座標、地址或 JSON 的大括號

#### Scenario: 行程範圍外的項目用日期標示
- GIVEN 行程改成 2026/10/14–2026/10/16，10/14 有伏見稻荷，10/17 還留著嵐山
- WHEN 使用者打開路線頁
- THEN 嵐山那段的標題是「2026/10/17」，不寫「第 4 天」
- AND 分享的文字是「京都大阪 5 日遊」「第 1 天：伏見稻荷」「2026/10/17：嵐山」三行

#### Scenario: 資訊卡算不同地點的數量
- GIVEN 第 1 天是飯店、伏見稻荷，第 4 天是金閣寺、飯店
- WHEN 使用者打開路線頁
- THEN 資訊卡寫「3 個地點」

**第 6 組：標籤與手勢**

### Requirement: 點標籤篩選同標籤的項目 <!-- ADDED 2026-10-27 -->

在行程詳情點項目上的標籤 SHALL 只列出這趟行程裡有同一個標籤的項目；再點一次 SHALL 取消篩選。（待確認 #14）

細節（2026-10-28 動工時補）：點標籤時時間軸回到「全部」，看整趟行程；篩選中再選某一天，兩個條件一起套用。一次只篩一個標籤，點別的標籤就換過去。列表上方顯示篩選條，按 ✕ 也能取消。篩選留到使用者取消為止，刪除、編輯、新增項目回來都不會被洗掉；離開行程詳情再進來就不保留。多選模式時點標籤等於點卡片。

以下情境的資料：京都大阪 5 日遊（2026/10/14–2026/10/18）有三個項目：10/14 一蘭拉麵（標籤 #拉麵 #宵夜）、10/14 伏見稻荷（#神社）、10/16 天下一品（#拉麵）。

#### Scenario: 點標籤只列出同標籤的項目
- WHEN 使用者點一蘭拉麵卡片上的 #拉麵
- THEN 列表只剩一蘭拉麵與天下一品，分在 2026/10/14、2026/10/16 兩個日期下
- AND 列表上方出現篩選條「#拉麵」，卡片上的 #拉麵 顯示成選中的樣子

#### Scenario: 再點一次取消篩選
- GIVEN 正在篩 #拉麵
- WHEN 使用者再點任一張卡片上的 #拉麵，或按篩選條的 ✕
- THEN 三個項目都回到列表
- AND 篩選條消失

#### Scenario: 點別的標籤換成新的篩選
- GIVEN 正在篩 #拉麵
- WHEN 使用者點一蘭拉麵卡片上的 #宵夜
- THEN 列表只剩一蘭拉麵
- AND 篩選條寫「#宵夜」

#### Scenario: 點標籤時看整趟行程
- GIVEN 使用者在時間軸選了第 1 天（10/14）
- WHEN 使用者點一蘭拉麵卡片上的 #拉麵
- THEN 時間軸回到「全部」
- AND 列表有 10/14 的一蘭拉麵與 10/16 的天下一品

#### Scenario: 選日期還在讀取時點標籤 <!-- ADDED 2026-10-29 -->
- GIVEN 使用者在時間軸點了第 3 天（10/16），這天的資料還沒讀回來
- WHEN 使用者馬上點一蘭拉麵卡片上的 #拉麵
- THEN 資料讀回來後，時間軸是「全部」，列表有 10/14 的一蘭拉麵與 10/16 的天下一品
- AND 不會只剩 10/16 的天下一品

#### Scenario: 篩選中選某一天，兩個條件一起套用
- GIVEN 正在篩 #拉麵
- WHEN 使用者在時間軸點第 3 天（10/16）
- THEN 列表只剩天下一品，篩選條還在
- AND 再點「全部」，列表回到一蘭拉麵與天下一品

#### Scenario: 刪除項目後篩選還在
- GIVEN 正在篩 #拉麵
- WHEN 使用者刪掉天下一品
- THEN 篩選條還在，列表只剩一蘭拉麵

#### Scenario: 編輯項目回來後篩選還在
- GIVEN 正在篩 #拉麵
- WHEN 使用者把伏見稻荷的備註加上 #拉麵，存檔回到行程詳情
- THEN 篩選條還在，列表是一蘭拉麵、伏見稻荷、天下一品

#### Scenario: 篩不到項目時說明原因
- GIVEN 正在篩 #拉麵，而且 10/15 另有金閣寺、竹林兩個沒有標籤的項目（只有這條情境加這兩個；2026-10-29 審查補，原本 10/15 沒有項目，拿掉標籤條件也篩不出東西，驗不到篩選）
- WHEN 使用者在時間軸點第 2 天（10/15，這天沒有拉麵）
- THEN 列表顯示「沒有 #拉麵 的項目」，沒有新增第一筆的按鈕
- AND 篩選條還在
- AND 上方的當日摘要卡照樣算整天：10/15 排了兩個項目就寫「2 個地點」，不出現「新增這天」
- AND 時間軸 10/15 那格的數量也照樣算這兩個項目

#### Scenario: 離開行程詳情再進來不保留篩選
- GIVEN 正在篩 #拉麵
- WHEN 使用者返回行程列表，再打開京都大阪 5 日遊
- THEN 三個項目都列出，沒有篩選條

#### Scenario: 多選模式時點標籤是選取
- GIVEN 行程詳情在多選模式
- WHEN 使用者點一蘭拉麵卡片上的 #拉麵
- THEN 一蘭拉麵被選取，跟點卡片一樣；列表不篩選

#### Scenario: 全形的＃也算標籤
（2026-10-29 實機走查時補：注音鍵盤打出的是全形「＃」，不認的話用注音鍵盤寫的標籤一個都不會產生）
- GIVEN 使用者用注音鍵盤寫備註，# 打出來是全形的 ＃
- WHEN 項目存檔，備註是「排隊 30 分＃拉麵 #宵夜」
- THEN 抓出「拉麵」與「宵夜」兩個標籤

### Requirement: 卡片可以左右滑 <!-- ADDED 2026-10-27 -->

行程卡片與項目卡片 SHALL 可以左滑刪除（先確認）、右滑編輯；原本的選單 SHALL 照樣能用，不會滑的人也能操作。（改善計畫「UI 改善」#8）

細節（2026-10-28 動工時補）：只做行程列表的行程卡片與行程詳情的項目卡片；今日頁的卡片沒有刪除與編輯，不做。滑動時卡片後面露出要做的動作，滑過門檻放手才算。刪除跳的是跟選單同一個確認。多選模式時不能滑。

#### Scenario: 左滑項目卡片先確認再刪除
- GIVEN 京都大阪 5 日遊的行程詳情有伏見稻荷
- WHEN 使用者把伏見稻荷的卡片往左滑，放手
- THEN 跳出跟選單「刪除」同一個確認：「確認刪除」「確定要刪除「伏見稻荷」嗎？此操作無法復原。」
- AND 按「刪除」後伏見稻荷從列表消失

#### Scenario: 刪除確認按取消，卡片回到原位
- GIVEN 左滑伏見稻荷後跳出刪除確認
- WHEN 使用者按「取消」
- THEN 卡片回到原位，伏見稻荷還在

#### Scenario: 右滑項目卡片打開編輯
- WHEN 使用者把伏見稻荷的卡片往右滑，放手
- THEN 打開伏見稻荷的編輯項目頁
- AND 從編輯頁返回後，卡片在原位

#### Scenario: 滑動時看得到要做什麼
- WHEN 使用者把卡片往左滑一點，還沒放手
- THEN 卡片後面露出錯誤色的底，有垃圾桶圖示與「刪除」
- AND 往右滑時露出主色系的底，有鉛筆圖示與「編輯」

#### Scenario: 滑不夠遠就放手不會觸發
（2026-10-29 改：元件放手時也看速度，實機證實快速甩約 38dp 就會觸發；使用者決定保留元件行為，改寫情境）
- WHEN 使用者把卡片慢慢往左或往右拖一點（不到 56dp）就放手
- THEN 卡片彈回原位，不跳確認、不換頁
- AND 手指很快地甩一下，就算距離很短也算滑動，跟滑過門檻一樣跳確認或換頁

#### Scenario: 滑動中轉螢幕不會再觸發 <!-- ADDED 2026-10-29 -->
- GIVEN 使用者剛右滑放手打開編輯頁，或卡片拖到一半還沒放手
- WHEN 這時轉螢幕
- THEN 畫面回來後卡片在原位，不會再打開一次編輯頁或跳刪除確認

#### Scenario: 左滑行程卡片先確認再刪除
- GIVEN 行程列表有京都大阪 5 日遊
- WHEN 使用者把京都大阪 5 日遊的卡片往左滑，放手
- THEN 跳出跟選單「刪除」同一個確認：「刪除行程」「確定要刪除「京都大阪 5 日遊」嗎？這個動作無法復原。」
- AND 按「取消」卡片回到原位；按「刪除」後行程從列表消失

#### Scenario: 右滑行程卡片打開編輯行程
- WHEN 使用者把京都大阪 5 日遊的卡片往右滑，放手
- THEN 打開這趟的編輯行程

#### Scenario: 選單照樣能用
- WHEN 使用者按行程卡片或項目卡片右上的「⋮」
- THEN 跟以前一樣有「編輯」「刪除」（項目卡片另有「標記完成」），按下的結果跟滑動相同

#### Scenario: 多選模式時不能滑
- GIVEN 行程詳情在多選模式
- WHEN 使用者左右滑項目卡片
- THEN 卡片不動，不跳確認也不換頁

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

### 第 4 組

**存檔時檢查日期**
- `ItineraryAddEditViewModel.save()`：標題檢查之後、「結束不能早於開始」之前，加一條：開始或結束日期沒填，`dateError = "請選擇開始與結束日期"` 並返回。新增、編輯模式都檢查。
- 錯誤沿用現有的 `dateError`，顯示在結束日期欄下方；選開始或結束日期時本來就會清掉 `dateError`，不用改。
- 標題沒填時照現在先返回，所以兩個都沒填時只看到標題的錯誤。
- 資料層與 use case 不加限制：`UpdateItineraryUseCase` 也被其他地方用來更新行程，加在那裡會讓沒日期的舊行程連其他更新都失敗。範例資料本來就有日期；舊版搬過來的資料不經過表單。

**詳情頁的提示**
- `ItineraryDetailState` 加唯讀屬性 `needsDates`：讀到行程、且開始或結束日期沒填。用單元測試驗。
- `ItineraryDetailScreen`：`needsDates` 時，在時間軸的位置顯示提示卡（文字見情境），「設定日期」呼叫既有的 `onEditItineraryClick`。有沒有項目都顯示。
- 回到詳情時，畫面重新進入組合會再送一次 `LoadItinerary`（新增項目回來也是靠這個），日期補上後 `needsDates` 就變成否。
- 右下「＋」與新增項目表單裡的「前往設定日期範圍」不動。

**同一筆 commit 要更新的既有文件**
- ItineraryDetail.md：Requirement「讀取行程與項目」補上沒日期時的提示（MODIFIED），情境「日期沒填完整時不顯示時間軸」加一句 AND。
- product-spec.md：
  - 詞彙表「行程」與「資料欄位」表：日期改成必填（舊行程可能沒有）。
  - 建立與編輯行程一節：開始、結束日期改成必填，加上錯誤訊息與檢查順序；入口加上提示卡的「設定日期」。
  - 行程詳情一節的時間軸加上提示；新增項目一節註明只剩舊行程會遇到，已知問題拿掉「沒有日期的行程無法新增項目」。
  - 第 3 組漏改的兩句一起拿掉：建立與編輯行程的「編輯時離開不提醒」、新增與編輯項目的「編輯項目離開不提醒」。
  - 待確認 #4 標成已完成。
- improvement-plan.md：規格調整第 1 批第一項打勾。

### 第 5 組

**路線的資料**
- `Route` 改成 `title`、`days`、`createdFrom`。新增 `RouteDay(date, dayNumber, stops)`：`dayNumber` 是跟行程開始日差幾天加 1，日期在行程範圍外、或行程沒有日期時是 null；`stops` 是當天的 `RouteLocation`，`order` 改成當天從 0 起算。`Route` 加唯讀屬性 `placeCount`：不同地點名稱的數量，給資訊卡用。
- `RouteRepositoryImpl.getRouteForItinerary`：照 `itemDayOrder` 排序後依日期分組；同一組裡只合併「跟前一站同名」的項目。地點名稱照舊用完全相同的字串比對。「至少要有兩個不同的地點」照舊，錯誤訊息不變。
- 只有路線頁與行程詳情的「生成路線」用到路線；後者只看成功或失敗，不用改。

**分享文字**
- 分享的文字在畫面這一層組：`feature/route/presentation/` 加一個組文字的函式，日期用既有的 `DateTimeFormatter.formatDate`（2026/10/17）。domain 層沒有日期格式工具，放在畫面層才不用另寫一份。
- `RouteViewViewModel` 收到分享時改用 `GetRouteForItineraryUseCase` 讀路線、組文字，事件的欄位從 `json` 改名 `text`；`App.kt` 的分享接法不變，只改參數名。
- 不再輸出 JSON，以下一併刪除：`ExportRouteUseCase`、`RouteRepository.exportRoute`、`RouteMapper`、`RouteResponse`、`RouteLocationResponse`、`JsonSerializer.serializeRoute`，以及 DI 與測試組裝裡的對應註冊。

**路線頁**
- 列表依 `days` 分段，每段標題是「第 N 天 · 2026/10/14」，`dayNumber` 是 null 時只寫日期；地點卡的編號用當天的順序。地點卡其他內容（地址、座標、開 Google 地圖）不動。
- 資訊卡的「N 個地點」改用 `placeCount`。

**既有測試**
- `RouteRepositoryImplTest#依日期與時間排序，同名地點只留第一次` 的前提跟新規則衝突（第 1 天、第 3 天都有飯店，以前只留一次），改成照新情境驗。其他路線測試照新的資料形狀改寫取值方式，比對的結果不變。
- `DataLayerAcceptanceContract` 範例行程生成路線那條，原本比對「8 個地點」，改成比對成功與天數。

**同一筆 commit 要更新的既有文件**
- product-spec.md：路線頁一節的「路線怎麼產生」「畫面內容」「分享」與已知問題改成新行為；詞彙表「路線」補上分天；待確認 #5 標成路線分天與分享已完成、地圖合併留到做真地圖。
- improvement-plan.md：規格調整第 1 批第二項註明前半完成、後半留到做真地圖。
- CLAUDE.md：「本專案不適用」表裡 `data/model/*Response` 的說明拿掉「路線的分享內容」。

### 第 6 組

App 沒有設計稿：示意圖沒畫標籤與滑動手勢，產品規格書寫明依待確認事項的決定做。下面的顏色都用 App 現有的主題色。

**標籤篩選（ViewModel）**
- `ItineraryDetailState` 加 `selectedHashtag: String?`（不含 #）。
- Intent 加 `ToggleHashtagFilter(tag)`：點的是目前在篩的標籤就取消（跟 ✕ 一樣，保留選中的日期），不是就換成它並把 `selectedDate` 設成 null。另加 `ClearHashtagFilter` 給篩選條的 ✕。
- 列表的分組統一由一個私有函式算：先套日期、再套標籤（項目的 `hashtags` 裡有同一個 `tag`，完全相同的字串）。`loadItinerary`、`selectDate` 與兩個新 Intent 都走它。10/16 刪掉的舊版半套，就是 `loadItinerary` 與 `selectDate` 各算各的、只帶了日期，一重新讀取就洗掉標籤；這兩條路徑都要有測試和突變。
- 新 Intent 不重新讀資料，用 `allGroupedItems` 裡的項目重算。`selectDate` 照舊重新讀取，不改它的行為。
- `allGroupedItems` 不套標籤，所以時間軸每天的數量照舊算整天（跟選日期時一樣）；選了某一天時的當日摘要也改讀 `allGroupedItems`，一律算整天。原本讀 `groupedItems`（數字跟著列表），2026-10-29 實機走查時篩不到項目的那天寫「0 個地點」並出現「新增這天」，看起來像那天沒排東西，使用者決定改成算整天。沒有篩標籤時兩者那天的項目相同，行為不變。
- 多選模式不在 ViewModel 擋：畫面在多選模式不給標籤點擊，點到就是點卡片。
- 抓標籤的規則（`HashtagRepositoryImpl`）開頭改成半形 # 或全形 ＃ 都認，後面的字照舊。新增與編輯項目都經過它，已存的項目要重新存檔才會抓出全形 ＃ 的標籤。

**標籤的畫面**
- `HashtagRow`、`HashtagChip` 加 `selectedTag` 與 `onTagClick`；`onTagClick` 是 null 時跟現在一樣只顯示、點到等於點卡片。選中的標籤用 `primary` 底、`onPrimary` 字，沒選中照舊 `secondaryContainer`。KDoc 裡「依標籤篩選的功能拿掉了」那句改掉。
- `ItemCard` 加 `selectedHashtag`、`onHashtagClick`，不給預設值（只有行程詳情一個呼叫端）；多選模式傳 null。
- 篩選條放在提示卡（`MissingDatesCard`）之後、當日摘要之前：一個選中的 `InputChip`，文字「#拉麵」、尾端 ✕，按下送 `ClearHashtagFilter`。
- 篩到沒有項目時（`groupedItems` 空、`selectedHashtag` 不是 null），列表顯示「沒有 #拉麵 的項目」，不顯示 `EmptyDatePlanningState`。

**滑動**
- 用 material3 的 `SwipeToDismissBox`（專案目前解析到的 Compose Multiplatform material3 1.9.0，在 commonMain 可用），包成一個共用元件 `SwipeActionsBox(onSwipeToDelete, onSwipeToEdit, enabled, content)` 放 `core/presentation/components/`，行程卡片與項目卡片都用它。
- 放手後在 `onDismiss(direction)` 處理：往左（`EndToStart`）呼叫刪除的 callback，往右（`StartToEnd`）呼叫編輯的 callback，接著 `state.reset()` 讓卡片回到原位。刪除後卡片消失靠的是列表重新讀取，不是靠滑動元件把卡片移走，所以確認時按取消，卡片本來就會回原位。舊的 `confirmValueChange` 寫法在 1.9.0 已標成不建議使用，不用它。放手時卡片會先滑出再回來，順不順在實機看。
- 門檻用元件預設：慢慢拖要滑過 56dp；放手時速度夠快也算（元件看放手速度，2026-10-30 實機證實甩約 38dp 就會觸發，使用者決定保留）。
- 卡片後面的底依滑動方向畫：往左是 `errorContainer` 底，靠右放刪除圖示與「刪除」；往右是 `primaryContainer` 底，靠左放編輯圖示與「編輯」。圓角跟卡片一樣用 `CardStyle.cornerRadius`。
- 行程卡片：包在 `ItineraryDashboardCard` 裡，三個區塊共用；刪除走現有的 `onDelete`（行程列表的確認框），編輯走 `onEdit`。
- 項目卡片：包在 `ItemCard` 右側的卡片外面，左邊的時間軸不跟著滑；刪除走現有的 `onDelete`（行程詳情的確認框），編輯走 `onEdit`。多選模式時 `enabled = false`。
- ViewModel 不用改：刪除照舊先確認，右滑編輯是單純換頁，照 rules 直接呼叫 callback。
- 列表都有 key（行程詳情是項目 id，行程列表三個區塊是 `current_`、`upcoming_`、`all_` 加行程 id），刪除後滑動狀態不會跑到別張卡片上。
- 項目卡片裡的標籤列、展開後的照片列可以橫向捲動，在那兩列上橫拖是捲動，在卡片其他地方橫拖才是滑動；上下捲列表時不會誤觸。這兩點不寫成情境，實機走查時一起看。

**既有測試**：不受影響。

**同一筆 commit 要更新的既有文件**
- ItineraryDetail.md：「畫面沒接上的依標籤篩選」那段、待辦表的標籤列、變更紀錄，改成標籤篩選已照規格重做；補上項目卡片可以滑。
- ItineraryList.md：行程卡片可以滑。
- product-spec.md：
  - 行程詳情一節：標籤可以點來篩選（取代「只顯示，不能點」）、項目卡片可以滑。
  - 行程列表一節：行程卡片可以滑。
  - 「標籤」一節：「目前標籤只用來顯示」改成行程詳情可以依標籤篩選。
  - 已知問題 #11 補一句：標籤後來改成可以點來篩選（2026-10-28）。
  - 待確認 #14 與 UI 改善「卡片左右滑」標成已完成。
- improvement-plan.md：規格調整裡標籤與滑動兩項打勾，UI 改善 #8 改成已做。
- `HashtagRow.kt` 的 KDoc（見上）。

### 第 4 步審查後的修正（2026-10-30）

六組做完後的獨立審查（交付資料夾 `spec-review.md`）與實機走查（`device-check.md` 第 4 步）找到的問題，使用者逐條決定後一起修：

- **畫面重建不重讀**：`EditItemViewModel.loadItem`、`ItineraryAddEditViewModel.loadItinerary` 讀過同一筆就不再讀；`loadDraft` 用 `draftLoaded` 只讀一次。原因是畫面在轉螢幕、切深色模式、改字體、分割畫面等情況會重建並再送一次讀取，ViewModel 還在，重讀會蓋掉還沒存的修改。固定螢幕方向擋不住其他情況，Android 16 在大螢幕上也會忽略方向固定，所以不靠固定方向。行程詳情「回來時重讀」是刻意的，不套用。
- **連按儲存**：新增／編輯行程、編輯項目、新增項目（複查補）的 `save()` 開頭在 `isLoading` 時直接返回，存好後不把 `isLoading` 改回 false（畫面要離開了）。「捨棄變更？」按「捨棄」時先關對話框再離開（編輯項目、新增項目、新增／編輯行程）。換頁的通用擋法（只在頂層是自己時才退）改動較大，記進待辦。
- **快速紀錄存檔中不能關面板**：`rememberModalBottomSheetState(confirmValueChange)` 在存檔中拒絕收合，`onDismissRequest` 也擋掉；面板開關改用 `rememberSaveable`，存檔中轉螢幕面板還在（複查補）。
- **範例紀錄寫不進去照常啟動**：`InitializeSampleDataUseCase` 的兩處 `markSampleDataAdded()` 不再 `getOrThrow()`，跟舊資料搬移標記同一種做法。下次啟動已經有行程，會走「補寫紀錄」那條，不會重複放。
- **選日期讀取途中點標籤**：`selectDate` 讀完後用當下的 `selectedDate`，跟 `loadItinerary` 一樣。
- **滑動狀態不跨轉螢幕保存**：`SwipeActionsBox` 改用 `remember { SwipeToDismissBoxState(Settled, positionalThreshold) }`，不用會存檔的 `rememberSwipeToDismissBoxState`。
- **快速甩一下算滑動**：保留元件行為（放手速度超過門檻就算），只改情境。
- **時間軸「第 N 天」只顯示一行**：`maxLines = 1, softWrap = false`，字放大時「第 10 天」不折行；「總覽」那格撐高度的隱形「第 1 天」也一樣只佔一行，不然字放大時總覽那格會比其他天高。代價是字很大、格子停在最小寬度時，「天」右邊可能被切掉一點（比錯位輕微）；徹底解法是格寬下限一起量「第 N 天」，記進待辦。
- **路線頁的連接線**：編號方塊上下改用 `VerticalDivider`（2dp 粗、16dp 高）；原本 `HorizontalDivider` 放在 2dp 寬的格子裡只剩一個點，規格調整前就是這樣。

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
- 第 4 組識別字：`ItineraryAddEditViewModel`、`ItineraryDetailState`、`ItineraryDetailScreen`
- 第 4 組路徑：
  - `feature/itinerary_add_edit/presentation/ItineraryAddEditViewModel.kt`
  - `feature/itinerary_detail/presentation/**`
- 第 5 組識別字：`Route`、`RouteDay`、`RouteLocation`、`RouteRepositoryImpl`、`RouteViewViewModel`、`RouteViewEvent`、`RouteViewScreen`
- 第 5 組路徑：
  - `feature/route/**`（地圖頁用的 `GetMapMarkersUseCase`、`MapMarker` 不動）
  - `core/data/local/JsonSerializer.kt`
  - `App.kt`
- 第 6 組識別字：`ItineraryDetailState`、`ItineraryDetailIntent`、`ItineraryDetailViewModel`、`ItineraryDetailScreen`、`HashtagRow`、`HashtagChip`、`ItemCard`、`SwipeActionsBox`、`ItineraryDashboardCard`
- 第 6 組路徑：
  - `feature/itinerary_detail/presentation/**`
  - `feature/itinerary_list/presentation/ItineraryListScreen.kt`
  - `core/presentation/components/HashtagRow.kt`、`ItemCard.kt`、`SwipeActionsBox.kt`

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
| 回憶故事沒有多餘空格 | 邏輯 | ✅ | 2026-10-27 | unit test: LocalRuleBasedMemoryStoryGeneratorTest#回憶故事沒有多餘空格；先紅後綠；突變：標題「的」前後、摘要地名前後、敘事句子之間、最後一站地名前後加回空格 → 紅。2026-10-30 第 4 步審查補：另驗「沒有開始日」「沒有項目」兩種開頭、另一種摘要與結尾（只檢查中文字旁沒有多的空格，不鎖整段文字）；突變：這四個模板各加一個空格 → 都紅。回憶頁的整頁 Preview 假資料也改成沒有空格的寫法 |
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
- **整體狀態**：🟡 十條邏輯情境都有單元測試、先紅後綠並做過突變（「紀錄寫不進去也照常啟動」與今日頁排序的補強是 2026-10-30 第 4 步審查補的）。2026-10-28 實機走查：6 條通過，回憶故事只驗了最後一站，地圖無法驗，「行程已經刪光」的升級沒走（交付資料夾 `device-check.md` 第 2 組）
- **高風險組**：是（改到存起來的資料：新增「範例已放過」的紀錄、資料庫讀出的順序）。實作時誤判成否，2026-10-28 補做實機走查

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 同一天的排序規則 | 邏輯 | ✅ | 2026-10-28 | unit test: ItemDayOrderTest#同一天的排序規則 、 ItemDayOrderTest#行程詳情的分組照同一套排序 、 ItineraryDetailViewModelTest#組內依時間排序，沒排時間的排最後；先紅後綠（行程詳情原本就把沒時間的排最後，那條一開始就綠）；突變：共用規則拿掉「有時間的在前」→ 三條都紅；時間相同改成依 id 倒序 → 前兩條紅；實機 2026-10-28：行程詳情把沒排時間的排在當天最後 |
| 今日頁沒排時間的排最後 | 邏輯 | ✅ | 2026-10-30 | unit test: TodayViewModelTest#沒排時間的項目排在最後面；先紅後綠；突變：共用規則拿掉「有時間的在前」→ 紅。2026-10-30 第 4 步審查補：資料來源改成照給的順序回傳（不靠資料層先排好），預期不變；突變：拿掉今日頁自己那行排序 → 紅；實機 2026-10-28 通過，「未排時間」字樣也確認了 |
| 地圖頁沒排時間的排在當天最後 | 邏輯 | ✅ | 2026-10-28 | unit test: GetMapMarkersUseCaseTest#地圖頁沒排時間的排在當天最後；測試直接餵沒排序的項目，不靠資料層；先紅後綠；突變：共用規則拿掉「有時間的在前」→ 紅；實機無法驗：沒設地點搜尋金鑰，新項目沒有座標 |
| 路線沒排時間的排在當天最後 | 邏輯 | ✅ | 2026-10-28 | unit test: RouteRepositoryImplTest#路線沒排時間的排在當天最後；測試直接餵沒排序的項目；先紅後綠；突變同上 → 紅；實機 2026-10-28 通過 |
| 回憶故事的第一站與最後一站 | 邏輯 | ✅ | 2026-10-28 | unit test: LocalRuleBasedMemoryStoryGeneratorTest#回憶故事的第一站與最後一站；先紅後綠；突變同上 → 紅；實機 2026-10-28 驗了最後一站 |
| 第一次安裝放入範例行程 | 邏輯 | ✅ | 2026-10-28 | unit test: InitializeSampleDataUseCaseTest#第一次安裝放入範例行程；原本就會放，一開始就綠；突變：不放範例 → 紅；實機 2026-10-28 通過 |
| 範例刪光後重開不會回來 | 邏輯 | ✅ | 2026-10-28 | unit test: InitializeSampleDataUseCaseTest#範例刪光後重開不會回來；先紅後綠；突變：放完範例不記紀錄 → 紅；實機 2026-10-28 通過 |
| 從舊版升級、本來就有行程 | 邏輯 | ✅ | 2026-10-28 | unit test: InitializeSampleDataUseCaseTest#從舊版升級、本來就有行程；先紅後綠；突變：已有行程時不記紀錄 → 紅；實機 2026-10-28 通過（新版直接蓋在第 1 組版本的資料上） |
| 從舊版升級、行程已經刪光 | 邏輯 | ✅ | 2026-10-28 | unit test: InitializeSampleDataUseCaseTest#從舊版升級、行程已經刪光；先紅後綠；突變：放完範例不記紀錄、不放範例 → 都紅；實機沒走（要舊版 build 加上刪光的資料） |
| 紀錄寫不進去也照常啟動 | 邏輯 | ✅ | 2026-10-30 | unit test: InitializeSampleDataUseCaseTest#紀錄寫不進去也照常啟動 、 InitializeSampleDataUseCaseTest#紀錄寫不進去也照常啟動（全新安裝）；先紅後綠；突變：已有行程時寫不進去就失敗 → 兩條紅，放完範例後寫不進去就失敗 → 全新安裝那條紅（第 4 步審查補） |

資料層讀出的順序（技術設計「排序」最後一點）由 `ItineraryItemRepositoryContract#項目依日期再依抵達或離開時間排序，沒有時間的排在當天最後，相同時保留新增順序` 驗證，資料庫版、記憶體版、舊版 JSON 版三種都跑；先紅後綠；突變：資料庫查詢拿掉 `IS NULL` 那段 → 資料庫版紅，共用規則拿掉「有時間的在前」→ 記憶體版、舊版 JSON 版紅；「相同時保留新增順序」也改壞過：資料庫查詢改成 `rowid DESC`、共用規則加上依 id 倒序 → 三種都紅。

### Slice 3 — 第 3 組：存檔與防呆
- **對應 Requirement**：今日頁讀取失敗時可以重試、快速紀錄存檔成功才關閉、編輯沒存就離開會提醒、刪照片一律先確認
- **commit**：`4c3817a`
- **整體狀態**：✅ 實作完成（2026-10-28）。11 條邏輯情境都有單元測試，先紅後綠（4 條在骨架上就綠，見表），每條都做過突變；2026-10-28 實機走查 17 條，沒有失敗，「每一欄都算」兩條只在手機上走了一欄，其餘欄靠單元測試（交付資料夾 `device-check.md` 第 3 組）
- **高風險組**：是（改到返回與面板關閉）

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 讀取行程失敗時顯示錯誤與重試 | UI | ✅ | 2026-10-28 | 實機（SM-S9110，臨時改成第 1 次讀行程失敗的版本，驗完裝回正式版、沒有 commit）：「讀取失敗：磁碟已滿」與「重試」，沒有「今天沒有旅行中的行程」 |
| 讀取今天的項目失敗時顯示錯誤與重試 | UI | ✅ | 2026-10-28 | 實機（同上，第 2 次讀項目失敗）：行程頁首照常，項目區顯示錯誤與「重試」，沒有「今天還沒有行程」與「新增第一筆」 |
| 按重試後重新讀取 | 邏輯 | ✅ | 2026-10-28 | unit test: TodayViewModelTest#按重試後重新讀取；既有程式本來就支援，一開始就綠；突變：重新讀取時不清錯誤 → 紅；實機 2026-10-28：按重試後錯誤消失、列出項目 |
| 存檔成功才關閉面板 | 邏輯 | ✅ | 2026-10-28 | unit test: TodayViewModelTest#存檔成功才關閉面板；先紅後綠；突變：成功時不送關閉面板、不清空表單、不跳提示、不重新讀取 → 紅；反向：先跳提示、最後才送關閉面板 → 維持綠；實機 2026-10-28：面板關閉、再打開是空白、出現「已加入今日紀錄」、清單出現新項目 |
| 存檔中不能再按 | 邏輯 | ✅ | 2026-10-28 | unit test: TodayViewModelTest#存檔中不能再按；先紅後綠（實作前連按兩次會存出兩筆）；突變：存檔中不標成存檔中、拿掉防重複 → 紅；實機 2026-10-28（存檔等 3 秒的臨時版）：按鈕停用並轉圈、面板開著，再按沒有反應。2026-10-30 第 4 步審查補 AND「存檔中面板不會關」：面板的狀態與關閉要求在存檔中都擋掉，畫面接法沒有單元測試，等實機（要存檔等 3 秒的臨時版） |
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
| 畫面重建後修改還在 | 邏輯 | ✅ | 2026-10-30 | unit test: EditItemViewModelTest#畫面重建後修改還在 、 ItineraryAddEditViewModelTest#畫面重建後修改還在（編輯行程、新增行程的草稿）；先紅後綠；突變：拿掉編輯項目、編輯行程「讀過就不重讀」、拿掉「草稿只讀一次」→ 都紅；實機 2026-10-30 第 4 步走查時失敗（轉螢幕後備註被蓋掉、返回不提醒），修正後重驗通過：轉橫向後備註還是改過的，返回跳「捨棄變更？」 |
| 連按儲存只存一次 | 邏輯 | ✅ | 2026-10-30 | unit test: EditItemViewModelTest#連按儲存只存一次 、 ItineraryAddEditViewModelTest#連按儲存只存一次 、 AddEditItemViewModelTest#連按儲存只存一次（新增項目，複查後使用者決定一起補）；先紅後綠；突變：拿掉儲存開頭的擋重複、存好後把「存檔中」改回來 → 都紅。測試是兩下都送出後才推進排程，記憶體版存檔不會暫停，第二下其實在第一下存完後才跑；「存到一半時第二下進來」靠同一個擋法，沒有直接測。「捨棄」先關對話框是畫面接法，沒有單元測試；實機 2026-10-30：連點兩次「捨棄」只退一頁 |
| 從縮圖選單刪照片要先確認 | UI | ✅ | 2026-10-28 | 實機（SM-S9110，東京 3 日遊「林蔭參道散步」的 3 張範例照片）：跳「刪除照片」與情境原句；「取消」還是 3 張，「刪除」剩 2 張 |
| 從照片預覽刪照片照舊先確認 | UI | ✅ | 2026-10-28 | 實機（同上）：跳同一個確認；「刪除」後預覽關閉、少一張 |

### Slice 4 — 第 4 組：日期必填
- **對應 Requirement**：行程一定要有日期
- **commit**：`f0e4a96`
- **整體狀態**：✅ 實作完成（2026-10-28）。10 條邏輯情境都有單元測試，先紅後綠（3 條在骨架上就綠，見表），每條都做過突變；2026-10-28 實機走查 10 條沒有失敗，舊行程用改版前的 App 建立後覆蓋安裝，等於從舊版升級（交付資料夾 `device-check.md` 第 4 組）
- **高風險組**：是（「設定日期」會換頁打開編輯行程，跟第 1 組「去看行程」同一類；也擋住了原本存得進去的行程）
- **既有測試**：`ItineraryAddEditViewModelTest` 的「建立成功後清掉草稿」「打字後馬上建立」原本只填標題就建立，改成先選日期再建立（2026-10-28 審查時同意），比對的結果不變

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 新增行程沒選日期不能儲存 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryAddEditViewModelTest#新增行程沒選日期不能儲存；先紅後綠；突變：沒選日期也照存、錯誤訊息改字 → 紅；實機 2026-10-28（SM-S9110）：結束日期欄紅框，下方顯示訊息 |
| 只選了開始日期也不能儲存 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryAddEditViewModelTest#只選了開始日期也不能儲存；先紅後綠；突變：兩個都沒填才擋 → 紅 |
| 補上日期後就能儲存 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryAddEditViewModelTest#補上日期後就能儲存；先紅後綠；突變：選結束日期時不清錯誤、補了日期還是擋 → 紅 |
| 標題和日期都沒填時先提示標題 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryAddEditViewModelTest#標題和日期都沒填時先提示標題；原本就先檢查標題，一開始就綠；突變：先檢查日期、兩個錯誤一起顯示 → 紅；反向：「沒填日期」放到「結束不能早於開始」之後 → 維持綠 |
| 編輯沒日期的舊行程要先補日期 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryAddEditViewModelTest#編輯沒日期的舊行程要先補日期；先紅後綠；突變：只在新增時檢查 → 紅 |
| 編輯有日期的行程照舊 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryAddEditViewModelTest#編輯有日期的行程照舊；原本就能存，一開始就綠；突變：編輯一律擋、存檔時把日期清掉 → 紅 |
| 沒日期的行程在詳情頁提示補日期 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryDetailViewModelTest#沒日期的行程在詳情頁提示補日期（驗 `needsDates`）；先紅後綠；突變：一律不提示 → 紅；反向：改用「沒有時間軸範圍就提示」→ 維持綠；實機 2026-10-28：提示卡文字與情境一致，按「設定日期」打開這趟的編輯行程 |
| 只填了一個日期的行程也提示 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryDetailViewModelTest#只填了一個日期的行程也提示；先紅後綠；突變：兩個都沒填才提示 → 紅 |
| 補完日期回到詳情後提示消失 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryDetailViewModelTest#補完日期回到詳情後提示消失；先紅後綠；突變：重新讀取時沒換成新的行程、一律提示 → 紅；實機 2026-10-28：補完日期存檔回到詳情，提示換成時間軸第 1～5 天 |
| 有日期的行程不顯示提示 | 邏輯 | ✅ | 2026-10-28 | unit test: ItineraryDetailViewModelTest#有日期的行程不顯示提示；骨架一律回「不用提示」，一開始就綠；突變：一律提示 → 紅 |

### Slice 5 — 第 5 組：路線
- **對應 Requirement**：路線分天，分享可讀的文字
- **commit**：`65c168b`
- **整體狀態**：✅ 實作完成（2026-10-28）。8 條邏輯情境都有單元測試，對骨架全部先紅後綠，每條都做過突變；2026-10-30 第 4 步實機走查：段落標題、每天從 1 編號、資訊卡、範圍外寫日期都通過，分享選單只看了預覽前兩行；編號方塊上下的連接線原本只剩一個點，改成直線後重看通過（交付資料夾 `device-check.md` 第 4 步）
- **高風險組**：否（不換頁、不動存起來的資料；分享只改文字內容，分享選單照舊）
- **既有測試**（2026-10-28 審查時同意）：`RouteRepositoryImplTest` 的「依日期與時間排序，同名地點只留第一次」刪除，由「不同天去同一個地點都會列出」取代；「路線沒排時間的排在當天最後」「路線跟著行程目前的項目」與 `RouteViewViewModelTest#打開路線頁` 改成照分天的形狀取值，比對的結果不變；`RouteViewViewModelTest#分享路線` 由「分享可讀的文字」取代；`DataLayerAcceptanceContract#範例行程可以生成路線` 從比對「8 個地點」改成「範例行程有安排的每一天都有一段」，答案從範例資料算

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 路線依天分段 | 邏輯 | ✅ | 2026-10-28 | unit test: RouteRepositoryImplTest#路線依天分段；先紅後綠；突變：不分天全部放一段、整趟連續編號 → 紅；段落標題與編號的畫面等實機 |
| 沒有項目的天不列 | 邏輯 | ✅ | 2026-10-28 | unit test: RouteRepositoryImplTest#沒有項目的天不列；先紅後綠；突變：依有安排的天依序編第幾天 → 紅 |
| 不同天去同一個地點都會列出 | 邏輯 | ✅ | 2026-10-28 | unit test: RouteRepositoryImplTest#不同天去同一個地點都會列出；先紅後綠；突變：整趟同名只留第一次（舊做法）→ 紅。2026-10-30 第 4 步審查補「晚上回飯店、隔天從飯店出發」；突變：整趟共用上一站（跨天同名也合併）→ 紅 |
| 同一天連續在同一個地點只列一次 | 邏輯 | ✅ | 2026-10-28 | unit test: RouteRepositoryImplTest#同一天連續在同一個地點只列一次；先紅後綠；突變：連續同名不合併 → 紅 |
| 同一天離開再回來會列兩次 | 邏輯 | ✅ | 2026-10-28 | unit test: RouteRepositoryImplTest#同一天離開再回來會列兩次；先紅後綠；突變：同一天同名只留第一次 → 紅 |
| 分享可讀的文字 | 邏輯 | ✅ | 2026-10-28 | unit test: RouteViewViewModelTest#分享可讀的文字；先紅後綠；突變：沒有標題行、箭頭換成頓號、只有一個地點的天也加箭頭、每個地點後面加地址 → 紅；反向：文字最後多一個換行 → 維持綠；分享選單打開等實機 |
| 行程範圍外的項目用日期標示 | 邏輯 | ✅ | 2026-10-28 | unit test: RouteRepositoryImplTest#行程範圍外的項目用日期標示（`dayNumber`）、RouteViewViewModelTest#行程範圍外的項目用日期標示（分享文字）；先紅後綠；突變：範圍外照樣算第幾天、寫成第 null 天、那天跳過 → 紅。2026-10-30 第 4 步審查補：早於開始日、沒日期的行程（沒有範圍，每天都用日期，這是推論、情境原文只寫範圍外）；突變：早於開始日也算第 N 天、沒日期時從第 1 天算 → 都紅；實機 2026-10-30：結束日改成 10/16 後，10/17、10/18 兩段標題寫日期 |
| 資訊卡算不同地點的數量 | 邏輯 | ✅ | 2026-10-28 | unit test: RouteRepositoryImplTest#資訊卡算不同地點的數量（`placeCount`）；先紅後綠；突變：改算總站數 → 紅；反向：換一種寫法算不同地點 → 維持綠 |

### Slice 6 — 第 6 組：標籤與手勢
- **對應 Requirement**：點標籤篩選同標籤的項目、卡片可以左右滑
- **commit**：`c939b08`（第 4 步審查後的修正尚未提交）
- **整體狀態**：✅ 實作完成（2026-10-29）。10 條邏輯情境都有單元測試（「選日期還在讀取時點標籤」是 2026-10-30 第 4 步審查補的），先紅後綠（「再點一次取消篩選」骨架不篩就符合結果，一開始就綠），每條都做過突變；2026-10-29 實機走查 19 條，摘要卡一處失敗已修正並重驗，「編輯項目回來後篩選還在」只走了沒改就返回，全形 ＃ 沒在手機上驗（交付資料夾 `device-check.md` 第 6 組）
- **高風險組**：是（右滑會換頁打開編輯；左滑會刪資料，雖然先確認）
- **既有測試**：沒有改動

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 點標籤只列出同標籤的項目 | 邏輯 | ✅ | 2026-10-29 | unit test: ItineraryDetailViewModelTest#點標籤只列出同標籤的項目；先紅後綠；突變：不套標籤篩選、不記下正在篩的標籤 → 紅；篩選條與選中樣式等實機 |
| 再點一次取消篩選 | 邏輯 | ✅ | 2026-10-29 | unit test: ItineraryDetailViewModelTest#再點一次取消篩選（再點標籤、按 ✕ 兩條路）；骨架不篩，一開始就綠；突變：再點一次不會取消、按 ✕ 沒反應 → 紅 |
| 點別的標籤換成新的篩選 | 邏輯 | ✅ | 2026-10-29 | unit test: ItineraryDetailViewModelTest#點別的標籤換成新的篩選；先紅後綠；突變：點別的標籤時保留舊標籤 → 紅 |
| 點標籤時看整趟行程 | 邏輯 | ✅ | 2026-10-29 | unit test: ItineraryDetailViewModelTest#點標籤時看整趟行程；先紅後綠；突變：點標籤時沒清掉日期 → 紅 |
| 選日期還在讀取時點標籤 | 邏輯 | ✅ | 2026-10-30 | unit test: ItineraryDetailViewModelTest#選日期還在讀取時點標籤（讀項目延遲 100ms 的替身）；先紅後綠；突變：讀完用一開始點的日期 → 紅（第 4 步審查補） |
| 篩選中選某一天，兩個條件一起套用 | 邏輯 | ✅ | 2026-10-29 | unit test: ItineraryDetailViewModelTest#篩選中選某一天，兩個條件一起套用；先紅後綠；突變：選日期時只套日期（舊版半套的壞法）、選日期時把標籤清掉 → 紅 |
| 刪除項目後篩選還在 | 邏輯 | ✅ | 2026-10-29 | unit test: ItineraryDetailViewModelTest#刪除項目後篩選還在；先紅後綠；突變：重新讀取時只套日期、重新讀取時把標籤清掉 → 紅。2026-10-30 複查補：也驗重新讀取後時間軸 10/14 照樣是 2 個；突變：重新讀取時時間軸也套標籤 → 紅 |
| 編輯項目回來後篩選還在 | 邏輯 | ✅ | 2026-10-29 | unit test: ItineraryDetailViewModelTest#編輯項目回來後篩選還在；先紅後綠；突變：重新讀取時把標籤清掉 → 紅。「重新讀取時只套日期」這條抓不到（編輯後三個項目都有 #拉麵，篩不篩一樣），由上一條負責 |
| 篩不到項目時說明原因 | 邏輯 | ✅ | 2026-10-29 | unit test: ItineraryDetailViewModelTest#篩不到項目時說明原因（驗列表空、篩選還在）；先紅後綠；突變：選日期時把標籤清掉 → 紅；實機 2026-10-29：「沒有 #ramen 的項目」與篩選條都對；摘要卡第一次寫「0 個地點」並出現「新增這天」，改成算整天後重驗通過（「2 個地點」、沒有新增按鈕）。摘要卡讀哪份資料是畫面接法，沒有單元測試。2026-10-30 審查補：測試資料 10/15 原本沒有項目，拿掉標籤條件也會過；改成 10/15 另有兩個沒標籤的項目，並驗時間軸 10/15 是 2 個；突變：選日期時時間軸也套標籤 → 紅 |
| 全形的＃也算標籤 | 邏輯 | ✅ | 2026-10-29 | unit test: HashtagRepositoryImplTest#全形的＃也算標籤；先紅後綠；突變：只認半形 #（改之前）、只認全形 ＃ → 紅；反向：全形寫成 `＃` 跳脫碼 → 維持綠 |
| 離開行程詳情再進來不保留篩選 | UI | ✅ | 2026-10-29 | 實機（SM-S9110）：返回列表再打開，列出全部項目、沒有篩選條 |
| 多選模式時點標籤是選取 | UI | ✅ | 2026-10-29 | 實機（SM-S9110）：點 #night 變成「已選擇 1 項」，篩選不變 |
| 左滑項目卡片先確認再刪除 | UI | ✅ | 2026-10-29 | 實機（SM-S9110）：跳「確認刪除」與情境原句格式，按刪除後項目消失 |
| 刪除確認按取消，卡片回到原位 | UI | ✅ | 2026-10-29 | 實機（SM-S9110）：確認框跳出時卡片已回原位，取消後項目還在 |
| 右滑項目卡片打開編輯 | UI | ✅ | 2026-10-29 | 實機（SM-S9110）：三張卡片都打開對的編輯項目，返回後卡片在原位 |
| 滑動時看得到要做什麼 | UI | ✅ | 2026-10-29 | 實機（SM-S9110）：左滑淡紅底、垃圾桶與「刪除」；右滑黃底（主題的 primaryContainer）、鉛筆與「編輯」；時間軸不跟著動 |
| 滑不夠遠就放手不會觸發 | UI | ✅ | 2026-10-30 | 實機（SM-S9110）：慢慢滑約 100 像素放手都彈回；斜著上下捲列表也不會觸發（2026-10-29）。情境改成「快速甩就算」後，2026-10-30 甩 100 像素跳「確認刪除」，按取消 |
| 滑動中轉螢幕不會再觸發 | UI | ✅ | 2026-10-30 | 實機（SM-S9110）：卡片拖過一半按著不放 → 轉橫向 → 放手，沒打開編輯頁，卡片回原位。「放手後 0.3 秒內轉螢幕」工具做不到，沒走 |
| 左滑行程卡片先確認再刪除 | UI | ✅ | 2026-10-29 | 實機（SM-S9110）：跳「刪除行程」與情境原句；取消回原位，刪除後只剩 1 筆 |
| 右滑行程卡片打開編輯行程 | UI | ✅ | 2026-10-29 | 實機（SM-S9110）：打開那趟的編輯行程 |
| 選單照樣能用 | UI | ✅ | 2026-10-29 | 實機（SM-S9110）：項目卡片「標記完成／編輯／刪除」、行程卡片「編輯／刪除」都在 |
| 多選模式時不能滑 | UI | ✅ | 2026-10-29 | 實機（SM-S9110）：左右滑都沒反應 |

反向突變（符合情境、寫法不同，預期維持綠）：換標籤時改成重新讀一次資料、改成先分組再篩標籤並拿掉空的日期 → 都維持綠。

---

## 待辦 / 跟外部對齊

| 項目 | 屬於誰 | 阻擋上線？ |
|------|-------|----------|
| 項目卡片的資訊順序：先盤點卡片上現有的欄位，再決定順序（改善計畫「UI 改善」#10） | App | 否 |
| 地圖頁與路線頁合成一頁：做真地圖時一起做（待確認 #5 的後半） | App | 否 |
| 行程詳情點標籤或換篩選後，列表停在原本的捲動位置，第一張卡片可能被蓋住一半；可以改成篩選一變就捲回最上面（2026-10-29 第 6 組實機走查看到） | App | 否 |
| 換頁沒有擋重複：連點會推兩個一樣的頁面或多退一頁（`App.kt` 的返回一律 `removeLastOrNull()`、「設定日期」直接 `add`）；改成只在頂層是自己時才退、已在頂層就不推（2026-10-30 第 4 步審查） | App | 否 |
| 足跡、回憶讀取失敗時顯示成空狀態加「去看行程」，只有底部提示一閃；照今日頁加錯誤與重試（2026-10-30 第 4 步審查） | App | 否 |
| 審查列的小問題，改版前就有或很難碰到：範例放到一半失敗會只剩半套、路線合併同名時留第一筆的座標、停在今日頁跨過午夜快速紀錄存到前一天、編輯模式讀不到行程時按儲存會新建一趟、新增行程清空標題後 1 秒內返回舊草稿會回來、「捨棄變更？」對話框轉螢幕後消失、重開快速紀錄面板看到上次的錯誤、標籤的點擊範圍小於 48dp、右滑放手後 0.3 秒內按返回會把編輯頁疊在列表上（2026-10-30 第 4 步審查，`spec-review.md` #18） | App | 否 |
| 快速紀錄存檔中用返回手勢（手勢導覽），面板跟著縮小一點，被擋下後不會彈回原尺寸，要等面板關掉才恢復；只影響外觀，實機看縮多明顯再決定（2026-10-30 第 4 步複查） | App | 否 |
| 新增行程、編輯項目存檔時，若前一個底部提示還在顯示，成功後要等提示消失（約 4 秒）才離開，期間按鈕轉圈；照今日頁另開 coroutine 顯示提示（2026-10-30 第 4 步複查，改版前就要等） | App | 否 |
| 時間軸格寬下限一起量「第 N 天」：字放很大、格子停在 64dp 時，「第 10 天」的「天」會被切掉一點（目前只限一行，避免錯位）（2026-10-30 第 4 步複查） | App | 否 |
| 日期選擇器頂端的日期是系統元件依語系的寫法，跟日期欄的 2026/11/10 不同；系統元件，記為已知（2026-10-30 第 4 步審查） | App | 否 |
| 地圖頁的「第 N 天」是依有地點的天依序編（10/14、10/16 有地點就寫第 1、2 天），跟行程詳情、路線頁依開始日算的不同；合成一頁時一起統一 | App | 否 |

---

## 變更紀錄

- **2026-10-27** ADDED: Requirement「介面用語統一」「「今日」分頁換成代表今天的圖示」「首頁拿掉最近編輯區塊與足跡按鈕」「沒有資料時有「去看行程」按鈕」（第 1 組，含情境）
- **2026-10-27** ADDED: Requirement「沒排時間的項目排在當天最後」「範例行程只在第一次安裝時放一次」「今日頁讀取失敗時可以重試」「快速紀錄存檔成功才關閉」「編輯沒存就離開會提醒」「刪照片一律先確認」「行程一定要有日期」「路線分天，分享可讀的文字」「點標籤篩選同標籤的項目」「卡片可以左右滑」（第 2～6 組，情境動工時補）
- **2026-10-27** 技術設計：時間軸選中那格的行李箱換成飛機（實機檢查後決定，示意圖仍是行李箱）
- **2026-10-28** ADDED: 第 2 組的 9 條情境與技術設計；Requirement「沒排時間的項目排在當天最後」補上回憶故事與「時間相同時先建立的在前」。實作完成，單元測試先紅後綠並做過突變
- **2026-10-28** ADDED: 第 3 組的 19 條情境與技術設計；Requirement「今日頁讀取失敗時可以重試」補上讀取今天的項目失敗也要能重試。第 2 組改標成高風險組（改到存起來的資料）
- **2026-10-28** 第 3 組實作完成，邏輯情境先紅後綠並做過突變，實機走查 17 條沒有失敗。技術設計補一條：今日頁的底部提示另開 coroutine 顯示，不讓關面板等提示消失
- **2026-10-28** ADDED: 第 4 組的 10 條情境與技術設計；Requirement「行程一定要有日期」補上細節：編輯也要有日期、只填一個日期也算沒填、錯誤一次一個先標題
- **2026-10-28** 第 4 組實作完成，邏輯情境先紅後綠並做過突變，實機走查 10 條沒有失敗。提示卡沿用同一頁「當日摘要」的卡片樣式（圓角、細框、內距），App 沒有設計稿可對照
- **2026-10-28** ADDED: 第 5 組的 8 條情境與技術設計；Requirement「路線分天，分享可讀的文字」補上細節：重複地點怎麼列、第 N 天怎麼算、範圍外的項目、分享文字的樣子、資訊卡的地點數。待辦加一列：地圖頁的第 N 天算法跟其他頁不同
- **2026-10-28** 第 5 組實作完成，邏輯情境先紅後綠並做過突變。路線頁的段落標題沿用行程詳情日期分組標題的字級與上方間距，App 沒有設計稿可對照
- **2026-10-28** ADDED: 第 6 組的 19 條情境與技術設計；兩條 Requirement 補上細節：點標籤時看整趟行程、篩選中選日期兩個條件一起套用、篩選留到使用者取消；滑動只做行程卡片與項目卡片、多選模式不能滑
- **2026-10-29** 第 6 組實作完成，邏輯情境先紅後綠並做過突變；技術設計補一句：再點同一個標籤取消時保留選中的日期，跟 ✕ 一樣
- **2026-10-29** ADDED: 情境「全形的＃也算標籤」。實機走查時發現注音鍵盤打出的是全形 ＃，原本的規則只認半形 #，用注音鍵盤寫的標籤一個都不會產生；使用者決定兩種都認
- **2026-10-29** MODIFIED: 情境「篩不到項目時說明原因」（PREVIOUSLY: 當日摘要卡跟著篩選後的列表算，篩不到時寫「0 個地點」並出現「新增這天」 / CHANGE: 摘要卡一律算整天）。實機走查發現，使用者決定
- **2026-10-30** 第 4 步審查與實機走查後的修正（使用者逐條決定，技術設計見「第 4 步審查後的修正」）：
  - ADDED: 情境「紀錄寫不進去也照常啟動」「畫面重建後修改還在」「連按儲存只存一次」「選日期還在讀取時點標籤」「滑動中轉螢幕不會再觸發」
  - MODIFIED: 情境「存檔中不能再按」（PREVIOUSLY: 只寫按鈕停用、再按只新增一筆 / CHANGE: 補一句存檔中面板關不掉）
  - MODIFIED: 情境「篩不到項目時說明原因」（PREVIOUSLY: 用共用資料，10/15 沒有項目 / CHANGE: 這條另加 10/15 兩個沒標籤的項目，並驗時間軸 10/15 照樣算整天；舊資料拿掉標籤條件也篩不出東西，驗不到篩選）
  - MODIFIED: 情境「滑不夠遠就放手不會觸發」（PREVIOUSLY: 滑一點就放手不會觸發 / CHANGE: 慢慢拖不到 56dp 不觸發，快速甩一下就算；實機證實元件看放手速度，使用者決定保留）
  - 時間軸「第 N 天」限一行、路線頁連接線改直線、回憶頁 Preview 假資料拿掉空格；補幾條測試（今日頁排序、路線範圍外與跨天同名、回憶故事其他模板）。待辦加四列

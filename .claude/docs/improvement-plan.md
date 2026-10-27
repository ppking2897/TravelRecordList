# TravelRecordList 改善計畫

> 2026-10-27 從 Claude Docs 搬進專案，之後以這份為準。

2026-10-14

## 現況評估

TravelRecordList 已是完整的 KMP 專案，先修完下表的地基問題再加新功能（#4 地圖例外，排在後續功能第一項）。評估依據為 GitHub master（2026-10-14 同步後版本），4 月版之後已處理的項目列在表後。

**已經很強的地方**

- 12 個 feature 模組、約 200 個 Kotlin 檔，涵蓋行程、項目、照片、Hashtag、路線、足跡、回憶、今日 Dashboard、草稿
- Kotlin 2.2.20、Compose Multiplatform 1.10.3、Navigation 3、AGP 9（`android.kotlin.multiplatform.library`）
- 已串 Google Places API（New）並使用 Field Mask；地點搜尋有 debounce
- `.claude/docs/` 有競品缺口分析與 UI/UX 修復計畫
- 測試採 Kotest property-based test，2026-10-14 起可在 JVM 執行（21 個，Windows 可跑）

**待修問題**

| # | 問題 | 影響 |
|---|---|---|
| 1 | Places key 寫進 APK，且以 REST 呼叫，Android 應用程式限制可被偽造 header 繞過 | 被盜用由你付費 |
| 2 | Android 以 DataStore Preferences、iOS 以 NSUserDefaults 存整包 JSON，不是資料庫（行程與項目已改存 Room，2026-10-15） | 項目存了兩份，造成範例行程無法生成路線等問題（見 #8） |
| 3 | package 為 `com.example.myapplication`，root project 為 `MyApplication` | Play 不接受 `com.example`；像範例專案 |
| 4 | Android 地圖是 `LazyColumn` + 跳轉 Google Maps；maplibre-compose 只寫進版本目錄、沒有模組引用；iOS 只顯示佔位文字 | 旅遊 App 缺真地圖 |
| 5 | 8 個測試檔、21 個測試，已可在 JVM 執行；ViewModel、Repository 幾乎沒測試；沒有 CI；iOS 端 Kotest 規格不會被執行 | 改程式時沒有測試能發現退步 |
| 6 | README 與現況不符：目錄結構、寫支援 Web 但 js / wasm target 未宣告、授權寫 Private | README 是別人認識這個專案的第一站 |
| 7 | iOS 地點搜尋必定失敗：key 寫死空字串 | 宣稱支援 iOS，實際不可用 |
| 8 | 「生成路線」對範例行程失敗，提示至少要兩個不同地點（範例有 8 個）；原因是項目存兩份（2026-10-15 改存 Room 後已修） | 路線功能無法使用 |
| 9 | 今日頁項目的「照片」與詳情頁的「新增照片」曾按下不會開啟相簿（行程封面的挑選器正常）；2026-10-15 早上實機發生一次，下午重測七種情境都未重現，調查紀錄在產品規格書已知問題 #1 | 發生時這兩個入口無法替項目加照片 |

**已處理（2026-10-14）**

- [x] 共用測試可在 JVM 執行：AGP 9 的 KMP 外掛預設不建 Android 端測試，已開啟 `withHostTest`
- [x] 16 KB 分頁：CameraX 拉高至 1.4.2，APK 內原生程式庫全部對齊。Play 規定 targetSdk 35 以上的更新自 2027-02-01 起必須支援（[來源](https://developer.android.com/guide/practices/page-sizes)）
- [x] 移除已無入口的旅遊歷史功能（已由「足跡」分頁取代）
- [x] 專案 CLAUDE.md 改寫為 feature-first 結構
- [x] package 與 App ID 改為 `io.github.ppking2897.travelrecord`，App 顯示名稱改為 TravelRecordList（地基第 1 步）
- [x] MVI 寫法對齊外層規則：BaseViewModel、Contract、ViewModel 註冊（地基第 2 步）
- [x] 行程與項目改存 Room，第一次啟動自動搬移舊資料（地基第 4 步）
- [x] GitHub Actions CI：每次 push 建置 APK 並跑 JVM 測試，結果檔測試數為 0 也判失敗（地基第 5 步）
- [x] 依程式現況寫成產品規格書 [TravelRecordList 產品規格書（現況版）](product-spec.md)
- [x] 有旅行中的行程時，打開 App 直接停在「今日」；旋轉螢幕不再跳回「行程」分頁（規格書待確認 #1 前半）
- [x] 行程詳情按「刪除行程」會先跳出確認視窗，跟行程列表一致（規格書已知問題 #2）
- [x] 足跡只算去過的地點：已打卡，或日期在今天以前（規格書待確認 #2）
- [x] 點足跡、回憶的卡片會打開那趟行程的詳情（規格書待確認 #3）
- [x] 行程列表：摘要卡「即將出發」算全部規劃中的行程；搜尋或篩選沒有結果時顯示「沒有符合的行程」；刪除失敗會在底部提示（規格書已知問題 #6～#8）
- [x] 行程詳情標記完成後，時間軸的完成數立即更新（規格書已知問題 #9）
- [x] 移除行程詳情畫面沒有接上的「全選」與「依標籤篩選」，之後有規劃再做回去
- [x] 刪除項目或行程時一併刪掉照片、縮圖與封面檔；編輯行程換封面要儲存成功才刪舊封面檔（規格書已知問題 #5、#15）
- [x] 新增行程停止輸入 1 秒後自動存草稿；新增或編輯項目時照片存檔失敗會提示「照片加入失敗」（規格書已知問題 #10、#16）
- [x] 新增行程按「捨棄」時一併清掉草稿，下次開啟不再帶回剛捨棄的內容
- [x] 生成路線、離開早於到達的錯誤訊息改為中文；編輯項目儲存時也檢查離開不早於到達（規格書已知問題 #12）
- [x] 旋轉螢幕時停在行程分頁原本的頁面，不再退回行程列表（規格書已知問題 #17）
- [x] 深色模式打開 App 時不再先閃一下白畫面
- [x] 項目卡片的 #標籤、今日頁的地點改成只顯示的小標籤，不再做成按鈕樣子（規格書已知問題 #11）
- [x] 新增行程的「✓ 已自動儲存草稿」提示移到標題下方，出現時不再把儲存按鈕擠出畫面
- [x] 路線不再每次生成就另存一份，打開路線頁時依行程現算，舊版存下的路線在啟動時清掉；拿掉路線頁永遠不會出現的「建議停留」「建議總時間」（規格書已知問題 #13、#14）

## API key 流向

**Places API key 流向 · 從 local.properties 到 UI**

```
local.properties（GOOGLE_PLACES_API_KEY）
└→ BuildKonfig（build.gradle.kts 第 20–29 行）
   └→ ApiKeyProvider.android：讀 BuildKonfig，打包進 APK   ← 風險點
      └→ GooglePlacesService
         ├ POST /v1/places:searchText
         ├ GET /v1/places/{placeId}
         ├ Header：X-Goog-Api-Key
         └→ LocationSearchField（輸入時建議地點，有 debounce）
            ├→ ItemFormContent（新增／編輯項目）
            └→ TodayScreen（快速記錄）

ApiKeyProvider.ios：回傳空字串（TODO），搜尋失敗
NominatimLocationService（虛線框）：同介面、免 key，但未綁定 DI
```

key 只被 `GooglePlacesService` 讀取，用於兩支 Places API（New）REST 請求；圖中標「風險點」的那一步：Android 把 key 打包進 APK。Web target 未在 `build.gradle.kts` 宣告，目前不會編譯，所以沒有網頁版外洩問題。

## key 處理方案

建議先做 A 止血，再做 B 並改用 Autocomplete。三個方案都在 `LocationSearchService` 介面背後替換實作，UI 不用改。

| 方案 | 做法 | 效果 | 代價 |
|---|---|---|---|
| **A 最小修正** | key 設 API 限制（只允許 Places API）、每日配額上限；請求加 `X-Android-Package`、`X-Android-Cert` 並設 Android 應用程式限制 | 擋掉隨手盜用；費用封頂 | 擋不住偽造 header 的人 |
| **B Places SDK for Android** | `GooglePlacesService` 換成 androidMain 的 Places SDK 實作，並接 Firebase App Check | App Check 擋掉非正版 App 的請求（REST 端點不支援 App Check）；不需後端 | iOS 日後要另接 Places SDK for iOS |
| **C 後端代理** | Cloudflare Worker 存 key，App 只呼叫代理 | key 完全不在 App；一份實作通用三平台 | 多維護一個服務與帳號 |

Google 對行動 App 呼叫 web service 「強烈建議」經代理（方案 C）；A 是無法架代理時的官方退路。A、B 都靠套件名稱加簽章 SHA-1 辨識，B 多出來的保護是 App Check（[來源](https://developers.google.com/maps/api-security-best-practices)）。

**費用優化：改用 Autocomplete**

目前每次 debounce 結束都打一次 Text Search。「邊打字邊建議」的情境，Google 提供 Autocomplete 搭配 session token，把一次搜尋過程的多次請求綁成同一個 session 計價，是更正確的用法。實際價差以官方定價頁為準。

**待辦**

- [ ] A：Google Cloud Console 設定 API 限制與配額上限（官方文件只列 Places API (New) 的每分鐘配額，有沒有每日項目以 Console 為準）
- [ ] A：`GooglePlacesService` 請求加上 Android 識別 header，並確認帶錯誤識別碼的請求會被拒絕（header 已加；拒絕驗證等 Console 設好限制）
- [ ] B：加入 Places SDK for Android 與 Firebase App Check，實作 androidMain 版 `LocationSearchService`
- [ ] B：搜尋改用 Autocomplete + session token，選定後再查 Place Details
- [ ] 移除未使用的 `NominatimLocationService`，或明確設為無 key 時的後備
- [ ] 為 `LocationSearchService` 的實作補上測試（假資料、錯誤處理、空 key）

## 地基修正任務清單

依序執行。package 更名放最前面，因為它會動到幾乎每個檔案，越晚做衝突越多。第 2 項 MVI 寫法遷移同樣會動到全部畫面，緊接在後。

### 1. package 與專案名稱

- [x] `com.example.myapplication` 改為自己的網域反寫（例如 `io.github.ppking2897.travelrecord`）
- [x] root project `MyApplication` 改名；BuildKonfig 的 `packageName`、`namespace` 一併更新
- [x] iOS bundle identifier 同步修改（未在 iOS 驗證；產品名稱需在 Mac 上改 Xcode 專案，已標 TODO(等外部)）

### 2. MVI 寫法對齊外層規則

外層 rules 比本專案寫法新（rules 4–7 月更新，本專案 4/9 定型）。BaseViewModel 是共用基底，新舊寫法不能並存，整批遷移。

- [x] BaseViewModel 改為 `BaseViewModel<Intent, State, Event>`，Screen 改呼 `processIntent`（10 個 ViewModel、10 個 Screen）
- [x] Contract 拿掉 `UiState` / `UiIntent` / `UiEvent` 介面（10 個檔，與上一項一起）
- [x] DI 改用 `viewModelOf(::XxxViewModel)`（10 筆註冊）
- [x] 驗證：測試全綠、實機四個分頁與詳情頁快速檢查；完成後更新專案 CLAUDE.md 的「待遷移」表（移除已遷移的三列，補上尚未對齊的 Screen 拆法）

Repository 回傳型別不在此項：規則的 `Flow<ApiResult<T>>` 是為打後端 API 設計，本專案沒有後端，換 Room 時再決定。

### 3. API key（見上一節）

- [ ] 完成方案 A

### 4. 資料庫：DataStore JSON → Room KMP

- [x] 設計 Entity：行程與項目兩張表；照片與標籤以 JSON 欄位存在項目列，路線與草稿留在鍵值儲存（有跨項目查詢需求時再拆表）
- [x] Android 與 iOS 實作 Room；`StorageService` 介面改為依實體的 DAO（Android 已實機驗證；iOS 已寫但未在 Mac 驗證，標 TODO(等外部)）
- [x] 一次性遷移：啟動時讀取舊 DataStore 資料寫入 Room，舊資料先保留一個版本（只搬單獨存的項目，不救回已刪除的內嵌副本）
- [x] 測試：Repository 合約測試同時跑在舊 JSON 與 Room 實作上、搬移測試 13 個，共 83 個測試；手機升級實測通過

### 5. 測試與 CI

- [x] 主要 ViewModel 測試（Intent → State、Event）：今日頁、行程列表、行程詳情都完成（2026-10-16；今日頁 24 個、行程列表 24 個、行程詳情 30 個，對應功能文件的「邏輯」Scenario，關鍵斷言都做過突變）
- [x] Repository 測試（使用假的 StorageService 或 in-memory Room）：第 4 步已補上合約測試，Room 版在 JVM 上用記憶體資料庫執行
- [x] GitHub Actions：每次 push（任何分支）都建置 APK、跑 JVM 測試（`testAndroidHostTest`），並檢查結果檔的測試數大於 0。2026-10-15 第一次執行綠燈、測試數 83、約 4 分半；在臨時分支故意讓一條測試失敗，CI 會變紅。「PR 必須通過」不設：目前直接在 master 開發。
- [ ] 變異測試：暫不做（2026-10-16 決定）。評估過兩種工具：PIT 免費版分不出 Kotlin 編譯器產生的程式碼，雜訊多，Kotlin 支援在付費版 Arcmutate；krispr 是 9 月底才出現的 0.1 版原型，只有一位作者。兩者試接都沒跑出結果。目前改用手動檢查：每片測試挑關鍵判定故意改壞，確認測試會失敗。README 不放分數。

### 6. README 與文件

- [ ] 目錄結構改為實際的 feature-first 結構
- [ ] 平台支援寫實際狀態（含 iOS 地點搜尋目前不可用）：Web target 目前未宣告，決定恢復或移除 `jsMain` / `wasmJsMain` / `webMain`
- [ ] 加上架構圖、截圖或 demo GIF、測試分數、API key 設定說明與安全取捨
- [ ] 授權改為公開授權（例如 MIT、Apache 2.0）或明確說明
- [ ] 更新 `.claude/docs/product-analysis.md` 中「使用 InMemoryStorage」的過時描述（實際是 Android 用 DataStore、iOS 用 NSUserDefaults）

## 規格調整

[產品規格書](product-spec.md)「待確認事項」決定後要做的改動。

**第 1 批（2026-10-27 決定）**

- [ ] 建立行程時開始與結束日期必填；既有沒日期的行程，在詳情頁提示補日期（待確認 #4）
- [ ] 路線分享改成「第 1 天：伏見稻荷 → 金閣寺」這種可讀文字，路線改成分天；地圖與路線合併留到做真地圖時（待確認 #5）
- [ ] 用語統一：「規劃中」、「第 N 天」、完整日期寫 2026/10/20，故事文字去掉多餘空格（待確認 #16）
- [ ] 首頁拿掉「最近編輯」區塊與右上角的足跡按鈕（待確認 #12、#13）
- [ ] 沒排時間的項目，各畫面一律排在當天最後（待確認 #6）

**第 2 批（2026-10-27 決定）**

- [ ] 範例行程只在第一次安裝時放一次；從舊版升級、本來就有行程的人視為已經放過（待確認 #7）
- [ ] 今日頁讀取行程失敗時顯示錯誤訊息與「重試」（待確認 #8）
- [ ] 快速紀錄存檔成功才關面板；存檔中按鈕停用；失敗時留在面板顯示錯誤（待確認 #9）
- [ ] 編輯行程、編輯項目有修改時離開會跳「捨棄變更？」（待確認 #10）
- [ ] 刪照片一律先確認，包含縮圖選單（待確認 #11）

**第 3 批（2026-10-27 決定）**

設計基準以手帳風示意圖為準，不另建規格；示意圖沒畫的部分照以下決定做。

- [ ] 「今日」分頁的圖示從「＋」換成日曆上標一個點的「今天」圖示（待確認 #1 後半）
- [ ] 足跡、回憶的空狀態加「去看行程」按鈕，切到行程分頁（待確認 #15、UI 改善 #7）
- [ ] 點項目上的標籤，篩出這趟行程裡同標籤的項目，再點一次取消（待確認 #14）
- [ ] 行程卡片與項目卡片：左滑刪除（先確認）、右滑編輯，原本的選單照樣能用（UI 改善 #8）

## UI 改善

沒有 Figma 時，先做下表 6 項不需要設計決定的；標「需要」的等設計基準定下來再動。下表用網路上的設計原則對照 2026-10-14 的現況，只列有依據的問題，不自訂視覺規格。

| # | 項目 | 依據 | 現況（2026-10-14） | 設計決定 |
|---|---|---|---|---|
| 1 | 深色模式判斷收進 Theme 層 | `uiux-fix-plan.md` 3.2 | Theme 以外有 10 處直接呼叫 `isSystemInDarkTheme()`（2026-10-27：App 改成只保留淺色，已沒有任何呼叫） | 不需要 |
| 2 | 硬寫 dp 換成既有的 `Spacing` token | `ui-review.md` H-3 | 109 行、21 個檔 | 不需要 |
| 3 | 補齊 `contentDescription` | `uiux-fix-plan.md` 3.7 | 59 處為 null（4 月盤點是 48 處） | 不需要 |
| 4 | 骨架載入擴到其他畫面 | `uiux-fix-plan.md` 3.4 | 只有行程列表用 `ShimmerPlaceholder` | 不需要 |
| 5 | 行程標題改為單行輸入（待你確認） | 實機：標題「test」的卡片多出一行 | 標題欄可輸入換行 | 不需要 |
| 6 | 清掉沒人用的共用空狀態元件 | 程式碼盤點 | `EmptyState`、`EmptyStateEnhanced` 皆 0 處使用 | 不需要 |
| 7 | 足跡、回憶的空狀態加上下一步按鈕（2026-10-27 定為「去看行程」，切到行程分頁） | [NN/g 空狀態三原則](https://www.nngroup.com/articles/empty-state-interface-design/)：說明狀態、教怎麼開始、給直接路徑 | 行程、今日、詳情有按鈕；足跡、回憶只有文字 | 需要 |
| 8 | 卡片左右滑手勢，並保留按鈕替代（2026-10-27 定為左滑刪除、右滑編輯） | [WCAG 2.5.1](https://www.w3.org/WAI/WCAG22/Understanding/pointer-gestures.html)（A 級）：路徑手勢要有單指點擊的替代 | 尚未做（`uiux-fix-plan.md` 3.3） | 需要 |
| 9 | 地圖與列表同步選取 | [旅遊 App 設計範例](https://screensdesign.com/articles/travel-itinerary-app-design-examples/)（二手）：列表講順序、地圖講距離，切換時保留選中的日期與項目 | 地圖目前是列表版，做真地圖時一起設計 | 需要 |
| 10 | 行程項目卡片的資訊順序 | 同上（二手）：日期、當地時間、地點、停留時間、預約狀態、下一步，順序固定 | 尚未盤點現有卡片欄位 | 需要 |
| 11 | 時間軸「Day N」與中文介面混用（2026-10-27 定為「第 N 天」） | 實機 | `TimelineNavigator` 寫死 "Day N" | 需要 |
| 12 | 自訂字級與圓角 | `ui-review.md` 第五節 | Theme 只定了色彩，Typography、Shapes 用 Material 預設（2026-10-27：字級已依手帳風示意圖自訂；圓角在各元件上依示意圖調整，Theme 的 Shapes 仍是預設） | 需要 |
| 13 | Material 3 Expressive（新動態、形狀、元件） | [Compose Material3 版本紀錄](https://developer.android.com/jetpack/androidx/releases/compose-material3)：1.5.0 正把 Expressive API 移出實驗階段（1.5.0-beta01，2026-10-07） | 專案用 Compose Multiplatform 1.10.3；等它的穩定版跟上再評估 | 先不採用 |

Android 16 強制的 edge-to-edge 與預測返回已具備：`MainActivity` 呼叫 `enableEdgeToEdge()`、表單用 `NavigationBackHandler`、全專案沒有 `onBackPressed`，實機畫面未見被系統列遮擋（[來源](https://developer.android.com/about/versions/16/behavior-changes-16)）。`uiux-fix-plan.md` 的 3.5 旅遊歷史統計已由足跡分頁的統計卡取代，不用做。

- [x] 要不要先建立設計基準（Figma，或至少色彩、字級、圓角規格）？建立前，表中「需要」的項目先不動。→ 2026-10-27 決定：以手帳風示意圖為設計基準，不另建規格；示意圖沒畫的部分依「規格調整」第 3 批的決定做。

## 後續功能

地基完成後，先做真地圖，再從下表挑一到兩個差異化功能，然後上架 Play。下表對應 `product-analysis.md` 的缺口與本次討論過的點子。

**真地圖**

- [ ] 加入 maplibre-compose 依賴（目前只寫在版本目錄），取代 Android 的列表版地圖與 iOS 的佔位文字
- [ ] 依日期上色的標記與路線連線（配色沿用既有的 DateColorPalette）
- [ ] 地圖頁與路線頁合成一個按天看的地圖頁，加「用 Google 地圖導航這天」（規格書待確認 #5）

**差異化功能候選**

| 功能 | 來源 | 主要技術 |
|---|---|---|
| 費用追蹤與旅伴分帳 | 缺口分析 P0；離線分帳點子 | Room、最少轉帳演算法、Nearby Connections |
| 行程提醒通知 | 缺口分析 P0 | AlarmManager、expect / actual |
| 旅行當天 Live Updates | Android 16 新 API | `Notification.ProgressStyle` |
| 今日行程小工具 | 缺口分析（平台功能） | Glance |
| 景點附近還能去哪 | 缺口分析（地點發現）；今天吃什麼點子 | Places Nearby Search |
| 請 AI 助理新增行程項目 | AppFunctions 點子 | AppFunctions（實驗性預覽） |
| 打包與行前清單 | 缺口分析 P0 | Room、模板 |

## 參考資料

- [TravelRecordList（GitHub）](https://github.com/ppking2897/travelrecordlist)
- [Places SDK for Android：用量與計費](https://developers.google.com/maps/documentation/places/android-sdk/usage-and-billing)
- [Places SDK for Android：概覽](https://developers.google.com/maps/documentation/places/android-sdk/overview)
- [Places API（New）Text Search](https://developers.google.com/maps/documentation/places/web-service/text-search)
- [Places API（New）Autocomplete](https://developers.google.com/maps/documentation/places/web-service/place-autocomplete)
- [Google Maps Platform API 安全性最佳做法](https://developers.google.com/maps/api-security-best-practices)
- [Room for Kotlin Multiplatform](https://developer.android.com/kotlin/multiplatform/room)
- [maplibre-compose（GitHub）](https://github.com/maplibre/maplibre-compose)
- [Android：16 KB 分頁大小支援](https://developer.android.com/guide/practices/page-sizes)
- [Android 16 行為變更](https://developer.android.com/about/versions/16/behavior-changes-16)
- [Compose Material3 版本紀錄](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [NN/g：Designing Empty States in Complex Applications](https://www.nngroup.com/articles/empty-state-interface-design/)
- [WCAG 2.2：2.5.1 Pointer Gestures](https://www.w3.org/WAI/WCAG22/Understanding/pointer-gestures.html)
- [10 Travel Itinerary App Design Examples（二手整理）](https://screensdesign.com/articles/travel-itinerary-app-design-examples/)

以上連結 2026-10-14 實測皆可開啟。

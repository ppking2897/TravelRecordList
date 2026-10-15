# TravelRecordList — 專案指南

> 只記 **TravelRecordList specific** 的事。通用工作鐵則見全域 `~/.claude/CLAUDE.md`；Android 共用慣例見外層 `Android Project/CLAUDE.md`。
> 結構已對齊外層標準（feature-first + Navigation 3）；以下只記外層沒講、或跟外層 rules 寫法不同的地方。

## 模組

- `composeApp`：共用程式碼（KMP library，Android + iOS target）
- `androidApp`：Android 殼（application module）
- `iosApp`：iOS 殼
- package `io.github.ppking2897.travelrecord`；Android `applicationId`、iOS bundle ID 同名

## 建置指令

- 建 APK：`./gradlew :androidApp:assembleDebug`
- 裝到手機：`./gradlew :androidApp:installDebug`
- 跑測試（JVM，Windows 可用）：`./gradlew :composeApp:testAndroidHostTest`；`allTests` 會另外嘗試 iOS 測試，非 Mac 上自動略過
- 看結果：`composeApp/build/test-results/testAndroidHostTest/*.xml`，以 `tests="N"` 的 N 為準，`BUILD SUCCESSFUL` 不代表有跑到測試

> 指令以本檔為準；`README.md` 的 `:composeApp:assembleDebug`、wasmJs 指令是拆出 `androidApp` 前的舊寫法。

## 與外層 kmp rules 不同的寫法

外層 `.claude/rules/kmp-*` 比本專案的寫法新（rules 4–7 月更新，本專案寫法 4/9 定型）。原則：**「怎麼寫」以 rules 為準，「專案裡有什麼」以專案為準。**

### 待遷移（rules 較新；新畫面照 rules 寫，既有畫面不在寫新功能時順手改）

| 面向 | 外層 rules（目標） | 本專案現況 |
|---|---|---|
| Screen 拆法 | 拆出無 ViewModel 依賴的 `XxxContentView`，只在 Screen 層放一個整頁 Preview（檔案最底）；callback 不給預設值 | 10 個 Screen 都沒有拆 ContentView；`ItineraryDetailScreen` 有 1 個 callback 帶 `= {}` |

> 既有 ViewModel 的 `processIntent` 把整段 `when` 包在 `viewModelScope.launch` 裡，是為了保留遷移前「每個 Intent 各跑一個 coroutine」的行為；新寫的 ViewModel 照 rules，由各處理函式自己 launch。

### 本專案不適用（rules 的前提跟本專案不同）

| 面向 | 外層 rules | 本專案 |
|---|---|---|
| 資料來源 | Ktor 打後端 API，`data/model/*Response` 是 API 回應 | **沒有後端**：行程與項目存 Room（見下方「本機資料」）；路線與草稿仍透過 `StorageService`（`core/data/local/`）存 JSON，`data/model/*Response` 是儲存格式。對外 HTTP 只有地點搜尋（`core/data/remote/`）|
| Repository 回傳 | `Flow<ApiResult<T>>`（為打 API 設計） | `suspend fun …: Result<T>`（kotlin `Result`；專案內沒有 `ApiResult`）。換 Room 時維持不變：沒有畫面需要監聽資料變化，不改成 Flow |

## 本機資料

- **Room**（`core/data/local/db/AppDatabase`）：`itineraries`、`itinerary_items` 兩張表，表與 DAO 放在各 feature 的 `data/local/`。每個項目只存一份；讀行程時 `Itinerary.items` 從項目表帶出，寫入行程時忽略 `items`，刪除行程由外鍵連帶刪除項目。照片與標籤以 JSON 字串存在項目列中。
- **改資料表**：升 `@Database(version)` 並提供 Migration，不用 destructive migration。schema 匯出在 `composeApp/schemas/`，跟程式一起 commit。
- **版本固定**：Room 2.8.5、sqlite-bundled 2.6.2、KSP 2.3.12 是配合 Kotlin 2.2.20 的組合（原因寫在 `gradle/libs.versions.toml`），升 Kotlin 時一起檢查。
- **舊版資料**：第一次啟動新版時由 `LegacyJsonImporter` 從鍵值儲存搬進 Room（`EnsureLocalDataReadyUseCase` 在行程列表啟動時呼叫），舊資料保留不刪。舊版的 JSON Repository 移到 `commonTest` 的 `core/data/local/legacy/`，只用來產生舊格式測試資料。
- **測試**：Repository 的行為規格在 `commonTest`（`*RepositoryContract`、`DataLayerAcceptanceContract`），Room 版的綁定在 `androidHostTest`，用記憶體資料庫在 JVM 上跑。JVM 測試會從 `sqlite-bundled-jvm` 取出對應作業系統的原生程式庫，並開啟 `isReturnDefaultValues`（Android API 在測試中回傳預設值而不是丟錯）。

## Feature 之間的關係

- 有 `data/` + `domain/` 的是**資料 feature**（`itinerary`、`itinerary_item`、`route`、`draft`、`memory`），擁有 domain model 與資料存取（Repository；`memory` 是 `domain/service`）。
- 其他是**畫面 feature**，直接 import 資料 feature 的 `domain/model`、`domain/repository`（例：`feature.itinerary_item.domain.model.ItineraryItem`），不自己複製一份 model。

## 新增畫面要接的地方

1. 路由：`core/presentation/navigation/Screen.kt` 的 `AppRoute`（檔名是 `Screen.kt`，內容是 `sealed interface AppRoute : NavKey`）
2. 畫面入口：`App.kt` → `PlanNavHost` 的 `entryProvider` 加 `is AppRoute.Xxx -> NavEntry(key) { … }`
   - `when` 有 `else` 兜底：漏加**不會編譯失敗**，只會顯示 "Unknown route"
3. DI：`feature/{name}/di/{Name}Module.kt`，再加進 `di/AppModule.kt` 的 `appModule` 加總

底部四個分頁在 `App.kt` 的 `AppTab`。只有「行程」分頁有 back stack（`PlanNavHost`），其他分頁是單一畫面；要從別的分頁進深層頁，先切回行程分頁再 push（參考 `TodayScreen` 的 `onOpenCurrentTrip`）。

## 環境

- 地點搜尋需要在 `local.properties` 設 `GOOGLE_PLACES_API_KEY`（BuildKonfig 注入，經 `config/ApiKeyProvider`）。沒設時搜尋回 failure，不會 crash。iOS 端的 key 讀取尚未實作。

## 規劃文件

- **Phase 進度與 UX 待辦**：`.claude/docs/uiux-fix-plan.md`（「執行狀態」段）——挑下一個任務前先讀。
- 其他 `.claude/docs/*.md`（product-analysis、ui-review、ux-improvements）是一次性分析報告，不是 feature-doc 格式。

## 等待標記

等外部而先不做的地方（帳號設定、需要 Mac 才能驗證等），標成 `TODO(等外部): 剩什麼；移除條件：…`，移除條件要能直接驗證。`/blocked` 只搜這個寫法；一般 `TODO` 是自己還沒做的事，不算等外部。

> `uiux-fix-plan.md` 的 ⏸ 意思是「待做」不是「被擋」，`/blocked` 會把它列成卡點，看結果時要扣掉。

## Skill 配置

- **Code root**: `composeApp/src/commonMain/kotlin/io/github/ppking2897/travelrecord`
- **Code repo**: `TravelRecordList`
- **Doc dir**: `.claude/docs/`
- **Integration branch**: `master`

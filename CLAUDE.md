# TravelRecordList — 專案指南

> 只記 **TravelRecordList specific** 的事。通用工作鐵則見全域 `~/.claude/CLAUDE.md`；Android 共用慣例見外層 `Android Project/CLAUDE.md`。
> 結構已對齊外層標準（feature-first + Navigation 3）；以下只記外層沒講、或跟外層 rules 寫法不同的地方。

## 模組

- `composeApp`：共用程式碼（KMP library，Android + iOS target）
- `androidApp`：Android 殼（application module）
- `iosApp`：iOS 殼
- package `com.example.myapplication`（模板預設，未改）

## 建置指令

- 建 APK：`./gradlew :androidApp:assembleDebug`
- 裝到手機：`./gradlew :androidApp:installDebug`
- 跑測試（JVM，Windows 可用）：`./gradlew :composeApp:testAndroidHostTest`；`allTests` 會另外嘗試 iOS 測試，非 Mac 上自動略過
- 看結果：`composeApp/build/test-results/testAndroidHostTest/*.xml`，以 `tests="N"` 的 N 為準，`BUILD SUCCESSFUL` 不代表有跑到測試

> 指令以本檔為準；`README.md` 的 `:composeApp:assembleDebug`、wasmJs 指令是拆出 `androidApp` 前的舊寫法。

## 與外層 kmp rules 不同的寫法

外層 `.claude/rules/kmp-*` 比本專案的寫法新（rules 4–7 月更新，本專案寫法 4/9 定型）。原則：**「怎麼寫」以 rules 為準，「專案裡有什麼」以專案為準。**

### 待遷移（rules 較新；遷移完成前照現有 code 寫）

BaseViewModel 是全專案共用的基底，新舊寫法不能並存，所以要整批遷移，不在寫新功能時順手改。遷移排在改善計畫的地基項目。

| 面向 | 外層 rules（目標） | 本專案現況 |
|---|---|---|
| BaseViewModel | `BaseViewModel<Intent, State, Event>`，Screen 呼 `processIntent` | `BaseViewModel<S : UiState, I : UiIntent, E : UiEvent>`（`core/presentation/mvi/`）；Screen 呼 `handleIntent(intent)`，子類實作 `suspend processIntent`；`sendEvent` 是 suspend |
| Contract | Intent / State / Event 不掛介面 | 各自實作 `UiIntent` / `UiState` / `UiEvent`（跟 BaseViewModel 一起遷移） |
| ViewModel 註冊 | `viewModelOf(::XxxViewModel)` | `viewModel { XxxViewModel(get(), …) }` |

### 本專案不適用（rules 的前提跟本專案不同）

| 面向 | 外層 rules | 本專案 |
|---|---|---|
| 資料來源 | Ktor 打後端 API，`data/model/*Response` 是 API 回應 | **沒有後端**：Repository 透過 `StorageService`（`core/data/local/`）存本機 JSON，`data/model/*Response` 是儲存格式。對外 HTTP 只有地點搜尋（`core/data/remote/`）|
| Repository 回傳 | `Flow<ApiResult<T>>`（為打 API 設計） | 讀寫資料的方法用 `suspend fun …: Result<T>`（kotlin `Result`；專案內沒有 `ApiResult`）。換 Room 時再決定要不要改 |

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

- **Code root**: `composeApp/src/commonMain/kotlin/com/example/myapplication`
- **Code repo**: `TravelRecordList`
- **Doc dir**: `.claude/docs/`
- **Integration branch**: `master`

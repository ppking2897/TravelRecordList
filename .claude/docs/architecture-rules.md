# 架構規則

哪一層、哪個套件可以依賴誰。每條規則都有一支測試，違規時 `./gradlew :composeApp:testAndroidHostTest` 會紅，失敗訊息會指到檔案與行號。

- 測試：`composeApp/src/androidHostTest/kotlin/io/github/ppking2897/travelrecord/architecture/ArchitectureRulesTest.kt`
- 這些規則是把 2026-10-30 的現況寫下來，不是重新設計。現有違規列在測試裡的例外清單，清單只能變短：例外已經不違規時測試也會紅，要求把它刪掉。
- 2026-11-02 確認：8 條規則、各條管不管測試、兩份例外清單照現在的寫法。每條規則都做過「放一個違規的暫時檔 → 那條測試紅、訊息指到暫時檔 → 刪掉變回綠」，例外清單刪一項、加一項不違規的都會紅。

## 怎麼認層

整個專案是單一模組 `composeApp`，任何套件都 import 得到彼此，編譯擋不住分層，所以靠這些測試。

- 層看 **package 宣告**，不看資料夾。
- `feature.<名稱>.<data|domain|presentation|di>`：feature 的一層。
- `core.<data|domain|presentation|di>`：共用的一層。
- 頂層的 `data`／`domain`／`presentation` 套件（舊寫法，例如 iosMain 的 `data.storage`）照段名算那一層。
- 根套件（`App.kt` 所在）、`di`（`AppModule` 加總）、`config`、`util`、`test`：不屬於任何層。
- 依賴包含 `import`，以及程式裡直接寫完整名稱的引用。
- 掃描範圍：`composeApp/src/*/kotlin` 底下全部原始碼集合，主程式（`commonMain`、`androidMain`、`iosMain`、`jsMain`、`wasmJsMain`、`webMain`）與測試（`commonTest`、`androidHostTest`）。每條規則各自寫明管不管測試。

## 規則

<a id="arch001"></a>
### ARCH001 domain 不依賴 data、presentation、di

- 為什麼：domain 是規則與資料模型本身，換資料來源（JSON → Room）或換畫面都不該動到它。
- 範圍：主程式。`feature.*.domain` 與 `core.domain`。
- 測試：`ARCH001 domain 不依賴 data presentation di`

<a id="arch002"></a>
### ARCH002 presentation 不依賴 data

- 為什麼：畫面只透過 domain 的 use case 與 repository 介面拿資料；接哪個實作由 DI 決定。
- 範圍：主程式與測試。presentation 底下的測試要用的資料層，一律在 `test` 套件組好（`InMemoryRepositories`、`TestUseCases`）。
- 測試：`ARCH002 presentation 不依賴 data`

<a id="arch003"></a>
### ARCH003 data 不依賴 presentation、di

- 為什麼：資料層不知道畫面存在，也不自己組裝依賴。
- 範圍：主程式。
- 測試：`ARCH003 data 不依賴 presentation di`

<a id="arch004"></a>
### ARCH004 domain 不用框架

- 規則：domain 在專案外只能用 Kotlin 標準庫（`kotlin.*`）、`kotlinx.datetime`、`kotlinx.coroutines`；在專案內只能用 domain 層（其他層由 ARCH001 管，根套件、`util`、`config`、`test` 由這條管）。
- 為什麼：Compose、Room、Ktor、Koin 都是 commonMain 的依賴，domain 寫得到但編譯不會擋。
- 範圍：主程式。
- 測試：`ARCH004 domain 只用 Kotlin 標準庫 datetime coroutines`

<a id="arch005"></a>
### ARCH005 畫面 feature 之間不互相依賴

- 規則：`feature.X.presentation` 不依賴 `feature.Y.presentation`。要共用的畫面元件放 `core.presentation`，要共用的資料走資料 feature 的 domain。
- 範圍：主程式。
- 測試：`ARCH005 feature 的 presentation 不依賴別的 feature 的 presentation`

<a id="arch006"></a>
### ARCH006 共用程式碼不用 android、java

- 規則：`commonMain`、`commonTest` 不依賴 `android.*`、`java.*`、`javax.*`。平台程式放 `androidMain`（`expect`／`actual`）。
- 為什麼：Android 目標的編譯看得到這些類別，本機 `testAndroidHostTest`、`assembleDebug` 與 CI 都會過；只有 metadata／iOS 編譯會擋，而平常不跑那些。
- 範圍：主程式與測試（`commonTest`）。
- 測試：`ARCH006 common 原始碼不依賴 android java`

<a id="arch007"></a>
### ARCH007 core 不依賴 feature 的 data、presentation、di

- 規則：`core.*` 不依賴 `feature.*.data`、`feature.*.presentation`、`feature.*.di`。
- 放行：feature 的 **domain**。專案的 domain model 放在資料 feature 裡（見 CLAUDE.md「Feature 之間的關係」），core 的共用元件（`ItemCard`、`PhotoGallery` 等）照這個慣例直接 import。
- 範圍排除：`core.data.local.db`（`AppDatabase`）對 `feature.*.data.local` 的依賴。Room 的 Database 要登錄每個 feature 的表與 DAO，結構上一定要依賴。
- 範圍：主程式。
- 例外清單（只能變短）：`JsonSerializer`（草稿、行程、項目的 JSON 格式）、`LegacyJsonImporter`（舊版鍵值資料搬進 Room），共 7 處。
- 測試：`ARCH007 core 不依賴 feature 的 data presentation di`

<a id="arch008"></a>
### ARCH008 feature 不依賴根套件

- 規則：`feature.*` 不依賴根套件 `io.github.ppking2897.travelrecord`（`App.kt` 等 App 殼）。
- 為什麼：App 殼組裝 feature，反過來依賴會形成循環。
- 範圍：主程式。
- 例外清單（只能變短）：9 個 Screen import `App.kt` 的 `LocalSnackbarHostState`。修法是把這個 CompositionLocal 搬到 `core.presentation`，屬於重構，另外做。
- 測試：`ARCH008 feature 不依賴根套件`

## 沒寫成測試的

- 資料 feature 的 data 層互相依賴（`itinerary` ⇄ `itinerary_item`）：兩者是同一組資料表，項目的 Entity 有外鍵指向行程、行程的 JSON 格式內嵌項目，結構上需要，不設規則。
- 資料 feature 的 domain 互相依賴（例：`memory` → `itinerary`）、畫面 feature 依賴資料 feature 的 domain：這是專案的設計（CLAUDE.md「Feature 之間的關係」），不設規則。

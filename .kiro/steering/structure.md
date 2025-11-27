# 專案結構 (Project Structure)

## 根目錄配置 (Root Layout)

```
/composeApp          - 共享的 Kotlin Multiplatform 程式碼
/iosApp              - iOS 特定的 Swift 程式碼和 Xcode 專案
/gradle              - Gradle wrapper 和版本目錄
build.gradle.kts     - 根建置配置
settings.gradle.kts  - 專案設定
```

## ComposeApp 結構

所有共享程式碼位於 `composeApp/src/`，包含平台特定的 source sets：

### Source Sets

- **commonMain** - 所有平台共享的程式碼
- **androidMain** - Android 特定實作
- **iosMain** - iOS 特定實作
- **jsMain** - JavaScript 特定實作
- **wasmJsMain** - WebAssembly 特定實作
- **webMain** - 共享的 web 資源 (HTML, CSS)
- **commonTest** - 共享的測試程式碼

### 套件組織 (Package Organization) - commonMain

```
com.example.myapplication/
├── data/
│   ├── model/           - Data classes (Itinerary, ItineraryItem, Location, Route 等)
│   ├── repository/      - Repository interfaces 和 implementations
│   ├── storage/         - Storage service interfaces 和 implementations
│   └── sync/            - Sync 管理
├── domain/
│   └── usecase/         - 業務邏輯 use cases
├── ui/
│   ├── component/       - 可重用的 UI components
│   ├── navigation/      - Navigation 定義 (Screen sealed class)
│   ├── screen/          - 完整的 screen composables
│   └── viewmodel/       - ViewModels 用於狀態管理
├── di/
│   └── AppModule.kt     - Koin 依賴注入配置
└── util/                - 工具函式和擴充功能

App.kt                   - 主應用程式進入點
```

## 命名慣例 (Naming Conventions)

### 檔案 (Files)
- **Models**: 單數名詞（例如 `Itinerary.kt`、`Location.kt`）
- **Repositories**: `[Entity]Repository.kt` interface，`[Entity]RepositoryImpl.kt` implementation
- **Use Cases**: `[Action][Entity]UseCase.kt`（例如 `CreateItineraryUseCase.kt`）
- **ViewModels**: `[Screen]ViewModel.kt`（例如 `ItineraryListViewModel.kt`）
- **Screens**: `[Screen]Screen.kt`（例如 `ItineraryDetailScreen.kt`）

### 程式碼風格 (Code Style)
- 使用**中文註解**撰寫文件說明
- Data classes 標記 `@Serializable` 用於持久化
- 需要時使用 `@OptIn(ExperimentalTime::class)` for kotlinx.datetime
- ViewModels 使用 Koin 的 `viewModel` DSL
- Use cases 和 repositories 使用 Koin 的 `factory` 或 `single`

## 平台特定程式碼 (Platform-Specific Code)

平台特定實作放在各自的 source sets：
- Storage services 有平台特定實作（例如 `androidMain` 中的 `AndroidStorageService.kt`）
- 透過 `Platform.kt` 使用 expect/actual 模式進行平台偵測
- 資源放在 `commonMain/composeResources/`

## 測試 (Testing)

- 使用 Kotest 進行 property-based 測試
- 測試檔案在 `commonTest/kotlin/` 中鏡像 source 結構
- 命名：`[Class]PropertyTest.kt` 用於 property tests

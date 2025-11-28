# 技術棧 (Tech Stack)

## 建置系統 (Build System)

- **Gradle** 使用 Kotlin DSL (`.gradle.kts`)
- 版本目錄在 `gradle/libs.versions.toml`

## 核心技術 (Core Technologies)

- **Kotlin 2.2.20** - 多平台語言
- **Compose Multiplatform 1.9.1** - UI 框架
- **Koin 4.0.1** - 依賴注入
- **kotlinx.datetime 0.7.1** - 日期時間處理
- **kotlinx.serialization 1.7.3** - JSON 序列化
- **Navigation Compose 2.8.0-alpha10** - 導航
- **Lifecycle ViewModel Compose 2.9.5** - 狀態管理

## 測試 (Testing)

- **Kotest 5.9.1** - Property-based 測試框架
- 測試位於 `composeApp/src/commonTest/kotlin`

## Compose Preview (KMP)

在 Kotlin Multiplatform 專案中使用 Compose Preview：

```kotlin
// 檔案頂部 import
import org.jetbrains.compose.ui.tooling.preview.Preview

// Preview 函數
@Preview
@Composable
private fun MyScreenPreview() {
    MaterialTheme {
        Surface {
            MyScreenContent(...)
        }
    }
}
```

**重要提醒**：
- KMP 專案必須使用 `org.jetbrains.compose.ui.tooling.preview.Preview`
- 不要使用 `androidx.compose.ui.tooling.preview.Preview`（僅適用於 Android 專案）
- Preview 函數應該是 `private` 並且不包含 ViewModel 依賴
- 使用依賴注入模式分離 UI 邏輯，讓 Preview 可以使用假資料

## MVI 架構 (Model-View-Intent)

專案使用 MVI 架構模式來管理 UI 狀態和事件：

### 核心概念

- **State (UiState)**: 不可變的 UI 狀態，使用 `StateFlow` 管理
- **Intent (UiIntent)**: 使用者意圖/動作，觸發狀態變更
- **Event (UiEvent)**: 單次性事件（如導航、Toast），使用 `Channel` 處理

### BaseViewModel

所有 ViewModel 繼承 `BaseViewModel<State, Intent, Event>`：

```kotlin
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEvent>(
    initialState: S
) : ViewModel() {
    val state: StateFlow<S>           // UI 狀態
    val event: Flow<E>                // 單次事件
    protected val currentState: S     // 當前狀態快照
    
    fun handleIntent(intent: I)       // 處理 Intent
    protected abstract suspend fun processIntent(intent: I)
    protected fun updateState(reducer: S.() -> S)
    protected suspend fun sendEvent(event: E)
}
```

### 使用範例

```kotlin
// 1. Contract: 定義 State、Intent、Event
data class MyState(
    val data: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed class MyIntent : UiIntent {
    object LoadData : MyIntent()
    data class DeleteItem(val id: String) : MyIntent()
}

sealed class MyEvent : UiEvent {
    data class NavigateToDetail(val id: String) : MyEvent()
    data class ShowError(val message: String) : MyEvent()
}

// 2. ViewModel: 處理業務邏輯
class MyViewModel(
    private val repository: Repository
) : BaseViewModel<MyState, MyIntent, MyEvent>(
    initialState = MyState()
) {
    override suspend fun processIntent(intent: MyIntent) {
        when (intent) {
            is MyIntent.LoadData -> loadData()
            is MyIntent.DeleteItem -> deleteItem(intent.id)
        }
    }
    
    private suspend fun loadData() {
        val snapshot = currentState  // 使用狀態快照避免競態條件
        updateState { copy(isLoading = true) }
        
        repository.getData()
            .onSuccess { data ->
                updateState { copy(data = data, isLoading = false) }
            }
            .onFailure { error ->
                updateState { copy(isLoading = false, error = error.message) }
                sendEvent(MyEvent.ShowError(error.message ?: "載入失敗"))
            }
    }
}

// 3. Screen: UI 層
@Composable
fun MyScreen(
    viewModel: MyViewModel = koinViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // 收集單次事件
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is MyEvent.NavigateToDetail -> onNavigateToDetail(event.id)
                is MyEvent.ShowError -> { /* 顯示錯誤 */ }
            }
        }
    }
    
    MyScreenContent(
        state = state,
        onIntent = { viewModel.handleIntent(it) }
    )
}
```

### 重要原則

1. **使用 `currentState` 而非 `state.value`**：避免競態條件，確保狀態一致性
2. **State 用於持久狀態**：資料、載入狀態、錯誤訊息
3. **Event 用於單次事件**：導航、Toast、Dialog
4. **Channel vs SharedFlow**：Event 使用 Channel 確保單次消費

## Compose 畫面架構最佳實踐

### 畫面分離模式（MVI 架構）

為了提高可測試性和可維護性，每個畫面應該分成兩個 Composable 函數：

```kotlin
// 1. 主畫面：負責 ViewModel 互動和事件收集
@Composable
fun MyScreen(
    viewModel: MyViewModel = koinViewModel(),
    onNavigate: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // 收集單次事件
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is MyEvent.NavigateBack -> onNavigate()
                is MyEvent.ShowError -> { /* 處理錯誤 */ }
            }
        }
    }
    
    MyScreenContent(
        state = state,
        onIntent = { viewModel.handleIntent(it) }
    )
}

// 2. Content 函數：純 UI 渲染
@Composable
private fun MyScreenContent(
    state: MyState,
    onIntent: (MyIntent) -> Unit
) {
    // 純 UI 邏輯
}

// 3. Preview：使用假資料
@Preview
@Composable
private fun MyScreenPreview() {
    MaterialTheme {
        Surface {
            MyScreenContent(
                state = MyState(...),
                onIntent = {}
            )
        }
    }
}
```

### Import 其他畫面的最佳實踐

**推薦做法**：盡量使用 import 方式引入其他畫面，而不是直接在同一檔案中定義

```kotlin
// ✅ 好的做法：使用 import
import com.example.myapplication.ui.screen.DetailScreen
import com.example.myapplication.ui.screen.SettingsScreen

@Composable
fun MainScreen(navController: NavController) {
    // 使用 import 的畫面
    DetailScreen(...)
}
```

```kotlin
// ❌ 避免：在同一檔案中定義多個畫面
@Composable
fun MainScreen() {
    // 主畫面邏輯
}

@Composable
fun DetailScreen() {  // 應該在獨立檔案中
    // 詳情畫面邏輯
}
```

**優點**：
- 每個畫面有獨立的檔案，易於維護
- 清楚的模組邊界和職責分離
- 更好的程式碼組織和可讀性
- 避免單一檔案過大
- 便於團隊協作（減少合併衝突）

**檔案組織**：
```
ui/screen/
├── MainScreen.kt          // 主畫面
├── DetailScreen.kt        // 詳情畫面
├── SettingsScreen.kt      // 設定畫面
└── ...
```

## 平台目標 (Platform Targets)

- **Android**: Min SDK 24, Target SDK 36, Compile SDK 36
- **iOS**: arm64 和 Simulator arm64
- **Web**: JS 和 WasmJS 目標

## 常用指令 (Common Commands)

### Android
```bash
# Windows
.\gradlew.bat :composeApp:assembleDebug

# macOS/Linux
./gradlew :composeApp:assembleDebug
```

### Web (Wasm - 更快，現代瀏覽器)
```bash
# Windows
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun

# macOS/Linux
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### Web (JS - 支援舊版瀏覽器)
```bash
# Windows
.\gradlew.bat :composeApp:jsBrowserDevelopmentRun

# macOS/Linux
./gradlew :composeApp:jsBrowserDevelopmentRun
```

### iOS
在 Xcode 中開啟 `iosApp` 目錄並執行

### 測試 (Testing)
```bash
# Windows
.\gradlew.bat :composeApp:test

# macOS/Linux
./gradlew :composeApp:test
```

### 編譯驗證 (Compilation Verification)
```bash
# 主要編譯驗證方式（推薦使用）
.\gradlew.bat :composeApp:compileDebugKotlinAndroid

# 其他編譯任務
.\gradlew.bat :composeApp:compileKotlinMetadata
```

## 平台特定儲存 (Platform-Specific Storage)

各平台的儲存實作不同：
- **Android**: DataStore (androidx.datastore:datastore-preferences)
- **iOS**: UserDefaults (待實作)
- **Web**: LocalStorage (待實作)
- **Development**: InMemoryStorageService (目前預設)

## 文件風格 (Documentation Style)

- 使用**中英混合**撰寫文件和註解
- 技術名詞保留英文（如 Compose、ViewModel、Repository）
- 其他說明使用中文

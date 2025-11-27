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

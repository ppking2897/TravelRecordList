# Travel Itinerary App

Kotlin Multiplatform 旅遊行程記事應用程式

## Overview

| 項目 | 內容 |
|------|------|
| **類型** | Kotlin Multiplatform (KMP) + Compose Multiplatform |
| **平台** | Android, iOS, Web (JS/WASM) |
| **架構** | MVI (Model-View-Intent) + Clean Architecture |
| **DI** | Koin 4.0.1 |
| **語言** | Kotlin 2.2.20 |

## Features

- 行程管理 (CRUD)
- 行程項目管理 (時間、地點、活動)
- 照片管理 (新增、刪除、封面設定)
- 標籤 (Hashtag) 功能
- 路線生成與匯出
- 旅遊歷史瀏覽
- 草稿自動儲存
- 離線同步支援

## Project Structure

```
composeApp/src/
├── commonMain/kotlin/com/example/myapplication/
│   ├── data/           # 資料層 (Model, Repository, Storage)
│   ├── domain/         # 業務邏輯層 (Use Cases)
│   ├── ui/             # 展示層 (MVI, Screen, Component)
│   └── di/             # 依賴注入
├── androidMain/        # Android 平台實作
├── iosMain/            # iOS 平台實作
└── commonTest/         # 共用測試
```

## Build & Run

### Android

```bash
# macOS/Linux
./gradlew :composeApp:assembleDebug

# Windows
.\gradlew.bat :composeApp:assembleDebug
```

### Web (WASM)

```bash
# macOS/Linux
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Windows
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
```

### iOS

開啟 `/iosApp` 目錄於 Xcode 並執行。

### Tests

```bash
./gradlew :composeApp:allTests
```

## Architecture

### MVI Pattern

```
User Action → Intent → ViewModel → State Update → Recompose
                         ↓
                      Event (Navigation/Toast)
```

### Layers

| 層級 | 職責 | 目錄 |
|------|------|------|
| Data | 資料模型、Repository、Storage | `data/` |
| Domain | Use Cases (業務邏輯) | `domain/usecase/` |
| Presentation | ViewModel、Screen、Component | `ui/` |

## Documentation

- **專案詳細文檔**: `.claude/project.md`
- **Kiro Spec-Driven**: `.kiro/`
- **Gemini Skills**: `.gemini/`
- **開發歷程歸檔**: `docs/archive/`

## Tech Stack

- Kotlin 2.2.20
- Compose Multiplatform 1.9.1
- Koin 4.0.1
- kotlinx-datetime 0.7.1
- kotlinx-serialization 1.7.3
- Kotest 5.9.1 (Testing)
- Peekaboo (Image Picker)

## License

Private Project

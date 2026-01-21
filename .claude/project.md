# Travel Itinerary App - Project Documentation

## Project Overview

**名稱:** 旅遊行程記事應用程式 (Travel Itinerary Recording App)
**類型:** Kotlin Multiplatform (KMP) + Compose Multiplatform
**支援平台:** Android, iOS, Web (JS/WASM)
**架構模式:** MVI (Model-View-Intent) + Clean Architecture
**DI 框架:** Koin 4.0.1

## Quick Start

```bash
# Android Build
./gradlew :composeApp:assembleDebug

# Run Tests
./gradlew :composeApp:allTests
```

## Project Structure

```
composeApp/src/
├── commonMain/kotlin/com/example/myapplication/
│   ├── App.kt                    # 主入口點 (NavHost)
│   ├── data/
│   │   ├── model/                # 資料模型 (9 個)
│   │   ├── repository/           # Repository 介面與實作 (6 個)
│   │   ├── storage/              # 跨平台儲存抽象
│   │   └── sync/                 # 離線同步管理
│   ├── domain/
│   │   └── usecase/              # 業務邏輯 (22 個 Use Cases)
│   ├── ui/
│   │   ├── mvi/                  # MVI 基礎類別與 Contracts
│   │   ├── screen/               # Composable 畫面 (7 個)
│   │   ├── component/            # 可重用元件 (9 個)
│   │   └── navigation/           # 導航路由
│   └── di/                       # Koin 依賴注入
├── androidMain/                  # Android 平台實作
├── iosMain/                      # iOS 平台實作
└── commonTest/                   # 共用測試程式碼
```

## Architecture

### MVI Pattern Flow
```
User Action → Intent → ViewModel → State Update → Recompose
                         ↓
                      Event (Navigation/Toast)
```

### 核心 MVI 類別
- `BaseViewModel<S, I, E>` - 所有 ViewModel 的抽象基類
- `UiState` - 畫面狀態介面 (不可變 data class)
- `UiIntent` - 使用者意圖 (sealed class)
- `UiEvent` - 一次性事件 (導航、Toast)

## Data Models

| Model | 說明 | 關鍵欄位 |
|-------|------|----------|
| `Itinerary` | 行程 | id, title, startDate, endDate, items |
| `ItineraryItem` | 行程項目 | itineraryId, date, location, activity, arrivalTime, departureTime |
| `Photo` | 照片 | itemId, filePath, thumbnailPath, isCover, order |
| `Route` | 路線 | title, locations[], createdFrom |
| `Location` | 地點 (值物件) | name, latitude?, longitude?, address? |
| `Hashtag` | 標籤 | tag, usageCount |
| `Draft` | 草稿 | type, data (Map) |

### Entity Relationships
```
Itinerary (1) ──┬── (N) ItineraryItem ──── (N) Photo
                └── (N) Route
```

## Screens & Navigation

| 畫面 | ViewModel | 路由 |
|------|-----------|------|
| 行程列表 | `ItineraryListViewModel` | `itinerary_list` (START) |
| 行程詳情 | `ItineraryDetailViewModel` | `itinerary_detail/{id}` |
| 新增/編輯行程 | - | `add_itinerary`, `edit_itinerary/{id}` |
| 新增/編輯項目 | `AddEditItemViewModel` | `add_item/{id}`, `edit_item/{id}` |
| 旅遊歷史 | `TravelHistoryViewModel` | `travel_history` |
| 路線檢視 | `RouteViewViewModel` | `route_view/{id}` |

## Repositories

| Repository | 職責 |
|------------|------|
| `ItineraryRepository` | CRUD 行程、搜尋、索引 |
| `PhotoRepository` | 照片儲存、縮圖、封面 |
| `RouteRepository` | 路線建立與管理 |
| `DraftRepository` | 草稿存取 |
| `HashtagRepository` | 標籤擷取與管理 |

## Use Cases (22 個)

### 行程管理
- `CreateItineraryUseCase`, `UpdateItineraryUseCase`, `DeleteItineraryUseCase`
- `SearchItinerariesUseCase`

### 項目管理
- `AddItineraryItemUseCase`, `UpdateItineraryItemUseCase`, `DeleteItineraryItemUseCase`
- `GroupItemsByDateUseCase`, `FilterItemsByDateUseCase`

### 照片管理
- `AddPhotoUseCase`, `DeletePhotoUseCase`, `SetCoverPhotoUseCase`
- `ReorderPhotosUseCase`, `GenerateThumbnailUseCase`

### 其他
- `ExtractHashtagsUseCase`, `FilterByHashtagUseCase`
- `CreateRouteFromItineraryUseCase`, `GetTravelHistoryUseCase`
- `SaveDraftUseCase`, `LoadDraftUseCase`

## Key Dependencies

```toml
# gradle/libs.versions.toml
kotlin = "2.2.20"
composeMultiplatform = "1.9.1"
koin = "4.0.1"
kotlinx-datetime = "0.7.1"
kotlinx-serialization-json = "1.7.3"
kotest = "5.9.1"
```

## Testing

- **框架:** Kotest 5.9.1
- **類型:** Unit Tests, Property-based Tests
- **位置:** `commonTest/kotlin/`
- **基礎類別:** `BaseViewModelTest` (使用 TestDispatcher)

## Platform-Specific Implementations

### Android (`androidMain/`)
- `AndroidStorageService` - DataStore 實作
- `AndroidImageStorageService` - Bitmap 壓縮與儲存
- `AndroidImagePicker` - 系統圖片選擇器

### iOS/Web
- 平台特定實作待完善

## Development Guidelines

### 新增功能時
1. 在 `data/model/` 定義資料模型
2. 在 `data/repository/` 建立 Repository 介面與實作
3. 在 `domain/usecase/` 建立 Use Case
4. 在 `ui/mvi/{feature}/` 建立 Contract 與 ViewModel
5. 在 `ui/screen/` 建立 Screen Composable
6. 在 `di/AppModule.kt` 註冊依賴

### 程式碼風格
- **使用 import 而非完整路徑**: 避免使用 `com.example.myapplication.data.model.Location` 這類完整路徑，應在檔案開頭加入 `import` 語句
- **範例:**
  ```kotlin
  // ✅ 正確
  import com.example.myapplication.data.model.Location
  val location: Location = ...

  // ❌ 避免
  val location: com.example.myapplication.data.model.Location = ...
  ```

### MVI 命名規範
- State: `{Feature}State`
- Intent: `{Feature}Intent` (sealed class)
- Event: `{Feature}Event` (sealed class)
- ViewModel: `{Feature}ViewModel`

### 錯誤處理
- 使用 `Result<T>` 回傳型別
- 定義 `TravelAppError` sealed class 子類別

## Important Files

### 核心架構
- `ui/mvi/BaseViewModel.kt` - MVI 基礎類別
- `di/AppModule.kt` - Koin 模組定義
- `App.kt` - Navigation 設定

### 資料層
- `data/storage/StorageService.kt` - 儲存抽象介面
- `data/storage/JsonSerializer.kt` - JSON 序列化
- `data/sync/SyncManager.kt` - 離線同步

### 驗證
- `data/model/Validation.kt` - 輸入驗證規則
- `data/model/TravelAppError.kt` - 錯誤類型定義

## Current Status

- **完成度:** ~85%
- **已完成:** 所有核心功能、MVI 架構、7 個畫面、22 個 Use Cases
- **待完善:** iOS/Web 平台實作、測試覆蓋率、效能最佳化

# Google Places API 整合

## 概述

使用 Google Places API (New) 取代 Nominatim，提供更準確的地點搜尋功能。

## API Key 申請

### 相關網址

| 用途 | 網址 |
|------|------|
| Google Cloud Console | https://console.cloud.google.com/ |
| Places API 頁面 | https://console.cloud.google.com/apis/library/places-backend.googleapis.com |
| 憑證管理 | https://console.cloud.google.com/apis/credentials |

### 申請步驟

1. 登入 Google Cloud Console
2. 建立或選擇專案
3. 啟用 API：搜尋「**Places API (New)**」→ 點擊「啟用」
4. 建立憑證：左側選單 → API 和服務 → 憑證 → 建立憑證 → API 金鑰

### API Key 限制設定

#### 開發階段

| 設定 | 選擇 |
|------|------|
| 應用程式限制 | 無（不設定） |
| API 限制 | 僅限 Places API (New) |

這樣 Android 和 iOS 都能用同一個 Key。

#### 正式上線時

建議建立 **兩個不同的 Key**，分別設定應用程式限制：

| Key | 應用程式限制 | 需要資訊 |
|-----|-------------|---------|
| Android Key | Android 應用程式 | Package name + SHA-1 |
| iOS Key | iOS 應用程式 | Bundle ID |

然後在各平台的 `ApiKeyProvider.actual` 中設定對應的 Key。

## 本機設定

在專案根目錄的 `local.properties` 加入：

```properties
GOOGLE_PLACES_API_KEY=你的_API_Key
```

> **注意**：`local.properties` 不會提交到 git，請妥善保管 API Key。

## 費用說明

- 每月免費額度：$200 USD
- Text Search：約 $0.032/次
- 免費額度約可使用 6,000+ 次搜尋/月

詳細定價：https://developers.google.com/maps/documentation/places/web-service/usage-and-billing

## 架構說明

### 檔案結構

```
composeApp/src/
├── commonMain/kotlin/.../
│   ├── config/
│   │   └── ApiKeyProvider.kt          # expect 宣告
│   ├── data/service/
│   │   ├── dto/
│   │   │   └── GooglePlacesDto.kt     # API DTO
│   │   ├── GooglePlacesService.kt     # 實作
│   │   └── NominatimLocationService.kt # 備用（保留）
│   └── domain/service/
│       └── LocationSearchService.kt   # 介面（不變）
├── androidMain/kotlin/.../config/
│   └── ApiKeyProvider.android.kt      # Android actual
└── iosMain/kotlin/.../config/
    └── ApiKeyProvider.ios.kt          # iOS actual
```

### API 端點

| 功能 | Method | Endpoint |
|------|--------|----------|
| Text Search | POST | `https://places.googleapis.com/v1/places:searchText` |
| Place Details | GET | `https://places.googleapis.com/v1/places/{placeId}` |

### Headers

```
X-Goog-Api-Key: {API_KEY}
X-Goog-FieldMask: places.id,places.displayName,places.formattedAddress,places.location
Accept-Language: {語言代碼}
Content-Type: application/json
```

> **重要**：`Accept-Language` header 是回傳正確語言結果的關鍵，光靠 request body 的 `languageCode` 不夠。

### 多語言支援

根據輸入文字自動檢測語言，使用 `detectLanguage()` 函數：

| 輸入內容 | 語言代碼 | 結果語言 |
|---------|---------|---------|
| 含平假名/片假名 | `ja` | 日文 |
| 含韓文 | `ko` | 韓文 |
| 含中文（無假名）| `zh-TW` | 繁體中文 |
| 純英文/其他 | `en` | 英文 |

**Unicode 字符範圍檢測**：
- 平假名：`\u3040-\u309F`
- 片假名：`\u30A0-\u30FF`
- CJK 漢字：`\u4E00-\u9FFF`
- 韓文諺文：`\uAC00-\uD7AF`

### 資料映射

| Google API | LocationSuggestion |
|------------|-------------------|
| `id` | `placeId` |
| `displayName.text` | `name` |
| `formattedAddress` | `address` |
| `location.latitude` | `latitude` |
| `location.longitude` | `longitude` |

## UI 元件：LocationSearchField

### 元件特性

使用 `ExposedDropdownMenuBox` 實作智慧地址搜尋：

| 特性 | 說明 |
|------|------|
| 下拉位置 | 顯示在輸入框**下方**，不遮擋輸入 |
| 焦點保持 | 輸入時焦點不會被下拉選單搶走 |
| Debounce | 預設 800ms，避免輸入時頻繁觸發 API |
| 自動語言 | 根據輸入內容自動選擇結果語言 |

### 為什麼用 ExposedDropdownMenuBox？

| 元件 | 問題 |
|------|------|
| `DropdownMenu` | 會覆蓋輸入框、搶焦點 |
| `ExposedDropdownMenuBox` | 專為搜尋/自動完成設計 |

### Debounce 機制

```kotlin
LaunchedEffect(value) {
    // 等待使用者停止輸入
    delay(debounceMs)  // 預設 800ms

    // 才執行 API 搜尋
    locationSearchService.searchByName(value)
}
```

當 `value` 改變時，舊的 coroutine 會被取消，新的會啟動。只要使用者持續輸入，之前的搜尋就會被取消，達成 debounce 效果。

### 使用方式

```kotlin
LocationSearchField(
    value = locationName,
    onValueChange = { locationName = it },
    onLocationSelected = { suggestion ->
        // 處理選中的地點
        suggestion?.let {
            latitude = it.latitude
            longitude = it.longitude
        }
    },
    locationSearchService = locationSearchService,
    debounceMs = 800L,  // 可調整
)
```

## 切換回 Nominatim

如需切換回免費的 Nominatim 服務，修改 `AppModule.kt`：

```kotlin
// 從 Google Places
single<LocationSearchService> { GooglePlacesService(get()) }

// 改回 Nominatim
single<LocationSearchService> { NominatimLocationService(get()) }
```

## 驗證方式

1. 編譯：`./gradlew compileDebugKotlin`
2. 執行 App，開啟 AddEditItemScreen
3. 輸入「京都稻荷神社」
4. 應顯示「伏見稲荷大社」相關結果
5. 選擇地點，確認座標正確填入

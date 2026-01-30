# 智慧地址輸入功能規格

## 概述

提供地點名稱搜尋功能，使用者輸入地點名稱時即時顯示搜尋建議，選擇後自動填入名稱、地址和經緯度。

## 功能

- 輸入時即時搜尋建議 (debounced, 500ms)
- 下拉選單顯示搜尋結果
- 選擇後自動填入地點資訊
- 支援手動輸入（不使用搜尋功能）
- 顯示已選擇的座標資訊

## 架構

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌─────────────────────┐    ┌─────────────────────────────┐ │
│  │ LocationSearchField │────│ AddEditItemScreen           │ │
│  │ (Compose Component) │    │ (使用 LocationSearchField)   │ │
│  └─────────────────────┘    └─────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                            │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ LocationSearchService (Interface)                        │ │
│  │ LocationSuggestion, LocationDetails (Data Classes)       │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                       Data Layer                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ NominatimLocationService (Implementation)                │ │
│  │ - OpenStreetMap Nominatim API                           │ │
│  │ - Ktor HttpClient                                        │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## API

### LocationSearchService (Interface)

```kotlin
interface LocationSearchService {
    suspend fun searchByName(query: String): Result<List<LocationSuggestion>>
    suspend fun getPlaceDetails(placeId: String): Result<LocationDetails>
}
```

### LocationSuggestion

```kotlin
data class LocationSuggestion(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?
)
```

### LocationDetails

```kotlin
data class LocationDetails(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String? = null,
    val website: String? = null
)
```

### LocationSearchField (Composable)

```kotlin
@Composable
fun LocationSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSelected: (LocationSuggestion?) -> Unit,
    locationSearchService: LocationSearchService? = null,
    modifier: Modifier = Modifier,
    label: String = "地點名稱",
    placeholder: String = "輸入地點名稱搜尋...",
    debounceMs: Long = 500L
)
```

#### 參數

| 參數 | 類型 | 說明 |
|------|------|------|
| value | String | 當前輸入值 |
| onValueChange | (String) -> Unit | 輸入值變更回調 |
| onLocationSelected | (LocationSuggestion?) -> Unit | 選擇地點回調 |
| locationSearchService | LocationSearchService? | 搜尋服務（null 則只能手動輸入） |
| label | String | 輸入框標籤 |
| placeholder | String | 提示文字 |
| debounceMs | Long | 防抖延遲 (毫秒) |

## 實作細節

### NominatimLocationService

- 使用 OpenStreetMap Nominatim API
- API URL: `https://nominatim.openstreetmap.org`
- 需要設置 User-Agent header
- 速率限制：最多 1 request/second
- 僅供非商業用途

### Ktor 依賴

```kotlin
// commonMain
implementation(libs.ktor.client.core)
implementation(libs.ktor.client.content.negotiation)
implementation(libs.ktor.serialization.kotlinx.json)

// androidMain
implementation(libs.ktor.client.okhttp)

// iosMain
implementation(libs.ktor.client.darwin)
```

## 整合位置

### AddEditItemScreen

- 使用 `LocationSearchField` 取代原本的 `OutlinedTextField`
- 選擇地點後觸發 `SelectLocation` Intent
- ViewModel 處理選擇邏輯並更新 State

### AddEditItemContract

新增欄位：
- `locationLatitude: Double?`
- `locationLongitude: Double?`
- `locationPlaceId: String?`

新增 Intent：
- `SelectLocation(suggestion: LocationSuggestion?)`

## 相關檔案

| 檔案 | 說明 |
|------|------|
| `domain/service/LocationSearchService.kt` | 介面定義 |
| `data/service/NominatimLocationService.kt` | API 實作 |
| `presentation/components/LocationSearchField.kt` | UI 元件 |
| `presentation/add_edit_item/AddEditItemScreen.kt` | 使用位置 |
| `presentation/add_edit_item/AddEditItemContract.kt` | State/Intent |
| `presentation/add_edit_item/AddEditItemViewModel.kt` | 處理邏輯 |
| `di/AppModule.kt` | DI 註冊 |
| `gradle/libs.versions.toml` | Ktor 版本 |
| `composeApp/build.gradle.kts` | Ktor 依賴 |

## 完成日期

2026-01-30

# UI Deprecation 修復規格

## 目標

修復所有 Compose Material3 的 deprecation 警告。

## 需修改的檔案

### 1. Icons.Filled.ArrowBack → Icons.AutoMirrored.Filled.ArrowBack

| 檔案 | 行數（約） |
|------|-----------|
| `presentation/add_edit_item/AddEditItemScreen.kt` | 111 |
| `presentation/add_edit_itinerary/AddEditItineraryScreen.kt` | 129, 393 |
| `presentation/edit_item/EditItemScreen.kt` | 123 |
| `presentation/itinerary_detail/ItineraryDetailScreen.kt` | 271 |
| `presentation/route_view/RouteViewScreen.kt` | 372 |
| `presentation/travel_history/TravelHistoryScreen.kt` | 81 |

**變更**：
```kotlin
// 舊
Icons.Filled.ArrowBack

// 新
Icons.AutoMirrored.Filled.ArrowBack
```

**需加入 import**：
```kotlin
import androidx.compose.material.icons.automirrored.filled.ArrowBack
```

---

### 2. Divider → HorizontalDivider

| 檔案 | 行數（約） |
|------|-----------|
| `presentation/route_view/RouteViewScreen.kt` | 89, 151, 176 |
| `presentation/travel_history/TravelHistoryScreen.kt` | 243 |

**變更**：
```kotlin
// 舊
Divider(
    modifier = ...,
    thickness = ...,
    color = ...
)

// 新
HorizontalDivider(
    modifier = ...,
    thickness = ...,
    color = ...
)
```

**需加入 import**：
```kotlin
import androidx.compose.material3.HorizontalDivider
```

**需移除 import**（如果有）：
```kotlin
// 移除
import androidx.compose.material3.Divider
```

---

### 3. Modifier.menuAnchor() → 新版 API

| 檔案 |
|------|
| `presentation/components/DateDropdown.kt` |

**變更**：
```kotlin
// 舊
Modifier.menuAnchor()

// 新
Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
```

**需加入 import**：
```kotlin
import androidx.compose.material3.MenuAnchorType
```

---

## 執行步驟

1. 依序修改上述檔案
2. 執行編譯：`./gradlew :composeApp:compileDebugKotlinAndroid`
3. 確認 deprecation 警告消失
4. Git commit：`fix: 修復 Material3 deprecation 警告`

## 驗證標準

編譯輸出應不再出現以下警告：
- `'val Icons.Filled.ArrowBack: ImageVector' is deprecated`
- `'fun Divider(...)' is deprecated`
- `'fun Modifier.menuAnchor(): Modifier' is deprecated`

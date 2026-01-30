---
task_type: mechanical
verification:
  - compile
max_turns: 8
model: haiku
---

# Entity KDoc 註解補充

## 目標

為 domain/entity 目錄下的資料類別補充 KDoc 文件註解。

## 需修改的檔案

| 檔案 | 變更 |
|------|------|
| `domain/entity/Itinerary.kt` | 加入類別和屬性 KDoc |
| `domain/entity/ItineraryItem.kt` | 加入類別和屬性 KDoc |
| `domain/entity/Location.kt` | 加入類別和屬性 KDoc |

## 具體變更

### 1. Itinerary.kt

在 `data class Itinerary` 上方加入：

```kotlin
/**
 * 行程實體，代表一個完整的旅行計畫。
 *
 * @property id 行程唯一識別碼
 * @property title 行程標題
 * @property description 行程描述
 * @property startDate 行程開始日期
 * @property endDate 行程結束日期
 * @property coverImagePath 封面圖片路徑
 * @property items 行程項目列表
 * @property createdAt 建立時間
 * @property updatedAt 更新時間
 */
```

### 2. ItineraryItem.kt

在 `data class ItineraryItem` 上方加入：

```kotlin
/**
 * 行程項目實體，代表行程中的單一活動或景點。
 *
 * @property id 項目唯一識別碼
 * @property itineraryId 所屬行程 ID
 * @property title 項目標題
 * @property description 項目描述
 * @property location 地點資訊
 * @property startTime 開始時間
 * @property endTime 結束時間
 * @property order 排序順序
 * @property isCompleted 是否已完成
 * @property hashtags 標籤列表
 * @property photos 照片列表
 * @property createdAt 建立時間
 * @property updatedAt 更新時間
 */
```

### 3. Location.kt

在 `data class Location` 上方加入：

```kotlin
/**
 * 地點實體，代表地理位置資訊。
 *
 * @property name 地點名稱
 * @property address 地址
 * @property latitude 緯度
 * @property longitude 經度
 */
```

## 執行步驟

1. 讀取各檔案的現有內容
2. 在 data class 宣告上方加入對應的 KDoc
3. 確保 KDoc 與 data class 之間無空行
4. 執行編譯：`./gradlew :composeApp:compileDebugKotlinAndroid`

## 驗證標準

- 編譯無錯誤
- 每個目標類別都有 KDoc 註解
- KDoc 參數與實際屬性對應

## 注意事項

- 保持現有 import 不變
- KDoc 使用繁體中文
- 屬性順序依照 data class 中的定義順序

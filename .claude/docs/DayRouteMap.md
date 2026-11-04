# 用 Google 地圖看當天路線

> **目的**：在路線頁，一鍵把某一天的地點交給 Google 地圖 App，看這天整體怎麼走；旅途中要移動時，查相鄰兩站之間怎麼搭大眾運輸。不在 App 內畫地圖、不呼叫付費 API。

---

## 行為規格

> 情境寫使用者看得到的結果，程式名稱放技術設計。

### Requirement: 每天可以在 Google 地圖看當天的多站路線 <!-- ADDED 2026-11-04 -->

路線頁每一天的段落 SHALL 提供「當天路線」按鈕，按下後開啟 Google 地圖，帶入這天依序的所有地點。不指定交通方式，由使用者在 Google 地圖裡切換（多站路線在 Google 地圖只能開車或走路，大眾運輸由下一條 Requirement 負責）。

#### Scenario: 兩站以上的天可以開多站路線
- GIVEN 某天依序有伏見稻荷大社、清水寺、金閣寺三個地點
- WHEN 使用者按這天的「當天路線」
- THEN 開啟 Google 地圖，起點是伏見稻荷大社、中途站是清水寺、終點是金閣寺
- AND 不指定交通方式

#### Scenario: 只有一站的天沒有「當天路線」
- GIVEN 某天只有一個地點
- WHEN 使用者看路線頁的這一天
- THEN 這天沒有「當天路線」

#### Scenario: 地點沒有座標時改用地址或名稱
- GIVEN 某站有座標、某站沒有座標但有地址、某站只有名稱
- WHEN 使用者開這天的 Google 地圖
- THEN 有座標的站用座標，沒座標的站用地址，連地址都沒有的站用名稱

#### Scenario: 超過 11 站只帶前 11 站並提示
- GIVEN 某天有 13 個地點
- WHEN 使用者看路線頁的這一天
- THEN 「當天路線」下方提示「Google 地圖一次最多 11 站，只會帶前 11 站」
- AND 按下後開啟的路線是前 11 站（第 1 站為起點、第 11 站為終點）

### Requirement: 相鄰兩站之間可以查大眾運輸怎麼去 <!-- ADDED 2026-11-04 -->

路線頁同一天裡每兩個相鄰地點之間 SHALL 提供「怎麼去」，按下後開啟 Google 地圖，從前一站到後一站，交通方式指定大眾運輸。不同天之間不提供。

#### Scenario: 相鄰兩站之間查大眾運輸
- GIVEN 某天依序有淺草寺、東京晴空塔
- WHEN 使用者按兩站之間的「怎麼去」
- THEN 開啟 Google 地圖，起點是淺草寺、終點是東京晴空塔，交通方式是大眾運輸

#### Scenario: 「怎麼去」只出現在同一天的相鄰兩站之間
- GIVEN 第 1 天有 A、B 兩站，第 2 天有 C 一站
- WHEN 使用者看路線頁
- THEN A 與 B 之間有「怎麼去」
- AND B 與 C 之間、C 之後都沒有「怎麼去」

#### Scenario: 「怎麼去」的地點沒有座標時改用地址或名稱
- GIVEN 前一站有座標、後一站只有名稱
- WHEN 使用者按兩站之間的「怎麼去」
- THEN 起點用座標，終點用名稱

### Requirement: 每一站可以查從目前位置怎麼過去 <!-- ADDED 2026-11-04 -->

路線頁每一站卡片上的地圖按鈕 SHALL 開啟 Google 地圖，從使用者目前的位置到這一站，交通方式指定大眾運輸。給旅途中「我現在要怎麼去下一站」用；「當天路線」則留給排行程時看整體怎麼繞。

#### Scenario: 從目前位置到這一站
- GIVEN 某站是東京晴空塔
- WHEN 使用者按這站卡片上的地圖按鈕
- THEN 開啟 Google 地圖，起點是使用者目前的位置，終點是東京晴空塔，交通方式是大眾運輸

#### Scenario: 這一站沒有座標時改用地址或名稱
- GIVEN 某站只有名稱「秋葉原」
- WHEN 使用者按這站卡片上的地圖按鈕
- THEN 終點用「秋葉原」

### Requirement: 行程詳情的地圖圖示直接打開路線頁 <!-- ADDED 2026-11-04 -->

行程詳情頂部的地圖圖示 SHALL 打開路線頁（原本打開地圖頁）。地圖頁跟路線頁功能重疊（規格書待確認 #5），路線頁已能依天看、跳 Google 地圖，所以拿掉地圖頁；「⋮」選單裡的「生成路線」也拿掉，入口只留地圖圖示。

#### Scenario: 地點夠的行程按地圖圖示進入路線頁
- GIVEN 行程有兩個以上不同的地點
- WHEN 使用者按行程詳情頂部的地圖圖示
- THEN 進入這個行程的路線頁

#### Scenario: 地點不夠的行程按地圖圖示留在詳情頁並提示
- GIVEN 行程只有一個地點
- WHEN 使用者按行程詳情頂部的地圖圖示
- THEN 留在行程詳情，提示「至少要有兩個不同的地點才能看路線」

#### Scenario: 「⋮」選單沒有「生成路線」
- WHEN 使用者打開行程詳情的「⋮」選單
- THEN 選單只有選擇模式、編輯行程、刪除行程

#### Scenario: 從路線頁返回回到行程詳情
- GIVEN 使用者從行程詳情的地圖圖示進入路線頁
- WHEN 使用者按返回（左上角箭頭或系統返回）
- THEN 回到同一個行程的詳情

---

## 技術設計

### 架構基準

照 `feature/route/presentation/RouteShareText.kt`：組字串的規則寫成路線頁 presentation 層的純函式，由 `RouteViewScreen` 呼叫，用既有的 `LocalUriHandler.openUri` 開網址（路線頁「開啟 Google 地圖」單一地點已經這樣做）。不新增 ViewModel 狀態、Repository 或 DI。

### Google 地圖網址（Maps URLs，Directions）

文件：<https://developers.google.com/maps/documentation/urls/get-started>。免費、不需要 API key。

```
https://www.google.com/maps/dir/?api=1
  &origin=<第一站>
  &destination=<最後一站>
  &waypoints=<中間各站，用 | 分隔，編碼成 %7C>
  &travelmode=transit        ← 只有「怎麼去」帶
```

- 每站的值：有座標用 `緯度,經度`，否則用地址（空字串或只有空白視為沒有），再沒有就用名稱；一律做 URL 參數編碼
- 站名裡的 `|` 換成空白：`|` 是中途站的分隔符，留在站名裡 Google 會拆成兩站
- 站的順序照 `RouteDay.stops` 在清單裡的順序，跟路線頁畫面與「怎麼去」一致，不另外依 `order` 排序
- 手機上沒有能開網址的 App 時，`openUri` 會丟例外；路線頁改成底部提示「沒有可以開啟地圖的 App」
- 中途站上限：文件寫「手機瀏覽器 3 個、其他 9 個」，所以一次最多 11 站（起點＋9 中途＋終點）
- 網址長度上限 2,048 字元；11 站全用中文名稱時約 1,000 字元，不另外處理
- 2026-11-04 實機確認：多站路線指定 `travelmode=transit` 時，Google 地圖 App 的大眾運輸顯示「—」並改用開車；單段大眾運輸在日本可以查到班次與票價

### 地圖圖示改接路線頁（Slice 3）

```diff
  ItineraryDetailScreen 頂部
-   地圖圖示 → onMapClick → AppRoute.ItineraryMap
+   地圖圖示 → ItineraryDetailIntent.GenerateRoute（沿用：排得出路線才 NavigateToRoute）
-   ⋮ 選單「生成路線」
  App.kt PlanNavHost
-   is AppRoute.ItineraryMap -> NavEntry { ItineraryMapScreen(...) }
  core/presentation/navigation/Screen.kt
-   AppRoute.ItineraryMap
- feature/itinerary_map/**（Contract、Screen、ViewModel、DI）
- core/presentation/components/ItineraryMapView（expect + Android、iOS actual）、MapInfoCard
- feature/route：MapMarker、GetMapMarkersUseCase（＋測試、DI）
  feature/route/data/repository/RouteRepositoryImpl
~   錯誤訊息「…才能生成路線」→「…才能看路線」
```

- 刪除範圍以「拿掉入口後沒有任何呼叫者」為準；做真地圖時地圖改放進路線頁，需要時從 git 歷史找回
- 跟著變成沒人用而刪掉的：`CommonComponents.kt` 的 `ErrorContent`、行程詳情的分享圖示 import
- 刻意保留：`DateColorPalette`（依日期上色，留給真地圖，改善計畫「真地圖」那段會用到）
- 連點地圖圖示會送兩次前往路線頁（每個 Intent 各跑一個 coroutine）；`App.kt` 推路線頁前檢查頂端是否已是同一趟的路線頁，是就不再推
- 舊版停在地圖頁時更新 App：系統更新時會結束 App，畫面存檔跟著丟掉，打開時從行程列表重新開始，不會還原到不存在的地圖頁（2026-11-04 實機確認，所以不保留舊路由）
- 版本目錄裡沒被引用的 `maplibre-compose` 不動（改善計畫「真地圖」的待辦）

### 函式（暫定名稱，實作時可調）

```kotlin
// feature/route/presentation/RouteGoogleMapsLink.kt
data class DayMapsLink(val url: String, val truncated: Boolean)

fun RouteDay.toGoogleMapsDayLink(): DayMapsLink?                       // 不到 2 站回 null
fun googleMapsTransitUrl(from: RouteLocation, to: RouteLocation): String
fun googleMapsTransitFromHereUrl(to: RouteLocation): String  // 不帶 origin，Google 地圖用裝置位置
```

- 「從目前位置」靠 Maps URLs 的規則：沒帶 `origin` 時用裝置位置（文件：「most relevant starting location, such as device location, if available」）。沒有定位權限時 Google 地圖會讓使用者自己填起點。

---

## 涵蓋範圍

- 識別字：`DayMapsLink`、`toGoogleMapsDayLink`、`googleMapsTransitUrl`、`googleMapsTransitFromHereUrl`、`RouteViewScreen`、`ItineraryDetailScreen`、`ItineraryDetailIntent.GenerateRoute`、`ItineraryDetailEvent.NavigateToRoute`、`AppRoute.RouteView`、`RouteRepositoryImpl`
- 路徑：`feature/route/presentation/**`、`feature/itinerary_detail/presentation/**`、`feature/route/data/repository/RouteRepositoryImpl.kt`、`App.kt`、`core/presentation/navigation/Screen.kt`
- 外部：Google Maps URLs（`https://www.google.com/maps/dir/?api=1`）

---

## 實作計畫 + 驗證紀錄

### Slice 1 — 路線頁的「當天路線」與「怎麼去」
- **對應 Requirement**: 每天可以在 Google 地圖看當天的多站路線；相鄰兩站之間可以查大眾運輸怎麼去
- **commit**: `6b7cabb`；審查修正 `4c0c461`
- **高風險**: 否（只開外部網址，不換 App 內的頁面、不改存起來的資料）
- **整體狀態**: ✅

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 兩站以上的天可以開多站路線 | 邏輯 | ✅ | 2026-11-04 | `unit test: RouteGoogleMapsLinkTest#三站的天起點是第一站中途站是中間終點是最後一站且不指定交通方式`、`#兩站的天沒有中途站`、`#多站路線照當天清單的順序跟畫面一致`、`#中途站之間用直線分隔`、`#站名含特殊字元時網址不會被切斷`、`#中途站分隔符在網址裡編碼成百分比寫法`、`#站名裡的直線換成空白不會被拆成兩站`（改壞：漏中途站、指定大眾運輸、不照清單順序、不做編碼、站名的 | 不換掉都紅；反向：參數順序對調維持綠）；實機 `DayRouteMap/device-check.md` #1（13 站的天帶 11 站，終點日本橋）；實機：東京第 2 天明治神宮 → 原宿竹下通在 Google 地圖帶入兩站、預設開車；東京第 1 天暫時加到 5 站（淺草寺、上野公園、晴空塔、東京車站、銀座，測完還原資料），Google 地圖起點淺草寺、3 個停靠站依序、終點銀座，開車 1 小時 2 分 |
| 只有一站的天沒有「當天路線」 | 邏輯 | ✅ | 2026-11-04 | `unit test: RouteGoogleMapsLinkTest#只有一站的天沒有多站路線`（改壞：一站也給路線 → 紅）；實機：東京第 3 天只有秋葉原，標題旁沒有按鈕；實機 `DayRouteMap/device-check.md` #3 |
| 地點沒有座標時改用地址或名稱 | 邏輯 | ✅ | 2026-11-04 | `unit test: RouteGoogleMapsLinkTest#沒座標的站用地址連地址都沒有就用名稱`、`#只有緯度沒有經度的站視為沒有座標`、`#地址是空字串或空白時用名稱`（改壞：不用座標、跳過地址、空白地址照用都紅）；實機 `DayRouteMap/device-check.md` #4（只有名稱的「竹下通可麗餅」被 Google 找到） |
| 超過 11 站只帶前 11 站並提示 | 邏輯 | ✅ | 2026-11-04 | `unit test: RouteGoogleMapsLinkTest#十三站的天只帶前十一站並標示被截斷`、`#剛好十一站不算被截斷`（改壞：不截斷、11 站也標截斷都紅）；實機 `DayRouteMap/device-check.md` #1、#2（提示文字、終點是第 11 站） |
| 相鄰兩站之間查大眾運輸 | 邏輯 | ✅ | 2026-11-04 | `unit test: RouteGoogleMapsLinkTest#怎麼去從前一站到後一站並指定大眾運輸`（改壞：不指定大眾運輸 → 紅）；實機：淺草寺 → 晴空塔開啟 Google 地圖大眾運輸，19 分、都08 公車；實機 `DayRouteMap/device-check.md` #5 |
| 「怎麼去」只出現在同一天的相鄰兩站之間 | UI | ✅ | 2026-11-04 | 實機：東京第 1、2 天的兩站之間各有一個；晴空塔與明治神宮（跨天）之間、秋葉原之後都沒有；實機 `DayRouteMap/device-check.md` #6 |
| 「怎麼去」的地點沒有座標時改用地址或名稱 | 邏輯 | ✅ | 2026-11-04 | `unit test: RouteGoogleMapsLinkTest#怎麼去的終點沒有座標時用名稱`；實機 `DayRouteMap/device-check.md` #7 |

### Slice 2 — 每站的「從目前位置怎麼過去」
- **對應 Requirement**: 每一站可以查從目前位置怎麼過去
- **commit**: `6b7cabb`；審查修正 `4c0c461`
- **高風險**: 否（只開外部網址）
- **整體狀態**: ✅

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 從目前位置到這一站 | 邏輯 | ✅ | 2026-11-04 | `unit test: RouteGoogleMapsLinkTest#從目前位置到這站不帶起點並指定大眾運輸`（改壞：帶了起點、沒指定大眾運輸都紅）；實機：按上野公園卡片的地圖按鈕，Google 地圖起點「你的位置」、終點上野公園（人在台灣，Google 顯示找不到路線）；同格式網址到台北 101 開出大眾運輸 2 小時 35 分；實機 `DayRouteMap/device-check.md` #8 |
| 這一站沒有座標時改用地址或名稱 | 邏輯 | ✅ | 2026-11-04 | `unit test: RouteGoogleMapsLinkTest#從目前位置到沒有座標的站用名稱`；實機 `DayRouteMap/device-check.md` #9 |

### Slice 3 — 地圖圖示改打開路線頁，拿掉地圖頁
- **對應 Requirement**: 行程詳情的地圖圖示直接打開路線頁
- **commit**: `194dcf5`；審查修正 `4c0c461`
- **高風險**: 是（換頁或返回：地圖圖示的目的地改變、拿掉一個 route）
- **整體狀態**: ✅

| Scenario | 性質 | 狀態 | 驗證日 | 驗證方式 |
|----------|------|------|--------|---------|
| 地點夠的行程按地圖圖示進入路線頁 | 邏輯＋UI | ✅ | 2026-11-04 | 單元測試只驗 ViewModel 那段：既有 `ItineraryDetailViewModelTest#生成路線成功`（送出前往路線頁）；圖示接到 `GenerateRoute`、`App.kt` 推路線頁這段接線沒有單元測試，靠實機：實機 `DayRouteMap/device-check.md` #10 |
| 地點不夠的行程按地圖圖示留在詳情頁並提示 | 邏輯 | ✅ | 2026-11-04 | `unit test: ItineraryDetailViewModelTest#地點不足` 預期文字改成「…才能看路線」，改 code 前先跑紅、改後綠；實機 `DayRouteMap/device-check.md` #11 |
| 「⋮」選單沒有「生成路線」 | UI | ✅ | 2026-11-04 | 實機：選單只剩選擇模式、編輯行程、刪除行程；實機 `DayRouteMap/device-check.md` #12 |
| 從路線頁返回回到行程詳情 | UI | ✅ | 2026-11-04 | 實機：左上箭頭、系統返回各一次，都回到東京 3 日遊的詳情；實機 `DayRouteMap/device-check.md` #13；快速連點地圖圖示後按一次返回就回到詳情（#14）；舊版停在地圖頁時覆蓋安裝，不閃退（#15） |

---

## 待辦 / 跟外部對齊

| 項目 | 屬於誰 | 阻擋上線？ | 移除條件 |
|------|-------|----------|---------|
| iOS 開 Google 地圖網址未驗證 | App（需要 Mac） | 否 | 在 iOS 實機按「當天路線」「怎麼去」與每站的地圖按鈕，都能開啟 Google 地圖或瀏覽器並帶入各站，且確認瀏覽器時的站數上限與提示文字相符（code 標記在 `RouteGoogleMapsLink.kt` 檔頭） |

---

## 變更紀錄

- **2026-11-04** ADDED: Requirement「每天可以在 Google 地圖看當天的多站路線」
- **2026-11-04** ADDED: Requirement「相鄰兩站之間可以查大眾運輸怎麼去」
- **2026-11-04** ADDED: Requirement「每一站可以查從目前位置怎麼過去」（每站卡片的地圖按鈕原本是在 Google 地圖搜尋這個地點，改成從目前位置規劃大眾運輸路線）
- **2026-11-04** 「在 Google 地圖看這天」按鈕文字改成「當天路線」：原文字太長，把每天的標題擠成兩行
- **2026-11-04** ADDED: Requirement「行程詳情的地圖圖示直接打開路線頁」（地圖頁與「⋮ → 生成路線」拿掉）
- **2026-11-04** 路線頁地點卡片拿掉「座標：35.7101, 139.8107」那一行：數字對使用者沒有意義，按鈕仍用資料裡的座標定位
- **2026-11-04** 先試做「App 內 Google 地圖＋Routes API」後放棄：Routes API 在日本查不到大眾運輸（紀錄在 improvement-plan.md 後續功能）。改成跳到 Google 地圖 App。
- **2026-11-04** 審查修正：站的順序改照清單順序（跟畫面一致，原本另外依 `order` 排序，兩者現況一定相同）；站名裡的 `|` 換成空白；手機上沒有能開網址的 App 時提示而不閃退；連點地圖圖示不會推兩層路線頁；刪掉跟著沒人用的 `ErrorContent`。行程詳情的 Requirement「生成路線」同步改寫，見 [ItineraryDetail.md](ItineraryDetail.md)。

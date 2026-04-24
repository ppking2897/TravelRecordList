---
name: travelapp-firstrun-walkthrough
description: |
  This skill should be used when you need to smoke-test the TravelApp app as a brand-new user — fresh install, no seed data. Useful for:
  - 驗證 empty state / 首次使用體驗
  - 回歸測試核心流程（建立行程 → 加項目 → 打卡 → 快速紀錄 → 四個 tab 狀態）
  - 產生 PR / release 前的示意截圖
  - 在改 `InitializeSampleDataUseCase` 或 tab 切換邏輯後，快速重走一遍

  觸發詞: 新使用者流程, 首次使用, firstrun walkthrough, empty state 測試, TravelApp smoke test
model: sonnet
tools: ["Bash", "Read", "Edit", "mcp__mobile-mcp__mobile_list_elements_on_screen", "mcp__mobile-mcp__mobile_click_on_screen_at_coordinates", "mcp__mobile-mcp__mobile_type_keys", "mcp__mobile-mcp__mobile_swipe_on_screen", "mcp__mobile-mcp__mobile_save_screenshot", "mcp__mobile-mcp__mobile_take_screenshot"]
---

# TravelApp First-Run Walkthrough

完整走過 TravelApp 新使用者流程的自動化腳本。每個關鍵畫面落地 PNG，供 PR 截圖或回歸比對。

## Prerequisites

- Android emulator `emulator-5554` 已開機
- `adb.exe` 路徑：`C:\Users\{USERNAME}\AppData\Local\Android\Sdk\platform-tools\adb.exe`（Git Bash：`/c/Users/.../adb.exe`）
- 網路連線可用（Google Places 搜尋會用到）
- 若目前 device 有裝 `com.example.myapplication`，會被 uninstall

## When to Use vs When NOT to Use

**會用**：
- 要產空白狀態 → 有資料的對比截圖
- 想驗證 empty state 文案 / CTA
- 改了 navigation、tab 切換、QuickRecord sheet 後想整體跑一遍

**不要用**：
- 只想看「有資料」狀態 → 直接安裝 + 不要關 seed，用 `adb launch` 即可
- 想測 process death / 權限 → 這個 skill 不處理

## Flow Overview

```
uninstall → (可選) 關 seed → build+install → launch
  ↓
① 行程 tab empty state
② 建立行程：Hokkaido Winter Trip, 2026-04-24~27
③ 詳情 empty → 新增項目
④ Places auto-fill：Ski at Niseko @ Niseko
⑤ 時間軸顯示 (0/1)
⑥ 今日 tab hero + item card
⑦ 打卡 → 1/1 已完成
⑧ 快速紀錄 sheet → Ramen break @ Sapporo
⑨ 足跡 tab：1 旅行 / 2 地點 / 1 打卡
⑩ 回憶 tab：empty（行程未結束）
  ↓
還原 seed → 結束
```

## Step-by-Step

### Step 0 — Clean slate

若要完全空白（連 seed 資料都不要），**先** 編輯 `composeApp/src/commonMain/kotlin/com/example/myapplication/feature/itinerary_list/presentation/ItineraryListViewModel.kt`，把 `initializeSampleDataUseCase()` 暫時註解掉。**走完流程務必還原**（這個 skill 最後會強制提醒）。

```bash
ADB="/c/Users/EF11409254/AppData/Local/Android/Sdk/platform-tools/adb.exe"
"$ADB" uninstall com.example.myapplication
cd "D:/My Data/EF11409254/Bianca/Android/Project/TravelApp/main"
./gradlew :androidApp:installDebug
"$ADB" shell monkey -p com.example.myapplication -c android.intent.category.LAUNCHER 1
```

> **Gradle task 注意**：install 任務只在 `:androidApp` 模組，不在 `:composeApp`。`:composeApp:installDebug` 不存在。

### Step 1 — 行程 tab empty state

**預期**：標題「還沒有行程」、CTA「建立第一個行程」、三色 chip（規劃中/旅行中/已結束）都 0。

**操作**：點「建立第一個行程」按鈕。

| 座標 | 元素 |
|------|------|
| (582, 1610) | 建立第一個行程 CTA |

**截圖**：`screenshots/01-empty-itinerary-list.png`

### Step 2 — 建立行程表單

**操作**：
1. 點活動標題欄位 (540, 1030) → 輸入 `Hokkaido Winter Trip`
2. 點開始日期「選擇」(925, 1508) → date picker
3. picker 裡點 `Today, Friday, April 24, 2026` (791, 1483) → 確定 (919, 1835)
4. 點結束日期「選擇」(925, 1718) → date picker
5. picker 裡點 `Monday, April 27, 2026` (288, 1609) → 確定 (919, 1835)
6. 點底部儲存 (540, 1938)

**截圖**：
- `02-create-itinerary-form.png`（初始）
- `03-date-picker.png`（picker 展開）
- `04-create-itinerary-filled.png`（欄位都填完）

### Step 3 — 詳情 empty state → 新增項目

**預期**：Day 1~4 tabs、「還沒有行程項目」、CTA「新增第一個地點」。

**操作**：點「新增第一個地點」(582, 1148)。

**截圖**：`05-detail-empty.png`

### Step 4 — 新增項目 + Google Places 自動帶入

**操作**：
1. 點活動欄位 (540, 490) → 輸入 `Ski at Niseko`
2. 點地點名稱 (540, 700) → 輸入 `Niseko`
3. **等 Places 建議跳出**（需要網路）→ 點下拉第一筆 `Niseko, Abuta District, Hokkaido, Japan` (540, 800)
4. 驗證自動帶入：地點地址變成完整地址，「座標: 42.804785599999995, 140.6874494」顯示出來
5. 點日期 (540, 900) → 選 `2026-04-24 (週五)` (540, 1008)
6. 向上滑 (`swipe up 600`) 讓儲存按鈕進入畫面
7. 點儲存 (540, 1933)

**截圖**：
- `06-add-item-empty-form.png`
- `07-places-suggestion.png`（下拉建議可見）
- `08-places-autofilled.png`（選完後地址+座標填好）

> **Places 重點驗證**：選建議後，**地點名稱、地址、座標** 三個欄位同時被填入。若只有名稱被填，代表 LocationSearchField 的 suggestion callback 壞掉。

### Step 5 — 詳情 with item

**預期**：
- 時間軸 Day 1 顯示「Niseko (0/1)」chip，下方卡片顯示 `2026-04-24` + `Ski at Niseko` + `Niseko`
- **Bottom navigation 不應顯示**（App.kt 的 `isPlanDeepLevel` 邏輯：`selectedTab == Plan && backStack.size > 1` 時隱藏）。若 elements 裡仍抓到「行程/今日/足跡/回憶」四個 text，代表 guard 壞掉
- FAB「新增項目」會補滿 bottom bar 騰出的空間（y 約 2127）

**截圖**：`09-detail-with-item.png`

### Step 6 — 今日 tab

**操作**：點 bottom bar 今日 (405, 2245)。

**預期**：
- Hero 卡：`今天 2026/04/24` + `Hokkaido Winter Trip` + `0/1 已完成` + 進度條空
- 兩個 CTA：`新增現在`、`看完整行程`
- Item card：`未排時間` (因為沒設到達時間) + `Ski at Niseko` + `Niseko` + 右側「打卡」按鈕
- FAB `快速紀錄` 在右下

**截圖**：`10-today-tab.png`

### Step 7 — 打卡

**操作**：點 item 卡上的「打卡」(895, 1115)。

**預期**：
- Hero 變 `1/1 已完成`，進度條填滿
- Item 右側 badge 從「打卡」變「完成」

**截圖**：`11-today-after-checkin.png`

### Step 8 — 快速紀錄 sheet

**操作**：點 FAB 快速紀錄 (862, 1944)。

**預期 sheet 內容**：
- 標題「快速紀錄」
- Subtitle「預設加入 2026/04/24 · Hokkaido Winter Trip」
- 欄位：活動、地點（有放大鏡 icon = LocationSearchField）、備註
- 加入照片 0、儲存到今日

**操作**：
1. 活動 (540, 1360) → 輸入 `Ramen break`
2. 地點 (540, 1560) → 輸入 `Sapporo`
3. 等 Places 下拉 → 點 `Sapporo, Hokkaido, Japan` (540, 1720)
4. 點「儲存到今日」(540, 2185)

**預期 save 後**：
- Sheet 關閉
- 今日 tab hero 變 `1/2 已完成`
- 時間軸多一張卡：`06:08` + `Ramen break` + `Sapporo`（06:08 是 `now()` 時間）
- 底部 snackbar「已加入今日紀錄」

**截圖**：
- `12-quickrecord-sheet.png`
- `13-quickrecord-places-suggestion.png`
- `14-today-after-quickrecord.png`

> **Sheet 高度驗證**：sheet 從底部展開時應該完整顯示到「儲存到今日」按鈕，不會被 nav bar 擋住。若按鈕被切掉代表 `rememberModalBottomSheetState(skipPartiallyExpanded = true)` 或 `verticalScroll` 被拔掉。

### Step 9 — 足跡 tab

**操作**：點 bottom bar 足跡 (680, 2245)。

**預期**（1 個進行中的行程）：
- 頂部統計：`1 旅行 / 2 地點 / 1 打卡`
- 行程卡：`Hokkaido Winter Trip` + `2 個地點 · 1 打卡 · 2026/04/24`

**截圖**：`15-footprint-tab.png`

### Step 10 — 回憶 tab

**操作**：點 bottom bar 回憶 (955, 2245)。

**預期**：Empty state「還沒有可以回味的旅程」+ 說明文字「等行程的結束日期過了之後，這裡會留下屬於你的故事。」

（行程結束日 `2026-04-27` 還沒到，所以回憶為空）

**截圖**：`16-memory-empty.png`

### Step 11 — 還原 seed（MUST）

走完 **一定要還原** `ItineraryListViewModel.kt` 裡被註解掉的 `initializeSampleDataUseCase()`，否則下次別人 clone 專案會遇到永遠空白的假 bug。

```bash
# 檔案：composeApp/src/commonMain/kotlin/com/example/myapplication/feature/itinerary_list/presentation/ItineraryListViewModel.kt
# 把這行還原：
# initializeSampleDataUseCase()
```

## Troubleshooting

| 現象 | 可能原因 | 解法 |
|------|---------|------|
| Gradle 找不到 `:composeApp:installDebug` | install task 只在 `:androidApp` | 用 `:androidApp:installDebug` |
| Places 建議沒跳出來 | 無網路 / API key 未設 | 檢查 `local.properties` 的 `MAPS_API_KEY` + emulator WiFi |
| 點建議後只填名稱、地址沒變 | LocationSearchField callback 壞 | 查 `LocationSearchField.kt` + `ItineraryItemViewModel.selectLocation` |
| 快速紀錄 sheet 按鈕被切掉 | partially expanded / 沒有 scroll | `TodayScreen.kt QuickRecordSheet` 加 `skipPartiallyExpanded` + `verticalScroll` |
| 中文輸入不進去 | mobile_type_keys 只吃 ASCII | 改用英文 trip 名（本 skill 已採用） |
| 打卡後沒進度條變化 | State 沒回流 | 查 `TodayViewModel` 處理 `ToggleComplete` intent |

## Output

走完後提供：
1. 16 張截圖於 `.claude/skills/travelapp-firstrun-walkthrough/screenshots/`
2. 任何異常畫面的 finding（截圖 + 對應檔案位置）
3. 確認 seed 已還原

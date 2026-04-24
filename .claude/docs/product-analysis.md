# TravelApp 產品分析報告

> 分析日期：2026-04-24
> 分析基礎：程式碼現狀掃描 + 同類 App 參考（Google Maps、TripAdvisor、Klook、KKday、Airbnb、Booking.com、TripIt、Wanderlog、Polarsteps、Foursquare、Splitwise、TravelSpend、Maps.me）

---

## 一、產品概述

### 定位

**旅遊行程記事 App** — 一款以「自助旅行者的行程規劃 + 旅途記錄 + 事後回顧」為核心的 KMP 跨平台工具應用。產品形態介於 *規劃工具（Wanderlog）* 與 *旅行日記（Polarsteps / Journi）* 之間。

### 目標使用者

- 自助旅行者（主要場景：海外自由行、國內多日遊）
- 重視「事前規劃 + 事後回顧」的旅行愛好者
- 家庭 / 小團體旅行的召集人

### 技術棧

| 項目 | 技術 |
|------|------|
| 平台 | Kotlin Multiplatform（Android / iOS / Web） |
| UI | Compose Multiplatform + Material 3 |
| 導航 | Navigation 3（NavDisplay + backStack） |
| 架構 | MVI + Clean Architecture |
| DI | Koin 4.0.1 |
| 外部整合 | Google Places API、Peekaboo Image Picker |

---

## 二、現有功能清單

### 2.1 Feature 模組（12 個）

| # | Feature | 有畫面 | 核心能力 | 完整度 |
|---|---------|--------|---------|--------|
| 1 | `itinerary` | — | 行程 CRUD、搜尋、Phase 判斷（規劃中/旅行中/已結束） | 高 |
| 2 | `itinerary_list` | Yes | 行程列表 | 高 |
| 3 | `itinerary_detail` | Yes | 行程詳情、Timeline | 高 |
| 4 | `itinerary_add_edit` | Yes | 新增 / 編輯行程 | 高 |
| 5 | `itinerary_item` | Yes（Add + Edit） | 項目 CRUD、批次、照片、Hashtag、日期分組 | 高 |
| 6 | `itinerary_map` | Yes | 行程地圖（P2-3：按日期上色待做） | 中 |
| 7 | `route` | Yes | 路線生成、JSON 匯出、Google Maps 連結、Map Marker | 中 |
| 8 | `travel_history` | Yes | 旅遊歷史、日期篩選 | 中 |
| 9 | `footprint` | Yes | 造訪地點足跡 | 中 |
| 10 | `memory` | Yes | 旅遊故事（每日 highlights） | 中 |
| 11 | `today` | Yes | 今日行程 Dashboard | 中 |
| 12 | `draft` | — | 草稿自動儲存 | 高 |

### 2.2 Domain Model

已具備的核心模型：`Itinerary`、`ItineraryItem`、`Location`（含經緯度）、`Photo`（含 cover、order、thumbnail）、`Hashtag`（含 usageCount）、`Route` / `RouteLocation` / `MapMarker`、`VisitedPlace`、`TripMemory` / `MemoryStory` / `DailyHighlight`、`Draft`、`TravelPhase`。

### 2.3 UseCase 盤點（已具備的業務能力）

| 分類 | 已有能力 |
|------|---------|
| 行程 | Create / Update / Delete / GetAll / GetById / Search / GetPhase / InitSample |
| 項目 | Add / Update / Delete / GetByItinerary / GetById / BatchUpdate / BatchDelete / GroupByDate / FilterByDate / FilterByHashtag / ExtractHashtags |
| 照片 | Add / Delete / SetCover / Reorder / GenerateThumbnail |
| 路線 | CreateFromItinerary / GetRoute / Export / GetMapMarkers |
| 歷史 | GetTravelHistory / FilterByDateRange |
| 足跡 | GetVisitedPlaces |
| 回憶 | GetMemories |
| 草稿 | Load / Save / Delete |

### 2.4 Navigation 路由

`ItineraryList`、`ItineraryDetail`、`AddItinerary`、`EditItinerary`、`AddItem`、`QuickAddItem`（帶 defaultDate）、`EditItem`、`TravelHistory`、`RouteView`、`ItineraryMap`。

> 推測 `today` / `footprint` / `memory` 可能透過底部 Tab 進入（非 Route）。

---

## 三、現階段強項（Already Good）

1. **旅行週期全覆蓋**：Planning → InProgress → Completed，三階段都有對應畫面（Today / ItineraryDetail / Memory）。這是多數工具型 App 缺乏的。
2. **記錄顆粒度細**：每項目支援到達/離開時間、Hashtag、多照片、封面。
3. **事後回顧差異化**：`memory` 模組產生每日故事 + highlights，這是 Klook / KKday 完全沒有的，與 Polarsteps 重疊。
4. **架構清晰**：feature-first Clean Architecture，新增功能成本低。
5. **跨平台**：KMP Android/iOS/Web 同一份程式碼。

---

## 四、功能缺口分析（對比同類 App）

### 4.1 旅行規劃類（TripIt / Wanderlog）缺口

| 缺口 | 說明 |
|------|------|
| 訂房 / 機票 / 票券資訊卡 | 無法承載「交通」「住宿」資訊（只有 Activity 欄位） |
| 行程模板庫 | 沒有「3 日東京範本」可套用 |
| 自動 Email 解析 | 無法從訂房確認信自動建立項目 |
| 行前待辦（打包清單） | 無 |
| 跨行程比較 | 無 |

### 4.2 地點發現類（Google Maps / TripAdvisor）缺口

| 缺口 | 說明 |
|------|------|
| 景點評分 / 評論 | 只有自己的備註 |
| 附近景點推薦 | 規劃時無法「在這地點附近還可以去哪」 |
| 景點營業時間 / 電話 | Place 只有 name + lat/lng + address |
| 步行 / 車程時間計算 | 路線只生成不計算時間 |
| 同類景點排行 | 無 |

### 4.3 體驗預訂類（Klook / KKday）缺口

| 缺口 | 說明 |
|------|------|
| 門票 / 活動預訂 | 無 |
| 票券 Wallet（QR Code 集中） | 無 |
| 價格追蹤 | 無 |

*（此類通常屬於 OTA 業務，自建成本極高，但可用深度連結 / 分潤導流）*

### 4.4 旅途記錄類（Polarsteps / Journi）缺口

| 缺口 | 說明 |
|------|------|
| GPS 自動追蹤軌跡 | 需手動新增 Item |
| 抵達自動打卡 | 無 geofence |
| 當日照片自動匯入當日項目 | 需手動選擇照片 |
| 旅行日記文字編輯 | 只有 notes 欄位，無富文本 |
| 電子書 / 相冊匯出 | 無 |
| 徽章 / 成就（國家數、城市數） | 無 |

### 4.5 費用記帳類（Splitwise / TravelSpend）缺口

| 缺口 | 說明 |
|------|------|
| 費用記錄 | 無任何欄位 |
| 多幣別 / 匯率換算 | 無 |
| 預算追蹤 | 無 |
| 旅伴分帳 | 無 |
| 分類統計（交通/住宿/餐飲） | 無 |

### 4.6 離線能力（Maps.me）缺口

| 缺口 | 說明 |
|------|------|
| 離線地圖 | 無 |
| 離線翻譯 / 常用語 | 無 |
| 無網路時的資料持久化 | 目前使用 InMemoryStorage（重啟即消失） |

### 4.7 社交 / 協作類（Airbnb / Wanderlog）缺口

| 缺口 | 說明 |
|------|------|
| 多人協作編輯 | 無帳號系統 |
| 行程分享連結 | 只有 JSON 匯出 |
| 旅伴邀請 | 無 |
| 公開行程庫（抄別人的行程） | 無 |
| 旅伴即時位置分享 | 無 |

### 4.8 平台功能缺口

| 缺口 | 說明 |
|------|------|
| 推播通知 | 無（行程開始、項目時間） |
| Widget（今日行程） | 無 |
| Deep Link / 分享 | 僅 Google Maps 連結 |
| 語音輸入 | 無 |
| Wear OS / watchOS | 無 |

---

## 五、功能建議（按優先級）

> **評估標準**：使用者價值（V）× 開發成本（C），並考慮「現有架構可支援度」。

### 5.1 P0 — MVP 差異化必要

#### 1. 費用追蹤 💰
**User Story**：作為旅行者，我希望在每個項目記錄花費，並看到整趟旅行的總花費與分類統計，以便控制預算與事後報銷。

- **能力**：每 `ItineraryItem` 加 `expense`（金額、幣別、分類、付款人）；新增 `BudgetSummary` 畫面
- **為何 P0**：差異化關鍵功能（競品 KKday/Wanderlog 皆有）、使用頻率高、與現有 Item 耦合低
- **規模**：中（~2 週）
- **依賴**：無

#### 2. 打包 / 行前清單 📋
**User Story**：作為旅行者，我希望每個行程有獨立的待辦清單（簽證、訂房、打包物品），以免漏帶東西。

- **能力**：`Itinerary` 下增加 `Checklist`；提供預設模板（海島、滑雪、商務、親子）
- **為何 P0**：低成本（單 Entity + UI）、高使用頻率、無外部依賴
- **規模**：小（~3-5 天）

#### 3. 推播提醒通知 🔔
**User Story**：作為旅行者，我希望在行程開始前 1 天、當日每個項目時間前 30 分鐘收到提醒，不會錯過行程。

- **能力**：行程 D-1、當日項目 arrival time 前 N 分鐘
- **為何 P0**：使用者實際場景（容易錯過景點時間）；平台基礎能力應具備
- **規模**：中（Android 用 WorkManager / iOS 用 UNUserNotificationCenter，需寫 expect/actual）

#### 4. 資料持久化（Room / SQLDelight） 💾
**User Story**：作為使用者，重開 App 後我的行程資料仍然存在。

- **能力**：把 `InMemoryStorageService` 換成 SQLDelight / Room（跨平台）
- **為何 P0**：**目前 App 無法真正使用**（重啟即消失），這是出貨前的硬門檻
- **規模**：中（~1-2 週）
- **依賴**：無，但建議最先做

---

### 5.2 P1 — 體驗提升 & 社交傳播

#### 5. 距離 / 車程計算 🚗
**User Story**：規劃時我希望看到兩個景點之間的車程 / 步行時間，判斷行程安排是否合理。

- **能力**：`ItineraryDetail` 項目之間顯示「12 分鐘車程」「8 分鐘步行」
- **實作**：Google Directions API / 簡易 Haversine 估算
- **規模**：小-中

#### 6. 行程模板庫 📚
**User Story**：我想從「3 日東京自由行」「5 日京阪奈」範本快速建立行程，而不是從零開始。

- **能力**：內建 5-10 個熱門地區範本；一鍵複製到新行程；日後可開放「社群範本」
- **為何 P1**：降低新手使用門檻、提升首次留存率
- **規模**：小（資料層為主）

#### 7. 天氣整合 🌤️
**User Story**：行程卡片顯示當日天氣預報，我能據此調整活動安排。

- **能力**：OpenWeatherMap / Weather API 整合；ItineraryDetail 每日顯示
- **規模**：小

#### 8. 分享連結 / PDF 匯出 📤
**User Story**：我想把行程傳給朋友 / 家人看，不需要他們裝同一個 App。

- **能力**：生成唯讀分享連結（H5 頁面）或 PDF；含時間、地點、照片
- **為何 P1**：社交傳播關鍵、拉新成本最低的功能
- **規模**：中

#### 9. 離線地圖快取 📍
**User Story**：出國沒網路時我仍能看見行程地圖與地點位置。

- **能力**：下載當前行程範圍的地圖圖磚；地點卡片本地快取
- **規模**：中-高

#### 10. GPS 自動打卡 / 抵達建議 📌
**User Story**：抵達景點時 App 自動提示「是否標記完成 + 拍照」。

- **能力**：Geofence（Android/iOS 原生）
- **規模**：中（需處理背景權限）

---

### 5.3 P2 — 長期規劃 / 進階功能

#### 11. 雲端同步 + 帳號系統 ☁️
- Google / Apple Sign-In、多裝置同步、備份還原
- **規模**：高（需後端）

#### 12. 多人即時協作 👥
- 邀請旅伴共同編輯、評論、指派任務
- **依賴**：雲端同步

#### 13. 旅伴分帳 💳
- 誰付了什麼、自動計算應付
- **實作參考**：Splitwise

#### 14. 票券錢包 🎫
- 掃描門票 / 訂房 QR Code 集中管理
- OCR 識別訂單確認信

#### 15. AI 旅遊助手 🤖
- 推薦景點（基於偏好）
- 自動優化路線
- 自然語言新增（「下午 3 點到淺草雷門」）

#### 16. 自動 Email 解析（訂房 / 機票）
- 類似 TripIt；從 Gmail/Outlook 抓訂單資訊
- **規模**：高，需 OAuth + NLP

#### 17. Widget / 快捷方式
- 桌面 Widget（今日行程）、iOS 鎖定畫面、Siri 快捷

#### 18. 社群分享 / 行程市集
- 公開行程庫；追蹤 KOL 旅行；一鍵套用

---

## 六、腦洞建議（Wild Cards — 極思廣益）

> 以下為跳脫同類 App 框架、可能成為記憶點 / 差異化的功能。非短期必做，但值得在產品路線圖上保留。

| # | 想法 | 描述 |
|---|------|------|
| W1 | **時光膠囊** | 旅行結束滿 1 年 / 3 年，App 主動推播「當年今天你在 {地點}」，讓回憶被重新喚起 |
| W2 | **心情曲線** | 每項目可打心情分數（1-5 星 + emoji），旅行結束生成「心情地圖」與折線圖 |
| W3 | **聲音日記** | 每項目可錄 30 秒語音，AI 轉文字自動填入 notes；事後回放成「旅行 Podcast」 |
| W4 | **AR 景點辨識** | 在景點用相機對準建築，辨識後自動填入項目（利用 MLKit / CoreML） |
| W5 | **旅伴足跡重疊** | 匿名顯示「你的 3 位朋友也去過這裡」，帶動社交黏性（類似 Swarm） |
| W6 | **行程再跑一次** | 一鍵複製過往行程為新行程（或 KOL 行程套用到我的日期） |
| W7 | **旅行碳足跡** | 根據交通方式計算碳排；累積顯示「你今年旅行的碳足跡 = 種 N 棵樹」 |
| W8 | **當地人視角** | 整合小紅書 / IG 在地熱點標記，過濾觀光陷阱 |
| W9 | **行李追蹤整合** | 支援 AirTag / Tile，把行李位置顯示在地圖上 |
| W10 | **時區旅伴小 Widget** | 在旅途中永遠顯示「當地時間 ↔ 家鄉時間」，避免時差打電話擾人 |
| W11 | **旅行音樂播放清單** | 行程自動配對當時聽的 Spotify 清單，事後回顧有 BGM |
| W12 | **自動相冊章節** | 事後把照片依地點 / 日期自動分章節，一鍵生成電子書 |
| W13 | **旅行徽章系統** | 達成「5 個國家」「100 個城市」「連續 10 天旅行」解鎖徽章 |
| W14 | **「不確定行程」模式** | 規劃時項目可標記「待定」「備選」，支援背包客的彈性旅行 |
| W15 | **AI 每日摘要 push** | 旅行中每晚自動推播「今日 3 個 highlight + 明天重點」 |
| W16 | **旅行緊急小工具** | 一鍵顯示 當地大使館 / 緊急電話 / 常用語音翻譯（SOS 模式） |
| W17 | **「我在哪」即時分享** | 簡訊 / LINE 一鍵分享當前行程位置給家人，有時效連結 |
| W18 | **家族回憶牆** | 同一家族成員的歷次旅行合併顯示年度回顧（小孩看到爸媽 10 年前帶他去哪） |

---

## 七、優先級矩陣

```
使用者價值 ↑
    高 │ [資料持久化] [分享連結/PDF]  │ [AI 助手] [自動Email解析]
       │ [費用追蹤]   [打包清單]      │ [雲端同步] [多人協作]
       │ [推播通知]   [距離計算]      │ [離線地圖]
    中 │ [天氣] [模板庫]              │ [票券錢包] [分帳]
       │ [GPS 打卡]                   │ [行程市集]
    低 │ [心情曲線][時光膠囊]          │ [AR 辨識] [碳足跡]
       └──────────────────────────────┴─────────────────→
         低          中                 高       開發成本
```

---

## 八、建議開發順序

### Phase 1（1-2 個月）— 出貨前門檻 + 核心差異化
1. **資料持久化（Room/SQLDelight）** — 必做門檻
2. **費用追蹤** — 最強差異化
3. **打包清單** — 低成本高價值
4. **推播提醒** — 平台基礎能力

### Phase 2（2-3 個月）— 體驗提升 + 傳播
5. 距離 / 車程計算
6. 天氣整合
7. 行程模板庫
8. 分享連結 / PDF 匯出
9. UI/UX P2-P3 收尾（拖拽排序、日期顏色、Shimmer、深色模式，見 ux-improvements.md）

### Phase 3（3-6 個月）— 旅途中體驗
10. 離線地圖
11. GPS 自動打卡
12. Widget
13. 心情曲線 / 聲音日記（Wild card 試水）

### Phase 4（6 個月+）— 平台 & 社交
14. 雲端同步 + 登入
15. 多人協作
16. 分帳功能
17. 票券錢包
18. AI 旅遊助手

---

## 九、技術債 & 先決工作

先於新功能處理：

1. **InMemoryStorage → 真實持久化**（Room / SQLDelight）— P0 門檻
2. **MVI 架構收尾**：確認 7 個畫面全部遷移完成
3. **Fake-First 流程落地**：目前新功能規劃時，先寫 Fake Repository + Domain，再等合約定案才做 staging（見 CLAUDE.md「開發階段 Repository 策略」）
4. **測試覆蓋率補強**：ViewModel、UseCase 單元測試
5. **UI/UX 剩餘任務**：P2-2 拖拽排序、P2-3 地圖按日期上色、P3 系列（參考 ux-improvements.md）

---

## 十、同類 App 對標矩陣（快速視覺化）

| 功能類別 | TravelApp | Wanderlog | TripIt | Polarsteps | Klook |
|---------|:---------:|:---------:|:------:|:----------:|:-----:|
| 行程 CRUD | ✅ | ✅ | ✅ | ✅ | ❌ |
| 地圖視覺化 | ✅ | ✅ | ❌ | ✅ | ❌ |
| 費用追蹤 | ❌ | ✅ | ❌ | ❌ | ❌ |
| 打包清單 | ❌ | ✅ | ❌ | ❌ | ❌ |
| 通知提醒 | ❌ | ✅ | ✅ | ❌ | ✅ |
| 協作編輯 | ❌ | ✅ | ❌ | ❌ | ❌ |
| 離線地圖 | ❌ | ✅ | ❌ | ✅ | ❌ |
| GPS 追蹤 | ❌ | ❌ | ❌ | ✅ | ❌ |
| Email 解析 | ❌ | ✅ | ✅ | ❌ | ❌ |
| 票券預訂 | ❌ | ❌ | ❌ | ❌ | ✅ |
| 事後故事回顧 | ✅ | ❌ | ❌ | ✅ | ❌ |
| 跨平台（含 Web） | ✅ | ✅ | ✅ | ❌ | ❌ |

> TravelApp 獨有優勢：**事後故事回顧**（memory 模組）＋ **跨平台（含 Web）**

---

## 變更紀錄

- 2026-04-24：重寫一版，納入同類 App 對標、Wild Cards 腦洞建議、優先級矩陣。原因：用戶要求極思廣益提出想法
- 2026-04-09：首次分析版本

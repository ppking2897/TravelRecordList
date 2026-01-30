---
name: plan-agent-prompt
description: |
  開始實作任何規格 (spec) 或功能時必須使用此 Skill。
  觸發時機：「開 spec」、「實作功能」、「啟動 agent」、「plan agent」、
  「準備 agent prompt」、「規劃實作」、「開始 phase」。
  用於規劃精確的檔案路徑和具體指令，避免 Agent 浪費 token 探索專案。
---

# Agent Prompt 規劃器

## 目的

在啟動 Agent 之前，先規劃精確的檔案路徑和具體修改指令，讓 Agent 可以直接開工，不需要花費 token 探索專案結構。

## 執行步驟

### Step 1: 確認任務範圍

向用戶確認：
- 要實作什麼功能？
- 有沒有相關的 spec 文件？

### Step 2: 使用 Glob 找出相關檔案

根據功能需求，用 Glob 搜尋可能相關的檔案：

```bash
# 範例：搜尋 presentation 層的檔案
Glob: composeApp/src/commonMain/kotlin/**/presentation/**/*.kt

# 範例：搜尋特定功能相關
Glob: **/*Itinerary*.kt
```

**只搜尋路徑，不讀取內容**（省 token）

### Step 2.5: （可選）用 Grep 查看關鍵結構

如果不熟悉現有程式碼結構，用 Grep + context 參數查看關鍵定義，**不需讀取整個檔案**：

```bash
# 查看 State 結構（-A = After，顯示匹配行之後 N 行）
Grep: "data class.*State" -A 15

# 查看 Intent 定義
Grep: "sealed class.*Intent" -A 20

# 查看現有函數簽名（-C = Context，顯示前後各 N 行）
Grep: "private.*fun " -C 1

# 查看 Repository 介面方法
Grep: "suspend fun " --path=*Repository.kt -A 1
```

**參數說明**：
| 參數 | 意思 | 用途 |
|-----|------|------|
| `-A 10` | After - 之後 10 行 | 看定義內容 |
| `-B 5` | Before - 之前 5 行 | 看註解說明 |
| `-C 3` | Context - 前後各 3 行 | 看上下文 |

**Token 比較**：
- 讀整個 ViewModel（400 行）：~800 tokens
- Grep 關鍵結構：~50 tokens

### Step 3: 整理檔案清單

將找到的檔案分類：

| 類型 | 檔案路徑 | 預期變更 |
|-----|---------|---------|
| 需新建 | `.../components/NewComponent.kt` | 新元件 |
| 需修改 | `.../feature/FeatureScreen.kt` | 加入新元件 |
| 需修改 | `.../feature/FeatureContract.kt` | 新增 State/Intent |
| 需修改 | `.../feature/FeatureViewModel.kt` | 處理邏輯 |

### Step 4: 產生精確的 Agent Prompt

根據檔案清單，產生結構化的 prompt：

```markdown
## 任務：[功能名稱]

### 需修改的檔案

1. `composeApp/src/commonMain/kotlin/.../FeatureContract.kt`
   - 在 State 加入 `newField: Type`
   - 新增 Intent: `NewAction`

2. `composeApp/src/commonMain/kotlin/.../FeatureScreen.kt`
   - 在 Content 區塊加入新元件
   - 處理 onNewAction 回調

3. `composeApp/src/commonMain/kotlin/.../FeatureViewModel.kt`
   - 在 handleIntent 加入 NewAction 處理

### 需新建的檔案

1. `composeApp/src/commonMain/kotlin/.../components/NewComponent.kt`
   - Composable 函數
   - 參數：...
   - 功能：...

### 執行指令

請依序：
1. 讀取上述需修改的檔案
2. 建立新檔案
3. 修改現有檔案
4. 執行編譯驗證
```

### Step 5: 輸出並確認

將產生的 prompt 輸出給用戶確認，確認後可以：
- 直接在主對話執行（最省 token）
- 複製給 Agent 執行（適合平行任務）

## 範例輸出

```
已規劃 Agent Prompt：

## 任務：實作拖曳排序功能

### 需修改的檔案（3 個）

1. `composeApp/src/commonMain/kotlin/com/example/myapplication/presentation/itinerary_detail/ItineraryDetailContract.kt`
   - State 加入 `isDragging: Boolean`, `draggedItemId: String?`
   - 新增 Intent: `StartDrag(itemId)`, `EndDrag`, `ReorderItems(fromIndex, toIndex)`

2. `composeApp/src/commonMain/kotlin/com/example/myapplication/presentation/itinerary_detail/ItineraryDetailScreen.kt`
   - LazyColumn 加入 dragAndDropModifier
   - 處理拖曳手勢回調

3. `composeApp/src/commonMain/kotlin/com/example/myapplication/presentation/itinerary_detail/ItineraryDetailViewModel.kt`
   - handleIntent 加入拖曳相關處理
   - 實作 reorderItems 邏輯

### 執行方式

建議：主對話直接執行（檔案互相關聯，適合累積上下文）
```

## 注意事項

- **Glob 找路徑**：找檔案位置，不讀內容
- **Grep 看結構**：需要了解現有定義時，用 `-A`/`-B`/`-C` 只看關鍵部分
- **不要用 Read 讀整個檔案**：除非檔案很小（如 Contract.kt）
- 產生的 prompt 應該具體到「哪個檔案的哪個部分要改什麼」

## Token 消耗參考

| 操作 | Token 消耗 |
|-----|-----------|
| Glob 找 10 個檔案路徑 | ~30 tokens |
| Grep 關鍵結構 | ~50 tokens |
| Read 整個 ViewModel | ~800 tokens |
| Read 整個 Screen | ~1000 tokens |

**目標**：規劃階段總消耗 < 100 tokens

---
name: run-agent-task
description: |
  執行帶驗證的 Agent 任務。觸發時機：
  - "run agent task", "執行 agent 任務", "跑 agent"
  - "用 agent 做", "agent 執行 spec"
  - 提供 spec 檔案路徑時

  此 Skill 會：
  1. 解析 Spec 檔案中的任務定義
  2. 使用適當的 Agent 執行任務
  3. 執行驗證（編譯、架構檢查）
  4. 報告結果並提供修復建議
---

# Agent 任務執行器（帶驗證）

## 使用方式

```
/run-agent-task <spec-file-path>
/run-agent-task docs/specs/MY_FEATURE_SPEC.md
```

## 執行流程

### Step 1: 解析 Spec 檔案

讀取 Spec 檔案，提取以下資訊：

```yaml
# Spec 檔案應包含的 frontmatter（可選）
---
task_type: mechanical | logic | refactor
verification:
  - compile
  - architecture
  - custom: "自訂驗證指令"
max_turns: 10
model: haiku | sonnet | opus
---
```

**任務類型判斷**：
| 類型 | 特徵 | 適合 Agent |
|------|------|-----------|
| `mechanical` | 明確的程式碼替換、格式修正 | ✅ 是 |
| `logic` | 需要理解業務邏輯的實作 | ⚠️ 謹慎 |
| `refactor` | 大規模結構調整 | ❌ 否 |

### Step 2: 執行 Agent 任務

根據任務類型選擇執行方式：

**Mechanical 任務**：
```
使用 Task tool，設定：
- subagent_type: "general-purpose"
- model: "haiku" (快速且便宜)
- prompt: Spec 中的具體指令
```

**Logic 任務**：
```
警告使用者此任務可能不適合 Agent，建議：
1. 拆分成更小的 mechanical 任務
2. 或在主 Chat 中執行
```

### Step 3: 驗證階段

依序執行驗證：

#### 3.1 編譯檢查（預設啟用）
```bash
./gradlew :composeApp:compileDebugKotlinAndroid 2>&1
```

**成功標準**：
- 退出碼為 0
- 無 `error:` 輸出
- deprecation 警告數量不增加

#### 3.2 架構檢查（可選）
```
執行 /check-architecture skill
```

#### 3.3 自訂驗證（Spec 中定義）
```bash
# 例如：檢查特定檔案是否修改
git diff --name-only | grep "expected_file.kt"
```

### Step 4: 結果報告

#### 成功報告
```
✅ Agent 任務完成

📋 任務摘要：
   - Spec: docs/specs/UI_DEPRECATION_FIX_SPEC.md
   - 修改檔案數: 7
   - 執行輪數: 3/10

📝 變更清單：
   - presentation/add_edit_item/AddEditItemScreen.kt
   - presentation/components/DateDropdown.kt
   - ...

✔️ 驗證結果：
   - [✅] 編譯成功
   - [✅] 架構檢查通過
   - [✅] 無新增 deprecation 警告
```

#### 失敗報告
```
❌ Agent 任務失敗

📋 任務摘要：
   - Spec: docs/specs/FEATURE_SPEC.md
   - 執行輪數: 10/10 (達到上限)

🔴 失敗原因：
   - 編譯錯誤：Unresolved reference 'newFunction'

💡 建議：
   1. 檢查 Agent 是否遺漏了必要的 import
   2. 此任務可能涉及邏輯實作，建議改用主 Chat
   3. 手動修復後執行：./gradlew compileDebugKotlinAndroid
```

---

## Spec 檔案格式

### 完整範例

```markdown
---
task_type: mechanical
verification:
  - compile
  - architecture
max_turns: 5
model: haiku
---

# 任務名稱

## 目標
簡述要達成的目標

## 需修改的檔案

| 檔案 | 變更 |
|------|------|
| `path/to/file.kt` | 說明 |

## 具體變更

### 變更 1: 說明
```kotlin
// 舊
old_code()

// 新
new_code()
```

## 驗證標準
- 編譯無錯誤
- 無新增警告
```

---

## 安全機制

### 任務過濾
以下任務會被拒絕或警告：

1. **涉及敏感檔案**
   - `.env`, `credentials.json`, `secrets.*`
   - 會警告並要求確認

2. **大範圍刪除**
   - 刪除超過 3 個檔案
   - 會列出將刪除的檔案，要求確認

3. **邏輯複雜任務**
   - 沒有明確「舊→新」對照的任務
   - 建議改用主 Chat

### Max Turns 保護
- 預設 max_turns: 10
- 達到上限時自動停止
- 報告已完成的部分

---

## 最佳實踐

### 適合 Agent 的任務
- ✅ 批量替換 deprecated API
- ✅ 新增/移除 import
- ✅ 格式化程式碼
- ✅ 重命名變數/函數（有明確映射）

### 不適合 Agent 的任務
- ❌ 實作新功能邏輯
- ❌ 修復複雜 bug
- ❌ 重構程式架構
- ❌ 需要理解業務規則的任務

### 拆分策略
將複雜任務拆成多個 mechanical spec：

```
❌ 不好：實作用戶認證功能
✅ 好：
  1. 新增 AuthService 介面 (主 Chat)
  2. 新增必要的 import 到 ViewModel (Agent)
  3. 更新 DI 配置 (Agent)
```

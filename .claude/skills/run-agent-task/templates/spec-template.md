---
task_type: mechanical  # mechanical | logic | refactor
verification:
  - compile            # 編譯檢查
  - architecture       # Clean Architecture 檢查
max_turns: 10          # Agent 最大執行輪數
model: haiku           # haiku (快) | sonnet (平衡) | opus (強)
---

# [任務名稱]

## 目標

[用 1-2 句話描述要達成什麼]

## 需修改的檔案

| 檔案 | 行數（約） | 變更說明 |
|------|-----------|---------|
| `path/to/file1.kt` | 100 | 說明 |
| `path/to/file2.kt` | 50, 80 | 說明 |

## 具體變更

### 變更 1: [說明]

**位置**: `path/to/file.kt`

```kotlin
// 舊（要被取代的程式碼）
oldCode()

// 新（取代後的程式碼）
newCode()
```

**需加入 import**（如果有）：
```kotlin
import some.new.Import
```

**需移除 import**（如果有）：
```kotlin
// 移除
import old.deprecated.Import
```

---

### 變更 2: [說明]

[重複上述格式]

---

## 執行步驟

1. 依序修改上述檔案
2. [其他步驟]
3. 執行編譯：`./gradlew :composeApp:compileDebugKotlinAndroid`

## 驗證標準

編譯輸出應符合以下條件：
- [ ] 無編譯錯誤
- [ ] 無新增的 deprecation 警告
- [ ] [其他具體標準]

## 注意事項

- [任何需要特別注意的地方]
- [可能的邊界情況]

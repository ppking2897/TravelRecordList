# Agent 任務驗證規則

## 驗證層級

### Level 1: 編譯驗證（必選）

最基本的驗證，確保程式碼能編譯。

```bash
./gradlew :composeApp:compileDebugKotlinAndroid 2>&1
```

**通過標準**：
- 退出碼 = 0
- 無 `e:` 開頭的錯誤訊息

**失敗處理**：
1. 提取錯誤訊息
2. 識別問題檔案和行數
3. 判斷是否為 Agent 錯誤（如遺漏 import）還是 Spec 錯誤

---

### Level 2: 架構驗證（建議）

確保變更不破壞 Clean Architecture。

**檢查項目**：
1. domain 層不依賴 data/presentation
2. presentation 層不直接依賴 data 實作
3. ViewModel 透過 UseCase 存取資料

**執行方式**：
```
使用 /check-architecture skill
```

---

### Level 3: 靜態分析（可選）

使用 linter 檢查程式碼品質。

```bash
./gradlew ktlintCheck
./gradlew detekt
```

**通過標準**：
- 無新增違規
- 或違規數量不增加

---

### Level 4: 測試驗證（可選）

執行相關單元測試。

```bash
./gradlew :composeApp:testDebugUnitTest
```

**通過標準**：
- 所有測試通過
- 或新增測試覆蓋修改的程式碼

---

## 任務類型與驗證對照

| 任務類型 | Level 1 | Level 2 | Level 3 | Level 4 |
|---------|---------|---------|---------|---------|
| mechanical | ✅ 必要 | ⚪ 建議 | ⚪ 可選 | ⚪ 可選 |
| logic | ✅ 必要 | ✅ 必要 | ⚪ 建議 | ✅ 必要 |
| refactor | ✅ 必要 | ✅ 必要 | ✅ 必要 | ✅ 必要 |

---

## 常見失敗原因與修復

### 1. Unresolved reference

**原因**: Agent 使用了新的類別但沒加 import

**修復**:
```kotlin
// 在檔案頂部加入遺漏的 import
import missing.package.ClassName
```

### 2. Type mismatch

**原因**: Agent 替換程式碼時類型不匹配

**修復**:
- 檢查新舊 API 的參數類型
- 可能需要額外的型別轉換

### 3. 'xxx' is deprecated

**原因**: Agent 修復了一個 deprecation 但引入了另一個

**修復**:
- 檢查替換的 API 是否也已 deprecated
- 查詢最新的 API 使用方式

### 4. 架構違規

**原因**: Agent 在錯誤的層級加入了依賴

**修復**:
- 移動程式碼到正確的層級
- 或透過介面解耦

---

## 驗證輸出格式

### 成功輸出

```
╔═══════════════════════════════════════════════════════════╗
║                    ✅ 驗證通過                              ║
╠═══════════════════════════════════════════════════════════╣
║ 📋 任務: UI_DEPRECATION_FIX_SPEC.md                       ║
║ 📁 修改: 7 個檔案                                          ║
║ ⏱️  輪數: 3/10                                             ║
╠═══════════════════════════════════════════════════════════╣
║ ✔️ [Level 1] 編譯成功                                      ║
║ ✔️ [Level 2] 架構檢查通過                                  ║
╚═══════════════════════════════════════════════════════════╝
```

### 失敗輸出

```
╔═══════════════════════════════════════════════════════════╗
║                    ❌ 驗證失敗                              ║
╠═══════════════════════════════════════════════════════════╣
║ 📋 任務: FEATURE_SPEC.md                                  ║
║ 📁 修改: 3 個檔案                                          ║
║ ⏱️  輪數: 10/10 (達到上限)                                 ║
╠═══════════════════════════════════════════════════════════╣
║ ✔️ [Level 1] 編譯成功                                      ║
║ ❌ [Level 2] 架構違規                                      ║
║    └── presentation/MyViewModel.kt 直接依賴 Repository    ║
╠═══════════════════════════════════════════════════════════╣
║ 💡 建議修復:                                               ║
║    1. 建立 UseCase 封裝 Repository 呼叫                   ║
║    2. ViewModel 改為依賴 UseCase                          ║
╚═══════════════════════════════════════════════════════════╝
```

---

## 自訂驗證

在 Spec 的 frontmatter 中定義：

```yaml
verification:
  - compile
  - architecture
  - custom: |
      # 檢查特定檔案是否被修改
      git diff --name-only | grep -q "TargetFile.kt"
```

自訂驗證的退出碼：
- 0 = 通過
- 非 0 = 失敗

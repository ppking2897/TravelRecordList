# Phase 4: 操作流程優化規格

## 概述

本階段實作三個核心功能，提升行程編輯的操作效率。

## 功能規格

### A. 快速新增項目

**目標**：從 TimelineNavigator 節點間快速新增項目

**互動流程**：
```
TimelineNavigator 顯示：[Day1] ─ + ─ [Day2] ─ + ─ [Day3]
                              ↑
                        點擊「+」按鈕
                              ↓
                    導航到 AddEditItemScreen
                    （預設選擇該日期、順序在兩天之間）
```

**需修改檔案**：
| 檔案 | 變更 |
|------|------|
| `presentation/components/TimelineNavigator.kt` | 節點間加入 AddButton，onClick 回調 |
| `presentation/itinerary_detail/ItineraryDetailContract.kt` | Intent: `QuickAddItem(afterDayIndex: Int)` |
| `presentation/itinerary_detail/ItineraryDetailScreen.kt` | 處理 QuickAddItem，導航並傳遞預設參數 |

---

### B. 拖曳排序

**目標**：長按 ItemCard 拖曳調整順序

**互動流程**：
```
長按 ItemCard
    ↓
進入拖曳模式（卡片浮起、增加陰影）
    ↓
拖曳到目標位置（其他卡片讓位）
    ↓
放開 → 更新順序 → 儲存
```

**State 變更**：
```kotlin
data class ItineraryDetailState(
    // ... 現有欄位
    val isDragging: Boolean = false,
    val draggedItemId: String? = null,
    val dragTargetIndex: Int? = null
)
```

**Intent 變更**：
```kotlin
sealed class ItineraryDetailIntent {
    // ... 現有 Intent
    data class StartDrag(val itemId: String) : ItineraryDetailIntent()
    data class UpdateDragTarget(val targetIndex: Int) : ItineraryDetailIntent()
    data object EndDrag : ItineraryDetailIntent()
    data class ReorderItems(val fromIndex: Int, val toIndex: Int) : ItineraryDetailIntent()
}
```

**需修改/新建檔案**：
| 類型 | 檔案 | 變更 |
|------|------|------|
| 修改 | `presentation/itinerary_detail/ItineraryDetailContract.kt` | State/Intent 如上 |
| 修改 | `presentation/itinerary_detail/ItineraryDetailViewModel.kt` | handleIntent 處理拖曳，呼叫 UseCase |
| 修改 | `presentation/itinerary_detail/ItineraryDetailScreen.kt` | LazyColumn 加入 detectDragGestures |
| 修改 | `presentation/components/ItemCard.kt` | isDragging 視覺狀態（elevation、alpha） |
| 新建 | `domain/usecase/ReorderItineraryItemsUseCase.kt` | 執行重排邏輯 |
| 修改 | `domain/repository/ItineraryItemRepository.kt` | 新增 `suspend fun reorderItems(itemIds: List<String>)` |

---

### C. 批量操作

**目標**：多選項目後進行批量刪除或標記完成

**互動流程**：
```
長按任一 ItemCard 或點擊「選擇」按鈕
    ↓
進入選擇模式（顯示 Checkbox）
    ↓
點擊選擇多個項目
    ↓
底部顯示 BatchActionBar：
┌─────────────────────────────────────┐
│  已選 3 項  │ 🗑️ 刪除 │ ✓ 標記完成 │
└─────────────────────────────────────┘
    ↓
執行操作 → 確認對話框 → 執行 → 退出選擇模式
```

**State 變更**：
```kotlin
data class ItineraryDetailState(
    // ... 現有欄位
    val isSelectionMode: Boolean = false,
    val selectedItemIds: Set<String> = emptySet()
)
```

**Intent 變更**：
```kotlin
sealed class ItineraryDetailIntent {
    // ... 現有 Intent
    data object ToggleSelectionMode : ItineraryDetailIntent()
    data class ToggleItemSelection(val itemId: String) : ItineraryDetailIntent()
    data object SelectAll : ItineraryDetailIntent()
    data object ClearSelection : ItineraryDetailIntent()
    data object BatchDelete : ItineraryDetailIntent()
    data object BatchMarkComplete : ItineraryDetailIntent()
}
```

**需修改/新建檔案**：
| 類型 | 檔案 | 變更 |
|------|------|------|
| 修改 | `presentation/itinerary_detail/ItineraryDetailContract.kt` | State/Intent 如上 |
| 修改 | `presentation/itinerary_detail/ItineraryDetailViewModel.kt` | 處理批量邏輯 |
| 修改 | `presentation/itinerary_detail/ItineraryDetailScreen.kt` | 選擇模式切換、BatchActionBar |
| 修改 | `presentation/components/ItemCard.kt` | 加入 Checkbox、選中視覺狀態 |
| 新建 | `presentation/components/BatchActionBar.kt` | 批量操作底部工具列 |
| 新建 | `domain/usecase/BatchDeleteItemsUseCase.kt` | 批量刪除邏輯 |
| 新建 | `domain/usecase/BatchUpdateItemsUseCase.kt` | 批量更新邏輯 |
| 修改 | `domain/repository/ItineraryItemRepository.kt` | 批量方法 |

---

## 實作順序建議

```
1. 功能 A: 快速新增（最簡單，改動最少）
   ↓
2. 功能 C: 批量操作（State 結構可為拖曳鋪路）
   ↓
3. 功能 B: 拖曳排序（最複雜，需要手勢處理）
```

## 依賴關係

- 功能 B 和 C 都會修改 `ItemCard.kt`，需注意合併衝突
- UseCase 都依賴 `ItineraryItemRepository`

## 測試重點

- 拖曳邊界情況（第一個/最後一個項目）
- 批量操作確認對話框
- 選擇模式與正常模式切換
- 資料持久化驗證

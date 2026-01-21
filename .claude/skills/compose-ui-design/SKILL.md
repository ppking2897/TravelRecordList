---
name: compose-ui-design
description: |
  Compose Multiplatform / Jetpack Compose UI 設計指南。
  觸發時機：建立新畫面、設計 UI 組件、Material3 主題配置、
  佈局優化、響應式設計、動畫效果、Composable 最佳實踐。
  關鍵字：UI, 畫面, 組件, Material3, 主題, 顏色, 字體, 佈局,
  Card, Button, TextField, Scaffold, 動畫, 響應式
allowed-tools: Read, Write, Edit, Glob, Grep
---

# Compose UI Design Skill

針對 Compose Multiplatform 和 Jetpack Compose 的 UI 設計指南。

## 設計原則

### 1. Material3 設計語言
- **Dynamic Color**: 支援動態主題色彩
- **Expressive**: 豐富的視覺表達
- **Accessible**: 無障礙設計優先

### 2. Compose 最佳實踐
- **State Hoisting**: 狀態提升到父層
- **Single Source of Truth**: 單一資料來源
- **Unidirectional Data Flow**: 單向資料流

---

## 主題系統

### ColorScheme 使用規範

```kotlin
// 主要顏色 - 用於重要元素
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onPrimary

// 次要顏色 - 用於次要元素
MaterialTheme.colorScheme.secondary
MaterialTheme.colorScheme.onSecondary

// 表面顏色 - 用於背景和卡片
MaterialTheme.colorScheme.surface
MaterialTheme.colorScheme.surfaceVariant
MaterialTheme.colorScheme.onSurface

// 容器顏色 - 用於選中狀態
MaterialTheme.colorScheme.primaryContainer
MaterialTheme.colorScheme.onPrimaryContainer

// 錯誤顏色
MaterialTheme.colorScheme.error
MaterialTheme.colorScheme.onError
```

### Typography 層級

```kotlin
// 標題層級
MaterialTheme.typography.displayLarge   // 57sp - 大標題
MaterialTheme.typography.displayMedium  // 45sp
MaterialTheme.typography.displaySmall   // 36sp
MaterialTheme.typography.headlineLarge  // 32sp - 頁面標題
MaterialTheme.typography.headlineMedium // 28sp
MaterialTheme.typography.headlineSmall  // 24sp
MaterialTheme.typography.titleLarge     // 22sp - 區塊標題
MaterialTheme.typography.titleMedium    // 16sp
MaterialTheme.typography.titleSmall     // 14sp

// 內容層級
MaterialTheme.typography.bodyLarge      // 16sp - 主要內容
MaterialTheme.typography.bodyMedium     // 14sp - 次要內容
MaterialTheme.typography.bodySmall      // 12sp - 輔助文字

// 標籤層級
MaterialTheme.typography.labelLarge     // 14sp - 按鈕文字
MaterialTheme.typography.labelMedium    // 12sp
MaterialTheme.typography.labelSmall     // 11sp - 標籤
```

---

## 組件模式

### Slot Pattern (插槽模式)

```kotlin
@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    content: @Composable () -> Unit,
    footer: @Composable (() -> Unit)? = null
) {
    Card(modifier = modifier) {
        Column {
            header()
            content()
            footer?.invoke()
        }
    }
}
```

### Stateless Composable (無狀態組件)

```kotlin
// 推薦：狀態由外部控制
@Composable
fun CounterDisplay(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        IconButton(onClick = onDecrement) {
            Icon(Icons.Default.Remove, "減少")
        }
        Text(text = "$count")
        IconButton(onClick = onIncrement) {
            Icon(Icons.Default.Add, "增加")
        }
    }
}
```

---

## 佈局指南

### Scaffold 結構

```kotlin
Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("標題") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                }
            },
            actions = { /* 右側按鈕 */ }
        )
    },
    bottomBar = { /* 底部導航 */ },
    floatingActionButton = {
        FloatingActionButton(onClick = onFabClick) {
            Icon(Icons.Default.Add, "新增")
        }
    }
) { innerPadding ->
    // 內容區域，必須使用 innerPadding
    Column(modifier = Modifier.padding(innerPadding)) {
        // ...
    }
}
```

### Design Tokens 系統

本專案使用集中式 Design Tokens 系統，定義於 `ui/theme/Dimensions.kt`：

```kotlin
// 間距系統 - Spacing
Spacing.xs   // 4.dp  - 極小間距
Spacing.sm   // 8.dp  - 小間距
Spacing.md   // 12.dp - 中間距
Spacing.lg   // 16.dp - 標準間距
Spacing.xl   // 24.dp - 大間距
Spacing.xxl  // 32.dp - 超大間距
Spacing.xxxl // 48.dp - 極大間距

// 圖標尺寸 - IconSize
IconSize.xs  // 16.dp - 標籤內圖標
IconSize.sm  // 20.dp - 列表項目
IconSize.md  // 24.dp - 標準圖標
IconSize.lg  // 32.dp - 大圖標
IconSize.xl  // 48.dp - 空狀態
IconSize.xxl // 64.dp - 主圖標

// 圓角半徑 - CornerRadius
CornerRadius.xs   // 4.dp  - 標籤
CornerRadius.sm   // 8.dp  - 按鈕
CornerRadius.md   // 12.dp - 卡片
CornerRadius.lg   // 16.dp - 底部彈窗
CornerRadius.xl   // 24.dp - 圓形按鈕
CornerRadius.full // 1000.dp - 完全圓形

// 陰影高度 - Elevation
Elevation.none // 0.dp  - 平面
Elevation.xs   // 1.dp  - 微小
Elevation.sm   // 2.dp  - 卡片
Elevation.md   // 4.dp  - 懸浮卡片
Elevation.lg   // 8.dp  - 對話框
Elevation.xl   // 12.dp - 模態窗口

// 卡片樣式 - CardStyle
CardStyle.listCardElevation    // 列表卡片陰影
CardStyle.detailCardElevation  // 詳情卡片陰影
CardStyle.contentPadding       // 卡片內部間距
CardStyle.compactContentPadding // 緊湊卡片間距

// 列表樣式 - ListStyle
ListStyle.itemSpacing          // 列表項目間距
ListStyle.contentPadding       // 列表內容間距
ListStyle.gridSpacing          // 網格項目間距

// 元件尺寸 - ComponentSize
ComponentSize.buttonHeight     // 按鈕高度 48.dp
ComponentSize.thumbnailSize    // 縮圖尺寸 80.dp
ComponentSize.coverPhotoHeight // 封面照片高度 150.dp
ComponentSize.minTouchTarget   // 最小觸控區域 48.dp
```

### 使用 Design Tokens

```kotlin
// 推薦：使用 Design Tokens
Card(
    modifier = Modifier.padding(Spacing.lg),
    elevation = CardDefaults.cardElevation(CardStyle.listCardElevation)
) {
    Column(
        modifier = Modifier.padding(CardStyle.contentPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            modifier = Modifier.size(IconSize.md)
        )
    }
}

// 避免：硬編碼數值
Card(
    modifier = Modifier.padding(16.dp),  // ❌ 不推薦
    elevation = CardDefaults.cardElevation(2.dp)  // ❌ 不推薦
) { }
```

### 響應式佈局

```kotlin
// 根據螢幕寬度調整列數
val columns = when {
    windowWidth < 600.dp -> 1
    windowWidth < 840.dp -> 2
    else -> 3
}

LazyVerticalGrid(
    columns = GridCells.Fixed(columns),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(data) { item ->
        ItemCard(item)
    }
}
```

---

## 動畫效果

### AnimatedVisibility

```kotlin
AnimatedVisibility(
    visible = isVisible,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically()
) {
    // 內容
}
```

### animateContentSize

```kotlin
Card(
    modifier = Modifier.animateContentSize(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
) {
    // 會自動動畫調整大小
}
```

---

## 通用組件庫

本專案提供一組通用 UI 組件，位於 `ui/component/CommonComponents.kt`：

### 載入狀態

```kotlin
// 全螢幕載入遮罩
LoadingOverlay(
    isLoading = state.isLoading,
    content = { MainContent() }
)

// 內容區載入
LoadingContent(message = "載入中...")
```

### 錯誤狀態

```kotlin
// 全螢幕錯誤
ErrorContent(
    message = "發生錯誤",
    onRetry = { viewModel.retry() }
)

// 行內錯誤提示
ErrorBanner(
    message = "網路連線失敗",
    onDismiss = { hideError() }
)
```

### 區塊標題

```kotlin
// 區塊標題（帶操作按鈕）
SectionHeader(
    title = "相關行程",
    subtitle = "共 5 個",
    action = { TextButton(onClick = onViewAll) { Text("查看全部") } }
)

// 帶標籤的標題（年份分組）
SectionBadge(text = "2024 年")
```

### 空狀態

```kotlin
EmptyStateEnhanced(
    icon = Icons.Default.Search,
    title = "找不到結果",
    description = "試試其他關鍵字",
    actionLabel = "清除篩選",
    onAction = { clearFilter() }
)
```

### 表單欄位

```kotlin
FormField(
    label = "標題",
    isRequired = true,
    error = state.titleError,
    helperText = "最多 50 個字"
) {
    OutlinedTextField(
        value = state.title,
        onValueChange = { onTitleChange(it) }
    )
}
```

### 資訊標籤

```kotlin
// 位置標籤
LocationTag(
    location = "台北",
    icon = Icons.Default.Place
)

// 狀態標籤
StatusTag(
    text = "進行中",
    isActive = true
)
```

---

## 詳細參考

更多詳細內容請參考：
- [REFERENCE.md](./references/REFERENCE.md) - 完整組件範例
- [patterns.md](./references/patterns.md) - 設計模式詳解

## 使用範例

當建立新畫面時，遵循以下結構：

```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { /* TopAppBar */ }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(state.error)
            else -> FeatureContent(
                data = state.data,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
```

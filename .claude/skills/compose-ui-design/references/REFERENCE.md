# Compose UI Design Reference

完整的 Material3 組件範例與使用指南。

---

## 表單組件

### TextField 變體

```kotlin
// 基本 OutlinedTextField
@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// 多行文字輸入
@Composable
fun MultilineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 3,
    maxLines: Int = 5
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        maxLines = maxLines
    )
}

// 搜尋欄位
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜尋..."
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "搜尋")
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "清除")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}
```

### 日期時間選擇器

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toEpochDays()?.times(86400000L)
    )

    OutlinedTextField(
        value = selectedDate?.toString() ?: "",
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "選擇日期")
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                        onDateSelected(date)
                    }
                    showDatePicker = false
                }) {
                    Text("確定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime?.hour ?: 9,
        initialMinute = selectedTime?.minute ?: 0
    )

    OutlinedTextField(
        value = selectedTime?.let {
            "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
        } ?: "",
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showTimePicker = true }) {
                Icon(Icons.Default.Schedule, contentDescription = "選擇時間")
            }
        }
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(LocalTime(timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) {
                    Text("確定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}
```

---

## 列表組件

### LazyColumn 最佳實踐

```kotlin
@Composable
fun <T> PaginatedList(
    items: List<T>,
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    itemContent: @Composable (T) -> Unit
) {
    val listState = rememberLazyListState()

    // 偵測滾動到底部
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = items,
            key = { it.hashCode() }  // 建議使用唯一 ID
        ) { item ->
            itemContent(item)
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// Sticky Header 範例
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupedList(
    groups: Map<String, List<Item>>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        groups.forEach { (header, items) ->
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = header,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            items(items) { item ->
                ItemRow(item)
            }
        }
    }
}
```

### SwipeToDismiss

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableListItem(
    item: Item,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false  // 不要真正 dismiss，只觸發編輯
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                }
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
            ) {
                icon?.let {
                    Icon(it, contentDescription = null, tint = Color.White)
                }
            }
        }
    ) {
        content()
    }
}
```

---

## 導航組件

### Bottom Navigation

```kotlin
@Composable
fun AppBottomNavigation(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == tab) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) }
            )
        }
    }
}

enum class Tab(
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
) {
    HOME(Icons.Outlined.Home, Icons.Filled.Home, "首頁"),
    LIST(Icons.Outlined.List, Icons.Filled.List, "行程"),
    HISTORY(Icons.Outlined.History, Icons.Filled.History, "歷史"),
    SETTINGS(Icons.Outlined.Settings, Icons.Filled.Settings, "設定")
}
```

### Navigation Drawer

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    selectedItem: String,
    onItemClick: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "旅遊紀錄",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text("首頁") },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            selected = selectedItem == "home",
            onClick = { onItemClick("home"); onClose() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("行程列表") },
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            selected = selectedItem == "list",
            onClick = { onItemClick("list"); onClose() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("旅行歷史") },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            selected = selectedItem == "history",
            onClick = { onItemClick("history"); onClose() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
```

---

## 回饋組件

### Snackbar

```kotlin
@Composable
fun SnackbarHost(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { data ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = data.visuals.actionLabel?.let { actionLabel ->
                    {
                        TextButton(onClick = { data.performAction() }) {
                            Text(actionLabel)
                        }
                    }
                },
                dismissAction = if (data.visuals.withDismissAction) {
                    {
                        IconButton(onClick = { data.dismiss() }) {
                            Icon(Icons.Default.Close, contentDescription = "關閉")
                        }
                    }
                } else null
            ) {
                Text(data.visuals.message)
            }
        }
    )
}

// 使用方式
@Composable
fun ScreenWithSnackbar() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Button(
            onClick = {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "項目已刪除",
                        actionLabel = "復原",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> { /* 復原操作 */ }
                        SnackbarResult.Dismissed -> { /* 已關閉 */ }
                    }
                }
            },
            modifier = Modifier.padding(innerPadding)
        ) {
            Text("顯示 Snackbar")
        }
    }
}
```

### Dialog 變體

```kotlin
// 確認對話框
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "確定",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = if (isDestructive) {
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.textButtonColors()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

// 輸入對話框
@Composable
fun InputDialog(
    title: String,
    initialValue: String = "",
    placeholder: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
```

---

## Loading 狀態

### 各種 Loading 指示器

```kotlin
// 全螢幕載入
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// 內容載入佔位符
@Composable
fun ContentPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) {
            ShimmerPlaceholder(
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )
        }
    }
}

@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation.value - 200, 0f),
        end = Offset(translateAnimation.value, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(brush)
    )
}

// Pull-to-refresh
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshList(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = modifier
    ) {
        content()
    }
}
```

---

## 空狀態

### Empty State 組件

```kotlin
@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        action?.let {
            Spacer(modifier = Modifier.height(24.dp))
            it()
        }
    }
}

// 使用範例
@Composable
fun EmptyItineraryState(
    onCreateClick: () -> Unit
) {
    EmptyStateView(
        icon = Icons.Default.FlightTakeoff,
        title = "尚無行程",
        description = "開始規劃您的第一個旅行行程吧！",
        action = {
            Button(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("建立行程")
            }
        }
    )
}
```

---

## 錯誤狀態

### Error State 組件

```kotlin
@Composable
fun ErrorStateView(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "發生錯誤",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        onRetry?.let {
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(onClick = it) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重試")
            }
        }
    }
}
```

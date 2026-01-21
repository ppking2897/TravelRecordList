# Compose Design Patterns

常用的 Compose 設計模式與架構實踐。

---

## 1. State Hoisting Pattern

將狀態提升到父組件，使子組件成為無狀態的純 UI。

### 基本模式

```kotlin
// ❌ 不推薦：狀態內嵌於組件
@Composable
fun BadCounter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}

// ✅ 推薦：狀態提升
@Composable
fun GoodCounter(
    count: Int,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onCountChange(count + 1) },
        modifier = modifier
    ) {
        Text("Count: $count")
    }
}

// 父組件管理狀態
@Composable
fun CounterScreen() {
    var count by remember { mutableStateOf(0) }

    GoodCounter(
        count = count,
        onCountChange = { count = it }
    )
}
```

### 複雜表單狀態

```kotlin
// 表單狀態資料類別
data class ItineraryFormState(
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    val isValid: Boolean
        get() = title.isNotBlank() && startDate != null
}

// 無狀態表單組件
@Composable
fun ItineraryForm(
    state: ItineraryFormState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate?) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text("行程名稱") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            label = { Text("說明") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // 日期選擇...

        Button(
            onClick = onSubmit,
            enabled = state.isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("儲存")
        }
    }
}
```

---

## 2. Slot API Pattern

使用 Composable lambda 作為參數，提供靈活的內容組合。

### 基本 Slot

```kotlin
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                title()
                subtitle?.invoke()
            }

            trailing?.invoke()
        }
    }
}

// 使用方式
@Composable
fun LocationCard(location: Location) {
    InfoCard(
        icon = {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleMedium
            )
        },
        subtitle = {
            Text(
                text = location.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailing = {
            IconButton(onClick = { /* 導航 */ }) {
                Icon(Icons.Default.Navigation, contentDescription = "導航")
            }
        }
    )
}
```

### Scaffold Slot Pattern

```kotlin
@Composable
fun ScreenScaffold(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    onBackClick?.let {
                        IconButton(onClick = it) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                },
                actions = actions
            )
        },
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        content = content
    )
}
```

---

## 3. Modifier Chain Pattern

建立可重用的 Modifier 鏈。

```kotlin
// 定義常用 Modifier 組合
fun Modifier.cardStyle() = this
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 8.dp)

fun Modifier.clickableCard(onClick: () -> Unit) = this
    .clip(RoundedCornerShape(12.dp))
    .clickable(onClick = onClick)

fun Modifier.listItemPadding() = this
    .padding(horizontal = 16.dp, vertical = 12.dp)

// 條件式 Modifier
fun Modifier.conditionalBorder(
    show: Boolean,
    color: Color = Color.Red
) = if (show) {
    this.border(2.dp, color, RoundedCornerShape(8.dp))
} else {
    this
}

// 使用範例
@Composable
fun StyledCard(
    onClick: () -> Unit,
    isSelected: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .cardStyle()
            .clickableCard(onClick)
            .conditionalBorder(isSelected, MaterialTheme.colorScheme.primary)
    ) {
        content()
    }
}
```

---

## 4. Remember Patterns

正確使用 remember 和 key。

### remember with keys

```kotlin
// 當 key 變化時重新計算
@Composable
fun FormattedDate(date: LocalDate) {
    val formatted = remember(date) {
        // 只在 date 變化時重新格式化
        date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
    }
    Text(text = formatted)
}

// 多個 key
@Composable
fun FilteredList(
    items: List<Item>,
    filter: String
) {
    val filteredItems = remember(items, filter) {
        if (filter.isEmpty()) items
        else items.filter { it.name.contains(filter) }
    }

    LazyColumn {
        items(filteredItems) { item ->
            ItemRow(item)
        }
    }
}
```

### rememberSaveable

```kotlin
// 跨配置變更保存狀態
@Composable
fun SearchBar() {
    var query by rememberSaveable { mutableStateOf("") }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    // query 和 isExpanded 在螢幕旋轉後仍會保留
}

// 自訂 Saver
data class SearchState(
    val query: String,
    val filters: List<String>
)

val SearchStateSaver = Saver<SearchState, Map<String, Any>>(
    save = { mapOf("query" to it.query, "filters" to it.filters) },
    restore = { SearchState(
        query = it["query"] as String,
        filters = it["filters"] as List<String>
    )}
)

@Composable
fun SearchScreen() {
    var searchState by rememberSaveable(stateSaver = SearchStateSaver) {
        mutableStateOf(SearchState("", emptyList()))
    }
}
```

---

## 5. derivedStateOf Pattern

當狀態計算成本較高或需要減少重組時使用。

```kotlin
@Composable
fun FilteredListScreen(
    allItems: List<Item>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    // 使用 derivedStateOf 避免不必要的重組
    val filteredItems by remember(allItems) {
        derivedStateOf {
            allItems
                .filter { item ->
                    searchQuery.isEmpty() ||
                    item.name.contains(searchQuery, ignoreCase = true)
                }
                .filter { item ->
                    selectedCategory == null ||
                    item.category == selectedCategory
                }
        }
    }

    // 滾動狀態監聽
    val listState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 5
        }
    }

    Box {
        LazyColumn(state = listState) {
            items(filteredItems) { item ->
                ItemCard(item)
            }
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            FloatingActionButton(
                onClick = { /* scroll to top */ }
            ) {
                Icon(Icons.Default.KeyboardArrowUp, "回到頂部")
            }
        }
    }
}
```

---

## 6. Side Effect Patterns

### LaunchedEffect

```kotlin
@Composable
fun DetailScreen(
    itemId: String,
    viewModel: DetailViewModel
) {
    // 當 itemId 變化時載入資料
    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    // 只執行一次
    LaunchedEffect(Unit) {
        viewModel.trackScreenView()
    }
}
```

### DisposableEffect

```kotlin
@Composable
fun MapScreen(
    mapController: MapController
) {
    DisposableEffect(mapController) {
        mapController.initialize()

        onDispose {
            mapController.cleanup()
        }
    }
}
```

### SideEffect

```kotlin
@Composable
fun AnalyticsScreen(
    screenName: String,
    analytics: Analytics
) {
    // 每次重組都會執行
    SideEffect {
        analytics.setCurrentScreen(screenName)
    }
}
```

---

## 7. MVI with Compose

本專案使用的 MVI 架構模式。

```kotlin
// Contract 定義
object ItineraryListContract {
    data class State(
        val items: List<Itinerary> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed class Intent {
        data object LoadItems : Intent()
        data class DeleteItem(val id: String) : Intent()
        data class ToggleComplete(val id: String) : Intent()
    }

    sealed class Event {
        data class ShowError(val message: String) : Event()
        data object NavigateToAdd : Event()
    }
}

// ViewModel
class ItineraryListViewModel(
    private val loadItinerariesUseCase: LoadItinerariesUseCase,
    private val deleteItineraryUseCase: DeleteItineraryUseCase
) : BaseViewModel<State, Intent, Event>(State()) {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadItems -> loadItems()
            is Intent.DeleteItem -> deleteItem(intent.id)
            is Intent.ToggleComplete -> toggleComplete(intent.id)
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            loadItinerariesUseCase()
                .onSuccess { items ->
                    updateState { copy(items = items, isLoading = false) }
                }
                .onFailure { error ->
                    updateState { copy(isLoading = false, error = error.message) }
                }
        }
    }
}

// Screen 使用
@Composable
fun ItineraryListScreen(
    viewModel: ItineraryListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 處理一次性事件
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is Event.ShowError -> { /* 顯示 Snackbar */ }
                is Event.NavigateToAdd -> { /* 導航 */ }
            }
        }
    }

    ItineraryListContent(
        state = state,
        onLoadItems = { viewModel.sendIntent(Intent.LoadItems) },
        onDeleteItem = { viewModel.sendIntent(Intent.DeleteItem(it)) }
    )
}
```

---

## 8. Composition Local Pattern

跨組件共享資料，避免 prop drilling。

```kotlin
// 定義 CompositionLocal
val LocalAppTheme = compositionLocalOf<AppTheme> {
    error("No AppTheme provided")
}

val LocalDateFormatter = staticCompositionLocalOf {
    DateTimeFormatter.ofPattern("yyyy-MM-dd")
}

// 提供值
@Composable
fun App() {
    val appTheme = remember { AppTheme() }

    CompositionLocalProvider(
        LocalAppTheme provides appTheme
    ) {
        AppNavigation()
    }
}

// 使用值
@Composable
fun ThemedCard() {
    val theme = LocalAppTheme.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = theme.cardBackground
        )
    ) {
        // ...
    }
}
```

---

## 9. Preview Patterns

### 多種預覽配置

```kotlin
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ItemCardPreview() {
    MaterialTheme {
        ItemCard(
            item = previewItem,
            onEdit = {},
            onDelete = {}
        )
    }
}

// 不同裝置預覽
@Preview(name = "Phone", device = Devices.PHONE)
@Preview(name = "Tablet", device = Devices.TABLET)
@Composable
fun ResponsiveLayoutPreview() {
    ResponsiveContent()
}

// 預覽資料
private val previewItem = ItineraryItem(
    id = "preview-1",
    itineraryId = "itinerary-1",
    activity = "參觀博物館",
    location = Location(
        name = "國立故宮博物院",
        latitude = 25.1024,
        longitude = 121.5485
    ),
    notes = "記得帶相機",
    createdAt = Clock.System.now(),
    modifiedAt = Clock.System.now()
)
```

### PreviewParameterProvider

```kotlin
class ItemPreviewParameterProvider : PreviewParameterProvider<ItineraryItem> {
    override val values = sequenceOf(
        ItineraryItem(
            id = "1",
            activity = "短活動",
            notes = "",
            // ...
        ),
        ItineraryItem(
            id = "2",
            activity = "這是一個很長很長的活動名稱需要測試截斷",
            notes = "這是一段很長的備註內容...",
            // ...
        ),
        ItineraryItem(
            id = "3",
            activity = "已完成活動",
            isCompleted = true,
            // ...
        )
    )
}

@Preview
@Composable
fun ItemCardVariantsPreview(
    @PreviewParameter(ItemPreviewParameterProvider::class) item: ItineraryItem
) {
    MaterialTheme {
        ItemCard(item = item, onEdit = {}, onDelete = {})
    }
}
```

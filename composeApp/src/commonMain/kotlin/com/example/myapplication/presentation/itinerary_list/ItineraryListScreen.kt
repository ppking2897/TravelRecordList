package com.example.myapplication.presentation.itinerary_list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import com.example.myapplication.presentation.theme.CardStyle
import com.example.myapplication.presentation.theme.CornerRadius
import com.example.myapplication.presentation.theme.IconSize
import com.example.myapplication.presentation.theme.ListStyle
import com.example.myapplication.presentation.theme.Spacing
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.myapplication.domain.entity.Itinerary
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.presentation.components.DeleteConfirmDialog
import com.example.myapplication.presentation.itinerary_list.ItineraryListEvent
import com.example.myapplication.presentation.itinerary_list.ItineraryListIntent
import com.example.myapplication.presentation.itinerary_list.ItineraryListViewModel
import com.example.myapplication.presentation.theme.TravelRecordTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.absoluteValue

/**
 * 行程列表畫面（MVI 架構）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryListScreen(
    onItineraryClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: ItineraryListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var itineraryToDelete by remember { mutableStateOf<Itinerary?>(null) }

    // 每次畫面恢復顯示時（ON_RESUME），重新載入資料
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.handleIntent(ItineraryListIntent.LoadItineraries)
    }

    // 收集 Event
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ItineraryListEvent.NavigateToDetail -> onItineraryClick(event.id)
                is ItineraryListEvent.NavigateToEdit -> onEditClick(event.id)
                is ItineraryListEvent.NavigateToAdd -> onAddClick()
                is ItineraryListEvent.ShowDeleteConfirm -> {
                    itineraryToDelete = event.itinerary
                    showDeleteDialog = true
                }

                is ItineraryListEvent.ShowError -> {
                    // 錯誤訊息透過 Event 顯示
                }
            }
        }
    }

    ItineraryListScreenContent(
        itineraries = state.itineraries,
        searchQuery = state.searchQuery,
        isLoading = state.isLoading,
        error = state.error,
        onSearchChange = { viewModel.handleIntent(ItineraryListIntent.Search(it)) },
        onItineraryClick = onItineraryClick,
        onEditClick = onEditClick,
        onDeleteClick = { itinerary ->
            itineraryToDelete = itinerary
            showDeleteDialog = true
        },
        onAddClick = onAddClick,
        onHistoryClick = onHistoryClick,
        onRefresh = { viewModel.handleIntent(ItineraryListIntent.Refresh) }
    )

    // 刪除確認 Dialog
    if (showDeleteDialog && itineraryToDelete != null) {
        DeleteConfirmDialog(
            title = "確認刪除",
            message = "確定要刪除「${itineraryToDelete!!.title}」嗎？此操作無法復原。",
            onConfirm = {
                viewModel.handleIntent(ItineraryListIntent.DeleteItinerary(itineraryToDelete!!.id))
                showDeleteDialog = false
                itineraryToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                itineraryToDelete = null
            }
        )
    }
}


// Preview
@Preview
@kotlin.time.ExperimentalTime
@Composable
private fun ItineraryListScreenPreview() {
    TravelRecordTheme {
        Surface {
            ItineraryListScreenContent(
                itineraries = listOf(
                    Itinerary(
                        id = "1",
                        title = "東京之旅",
                        description = "探索日本首都的美食與文化",
                        startDate = kotlinx.datetime.LocalDate(2024, 3, 15),
                        endDate = kotlinx.datetime.LocalDate(2024, 3, 20),
                        items = emptyList(),
                        createdAt = kotlin.time.Clock.System.now(),
                        modifiedAt = kotlin.time.Clock.System.now()
                    ),
                    Itinerary(
                        id = "2",
                        title = "台南美食之旅",
                        description = "品嚐台南在地小吃",
                        startDate = kotlinx.datetime.LocalDate(2024, 4, 1),
                        endDate = kotlinx.datetime.LocalDate(2024, 4, 3),
                        items = emptyList(),
                        createdAt = kotlin.time.Clock.System.now(),
                        modifiedAt = kotlin.time.Clock.System.now()
                    )
                ),
                searchQuery = "",
                isLoading = false,
                error = null,
                onSearchChange = {},
                onItineraryClick = {},
                onEditClick = {},
                onDeleteClick = {},
                onAddClick = {},
                onHistoryClick = {},
                onRefresh = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryListScreenContent(
    itineraries: List<Itinerary>,
    searchQuery: String,
    isLoading: Boolean,
    error: String?,
    onSearchChange: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (Itinerary) -> Unit,
    onAddClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRefresh: () -> Unit
) {
    var actionMenuItinerary by remember { mutableStateOf<Itinerary?>(null) }
    var showActionMenu by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "我的行程",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                actions = {
                    // 歷史記錄按鈕
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = "旅遊歷史",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // 橘色圓形新增按鈕
                    Surface(
                        onClick = onAddClick,
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .padding(end = Spacing.md)
                            .size(IconSize.lg)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "新增行程",
                                tint = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 即時搜尋列（始終顯示）
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    placeholder = { Text("搜尋行程...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true
                )

                // 錯誤訊息
                error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(Spacing.lg)
                    )
                }

                // 列表內容
                if (itineraries.isEmpty() && !isLoading) {
                    // 空狀態
                    EmptyStateGrid(onAddClick = onAddClick)
                } else {
                    // 網格佈局
                    ItineraryGridWithGroups(
                        itineraries = itineraries,
                        onItineraryClick = onItineraryClick,
                        onItineraryLongPress = { itinerary ->
                            actionMenuItinerary = itinerary
                            showActionMenu = true
                        }
                    )
                }
            }
        }
    }

    // 操作選單
    actionMenuItinerary?.let { itinerary ->
        ItineraryActionMenu(
            expanded = showActionMenu,
            onDismiss = {
                showActionMenu = false
                actionMenuItinerary = null
            },
            onEdit = { onEditClick(itinerary.id) },
            onDelete = { onDeleteClick(itinerary) }
        )
    }
}

// ============================================================================
// 新增的網格佈局組件
// ============================================================================

/**
 * 網格佈局帶年份分組
 */
@Composable
private fun ItineraryGridWithGroups(
    itineraries: List<Itinerary>,
    onItineraryClick: (String) -> Unit,
    onItineraryLongPress: (Itinerary) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedItineraries = remember(itineraries) { groupByCreationYear(itineraries) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(ListStyle.contentPadding),
        horizontalArrangement = Arrangement.spacedBy(ListStyle.gridSpacing),
        verticalArrangement = Arrangement.spacedBy(ListStyle.gridSpacing)
    ) {
        groupedItineraries.forEach { (year, yearItineraries) ->
            // 年份標題（跨兩列）
            item(key = "year_$year", span = { GridItemSpan(2) }) {
                YearGroupHeader(year = year)
            }

            // 行程卡片
            items(items = yearItineraries, key = { it.id }) { itinerary ->
                ItineraryGridCard(
                    itinerary = itinerary,
                    onClick = { onItineraryClick(itinerary.id) },
                    onLongPress = { onItineraryLongPress(itinerary) }
                )
            }
        }
    }
}

/**
 * 網格卡片組件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItineraryGridCard(
    itinerary: Itinerary,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        elevation = CardDefaults.cardElevation(defaultElevation = CardStyle.listCardElevation),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // 封面圖片區域（含位置標籤）
            Box(modifier = Modifier.fillMaxWidth().height(Spacing.xxxl * 2.5f)) {
                GradientPlaceholder(gradientId = itinerary.id.hashCode())

                extractMainLocation(itinerary.items)?.let { location ->
                    LocationBadge(
                        location = location,
                        modifier = Modifier.align(Alignment.TopStart).padding(Spacing.sm)
                    )
                }
            }

            // 內容區域
            Column(
                modifier = Modifier.fillMaxWidth().padding(CardStyle.compactContentPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = itinerary.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                formatDateRange(itinerary.startDate, itinerary.endDate)?.let { dateText ->
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "建立於 ${formatCreatedDate(itinerary.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 漸層色佔位符
 */
@Composable
private fun GradientPlaceholder(
    gradientId: Int,
    modifier: Modifier = Modifier
) {
    val gradients = listOf(
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.surfaceVariant
    )

    val (startColor, endColor) = gradients[gradientId.absoluteValue % gradients.size]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = listOf(startColor, endColor))
            )
    )
}

/**
 * 位置標籤組件
 */
@Composable
private fun LocationBadge(
    location: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(IconSize.xs),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = location,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 年份分組標題
 */
@Composable
private fun YearGroupHeader(
    year: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.padding(start = Spacing.xs)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$year 年",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * 操作選單
 */
@Composable
private fun ItineraryActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("編輯") },
            onClick = { onDismiss(); onEdit() },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("刪除") },
            onClick = { onDismiss(); onDelete() },
            leadingIcon = {
                Icon(
                    Icons.Default.Delete, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}

/**
 * 空狀態組件（網格版本）
 */
@Composable
private fun EmptyStateGrid(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(IconSize.xxl),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = "還沒有任何行程",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "開始規劃您的第一個旅遊行程",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("建立行程")
        }
    }
}

// ============================================================================
// 輔助函數
// ============================================================================

/**
 * 從行程項目中提取最常出現的城市名稱
 */
private fun extractMainLocation(items: List<ItineraryItem>): String? {
    if (items.isEmpty()) return null
    return items.groupingBy { it.location.name }.eachCount().maxByOrNull { it.value }?.key
}

/**
 * 格式化日期範圍顯示
 */
private fun formatDateRange(startDate: LocalDate?, endDate: LocalDate?): String? {
    return when {
        startDate != null && endDate != null ->
            "${startDate.toDisplayString()} - ${endDate.toDisplayString()}"

        startDate != null -> "開始：${startDate.toDisplayString()}"
        endDate != null -> "結束：${endDate.toDisplayString()}"
        else -> null
    }
}

/**
 * 格式化建立日期
 */
@OptIn(kotlin.time.ExperimentalTime::class)
private fun formatCreatedDate(instant: Instant): String {
    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return localDate.toDisplayString()
}

/**
 * LocalDate 轉換為顯示格式 (yyyy/MM/dd)
 */
private fun LocalDate.toDisplayString(): String {
    return "$year/${monthNumber.toString().padStart(2, '0')}/${
        dayOfMonth.toString().padStart(2, '0')
    }"
}

/**
 * 按建立年份對行程進行分組（降序）
 */
@OptIn(kotlin.time.ExperimentalTime::class)
private fun groupByCreationYear(itineraries: List<Itinerary>): Map<Int, List<Itinerary>> {
    return itineraries
        .groupBy { it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).year }
        .toList()
        .sortedByDescending { it.first }
        .toMap()
}

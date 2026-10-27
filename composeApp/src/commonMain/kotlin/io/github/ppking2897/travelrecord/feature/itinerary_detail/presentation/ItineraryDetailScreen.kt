package io.github.ppking2897.travelrecord.feature.itinerary_detail.presentation

import io.github.ppking2897.travelrecord.util.toShortStringWithWeekday
import io.github.ppking2897.travelrecord.core.presentation.theme.CardStyle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.core.presentation.theme.AppGradients
import io.github.ppking2897.travelrecord.core.presentation.theme.ListStyle
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.ItemsByDate
import io.github.ppking2897.travelrecord.core.presentation.components.AnimatedBatchActionBar
import io.github.ppking2897.travelrecord.core.presentation.components.DeleteConfirmDialog
import io.github.ppking2897.travelrecord.core.presentation.components.EmptyState
import io.github.ppking2897.travelrecord.core.presentation.components.ItemCard
import io.github.ppking2897.travelrecord.core.presentation.components.LoadingContent
import io.github.ppking2897.travelrecord.core.presentation.components.TimelineNavigator
import io.github.ppking2897.travelrecord.core.presentation.theme.TravelRecordTheme
import io.github.ppking2897.travelrecord.LocalSnackbarHostState
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import androidx.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryDetailScreen(
    viewModel: ItineraryDetailViewModel,
    itineraryId: String,
    onNavigateBack: () -> Unit,
    onAddItemClick: () -> Unit,
    onQuickAddItemClick: (LocalDate) -> Unit,
    onEditItemClick: (String) -> Unit,
    onEditItineraryClick: () -> Unit,
    onRouteClick: (String) -> Unit,
    onMapClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val itinerary = state.itinerary
    val groupedItems = state.groupedItems
    val selectedDate = state.selectedDate
    val dateRange = state.dateRange
    val isLoading = state.isLoading
    val error = state.error
    val isSelectionMode = state.isSelectionMode
    val selectedItemIds = state.selectedItemIds

    val snackbarHostState = LocalSnackbarHostState.current
    var showDeleteItemDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ItineraryItem?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteItineraryDialog by remember { mutableStateOf(false) }
    var selectedItemIdForPhoto by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            val imageData = byteArrays.firstOrNull()
            val itemId = selectedItemIdForPhoto

            if (imageData != null && itemId != null) {
                viewModel.processIntent(ItineraryDetailIntent.AddPhoto(itemId, imageData))
            }
            selectedItemIdForPhoto = null
        }
    )

    LaunchedEffect(itineraryId) {
        viewModel.processIntent(ItineraryDetailIntent.LoadItinerary(itineraryId))
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ItineraryDetailEvent.NavigateBack -> onNavigateBack()
                is ItineraryDetailEvent.NavigateToQuickAddItem -> onQuickAddItemClick(event.defaultDate)
                is ItineraryDetailEvent.NavigateToRoute -> onRouteClick(event.itineraryId)
                is ItineraryDetailEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ItineraryDetailEvent.ShowBatchOperationResult -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }

    ItineraryDetailScreenContent(
        itinerary = itinerary,
        groupedItems = groupedItems.map { ItemsByDate(it.date, it.items) },
        allGroupedItems = state.allGroupedItems.map { ItemsByDate(it.date, it.items) },
        selectedDate = selectedDate,
        dateRange = dateRange,
        expandedItemIds = state.expandedItemIds,
        isLoading = isLoading,
        error = error,
        // 批量操作相關
        isSelectionMode = isSelectionMode,
        selectedItemIds = selectedItemIds,
        onNavigateBack = onNavigateBack,
        onAddItemClick = onAddItemClick,
        onQuickAdd = { afterDayIndex ->
            viewModel.processIntent(ItineraryDetailIntent.QuickAddItem(afterDayIndex))
        },
        onEditItemClick = onEditItemClick,
        onEditItineraryClick = onEditItineraryClick,
        onDeleteItineraryClick = { showDeleteItineraryDialog = true },
        onGenerateRouteClick = { viewModel.processIntent(ItineraryDetailIntent.GenerateRoute) },
        onSelectDate = { viewModel.processIntent(ItineraryDetailIntent.SelectDate(it)) },
        onToggleItemCompletion = { viewModel.processIntent(ItineraryDetailIntent.ToggleItemCompletion(it)) },
        onToggleItemExpansion = { viewModel.processIntent(ItineraryDetailIntent.ToggleItemExpansion(it)) },
        onDeleteItem = { itemId ->
            itemToDelete = groupedItems.flatMap { it.items }.find { it.id == itemId }
            showDeleteItemDialog = true
        },
        onAddPhoto = { itemId ->
            selectedItemIdForPhoto = itemId
            singleImagePicker.launch()
        },
        onSetCoverPhoto = { itemId, photoId ->
            viewModel.processIntent(ItineraryDetailIntent.SetCoverPhoto(itemId, photoId))
        },
        onDeletePhoto = { photoId ->
            viewModel.processIntent(ItineraryDetailIntent.DeletePhoto(photoId))
        },
        // 批量操作 callbacks
        onToggleSelectionMode = { viewModel.processIntent(ItineraryDetailIntent.ToggleSelectionMode) },
        onToggleItemSelection = { viewModel.processIntent(ItineraryDetailIntent.ToggleItemSelection(it)) },
        onBatchDelete = { showBatchDeleteDialog = true },
        onBatchMarkComplete = { viewModel.processIntent(ItineraryDetailIntent.BatchMarkComplete) },
        onCancelSelection = { viewModel.processIntent(ItineraryDetailIntent.ClearSelection) },
        onMapClick = onMapClick
    )

    // 刪除項目確認 Dialog
    if (showDeleteItemDialog && itemToDelete != null) {
        DeleteConfirmDialog(
            title = "確認刪除",
            message = "確定要刪除「${itemToDelete!!.activity}」嗎？此操作無法復原。",
            onConfirm = {
                viewModel.processIntent(ItineraryDetailIntent.DeleteItem(itemToDelete!!.id))
                showDeleteItemDialog = false
                itemToDelete = null
            },
            onDismiss = {
                showDeleteItemDialog = false
                itemToDelete = null
            }
        )
    }

    // 刪除行程確認 Dialog
    if (showDeleteItineraryDialog && itinerary != null) {
        DeleteConfirmDialog(
            title = "刪除行程",
            message = "確定要刪除「${itinerary.title}」嗎？這個動作無法復原。",
            onConfirm = {
                viewModel.processIntent(ItineraryDetailIntent.DeleteItinerary(itineraryId))
                showDeleteItineraryDialog = false
            },
            onDismiss = { showDeleteItineraryDialog = false }
        )
    }

    // 批量刪除確認 Dialog
    if (showBatchDeleteDialog) {
        DeleteConfirmDialog(
            title = "確認批量刪除",
            message = "確定要刪除所選的 ${selectedItemIds.size} 個項目嗎？此操作無法復原。",
            onConfirm = {
                viewModel.processIntent(ItineraryDetailIntent.BatchDelete)
                showBatchDeleteDialog = false
            },
            onDismiss = {
                showBatchDeleteDialog = false
            }
        )
    }
}

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryDetailScreenContent(
    itinerary: Itinerary?,
    groupedItems: List<ItemsByDate>,
    allGroupedItems: List<ItemsByDate> = groupedItems,
    selectedDate: LocalDate?,
    dateRange: ClosedRange<LocalDate>?,
    expandedItemIds: Set<String>,
    isLoading: Boolean,
    error: String?,
    // 批量操作相關
    isSelectionMode: Boolean = false,
    selectedItemIds: Set<String> = emptySet(),
    onNavigateBack: () -> Unit,
    onAddItemClick: () -> Unit,
    onQuickAdd: (afterDayIndex: Int) -> Unit,
    onEditItemClick: (String) -> Unit,
    onEditItineraryClick: () -> Unit,
    onDeleteItineraryClick: () -> Unit,
    onGenerateRouteClick: () -> Unit,
    onSelectDate: (LocalDate?) -> Unit,
    onToggleItemCompletion: (String) -> Unit,
    onToggleItemExpansion: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onAddPhoto: (String) -> Unit,
    onSetCoverPhoto: (String, String) -> Unit,
    onDeletePhoto: (String) -> Unit,
    // 批量操作 callbacks
    onToggleSelectionMode: () -> Unit = {},
    onToggleItemSelection: (String) -> Unit = {},
    onBatchDelete: () -> Unit = {},
    onBatchMarkComplete: () -> Unit = {},
    onCancelSelection: () -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    // 選日期後 LazyColumn 自動捲動到對應區段
    LaunchedEffect(selectedDate, groupedItems) {
        if (selectedDate == null || groupedItems.isEmpty()) return@LaunchedEffect
        var targetIndex = if (error != null) 1 else 0
        // 選了某一天時列表不放日期標題，每個區段只有項目
        for (group in groupedItems) {
            if (group.date == selectedDate) break
            targetIndex += group.items.size
        }
        lazyListState.animateScrollToItem(targetIndex)
    }

    // FAB 滑動隱藏狀態
    var isFabVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 向下滑動 (available.y < 0) -> 隱藏 FAB
                // 向上滑動 (available.y > 0) -> 顯示 FAB
                if (available.y < -10) {
                    isFabVisible = false
                } else if (available.y > 10) {
                    isFabVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val backgroundGradient = AppGradients.warmBackgroundGradient

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("已選擇 ${selectedItemIds.size} 項")
                    } else {
                        Text(itinerary?.title ?: "行程詳情")
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            onCancelSelection()
                            onToggleSelectionMode()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "取消選擇")
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    if (!isSelectionMode) {
                        // 地圖按鈕
                        IconButton(onClick = onMapClick) {
                            Icon(Icons.Default.Map, contentDescription = "地圖檢視")
                        }
                        // 更多選項（含選擇模式、生成路線、編輯、刪除）
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "更多選項")
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("選擇模式") },
                                    onClick = {
                                        showMoreMenu = false
                                        onToggleSelectionMode()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Checklist, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("生成路線") },
                                    onClick = {
                                        showMoreMenu = false
                                        onGenerateRouteClick()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("編輯行程") },
                                    onClick = {
                                        showMoreMenu = false
                                        onEditItineraryClick()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("刪除行程") },
                                    onClick = {
                                        showMoreMenu = false
                                        onDeleteItineraryClick()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // 選擇模式下隱藏 FAB，滑動時也會隱藏
            AnimatedVisibility(
                visible = !isSelectionMode && isFabVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onAddItemClick,
                    // 手帳風：正圓、墨色框、不加陰影
                    modifier = Modifier.border(CardStyle.borderWidth, MaterialTheme.colorScheme.outline, CircleShape),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "新增項目")
                }
            }
        },
        bottomBar = {
            // 選擇模式下顯示批量操作工具列
            AnimatedBatchActionBar(
                visible = isSelectionMode,
                selectedCount = selectedItemIds.size,
                onDelete = onBatchDelete,
                onMarkComplete = onBatchMarkComplete,
                onCancel = {
                    onCancelSelection()
                    onToggleSelectionMode()
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
                    .padding(paddingValues)
            ) {
                LoadingContent()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
                    .padding(paddingValues)
            ) {
                // 時間軸導覽（用 allGroupedItems 顯示每日總計數，不受日期 filter 影響）
                dateRange?.let { range ->
                    TimelineNavigator(
                        dateRange = range,
                        groupedItems = allGroupedItems,
                        selectedDate = selectedDate,
                        onDateSelected = onSelectDate,
                        onQuickAdd = onQuickAdd
                    )
                }

                selectedDate?.let { date ->
                    DayPlanningSummary(
                        date = date,
                        dayNumber = dateRange?.let { dayIndexFor(it, date) + 1 },
                        items = groupedItems.firstOrNull { it.date == date }?.items.orEmpty(),
                        onAddFirstItem = {
                            val range = dateRange
                            if (range != null) {
                                onQuickAdd(dayIndexFor(range, date))
                            } else {
                                onAddItemClick()
                            }
                        }
                    )
                }

                // 內容
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .nestedScroll(nestedScrollConnection),
                    contentPadding = PaddingValues(ListStyle.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(ListStyle.contentPadding)
                ) {
                    // 錯誤訊息
                    error?.let {
                        item {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = Spacing.sm)
                            )
                        }
                    }

                    // 項目列表
                    if (groupedItems.isEmpty()) {
                        item {
                            EmptyDatePlanningState(
                                selectedDate = selectedDate,
                                onAddFirstItem = {
                                    val date = selectedDate
                                    val range = dateRange
                                    if (date != null && range != null) {
                                        onQuickAdd(dayIndexFor(range, date))
                                    } else {
                                        onAddItemClick()
                                    }
                                }
                            )
                        }
                    } else {
                        groupedItems.forEach { group ->
                            // 選了某一天時，上方的摘要卡已經寫了日期，不再重複
                            if (selectedDate == null) {
                                item {
                                    Text(
                                        text = group.date.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(top = Spacing.sm)
                                    )
                                }
                            }

                            itemsIndexed(
                                items = group.items,
                                key = { _, item -> item.id }
                            ) { _, item ->
                                // 時間軸虛線以一天為單位：每天最後一個項目不再往下畫
                                val isLastItem = item == group.items.last()

                                Box(modifier = Modifier.animateItem()) {
                                    ItemCard(
                                        item = item,
                                        isExpanded = expandedItemIds.contains(item.id),
                                        isLastItem = isLastItem,
                                        onToggleComplete = onToggleItemCompletion,
                                        onDelete = onDeleteItem,
                                        onEdit = onEditItemClick,
                                        onExpandToggle = if (isSelectionMode) null else onToggleItemExpansion,
                                        onAddPhoto = onAddPhoto,
                                        onSetCoverPhoto = onSetCoverPhoto,
                                        onDeletePhoto = onDeletePhoto,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = selectedItemIds.contains(item.id),
                                        onSelectionToggle = { onToggleItemSelection(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayPlanningSummary(
    date: LocalDate,
    dayNumber: Int?,
    items: List<ItineraryItem>,
    onAddFirstItem: () -> Unit
) {
    val completedCount = items.count { it.isCompleted }
    val photoCount = items.sumOf { it.photos.size }

    // 手帳風：有框的圓角卡片，小字「Day N」、大字日期
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ListStyle.contentPadding, end = ListStyle.contentPadding, top = Spacing.xs),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(CardStyle.cornerRadius),
        border = BorderStroke(CardStyle.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                dayNumber?.let {
                    Text(
                        text = "Day $it",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = date.toShortStringWithWeekday(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${items.size} 個地點 · $completedCount 已完成 · $photoCount 張照片",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (items.isEmpty()) {
                TextButton(onClick = onAddFirstItem) {
                    Text("新增這天")
                }
            }
        }
    }
}

@Composable
private fun EmptyDatePlanningState(
    selectedDate: LocalDate?,
    onAddFirstItem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            text = if (selectedDate != null) "這一天還沒有安排" else "還沒有行程項目",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (selectedDate != null) {
                "先新增第一個地點，開始規劃這一天。"
            } else {
                "先選擇日期或點擊新增，建立第一個地點。"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onAddFirstItem) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(if (selectedDate != null) "新增這天的第一個地點" else "新增第一個地點")
        }
    }
}

private fun dayIndexFor(
    range: ClosedRange<LocalDate>,
    selectedDate: LocalDate
): Int {
    var current = range.start
    var index = 0
    while (current < selectedDate && current < range.endInclusive) {
        current = current.plus(1, DateTimeUnit.DAY)
        index += 1
    }
    return index
}

// Preview
@Preview
@ExperimentalTime
@Composable
private fun ItineraryDetailScreenPreview() {
    TravelRecordTheme {
        Surface {
            ItineraryDetailScreenContent(
                itinerary = Itinerary(
                    id = "1",
                    title = "東京之旅",
                    description = "探索日本首都",
                    startDate = LocalDate(2024, 3, 15),
                    endDate = LocalDate(2024, 3, 17),
                    items = emptyList(),
                    createdAt = Clock.System.now(),
                    modifiedAt = Clock.System.now()
                ),
                groupedItems = listOf(
                    ItemsByDate(
                        date = LocalDate(2024, 3, 15),
                        items = listOf(
                            ItineraryItem(
                                id = "1",
                                itineraryId = "1",
                                date = LocalDate(2024, 3, 15),
                                arrivalTime = LocalTime(9, 0),
                                departureTime = null,
                                location = Location("淺草寺", null, null, "東京都台東區"),
                                activity = "參觀淺草寺",
                                notes = "早上參觀",
                                isCompleted = false,
                                completedAt = null,
                                createdAt = Clock.System.now(),
                                modifiedAt = Clock.System.now()
                            )
                        )
                    )
                ),
                selectedDate = null,
                dateRange = LocalDate(2024, 3, 15)..LocalDate(2024, 3, 17),
                expandedItemIds = emptySet(),
                isLoading = false,
                error = null,
                onNavigateBack = {},
                onAddItemClick = {},
                onQuickAdd = {},
                onEditItemClick = {},
                onEditItineraryClick = {},
                onDeleteItineraryClick = {},
                onGenerateRouteClick = {},
                onSelectDate = {},
                onToggleItemCompletion = {},
                onToggleItemExpansion = {},
                onDeleteItem = {},
                onAddPhoto = {},
                onSetCoverPhoto = { _, _ -> },
                onDeletePhoto = {}
            )
        }
    }
}

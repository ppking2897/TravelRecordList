package com.example.myapplication.feature.travel_history.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import com.example.myapplication.LocalSnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.feature.itinerary_item.domain.model.ItineraryItem
import com.example.myapplication.feature.itinerary_item.domain.model.Photo
import com.example.myapplication.core.presentation.components.HashtagRow
import com.example.myapplication.core.presentation.components.LocalImage
import com.example.myapplication.core.presentation.components.PhotoPreviewDialog
import com.example.myapplication.core.presentation.theme.CardStyle
import com.example.myapplication.core.presentation.theme.CornerRadius
import com.example.myapplication.core.presentation.theme.IconSize
import com.example.myapplication.core.presentation.theme.ListStyle
import com.example.myapplication.core.presentation.theme.Spacing
import com.example.myapplication.feature.itinerary_item.domain.model.Location
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

/**
 * 旅遊歷史畫面（MVI 架構）
 */
@ExperimentalTime
@Composable
fun TravelHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: TravelHistoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = LocalSnackbarHostState.current

    // 收集 Event
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is TravelHistoryEvent.NavigateBack -> onNavigateBack()
                is TravelHistoryEvent.ShowFilterDialog -> {
                    // Dialog 在 UI 中處理
                }
                is TravelHistoryEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    TravelHistoryScreenContent(
        historyByLocation = state.historyByLocation,
        dateFilter = state.dateFilter,
        isLoading = state.isLoading,
        error = state.error,
        expandedItemIds = state.expandedItemIds,
        onNavigateBack = onNavigateBack,
        onFilterByDateRange = { dateRange ->
            viewModel.handleIntent(TravelHistoryIntent.FilterByDateRange(dateRange))
        },
        onClearFilter = { viewModel.handleIntent(TravelHistoryIntent.ClearFilter) },
        onToggleItemExpansion = { itemId ->
            viewModel.handleIntent(TravelHistoryIntent.ToggleItemExpansion(itemId))
        }
    )
}

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TravelHistoryScreenContent(
    historyByLocation: Map<String, List<ItineraryItem>>,
    dateFilter: ClosedRange<LocalDate>?,
    isLoading: Boolean,
    error: String?,
    expandedItemIds: Set<String>,
    onNavigateBack: () -> Unit,
    onFilterByDateRange: (ClosedRange<LocalDate>) -> Unit,
    onClearFilter: () -> Unit,
    onToggleItemExpansion: (String) -> Unit
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var previewPhoto by remember { mutableStateOf<Photo?>(null) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("旅遊歷史") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            if (dateFilter != null) Icons.Default.DateRange else Icons.Default.DateRange,
                            contentDescription = "日期過濾",
                            tint = if (dateFilter != null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (dateFilter != null) {
                        IconButton(onClick = onClearFilter) {
                            Icon(Icons.Default.Clear, contentDescription = "清除過濾")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 日期過濾顯示
            dateFilter?.let { filter ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${filter.start} - ${filter.endInclusive}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 統計摘要
            if (historyByLocation.isNotEmpty()) {
                val totalLocations = historyByLocation.size
                val totalItems = historyByLocation.values.sumOf { it.size }
                val completedItems = historyByLocation.values.sumOf { items ->
                    items.count { it.isCompleted }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(value = "$totalLocations", label = "個地點")
                        StatItem(value = "$totalItems", label = "次旅行")
                        StatItem(value = "$completedItems", label = "已完成")
                    }
                }
            }

            // 錯誤訊息
            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(Spacing.lg)
                )
            }

            // 載入中
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (historyByLocation.isEmpty()) {
                // 空狀態
                EmptyHistoryState()
            } else {
                // 歷史列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(ListStyle.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(ListStyle.contentPadding)
                ) {
                    historyByLocation.forEach { (locationName, items) ->
                        item {
                            LocationHistoryCard(
                                locationName = locationName,
                                items = items,
                                expandedItemIds = expandedItemIds,
                                onToggleItemExpansion = onToggleItemExpansion,
                                onPhotoClick = { photo -> previewPhoto = photo }
                            )
                        }
                    }
                }
            }
        }
    }

    // 照片預覽 Dialog
    previewPhoto?.let { photo ->
        PhotoPreviewDialog(
            photo = photo,
            isCover = false,
            onDismiss = { previewPhoto = null },
            onSetCoverPhoto = null,
            onDeletePhoto = null
        )
    }

    // 日期篩選對話框
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("選擇日期範圍") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // 開始日期
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("開始日期")
                        TextButton(onClick = { showStartDatePicker = true }) {
                            Text(selectedStartDate?.toString() ?: "選擇")
                        }
                    }
                    // 結束日期
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("結束日期")
                        TextButton(onClick = { showEndDatePicker = true }) {
                            Text(selectedEndDate?.toString() ?: "選擇")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = selectedStartDate
                        val end = selectedEndDate
                        if (start != null && end != null && start <= end) {
                            onFilterByDateRange(start..end)
                            showFilterDialog = false
                            selectedStartDate = null
                            selectedEndDate = null
                        }
                    },
                    enabled = selectedStartDate != null && selectedEndDate != null &&
                            (selectedStartDate!! <= selectedEndDate!!)
                ) {
                    Text("套用")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFilterDialog = false
                    selectedStartDate = null
                    selectedEndDate = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    // 開始日期選擇器
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        selectedStartDate = instant.toLocalDateTime(TimeZone.UTC).date
                    }
                    showStartDatePicker = false
                }) {
                    Text("確定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 結束日期選擇器
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        selectedEndDate = instant.toLocalDateTime(TimeZone.UTC).date
                    }
                    showEndDatePicker = false
                }) {
                    Text("確定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Place,
            contentDescription = null,
            modifier = Modifier.size(IconSize.xxl),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = "還沒有旅遊歷史",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "完成行程項目後會顯示在這裡",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocationHistoryCard(
    locationName: String,
    items: List<ItineraryItem>,
    expandedItemIds: Set<String>,
    onToggleItemExpansion: (String) -> Unit,
    onPhotoClick: (Photo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = CardStyle.listCardElevation)
    ) {
        Column(
            modifier = Modifier.padding(CardStyle.contentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // 地點標題
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    items.firstOrNull()?.location?.address?.let { address ->
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // 訪問次數
            Text(
                text = "訪問 ${items.size} 次",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // 項目列表
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items.forEach { item ->
                    HistoryItemRow(
                        item = item,
                        isExpanded = item.id in expandedItemIds,
                        onToggleExpansion = { onToggleItemExpansion(item.id) },
                        onPhotoClick = onPhotoClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemRow(
    item: ItineraryItem,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit,
    onPhotoClick: (Photo) -> Unit
) {
    val hasExtraContent = item.notes.isNotBlank() || item.photos.isNotEmpty() || item.hashtags.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable(enabled = hasExtraContent) { onToggleExpansion() }
            .padding(Spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.Top
        ) {
            // 日期
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${item.date.monthNumber}月",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = item.date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = item.date.year.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // 活動資訊
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                // 活動名稱
                Text(
                    text = item.activity,
                    style = MaterialTheme.typography.bodyMedium
                )

                // 完整時間段
                val timeText = buildTimeRangeText(item.arrivalTime, item.departureTime)
                if (timeText.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 備註預覽（未展開時最多顯示 2 行）
                if (item.notes.isNotBlank() && !isExpanded) {
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 照片縮圖列（未展開時顯示）
                if (item.photos.isNotEmpty() && !isExpanded) {
                    PhotoThumbnailRow(
                        photos = item.photos,
                        onPhotoClick = onPhotoClick
                    )
                }

                // Hashtag 標籤（未展開時顯示）
                if (item.hashtags.isNotEmpty() && !isExpanded) {
                    HashtagRow(
                        hashtags = item.hashtags,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }

            // 右側圖示
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                // 完成標記
                if (item.isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "已完成",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(IconSize.sm)
                    )
                }

                // 展開/收合圖示
                if (hasExtraContent) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "收合" else "展開",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(IconSize.sm)
                    )
                }
            }
        }

        // 展開的詳細內容
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                HorizontalDivider()

                // 完整備註
                if (item.notes.isNotBlank()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Notes,
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.xs),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "備註",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 照片列表
                if (item.photos.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Photo,
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.xs),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "照片 (${item.photos.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        PhotoThumbnailRow(
                            photos = item.photos,
                            onPhotoClick = onPhotoClick,
                            showAll = true
                        )
                    }
                }

                // Hashtag 標籤
                if (item.hashtags.isNotEmpty()) {
                    HashtagRow(
                        hashtags = item.hashtags
                    )
                }
            }
        }
    }
}

/**
 * 照片縮圖列
 */
@Composable
private fun PhotoThumbnailRow(
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit,
    showAll: Boolean = false
) {
    val displayPhotos = if (showAll) photos else photos.take(3)
    val remainingCount = photos.size - displayPhotos.size

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        items(displayPhotos, key = { it.id }) { photo ->
            Box(
                modifier = Modifier
                    .size(IconSize.xl)
                    .clip(RoundedCornerShape(CornerRadius.xs))
                    .clickable { onPhotoClick(photo) }
            ) {
                LocalImage(
                    photo = photo,
                    contentDescription = "照片",
                    modifier = Modifier.fillMaxSize(),
                    useThumbnail = true
                )
            }
        }

        // 顯示剩餘數量 badge
        if (remainingCount > 0 && !showAll) {
            item {
                Box(
                    modifier = Modifier
                        .size(IconSize.xl)
                        .clip(RoundedCornerShape(CornerRadius.xs))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$remainingCount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 構建時間範圍文字
 */
private fun buildTimeRangeText(
    arrivalTime: kotlinx.datetime.LocalTime?,
    departureTime: kotlinx.datetime.LocalTime?
): String {
    return when {
        arrivalTime != null && departureTime != null -> {
            "${formatTime(arrivalTime)} - ${formatTime(departureTime)}"
        }
        arrivalTime != null -> formatTime(arrivalTime)
        departureTime != null -> "離開 ${formatTime(departureTime)}"
        else -> ""
    }
}

/**
 * 格式化時間
 */
private fun formatTime(time: kotlinx.datetime.LocalTime): String {
    val hour = time.hour.toString().padStart(2, '0')
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}


// Preview
@Preview
@ExperimentalTime
@Composable
private fun TravelHistoryScreenPreview() {
    MaterialTheme {
        Surface {
            TravelHistoryScreenContent(
                historyByLocation = mapOf(
                    "淺草寺" to listOf(
                        ItineraryItem(
                            id = "1",
                            itineraryId = "1",
                            date = kotlinx.datetime.LocalDate(2024, 3, 15),
                            arrivalTime = kotlinx.datetime.LocalTime(9, 0),
                            departureTime = kotlinx.datetime.LocalTime(11, 30),
                            location = Location("淺草寺", null, null, "東京都台東區"),
                            activity = "參觀雷門",
                            notes = "很棒的體驗，推薦必訪！人潮很多，建議早點到。",
                            isCompleted = true,
                            completedAt = kotlin.time.Clock.System.now(),
                            createdAt = kotlin.time.Clock.System.now(),
                            modifiedAt = kotlin.time.Clock.System.now()
                        ),
                        ItineraryItem(
                            id = "2",
                            itineraryId = "2",
                            date = kotlinx.datetime.LocalDate(2024, 4, 10),
                            arrivalTime = kotlinx.datetime.LocalTime(14, 0),
                            departureTime = kotlinx.datetime.LocalTime(16, 0),
                            location = Location("淺草寺", null, null, "東京都台東區"),
                            activity = "購買紀念品",
                            notes = "",
                            isCompleted = true,
                            completedAt = kotlin.time.Clock.System.now(),
                            createdAt = kotlin.time.Clock.System.now(),
                            modifiedAt = kotlin.time.Clock.System.now()
                        )
                    )
                ),
                dateFilter = null,
                isLoading = false,
                error = null,
                expandedItemIds = emptySet(),
                onNavigateBack = {},
                onFilterByDateRange = {},
                onClearFilter = {},
                onToggleItemExpansion = {}
            )
        }
    }
}

@Preview
@ExperimentalTime
@Composable
private fun TravelHistoryScreenPreview_Loading() {
    MaterialTheme {
        Surface {
            TravelHistoryScreenContent(
                historyByLocation = emptyMap(),
                dateFilter = null,
                isLoading = true,
                error = null,
                expandedItemIds = emptySet(),
                onNavigateBack = {},
                onFilterByDateRange = {},
                onClearFilter = {},
                onToggleItemExpansion = {}
            )
        }
    }
}

@Preview
@ExperimentalTime
@Composable
private fun TravelHistoryScreenPreview_Empty() {
    MaterialTheme {
        Surface {
            TravelHistoryScreenContent(
                historyByLocation = emptyMap(),
                dateFilter = null,
                isLoading = false,
                error = null,
                expandedItemIds = emptySet(),
                onNavigateBack = {},
                onFilterByDateRange = {},
                onClearFilter = {},
                onToggleItemExpansion = {}
            )
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

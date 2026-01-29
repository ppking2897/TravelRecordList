package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.ui.theme.CardStyle
import com.example.myapplication.ui.theme.IconSize
import com.example.myapplication.ui.theme.ListStyle
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.domain.entity.Location
import com.example.myapplication.ui.mvi.history.TravelHistoryEvent
import com.example.myapplication.ui.mvi.history.TravelHistoryIntent
import com.example.myapplication.ui.mvi.history.TravelHistoryViewModel
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
    
    // 收集 Event
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is TravelHistoryEvent.NavigateBack -> onNavigateBack()
                is TravelHistoryEvent.ShowFilterDialog -> {
                    // Dialog 在 UI 中處理
                }
                is TravelHistoryEvent.ShowError -> {
                    // 錯誤已經在 state 中
                }
            }
        }
    }
    
    TravelHistoryScreenContent(
        historyByLocation = state.historyByLocation,
        dateFilter = state.dateFilter,
        isLoading = state.isLoading,
        error = state.error,
        onNavigateBack = onNavigateBack,
        onClearFilter = { viewModel.handleIntent(TravelHistoryIntent.ClearFilter) }
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
    onNavigateBack: () -> Unit,
    onClearFilter: () -> Unit
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("旅遊歷史") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                                items = items
                            )
                        }
                    }
                }
            }
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
    items: List<ItineraryItem>
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
            
            Divider()
            
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
                    HistoryItemRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun HistoryItemRow(item: ItineraryItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
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
                    text = item.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "${item.date.month}月",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        // 活動資訊
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs / 2)
        ) {
            Text(
                text = item.activity,
                style = MaterialTheme.typography.bodyMedium
            )
            item.primaryTime()?.let { time ->
                Text(
                    text = time.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 完成標記
        if (item.isCompleted) {
            Icon(
                Icons.Default.Check,
                contentDescription = "已完成",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(IconSize.sm)
            )
        }
    }
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
                            departureTime = null,
                            location = Location("淺草寺", null, null, "東京都台東區"),
                            activity = "參觀雷門",
                            notes = "",
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
                            departureTime = null,
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
                onNavigateBack = {},
                onClearFilter = {}
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
                onNavigateBack = {},
                onClearFilter = {}
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
                onNavigateBack = {},
                onClearFilter = {}
            )
        }
    }
}

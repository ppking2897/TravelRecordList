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
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.component.DateTabsRow
import com.example.myapplication.ui.component.DeleteConfirmDialog
import com.example.myapplication.ui.component.EmptyState
import com.example.myapplication.ui.component.ItemCard
import com.example.myapplication.ui.viewmodel.ItineraryDetailViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryDetailScreen(
    viewModel: ItineraryDetailViewModel,
    itineraryId: String,
    onNavigateBack: () -> Unit,
    onAddItemClick: () -> Unit,
    onEditItemClick: (String) -> Unit,
    onEditItineraryClick: () -> Unit,
    onDeleteItineraryClick: () -> Unit,
    onGenerateRouteClick: () -> Unit
) {
    val itinerary by viewModel.itinerary.collectAsState()
    val groupedItems by viewModel.groupedItems.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showDeleteItemDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<com.example.myapplication.data.model.ItineraryItem?>(null) }
    
    LaunchedEffect(itineraryId) {
        viewModel.loadItinerary(itineraryId)
    }
    
    ItineraryDetailScreenContent(
        itinerary = itinerary,
        groupedItems = groupedItems,
        selectedDate = selectedDate,
        dateRange = dateRange,
        isLoading = isLoading,
        error = error,
        onNavigateBack = onNavigateBack,
        onAddItemClick = onAddItemClick,
        onEditItemClick = onEditItemClick,
        onEditItineraryClick = onEditItineraryClick,
        onDeleteItineraryClick = onDeleteItineraryClick,
        onGenerateRouteClick = onGenerateRouteClick,
        onSelectDate = { viewModel.selectDate(it) },
        onToggleItemCompletion = { viewModel.toggleItemCompletion(it) },
        onDeleteItem = { itemId ->
            itemToDelete = groupedItems.flatMap { it.items }.find { it.id == itemId }
            showDeleteItemDialog = true
        }
    )
    
    // 刪除項目確認 Dialog
    if (showDeleteItemDialog && itemToDelete != null) {
        DeleteConfirmDialog(
            title = "確認刪除",
            message = "確定要刪除「${itemToDelete!!.activity}」嗎？此操作無法復原。",
            onConfirm = {
                viewModel.deleteItem(itemToDelete!!.id)
                showDeleteItemDialog = false
                itemToDelete = null
            },
            onDismiss = {
                showDeleteItemDialog = false
                itemToDelete = null
            }
        )
    }
}

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryDetailScreenContent(
    itinerary: com.example.myapplication.data.model.Itinerary?,
    groupedItems: List<com.example.myapplication.domain.usecase.ItemsByDate>,
    selectedDate: kotlinx.datetime.LocalDate?,
    dateRange: ClosedRange<kotlinx.datetime.LocalDate>?,
    isLoading: Boolean,
    error: String?,
    onNavigateBack: () -> Unit,
    onAddItemClick: () -> Unit,
    onEditItemClick: (String) -> Unit,
    onEditItineraryClick: () -> Unit,
    onDeleteItineraryClick: () -> Unit,
    onGenerateRouteClick: () -> Unit,
    onSelectDate: (kotlinx.datetime.LocalDate?) -> Unit,
    onToggleItemCompletion: (String) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(itinerary?.title ?: "行程詳情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onGenerateRouteClick) {
                        Icon(Icons.Default.Share, contentDescription = "生成路線")
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多選項")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
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
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItemClick) {
                Icon(Icons.Default.Add, contentDescription = "新增項目")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                // 日期 Tabs
                dateRange?.let { range ->
                    DateTabsRow(
                        dateRange = range,
                        selectedDate = selectedDate,
                        onDateSelected = onSelectDate
                    )
                    HorizontalDivider()
                }
                
                // 內容
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 錯誤訊息
                    error?.let {
                        item {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    // 項目列表
                    if (groupedItems.isEmpty()) {
                        item {
                            EmptyState(
                                message = if (selectedDate != null) 
                                    "此日期沒有項目\n點擊右下角按鈕新增" 
                                else 
                                    "還沒有項目\n點擊右下角按鈕新增第一個項目"
                            )
                        }
                    } else {
                        groupedItems.forEach { group ->
                            item {
                                Text(
                                    text = group.date.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                            items(group.items, key = { it.id }) { item ->
                                ItemCard(
                                    item = item,
                                    onToggleComplete = onToggleItemCompletion,
                                    onDelete = onDeleteItem,
                                    onEdit = onEditItemClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Preview
@Preview
@ExperimentalTime
@Composable
private fun ItineraryDetailScreenPreview() {
    MaterialTheme {
        Surface {
            ItineraryDetailScreenContent(
                itinerary = com.example.myapplication.data.model.Itinerary(
                    id = "1",
                    title = "東京之旅",
                    description = "探索日本首都",
                    startDate = kotlinx.datetime.LocalDate(2024, 3, 15),
                    endDate = kotlinx.datetime.LocalDate(2024, 3, 17),
                    items = emptyList(),
                    createdAt = kotlin.time.Clock.System.now(),
                    modifiedAt = kotlin.time.Clock.System.now()
                ),
                groupedItems = listOf(
                    com.example.myapplication.domain.usecase.ItemsByDate(
                        date = kotlinx.datetime.LocalDate(2024, 3, 15),
                        items = listOf(
                            com.example.myapplication.data.model.ItineraryItem(
                                id = "1",
                                itineraryId = "1",
                                date = kotlinx.datetime.LocalDate(2024, 3, 15),
                                arrivalTime = kotlinx.datetime.LocalTime(9, 0),
                                departureTime = null,
                                location = com.example.myapplication.data.model.Location("淺草寺", null, null, "東京都台東區"),
                                activity = "參觀淺草寺",
                                notes = "早上參觀",
                                isCompleted = false,
                                completedAt = null,
                                photoReferences = emptyList(),
                                createdAt = kotlin.time.Clock.System.now(),
                                modifiedAt = kotlin.time.Clock.System.now()
                            )
                        )
                    )
                ),
                selectedDate = null,
                dateRange = kotlinx.datetime.LocalDate(2024, 3, 15)..kotlinx.datetime.LocalDate(2024, 3, 17),
                isLoading = false,
                error = null,
                onNavigateBack = {},
                onAddItemClick = {},
                onEditItemClick = {},
                onEditItineraryClick = {},
                onDeleteItineraryClick = {},
                onGenerateRouteClick = {},
                onSelectDate = {},
                onToggleItemCompletion = {},
                onDeleteItem = {}
            )
        }
    }
}

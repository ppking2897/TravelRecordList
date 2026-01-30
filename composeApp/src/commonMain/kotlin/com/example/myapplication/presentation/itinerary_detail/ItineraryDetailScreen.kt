package com.example.myapplication.presentation.itinerary_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapplication.domain.entity.Itinerary
import com.example.myapplication.presentation.theme.ListStyle
import com.example.myapplication.presentation.theme.Spacing
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.entity.Location
import com.example.myapplication.domain.usecase.ItemsByDate
import com.example.myapplication.presentation.components.DeleteConfirmDialog
import com.example.myapplication.presentation.components.EmptyState
import com.example.myapplication.presentation.components.ItemCard
import com.example.myapplication.presentation.components.TimelineNavigator
import com.example.myapplication.presentation.itinerary_detail.ItineraryDetailEvent
import com.example.myapplication.presentation.itinerary_detail.ItineraryDetailIntent
import com.example.myapplication.presentation.itinerary_detail.ItineraryDetailViewModel
import com.example.myapplication.presentation.theme.TravelRecordTheme
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.ui.tooling.preview.Preview
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
    onEditItemClick: (String) -> Unit,
    onEditItineraryClick: () -> Unit,
    onDeleteItineraryClick: () -> Unit,
    onGenerateRouteClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val itinerary = state.itinerary
    val groupedItems = state.groupedItems
    val selectedDate = state.selectedDate
    val dateRange = state.dateRange
    val isLoading = state.isLoading
    val error = state.error
    
    var showDeleteItemDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ItineraryItem?>(null) }
    var selectedItemIdForPhoto by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            val imageData = byteArrays.firstOrNull()
            val itemId = selectedItemIdForPhoto
            
            if (imageData != null && itemId != null) {
                viewModel.handleIntent(ItineraryDetailIntent.AddPhoto(itemId, imageData))
            }
            selectedItemIdForPhoto = null
        }
    )
    
    LaunchedEffect(itineraryId) {
        viewModel.handleIntent(ItineraryDetailIntent.LoadItinerary(itineraryId))
    }
    
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ItineraryDetailEvent.NavigateBack -> onNavigateBack()
                is ItineraryDetailEvent.ShowError -> { /* Handle Error */ }
                is ItineraryDetailEvent.NavigateToRoute -> { /* Handle Route Navigation if needed here or in UI */ }
                else -> {}
            }
        }
    }
    
    ItineraryDetailScreenContent(
        itinerary = itinerary,
        groupedItems = groupedItems.map { ItemsByDate(it.date, it.items) },
        selectedDate = selectedDate,
        dateRange = dateRange,
        expandedItemIds = state.expandedItemIds,
        isLoading = isLoading,
        error = error,
        onNavigateBack = onNavigateBack,
        onAddItemClick = onAddItemClick,
        onEditItemClick = onEditItemClick,
        onEditItineraryClick = onEditItineraryClick,
        onDeleteItineraryClick = { viewModel.handleIntent(ItineraryDetailIntent.DeleteItinerary(itineraryId)) },
        onGenerateRouteClick = { viewModel.handleIntent(ItineraryDetailIntent.GenerateRoute) },
        onSelectDate = { viewModel.handleIntent(ItineraryDetailIntent.SelectDate(it)) },
        onToggleItemCompletion = { viewModel.handleIntent(ItineraryDetailIntent.ToggleItemCompletion(it)) },
        onToggleItemExpansion = { viewModel.handleIntent(ItineraryDetailIntent.ToggleItemExpansion(it)) },
        onDeleteItem = { itemId ->
            itemToDelete = groupedItems.flatMap { it.items }.find { it.id == itemId }
            showDeleteItemDialog = true
        },
        onAddPhoto = { itemId ->
            selectedItemIdForPhoto = itemId
            singleImagePicker.launch()
        },
        onSetCoverPhoto = { itemId, photoId ->
            viewModel.handleIntent(ItineraryDetailIntent.SetCoverPhoto(itemId, photoId))
        },
        onDeletePhoto = { photoId ->
            viewModel.handleIntent(ItineraryDetailIntent.DeletePhoto(photoId))
        }
    )
    
    // 刪除項目確認 Dialog
    if (showDeleteItemDialog && itemToDelete != null) {
        DeleteConfirmDialog(
            title = "確認刪除",
            message = "確定要刪除「${itemToDelete!!.activity}」嗎？此操作無法復原。",
            onConfirm = {
                viewModel.handleIntent(ItineraryDetailIntent.DeleteItem(itemToDelete!!.id))
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
    itinerary: Itinerary?,
    groupedItems: List<ItemsByDate>,
    selectedDate: LocalDate?,
    dateRange: ClosedRange<LocalDate>?,
    expandedItemIds: Set<String>,
    isLoading: Boolean,
    error: String?,
    onNavigateBack: () -> Unit,
    onAddItemClick: () -> Unit,
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
    onDeletePhoto: (String) -> Unit
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
                // 時間軸導覽
                dateRange?.let { range ->
                    TimelineNavigator(
                        dateRange = range,
                        groupedItems = groupedItems,
                        selectedDate = selectedDate,
                        onDateSelected = onSelectDate
                    )
                }
                
                // 內容
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
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
                                    modifier = Modifier.padding(top = Spacing.sm)
                                )
                            }
                            
                            items(
                                items = group.items,
                                key = { it.id }
                            ) { item ->
                                val isLastInGroup = item == group.items.last()
                                val isLastGroup = group == groupedItems.last()
                                val isLastItem = isLastInGroup && isLastGroup

                                ItemCard(
                                    item = item,
                                    isExpanded = expandedItemIds.contains(item.id),
                                    isLastItem = isLastItem,
                                    onToggleComplete = onToggleItemCompletion,
                                    onDelete = onDeleteItem,
                                    onEdit = onEditItemClick,
                                    onExpandToggle = onToggleItemExpansion,
                                    onAddPhoto = onAddPhoto,
                                    onSetCoverPhoto = onSetCoverPhoto,
                                    onDeletePhoto = onDeletePhoto
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

package com.example.myapplication.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.ui.component.DeleteConfirmDialog
import com.example.myapplication.ui.mvi.itinerary.ItineraryListEvent
import com.example.myapplication.ui.mvi.itinerary.ItineraryListIntent
import com.example.myapplication.ui.mvi.itinerary.ItineraryListViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * 行程列表畫面（MVI 架構）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryListScreen(
    onItineraryClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onAddClick: () -> Unit,
    viewModel: ItineraryListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itineraryToDelete by remember { mutableStateOf<Itinerary?>(null) }
    
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
        onAddClick = onAddClick
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

@Composable
private fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "還沒有任何行程",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "點擊下方按鈕建立您的第一個旅遊行程",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("建立行程")
        }
    }
}

@Composable
private fun ItineraryCard(
    itinerary: Itinerary,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)

                ) {
                    Text(
                        text = itinerary.title,
                        style = MaterialTheme.typography.titleLarge
                    )

                    if (itinerary.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = itinerary.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "編輯",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "刪除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 日期範圍
                if (itinerary.startDate != null || itinerary.endDate != null) {
                    Text(
                        text = buildString {
                            if (itinerary.startDate != null) {
                                append(itinerary.startDate.toString())
                            }
                            if (itinerary.endDate != null) {
                                if (itinerary.startDate != null) append(" - ")
                                append(itinerary.endDate.toString())
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 項目數量
                Text(
                    text = "${itinerary.items.size} 個項目",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Preview
@Preview
@kotlin.time.ExperimentalTime
@Composable
private fun ItineraryListScreenPreview() {
    MaterialTheme {
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
                onAddClick = {}
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
    onAddClick: () -> Unit
) {
    var showSearchBar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的行程") },
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Default.Search, contentDescription = "搜尋")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增行程")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜尋列
            if (showSearchBar) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("搜尋行程...") },
                    singleLine = true
                )
            }

            // 錯誤訊息
            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
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
            } else if (itineraries.isEmpty()) {
                // 空狀態
                EmptyState(onAddClick = onAddClick)
            } else {
                // 行程列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(itineraries) { itinerary ->
                        ItineraryCard(
                            itinerary = itinerary,
                            onClick = { onItineraryClick(itinerary.id) },
                            onEdit = { onEditClick(itinerary.id) },
                            onDelete = { onDeleteClick(itinerary) }
                        )
                    }
                }
            }
        }
    }
}

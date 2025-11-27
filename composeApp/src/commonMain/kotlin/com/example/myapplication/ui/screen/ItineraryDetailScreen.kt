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
import com.example.myapplication.ui.component.EmptyState
import com.example.myapplication.ui.component.ItemCard
import com.example.myapplication.ui.viewmodel.ItineraryDetailViewModel
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
    onGenerateRouteClick: () -> Unit
) {
    val itinerary by viewModel.itinerary.collectAsState()
    val groupedItems by viewModel.groupedItems.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(itineraryId) {
        viewModel.loadItinerary(itineraryId)
    }
    
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
                        onDateSelected = { viewModel.selectDate(it) }
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
                                    onToggleComplete = { viewModel.toggleItemCompletion(it) },
                                    onDelete = { viewModel.deleteItem(it) },
                                    onEdit = { onEditItemClick(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.myapplication.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.ui.viewmodel.ItineraryListViewModel

/**
 * 行程列表畫面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryListScreen(
    viewModel: ItineraryListViewModel,
    onItineraryClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val itineraries by viewModel.itineraries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
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
                    onValueChange = { viewModel.search(it) },
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
                            onClick = { onItineraryClick(itinerary.id) }
                        )
                    }
                }
            }
        }
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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

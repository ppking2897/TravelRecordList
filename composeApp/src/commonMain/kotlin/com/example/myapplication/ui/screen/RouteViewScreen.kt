package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Route
import com.example.myapplication.data.model.RouteLocation
import com.example.myapplication.data.repository.RouteRepository
import kotlinx.coroutines.launch

/**
 * 路線檢視畫面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteViewScreen(
    routeId: String,
    routeRepository: RouteRepository,
    onNavigateBack: () -> Unit,
    onExportClick: (String) -> Unit
) {
    var route by remember { mutableStateOf<Route?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(routeId) {
        scope.launch {
            isLoading = true
            error = null
            
            routeRepository.getRoute(routeId)
                .onSuccess { loadedRoute ->
                    route = loadedRoute
                }
                .onFailure { exception ->
                    error = exception.message ?: "載入路線失敗"
                }
            
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(route?.title ?: "路線詳情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                routeRepository.exportRoute(routeId)
                                    .onSuccess { json ->
                                        onExportClick(json)
                                    }
                                    .onFailure { exception ->
                                        error = exception.message ?: "匯出失敗"
                                    }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "匯出")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (route == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = error ?: "找不到路線",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 路線資訊卡片
                item {
                    RouteInfoCard(route = route!!)
                }
                
                // 錯誤訊息
                error?.let { errorMessage ->
                    item {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                // 地點列表標題
                item {
                    Text(
                        text = "路線地點",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // 地點列表
                itemsIndexed(route!!.locations) { index, routeLocation ->
                    RouteLocationCard(
                        routeLocation = routeLocation,
                        isFirst = index == 0,
                        isLast = index == route!!.locations.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteInfoCard(route: Route) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = route.title,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Divider()
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${route.locations.size} 個地點",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            // 總建議時間（如果有）
            val totalDuration = route.locations.mapNotNull { it.recommendedDuration }
                .fold(kotlin.time.Duration.ZERO) { acc, duration -> acc + duration }
            if (totalDuration.inWholeMinutes > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "建議總時間：${totalDuration.inWholeHours} 小時 ${totalDuration.inWholeMinutes % 60} 分鐘",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteLocationCard(
    routeLocation: RouteLocation,
    isFirst: Boolean,
    isLast: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 順序指示器
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 上方連接線
                if (!isFirst) {
                    Divider(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // 順序圓圈
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (routeLocation.order + 1).toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // 下方連接線
                if (!isLast) {
                    Divider(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 地點資訊
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 地點名稱
                Text(
                    text = routeLocation.location.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                // 地址
                routeLocation.location.address?.let { address ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 座標
                if (routeLocation.location.latitude != null && routeLocation.location.longitude != null) {
                    Text(
                        text = "座標：${routeLocation.location.latitude}, ${routeLocation.location.longitude}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 建議停留時間
                routeLocation.recommendedDuration?.let { duration ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "建議停留：${duration.inWholeHours} 小時 ${duration.inWholeMinutes % 60} 分鐘",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 備註
                if (routeLocation.notes.isNotBlank()) {
                    Text(
                        text = routeLocation.notes,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

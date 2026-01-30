package com.example.myapplication.presentation.route_view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.entity.Location
import com.example.myapplication.domain.entity.Route
import com.example.myapplication.domain.entity.RouteLocation
import com.example.myapplication.presentation.route_view.RouteViewEvent
import com.example.myapplication.presentation.route_view.RouteViewIntent
import com.example.myapplication.presentation.route_view.RouteViewViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * 路線檢視畫面（MVI 架構）
 */
@Composable
fun RouteViewScreen(
    routeId: String,
    onNavigateBack: () -> Unit,
    onExportSuccess: (String) -> Unit,
    viewModel: RouteViewViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    
    // 收集 Event
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is RouteViewEvent.NavigateBack -> onNavigateBack()
                is RouteViewEvent.ExportSuccess -> onExportSuccess(event.json)
                is RouteViewEvent.ShowError -> {
                    // 錯誤訊息透過 Event 顯示
                }
            }
        }
    }
    
    // 載入路線
    LaunchedEffect(routeId) {
        viewModel.handleIntent(RouteViewIntent.LoadRoute(routeId))
    }
    
    RouteViewScreenContent(
        route = state.route,
        isLoading = state.isLoading,
        isExporting = state.isExporting,
        error = state.error,
        onNavigateBack = onNavigateBack,
        onExportClick = {
            viewModel.handleIntent(RouteViewIntent.ExportRoute(routeId))
        },
        onOpenMapClick = { location ->
            val query = if (location.latitude != null && location.longitude != null) {
                "${location.latitude},${location.longitude}"
            } else {
                location.address ?: location.name
            }
            val url = "https://www.google.com/maps/search/?api=1&query=${query.replace(" ", "+")}"
            uriHandler.openUri(url)
        }
    )
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
            
            HorizontalDivider()
            
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
    isLast: Boolean,
    onOpenMapClick: (Location) -> Unit
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
            // ... (順序指示器 code unchanged)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!isFirst) {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
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
                
                if (!isLast) {
                    HorizontalDivider(
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = routeLocation.location.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(onClick = { onOpenMapClick(routeLocation.location) }) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "開啟地圖",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
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


// Preview
@Preview
@Composable
private fun RouteViewScreenPreview() {
    MaterialTheme {
        Surface {
            RouteViewScreenContent(
                route = Route(
                    id = "1",
                    title = "東京一日遊",
                    locations = listOf(
                        RouteLocation(
                            location = Location(
                                name = "淺草寺",
                                latitude = 35.7148,
                                longitude = 139.7967,
                                address = "東京都台東區淺草2-3-1"
                            ),
                            order = 0,
                            recommendedDuration = kotlin.time.Duration.parse("2h"),
                            notes = "參觀雷門和五重塔"
                        ),
                        RouteLocation(
                            location = Location(
                                name = "晴空塔",
                                latitude = 35.7101,
                                longitude = 139.8107,
                                address = "東京都墨田區押上1-1-2"
                            ),
                            order = 1,
                            recommendedDuration = kotlin.time.Duration.parse("3h"),
                            notes = "登塔觀景"
                        )
                    ),
                    createdFrom = "1"
                ),
                isLoading = false,
                isExporting = false,
                error = null,
                onNavigateBack = {},
                onExportClick = {},
                onOpenMapClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun RouteViewScreenPreview_Loading() {
    MaterialTheme {
        Surface {
            RouteViewScreenContent(
                route = null,
                isLoading = true,
                isExporting = false,
                error = null,
                onNavigateBack = {},
                onExportClick = {},
                onOpenMapClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun RouteViewScreenPreview_Error() {
    MaterialTheme {
        Surface {
            RouteViewScreenContent(
                route = null,
                isLoading = false,
                isExporting = false,
                error = "找不到路線",
                onNavigateBack = {},
                onExportClick = {},
                onOpenMapClick = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteViewScreenContent(
    route: Route?,
    isLoading: Boolean,
    isExporting: Boolean,
    error: String?,
    onNavigateBack: () -> Unit,
    onExportClick: () -> Unit,
    onOpenMapClick: (Location) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(route?.title ?: "路線詳情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(horizontal = 12.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = onExportClick,
                            enabled = route != null
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "匯出")
                        }
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
                    RouteInfoCard(route = route)
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
                itemsIndexed(route.locations) { index, routeLocation ->
                    RouteLocationCard(
                        routeLocation = routeLocation,
                        isFirst = index == 0,
                        isLast = index == route.locations.lastIndex,
                        onOpenMapClick = onOpenMapClick
                    )
                }
            }
        }
    }
}

package io.github.ppking2897.travelrecord.feature.route.presentation

import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import io.github.ppking2897.travelrecord.LocalSnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.ppking2897.travelrecord.core.presentation.components.LoadingContent
import io.github.ppking2897.travelrecord.core.presentation.theme.CardStyle
import io.github.ppking2897.travelrecord.core.presentation.theme.IconSize
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import io.github.ppking2897.travelrecord.core.presentation.theme.TravelRecordTheme
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.core.presentation.util.DateTimeFormatter
import io.github.ppking2897.travelrecord.feature.route.domain.model.Route
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteDay
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteLocation
import kotlinx.datetime.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * 路線檢視畫面（MVI 架構）
 */
@Composable
fun RouteViewScreen(
    itineraryId: String,
    onNavigateBack: () -> Unit,
    onExportSuccess: (String) -> Unit,
    viewModel: RouteViewViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    // 手機上沒有能開網址的 App 時 openUri 會丟例外，改成提示而不是閃退
    val openUrl: (String) -> Unit = { url ->
        runCatching { uriHandler.openUri(url) }.onFailure {
            scope.launch { snackbarHostState.showSnackbar("沒有可以開啟地圖的 App") }
        }
    }

    // 收集 Event
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is RouteViewEvent.NavigateBack -> onNavigateBack()
                is RouteViewEvent.ExportSuccess -> onExportSuccess(event.text)
                is RouteViewEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }
    
    // 載入路線
    LaunchedEffect(itineraryId) {
        viewModel.processIntent(RouteViewIntent.LoadRoute(itineraryId))
    }
    
    RouteViewScreenContent(
        route = state.route,
        isLoading = state.isLoading,
        isExporting = state.isExporting,
        error = state.error,
        onNavigateBack = onNavigateBack,
        onExportClick = {
            viewModel.processIntent(RouteViewIntent.ExportRoute(itineraryId))
        },
        onOpenMapClick = { routeLocation -> openUrl(googleMapsTransitFromHereUrl(routeLocation)) },
        onOpenUrl = openUrl
    )
}

/** 段落標題「第 1 天 · 2026/10/14」；日期在行程範圍外時只寫日期 */
private fun RouteDay.title(): String {
    val date = DateTimeFormatter.formatDate(date)
    return dayNumber?.let { "第 $it 天 · $date" } ?: date
}

@Composable
private fun RouteInfoCard(route: Route) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardStyle.cornerRadius),
        border = BorderStroke(CardStyle.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = route.title,
                style = MaterialTheme.typography.headlineSmall
            )
            
            HorizontalDivider()
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${route.placeCount} 個地點",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/** 每天的標題；兩站以上的天加「當天路線」（開 Google 地圖看這天），超過 11 站時提示只帶前 11 站 */
@Composable
private fun RouteDayHeader(
    title: String,
    dayLink: DayMapsLink?,
    onOpenUrl: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = Spacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            dayLink?.let { link ->
                TextButton(onClick = { onOpenUrl(link.url) }) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(IconSize.sm)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text("當天路線")
                }
            }
        }
        if (dayLink?.truncated == true) {
            Text(
                text = "Google 地圖一次最多 $MAX_GOOGLE_MAPS_STOPS 站，只會帶前 $MAX_GOOGLE_MAPS_STOPS 站",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RouteLocationCard(
    routeLocation: RouteLocation,
    isFirst: Boolean,
    isLast: Boolean,
    onOpenMapClick: (RouteLocation) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardStyle.cornerRadius),
        border = BorderStroke(CardStyle.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // ... (順序指示器 code unchanged)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                // 編號方塊上下的連接線用直的；橫線放在 2dp 寬的格子裡只剩一個點
                if (!isFirst) {
                    VerticalDivider(
                        modifier = Modifier.height(16.dp),
                        thickness = 2.dp,
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
                    VerticalDivider(
                        modifier = Modifier.height(16.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 地點資訊
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
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
                    
                    // 從目前位置到這站（大眾運輸）
                    IconButton(onClick = { onOpenMapClick(routeLocation) }) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "從目前位置怎麼過去",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 地址
                routeLocation.location.address?.let { address ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(IconSize.xs),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    TravelRecordTheme {
        Surface {
            RouteViewScreenContent(
                route = Route(
                    title = "東京一日遊",
                    days = listOf(
                        RouteDay(
                            date = LocalDate(2026, 9, 15),
                            dayNumber = 1,
                            stops = listOf(
                                RouteLocation(
                                    location = Location(
                                        name = "淺草寺",
                                        latitude = 35.7148,
                                        longitude = 139.7967,
                                        address = "東京都台東區淺草2-3-1"
                                    ),
                                    order = 0,
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
                                    notes = "登塔觀景"
                                )
                            )
                        )
                    ),
                    createdFrom = "1"
                ),
                isLoading = false,
                isExporting = false,
                error = null,
                onNavigateBack = {},
                onExportClick = {},
                onOpenMapClick = {},
                onOpenUrl = {}
            )
        }
    }
}

@Preview
@Composable
private fun RouteViewScreenPreview_Loading() {
    TravelRecordTheme {
        Surface {
            RouteViewScreenContent(
                route = null,
                isLoading = true,
                isExporting = false,
                error = null,
                onNavigateBack = {},
                onExportClick = {},
                onOpenMapClick = {},
                onOpenUrl = {}
            )
        }
    }
}

@Preview
@Composable
private fun RouteViewScreenPreview_Error() {
    TravelRecordTheme {
        Surface {
            RouteViewScreenContent(
                route = null,
                isLoading = false,
                isExporting = false,
                error = "找不到路線",
                onNavigateBack = {},
                onExportClick = {},
                onOpenMapClick = {},
                onOpenUrl = {}
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
    onOpenMapClick: (RouteLocation) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
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
                                .size(IconSize.md)
                                .padding(horizontal = Spacing.md),
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
            LoadingContent(modifier = Modifier.padding(paddingValues))
        } else if (route == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(IconSize.xxl),
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
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
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
                            modifier = Modifier.padding(vertical = Spacing.sm)
                        )
                    }
                }

                // 地點列表標題
                item {
                    Text(
                        text = "路線地點",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = Spacing.sm)
                    )
                }
                
                // 依天分段，每段的編號從 1 開始
                route.days.forEach { day ->
                    item(key = "day_${day.date}") {
                        RouteDayHeader(
                            title = day.title(),
                            dayLink = day.toGoogleMapsDayLink(),
                            onOpenUrl = onOpenUrl
                        )
                    }
                    itemsIndexed(day.stops) { index, routeLocation ->
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            RouteLocationCard(
                                routeLocation = routeLocation,
                                isFirst = index == 0,
                                isLast = index == day.stops.lastIndex,
                                onOpenMapClick = onOpenMapClick
                            )
                            // 只在同一天的相鄰兩站之間
                            day.stops.getOrNull(index + 1)?.let { next ->
                                TextButton(
                                    onClick = { onOpenUrl(googleMapsTransitUrl(routeLocation, next)) },
                                    modifier = Modifier.padding(start = Spacing.lg)
                                ) {
                                    Icon(
                                        Icons.Default.DirectionsTransit,
                                        contentDescription = null,
                                        modifier = Modifier.size(IconSize.sm)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text("怎麼去")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

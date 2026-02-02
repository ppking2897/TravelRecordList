package com.example.myapplication.presentation.itinerary_map

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.entity.MapMarker
import com.example.myapplication.presentation.components.ItineraryMapView
import com.example.myapplication.presentation.components.MapInfoCard
import com.example.myapplication.presentation.theme.AppGradients
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * 行程地圖畫面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryMapScreen(
    viewModel: ItineraryMapViewModel,
    itineraryId: String,
    onNavigateBack: () -> Unit,
    onEditItemClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(itineraryId) {
        viewModel.handleIntent(ItineraryMapIntent.LoadMapData(itineraryId))
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ItineraryMapEvent.NavigateBack -> onNavigateBack()
                is ItineraryMapEvent.NavigateToEditItem -> onEditItemClick(event.itemId)
                is ItineraryMapEvent.ShowError -> { /* Handle in UI */ }
                is ItineraryMapEvent.CenterOnMarker -> { /* Handled by map component */ }
            }
        }
    }

    ItineraryMapScreenContent(
        itineraryTitle = state.itinerary?.title ?: "地圖",
        markers = state.markers,
        selectedMarker = state.selectedMarker,
        selectedDate = state.selectedDate,
        dateRange = state.itinerary?.let { itinerary ->
            val start = itinerary.startDate
            val end = itinerary.endDate
            if (start != null && end != null) start..end else null
        },
        isLoading = state.isLoading,
        error = state.error,
        hasNoMarkersToShow = state.hasNoMarkersToShow,
        onNavigateBack = onNavigateBack,
        onMarkerClick = { viewModel.handleIntent(ItineraryMapIntent.SelectMarker(it)) },
        onDismissMarker = { viewModel.handleIntent(ItineraryMapIntent.SelectMarker(null)) },
        onEditItem = onEditItemClick,
        onFilterByDate = { viewModel.handleIntent(ItineraryMapIntent.FilterByDate(it)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryMapScreenContent(
    itineraryTitle: String,
    markers: List<MapMarker>,
    selectedMarker: MapMarker?,
    selectedDate: LocalDate?,
    dateRange: ClosedRange<LocalDate>?,
    isLoading: Boolean,
    error: String?,
    hasNoMarkersToShow: Boolean,
    onNavigateBack: () -> Unit,
    onMarkerClick: (MapMarker) -> Unit,
    onDismissMarker: () -> Unit,
    onEditItem: (String) -> Unit,
    onFilterByDate: (LocalDate?) -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val backgroundGradient = if (isLightTheme) {
        AppGradients.warmBackgroundGradient
    } else {
        AppGradients.darkBackgroundGradient
    }

    var showDateFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(itineraryTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 日期篩選按鈕
                    dateRange?.let { range ->
                        Box {
                            IconButton(onClick = { showDateFilterMenu = true }) {
                                Badge(
                                    modifier = Modifier.offset(x = 8.dp, y = (-8).dp),
                                    containerColor = if (selectedDate != null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ) {
                                    if (selectedDate != null) {
                                        Text(
                                            text = "1",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "篩選日期"
                                )
                            }

                            DropdownMenu(
                                expanded = showDateFilterMenu,
                                onDismissRequest = { showDateFilterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "顯示全部",
                                            color = if (selectedDate == null) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    },
                                    onClick = {
                                        onFilterByDate(null)
                                        showDateFilterMenu = false
                                    }
                                )
                                HorizontalDivider()

                                // 生成日期範圍內的所有日期
                                generateDateSequence(range).forEach { date ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = date.toString(),
                                                color = if (selectedDate == date) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        },
                                        onClick = {
                                            onFilterByDate(date)
                                            showDateFilterMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                hasNoMarkersToShow -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (selectedDate != null) {
                                    "此日期沒有可顯示的地點"
                                } else {
                                    "行程中的項目沒有座標資訊"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "請在項目中設定地點座標",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                else -> {
                    // 地圖視圖
                    ItineraryMapView(
                        markers = markers,
                        selectedMarker = selectedMarker,
                        onMarkerClick = onMarkerClick,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 底部資訊卡片
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        MapInfoCard(
                            marker = selectedMarker,
                            onEditClick = onEditItem,
                            onDismiss = onDismissMarker
                        )
                    }
                }
            }
        }
    }
}

/**
 * 生成日期範圍內的日期序列
 */
private fun generateDateSequence(range: ClosedRange<LocalDate>): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var current = range.start
    while (current <= range.endInclusive) {
        dates.add(current)
        current = current.plus(1, DateTimeUnit.DAY)
    }
    return dates
}

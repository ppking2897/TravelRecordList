@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.feature.footprint.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.LocalSnackbarHostState
import com.example.myapplication.core.presentation.components.ItineraryMapView
import com.example.myapplication.core.presentation.theme.CardStyle
import com.example.myapplication.core.presentation.theme.IconSize
import com.example.myapplication.core.presentation.theme.Spacing
import com.example.myapplication.core.presentation.theme.TravelRecordTheme
import com.example.myapplication.feature.footprint.domain.model.VisitedPlace
import com.example.myapplication.feature.route.domain.model.MapMarker
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FootprintScreen(
    modifier: Modifier = Modifier,
    viewModel: FootprintViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(Unit) {
        viewModel.handleIntent(FootprintIntent.Load)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is FootprintEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    FootprintScreenContent(
        state = state,
        modifier = modifier,
        onRefresh = { viewModel.handleIntent(FootprintIntent.Refresh) },
        onMarkerClick = { viewModel.handleIntent(FootprintIntent.SelectMarker(it)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FootprintScreenContent(
    state: FootprintState,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onMarkerClick: (MapMarker) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("足跡") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "重新整理")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            state.places.isEmpty() -> EmptyFootprintState(
                modifier = Modifier.padding(paddingValues)
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                FootprintMapSection(
                    markers = state.markers,
                    selectedMarker = state.selectedMarker,
                    onMarkerClick = onMarkerClick,
                    hasCoordinates = state.markers.isNotEmpty()
                )
                FootprintStatsRow(
                    total = state.totalPlaces,
                    completed = state.completedPlaces,
                    trips = state.totalTrips
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.lg,
                        top = Spacing.sm,
                        end = Spacing.lg,
                        bottom = Spacing.xxl
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(state.places, key = { it.itemId }) { place ->
                        VisitedPlaceRow(
                            place = place,
                            isSelected = state.selectedMarker?.id == place.itemId
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FootprintMapSection(
    markers: List<MapMarker>,
    selectedMarker: MapMarker?,
    onMarkerClick: (MapMarker) -> Unit,
    hasCoordinates: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        if (hasCoordinates) {
            ItineraryMapView(
                markers = markers,
                selectedMarker = selectedMarker,
                onMarkerClick = onMarkerClick,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(IconSize.lg)
                    )
                    Text(
                        text = "目前的地點還沒有座標資訊，等之後加上就能在這裡連成一張足跡圖。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FootprintStatsRow(
    total: Int,
    completed: Int,
    trips: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatBlock(icon = Icons.Default.Place, label = "地點", value = total.toString())
        StatBlock(icon = Icons.Default.CheckCircle, label = "打卡", value = completed.toString())
        StatBlock(icon = Icons.Default.Flag, label = "旅行", value = trips.toString())
    }
}

@Composable
private fun StatBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(IconSize.sm)
        )
        Text(
            text = "$value $label",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VisitedPlaceRow(
    place: VisitedPlace,
    isSelected: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardStyle.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (place.isCompleted) Icons.Default.CheckCircle else Icons.Default.Place,
                contentDescription = null,
                tint = if (place.isCompleted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(IconSize.sm)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.locationName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = place.activity.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${place.tripTitle} · ${formatDate(place.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyFootprintState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.padding(Spacing.xl)
        ) {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                modifier = Modifier.size(IconSize.xl),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "還沒有走過的地方",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "行程裡每新增一個地點，這裡就會多出一枚足跡。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(date: LocalDate): String {
    return "${date.year}/${date.monthNumber.toString().padStart(2, '0')}/${date.dayOfMonth.toString().padStart(2, '0')}"
}

// ==================== Preview 假資料 ====================

private fun previewPlaces(): List<VisitedPlace> = listOf(
    VisitedPlace(
        itemId = "fp-1",
        locationName = "伏見稲荷大社",
        activity = "千本鳥居參拜",
        date = LocalDate(2026, 3, 10),
        tripId = "trip-1",
        tripTitle = "京都大阪 5 日遊",
        latitude = 34.9671,
        longitude = 135.7727,
        isCompleted = true
    ),
    VisitedPlace(
        itemId = "fp-2",
        locationName = "清水寺",
        activity = "清水舞台賞景",
        date = LocalDate(2026, 3, 10),
        tripId = "trip-1",
        tripTitle = "京都大阪 5 日遊",
        latitude = 34.9949,
        longitude = 135.7850,
        isCompleted = true
    ),
    VisitedPlace(
        itemId = "fp-3",
        locationName = "大阪城",
        activity = "大阪城天守閣",
        date = LocalDate(2026, 3, 13),
        tripId = "trip-1",
        tripTitle = "京都大阪 5 日遊",
        latitude = 34.6873,
        longitude = 135.5262,
        isCompleted = false
    )
)

@ExperimentalTime
@Preview
@Composable
private fun FootprintScreen_WithData_Preview() {
    TravelRecordTheme {
        Surface {
            FootprintScreenContent(
                state = FootprintState(places = previewPlaces()),
                onRefresh = {},
                onMarkerClick = {}
            )
        }
    }
}

@ExperimentalTime
@Preview
@Composable
private fun FootprintScreen_Empty_Preview() {
    TravelRecordTheme {
        Surface {
            FootprintScreenContent(
                state = FootprintState(),
                onRefresh = {},
                onMarkerClick = {}
            )
        }
    }
}

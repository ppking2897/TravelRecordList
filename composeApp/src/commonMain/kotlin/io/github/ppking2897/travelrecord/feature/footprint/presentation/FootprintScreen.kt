@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.footprint.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import io.github.ppking2897.travelrecord.LocalSnackbarHostState
import io.github.ppking2897.travelrecord.core.presentation.theme.CardStyle
import io.github.ppking2897.travelrecord.core.presentation.theme.IconSize
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import io.github.ppking2897.travelrecord.core.presentation.theme.TravelRecordTheme
import io.github.ppking2897.travelrecord.core.presentation.util.DateTimeFormatter
import io.github.ppking2897.travelrecord.feature.footprint.domain.model.TripFootprintGroup
import io.github.ppking2897.travelrecord.feature.footprint.domain.model.VisitedPlace
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FootprintScreen(
    modifier: Modifier = Modifier,
    onOpenTrip: (String) -> Unit,
    viewModel: FootprintViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(Unit) {
        viewModel.processIntent(FootprintIntent.Load)
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
        onRefresh = { viewModel.processIntent(FootprintIntent.Refresh) },
        onToggleExpanded = { viewModel.processIntent(FootprintIntent.ToggleExpanded(it)) },
        onOpenTrip = onOpenTrip
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FootprintScreenContent(
    state: FootprintState,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onToggleExpanded: (String) -> Unit,
    onOpenTrip: (String) -> Unit
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

            state.groups.isEmpty() -> EmptyFootprintState(
                modifier = Modifier.padding(paddingValues)
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    top = Spacing.md,
                    end = Spacing.lg,
                    bottom = Spacing.xxl
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item {
                    SummaryCard(
                        totalTrips = state.totalTrips,
                        totalPlaces = state.totalPlaces,
                        totalCompleted = state.totalCompleted
                    )
                }

                items(state.groups, key = { it.tripId }) { group ->
                    TripFootprintCard(
                        group = group,
                        expanded = state.expandedTripId == group.tripId,
                        onToggleExpanded = { onToggleExpanded(group.tripId) },
                        onOpenTrip = { onOpenTrip(group.tripId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalTrips: Int,
    totalPlaces: Int,
    totalCompleted: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(CardStyle.cornerRadius),
        border = BorderStroke(CardStyle.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardStyle.contentPadding),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryStat(
                icon = Icons.Default.Flag,
                value = totalTrips.toString(),
                label = "旅行"
            )
            SummaryStat(
                icon = Icons.Default.Place,
                value = totalPlaces.toString(),
                label = "地點"
            )
            SummaryStat(
                icon = Icons.Default.CheckCircle,
                value = totalCompleted.toString(),
                label = "打卡"
            )
        }
    }
}

@Composable
private fun SummaryStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(IconSize.md)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun TripFootprintCard(
    group: TripFootprintGroup,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onOpenTrip: () -> Unit
) {
    Card(
        onClick = onOpenTrip,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(CardStyle.cornerRadius),
        border = BorderStroke(CardStyle.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(CardStyle.contentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(IconSize.md)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.tripTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val dateText = formatDateRange(group.startDate, group.endDate)
                    Text(
                        text = if (dateText != null) {
                            "${group.placeCount} 個地點 · ${group.completedCount} 打卡 · $dateText"
                        } else {
                            "${group.placeCount} 個地點 · ${group.completedCount} 打卡"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "收合" else "展開"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    group.places.forEach { place ->
                        VisitedPlaceRow(place = place)
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitedPlaceRow(place: VisitedPlace) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.Top
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
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (place.activity.isNotBlank()) {
                Text(
                    text = place.activity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = DateTimeFormatter.formatDate(place.date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                text = "打卡或旅行日期過了之後，這裡會按旅行收攏你去過的地方。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDateRange(start: LocalDate?, end: LocalDate?): String? {
    if (start == null && end == null) return null
    if (start != null && end != null) {
        return if (start == end) {
            DateTimeFormatter.formatDate(start)
        } else {
            "${DateTimeFormatter.formatDate(start)} - ${DateTimeFormatter.formatDate(end)}"
        }
    }
    return DateTimeFormatter.formatDate(start ?: end!!)
}

// ==================== Preview 假資料 ====================

private fun previewGroups(): List<TripFootprintGroup> = listOf(
    TripFootprintGroup(
        tripId = "trip-kyoto",
        tripTitle = "京都大阪 5 日遊",
        startDate = LocalDate(2026, 4, 23),
        endDate = LocalDate(2026, 4, 27),
        places = listOf(
            VisitedPlace("p1", "伏見稲荷大社", "千本鳥居參拜", LocalDate(2026, 4, 23), "trip-kyoto", "京都大阪 5 日遊", 34.9671, 135.7727, false),
            VisitedPlace("p2", "清水寺", "清水舞台賞景", LocalDate(2026, 4, 23), "trip-kyoto", "京都大阪 5 日遊", 34.9949, 135.7850, false),
            VisitedPlace("p3", "金閣寺", "參觀金閣寺", LocalDate(2026, 4, 24), "trip-kyoto", "京都大阪 5 日遊", 35.0394, 135.7292, true),
            VisitedPlace("p4", "嵐山竹林", "竹林散步", LocalDate(2026, 4, 24), "trip-kyoto", "京都大阪 5 日遊", 35.0173, 135.6717, false)
        )
    ),
    TripFootprintGroup(
        tripId = "trip-tokyo",
        tripTitle = "東京 3 日遊",
        startDate = LocalDate(2026, 3, 25),
        endDate = LocalDate(2026, 3, 27),
        places = listOf(
            VisitedPlace("t1", "淺草寺", "雷門參拜", LocalDate(2026, 3, 25), "trip-tokyo", "東京 3 日遊", 35.7148, 139.7967, true),
            VisitedPlace("t2", "晴空塔", "展望台", LocalDate(2026, 3, 25), "trip-tokyo", "東京 3 日遊", 35.7101, 139.8107, true)
        )
    )
)

@ExperimentalTime
@Preview
@Composable
private fun FootprintScreen_Collapsed_Preview() {
    TravelRecordTheme {
        Surface {
            FootprintScreenContent(
                state = FootprintState(groups = previewGroups()),
                onRefresh = {},
                onToggleExpanded = {},
                onOpenTrip = {}
            )
        }
    }
}

@ExperimentalTime
@Preview
@Composable
private fun FootprintScreen_Expanded_Preview() {
    TravelRecordTheme {
        Surface {
            FootprintScreenContent(
                state = FootprintState(
                    groups = previewGroups(),
                    expandedTripId = "trip-kyoto"
                ),
                onRefresh = {},
                onToggleExpanded = {},
                onOpenTrip = {}
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
                onToggleExpanded = {},
                onOpenTrip = {}
            )
        }
    }
}

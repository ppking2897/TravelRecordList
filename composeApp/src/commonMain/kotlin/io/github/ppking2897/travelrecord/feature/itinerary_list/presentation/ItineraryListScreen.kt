package io.github.ppking2897.travelrecord.feature.itinerary_list.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import io.github.ppking2897.travelrecord.LocalSnackbarHostState
import io.github.ppking2897.travelrecord.core.presentation.components.DefaultCoverPlaceholder
import io.github.ppking2897.travelrecord.core.presentation.components.DeleteConfirmDialog
import io.github.ppking2897.travelrecord.core.presentation.components.LocalImage
import io.github.ppking2897.travelrecord.core.presentation.theme.CardStyle
import io.github.ppking2897.travelrecord.core.presentation.theme.IconSize
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import io.github.ppking2897.travelrecord.core.presentation.theme.TravelRecordTheme
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.TravelPhase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetItineraryPhaseUseCase
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryListScreen(
    onItineraryClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: ItineraryListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = LocalSnackbarHostState.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itineraryToDelete by remember { mutableStateOf<Itinerary?>(null) }

    LifecycleResumeEffect(Unit) {
        viewModel.processIntent(ItineraryListIntent.LoadItineraries)
        onPauseOrDispose { }
    }

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
                is ItineraryListEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    ItineraryPlanningDashboard(
        itineraries = state.itineraries,
        searchQuery = state.searchQuery,
        isLoading = state.isLoading,
        isRefreshing = state.isRefreshing,
        error = state.error,
        onSearchChange = { viewModel.processIntent(ItineraryListIntent.Search(it)) },
        onItineraryClick = onItineraryClick,
        onEditClick = onEditClick,
        onDeleteClick = { itinerary ->
            itineraryToDelete = itinerary
            showDeleteDialog = true
        },
        onAddClick = onAddClick,
        onHistoryClick = onHistoryClick,
        onRefresh = { viewModel.processIntent(ItineraryListIntent.Refresh) }
    )

    if (showDeleteDialog && itineraryToDelete != null) {
        DeleteConfirmDialog(
            title = "刪除行程",
            message = "確定要刪除「${itineraryToDelete!!.title}」嗎？這個動作無法復原。",
            onConfirm = {
                viewModel.processIntent(ItineraryListIntent.DeleteItinerary(itineraryToDelete!!.id))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryPlanningDashboard(
    itineraries: List<Itinerary>,
    searchQuery: String,
    isLoading: Boolean,
    isRefreshing: Boolean = false,
    error: String?,
    onSearchChange: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (Itinerary) -> Unit,
    onAddClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    var selectedFilter by remember { mutableStateOf(ItineraryFilter.All) }
    val today = rememberToday()
    val phaseUseCase = remember { GetItineraryPhaseUseCase() }

    val dashboard = remember(itineraries, searchQuery, selectedFilter, today) {
        buildItineraryDashboard(itineraries, searchQuery, selectedFilter, today)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("行程規劃") },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = "查看足跡",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("建立行程") }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    top = Spacing.sm,
                    end = Spacing.lg,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                item {
                    PlanningHeader(
                        currentCount = dashboard.currentCount,
                        upcomingCount = dashboard.upcomingCount,
                        completedCount = dashboard.completedCount
                    )
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜尋行程、地點或活動...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        ItineraryFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter.label) }
                            )
                        }
                    }
                }

                error?.let { errorMessage ->
                    item {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (isLoading) {
                    item {
                        ItineraryListSkeleton()
                    }
                } else if (dashboard.emptyState == DashboardEmptyState.NoItineraries) {
                    item {
                        EmptyPlanningState(onAddClick = onAddClick)
                    }
                } else if (dashboard.emptyState == DashboardEmptyState.NoMatch) {
                    item {
                        NoMatchState()
                    }
                } else {
                    if (dashboard.currentTrips.isNotEmpty()) {
                        item { SectionHeader("現在旅行中", "今天可以直接進入每日規劃") }
                        items(dashboard.currentTrips, key = { "current_${it.id}" }) { itinerary ->
                            ItineraryDashboardCard(
                                itinerary = itinerary,
                                phase = phaseUseCase(itinerary, today),
                                emphasized = true,
                                onClick = { onItineraryClick(itinerary.id) },
                                onEdit = { onEditClick(itinerary.id) },
                                onDelete = { onDeleteClick(itinerary) }
                            )
                        }
                    }

                    if (dashboard.upcomingTrips.isNotEmpty()) {
                        item { SectionHeader("即將出發", "出發前先把每日地點安排好") }
                        items(dashboard.upcomingTrips, key = { "upcoming_${it.id}" }) { itinerary ->
                            ItineraryDashboardCard(
                                itinerary = itinerary,
                                phase = phaseUseCase(itinerary, today),
                                onClick = { onItineraryClick(itinerary.id) },
                                onEdit = { onEditClick(itinerary.id) },
                                onDelete = { onDeleteClick(itinerary) }
                            )
                        }
                    }

                    if (dashboard.recentTrips.isNotEmpty()) {
                        item { SectionHeader("最近編輯", "快速回到剛調整過的行程") }
                        items(dashboard.recentTrips, key = { "recent_${it.id}" }) { itinerary ->
                            ItineraryDashboardCard(
                                itinerary = itinerary,
                                phase = phaseUseCase(itinerary, today),
                                compact = true,
                                onClick = { onItineraryClick(itinerary.id) },
                                onEdit = { onEditClick(itinerary.id) },
                                onDelete = { onDeleteClick(itinerary) }
                            )
                        }
                    }

                    item { SectionHeader("全部行程", "${dashboard.allTrips.size} 筆行程") }
                    items(dashboard.allTrips, key = { "all_${it.id}" }) { itinerary ->
                        ItineraryDashboardCard(
                            itinerary = itinerary,
                            phase = phaseUseCase(itinerary, today),
                            compact = true,
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

@Composable
private fun PlanningHeader(
    currentCount: Int,
    upcomingCount: Int,
    completedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(CardStyle.cornerRadius),
        border = BorderStroke(CardStyle.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = "從出發前開始，把每一天安排好。",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DashboardStat("$currentCount", "旅行中")
                DashboardStat("$upcomingCount", "即將出發")
                DashboardStat("$completedCount", "已結束")
            }
        }
    }
}

@Composable
private fun DashboardStat(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ItineraryDashboardCard(
    itinerary: Itinerary,
    phase: TravelPhase,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    compact: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }
    val stats = remember(itinerary.items) { itinerary.cardStats() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = CardStyle.listCardElevation),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasized) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        ),
        shape = RoundedCornerShape(CardStyle.cornerRadius),
        border = BorderStroke(CardStyle.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardStyle.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TripCover(
                itinerary = itinerary,
                modifier = Modifier.size(if (compact) 72.dp else 96.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = itinerary.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    PhaseBadge(phase = phase)
                }

                formatDateRange(itinerary.startDate, itinerary.endDate)?.let { dateText ->
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "${stats.itemCount} 個地點 · ${stats.completedCount} 已完成 · ${stats.photoCount} 張照片",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                mainLocationOf(itinerary.items)?.let { location ->
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
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("編輯") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("刪除") },
                        onClick = {
                            showMenu = false
                            onDelete()
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
    }
}

@Composable
private fun TripCover(
    itinerary: Itinerary,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (itinerary.coverPhotoPath != null) {
            LocalImage(
                filePath = itinerary.coverPhotoPath,
                contentDescription = itinerary.title,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            DefaultCoverPlaceholder(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun PhaseBadge(
    phase: TravelPhase
) {
    val color = when (phase) {
        TravelPhase.Planning -> MaterialTheme.colorScheme.primaryContainer
        TravelPhase.InProgress -> MaterialTheme.colorScheme.tertiaryContainer
        TravelPhase.Completed -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (phase) {
        TravelPhase.Planning -> MaterialTheme.colorScheme.onPrimaryContainer
        TravelPhase.InProgress -> MaterialTheme.colorScheme.onTertiaryContainer
        TravelPhase.Completed -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = phase.label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
        )
    }
}

@Composable
private fun EmptyPlanningState(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(IconSize.xxl),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = "還沒有行程",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "先建立一趟旅行，再安排每天要去的地點。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("建立第一個行程")
        }
    }
}

/** 有行程，但搜尋或篩選後沒有符合的 */
@Composable
private fun NoMatchState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(IconSize.xxl),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = "沒有符合的行程",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
private fun rememberToday(): LocalDate {
    return remember {
        Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }
}

@Preview
@kotlin.time.ExperimentalTime
@Composable
private fun ItineraryPlanningDashboardPreview() {
    TravelRecordTheme {
        Surface {
            ItineraryPlanningDashboard(
                itineraries = listOf(
                    Itinerary(
                        id = "1",
                        title = "京都大阪 5 日遊",
                        description = "春季賞櫻行程",
                        startDate = LocalDate(2025, 4, 1),
                        endDate = LocalDate(2025, 4, 5),
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
                onAddClick = {},
                onHistoryClick = {},
                onRefresh = {}
            )
        }
    }
}

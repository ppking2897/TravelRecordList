@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.feature.memory.presentation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.ui.text.font.FontStyle
import com.example.myapplication.LocalSnackbarHostState
import com.example.myapplication.core.presentation.theme.CardStyle
import com.example.myapplication.core.presentation.theme.IconSize
import com.example.myapplication.core.presentation.theme.Spacing
import com.example.myapplication.core.presentation.theme.TravelRecordTheme
import com.example.myapplication.feature.itinerary.domain.model.Itinerary
import com.example.myapplication.feature.memory.domain.model.DailyHighlight
import com.example.myapplication.feature.memory.domain.model.MemoryStory
import com.example.myapplication.feature.memory.domain.model.TripMemory
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    modifier: Modifier = Modifier,
    viewModel: MemoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(Unit) {
        viewModel.handleIntent(MemoryIntent.Load)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is MemoryEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    MemoryScreenContent(
        state = state,
        modifier = modifier,
        onRefresh = { viewModel.handleIntent(MemoryIntent.Refresh) },
        onToggleExpanded = { viewModel.handleIntent(MemoryIntent.ToggleExpanded(it)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoryScreenContent(
    state: MemoryState,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onToggleExpanded: (String) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("回憶") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "重新整理")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingCenter(paddingValues)
            state.memories.isEmpty() -> EmptyMemoriesState(
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
                items(state.memories, key = { it.itinerary.id }) { memory ->
                    MemoryCard(
                        memory = memory,
                        expanded = state.expandedTripId == memory.itinerary.id,
                        onToggleExpanded = { onToggleExpanded(memory.itinerary.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryCard(
    memory: TripMemory,
    expanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(IconSize.sm)
                )
                Text(
                    text = memory.story.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = memory.itinerary.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            formatDateRange(memory.itinerary)?.let { dateText ->
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = memory.story.summary,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                StatChip(
                    icon = Icons.Default.Place,
                    label = "${memory.placeCount} 個地點"
                )
                StatChip(
                    icon = Icons.Default.PhotoLibrary,
                    label = "${memory.photoCount} 張照片"
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (expanded) "收合故事" else "展開故事與每日亮點",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "收合" else "展開"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        text = memory.story.narrative,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (memory.story.dailyHighlights.isNotEmpty()) {
                        Text(
                            text = "每日亮點",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        memory.story.dailyHighlights.forEach { highlight ->
                            DailyHighlightRow(highlight = highlight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(IconSize.xs),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DailyHighlightRow(highlight: DailyHighlight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = formatDate(highlight.date),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = highlight.headline,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${highlight.placeCount} 個地點",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyMemoriesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                modifier = Modifier.size(IconSize.xl),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "還沒有可以回味的旅程",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "等行程的結束日期過了之後，這裡會留下屬於你的故事。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingCenter(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private fun formatDateRange(itinerary: Itinerary): String? {
    val start = itinerary.startDate ?: return null
    val end = itinerary.endDate ?: return null
    return "${formatDate(start)} - ${formatDate(end)}"
}

private fun formatDate(date: LocalDate): String {
    return "${date.year}/${date.monthNumber.toString().padStart(2, '0')}/${date.dayOfMonth.toString().padStart(2, '0')}"
}

// ==================== Preview 假資料 ====================

@OptIn(ExperimentalTime::class)
private fun previewMemories(): List<TripMemory> {
    val now = Clock.System.now()
    val start = LocalDate(2026, 3, 10)
    val end = LocalDate(2026, 3, 14)
    val itinerary = Itinerary(
        id = "memory-preview",
        title = "京都大阪 5 日遊",
        description = "春天的旅程",
        startDate = start,
        endDate = end,
        createdAt = now,
        modifiedAt = now
    )
    val story = MemoryStory(
        title = "春天的 京都",
        summary = "3 月的 京都 之旅，走了 5 天、8 個地方。",
        narrative = "3 月 10 日，旅程從 伏見稲荷大社 展開。這 5 天裡走過 8 個地方，其中 6 處完成了打卡，其他留給下一次的回望。最後一站 日本環球影城 成了旅程的句點——相簿裡留下 24 張照片，足夠讓回憶細細展開。",
        dailyHighlights = listOf(
            DailyHighlight(start, "千本鳥居參拜", 2),
            DailyHighlight(LocalDate(2026, 3, 11), "參觀金閣寺", 2),
            DailyHighlight(LocalDate(2026, 3, 12), "餵鹿、東大寺參拜", 1),
            DailyHighlight(LocalDate(2026, 3, 13), "大阪城天守閣", 2),
            DailyHighlight(end, "環球影城一日遊", 1)
        )
    )
    return listOf(
        TripMemory(
            itinerary = itinerary,
            story = story,
            placeCount = 8,
            photoCount = 24,
            completedCount = 6,
            coverPhotoPath = null
        )
    )
}

@ExperimentalTime
@Preview
@Composable
private fun MemoryScreen_WithData_Preview() {
    TravelRecordTheme {
        Surface {
            MemoryScreenContent(
                state = MemoryState(memories = previewMemories()),
                onRefresh = {},
                onToggleExpanded = {}
            )
        }
    }
}

@ExperimentalTime
@Preview
@Composable
private fun MemoryScreen_Expanded_Preview() {
    TravelRecordTheme {
        Surface {
            MemoryScreenContent(
                state = MemoryState(
                    memories = previewMemories(),
                    expandedTripId = "memory-preview"
                ),
                onRefresh = {},
                onToggleExpanded = {}
            )
        }
    }
}

@ExperimentalTime
@Preview
@Composable
private fun MemoryScreen_Empty_Preview() {
    TravelRecordTheme {
        Surface {
            MemoryScreenContent(
                state = MemoryState(),
                onRefresh = {},
                onToggleExpanded = {}
            )
        }
    }
}

package io.github.ppking2897.travelrecord.feature.today.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ppking2897.travelrecord.LocalSnackbarHostState
import io.github.ppking2897.travelrecord.core.domain.service.LocationSearchService
import io.github.ppking2897.travelrecord.core.presentation.components.LocationSearchField
import io.github.ppking2897.travelrecord.core.presentation.components.LocationTag
import io.github.ppking2897.travelrecord.core.presentation.theme.CardStyle
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import io.github.ppking2897.travelrecord.core.presentation.theme.TravelRecordTheme
import io.github.ppking2897.travelrecord.core.presentation.util.DateTimeFormatter
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Photo
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    onOpenCurrentTrip: (String) -> Unit,
    onGoToPlan: () -> Unit,
    viewModel: TodayViewModel = koinViewModel(),
    locationSearchService: LocationSearchService = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var showQuickRecordSheet by remember { mutableStateOf(false) }
    var selectedItemIdForPhoto by remember { mutableStateOf<String?>(null) }

    val quickRecordImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Multiple(maxSelection = 3),
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.forEach { imageData ->
                viewModel.processIntent(TodayIntent.AddQuickPhoto(imageData))
            }
        }
    )

    val itemImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            val itemId = selectedItemIdForPhoto
            val imageData = byteArrays.firstOrNull()
            if (itemId != null && imageData != null) {
                viewModel.processIntent(TodayIntent.AddPhotoToItem(itemId, imageData))
            }
            selectedItemIdForPhoto = null
        }
    )

    LaunchedEffect(Unit) {
        viewModel.processIntent(TodayIntent.LoadToday)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is TodayEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    TodayScreenContent(
        state = state,
        modifier = modifier,
        onRefresh = { viewModel.processIntent(TodayIntent.Refresh) },
        onOpenCurrentTrip = onOpenCurrentTrip,
        onGoToPlan = onGoToPlan,
        onQuickRecord = { showQuickRecordSheet = true },
        onToggleComplete = { viewModel.processIntent(TodayIntent.ToggleItemCompletion(it)) },
        onAddPhoto = { itemId ->
            selectedItemIdForPhoto = itemId
            itemImagePicker.launch()
        },
        onEditNote = { viewModel.processIntent(TodayIntent.StartEditingNote(it)) }
    )

    if (showQuickRecordSheet) {
        QuickRecordSheet(
            state = state,
            onDismiss = { showQuickRecordSheet = false },
            onActivityChange = { viewModel.processIntent(TodayIntent.UpdateQuickActivity(it)) },
            onLocationChange = { viewModel.processIntent(TodayIntent.UpdateQuickLocation(it)) },
            onLocationSelected = { viewModel.processIntent(TodayIntent.SelectQuickLocation(it)) },
            onNotesChange = { viewModel.processIntent(TodayIntent.UpdateQuickNotes(it)) },
            onAddPhotos = { quickRecordImagePicker.launch() },
            onSave = { viewModel.processIntent(TodayIntent.SaveQuickRecord) },
            locationSearchService = locationSearchService
        )
    }

    if (state.editingNoteItemId != null) {
        NoteEditorDialog(
            value = state.editingNoteText,
            onValueChange = { viewModel.processIntent(TodayIntent.UpdateEditingNote(it)) },
            onDismiss = { viewModel.processIntent(TodayIntent.CancelEditingNote) },
            onSave = { viewModel.processIntent(TodayIntent.SaveEditingNote) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayScreenContent(
    state: TodayState,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onOpenCurrentTrip: (String) -> Unit,
    onGoToPlan: () -> Unit,
    onQuickRecord: () -> Unit,
    onToggleComplete: (String) -> Unit,
    onAddPhoto: (String) -> Unit,
    onEditNote: (String) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("今日") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "重新整理")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.currentTrip != null) {
                ExtendedFloatingActionButton(
                    onClick = onQuickRecord,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("快速紀錄") }
                )
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.currentTrip == null -> {
                NoCurrentTripState(
                    today = state.today,
                    onGoToPlan = onGoToPlan,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        start = Spacing.lg,
                        top = Spacing.md,
                        end = Spacing.lg,
                        bottom = 128.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    item {
                        TodayTripHeader(
                            trip = state.currentTrip,
                            today = state.today,
                            progress = state.progressText,
                            completedCount = state.completedCount,
                            totalCount = state.todayItems.size,
                            onOpenTrip = { onOpenCurrentTrip(state.currentTrip.id) }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Button(
                                onClick = onQuickRecord,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("新增現在")
                            }
                            OutlinedButton(
                                onClick = { onOpenCurrentTrip(state.currentTrip.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("看完整行程")
                            }
                        }
                    }

                    if (state.error != null) {
                        item {
                            Text(
                                text = state.error,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (state.todayItems.isEmpty()) {
                        item {
                            EmptyTodayItemsState(onQuickRecord = onQuickRecord)
                        }
                    } else {
                        items(state.todayItems, key = { it.id }) { item ->
                            TodayItemCard(
                                item = item,
                                onToggleComplete = { onToggleComplete(item.id) },
                                onAddPhoto = { onAddPhoto(item.id) },
                                onEditNote = { onEditNote(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayTripHeader(
    trip: Itinerary,
    today: LocalDate?,
    progress: String,
    completedCount: Int,
    totalCount: Int,
    onOpenTrip: () -> Unit
) {
    val progressValue = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(CardStyle.cornerRadius),
        border = BorderStroke(CardStyle.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(CardStyle.contentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = today?.let { "今天 ${DateTimeFormatter.formatDate(it)}" } ?: "今天",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = trip.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = progress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = onOpenTrip) {
                Text("開啟行程時間軸")
            }
        }
    }
}

@Composable
private fun TodayItemCard(
    item: ItineraryItem,
    onToggleComplete: () -> Unit,
    onAddPhoto: () -> Unit,
    onEditNote: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = CardStyle.listCardElevation),
        shape = RoundedCornerShape(CardStyle.cornerRadius),
        border = BorderStroke(CardStyle.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(CardStyle.contentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.primaryTime()?.let { DateTimeFormatter.formatTime(it) } ?: "未排時間",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = item.activity,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FilledTonalButton(onClick = onToggleComplete) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Text(if (item.isCompleted) "完成" else "打卡")
                }
            }

            LocationTag(location = item.location.name, icon = Icons.Default.Place)

            if (item.notes.isNotBlank()) {
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                OutlinedButton(onClick = onAddPhoto, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Text("照片 ${item.photos.size}")
                }
                OutlinedButton(onClick = onEditNote, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text("備註")
                }
            }
        }
    }
}

@Composable
private fun EmptyTodayItemsState(
    onQuickRecord: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(CardStyle.cornerRadius),
        border = BorderStroke(CardStyle.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(CardStyle.contentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("今天還沒有行程", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "可以先用快速紀錄，把現在去過的地方、照片和備註補上。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onQuickRecord) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("新增第一筆")
            }
        }
    }
}

@Composable
private fun NoCurrentTripState(
    today: LocalDate?,
    onGoToPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(Spacing.md))
        Text("今天沒有旅行中的行程", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = today?.let { "今天是 ${DateTimeFormatter.formatDate(it)}，目前沒有日期涵蓋今天的行程。" }
                ?: "目前沒有日期涵蓋今天的行程。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm)
        )
        Button(
            onClick = onGoToPlan,
            modifier = Modifier.padding(top = Spacing.lg)
        ) {
            Text("回行程規劃")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickRecordSheet(
    state: TodayState,
    onDismiss: () -> Unit,
    onActivityChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onLocationSelected: (io.github.ppking2897.travelrecord.core.domain.service.LocationSuggestion?) -> Unit,
    onNotesChange: (String) -> Unit,
    onAddPhotos: () -> Unit,
    onSave: () -> Unit,
    locationSearchService: LocationSearchService?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("快速紀錄", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "預設加入 ${state.today?.let { DateTimeFormatter.formatDate(it) } ?: "今天"} · ${state.currentTrip?.title.orEmpty()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = state.quickRecord.activity,
                onValueChange = onActivityChange,
                label = { Text("活動") },
                isError = state.quickRecord.activityError != null,
                supportingText = state.quickRecord.activityError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            LocationSearchField(
                value = state.quickRecord.locationName,
                onValueChange = onLocationChange,
                onLocationSelected = onLocationSelected,
                locationSearchService = locationSearchService,
                modifier = Modifier.fillMaxWidth(),
                label = "地點",
                placeholder = "輸入地點名稱搜尋..."
            )
            state.quickRecord.locationError?.let { errorText ->
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            state.quickRecord.locationAddress?.let { addressText ->
                Text(
                    text = addressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = state.quickRecord.notes,
                onValueChange = onNotesChange,
                label = { Text("備註") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            OutlinedButton(onClick = onAddPhotos) {
                Icon(Icons.Default.Image, contentDescription = null)
                Text("加入照片 ${state.quickRecord.photoPaths.size}")
            }
            Button(
                onClick = {
                    onSave()
                    if (state.quickRecord.activity.isNotBlank() && state.quickRecord.locationName.isNotBlank()) {
                        onDismiss()
                    }
                },
                enabled = !state.isSavingQuickRecord,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSavingQuickRecord) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                } else {
                    Text("儲存到今日")
                }
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("更新備註") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("儲存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// ==================== Preview 假資料 ====================

private val PreviewToday = LocalDate(2025, 4, 2)
private const val PreviewTripId = "preview-trip-id"

@OptIn(ExperimentalTime::class)
private fun previewItinerary(): Itinerary {
    val now = Clock.System.now()
    return Itinerary(
        id = PreviewTripId,
        title = "京都大阪 5 日遊",
        description = "春季賞櫻之旅",
        startDate = LocalDate(2025, 4, 1),
        endDate = LocalDate(2025, 4, 5),
        createdAt = now,
        modifiedAt = now
    )
}

@OptIn(ExperimentalTime::class)
private fun previewPhotos(itemId: String, count: Int): List<Photo> {
    val now = Clock.System.now()
    return List(count) { index ->
        Photo(
            id = "$itemId-photo-$index",
            itemId = itemId,
            fileName = "preview-$index.jpg",
            filePath = "",
            order = index,
            fileSize = 0L,
            uploadedAt = now,
            modifiedAt = now
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun previewTodayItems(): List<ItineraryItem> {
    val now = Clock.System.now()
    return listOf(
        ItineraryItem(
            id = "preview-item-1",
            itineraryId = PreviewTripId,
            date = PreviewToday,
            arrivalTime = LocalTime(9, 0),
            departureTime = LocalTime(12, 0),
            location = Location(name = "伏見稲荷大社"),
            activity = "千本鳥居參拜",
            notes = "人潮多，建議早一點到，穿好走的鞋。",
            photos = previewPhotos("preview-item-1", 2),
            isCompleted = true,
            completedAt = now,
            createdAt = now,
            modifiedAt = now
        ),
        ItineraryItem(
            id = "preview-item-2",
            itineraryId = PreviewTripId,
            date = PreviewToday,
            arrivalTime = LocalTime(14, 30),
            location = Location(name = "清水寺"),
            activity = "清水舞台賞景",
            notes = "",
            photos = previewPhotos("preview-item-2", 0),
            createdAt = now,
            modifiedAt = now
        ),
        ItineraryItem(
            id = "preview-item-3",
            itineraryId = PreviewTripId,
            date = PreviewToday,
            location = Location(name = "祇園 一蘭拉麵"),
            activity = "晚餐",
            notes = "需要排隊約 30 分鐘",
            photos = previewPhotos("preview-item-3", 1),
            createdAt = now,
            modifiedAt = now
        )
    )
}

@OptIn(ExperimentalTime::class)
private fun previewStateWithItems() = TodayState(
    isLoading = false,
    today = PreviewToday,
    currentTrip = previewItinerary(),
    todayItems = previewTodayItems()
)

@ExperimentalTime
@Preview
@Composable
private fun TodayScreen_WithItems_Preview() {
    TravelRecordTheme {
        Surface {
            TodayScreenContent(
                state = previewStateWithItems(),
                onRefresh = {},
                onOpenCurrentTrip = {},
                onGoToPlan = {},
                onQuickRecord = {},
                onToggleComplete = {},
                onAddPhoto = {},
                onEditNote = {}
            )
        }
    }
}

@ExperimentalTime
@Preview
@Composable
private fun TodayScreen_EmptyItems_Preview() {
    TravelRecordTheme {
        Surface {
            TodayScreenContent(
                state = previewStateWithItems().copy(todayItems = emptyList()),
                onRefresh = {},
                onOpenCurrentTrip = {},
                onGoToPlan = {},
                onQuickRecord = {},
                onToggleComplete = {},
                onAddPhoto = {},
                onEditNote = {}
            )
        }
    }
}

@ExperimentalTime
@Preview
@Composable
private fun TodayScreen_NoCurrentTrip_Preview() {
    TravelRecordTheme {
        Surface {
            TodayScreenContent(
                state = TodayState(today = PreviewToday),
                onRefresh = {},
                onOpenCurrentTrip = {},
                onGoToPlan = {},
                onQuickRecord = {},
                onToggleComplete = {},
                onAddPhoto = {},
                onEditNote = {}
            )
        }
    }
}

@ExperimentalTime
@Preview
@Composable
private fun TodayScreen_Loading_Preview() {
    TravelRecordTheme {
        Surface {
            TodayScreenContent(
                state = TodayState(isLoading = true, today = PreviewToday),
                onRefresh = {},
                onOpenCurrentTrip = {},
                onGoToPlan = {},
                onQuickRecord = {},
                onToggleComplete = {},
                onAddPhoto = {},
                onEditNote = {}
            )
        }
    }
}

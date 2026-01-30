package com.example.myapplication.presentation.add_edit_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.example.myapplication.domain.entity.Itinerary
import com.example.myapplication.domain.service.LocationSearchService
import com.example.myapplication.presentation.components.DateDropdown
import com.example.myapplication.presentation.components.LocalImage
import com.example.myapplication.presentation.components.LocationSearchField
import com.example.myapplication.presentation.components.SimplePhotoPreviewDialog
import com.example.myapplication.presentation.components.TimePickerDialog
import org.koin.compose.koinInject
import com.example.myapplication.presentation.add_edit_item.AddEditItemEvent
import com.example.myapplication.presentation.add_edit_item.AddEditItemIntent
import com.example.myapplication.presentation.add_edit_item.AddEditItemState
import com.example.myapplication.presentation.add_edit_item.AddEditItemViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Composable
fun AddEditItemScreen(
    itineraryId: String,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AddEditItemViewModel = koinViewModel(),
    locationSearchService: LocationSearchService = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(itineraryId) {
        viewModel.handleIntent(AddEditItemIntent.LoadItinerary(itineraryId))
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is AddEditItemEvent.SaveSuccess -> onSaveSuccess()
                is AddEditItemEvent.ShowError -> {
                    // Error is already shown in UI state, but can show toast here if needed
                }
            }
        }
    }

    AddEditItemScreenContent(
        state = state,
        onIntent = { viewModel.handleIntent(it) },
        onNavigateBack = onNavigateBack,
        locationSearchService = locationSearchService
    )
}

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreenContent(
    state: AddEditItemState,
    onIntent: (AddEditItemIntent) -> Unit,
    onNavigateBack: () -> Unit,
    locationSearchService: LocationSearchService? = null
) {
    var showArrivalTimePicker by remember { mutableStateOf(false) }
    var showDepartureTimePicker by remember { mutableStateOf(false) }
    var selectedPhotoForPreview by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val multipleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Multiple(maxSelection = 5),
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.forEach {
                onIntent(AddEditItemIntent.AddPhotoByContent(it))
            }
        }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增項目") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.activity,
                onValueChange = { onIntent(AddEditItemIntent.UpdateActivity(it)) },
                label = { Text("活動 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.activityError != null,
                supportingText = state.activityError?.let { { Text(it) } }
            )
            
            // 智慧地址輸入
            LocationSearchField(
                value = state.locationName,
                onValueChange = { onIntent(AddEditItemIntent.UpdateLocationName(it)) },
                onLocationSelected = { suggestion ->
                    onIntent(AddEditItemIntent.SelectLocation(suggestion))
                },
                locationSearchService = locationSearchService,
                modifier = Modifier.fillMaxWidth(),
                label = "地點名稱 *",
                placeholder = "輸入地點名稱搜尋..."
            )
            if (state.locationError != null) {
                Text(
                    text = state.locationError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 地址顯示（選擇地點後自動填入，或可手動輸入）
            OutlinedTextField(
                value = state.locationAddress,
                onValueChange = { onIntent(AddEditItemIntent.UpdateLocationAddress(it)) },
                label = { Text("地點地址") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = if (state.locationLatitude != null) {
                    { Text("座標: ${state.locationLatitude}, ${state.locationLongitude}") }
                } else null
            )
            
            // 日期選擇
            if (state.hasDateRange && state.itinerary != null) {
                DateDropdown(
                    dateRange = state.itinerary.startDate!!..state.itinerary.endDate!!,
                    selectedDate = state.selectedDate,
                    onDateSelected = { onIntent(AddEditItemIntent.UpdateDate(it)) },
                    label = "日期 *",
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.dateError != null) {
                    Text(
                        text = state.dateError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                OutlinedTextField(
                    value = state.selectedDate?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("日期 *") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    supportingText = { Text("請先在行程中設定日期範圍") }
                )
            }
            
            // 到達時間選擇
            OutlinedTextField(
                value = state.arrivalTime?.toString() ?: "",
                onValueChange = {},
                label = { Text("到達時間") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showArrivalTimePicker = true }) {
                        Text("選擇")
                    }
                }
            )
            
            // 離開時間選擇
            OutlinedTextField(
                value = state.departureTime?.toString() ?: "",
                onValueChange = {},
                label = { Text("離開時間") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDepartureTimePicker = true }) {
                        Text("選擇")
                    }
                }
            )
            
            // Photos Section
            Text(
                text = "照片",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (state.photos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                ) {
                    items(state.photos) { photoPath ->
                        Card(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { selectedPhotoForPreview = photoPath },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                LocalImage(
                                    filePath = photoPath,
                                    contentDescription = "照片",
                                    modifier = Modifier.fillMaxSize()
                                )
                                // 刪除按鈕
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp),
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                ) {
                                    IconButton(
                                        onClick = { onIntent(AddEditItemIntent.RemovePhoto(photoPath)) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "移除",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            OutlinedButton(
                onClick = { multipleImagePicker.launch() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("新增照片")
            }
            
            OutlinedTextField(
                value = state.notes,
                onValueChange = { onIntent(AddEditItemIntent.UpdateNotes(it)) },
                label = { Text("備註") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )
            
            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { onIntent(AddEditItemIntent.Save) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && state.hasDateRange
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("儲存")
                }
            }
        }
    }
    
    // 照片預覽 Dialog
    selectedPhotoForPreview?.let { photoPath ->
        SimplePhotoPreviewDialog(
            filePath = photoPath,
            onDismiss = { selectedPhotoForPreview = null },
            onDelete = {
                onIntent(AddEditItemIntent.RemovePhoto(photoPath))
                selectedPhotoForPreview = null
            }
        )
    }

    // 到達時間 TimePicker Dialog
    if (showArrivalTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showArrivalTimePicker = false },
            onConfirm = { time ->
                onIntent(AddEditItemIntent.UpdateArrivalTime(time))
                showArrivalTimePicker = false
            },
            initialTime = state.arrivalTime
        )
    }

    // 離開時間 TimePicker Dialog
    if (showDepartureTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showDepartureTimePicker = false },
            onConfirm = { time ->
                onIntent(AddEditItemIntent.UpdateDepartureTime(time))
                showDepartureTimePicker = false
            },
            initialTime = state.departureTime
        )
    }
}

// Preview
@Preview
@ExperimentalTime
@Composable
private fun AddEditItemScreenPreview() {
    MaterialTheme {
        Surface {
            AddEditItemScreenContent(
                state = AddEditItemState(
                    hasDateRange = true,
                    itinerary = Itinerary(
                        id = "1",
                        title = "Test",
                        startDate = kotlinx.datetime.LocalDate(2024, 1, 1),
                        endDate = kotlinx.datetime.LocalDate(2024, 1, 5),
                        createdAt = kotlin.time.Clock.System.now(),
                        modifiedAt = kotlin.time.Clock.System.now()
                    )
                ),
                onIntent = {},
                onNavigateBack = {},
                locationSearchService = null // Preview 不需要實際搜尋服務
            )
        }
    }
}
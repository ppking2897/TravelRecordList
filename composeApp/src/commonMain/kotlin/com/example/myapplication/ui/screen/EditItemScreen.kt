package com.example.myapplication.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.ui.component.DateDropdown
import com.example.myapplication.ui.component.LocalImage
import com.example.myapplication.ui.component.SimplePhotoPreviewDialog
import com.example.myapplication.ui.component.TimePickerDialog
import com.example.myapplication.ui.mvi.edititem.EditItemEvent
import com.example.myapplication.ui.mvi.edititem.EditItemIntent
import com.example.myapplication.ui.mvi.edititem.EditItemState
import com.example.myapplication.ui.mvi.edititem.EditItemViewModel
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Composable
fun EditItemScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditItemViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(itemId) {
        viewModel.handleIntent(EditItemIntent.LoadItem(itemId))
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is EditItemEvent.SaveSuccess -> onSaveSuccess()
                is EditItemEvent.ShowError -> {
                    // Error is already shown in UI state
                }
            }
        }
    }

    EditItemScreenContent(
        state = state,
        onIntent = { viewModel.handleIntent(it) },
        onNavigateBack = onNavigateBack
    )
}

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreenContent(
    state: EditItemState,
    onIntent: (EditItemIntent) -> Unit,
    onNavigateBack: () -> Unit
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
                onIntent(EditItemIntent.AddPhotoByContent(it))
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("編輯項目") },
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
                onValueChange = { onIntent(EditItemIntent.UpdateActivity(it)) },
                label = { Text("活動 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.activityError != null,
                supportingText = state.activityError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = state.locationName,
                onValueChange = { onIntent(EditItemIntent.UpdateLocationName(it)) },
                label = { Text("地點名稱 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.locationError != null,
                supportingText = state.locationError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = state.locationAddress,
                onValueChange = { onIntent(EditItemIntent.UpdateLocationAddress(it)) },
                label = { Text("地點地址") },
                modifier = Modifier.fillMaxWidth()
            )

            // 日期選擇
            if (state.hasDateRange && state.itinerary != null) {
                DateDropdown(
                    dateRange = state.itinerary.startDate!!..state.itinerary.endDate!!,
                    selectedDate = state.selectedDate,
                    onDateSelected = { onIntent(EditItemIntent.UpdateDate(it)) },
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
                                        onClick = { onIntent(EditItemIntent.RemovePhoto(photoPath)) },
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
                onValueChange = { onIntent(EditItemIntent.UpdateNotes(it)) },
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
                onClick = { onIntent(EditItemIntent.Save) },
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
                onIntent(EditItemIntent.RemovePhoto(photoPath))
                selectedPhotoForPreview = null
            }
        )
    }

    // 到達時間 TimePicker Dialog
    if (showArrivalTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showArrivalTimePicker = false },
            onConfirm = { time ->
                onIntent(EditItemIntent.UpdateArrivalTime(time))
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
                onIntent(EditItemIntent.UpdateDepartureTime(time))
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
private fun EditItemScreenPreview() {
    MaterialTheme {
        Surface {
            EditItemScreenContent(
                state = EditItemState(
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
                onNavigateBack = {}
            )
        }
    }
}
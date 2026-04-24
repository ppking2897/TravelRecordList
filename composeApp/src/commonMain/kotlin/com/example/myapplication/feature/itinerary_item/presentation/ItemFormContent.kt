package com.example.myapplication.feature.itinerary_item.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.domain.service.LocationSearchService
import com.example.myapplication.core.domain.service.LocationSuggestion
import com.example.myapplication.core.presentation.components.DateDropdown
import com.example.myapplication.core.presentation.components.LocalImage
import com.example.myapplication.core.presentation.components.LocationSearchField
import com.example.myapplication.core.presentation.components.SimplePhotoPreviewDialog
import com.example.myapplication.core.presentation.components.TimePickerDialog
import com.example.myapplication.core.presentation.util.DateTimeFormatter
import com.example.myapplication.feature.itinerary.domain.model.Itinerary
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.ExperimentalTime

/**
 * 行程項目表單共用內容
 *
 * 由 `AddEditItemScreen` 與 `EditItemScreen` 共用。不依賴任何 MVI Intent 型別，
 * 所有互動都以一般 Kotlin callback 傳入，由呼叫端負責轉成對應的 Intent。
 *
 * 內部管理局部 UI 狀態（TimePicker、照片預覽 Dialog），表單資料狀態由外部傳入。
 */
@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormContent(
    itinerary: Itinerary?,
    activity: String,
    activityError: String?,
    locationName: String,
    locationAddress: String,
    locationLatitude: Double?,
    locationLongitude: Double?,
    locationError: String?,
    selectedDate: LocalDate?,
    dateError: String?,
    hasDateRange: Boolean,
    arrivalTime: LocalTime?,
    departureTime: LocalTime?,
    photos: List<String>,
    notes: String,
    error: String?,
    isLoading: Boolean,
    locationSearchService: LocationSearchService?,
    onActivityChange: (String) -> Unit,
    onLocationNameChange: (String) -> Unit,
    onLocationSelected: (LocationSuggestion?) -> Unit,
    onLocationAddressChange: (String) -> Unit,
    onDateChange: (LocalDate?) -> Unit,
    onArrivalTimeChange: (LocalTime?) -> Unit,
    onDepartureTimeChange: (LocalTime?) -> Unit,
    onAddPhotoByContent: (ByteArray) -> Unit,
    onRemovePhoto: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateToEditItinerary: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var showArrivalTimePicker by remember { mutableStateOf(false) }
    var showDepartureTimePicker by remember { mutableStateOf(false) }
    var selectedPhotoForPreview by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val multipleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Multiple(maxSelection = 5),
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.forEach { onAddPhotoByContent(it) }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = activity,
            onValueChange = onActivityChange,
            label = { Text("活動 *") },
            modifier = Modifier.fillMaxWidth(),
            isError = activityError != null,
            supportingText = activityError?.let { { Text(it) } }
        )

        LocationSearchField(
            value = locationName,
            onValueChange = onLocationNameChange,
            onLocationSelected = onLocationSelected,
            locationSearchService = locationSearchService,
            modifier = Modifier.fillMaxWidth(),
            label = "地點名稱 *",
            placeholder = "輸入地點名稱搜尋..."
        )
        if (locationError != null) {
            Text(
                text = locationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (hasDateRange && itinerary != null) {
            DateDropdown(
                dateRange = itinerary.startDate!!..itinerary.endDate!!,
                selectedDate = selectedDate,
                onDateSelected = { onDateChange(it) },
                label = "日期 *",
                modifier = Modifier.fillMaxWidth()
            )
            if (dateError != null) {
                Text(
                    text = dateError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            OutlinedTextField(
                value = selectedDate?.toString() ?: "",
                onValueChange = {},
                label = { Text("日期 *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                supportingText = { Text("行程尚未設定日期範圍") }
            )
            if (onNavigateToEditItinerary != null) {
                TextButton(onClick = onNavigateToEditItinerary) {
                    Text("前往設定日期範圍")
                }
            }
        }

        HorizontalDivider()
        Text(
            text = "選填資訊",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = locationAddress,
            onValueChange = onLocationAddressChange,
            label = { Text("地點地址") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = if (locationLatitude != null) {
                { Text("座標: $locationLatitude, $locationLongitude") }
            } else null
        )

        OutlinedTextField(
            value = DateTimeFormatter.formatTime(arrivalTime),
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

        OutlinedTextField(
            value = DateTimeFormatter.formatTime(departureTime),
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

        Text(
            text = "照片",
            style = MaterialTheme.typography.titleMedium
        )

        if (photos.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                items(photos) { photoPath ->
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
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp),
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            ) {
                                IconButton(
                                    onClick = { onRemovePhoto(photoPath) }
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
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("備註") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 5
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && hasDateRange
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("儲存")
            }
        }
    }

    selectedPhotoForPreview?.let { photoPath ->
        SimplePhotoPreviewDialog(
            filePath = photoPath,
            onDismiss = { selectedPhotoForPreview = null },
            onDelete = {
                onRemovePhoto(photoPath)
                selectedPhotoForPreview = null
            }
        )
    }

    if (showArrivalTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showArrivalTimePicker = false },
            onConfirm = { time ->
                onArrivalTimeChange(time)
                showArrivalTimePicker = false
            },
            initialTime = arrivalTime
        )
    }

    if (showDepartureTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showDepartureTimePicker = false },
            onConfirm = { time ->
                onDepartureTimeChange(time)
                showDepartureTimePicker = false
            },
            initialTime = departureTime
        )
    }
}

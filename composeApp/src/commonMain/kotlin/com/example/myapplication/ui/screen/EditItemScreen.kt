package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.model.Location
import com.example.myapplication.domain.usecase.UpdateItineraryItemUseCase
import com.example.myapplication.ui.component.DateDropdown
import com.example.myapplication.ui.component.TimePickerDialog
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

/**
 * 編輯項目畫面
 * 
 * 用於編輯現有行程項目的資訊
 */
@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    item: ItineraryItem,
    itinerary: Itinerary,
    updateItemUseCase: UpdateItineraryItemUseCase,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var activity by remember { mutableStateOf(item.activity) }
    var locationName by remember { mutableStateOf(item.location.name) }
    var locationAddress by remember { mutableStateOf(item.location.address ?: "") }
    var notes by remember { mutableStateOf(item.notes) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(item.date) }
    var arrivalTime by remember { mutableStateOf<LocalTime?>(item.arrivalTime) }
    var departureTime by remember { mutableStateOf<LocalTime?>(item.departureTime) }
    var showArrivalTimePicker by remember { mutableStateOf(false) }
    var showDepartureTimePicker by remember { mutableStateOf(false) }
    
    var activityError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val hasDateRange = itinerary.startDate != null && itinerary.endDate != null
    
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = activity,
                onValueChange = {
                    activity = it
                    activityError = null
                },
                label = { Text("活動 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = activityError != null,
                supportingText = activityError?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = locationName,
                onValueChange = {
                    locationName = it
                    locationError = null
                },
                label = { Text("地點名稱 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = locationError != null,
                supportingText = locationError?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = locationAddress,
                onValueChange = { locationAddress = it },
                label = { Text("地點地址") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 日期選擇
            if (hasDateRange) {
                DateDropdown(
                    dateRange = itinerary.startDate!!..itinerary.endDate!!,
                    selectedDate = selectedDate,
                    onDateSelected = { 
                        selectedDate = it
                        dateError = null
                    },
                    label = "日期 *",
                    modifier = Modifier.fillMaxWidth()
                )
                if (dateError != null) {
                    Text(
                        text = dateError!!,
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
                    supportingText = { Text("請先在行程中設定日期範圍") }
                )
            }
            
            // 到達時間選擇
            OutlinedTextField(
                value = arrivalTime?.toString() ?: "",
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
                value = departureTime?.toString() ?: "",
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
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
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
                onClick = {
                    if (activity.isBlank()) {
                        activityError = "活動不可為空"
                        return@Button
                    }
                    
                    if (locationName.isBlank()) {
                        locationError = "地點名稱不可為空"
                        return@Button
                    }
                    
                    if (selectedDate == null) {
                        dateError = "請選擇日期"
                        return@Button
                    }
                    
                    val location = Location(
                        name = locationName,
                        latitude = item.location.latitude,
                        longitude = item.location.longitude,
                        address = locationAddress.ifBlank { null }
                    )
                    
                    val updatedItem = item.copy(
                        date = selectedDate!!,
                        arrivalTime = arrivalTime,
                        departureTime = departureTime,
                        location = location,
                        activity = activity,
                        notes = notes
                    )
                    
                    scope.launch {
                        isLoading = true
                        error = null
                        
                        updateItemUseCase(
                            item = updatedItem,
                            currentTimestamp = kotlin.time.Clock.System.now()
                        ).onSuccess {
                            onSaveSuccess()
                        }.onFailure { exception ->
                            error = exception.message ?: "儲存失敗"
                        }
                        
                        isLoading = false
                    }
                },
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
    }
    
    // 到達時間 TimePicker Dialog
    if (showArrivalTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showArrivalTimePicker = false },
            onConfirm = { time ->
                arrivalTime = time
                showArrivalTimePicker = false
            },
            initialTime = arrivalTime
        )
    }
    
    // 離開時間 TimePicker Dialog
    if (showDepartureTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showDepartureTimePicker = false },
            onConfirm = { time ->
                departureTime = time
                showDepartureTimePicker = false
            },
            initialTime = departureTime
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
                activity = "參觀淺草寺",
                locationName = "淺草寺",
                locationAddress = "東京都台東區",
                notes = "早上參觀",
                selectedDate = kotlinx.datetime.LocalDate(2024, 3, 15),
                selectedTime = kotlinx.datetime.LocalTime(9, 0),
                activityError = null,
                locationError = null,
                dateError = null,
                error = null,
                isLoading = false,
                hasDateRange = true,
                dateRange = kotlinx.datetime.LocalDate(2024, 3, 15)..kotlinx.datetime.LocalDate(2024, 3, 20),
                onActivityChange = {},
                onLocationNameChange = {},
                onLocationAddressChange = {},
                onNotesChange = {},
                onDateSelected = {},
                onTimeClick = {},
                onSave = {},
                onNavigateBack = {}
            )
        }
    }
}

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemScreenContent(
    activity: String,
    locationName: String,
    locationAddress: String,
    notes: String,
    selectedDate: LocalDate?,
    selectedTime: LocalTime?,
    activityError: String?,
    locationError: String?,
    dateError: String?,
    error: String?,
    isLoading: Boolean,
    hasDateRange: Boolean,
    dateRange: ClosedRange<LocalDate>?,
    onActivityChange: (String) -> Unit,
    onLocationNameChange: (String) -> Unit,
    onLocationAddressChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDateSelected: (LocalDate?) -> Unit,
    onTimeClick: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
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
                .padding(16.dp),
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
            
            OutlinedTextField(
                value = locationName,
                onValueChange = onLocationNameChange,
                label = { Text("地點名稱 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = locationError != null,
                supportingText = locationError?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = locationAddress,
                onValueChange = onLocationAddressChange,
                label = { Text("地點地址") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 日期選擇
            if (hasDateRange && dateRange != null) {
                DateDropdown(
                    dateRange = dateRange,
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
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
                    supportingText = { Text("請先在行程中設定日期範圍") }
                )
            }
            
            // 時間選擇
            OutlinedTextField(
                value = selectedTime?.toString() ?: "",
                onValueChange = {},
                label = { Text("時間") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = onTimeClick) {
                        Text("選擇")
                    }
                }
            )
            
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
    }
}

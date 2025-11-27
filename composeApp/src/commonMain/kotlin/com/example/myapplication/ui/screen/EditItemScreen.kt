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
import kotlin.time.ExperimentalTime

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
    var selectedTime by remember { mutableStateOf<LocalTime?>(item.time) }
    var showTimePicker by remember { mutableStateOf(false) }
    
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
            }
            
            OutlinedTextField(
                value = selectedTime?.toString() ?: "",
                onValueChange = {},
                label = { Text("時間") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Row {
                        if (selectedTime != null) {
                            TextButton(onClick = { selectedTime = null }) {
                                Text("清除")
                            }
                        }
                        TextButton(onClick = { showTimePicker = true }) {
                            Text("選擇")
                        }
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
                        activity = activity,
                        location = location,
                        date = selectedDate!!,
                        time = selectedTime,
                        notes = notes,
                        modifiedAt = kotlin.time.Clock.System.now()
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
                enabled = !isLoading
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
    
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = { time ->
                selectedTime = time
                showTimePicker = false
            },
            initialTime = selectedTime
        )
    }
}

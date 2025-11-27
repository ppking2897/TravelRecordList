@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.usecase.CreateItineraryUseCase
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Composable
fun AddEditItineraryScreen(
    createItineraryUseCase: CreateItineraryUseCase,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增行程") },
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
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = { Text("行程標題 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )
            
            OutlinedTextField(
                value = startDate?.toString() ?: "",
                onValueChange = {},
                label = { Text("開始日期") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showStartDatePicker = true }) {
                        Text("選擇")
                    }
                }
            )
            
            OutlinedTextField(
                value = endDate?.toString() ?: "",
                onValueChange = {},
                label = { Text("結束日期") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = dateError != null,
                supportingText = dateError?.let { { Text(it) } },
                trailingIcon = {
                    TextButton(onClick = { showEndDatePicker = true }) {
                        Text("選擇")
                    }
                }
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
                    if (title.isBlank()) {
                        titleError = "標題不可為空"
                        return@Button
                    }
                    
                    if (startDate != null && endDate != null && endDate!! < startDate!!) {
                        dateError = "結束日期不能早於開始日期"
                        return@Button
                    }
                    
                    scope.launch {
                        isLoading = true
                        error = null
                        dateError = null
                        
                        createItineraryUseCase(
                            title = title,
                            description = description,
                            startDate = startDate,
                            endDate = endDate,
                            currentTimestamp = kotlin.time.Clock.System.now()
                        ).onSuccess { itinerary ->
                            onSaveSuccess(itinerary.id)
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
    
    // Material3 DatePickerDialog for Start Date
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        startDate = instant.toLocalDateTime(TimeZone.UTC).date
                    }
                    showStartDatePicker = false
                }) {
                    Text("確定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Material3 DatePickerDialog for End Date
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        endDate = instant.toLocalDateTime(TimeZone.UTC).date
                    }
                    showEndDatePicker = false
                }) {
                    Text("確定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

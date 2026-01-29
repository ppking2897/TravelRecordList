@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.entity.DraftType
import com.example.myapplication.domain.entity.Itinerary
import com.example.myapplication.domain.repository.DraftRepository
import com.example.myapplication.domain.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.CreateItineraryUseCase
import com.example.myapplication.domain.usecase.LoadDraftUseCase
import com.example.myapplication.domain.usecase.SaveDraftUseCase
import com.example.myapplication.domain.usecase.UpdateItineraryUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Composable
fun AddEditItineraryScreen(
    itineraryId: String? = null,
    createItineraryUseCase: CreateItineraryUseCase,
    updateItineraryUseCase: UpdateItineraryUseCase? = null,
    itineraryRepository: ItineraryRepository? = null,
    saveDraftUseCase: SaveDraftUseCase? = null,
    loadDraftUseCase: LoadDraftUseCase? = null,
    draftRepository: DraftRepository? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (String) -> Unit
) {
    val isEditMode = itineraryId != null
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentItinerary by remember { mutableStateOf<Itinerary?>(null) }
    var showDraftSaved by remember { mutableStateOf(false) }

    // 載入草稿（僅新增模式）
    LaunchedEffect(Unit) {
        if (!isEditMode && loadDraftUseCase != null) {
            loadDraftUseCase(DraftType.ITINERARY).onSuccess { draftData ->
                draftData?.let {
                    title = it["title"] ?: ""
                    description = it["description"] ?: ""
                    it["startDate"]?.let { date ->
                        startDate = try {
                            LocalDate.parse(date)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    it["endDate"]?.let { date ->
                        endDate = try {
                            LocalDate.parse(date)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
            }
        }
    }

    // 載入現有行程資料（編輯模式）
    LaunchedEffect(itineraryId) {
        if (isEditMode && itineraryRepository != null) {
            itineraryRepository.getItinerary(itineraryId).onSuccess { itinerary ->
                itinerary?.let {
                    currentItinerary = it
                    title = it.title
                    description = it.description
                    startDate = it.startDate
                    endDate = it.endDate
                }
            }
        }
    }

    // 自動儲存草稿（僅新增模式，使用 debounce）
    LaunchedEffect(title, description, startDate, endDate) {
        if (!isEditMode && saveDraftUseCase != null) {
            delay(500) // debounce 500ms

            val draftData = buildMap {
                put("title", title)
                put("description", description)
                startDate?.let { put("startDate", it.toString()) }
                endDate?.let { put("endDate", it.toString()) }
            }

            saveDraftUseCase(
                DraftType.ITINERARY,
                draftData
            ).onSuccess {
                showDraftSaved = true
                delay(3000) // 3秒後隱藏
                showDraftSaved = false
            }
        }
    }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditMode) "編輯行程" else "新增行程") },
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

            // 草稿儲存指示器
            AnimatedVisibility(
                visible = showDraftSaved && !isEditMode,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Text(
                    text = "✓ 已自動儲存草稿",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
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

                        if (isEditMode && updateItineraryUseCase != null && currentItinerary != null) {
                            // 編輯模式
                            val updatedItinerary = currentItinerary!!.copy(
                                title = title,
                                description = description,
                                startDate = startDate,
                                endDate = endDate
                            )
                            updateItineraryUseCase(
                                itinerary = updatedItinerary,
                                currentTimestamp = kotlin.time.Clock.System.now()
                            ).onSuccess { itinerary ->
                                onSaveSuccess(itinerary.id)
                            }.onFailure { exception ->
                                error = exception.message ?: "儲存失敗"
                            }
                        } else {
                            // 新增模式
                            createItineraryUseCase(
                                title = title,
                                description = description,
                                startDate = startDate,
                                endDate = endDate,
                                currentTimestamp = kotlin.time.Clock.System.now()
                            ).onSuccess { itinerary ->
                                // 清除草稿
                                if (!isEditMode && draftRepository != null) {
                                    draftRepository.deleteDraft(DraftType.ITINERARY)
                                }
                                onSaveSuccess(itinerary.id)
                            }.onFailure { exception ->
                                error = exception.message ?: "儲存失敗"
                            }
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


// Preview
@Preview
@ExperimentalTime
@Composable
private fun AddEditItineraryScreenPreview() {
    MaterialTheme {
        Surface {
            AddEditItineraryScreenContent(
                isEditMode = false,
                title = "",
                description = "",
                startDate = null,
                endDate = null,
                titleError = null,
                dateError = null,
                error = null,
                isLoading = false,
                showDraftSaved = false,
                onTitleChange = {},
                onDescriptionChange = {},
                onStartDateClick = {},
                onEndDateClick = {},
                onSave = {},
                onNavigateBack = {}
            )
        }
    }
}

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditItineraryScreenContent(
    isEditMode: Boolean,
    title: String,
    description: String,
    startDate: LocalDate?,
    endDate: LocalDate?,
    titleError: String?,
    dateError: String?,
    error: String?,
    isLoading: Boolean,
    showDraftSaved: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "編輯行程" else "新增行程") },
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
                onValueChange = onTitleChange,
                label = { Text("行程標題 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
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
                    TextButton(onClick = onStartDateClick) {
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
                    TextButton(onClick = onEndDateClick) {
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

            // 草稿儲存指示器
            androidx.compose.animation.AnimatedVisibility(
                visible = showDraftSaved && !isEditMode,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Text(
                    text = "✓ 已自動儲存草稿",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSave,
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
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.feature.itinerary_add_edit.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.compose.ui.unit.dp
import com.example.myapplication.LocalSnackbarHostState
import com.example.myapplication.core.presentation.components.CoverPhotoSection
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Composable
fun ItineraryAddEditScreen(
    itineraryId: String? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (String) -> Unit,
    viewModel: ItineraryAddEditViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    // 初始化：編輯模式載入行程，新增模式載入草稿
    LaunchedEffect(itineraryId) {
        if (itineraryId != null) {
            viewModel.handleIntent(ItineraryAddEditIntent.LoadItinerary(itineraryId))
        } else {
            viewModel.handleIntent(ItineraryAddEditIntent.LoadDraft)
        }
    }

    // 監聽 Event
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ItineraryAddEditEvent.SaveSuccess -> onSaveSuccess(event.itineraryId)
                is ItineraryAddEditEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ItineraryAddEditEvent.DraftSaved -> { /* 已顯示在 state.showDraftSaved */ }
            }
        }
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // 新增模式下判斷是否有未儲存的變更
    val hasChanges = !state.isEditMode && (
        state.title.isNotEmpty() ||
        state.description.isNotEmpty() ||
        state.startDate != null ||
        state.endDate != null ||
        state.coverPhotoPath != null
    )

    val onBack: () -> Unit = {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    val backHandlerState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backHandlerState,
        isBackEnabled = hasChanges,
        onBackCompleted = { showDiscardDialog = true }
    )

    // 封面照片選擇器
    val coverImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            val imageData = byteArrays.firstOrNull()
            if (imageData != null) {
                scope.launch {
                    viewModel.handleIntent(ItineraryAddEditIntent.SetCoverPhoto(imageData))
                }
            }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.isEditMode) "編輯行程" else "新增行程") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            // 封面照片區塊
            CoverPhotoSection(
                coverPhotoPath = state.coverPhotoPath,
                onSelectPhoto = { coverImagePicker.launch() },
                onRemovePhoto = {
                    scope.launch {
                        viewModel.handleIntent(ItineraryAddEditIntent.RemoveCoverPhoto)
                    }
                }
            )

            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.handleIntent(ItineraryAddEditIntent.UpdateTitle(it)) },
                label = { Text("行程標題 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.handleIntent(ItineraryAddEditIntent.UpdateDescription(it)) },
                label = { Text("描述") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            OutlinedTextField(
                value = state.startDate?.toString() ?: "",
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
                value = state.endDate?.toString() ?: "",
                onValueChange = {},
                label = { Text("結束日期") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = state.dateError != null,
                supportingText = state.dateError?.let { { Text(it) } },
                trailingIcon = {
                    TextButton(onClick = { showEndDatePicker = true }) {
                        Text("選擇")
                    }
                }
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 草稿儲存指示器
            AnimatedVisibility(
                visible = state.showDraftSaved && !state.isEditMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "✓ 已自動儲存草稿",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.handleIntent(ItineraryAddEditIntent.Save) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
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

    // 離開未儲存確認 Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("捨棄變更？") },
            text = { Text("您有未儲存的變更，確定要離開嗎？") },
            confirmButton = {
                TextButton(onClick = { onNavigateBack() }) {
                    Text("捨棄")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("繼續編輯")
                }
            }
        )
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
                        val date = instant.toLocalDateTime(TimeZone.UTC).date
                        viewModel.handleIntent(ItineraryAddEditIntent.UpdateStartDate(date))
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
                        val date = instant.toLocalDateTime(TimeZone.UTC).date
                        viewModel.handleIntent(ItineraryAddEditIntent.UpdateEndDate(date))
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
private fun ItineraryAddEditScreenPreview() {
    MaterialTheme {
        Surface {
            ItineraryAddEditScreenContent(
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
private fun ItineraryAddEditScreenContent(
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
            CenterAlignedTopAppBar(
                title = { Text(if (isEditMode) "編輯行程" else "新增行程") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.ppking2897.travelrecord.feature.itinerary_add_edit.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.ppking2897.travelrecord.LocalSnackbarHostState
import io.github.ppking2897.travelrecord.core.presentation.theme.TravelRecordTheme
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.tooling.preview.Preview
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

    LaunchedEffect(itineraryId) {
        if (itineraryId != null) {
            viewModel.processIntent(ItineraryAddEditIntent.LoadItinerary(itineraryId))
        } else {
            viewModel.processIntent(ItineraryAddEditIntent.LoadDraft)
        }
    }

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

    val coverImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            val imageData = byteArrays.firstOrNull()
            if (imageData != null) {
                scope.launch {
                    viewModel.processIntent(ItineraryAddEditIntent.SetCoverPhoto(imageData))
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
        ItineraryFormContent(
            coverPhotoPath = state.coverPhotoPath,
            title = state.title,
            description = state.description,
            startDate = state.startDate,
            endDate = state.endDate,
            titleError = state.titleError,
            dateError = state.dateError,
            error = state.error,
            isLoading = state.isLoading,
            isEditMode = state.isEditMode,
            showDraftSaved = state.showDraftSaved,
            onTitleChange = { viewModel.processIntent(ItineraryAddEditIntent.UpdateTitle(it)) },
            onDescriptionChange = { viewModel.processIntent(ItineraryAddEditIntent.UpdateDescription(it)) },
            onStartDateClick = { showStartDatePicker = true },
            onEndDateClick = { showEndDatePicker = true },
            onSelectCoverPhoto = { coverImagePicker.launch() },
            onRemoveCoverPhoto = {
                scope.launch {
                    viewModel.processIntent(ItineraryAddEditIntent.RemoveCoverPhoto)
                }
            },
            onSave = { viewModel.processIntent(ItineraryAddEditIntent.Save) },
            contentPadding = paddingValues
        )
    }

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

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val date = instant.toLocalDateTime(TimeZone.UTC).date
                        viewModel.processIntent(ItineraryAddEditIntent.UpdateStartDate(date))
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

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val date = instant.toLocalDateTime(TimeZone.UTC).date
                        viewModel.processIntent(ItineraryAddEditIntent.UpdateEndDate(date))
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

@Preview
@ExperimentalTime
@Composable
private fun ItineraryAddEditScreenPreview() {
    TravelRecordTheme {
        Surface {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("新增行程") }
                    )
                }
            ) { paddingValues ->
                ItineraryFormContent(
                    coverPhotoPath = null,
                    title = "",
                    description = "",
                    startDate = null,
                    endDate = null,
                    titleError = null,
                    dateError = null,
                    error = null,
                    isLoading = false,
                    isEditMode = false,
                    showDraftSaved = false,
                    onTitleChange = {},
                    onDescriptionChange = {},
                    onStartDateClick = {},
                    onEndDateClick = {},
                    onSelectCoverPhoto = {},
                    onRemoveCoverPhoto = {},
                    onSave = {},
                    contentPadding = paddingValues
                )
            }
        }
    }
}

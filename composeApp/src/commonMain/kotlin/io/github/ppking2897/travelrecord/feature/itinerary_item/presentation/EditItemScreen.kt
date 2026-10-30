package io.github.ppking2897.travelrecord.feature.itinerary_item.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.ppking2897.travelrecord.LocalSnackbarHostState
import io.github.ppking2897.travelrecord.core.domain.service.LocationSearchService
import io.github.ppking2897.travelrecord.core.presentation.theme.TravelRecordTheme
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Composable
fun EditItemScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditItemViewModel = koinViewModel(),
    locationSearchService: LocationSearchService = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(itemId) {
        viewModel.processIntent(EditItemIntent.LoadItem(itemId))
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is EditItemEvent.SaveSuccess -> onSaveSuccess()
                is EditItemEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    var showDiscardDialog by remember { mutableStateOf(false) }

    val onBack: () -> Unit = {
        if (state.hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    val backHandlerState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backHandlerState,
        isBackEnabled = state.hasUnsavedChanges,
        onBackCompleted = { showDiscardDialog = true }
    )

    EditItemScreenContent(
        state = state,
        onIntent = { viewModel.processIntent(it) },
        onNavigateBack = onBack,
        locationSearchService = locationSearchService
    )

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("捨棄變更？") },
            text = { Text("您有未儲存的變更，確定要離開嗎？") },
            confirmButton = {
                // 先關掉對話框：換頁動畫期間對話框還在，連點兩下會多退一頁
                TextButton(onClick = { showDiscardDialog = false; onNavigateBack() }) {
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
}

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreenContent(
    state: EditItemState,
    onIntent: (EditItemIntent) -> Unit,
    onNavigateBack: () -> Unit,
    locationSearchService: LocationSearchService? = null
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("編輯項目") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        ItemFormContent(
            itinerary = state.itinerary,
            activity = state.activity,
            activityError = state.activityError,
            locationName = state.locationName,
            locationAddress = state.locationAddress,
            locationLatitude = state.locationLatitude,
            locationLongitude = state.locationLongitude,
            locationError = state.locationError,
            selectedDate = state.selectedDate,
            dateError = state.dateError,
            hasDateRange = state.hasDateRange,
            arrivalTime = state.arrivalTime,
            departureTime = state.departureTime,
            photos = state.photos,
            notes = state.notes,
            error = state.error,
            isLoading = state.isLoading,
            locationSearchService = locationSearchService,
            onActivityChange = { onIntent(EditItemIntent.UpdateActivity(it)) },
            onLocationNameChange = { onIntent(EditItemIntent.UpdateLocationName(it)) },
            onLocationSelected = { onIntent(EditItemIntent.SelectLocation(it)) },
            onLocationAddressChange = { onIntent(EditItemIntent.UpdateLocationAddress(it)) },
            onDateChange = { onIntent(EditItemIntent.UpdateDate(it)) },
            onArrivalTimeChange = { onIntent(EditItemIntent.UpdateArrivalTime(it)) },
            onDepartureTimeChange = { onIntent(EditItemIntent.UpdateDepartureTime(it)) },
            onAddPhotoByContent = { onIntent(EditItemIntent.AddPhotoByContent(it)) },
            onRemovePhoto = { onIntent(EditItemIntent.RemovePhoto(it)) },
            onNotesChange = { onIntent(EditItemIntent.UpdateNotes(it)) },
            onSave = { onIntent(EditItemIntent.Save) },
            contentPadding = paddingValues
        )
    }
}

@Preview
@ExperimentalTime
@Composable
private fun EditItemScreenPreview() {
    TravelRecordTheme {
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

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
import kotlinx.datetime.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Composable
fun AddEditItemScreen(
    itineraryId: String,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onNavigateToEditItinerary: (() -> Unit)? = null,
    defaultDate: LocalDate? = null,
    viewModel: AddEditItemViewModel = koinViewModel(),
    locationSearchService: LocationSearchService = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(itineraryId) {
        viewModel.handleIntent(AddEditItemIntent.LoadItinerary(itineraryId))
    }

    LaunchedEffect(defaultDate, state.itinerary) {
        if (defaultDate != null && state.itinerary != null && state.selectedDate == null) {
            viewModel.handleIntent(AddEditItemIntent.UpdateDate(defaultDate))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is AddEditItemEvent.SaveSuccess -> onSaveSuccess()
                is AddEditItemEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasChanges = state.activity.isNotEmpty() ||
        state.locationName.isNotEmpty() ||
        state.locationAddress.isNotEmpty() ||
        state.notes.isNotEmpty() ||
        state.arrivalTime != null ||
        state.departureTime != null ||
        state.photos.isNotEmpty()

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

    AddEditItemScreenContent(
        state = state,
        onIntent = { viewModel.handleIntent(it) },
        onNavigateBack = onBack,
        onNavigateToEditItinerary = onNavigateToEditItinerary,
        locationSearchService = locationSearchService
    )

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
}

@ExperimentalTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreenContent(
    state: AddEditItemState,
    onIntent: (AddEditItemIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditItinerary: (() -> Unit)? = null,
    locationSearchService: LocationSearchService? = null
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("新增項目") },
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
            onActivityChange = { onIntent(AddEditItemIntent.UpdateActivity(it)) },
            onLocationNameChange = { onIntent(AddEditItemIntent.UpdateLocationName(it)) },
            onLocationSelected = { onIntent(AddEditItemIntent.SelectLocation(it)) },
            onLocationAddressChange = { onIntent(AddEditItemIntent.UpdateLocationAddress(it)) },
            onDateChange = { onIntent(AddEditItemIntent.UpdateDate(it)) },
            onArrivalTimeChange = { onIntent(AddEditItemIntent.UpdateArrivalTime(it)) },
            onDepartureTimeChange = { onIntent(AddEditItemIntent.UpdateDepartureTime(it)) },
            onAddPhotoByContent = { onIntent(AddEditItemIntent.AddPhotoByContent(it)) },
            onRemovePhoto = { onIntent(AddEditItemIntent.RemovePhoto(it)) },
            onNotesChange = { onIntent(AddEditItemIntent.UpdateNotes(it)) },
            onSave = { onIntent(AddEditItemIntent.Save) },
            onNavigateToEditItinerary = onNavigateToEditItinerary,
            contentPadding = paddingValues
        )
    }
}

@Preview
@ExperimentalTime
@Composable
private fun AddEditItemScreenPreview() {
    TravelRecordTheme {
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
                locationSearchService = null
            )
        }
    }
}

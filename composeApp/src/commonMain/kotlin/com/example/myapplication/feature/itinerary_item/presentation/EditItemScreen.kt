package com.example.myapplication.feature.itinerary_item.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.LocalSnackbarHostState
import com.example.myapplication.core.domain.service.LocationSearchService
import com.example.myapplication.feature.itinerary.domain.model.Itinerary
import org.jetbrains.compose.ui.tooling.preview.Preview
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
        viewModel.handleIntent(EditItemIntent.LoadItem(itemId))
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

    EditItemScreenContent(
        state = state,
        onIntent = { viewModel.handleIntent(it) },
        onNavigateBack = onNavigateBack,
        locationSearchService = locationSearchService
    )
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

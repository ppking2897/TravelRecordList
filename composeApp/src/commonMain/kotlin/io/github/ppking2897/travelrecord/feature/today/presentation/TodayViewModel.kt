@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.today.presentation

import androidx.lifecycle.viewModelScope
import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.core.presentation.mvi.BaseViewModel
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetAllItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddPhotoUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GenerateThumbnailUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.UpdateItineraryItemUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TodayViewModel(
    private val getAllItinerariesUseCase: GetAllItinerariesUseCase,
    private val getItemsByItineraryUseCase: GetItemsByItineraryUseCase,
    private val addItineraryItemUseCase: AddItineraryItemUseCase,
    private val updateItineraryItemUseCase: UpdateItineraryItemUseCase,
    private val addPhotoUseCase: AddPhotoUseCase,
    private val generateThumbnailUseCase: GenerateThumbnailUseCase,
    private val imageStorageService: ImageStorageService
) : BaseViewModel<TodayState, TodayIntent, TodayEvent>(
    initialState = TodayState()
) {
    override suspend fun processIntent(intent: TodayIntent) {
        when (intent) {
            TodayIntent.LoadToday -> loadToday()
            TodayIntent.Refresh -> loadToday()
            is TodayIntent.UpdateQuickActivity -> updateQuickActivity(intent.value)
            is TodayIntent.UpdateQuickLocation -> updateQuickLocation(intent.value)
            is TodayIntent.SelectQuickLocation -> selectQuickLocation(intent.suggestion)
            is TodayIntent.UpdateQuickNotes -> updateQuickNotes(intent.value)
            is TodayIntent.AddQuickPhoto -> addQuickPhoto(intent.imageData)
            TodayIntent.SaveQuickRecord -> saveQuickRecord()
            is TodayIntent.ToggleItemCompletion -> toggleItemCompletion(intent.itemId)
            is TodayIntent.AddPhotoToItem -> addPhotoToItem(intent.itemId, intent.imageData)
            is TodayIntent.StartEditingNote -> startEditingNote(intent.itemId)
            is TodayIntent.UpdateEditingNote -> updateEditingNote(intent.value)
            TodayIntent.SaveEditingNote -> saveEditingNote()
            TodayIntent.CancelEditingNote -> cancelEditingNote()
        }
    }

    private suspend fun loadToday() {
        val today = currentLocalDate()
        updateState { copy(isLoading = true, error = null, today = today) }

        getAllItinerariesUseCase()
            .onSuccess { itineraries ->
                val currentTrip = findCurrentTrip(itineraries, today)
                if (currentTrip == null) {
                    updateState {
                        copy(
                            isLoading = false,
                            currentTrip = null,
                            todayItems = emptyList(),
                            today = today
                        )
                    }
                    return
                }

                getItemsByItineraryUseCase(currentTrip.id)
                    .onSuccess { items ->
                        updateState {
                            copy(
                                isLoading = false,
                                currentTrip = currentTrip,
                                todayItems = items
                                    .filter { it.date == today }
                                    .sortedWith(compareBy({ it.primaryTime() }, { it.createdAt })),
                                today = today
                            )
                        }
                    }
                    .onFailure { exception ->
                        updateState {
                            copy(
                                isLoading = false,
                                currentTrip = currentTrip,
                                todayItems = emptyList(),
                                error = exception.message
                            )
                        }
                    }
            }
            .onFailure { exception ->
                updateState {
                    copy(
                        isLoading = false,
                        currentTrip = null,
                        todayItems = emptyList(),
                        error = exception.message
                    )
                }
            }
    }

    private fun updateQuickActivity(value: String) {
        updateState {
            copy(
                quickRecord = quickRecord.copy(
                    activity = value,
                    activityError = null
                )
            )
        }
    }

    private fun updateQuickLocation(value: String) {
        updateState {
            // 手動輸入代表使用者脫離上次選到的建議，清掉 address / lat / lng
            copy(
                quickRecord = quickRecord.copy(
                    locationName = value,
                    locationAddress = null,
                    locationLatitude = null,
                    locationLongitude = null,
                    locationError = null
                )
            )
        }
    }

    private fun selectQuickLocation(suggestion: io.github.ppking2897.travelrecord.core.domain.service.LocationSuggestion?) {
        updateState {
            if (suggestion == null) {
                copy(
                    quickRecord = quickRecord.copy(
                        locationAddress = null,
                        locationLatitude = null,
                        locationLongitude = null
                    )
                )
            } else {
                copy(
                    quickRecord = quickRecord.copy(
                        locationName = suggestion.name,
                        locationAddress = suggestion.address,
                        locationLatitude = suggestion.latitude,
                        locationLongitude = suggestion.longitude,
                        locationError = null
                    )
                )
            }
        }
    }

    private fun updateQuickNotes(value: String) {
        updateState {
            copy(quickRecord = quickRecord.copy(notes = value))
        }
    }

    private suspend fun addQuickPhoto(imageData: ByteArray) {
        val tempId = Uuid.random().toString()
        imageStorageService.saveImage(imageData, tempId)
            .onSuccess { path ->
                updateState {
                    copy(quickRecord = quickRecord.copy(photoPaths = quickRecord.photoPaths + path))
                }
                sendEvent(TodayEvent.ShowMessage("已加入照片"))
            }
            .onFailure { exception ->
                sendEvent(TodayEvent.ShowMessage(exception.message ?: "照片加入失敗"))
            }
    }

    private suspend fun saveQuickRecord() {
        val snapshot = currentState
        val trip = snapshot.currentTrip ?: run {
            sendEvent(TodayEvent.ShowMessage("目前沒有旅行中的行程"))
            return
        }
        val today = snapshot.today ?: currentLocalDate()
        val quickRecord = snapshot.quickRecord

        if (quickRecord.activity.isBlank()) {
            updateState {
                copy(quickRecord = quickRecord.copy(activityError = "請輸入活動"))
            }
            return
        }

        if (quickRecord.locationName.isBlank()) {
            updateState {
                copy(quickRecord = quickRecord.copy(locationError = "請輸入地點"))
            }
            return
        }

        updateState { copy(isSavingQuickRecord = true, error = null) }

        addItineraryItemUseCase(
            itineraryId = trip.id,
            date = today,
            arrivalTime = currentLocalTime(),
            location = Location(
                name = quickRecord.locationName,
                latitude = quickRecord.locationLatitude,
                longitude = quickRecord.locationLongitude,
                address = quickRecord.locationAddress
            ),
            activity = quickRecord.activity,
            notes = quickRecord.notes,
            photoPaths = quickRecord.photoPaths,
            currentTimestamp = Clock.System.now()
        )
            .onSuccess {
                updateState {
                    copy(
                        isSavingQuickRecord = false,
                        quickRecord = QuickRecordState()
                    )
                }
                loadToday()
                sendEvent(TodayEvent.ShowMessage("已加入今日紀錄"))
            }
            .onFailure { exception ->
                updateState { copy(isSavingQuickRecord = false, error = exception.message) }
                sendEvent(TodayEvent.ShowMessage(exception.message ?: "新增紀錄失敗"))
            }
    }

    private suspend fun toggleItemCompletion(itemId: String) {
        val item = currentState.todayItems.find { it.id == itemId } ?: return
        val updated = item.copy(
            isCompleted = !item.isCompleted,
            completedAt = if (item.isCompleted) null else Clock.System.now()
        )

        updateState {
            copy(todayItems = todayItems.map { if (it.id == itemId) updated else it })
        }

        updateItineraryItemUseCase(updated, Clock.System.now())
            .onFailure { exception ->
                updateState {
                    copy(todayItems = todayItems.map { if (it.id == itemId) item else it })
                }
                sendEvent(TodayEvent.ShowMessage(exception.message ?: "更新完成狀態失敗"))
            }
    }

    private suspend fun addPhotoToItem(itemId: String, imageData: ByteArray) {
        addPhotoUseCase(itemId, imageData)
            .onSuccess { photo ->
                loadToday()
                viewModelScope.launch(Dispatchers.Default) {
                    generateThumbnailUseCase(photo)
                }
                sendEvent(TodayEvent.ShowMessage("已加入照片"))
            }
            .onFailure { exception ->
                sendEvent(TodayEvent.ShowMessage(exception.message ?: "照片加入失敗"))
            }
    }

    private fun startEditingNote(itemId: String) {
        val item = currentState.todayItems.find { it.id == itemId } ?: return
        updateState {
            copy(
                editingNoteItemId = itemId,
                editingNoteText = item.notes
            )
        }
    }

    private fun updateEditingNote(value: String) {
        updateState { copy(editingNoteText = value) }
    }

    private suspend fun saveEditingNote() {
        val itemId = currentState.editingNoteItemId ?: return
        val item = currentState.todayItems.find { it.id == itemId } ?: return
        val updated = item.copy(notes = currentState.editingNoteText)

        updateItineraryItemUseCase(updated, Clock.System.now())
            .onSuccess {
                updateState {
                    copy(
                        todayItems = todayItems.map { if (it.id == itemId) updated else it },
                        editingNoteItemId = null,
                        editingNoteText = ""
                    )
                }
                sendEvent(TodayEvent.ShowMessage("備註已更新"))
            }
            .onFailure { exception ->
                sendEvent(TodayEvent.ShowMessage(exception.message ?: "更新備註失敗"))
            }
    }

    private fun cancelEditingNote() {
        updateState {
            copy(
                editingNoteItemId = null,
                editingNoteText = ""
            )
        }
    }

    private fun findCurrentTrip(itineraries: List<Itinerary>, today: LocalDate): Itinerary? {
        return itineraries
            .filter { itinerary ->
                val startDate = itinerary.startDate
                val endDate = itinerary.endDate
                startDate != null && endDate != null && today in startDate..endDate
            }
            .minByOrNull { it.endDate.toString() }
    }

    private fun currentLocalDate(): LocalDate {
        return Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }

    private fun currentLocalTime() =
        Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time
}

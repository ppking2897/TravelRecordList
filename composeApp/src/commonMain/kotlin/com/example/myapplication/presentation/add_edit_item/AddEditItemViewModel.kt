@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.myapplication.presentation.add_edit_item

import com.example.myapplication.domain.entity.Itinerary
import com.example.myapplication.domain.entity.Location
import com.example.myapplication.domain.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.AddItineraryItemUseCase
import com.example.myapplication.presentation.mvi.BaseViewModel
import com.example.myapplication.data.storage.ImageStorageService
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * AddEditItem 畫面的 ViewModel
 * 
 * 負責處理行程項目新增相關的業務邏輯
 */
@OptIn(ExperimentalUuidApi::class)
class AddEditItemViewModel(
    private val addItemUseCase: AddItineraryItemUseCase,
    private val itineraryRepository: ItineraryRepository,
    private val imageStorageService: ImageStorageService
) : BaseViewModel<AddEditItemState, AddEditItemIntent, AddEditItemEvent>(
    initialState = AddEditItemState()
) {
    
    override suspend fun processIntent(intent: AddEditItemIntent) {
        when (intent) {
            is AddEditItemIntent.LoadItinerary -> loadItinerary(intent.itineraryId)
            is AddEditItemIntent.Initialize -> initialize(intent.itinerary)
            is AddEditItemIntent.UpdateActivity -> updateActivity(intent.activity)
            is AddEditItemIntent.UpdateLocationName -> updateLocationName(intent.name)
            is AddEditItemIntent.UpdateLocationAddress -> updateLocationAddress(intent.address)
            is AddEditItemIntent.UpdateNotes -> updateNotes(intent.notes)
            is AddEditItemIntent.UpdateDate -> updateDate(intent.date)
            is AddEditItemIntent.UpdateArrivalTime -> updateArrivalTime(intent.time)
            is AddEditItemIntent.UpdateDepartureTime -> updateDepartureTime(intent.time)
            is AddEditItemIntent.AddPhoto -> addPhoto(intent.path)
            is AddEditItemIntent.AddPhotoByContent -> addPhotoByContent(intent.content)
            is AddEditItemIntent.RemovePhoto -> removePhoto(intent.path)
            is AddEditItemIntent.Save -> save()
        }
    }

    private suspend fun loadItinerary(id: String) {
        updateState { copy(isLoading = true, error = null) }
        itineraryRepository.getItinerary(id)
            .onSuccess { itinerary ->
                if (itinerary != null) {
                    initialize(itinerary)
                } else {
                    updateState { copy(isLoading = false, error = "找不到行程") }
                }
            }
            .onFailure { e ->
                updateState { copy(isLoading = false, error = e.message) }
            }
    }
    
    /**
     * 初始化（設定行程資訊）
     */
    private fun initialize(itinerary: Itinerary) {
        val hasDateRange = itinerary.startDate != null && itinerary.endDate != null
        updateState {
            copy(
                isLoading = false,
                itinerary = itinerary,
                hasDateRange = hasDateRange
            )
        }
    }
    
    /**
     * 更新活動
     */
    private fun updateActivity(activity: String) {
        updateState {
            copy(
                activity = activity,
                activityError = null
            )
        }
    }
    
    /**
     * 更新地點名稱
     */
    private fun updateLocationName(name: String) {
        updateState {
            copy(
                locationName = name,
                locationError = null
            )
        }
    }
    
    /**
     * 更新地點地址
     */
    private fun updateLocationAddress(address: String) {
        updateState { copy(locationAddress = address) }
    }
    
    /**
     * 更新備註
     */
    private fun updateNotes(notes: String) {
        updateState { copy(notes = notes) }
    }
    
    /**
     * 更新日期
     */
    private fun updateDate(date: LocalDate?) {
        updateState {
            copy(
                selectedDate = date,
                dateError = null
            )
        }
    }
    
    /**
     * 更新到達時間
     */
    private fun updateArrivalTime(time: LocalTime?) {
        updateState { copy(arrivalTime = time) }
    }
    
    /**
     * 更新離開時間
     */
    private fun updateDepartureTime(time: LocalTime?) {
        updateState { copy(departureTime = time) }
    }
    
    /**
     * 新增照片
     */
    private fun addPhoto(path: String) {
        updateState {
            copy(photos = photos + path)
        }
    }

    /**
     * 從內容新增照片
     */
    private suspend fun addPhotoByContent(content: ByteArray) {
        // 使用隨機 UUID 作為暫時的 itemId
        val tempId = Uuid.random().toString()
        imageStorageService.saveImage(content, tempId)
            .onSuccess { path ->
                updateState {
                    copy(photos = photos + path)
                }
            }
            .onFailure {
                // Handle save error if needed
            }
    }
    
    /**
     * 移除照片
     */
    private fun removePhoto(path: String) {
        updateState {
            copy(photos = photos - path)
        }
    }
    
    /**
     * 儲存項目
     */
    private suspend fun save() {
        val snapshot = currentState
        
        // 驗證
        if (snapshot.activity.isBlank()) {
            updateState { copy(activityError = "活動不可為空") }
            return
        }
        
        if (snapshot.locationName.isBlank()) {
            updateState { copy(locationError = "地點名稱不可為空") }
            return
        }
        
        if (snapshot.selectedDate == null) {
            updateState { copy(dateError = "請選擇日期") }
            return
        }
        
        if (snapshot.itinerary == null) {
            updateState { copy(error = "行程資訊遺失") }
            return
        }
        
        updateState { copy(isLoading = true, error = null) }
        
        val location = Location(
            name = snapshot.locationName,
            latitude = null,
            longitude = null,
            address = snapshot.locationAddress.ifBlank { null }
        )
        
        addItemUseCase(
            itineraryId = snapshot.itinerary.id,
            date = snapshot.selectedDate,
            arrivalTime = snapshot.arrivalTime,
            departureTime = snapshot.departureTime,
            location = location,
            activity = snapshot.activity,
            notes = snapshot.notes,
            photoPaths = snapshot.photos,
            currentTimestamp = Clock.System.now()
        )
            .onSuccess {
                updateState { copy(isLoading = false) }
                sendEvent(AddEditItemEvent.SaveSuccess)
            }
            .onFailure { exception ->
                updateState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "儲存失敗"
                    )
                }
                sendEvent(AddEditItemEvent.ShowError(exception.message ?: "儲存失敗"))
            }
    }
}

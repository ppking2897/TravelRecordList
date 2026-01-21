@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.myapplication.ui.mvi.edititem

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.model.Location
import com.example.myapplication.data.repository.ItineraryItemRepository
import com.example.myapplication.data.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.UpdateItineraryItemUseCase
import com.example.myapplication.ui.mvi.BaseViewModel
import com.example.myapplication.data.storage.ImageStorageService
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * EditItem 畫面的 ViewModel
 * 
 * 負責處理行程項目編輯相關的業務邏輯
 */
@OptIn(ExperimentalUuidApi::class)
class EditItemViewModel(
    private val updateItemUseCase: UpdateItineraryItemUseCase,
    private val itemRepository: ItineraryItemRepository,
    private val itineraryRepository: ItineraryRepository,
    private val imageStorageService: ImageStorageService
) : BaseViewModel<EditItemState, EditItemIntent, EditItemEvent>(
    initialState = EditItemState()
) {
    
    override suspend fun processIntent(intent: EditItemIntent) {
        when (intent) {
            is EditItemIntent.LoadItem -> loadItem(intent.itemId)
            is EditItemIntent.Initialize -> initialize(intent.item, intent.itinerary)
            is EditItemIntent.UpdateActivity -> updateActivity(intent.activity)
            is EditItemIntent.UpdateLocationName -> updateLocationName(intent.name)
            is EditItemIntent.UpdateLocationAddress -> updateLocationAddress(intent.address)
            is EditItemIntent.UpdateNotes -> updateNotes(intent.notes)
            is EditItemIntent.UpdateDate -> updateDate(intent.date)
            is EditItemIntent.UpdateArrivalTime -> updateArrivalTime(intent.time)
            is EditItemIntent.UpdateDepartureTime -> updateDepartureTime(intent.time)
            is EditItemIntent.AddPhoto -> addPhoto(intent.path)
            is EditItemIntent.AddPhotoByContent -> addPhotoByContent(intent.content)
            is EditItemIntent.RemovePhoto -> removePhoto(intent.path)
            is EditItemIntent.Save -> save()
        }
    }

    private suspend fun loadItem(itemId: String) {
        updateState { copy(isLoading = true, error = null) }
        
        itemRepository.getItem(itemId)
            .onSuccess { item ->
                if (item != null) {
                    // 載入項目成功後，載入所屬行程以獲取日期範圍
                    itineraryRepository.getItinerary(item.itineraryId)
                        .onSuccess { itinerary ->
                            if (itinerary != null) {
                                initialize(item, itinerary)
                            } else {
                                updateState { copy(isLoading = false, error = "找不到所屬行程") }
                            }
                        }
                        .onFailure { e ->
                            updateState { copy(isLoading = false, error = e.message) }
                        }
                } else {
                    updateState { copy(isLoading = false, error = "找不到項目") }
                }
            }
            .onFailure { e ->
                updateState { copy(isLoading = false, error = e.message) }
            }
    }
    
    /**
     * 初始化（載入項目資料）
     */
    private fun initialize(item: ItineraryItem, itinerary: Itinerary) {
        val hasDateRange = itinerary.startDate != null && itinerary.endDate != null
        updateState {
            copy(
                isLoading = false,
                item = item,
                itinerary = itinerary,
                activity = item.activity,
                locationName = item.location.name,
                locationAddress = item.location.address ?: "",
                notes = item.notes,
                selectedDate = item.date,
                arrivalTime = item.arrivalTime,
                departureTime = item.departureTime,
                hasDateRange = hasDateRange,
                photos = item.photos.map { it.filePath }
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
        val itemId = currentState.item?.id ?: Uuid.random().toString()
        imageStorageService.saveImage(content, itemId)
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
        
        if (snapshot.item == null) {
            updateState { copy(error = "項目資訊遺失") }
            return
        }
        
        updateState { copy(isLoading = true, error = null) }
        
        val location = Location(
            name = snapshot.locationName,
            latitude = snapshot.item.location.latitude,
            longitude = snapshot.item.location.longitude,
            address = snapshot.locationAddress.ifBlank { null }
        )

        // 建立照片物件
        val currentTimestamp = Clock.System.now()
        val photos = snapshot.photos.mapIndexed { index, path ->
            com.example.myapplication.data.model.Photo(
                id = snapshot.item.photos.find { it.filePath == path }?.id ?: Uuid.random().toString(),
                itemId = snapshot.item.id,
                fileName = path.substringAfterLast('/'),
                filePath = path,
                order = index,
                fileSize = 0L,
                uploadedAt = currentTimestamp,
                modifiedAt = currentTimestamp
            )
        }
        
        val updatedItem = snapshot.item.copy(
            date = snapshot.selectedDate,
            arrivalTime = snapshot.arrivalTime,
            departureTime = snapshot.departureTime,
            location = location,
            activity = snapshot.activity,
            notes = snapshot.notes,
            photos = photos
        )
        
        updateItemUseCase(
            item = updatedItem,
            currentTimestamp = currentTimestamp
        )
            .onSuccess {
                updateState { copy(isLoading = false) }
                sendEvent(EditItemEvent.SaveSuccess)
            }
            .onFailure { exception ->
                updateState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "儲存失敗"
                    )
                }
                sendEvent(EditItemEvent.ShowError(exception.message ?: "儲存失敗"))
            }
    }
}

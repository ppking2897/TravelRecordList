@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.myapplication.ui.mvi.edititem

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.model.Location
import com.example.myapplication.domain.usecase.UpdateItineraryItemUseCase
import com.example.myapplication.ui.mvi.BaseViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Clock

/**
 * EditItem 畫面的 ViewModel
 * 
 * 負責處理行程項目編輯相關的業務邏輯
 */
class EditItemViewModel(
    private val updateItemUseCase: UpdateItineraryItemUseCase
) : BaseViewModel<EditItemState, EditItemIntent, EditItemEvent>(
    initialState = EditItemState()
) {
    
    override suspend fun processIntent(intent: EditItemIntent) {
        when (intent) {
            is EditItemIntent.Initialize -> initialize(intent.item, intent.itinerary)
            is EditItemIntent.UpdateActivity -> updateActivity(intent.activity)
            is EditItemIntent.UpdateLocationName -> updateLocationName(intent.name)
            is EditItemIntent.UpdateLocationAddress -> updateLocationAddress(intent.address)
            is EditItemIntent.UpdateNotes -> updateNotes(intent.notes)
            is EditItemIntent.UpdateDate -> updateDate(intent.date)
            is EditItemIntent.UpdateArrivalTime -> updateArrivalTime(intent.time)
            is EditItemIntent.UpdateDepartureTime -> updateDepartureTime(intent.time)
            is EditItemIntent.Save -> save()
        }
    }
    
    /**
     * 初始化（載入項目資料）
     */
    private fun initialize(item: ItineraryItem, itinerary: Itinerary) {
        val hasDateRange = itinerary.startDate != null && itinerary.endDate != null
        updateState {
            copy(
                item = item,
                itinerary = itinerary,
                activity = item.activity,
                locationName = item.location.name,
                locationAddress = item.location.address ?: "",
                notes = item.notes,
                selectedDate = item.date,
                arrivalTime = item.arrivalTime,
                departureTime = item.departureTime,
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
        
        val updatedItem = snapshot.item.copy(
            date = snapshot.selectedDate,
            arrivalTime = snapshot.arrivalTime,
            departureTime = snapshot.departureTime,
            location = location,
            activity = snapshot.activity,
            notes = snapshot.notes
        )
        
        updateItemUseCase(
            item = updatedItem,
            currentTimestamp = Clock.System.now()
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

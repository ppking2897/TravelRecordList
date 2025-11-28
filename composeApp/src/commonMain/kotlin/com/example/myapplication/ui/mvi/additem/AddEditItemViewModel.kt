@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.myapplication.ui.mvi.additem

import com.example.myapplication.data.model.Location
import com.example.myapplication.domain.usecase.AddItineraryItemUseCase
import com.example.myapplication.ui.mvi.BaseViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Clock

/**
 * AddEditItem 畫面的 ViewModel
 * 
 * 負責處理行程項目新增相關的業務邏輯
 */
class AddEditItemViewModel(
    private val addItemUseCase: AddItineraryItemUseCase
) : BaseViewModel<AddEditItemState, AddEditItemIntent, AddEditItemEvent>(
    initialState = AddEditItemState()
) {
    
    override suspend fun processIntent(intent: AddEditItemIntent) {
        when (intent) {
            is AddEditItemIntent.Initialize -> initialize(intent.itinerary)
            is AddEditItemIntent.UpdateActivity -> updateActivity(intent.activity)
            is AddEditItemIntent.UpdateLocationName -> updateLocationName(intent.name)
            is AddEditItemIntent.UpdateLocationAddress -> updateLocationAddress(intent.address)
            is AddEditItemIntent.UpdateNotes -> updateNotes(intent.notes)
            is AddEditItemIntent.UpdateDate -> updateDate(intent.date)
            is AddEditItemIntent.UpdateTime -> updateTime(intent.time)
            is AddEditItemIntent.Save -> save()
        }
    }
    
    /**
     * 初始化（設定行程資訊）
     */
    private fun initialize(itinerary: com.example.myapplication.data.model.Itinerary) {
        val hasDateRange = itinerary.startDate != null && itinerary.endDate != null
        updateState {
            copy(
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
     * 更新時間
     */
    private fun updateTime(time: LocalTime?) {
        updateState { copy(selectedTime = time) }
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
            arrivalTime = snapshot.selectedTime,
            departureTime = null,
            location = location,
            activity = snapshot.activity,
            notes = snapshot.notes,
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

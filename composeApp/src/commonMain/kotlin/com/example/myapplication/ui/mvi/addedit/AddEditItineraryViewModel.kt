@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.myapplication.ui.mvi.addedit

import com.example.myapplication.data.model.DraftType
import com.example.myapplication.data.repository.DraftRepository
import com.example.myapplication.data.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.CreateItineraryUseCase
import com.example.myapplication.domain.usecase.LoadDraftUseCase
import com.example.myapplication.domain.usecase.SaveDraftUseCase
import com.example.myapplication.domain.usecase.UpdateItineraryUseCase
import com.example.myapplication.ui.mvi.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/**
 * AddEditItinerary 畫面的 ViewModel
 * 
 * 負責處理行程新增和編輯相關的業務邏輯
 */
class AddEditItineraryViewModel(
    private val createItineraryUseCase: CreateItineraryUseCase,
    private val updateItineraryUseCase: UpdateItineraryUseCase,
    private val itineraryRepository: ItineraryRepository,
    private val saveDraftUseCase: SaveDraftUseCase,
    private val loadDraftUseCase: LoadDraftUseCase,
    private val draftRepository: DraftRepository
) : BaseViewModel<AddEditItineraryState, AddEditItineraryIntent, AddEditItineraryEvent>(
    initialState = AddEditItineraryState()
) {
    
    override suspend fun processIntent(intent: AddEditItineraryIntent) {
        when (intent) {
            is AddEditItineraryIntent.LoadItinerary -> loadItinerary(intent.itineraryId)
            is AddEditItineraryIntent.LoadDraft -> loadDraft()
            is AddEditItineraryIntent.UpdateTitle -> updateTitle(intent.title)
            is AddEditItineraryIntent.UpdateDescription -> updateDescription(intent.description)
            is AddEditItineraryIntent.UpdateStartDate -> updateStartDate(intent.date)
            is AddEditItineraryIntent.UpdateEndDate -> updateEndDate(intent.date)
            is AddEditItineraryIntent.Save -> save()
            is AddEditItineraryIntent.SaveDraft -> saveDraft()
        }
    }
    
    /**
     * 載入現有行程（編輯模式）
     */
    private suspend fun loadItinerary(itineraryId: String) {
        updateState { copy(isLoading = true, isEditMode = true) }
        
        itineraryRepository.getItinerary(itineraryId)
            .onSuccess { itinerary ->
                if (itinerary != null) {
                    updateState {
                        copy(
                            itinerary = itinerary,
                            title = itinerary.title,
                            description = itinerary.description,
                            startDate = itinerary.startDate,
                            endDate = itinerary.endDate,
                            isLoading = false
                        )
                    }
                } else {
                    updateState {
                        copy(
                            isLoading = false,
                            error = "找不到行程"
                        )
                    }
                }
            }
            .onFailure { exception ->
                updateState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "載入失敗"
                    )
                }
            }
    }
    
    /**
     * 載入草稿（新增模式）
     */
    private suspend fun loadDraft() {
        if (state.value.isEditMode) return
        
        loadDraftUseCase(DraftType.ITINERARY)
            .onSuccess { draftData ->
                draftData?.let {
                    updateState {
                        copy(
                            title = it["title"] ?: "",
                            description = it["description"] ?: "",
                            startDate = it["startDate"]?.let { date ->
                                try { LocalDate.parse(date) } catch (e: Exception) { null }
                            },
                            endDate = it["endDate"]?.let { date ->
                                try { LocalDate.parse(date) } catch (e: Exception) { null }
                            }
                        )
                    }
                }
            }
    }
    
    /**
     * 更新標題
     */
    private fun updateTitle(title: String) {
        updateState {
            copy(
                title = title,
                titleError = null
            )
        }
    }
    
    /**
     * 更新描述
     */
    private fun updateDescription(description: String) {
        updateState { copy(description = description) }
    }
    
    /**
     * 更新開始日期
     */
    private fun updateStartDate(date: LocalDate?) {
        updateState {
            copy(
                startDate = date,
                dateError = null
            )
        }
    }
    
    /**
     * 更新結束日期
     */
    private fun updateEndDate(date: LocalDate?) {
        updateState {
            copy(
                endDate = date,
                dateError = null
            )
        }
    }
    
    /**
     * 儲存行程
     */
    private suspend fun save() {
        val snapshot = currentState
        
        // 驗證
        if (snapshot.title.isBlank()) {
            updateState { copy(titleError = "標題不可為空") }
            return
        }
        
        if (snapshot.startDate != null && snapshot.endDate != null &&
            snapshot.endDate!! < snapshot.startDate!!) {
            updateState { copy(dateError = "結束日期不能早於開始日期") }
            return
        }
        
        updateState { copy(isLoading = true, error = null) }
        
        val currentTimestamp = Clock.System.now()
        
        if (snapshot.isEditMode && snapshot.itinerary != null) {
            // 編輯模式
            val updatedItinerary = snapshot.itinerary.copy(
                title = snapshot.title,
                description = snapshot.description,
                startDate = snapshot.startDate,
                endDate = snapshot.endDate
            )
            
            updateItineraryUseCase(updatedItinerary, currentTimestamp)
                .onSuccess { itinerary ->
                    updateState { copy(isLoading = false) }
                    sendEvent(AddEditItineraryEvent.SaveSuccess(itinerary.id))
                }
                .onFailure { exception ->
                    updateState {
                        copy(
                            isLoading = false,
                            error = exception.message ?: "儲存失敗"
                        )
                    }
                    sendEvent(AddEditItineraryEvent.ShowError(exception.message ?: "儲存失敗"))
                }
        } else {
            // 新增模式
            createItineraryUseCase(
                title = snapshot.title,
                description = snapshot.description,
                startDate = snapshot.startDate,
                endDate = snapshot.endDate,
                currentTimestamp = currentTimestamp
            )
                .onSuccess { itinerary ->
                    // 清除草稿
                    draftRepository.deleteDraft(DraftType.ITINERARY)
                    
                    updateState { copy(isLoading = false) }
                    sendEvent(AddEditItineraryEvent.SaveSuccess(itinerary.id))
                }
                .onFailure { exception ->
                    updateState {
                        copy(
                            isLoading = false,
                            error = exception.message ?: "儲存失敗"
                        )
                    }
                    sendEvent(AddEditItineraryEvent.ShowError(exception.message ?: "儲存失敗"))
                }
        }
    }
    
    /**
     * 儲存草稿（僅新增模式）
     */
    private suspend fun saveDraft() {
        val snapshot = currentState
        if (snapshot.isEditMode) return
        
        val draftData = buildMap {
            put("title", snapshot.title)
            put("description", snapshot.description)
            snapshot.startDate?.let { put("startDate", it.toString()) }
            snapshot.endDate?.let { put("endDate", it.toString()) }
        }
        
        saveDraftUseCase(DraftType.ITINERARY, draftData)
            .onSuccess {
                updateState { copy(showDraftSaved = true) }
                sendEvent(AddEditItineraryEvent.DraftSaved)
                
                // 3秒後隱藏指示器
                delay(3000)
                updateState { copy(showDraftSaved = false) }
            }
    }
}

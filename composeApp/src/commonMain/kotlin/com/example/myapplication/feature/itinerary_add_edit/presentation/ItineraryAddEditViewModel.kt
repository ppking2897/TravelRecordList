@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.myapplication.feature.itinerary_add_edit.presentation

import kotlinx.datetime.LocalDate
import com.example.myapplication.core.domain.service.ImageStorageService
import com.example.myapplication.feature.draft.domain.model.DraftType
import com.example.myapplication.feature.itinerary.domain.usecase.CreateItineraryUseCase
import com.example.myapplication.feature.draft.domain.usecase.DeleteDraftUseCase
import com.example.myapplication.feature.itinerary.domain.usecase.GetItineraryUseCase
import com.example.myapplication.feature.draft.domain.usecase.LoadDraftUseCase
import com.example.myapplication.feature.draft.domain.usecase.SaveDraftUseCase
import com.example.myapplication.feature.itinerary.domain.usecase.UpdateItineraryUseCase
import com.example.myapplication.core.presentation.mvi.BaseViewModel
import kotlinx.coroutines.delay
import kotlin.time.Clock

/**
 * ItineraryAddEdit 畫面的 ViewModel
 *
 * 負責處理行程新增和編輯相關的業務邏輯
 */
class ItineraryAddEditViewModel(
    private val createItineraryUseCase: CreateItineraryUseCase,
    private val updateItineraryUseCase: UpdateItineraryUseCase,
    private val getItineraryUseCase: GetItineraryUseCase,
    private val saveDraftUseCase: SaveDraftUseCase,
    private val loadDraftUseCase: LoadDraftUseCase,
    private val deleteDraftUseCase: DeleteDraftUseCase,
    private val imageStorageService: ImageStorageService
) : BaseViewModel<ItineraryAddEditState, ItineraryAddEditIntent, ItineraryAddEditEvent>(
    initialState = ItineraryAddEditState()
) {

    override suspend fun processIntent(intent: ItineraryAddEditIntent) {
        when (intent) {
            is ItineraryAddEditIntent.LoadItinerary -> loadItinerary(intent.itineraryId)
            is ItineraryAddEditIntent.LoadDraft -> loadDraft()
            is ItineraryAddEditIntent.UpdateTitle -> updateTitle(intent.title)
            is ItineraryAddEditIntent.UpdateDescription -> updateDescription(intent.description)
            is ItineraryAddEditIntent.UpdateStartDate -> updateStartDate(intent.date)
            is ItineraryAddEditIntent.UpdateEndDate -> updateEndDate(intent.date)
            is ItineraryAddEditIntent.SetCoverPhoto -> setCoverPhoto(intent.imageData)
            is ItineraryAddEditIntent.RemoveCoverPhoto -> removeCoverPhoto()
            is ItineraryAddEditIntent.Save -> save()
            is ItineraryAddEditIntent.SaveDraft -> saveDraft()
        }
    }

    /**
     * 載入現有行程（編輯模式）
     */
    private suspend fun loadItinerary(itineraryId: String) {
        updateState { copy(isLoading = true, isEditMode = true) }

        getItineraryUseCase(itineraryId)
            .onSuccess { itinerary ->
                if (itinerary != null) {
                    updateState {
                        copy(
                            itinerary = itinerary,
                            title = itinerary.title,
                            description = itinerary.description,
                            startDate = itinerary.startDate,
                            endDate = itinerary.endDate,
                            coverPhotoPath = itinerary.coverPhotoPath,
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
     * 設定封面照片
     */
    private suspend fun setCoverPhoto(imageData: ByteArray) {
        updateState { copy(isLoading = true) }

        // 使用唯一的 ID 前綴來區分行程封面照片
        val coverId = "itinerary_cover_${Clock.System.now().toEpochMilliseconds()}"

        imageStorageService.saveImage(imageData, coverId)
            .onSuccess { path ->
                // 刪除舊的封面照片（如果有）
                currentState.coverPhotoPath?.let { oldPath ->
                    imageStorageService.deleteImage(oldPath)
                }
                updateState { copy(coverPhotoPath = path, isLoading = false) }
            }
            .onFailure { exception ->
                updateState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "儲存封面照片失敗"
                    )
                }
            }
    }

    /**
     * 移除封面照片
     */
    private suspend fun removeCoverPhoto() {
        val coverPath = currentState.coverPhotoPath ?: return

        imageStorageService.deleteImage(coverPath)
        updateState { copy(coverPhotoPath = null) }
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
                endDate = snapshot.endDate,
                coverPhotoPath = snapshot.coverPhotoPath
            )

            updateItineraryUseCase(updatedItinerary, currentTimestamp)
                .onSuccess { itinerary ->
                    updateState { copy(isLoading = false) }
                    sendEvent(ItineraryAddEditEvent.SaveSuccess(itinerary.id))
                }
                .onFailure { exception ->
                    updateState {
                        copy(
                            isLoading = false,
                            error = exception.message ?: "儲存失敗"
                        )
                    }
                    sendEvent(ItineraryAddEditEvent.ShowError(exception.message ?: "儲存失敗"))
                }
        } else {
            // 新增模式
            createItineraryUseCase(
                title = snapshot.title,
                description = snapshot.description,
                startDate = snapshot.startDate,
                endDate = snapshot.endDate,
                currentTimestamp = currentTimestamp,
                coverPhotoPath = snapshot.coverPhotoPath
            )
                .onSuccess { itinerary ->
                    // 清除草稿
                    deleteDraftUseCase(DraftType.ITINERARY)

                    updateState { copy(isLoading = false) }
                    sendEvent(ItineraryAddEditEvent.SaveSuccess(itinerary.id))
                }
                .onFailure { exception ->
                    updateState {
                        copy(
                            isLoading = false,
                            error = exception.message ?: "儲存失敗"
                        )
                    }
                    sendEvent(ItineraryAddEditEvent.ShowError(exception.message ?: "儲存失敗"))
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
                sendEvent(ItineraryAddEditEvent.DraftSaved)

                // 3秒後隱藏指示器
                delay(3000)
                updateState { copy(showDraftSaved = false) }
            }
    }
}

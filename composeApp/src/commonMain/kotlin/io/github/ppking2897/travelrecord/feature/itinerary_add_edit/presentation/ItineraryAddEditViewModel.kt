@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.itinerary_add_edit.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.feature.draft.domain.model.DraftType
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.CreateItineraryUseCase
import io.github.ppking2897.travelrecord.feature.draft.domain.usecase.DeleteDraftUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetItineraryUseCase
import io.github.ppking2897.travelrecord.feature.draft.domain.usecase.LoadDraftUseCase
import io.github.ppking2897.travelrecord.feature.draft.domain.usecase.SaveDraftUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.UpdateItineraryUseCase
import io.github.ppking2897.travelrecord.core.presentation.mvi.BaseViewModel
import kotlinx.coroutines.Job
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
) : BaseViewModel<ItineraryAddEditIntent, ItineraryAddEditState, ItineraryAddEditEvent>(
    initialState = ItineraryAddEditState()
) {

    /** 等待中的自動存草稿；輸入時重新計時 */
    private var draftSaveJob: Job? = null

    /** 「已自動儲存草稿」的隱藏倒數 */
    private var draftHintJob: Job? = null

    override fun processIntent(intent: ItineraryAddEditIntent) {
        viewModelScope.launch {
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
                is ItineraryAddEditIntent.Discard -> discard()
            }
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
        scheduleDraftSave()
    }

    /**
     * 更新描述
     */
    private fun updateDescription(description: String) {
        updateState { copy(description = description) }
        scheduleDraftSave()
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
        scheduleDraftSave()
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
        scheduleDraftSave()
    }

    /**
     * 新增模式下，停止輸入一段時間後自動存草稿；期間有新的輸入就重新計時，只存最後一次
     */
    private fun scheduleDraftSave() {
        if (state.value.isEditMode) return

        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            delay(DRAFT_SAVE_DELAY_MS)
            saveDraft()
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
                deleteUnsavedCover()
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
        if (state.value.coverPhotoPath == null) return

        deleteUnsavedCover()
        updateState { copy(coverPhotoPath = null) }
    }

    /**
     * 刪掉這次挑了、還沒存進行程的封面檔
     *
     * 行程原本的封面要等儲存成功才刪，不然沒儲存就離開時，行程會指著已經不在的檔案。
     */
    private suspend fun deleteUnsavedCover() {
        val current = state.value.coverPhotoPath ?: return
        if (current != state.value.itinerary?.coverPhotoPath) {
            imageStorageService.deleteImage(current)
        }
    }

    /**
     * 儲存行程
     */
    private suspend fun save() {
        val snapshot = state.value

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

        // 要存行程了，還沒存的草稿不用再存；不然建立成功清掉草稿後，草稿又會被存回去
        draftSaveJob?.cancel()
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
                    // 封面換掉或移除了，存好之後才刪原本的檔
                    val originalCover = snapshot.itinerary.coverPhotoPath
                    if (originalCover != null && originalCover != snapshot.coverPhotoPath) {
                        imageStorageService.deleteImage(originalCover)
                    }
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
        val snapshot = state.value
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
                hideDraftSavedHintLater()
            }
    }

    /**
     * 捨棄這次輸入的內容並離開
     *
     * 新增模式下連草稿一起清掉，下次開啟才不會又帶回來。刪草稿失敗也照樣離開。
     */
    private suspend fun discard() {
        // 還沒存的草稿不用再存；不然清掉草稿後，草稿又會被存回去
        draftSaveJob?.cancel()
        if (!state.value.isEditMode) {
            deleteDraftUseCase(DraftType.ITINERARY)
        }
        sendEvent(ItineraryAddEditEvent.NavigateBack)
    }

    /**
     * 「已自動儲存草稿」顯示一段時間後隱藏；連續存檔時從最後一次重新計時
     *
     * 另外計時，不放在存草稿的流程裡：存草稿會被新的輸入取消，提示就會一直留著。
     */
    private fun hideDraftSavedHintLater() {
        draftHintJob?.cancel()
        draftHintJob = viewModelScope.launch {
            delay(DRAFT_SAVED_HINT_MS)
            updateState { copy(showDraftSaved = false) }
        }
    }

    private companion object {
        /** 停止輸入多久後自動存草稿 */
        const val DRAFT_SAVE_DELAY_MS = 1_000L

        /** 「已自動儲存草稿」顯示多久 */
        const val DRAFT_SAVED_HINT_MS = 3_000L
    }
}

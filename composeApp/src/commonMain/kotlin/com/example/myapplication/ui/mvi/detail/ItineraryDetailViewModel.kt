package com.example.myapplication.ui.mvi.detail

import com.example.myapplication.data.repository.ItineraryItemRepository
import com.example.myapplication.data.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.*
import com.example.myapplication.ui.mvi.BaseViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ItineraryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ItineraryDetailViewModel(
    private val itineraryRepository: ItineraryRepository,
    private val itemRepository: ItineraryItemRepository,
    private val updateItemUseCase: UpdateItineraryItemUseCase,
    private val deleteItemUseCase: DeleteItineraryItemUseCase,
    private val deleteItineraryUseCase: DeleteItineraryUseCase,
    private val groupItemsByDateUseCase: GroupItemsByDateUseCase,
    private val filterItemsByDateUseCase: FilterItemsByDateUseCase,
    private val createRouteUseCase: CreateRouteFromItineraryUseCase,
    private val addPhotoUseCase: AddPhotoUseCase,
    private val generateThumbnailUseCase: GenerateThumbnailUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val setCoverPhotoUseCase: SetCoverPhotoUseCase,
    private val filterByHashtagUseCase: FilterByHashtagUseCase
) : BaseViewModel<ItineraryDetailState, ItineraryDetailIntent, ItineraryDetailEvent>(
    initialState = ItineraryDetailState()
) {
    
    override suspend fun processIntent(intent: ItineraryDetailIntent) {
        when (intent) {
            is ItineraryDetailIntent.LoadItinerary -> loadItinerary(intent.id)
            is ItineraryDetailIntent.SelectDate -> selectDate(intent.date)
            is ItineraryDetailIntent.ToggleItemCompletion -> toggleItemCompletion(intent.itemId)
            is ItineraryDetailIntent.DeleteItem -> deleteItem(intent.itemId)
            is ItineraryDetailIntent.DeleteItinerary -> deleteItinerary(intent.id)
            is ItineraryDetailIntent.GenerateRoute -> generateRoute()
            is ItineraryDetailIntent.ToggleItemExpansion -> toggleItemExpansion(intent.itemId)
            is ItineraryDetailIntent.AddPhoto -> addPhoto(intent.itemId, intent.imageData)
            is ItineraryDetailIntent.DeletePhoto -> deletePhoto(intent.photoId)
            is ItineraryDetailIntent.SetCoverPhoto -> setCoverPhoto(intent.itemId, intent.photoId)
            is ItineraryDetailIntent.FilterByHashtag -> filterByHashtag(intent.hashtag)
        }
    }
    
    private suspend fun loadItinerary(id: String) {
        updateState { copy(isLoading = true, error = null) }
        
        itineraryRepository.getItinerary(id)
            .onSuccess { itinerary ->
                if (itinerary != null) {
                    val dateRange = if (itinerary.startDate != null && itinerary.endDate != null) {
                        itinerary.startDate..itinerary.endDate
                    } else null
                    
                    itemRepository.getItemsByItinerary(id)
                        .onSuccess { items ->
                            val grouped = groupItemsByDateUseCase(items)
                            updateState {
                                copy(
                                    itinerary = itinerary,
                                    groupedItems = grouped.map { ItemsByDate(it.date, it.items) },
                                    dateRange = dateRange,
                                    isLoading = false
                                )
                            }
                        }
                        .onFailure { exception ->
                            updateState {
                                copy(
                                    itinerary = itinerary,
                                    dateRange = dateRange,
                                    isLoading = false,
                                    error = exception.message
                                )
                            }
                        }
                } else {
                    updateState {
                        copy(isLoading = false, error = "找不到行程")
                    }
                }
            }
            .onFailure { exception ->
                updateState {
                    copy(isLoading = false, error = exception.message)
                }
            }
    }
    
    private suspend fun selectDate(date: LocalDate?) {
        updateState { copy(selectedDate = date) }
        
        currentState.itinerary?.let { itinerary ->
            itemRepository.getItemsByItinerary(itinerary.id)
                .onSuccess { items ->
                    val filtered = if (date != null) {
                        filterItemsByDateUseCase(items, date)
                    } else {
                        items
                    }
                    val grouped = groupItemsByDateUseCase(filtered)
                    updateState {
                        copy(groupedItems = grouped.map { ItemsByDate(it.date, it.items) })
                    }
                }
        }
    }
    
    private suspend fun toggleItemCompletion(itemId: String) {
        // 先在 UI 上立即更新（Optimistic Update）
        val currentItem = currentState.groupedItems
            .flatMap { it.items }
            .find { it.id == itemId }

        if (currentItem != null) {
            val optimisticUpdate = currentItem.copy(
                isCompleted = !currentItem.isCompleted,
                completedAt = if (!currentItem.isCompleted) Clock.System.now() else null
            )
            updateItemInState(itemId) { optimisticUpdate }
        }

        // 然後同步到 Repository
        itemRepository.getItem(itemId)
            .onSuccess { item ->
                if (item != null) {
                    val updated = item.copy(
                        isCompleted = !item.isCompleted,
                        completedAt = if (!item.isCompleted) Clock.System.now() else null
                    )
                    updateItemUseCase(updated, Clock.System.now())
                        .onFailure { exception ->
                            // 失敗時回滾 UI 狀態
                            refreshItemInState(itemId)
                            sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "更新失敗"))
                        }
                }
            }
    }
    
    private suspend fun deleteItem(itemId: String) {
        deleteItemUseCase(itemId)
            .onSuccess {
                currentState.itinerary?.let { itinerary ->
                    handleIntent(ItineraryDetailIntent.LoadItinerary(itinerary.id))
                }
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "刪除失敗"))
            }
    }
    
    private suspend fun deleteItinerary(id: String) {
        deleteItineraryUseCase(id)
            .onSuccess {
                sendEvent(ItineraryDetailEvent.NavigateBack)
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "刪除失敗"))
            }
    }
    
    private suspend fun generateRoute() {
        currentState.itinerary?.let { itinerary ->
            createRouteUseCase(itinerary.id)
                .onSuccess { route ->
                    sendEvent(ItineraryDetailEvent.NavigateToRoute(route.id))
                }
                .onFailure { exception ->
                    sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "生成路線失敗"))
                }
        }
    }
    
    private fun toggleItemExpansion(itemId: String) {
        val expandedIds = currentState.expandedItemIds
        updateState {
            copy(
                expandedItemIds = if (itemId in expandedIds) {
                    expandedIds - itemId
                } else {
                    expandedIds + itemId
                }
            )
        }
    }

    /**
     * 只更新 State 中特定的 item，避免重新載入整個列表
     * 這樣可以減少不必要的 recomposition，提升效能
     */
    private fun updateItemInState(itemId: String, updater: (ItineraryItem) -> ItineraryItem) {
        updateState {
            copy(
                groupedItems = groupedItems.map { group ->
                    group.copy(
                        items = group.items.map { item ->
                            if (item.id == itemId) updater(item) else item
                        }
                    )
                }
            )
        }
    }

    /**
     * 從 Repository 重新取得特定 item 並更新 State
     */
    private suspend fun refreshItemInState(itemId: String) {
        itemRepository.getItem(itemId)
            .onSuccess { updatedItem ->
                if (updatedItem != null) {
                    updateItemInState(itemId) { updatedItem }
                }
            }
    }

    private suspend fun addPhoto(itemId: String, imageData: ByteArray) {
        addPhotoUseCase(itemId, imageData)
            .onSuccess { photo ->
                // 只更新特定 item，不重新載入整個列表
                refreshItemInState(itemId)

                // 背景生成縮圖
                viewModelScope.launch(Dispatchers.Default) {
                    generateThumbnailUseCase(photo)
                        .onSuccess {
                            // 縮圖生成成功，只更新特定 item
                            refreshItemInState(itemId)
                        }
                }
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "新增照片失敗"))
            }
    }

    private suspend fun deletePhoto(photoId: String) {
        // 先找出這張照片屬於哪個 item
        val itemId = currentState.groupedItems
            .flatMap { it.items }
            .find { item -> item.photos.any { it.id == photoId } }
            ?.id

        deletePhotoUseCase(photoId)
            .onSuccess {
                // 只更新特定 item
                itemId?.let { refreshItemInState(it) }
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "刪除照片失敗"))
            }
    }

    private suspend fun setCoverPhoto(itemId: String, photoId: String) {
        setCoverPhotoUseCase(itemId, photoId)
            .onSuccess {
                // 只更新特定 item
                refreshItemInState(itemId)
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "設定封面失敗"))
            }
    }
    
    private suspend fun filterByHashtag(hashtag: String?) {
        updateState { copy(selectedHashtag = hashtag) }
        
        currentState.itinerary?.let { itinerary ->
            itemRepository.getItemsByItinerary(itinerary.id)
                .onSuccess { items ->
                    val filtered = if (hashtag != null) {
                        filterByHashtagUseCase(items, hashtag)
                    } else {
                        items
                    }
                    
                    val dateFiltered = currentState.selectedDate?.let { date ->
                        filterItemsByDateUseCase(filtered, date)
                    } ?: filtered
                    
                    val grouped = groupItemsByDateUseCase(dateFiltered)
                    updateState {
                        copy(groupedItems = grouped.map { ItemsByDate(it.date, it.items) })
                    }
                }
        }
    }
}

package com.example.myapplication.feature.itinerary_detail.presentation

import com.example.myapplication.feature.itinerary_item.domain.model.ItineraryItem
import com.example.myapplication.feature.itinerary_item.domain.usecase.AddPhotoUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.BatchDeleteItemsUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.BatchUpdateItemsUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.DeleteItineraryItemUseCase
import com.example.myapplication.feature.itinerary.domain.usecase.DeleteItineraryUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.DeletePhotoUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.FilterByHashtagUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.FilterItemsByDateUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.GenerateThumbnailUseCase
import com.example.myapplication.feature.itinerary.domain.usecase.GetItineraryUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.GroupItemsByDateUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.SetCoverPhotoUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.UpdateItineraryItemUseCase
import com.example.myapplication.feature.route.domain.usecase.CreateRouteFromItineraryUseCase
import com.example.myapplication.core.presentation.mvi.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.plus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ItineraryDetail 畫面的 ViewModel（MVI 架構）
 */
@ExperimentalTime
class ItineraryDetailViewModel(
    private val getItineraryUseCase: GetItineraryUseCase,
    private val getItemsByItineraryUseCase: GetItemsByItineraryUseCase,
    private val deleteItineraryUseCase: DeleteItineraryUseCase,
    private val createRouteUseCase: CreateRouteFromItineraryUseCase,
    private val updateItemUseCase: UpdateItineraryItemUseCase,
    private val deleteItemUseCase: DeleteItineraryItemUseCase,
    private val groupItemsByDateUseCase: GroupItemsByDateUseCase,
    private val filterItemsByDateUseCase: FilterItemsByDateUseCase,
    private val filterByHashtagUseCase: FilterByHashtagUseCase,
    private val addPhotoUseCase: AddPhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val setCoverPhotoUseCase: SetCoverPhotoUseCase,
    private val generateThumbnailUseCase: GenerateThumbnailUseCase,
    private val batchDeleteItemsUseCase: BatchDeleteItemsUseCase,
    private val batchUpdateItemsUseCase: BatchUpdateItemsUseCase
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
            is ItineraryDetailIntent.QuickAddItem -> quickAddItem(intent.afterDayIndex)
            // 批量操作相關
            is ItineraryDetailIntent.ToggleSelectionMode -> toggleSelectionMode()
            is ItineraryDetailIntent.ToggleItemSelection -> toggleItemSelection(intent.itemId)
            is ItineraryDetailIntent.SelectAll -> selectAll()
            is ItineraryDetailIntent.ClearSelection -> clearSelection()
            is ItineraryDetailIntent.BatchDelete -> batchDelete()
            is ItineraryDetailIntent.BatchMarkComplete -> batchMarkComplete()
        }
    }

    private suspend fun loadItinerary(id: String) {
        updateState { copy(isLoading = true, error = null) }

        getItineraryUseCase(id)
            .onSuccess { itinerary ->
                if (itinerary != null) {
                    val dateRange = if (itinerary.startDate != null && itinerary.endDate != null) {
                        itinerary.startDate..itinerary.endDate
                    } else null

                    getItemsByItineraryUseCase(id)
                        .onSuccess { items ->
                            // 保持先前的日期篩選
                            val currentSelectedDate = currentState.selectedDate
                            val filtered = if (currentSelectedDate != null) {
                                filterItemsByDateUseCase(items, currentSelectedDate)
                            } else {
                                items
                            }
                            val grouped = groupItemsByDateUseCase(filtered)
                            val allGrouped = groupItemsByDateUseCase(items)
                            updateState {
                                copy(
                                    itinerary = itinerary,
                                    groupedItems = grouped,
                                    allGroupedItems = allGrouped,
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
            getItemsByItineraryUseCase(itinerary.id)
                .onSuccess { items ->
                    val filtered = if (date != null) {
                        filterItemsByDateUseCase(items, date)
                    } else {
                        items
                    }
                    val grouped = groupItemsByDateUseCase(filtered)
                    val allGrouped = groupItemsByDateUseCase(items)
                    updateState {
                        copy(
                            groupedItems = grouped,
                            allGroupedItems = allGrouped
                        )
                    }
                }
        }
    }

    private suspend fun toggleItemCompletion(itemId: String) {
        val currentItem = currentState.groupedItems
            .flatMap { it.items }
            .find { it.id == itemId }

        if (currentItem != null) {
            val optimisticUpdate = currentItem.copy(
                isCompleted = !currentItem.isCompleted,
                completedAt = if (!currentItem.isCompleted) Clock.System.now() else null
            )
            updateItemInState(itemId) { optimisticUpdate }

            val updatedItem = currentItem.copy(
                isCompleted = !currentItem.isCompleted,
                completedAt = if (!currentItem.isCompleted) Clock.System.now() else null
            )
            updateItemUseCase(updatedItem, Clock.System.now())
                .onFailure { exception ->
                    updateItemInState(itemId) { currentItem }
                    sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "更新失敗"))
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
        currentState.itinerary?.let { itinerary ->
            getItemsByItineraryUseCase(itinerary.id)
                .onSuccess { items ->
                    val updatedItem = items.find { it.id == itemId }
                    if (updatedItem != null) {
                        updateItemInState(itemId) { updatedItem }
                    }
                }
        }
    }

    private suspend fun addPhoto(itemId: String, imageData: ByteArray) {
        addPhotoUseCase(itemId, imageData)
            .onSuccess { photo ->
                refreshItemInState(itemId)

                viewModelScope.launch(Dispatchers.Default) {
                    generateThumbnailUseCase(photo)
                        .onSuccess {
                            refreshItemInState(itemId)
                        }
                }
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "新增照片失敗"))
            }
    }

    private suspend fun deletePhoto(photoId: String) {
        val itemId = currentState.groupedItems
            .flatMap { it.items }
            .find { item -> item.photos.any { it.id == photoId } }
            ?.id

        deletePhotoUseCase(photoId)
            .onSuccess {
                itemId?.let { refreshItemInState(it) }
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "刪除照片失敗"))
            }
    }

    private suspend fun setCoverPhoto(itemId: String, photoId: String) {
        setCoverPhotoUseCase(itemId, photoId)
            .onSuccess {
                refreshItemInState(itemId)
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "設定封面失敗"))
            }
    }

    private suspend fun filterByHashtag(hashtag: String?) {
        updateState { copy(selectedHashtag = hashtag) }

        currentState.itinerary?.let { itinerary ->
            getItemsByItineraryUseCase(itinerary.id)
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
                    val allGrouped = groupItemsByDateUseCase(items)
                    updateState {
                        copy(
                            groupedItems = grouped,
                            allGroupedItems = allGrouped
                        )
                    }
                }
        }
    }

    private suspend fun quickAddItem(dayIndex: Int) {
        val dateRange = currentState.dateRange ?: return
        val startDate = dateRange.start

        // dayIndex 為 0-based 的目標日序（Day 1 → 0、Day 2 → 1 ...）
        // 直接加該 index 就是目標日，不需要 +1（原本 +1 讓「新增這天」會跑到下一天）
        val defaultDate = startDate.plus(kotlinx.datetime.DatePeriod(days = dayIndex))

        val clampedDate = if (defaultDate > dateRange.endInclusive) {
            dateRange.endInclusive
        } else if (defaultDate < startDate) {
            startDate
        } else {
            defaultDate
        }

        sendEvent(ItineraryDetailEvent.NavigateToQuickAddItem(clampedDate))
    }

    // ========== 批量操作相關方法 ==========

    private fun toggleSelectionMode() {
        updateState {
            copy(
                isSelectionMode = !isSelectionMode,
                selectedItemIds = if (isSelectionMode) emptySet() else selectedItemIds
            )
        }
    }

    private fun toggleItemSelection(itemId: String) {
        updateState {
            val newSelection = if (itemId in selectedItemIds) {
                selectedItemIds - itemId
            } else {
                selectedItemIds + itemId
            }
            copy(selectedItemIds = newSelection)
        }
    }

    private fun selectAll() {
        val allItemIds = currentState.groupedItems
            .flatMap { it.items }
            .map { it.id }
            .toSet()
        updateState {
            copy(selectedItemIds = allItemIds)
        }
    }

    private fun clearSelection() {
        updateState {
            copy(selectedItemIds = emptySet())
        }
    }

    private suspend fun batchDelete() {
        val selectedIds = currentState.selectedItemIds.toList()
        if (selectedIds.isEmpty()) return

        batchDeleteItemsUseCase(selectedIds)
            .onSuccess { deletedCount ->
                updateState {
                    copy(
                        isSelectionMode = false,
                        selectedItemIds = emptySet()
                    )
                }
                currentState.itinerary?.let { itinerary ->
                    handleIntent(ItineraryDetailIntent.LoadItinerary(itinerary.id))
                }
                sendEvent(ItineraryDetailEvent.ShowBatchOperationResult("已刪除 $deletedCount 個項目"))
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "批量刪除失敗"))
            }
    }

    private suspend fun batchMarkComplete() {
        val selectedIds = currentState.selectedItemIds.toList()
        if (selectedIds.isEmpty()) return

        batchUpdateItemsUseCase.markComplete(selectedIds)
            .onSuccess { updatedCount ->
                updateState {
                    copy(
                        isSelectionMode = false,
                        selectedItemIds = emptySet()
                    )
                }
                currentState.itinerary?.let { itinerary ->
                    handleIntent(ItineraryDetailIntent.LoadItinerary(itinerary.id))
                }
                sendEvent(ItineraryDetailEvent.ShowBatchOperationResult("已標記 $updatedCount 個項目為完成"))
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "批量更新失敗"))
            }
    }
}

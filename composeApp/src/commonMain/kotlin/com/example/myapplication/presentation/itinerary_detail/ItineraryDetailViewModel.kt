package com.example.myapplication.presentation.itinerary_detail

import com.example.myapplication.domain.interactor.ItemInteractor
import com.example.myapplication.domain.interactor.PhotoInteractor
import com.example.myapplication.domain.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.BatchDeleteItemsUseCase
import com.example.myapplication.domain.usecase.BatchUpdateItemsUseCase
import com.example.myapplication.domain.usecase.CreateRouteFromItineraryUseCase
import com.example.myapplication.domain.usecase.DeleteItineraryUseCase
import com.example.myapplication.presentation.mvi.BaseViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.entity.ItineraryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ItineraryDetail 畫面的 ViewModel（MVI 架構）
 *
 * 使用 Interactor 模式簡化依賴：
 * - ItemInteractor: 處理項目相關操作（CRUD、篩選、分組）
 * - PhotoInteractor: 處理照片相關操作（新增、刪除、封面、縮圖）
 */
@ExperimentalTime
class ItineraryDetailViewModel(
    private val itineraryRepository: ItineraryRepository,
    private val deleteItineraryUseCase: DeleteItineraryUseCase,
    private val createRouteUseCase: CreateRouteFromItineraryUseCase,
    private val itemInteractor: ItemInteractor,
    private val photoInteractor: PhotoInteractor,
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

        itineraryRepository.getItinerary(id)
            .onSuccess { itinerary ->
                if (itinerary != null) {
                    val dateRange = if (itinerary.startDate != null && itinerary.endDate != null) {
                        itinerary.startDate..itinerary.endDate
                    } else null

                    itemInteractor.getItemsByItinerary(id)
                        .onSuccess { items ->
                            val grouped = itemInteractor.groupByDate(items)
                            updateState {
                                copy(
                                    itinerary = itinerary,
                                    groupedItems = grouped.map { (date, items) ->
                                        ItemsByDate(date, items)
                                    },
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
            itemInteractor.getItemsByItinerary(itinerary.id)
                .onSuccess { items ->
                    val filtered = if (date != null) {
                        itemInteractor.filterByDate(items, date)
                    } else {
                        items
                    }
                    val grouped = itemInteractor.groupByDate(filtered)
                    updateState {
                        copy(groupedItems = grouped.map { (d, i) -> ItemsByDate(d, i) })
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

            // 使用 Interactor 更新
            itemInteractor.toggleCompletion(currentItem)
                .onFailure { exception ->
                    // 失敗時回滾 UI 狀態
                    updateItemInState(itemId) { currentItem }
                    sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "更新失敗"))
                }
        }
    }

    private suspend fun deleteItem(itemId: String) {
        itemInteractor.deleteItem(itemId)
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
            itemInteractor.getItemsByItinerary(itinerary.id)
                .onSuccess { items ->
                    val updatedItem = items.find { it.id == itemId }
                    if (updatedItem != null) {
                        updateItemInState(itemId) { updatedItem }
                    }
                }
        }
    }

    private suspend fun addPhoto(itemId: String, imageData: ByteArray) {
        photoInteractor.addPhoto(itemId, imageData)
            .onSuccess { photo ->
                // 只更新特定 item，不重新載入整個列表
                refreshItemInState(itemId)

                // 背景生成縮圖
                viewModelScope.launch(Dispatchers.Default) {
                    photoInteractor.generateThumbnail(photo)
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

        photoInteractor.deletePhoto(photoId)
            .onSuccess {
                // 只更新特定 item
                itemId?.let { refreshItemInState(it) }
            }
            .onFailure { exception ->
                sendEvent(ItineraryDetailEvent.ShowError(exception.message ?: "刪除照片失敗"))
            }
    }

    private suspend fun setCoverPhoto(itemId: String, photoId: String) {
        photoInteractor.setCoverPhoto(itemId, photoId)
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
            itemInteractor.getItemsByItinerary(itinerary.id)
                .onSuccess { items ->
                    val filtered = if (hashtag != null) {
                        itemInteractor.filterByHashtag(items, hashtag)
                    } else {
                        items
                    }

                    val dateFiltered = currentState.selectedDate?.let { date ->
                        itemInteractor.filterByDate(filtered, date)
                    } ?: filtered

                    val grouped = itemInteractor.groupByDate(dateFiltered)
                    updateState {
                        copy(groupedItems = grouped.map { (d, i) -> ItemsByDate(d, i) })
                    }
                }
        }
    }

    private suspend fun quickAddItem(afterDayIndex: Int) {
        val dateRange = currentState.dateRange ?: return
        val startDate = dateRange.start

        // 計算預設日期：afterDayIndex 代表「第 N 天之後」，所以新項目預設為「第 N+1 天」
        // afterDayIndex 是 0-indexed，所以 afterDayIndex = 0 表示 Day 1 之後，即 Day 2
        val defaultDate = startDate.plus(kotlinx.datetime.DatePeriod(days = afterDayIndex + 1))

        // 確保日期不超出範圍
        val clampedDate = if (defaultDate > dateRange.endInclusive) {
            dateRange.endInclusive
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
                // 清除選擇狀態
                updateState {
                    copy(
                        isSelectionMode = false,
                        selectedItemIds = emptySet()
                    )
                }
                // 重新載入列表
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
                // 清除選擇狀態
                updateState {
                    copy(
                        isSelectionMode = false,
                        selectedItemIds = emptySet()
                    )
                }
                // 重新載入列表
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

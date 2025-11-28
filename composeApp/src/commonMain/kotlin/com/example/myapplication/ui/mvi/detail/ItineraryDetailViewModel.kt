package com.example.myapplication.ui.mvi.detail

import com.example.myapplication.data.repository.ItineraryItemRepository
import com.example.myapplication.data.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.*
import com.example.myapplication.ui.mvi.BaseViewModel
import kotlinx.datetime.LocalDate
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
    private val createRouteUseCase: CreateRouteFromItineraryUseCase
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
        itemRepository.getItem(itemId)
            .onSuccess { item ->
                if (item != null) {
                    val updated = item.copy(
                        isCompleted = !item.isCompleted,
                        completedAt = if (!item.isCompleted) kotlin.time.Clock.System.now() else null
                    )
                    updateItemUseCase(updated, kotlin.time.Clock.System.now())
                        .onSuccess {
                            currentState.itinerary?.let { itinerary ->
                                handleIntent(ItineraryDetailIntent.LoadItinerary(itinerary.id))
                            }
                        }
                        .onFailure { exception ->
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
}

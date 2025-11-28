package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.model.Location
import com.example.myapplication.data.repository.ItineraryItemRepository
import com.example.myapplication.data.repository.ItineraryRepository
import kotlin.time.ExperimentalTime

/**
 * 取得旅遊歷史的 Use Case
 * 按 location 分組所有已完成的 items
 */
@ExperimentalTime
class GetTravelHistoryUseCase(
    private val itemRepository: ItineraryItemRepository,
    private val itineraryRepository: ItineraryRepository
) {
    suspend operator fun invoke(): Result<Map<Location, List<ItineraryItem>>> {
        return try {
            // 取得所有 itineraries
            val itineraries = itineraryRepository.getAllItineraries().getOrElse {
                return Result.failure(it)
            }
            
            // 收集所有 items
            val allItems = mutableListOf<ItineraryItem>()
            for (itinerary in itineraries) {
                val items = itemRepository.getItemsByItinerary(itinerary.id).getOrElse {
                    // 如果某個 itinerary 的 items 取得失敗，繼續處理其他的
                    continue
                }
                allItems.addAll(items)
            }
            
            // 按 location 分組（只包含已完成的 items）
            val completedItems = allItems.filter { it.isCompleted }
            val groupedByLocation = completedItems.groupBy { it.location }
            
            // 對每個 location 的 items 按日期排序
            val sortedGroupedByLocation = groupedByLocation.mapValues { (_, items) ->
                items.sortedWith(
                    compareBy<ItineraryItem> { it.date }
                        .thenBy { it.primaryTime() }
                )
            }
            
            // 按 location name 排序
            val sortedMap = sortedGroupedByLocation.entries
                .sortedBy { it.key.name }
                .associate { it.key to it.value }
            
            Result.success(sortedMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

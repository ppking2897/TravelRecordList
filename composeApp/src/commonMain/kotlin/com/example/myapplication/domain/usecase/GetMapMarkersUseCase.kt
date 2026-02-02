package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.entity.MapMarker
import com.example.myapplication.domain.repository.ItineraryItemRepository

/**
 * 取得行程項目的地圖標記
 *
 * 將 ItineraryItem 轉換為 MapMarker，過濾掉沒有座標的項目
 */
class GetMapMarkersUseCase(
    private val itemRepository: ItineraryItemRepository
) {
    /**
     * 根據行程 ID 取得所有可顯示在地圖上的標記
     *
     * @param itineraryId 行程 ID
     * @return 地圖標記列表，按日期和時間排序
     */
    suspend operator fun invoke(itineraryId: String): Result<List<MapMarker>> {
        return itemRepository.getItemsByItinerary(itineraryId).map { items ->
            convertToMarkers(items)
        }
    }

    private fun convertToMarkers(items: List<ItineraryItem>): List<MapMarker> {
        // 過濾掉沒有座標的項目
        val itemsWithCoordinates = items.filter { item ->
            item.location.latitude != null && item.location.longitude != null
        }

        // 按日期和時間排序
        val sortedItems = itemsWithCoordinates.sortedWith(
            compareBy({ it.date }, { it.primaryTime() })
        )

        // 計算每個項目在當日的順序
        val dateGroups = sortedItems.groupBy { it.date }

        return sortedItems.map { item ->
            val dayItems = dateGroups[item.date] ?: emptyList()
            val order = dayItems.indexOf(item) + 1

            MapMarker(
                id = item.id,
                latitude = item.location.latitude!!,
                longitude = item.location.longitude!!,
                title = item.activity,
                locationName = item.location.name,
                date = item.date,
                order = order,
                isCompleted = item.isCompleted
            )
        }
    }
}

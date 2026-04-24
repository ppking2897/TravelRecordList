package com.example.myapplication.feature.itinerary.domain.usecase

import com.example.myapplication.core.domain.sample.SampleDataProvider
import com.example.myapplication.feature.itinerary_item.domain.repository.ItineraryItemRepository
import com.example.myapplication.feature.itinerary.domain.repository.ItineraryRepository

/**
 * 初始化範例資料 UseCase
 *
 * 在 App 首次啟動時，如果沒有任何行程資料，則插入預設的範例行程
 */
class InitializeSampleDataUseCase(
    private val itineraryRepository: ItineraryRepository,
    private val itemRepository: ItineraryItemRepository,
) {
    /**
     * 執行初始化
     *
     * @return true 表示有插入範例資料，false 表示已有資料不需要插入
     */
    suspend operator fun invoke(): Result<Boolean> {
        return runCatching {
            // 檢查是否已有行程資料
            val existingItineraries = itineraryRepository.getAllItineraries().getOrThrow()

            if (existingItineraries.isNotEmpty()) {
                // 已有資料，不需要插入
                return@runCatching false
            }

            // 取得範例行程（現在正在旅行 + 一筆已結束的過去旅行）
            val sampleItineraries = listOf(
                SampleDataProvider.getSampleItinerary(),
                SampleDataProvider.getSamplePastItinerary()
            )

            sampleItineraries.forEach { sampleItinerary ->
                // 先建立行程（不含 items）
                val itineraryWithoutItems = sampleItinerary.copy(items = emptyList())
                itineraryRepository.createItinerary(itineraryWithoutItems).getOrThrow()

                // 再逐一建立行程項目
                sampleItinerary.items.forEach { item ->
                    itemRepository.addItem(item).getOrThrow()
                }
            }

            true
        }
    }
}

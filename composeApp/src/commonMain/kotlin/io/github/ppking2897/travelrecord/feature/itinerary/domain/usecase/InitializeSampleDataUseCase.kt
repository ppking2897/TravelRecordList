package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.core.domain.sample.SampleDataProvider
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.SampleDataRecordRepository

/**
 * 初始化範例資料 UseCase
 *
 * 範例行程只放一次：沒有行程、也還沒放過範例時才放，放完記下來，之後行程刪光也不再放。
 * 從舊版升級、本來就有行程的不放，同樣記下來。
 */
class InitializeSampleDataUseCase(
    private val itineraryRepository: ItineraryRepository,
    private val itemRepository: ItineraryItemRepository,
    private val sampleDataRecord: SampleDataRecordRepository,
) {
    /**
     * 執行初始化
     *
     * @return true 表示有插入範例資料，false 表示放過範例或已有資料不需要插入
     */
    suspend operator fun invoke(): Result<Boolean> {
        return runCatching {
            if (sampleDataRecord.isSampleDataAdded().getOrThrow()) {
                return@runCatching false
            }

            // 檢查是否已有行程資料
            val existingItineraries = itineraryRepository.getAllItineraries().getOrThrow()

            if (existingItineraries.isNotEmpty()) {
                // 已有資料，不需要插入；記下來，之後行程刪光也不再放
                sampleDataRecord.markSampleDataAdded().getOrThrow()
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
            sampleDataRecord.markSampleDataAdded().getOrThrow()

            true
        }
    }
}

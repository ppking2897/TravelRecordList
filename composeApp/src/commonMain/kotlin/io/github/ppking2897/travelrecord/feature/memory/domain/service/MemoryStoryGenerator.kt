@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.memory.domain.service

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.memory.domain.model.MemoryStory

/**
 * 回憶敘事產生器
 *
 * 根據一段完成的行程與其項目資料，產出敘事文字（標題、摘要、完整
 * 敘事、每日 highlight）。Phase 3 只提供本地規則式實作，未來若接
 * 遠端 AI 僅需提供新的實作並替換 Koin 綁定。
 */
interface MemoryStoryGenerator {
    suspend fun generate(
        itinerary: Itinerary,
        items: List<ItineraryItem>
    ): MemoryStory
}

package io.github.ppking2897.travelrecord.test

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository

/**
 * 只提供「讀取全部行程」的 ItineraryRepository，給不測資料層的測試用；其他方法不該被呼叫
 */
class StubItineraryRepository(
    private val allItineraries: Result<List<Itinerary>>
) : ItineraryRepository {
    override suspend fun getAllItineraries(): Result<List<Itinerary>> = allItineraries

    override suspend fun createItinerary(itinerary: Itinerary) = error("不應呼叫")
    override suspend fun getItinerary(id: String) = error("不應呼叫")
    override suspend fun updateItinerary(itinerary: Itinerary) = error("不應呼叫")
    override suspend fun deleteItinerary(id: String) = error("不應呼叫")
    override suspend fun searchItineraries(query: String) = error("不應呼叫")
}

fun stubItineraryRepository(vararg itineraries: Itinerary) =
    StubItineraryRepository(Result.success(itineraries.toList()))

fun failingItineraryRepository() =
    StubItineraryRepository(Result.failure(IllegalStateException("讀取失敗")))

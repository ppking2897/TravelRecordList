package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.inMemorySampleDataRecord
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * 範例行程只在第一次安裝時放一次，測試名稱對應 .claude/docs/SpecAdjustments.md 的 Scenario
 */
class InitializeSampleDataUseCaseTest {

    private val items = InMemoryItineraryItemRepository()
    private val itineraries = InMemoryItineraryRepository(items)
    private val sampleDataRecord = inMemorySampleDataRecord()

    private val sampleTitles = listOf("京都大阪 5 日遊", "東京 3 日遊")

    /** 每次打開 App 都建一個新的 use case；資料與「範例已放過」的紀錄留著 */
    private suspend fun openApp() {
        InitializeSampleDataUseCase(itineraries, items, sampleDataRecord)().getOrThrow()
    }

    private suspend fun titles() = itineraries.getAllItineraries().getOrThrow().map { it.title }

    private suspend fun deleteAllTrips() {
        itineraries.getAllItineraries().getOrThrow().forEach { itineraries.deleteItinerary(it.id).getOrThrow() }
    }

    @Test
    fun `第一次安裝放入範例行程`() = runTest {
        openApp()

        titles() shouldContainExactlyInAnyOrder sampleTitles
    }

    @Test
    fun `範例刪光後重開不會回來`() = runTest {
        openApp()
        deleteAllTrips()

        openApp()

        titles().shouldBeEmpty()
    }

    @Test
    fun `從舊版升級、本來就有行程`() = runTest {
        itineraries.createItinerary(testItinerary("mine-1", title = "我的旅行 1")).getOrThrow()
        itineraries.createItinerary(testItinerary("mine-2", title = "我的旅行 2")).getOrThrow()

        openApp()

        titles() shouldContainExactlyInAnyOrder listOf("我的旅行 1", "我的旅行 2")

        deleteAllTrips()
        openApp()

        titles().shouldBeEmpty()
    }

    @Test
    fun `從舊版升級、行程已經刪光`() = runTest {
        openApp()

        titles() shouldContainExactlyInAnyOrder sampleTitles

        deleteAllTrips()
        openApp()

        titles().shouldBeEmpty()
    }
}

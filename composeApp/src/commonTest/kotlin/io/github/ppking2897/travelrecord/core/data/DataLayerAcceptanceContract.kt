@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.core.data

import io.github.ppking2897.travelrecord.core.data.local.InMemoryStorageService
import io.github.ppking2897.travelrecord.core.domain.sample.SampleDataProvider
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.DeleteItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.InitializeSampleDataUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.SearchItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.HashtagRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.PhotoRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddPhotoUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.BatchDeleteItemsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.BatchUpdateItemsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.ExtractHashtagsUseCase
import io.github.ppking2897.travelrecord.feature.route.data.repository.RouteRepositoryImpl
import io.github.ppking2897.travelrecord.feature.route.domain.usecase.CreateRouteFromItineraryUseCase
import io.github.ppking2897.travelrecord.test.FakeImageStorageService
import io.github.ppking2897.travelrecord.test.TestRepositories
import io.github.ppking2897.travelrecord.test.instantAt
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * 跨 Repository 的行為，透過 App 實際使用的 use case 驗證
 *
 * 核心要求：每個項目只有一份資料，從行程讀到的項目、項目清單、搜尋、刪除都看到同一份。
 * 路線與照片的 Repository 照 App 的接法組裝在被測的行程／項目 Repository 之上。
 */
abstract class DataLayerAcceptanceContract {

    protected abstract fun newRepositories(): TestRepositories

    private val sampleItineraryId = SampleDataProvider.getSampleItinerary().id

    private inner class App(val repos: TestRepositories = newRepositories()) {
        val photos = PhotoRepositoryImpl(FakeImageStorageService(), repos.itineraries, repos.items)
        val routes = RouteRepositoryImpl(InMemoryStorageService(), repos.itineraries)

        val initializeSampleData = InitializeSampleDataUseCase(repos.itineraries, repos.items)
        val createRoute = CreateRouteFromItineraryUseCase(routes)
        val addPhoto = AddPhotoUseCase(photos)
        val deleteItinerary = DeleteItineraryUseCase(repos.itineraries, repos.items)
        val search = SearchItinerariesUseCase(repos.itineraries)
        val batchUpdate = BatchUpdateItemsUseCase(repos.items)
        val batchDelete = BatchDeleteItemsUseCase(repos.items)
        val addItem = AddItineraryItemUseCase(repos.items, repos.itineraries, ExtractHashtagsUseCase(HashtagRepositoryImpl()))

        /** 照使用者操作的路徑建立行程與項目 */
        suspend fun createTripWithItems(vararg locationNames: String): List<ItineraryItem> {
            repos.itineraries.createItinerary(testItinerary(id = "trip", title = "我的旅行")).getOrThrow()
            return locationNames.map { name ->
                addItem(
                    itineraryId = "trip",
                    date = LocalDate(2026, 10, 1),
                    location = Location(name = name),
                    activity = "參觀 $name",
                    notes = "",
                    currentTimestamp = instantAt(0),
                ).getOrThrow()
            }
        }
    }

    @Test
    fun `範例行程可以生成路線`() = runTest {
        val app = App()
        app.initializeSampleData().getOrThrow()

        val route = app.createRoute(sampleItineraryId)

        route.isSuccess shouldBe true
        route.getOrThrow().locations.size shouldBe 8
    }

    @Test
    fun `範例項目可以加照片`() = runTest {
        val app = App()
        app.initializeSampleData().getOrThrow()

        val photo = app.addPhoto("sample-item-1", byteArrayOf(1))

        photo.isSuccess shouldBe true
        app.repos.items.getItem("sample-item-1").getOrThrow()!!.photos.size shouldBe 1
    }

    @Test
    fun `刪除範例行程會一併刪除它的項目`() = runTest {
        val app = App()
        app.initializeSampleData().getOrThrow()

        app.deleteItinerary(sampleItineraryId).getOrThrow()

        app.repos.items.getItemsByItinerary(sampleItineraryId).getOrThrow() shouldBe emptyList()
        app.repos.items.getItem("sample-item-1").getOrThrow() shouldBe null
    }

    @Test
    fun `搜尋得到範例項目的地點`() = runTest {
        val app = App()
        app.initializeSampleData().getOrThrow()

        app.search("清水寺").getOrThrow().map { it.id } shouldContain sampleItineraryId
    }

    @Test
    fun `從行程讀到的項目等於該行程的項目清單`() = runTest {
        val app = App()
        app.initializeSampleData().getOrThrow()

        val fromItinerary = app.repos.itineraries.getItinerary(sampleItineraryId).getOrThrow()!!.items

        fromItinerary shouldBe app.repos.items.getItemsByItinerary(sampleItineraryId).getOrThrow()
    }

    @Test
    fun `批次標記完成後再加照片，完成狀態維持`() = runTest {
        val app = App()
        val (item) = app.createTripWithItems("金閣寺")

        app.batchUpdate.markComplete(listOf(item.id)).getOrThrow()
        app.addPhoto(item.id, byteArrayOf(1)).getOrThrow()

        val saved = app.repos.items.getItem(item.id).getOrThrow()!!
        saved.photos.size shouldBe 1
        saved.isCompleted shouldBe true
    }

    @Test
    fun `批次刪除的項目不會留在行程裡，也搜尋不到`() = runTest {
        val app = App()
        val (deleted, kept) = app.createTripWithItems("嵐山竹林", "奈良公園")

        app.batchDelete(listOf(deleted.id)).getOrThrow()

        app.repos.itineraries.getItinerary("trip").getOrThrow()!!.items.map { it.id } shouldBe listOf(kept.id)
        app.search("嵐山竹林").getOrThrow().map { it.id } shouldNotContain "trip"
    }
}

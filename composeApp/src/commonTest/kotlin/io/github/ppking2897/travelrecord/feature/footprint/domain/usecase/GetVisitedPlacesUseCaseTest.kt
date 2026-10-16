@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.footprint.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetAllItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase
import io.github.ppking2897.travelrecord.test.StubItineraryItemRepository
import io.github.ppking2897.travelrecord.test.failingItineraryRepository
import io.github.ppking2897.travelrecord.test.stubItineraryRepository
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.time.ExperimentalTime

class GetVisitedPlacesUseCaseTest {

    private val today = LocalDate(2026, 10, 16)
    private val yesterday = LocalDate(2026, 10, 15)
    private val tomorrow = LocalDate(2026, 10, 17)

    @Test
    fun `沒打卡但日期已過的地點算去過`() = runTest {
        val useCase = useCaseWith(item("past", yesterday, completed = false))

        useCase(today).getOrThrow().map { it.itemId } shouldBe listOf("past")
    }

    @Test
    fun `已打卡的地點即使日期還沒到也算去過`() = runTest {
        val useCase = useCaseWith(item("checked-in-early", tomorrow, completed = true))

        useCase(today).getOrThrow().map { it.itemId } shouldBe listOf("checked-in-early")
    }

    @Test
    fun `今天已打卡的地點算去過`() = runTest {
        val useCase = useCaseWith(item("today-checked", today, completed = true))

        useCase(today).getOrThrow().map { it.itemId } shouldBe listOf("today-checked")
    }

    @Test
    fun `今天還沒打卡的地點不算`() = runTest {
        val useCase = useCaseWith(item("today-unchecked", today, completed = false))

        useCase(today).getOrThrow() shouldBe emptyList()
    }

    @Test
    fun `日期還沒到也沒打卡的地點不算`() = runTest {
        val useCase = useCaseWith(item("future", tomorrow, completed = false))

        useCase(today).getOrThrow() shouldBe emptyList()
    }

    @Test
    fun `一個地點都還沒去過的行程不會出現在結果裡`() = runTest {
        val useCase = GetVisitedPlacesUseCase(
            GetAllItinerariesUseCase(stubItineraryRepository(testItinerary("kyoto"), testItinerary("tokyo"))),
            GetItemsByItineraryUseCase(
                StubItineraryItemRepository(
                    listOf(
                        testItem("fushimi", "kyoto", date = yesterday),
                        testItem("kiyomizu", "kyoto", date = tomorrow),
                        testItem("asakusa", "tokyo", date = tomorrow),
                        testItem("skytree", "tokyo", date = tomorrow),
                    )
                )
            )
        )

        val places = useCase(today).getOrThrow()

        places.map { it.itemId } shouldContainExactlyInAnyOrder listOf("fushimi")
        places.map { it.tripId }.distinct() shouldBe listOf("kyoto")
    }

    @Test
    fun `讀取行程失敗時回傳失敗`() = runTest {
        val useCase = useCaseWith(itineraries = failingItineraryRepository())

        useCase(today).isFailure shouldBe true
    }

    private fun item(id: String, date: LocalDate, completed: Boolean): ItineraryItem =
        testItem(id, "trip", date = date).copy(isCompleted = completed)

    private fun useCaseWith(
        vararg items: ItineraryItem,
        itineraries: ItineraryRepository = stubItineraryRepository(testItinerary("trip")),
    ) = GetVisitedPlacesUseCase(
        GetAllItinerariesUseCase(itineraries),
        GetItemsByItineraryUseCase(StubItineraryItemRepository(items.toList()))
    )
}

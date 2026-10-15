@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.test.failingItineraryRepository
import io.github.ppking2897.travelrecord.test.stubItineraryRepository
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.time.ExperimentalTime

class GetCurrentTripUseCaseTest {

    private val today = LocalDate(2026, 10, 15)

    @Test
    fun `今天落在行程日期之間時回傳該行程`() = runTest {
        val useCase = useCaseWith(trip("kyoto", LocalDate(2026, 10, 14), LocalDate(2026, 10, 18)))

        useCase(today).getOrThrow()?.id shouldBe "kyoto"
    }

    @Test
    fun `今天是開始日也算進行中`() = runTest {
        val useCase = useCaseWith(trip("kyoto", today, LocalDate(2026, 10, 18)))

        useCase(today).getOrThrow()?.id shouldBe "kyoto"
    }

    @Test
    fun `今天是結束日也算進行中`() = runTest {
        val useCase = useCaseWith(trip("kyoto", LocalDate(2026, 10, 12), today))

        useCase(today).getOrThrow()?.id shouldBe "kyoto"
    }

    @Test
    fun `同時有好幾趟進行中時回傳結束日最早的`() = runTest {
        val useCase = useCaseWith(
            trip("a", LocalDate(2026, 10, 10), LocalDate(2026, 10, 20)),
            trip("b", LocalDate(2026, 10, 14), LocalDate(2026, 10, 16)),
            trip("c", LocalDate(2026, 10, 15), LocalDate(2026, 10, 17)),
        )

        useCase(today).getOrThrow()?.id shouldBe "b"
    }

    @Test
    fun `沒有行程涵蓋今天時回傳 null`() = runTest {
        val useCase = useCaseWith(
            trip("past", LocalDate(2026, 9, 1), LocalDate(2026, 9, 3)),
            trip("future", LocalDate(2026, 11, 1), LocalDate(2026, 11, 5)),
        )

        useCase(today).getOrThrow() shouldBe null
    }

    @Test
    fun `日期沒填完整的行程不算進行中`() = runTest {
        val useCase = useCaseWith(
            trip("start-only", LocalDate(2026, 10, 1), null),
            trip("end-only", null, LocalDate(2026, 10, 30)),
            trip("no-dates", null, null),
        )

        useCase(today).getOrThrow() shouldBe null
    }

    @Test
    fun `讀取行程失敗時回傳失敗`() = runTest {
        val useCase = GetCurrentTripUseCase(failingItineraryRepository())

        useCase(today).isFailure shouldBe true
    }

    private fun useCaseWith(vararg itineraries: Itinerary) =
        GetCurrentTripUseCase(stubItineraryRepository(*itineraries))

    private fun trip(id: String, start: LocalDate?, end: LocalDate?) =
        testItinerary(id = id).copy(startDate = start, endDate = end)
}

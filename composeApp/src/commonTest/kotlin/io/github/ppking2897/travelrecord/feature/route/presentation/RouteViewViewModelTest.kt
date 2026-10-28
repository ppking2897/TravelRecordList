@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.feature.route.presentation

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * 路線頁拿行程編號，打開時依行程目前的項目排出路線；分享出去的是可讀的文字
 *
 * 分享文字的情境對應 .claude/docs/SpecAdjustments.md 第 5 組。
 */
class RouteViewViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val oct14 = LocalDate(2026, 10, 14)
    private val oct15 = LocalDate(2026, 10, 15)
    private val oct16 = LocalDate(2026, 10, 16)
    private val kyoto = testItinerary("kyoto", title = "京都大阪 5 日遊").copy(startDate = oct14, endDate = LocalDate(2026, 10, 18))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun item(id: String, location: String, date: LocalDate) =
        testItem(id, "kyoto", date = date, locationName = location)

    private fun viewModelWith(vararg items: ItineraryItem, trip: Itinerary = kyoto): RouteViewViewModel {
        val stored = InMemoryItineraryItemRepository(*items)
        val useCases = TestUseCases(InMemoryItineraryRepository(stored, trip), stored)
        return RouteViewViewModel(useCases.getRouteForItinerary)
    }

    private fun TestScope.act(viewModel: RouteViewViewModel, vararg intents: RouteViewIntent) {
        intents.forEach {
            viewModel.processIntent(it)
            advanceUntilIdle()
        }
    }

    private fun TestScope.collectEvents(viewModel: RouteViewViewModel): List<RouteViewEvent> {
        val events = mutableListOf<RouteViewEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.collect { events += it }
        }
        return events
    }

    /** 按分享後送出的文字，一行一個元素 */
    private fun TestScope.sharedLines(viewModel: RouteViewViewModel): List<String> {
        val events = collectEvents(viewModel)
        act(viewModel, RouteViewIntent.LoadRoute("kyoto"), RouteViewIntent.ExportRoute("kyoto"))
        val exported = events.filterIsInstance<RouteViewEvent.ExportSuccess>()
        exported shouldHaveSize 1
        return exported.single().text.trimEnd().lines()
    }

    @Test
    fun `打開路線頁`() = runTest(dispatcher) {
        val viewModel = viewModelWith(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15))

        act(viewModel, RouteViewIntent.LoadRoute("kyoto"))

        val state = viewModel.state.value
        state.route?.title shouldBe "京都大阪 5 日遊"
        state.route?.days?.flatMap { day -> day.stops.map { it.location.name } } shouldBe listOf("伏見稻荷", "金閣寺")
        state.isLoading shouldBe false
        state.error shouldBe null
    }

    @Test
    fun `找不到行程`() = runTest(dispatcher) {
        val viewModel = viewModelWith()

        act(viewModel, RouteViewIntent.LoadRoute("missing"))

        viewModel.state.value.route shouldBe null
        viewModel.state.value.error shouldBe "找不到路線"
    }

    // ── 規格調整第 5 組：分享可讀的文字 ──

    @Test
    fun `分享可讀的文字`() = runTest(dispatcher) {
        val fushimi = item("fushimi", "伏見稻荷", oct14).copy(
            location = Location(name = "伏見稻荷", latitude = 34.9671, longitude = 135.7727, address = "京都市伏見區深草藪之內町68"),
        )
        val viewModel = viewModelWith(
            fushimi,
            item("gion", "祇園", oct14),
            item("kinkaku", "金閣寺", oct15),
            item("arashiyama", "嵐山", oct15),
            item("kiyomizu", "清水寺", oct16),
        )

        sharedLines(viewModel) shouldBe listOf(
            "京都大阪 5 日遊",
            "第 1 天：伏見稻荷 → 祇園",
            "第 2 天：金閣寺 → 嵐山",
            "第 3 天：清水寺",
        )
    }

    @Test
    fun `行程範圍外的項目用日期標示`() = runTest(dispatcher) {
        val viewModel = viewModelWith(
            item("fushimi", "伏見稻荷", oct14),
            item("arashiyama", "嵐山", LocalDate(2026, 10, 17)),
            trip = kyoto.copy(endDate = oct16),
        )

        sharedLines(viewModel) shouldBe listOf(
            "京都大阪 5 日遊",
            "第 1 天：伏見稻荷",
            "2026/10/17：嵐山",
        )
    }
}

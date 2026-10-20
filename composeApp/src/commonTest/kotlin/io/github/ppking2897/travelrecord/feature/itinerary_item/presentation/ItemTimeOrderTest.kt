@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.feature.itinerary_item.presentation

import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.instantAt
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * 離開時間不能早於到達時間，測試名稱對應 B 組修正討論時定下的情境
 */
class ItemTimeOrderTest {

    private val dispatcher = StandardTestDispatcher()

    private val kyoto = testItinerary("kyoto", title = "京都大阪 5 日遊")
    private val kinkaku = testItem(
        "kinkaku", "kyoto",
        arrivalTime = LocalTime(10, 0),
        departureTime = LocalTime(11, 0),
        locationName = "金閣寺",
    )

    /** 修正前從編輯畫面存進去、離開早於到達的項目 */
    private val ginkaku = testItem(
        "ginkaku", "kyoto",
        arrivalTime = LocalTime(15, 0),
        departureTime = LocalTime(14, 0),
        locationName = "銀閣寺",
    )

    private val useCases = run {
        val itemRepository = InMemoryItineraryItemRepository(kinkaku, ginkaku)
        TestUseCases(InMemoryItineraryRepository(itemRepository, kyoto), itemRepository)
    }

    private val timeOrderError = "離開時間不能早於到達時間"

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `新增項目時離開早於到達`() = runTest(dispatcher) {
        val viewModel = AddEditItemViewModel(useCases.addItem, useCases.getItinerary, useCases.imageStorage)
        val events = mutableListOf<AddEditItemEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events += it } }

        act(
            { viewModel.processIntent(AddEditItemIntent.LoadItinerary("kyoto")) },
            { viewModel.processIntent(AddEditItemIntent.UpdateActivity("吃午餐")) },
            { viewModel.processIntent(AddEditItemIntent.UpdateLocationName("錦市場")) },
            { viewModel.processIntent(AddEditItemIntent.UpdateDate(LocalDate(2026, 10, 1))) },
            { viewModel.processIntent(AddEditItemIntent.UpdateArrivalTime(LocalTime(12, 0))) },
            { viewModel.processIntent(AddEditItemIntent.UpdateDepartureTime(LocalTime(11, 30))) },
            { viewModel.processIntent(AddEditItemIntent.Save) },
        )

        events shouldBe listOf(AddEditItemEvent.ShowError(timeOrderError))
        viewModel.state.value.error shouldBe timeOrderError
        useCases.getItemsByItinerary("kyoto").getOrThrow().map { it.id } shouldBe listOf("kinkaku", "ginkaku")
    }

    @Test
    fun `編輯項目時離開早於到達`() = runTest(dispatcher) {
        val viewModel = editViewModel()
        val events = mutableListOf<EditItemEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events += it } }

        act(
            { viewModel.processIntent(EditItemIntent.LoadItem("kinkaku")) },
            { viewModel.processIntent(EditItemIntent.UpdateDepartureTime(LocalTime(9, 30))) },
            { viewModel.processIntent(EditItemIntent.Save) },
        )

        events shouldBe listOf(EditItemEvent.ShowError(timeOrderError))
        viewModel.state.value.error shouldBe timeOrderError
        useCases.getItem("kinkaku").getOrThrow()?.departureTime shouldBe LocalTime(11, 0)
    }

    @Test
    fun `編輯項目時離開等於到達`() = runTest(dispatcher) {
        val viewModel = editViewModel()
        val events = mutableListOf<EditItemEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events += it } }

        act(
            { viewModel.processIntent(EditItemIntent.LoadItem("kinkaku")) },
            { viewModel.processIntent(EditItemIntent.UpdateDepartureTime(LocalTime(10, 0))) },
            { viewModel.processIntent(EditItemIntent.Save) },
        )

        events shouldBe listOf(EditItemEvent.SaveSuccess)
        useCases.getItem("kinkaku").getOrThrow()?.departureTime shouldBe LocalTime(10, 0)
    }

    @Test
    fun `時間顛倒的舊項目仍能標記完成`() = runTest(dispatcher) {
        val completed = ginkaku.copy(isCompleted = true, completedAt = instantAt(1))

        useCases.updateItem(completed, instantAt(1)).isSuccess shouldBe true

        useCases.getItem("ginkaku").getOrThrow()?.isCompleted shouldBe true
    }

    private fun editViewModel() = EditItemViewModel(
        useCases.updateItem,
        useCases.getItem,
        useCases.getItinerary,
        useCases.imageStorage,
    )

    /** 依序送出指令，每送一個就把排程跑完 */
    private fun TestScope.act(vararg sends: () -> Unit) {
        sends.forEach {
            it()
            advanceUntilIdle()
        }
    }
}

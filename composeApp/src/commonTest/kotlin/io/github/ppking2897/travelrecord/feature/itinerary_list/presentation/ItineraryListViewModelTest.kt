@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.feature.itinerary_list.presentation

import io.github.ppking2897.travelrecord.core.domain.service.LegacyDataImporter
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.test.FailingDeleteItineraryRepository
import io.github.ppking2897.travelrecord.test.FakeLegacyDataImporter
import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.SwitchableReadItineraryRepository
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import kotlin.time.ExperimentalTime

/**
 * 行程列表的「邏輯」Scenario，測試名稱對應 .claude/docs/ItineraryList.md 的 Scenario
 */
class ItineraryListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val kyoto = testItinerary("kyoto", title = "京都大阪 5 日遊", createdAtMs = 2_000)
    private val tokyo = testItinerary("tokyo", title = "東京 3 日遊", createdAtMs = 1_000)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── 啟動時先準備本機資料，再載入行程 ──

    @Test
    fun `準備完成後列出行程`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf(listOf(kyoto, tokyo)))

        advanceUntilIdle()

        viewModel.state.value.titles shouldBe listOf("京都大阪 5 日遊", "東京 3 日遊")
        viewModel.state.value.isLoading shouldBe false
        viewModel.state.value.error shouldBe null
    }

    @Test
    fun `資料準備失敗`() = runTest(dispatcher) {
        val importer = FakeLegacyDataImporter(Result.failure(IllegalStateException("磁碟已滿")))
        val viewModel = viewModelWith(useCasesOf(listOf(kyoto, tokyo), importer = importer))

        advanceUntilIdle()

        viewModel.state.value.error shouldBe "資料準備失敗：磁碟已滿"
        viewModel.state.value.itineraries.shouldBeEmpty()
        viewModel.state.value.isLoading shouldBe false
    }

    // ── 回到這一頁時重新讀取 ──

    @Test
    fun `回到列表時看到新資料`() = runTest(dispatcher) {
        val useCases = useCasesOf(listOf(kyoto, tokyo))
        val viewModel = viewModelWith(useCases)
        advanceUntilIdle()
        val states = collectStates(viewModel)

        useCases.itineraries.createItinerary(osaka).getOrThrow()
        act(viewModel, ItineraryListIntent.LoadItineraries)

        viewModel.state.value.titles shouldContainExactlyInAnyOrder listOf("京都大阪 5 日遊", "東京 3 日遊", "大阪美食")
        states.filter { it.isLoading }.shouldBeEmpty()
    }

    @Test
    fun `第一次讀取失敗`() = runTest(dispatcher) {
        val viewModel = viewModelWith(
            useCasesOf(listOf(kyoto, tokyo), wrap = { SwitchableReadItineraryRepository(it).apply { failReads = true } })
        )
        advanceUntilIdle()

        act(viewModel, ItineraryListIntent.LoadItineraries)

        viewModel.state.value.error shouldBe "讀取失敗"
        viewModel.state.value.itineraries.shouldBeEmpty()
    }

    @Test
    fun `已有資料時重新讀取失敗`() = runTest(dispatcher) {
        lateinit var reads: SwitchableReadItineraryRepository
        val viewModel = viewModelWith(
            useCasesOf(listOf(kyoto, tokyo), wrap = { SwitchableReadItineraryRepository(it).also { r -> reads = r } })
        )
        advanceUntilIdle()

        reads.failReads = true
        act(viewModel, ItineraryListIntent.LoadItineraries)

        viewModel.state.value.titles shouldBe listOf("京都大阪 5 日遊", "東京 3 日遊")
        viewModel.state.value.error shouldBe null
    }

    // ── 下拉重新整理 ──

    @Test
    fun `下拉重新整理`() = runTest(dispatcher) {
        val useCases = useCasesOf(listOf(kyoto, tokyo))
        val viewModel = viewModelWith(useCases)
        advanceUntilIdle()
        val states = collectStates(viewModel)

        useCases.itineraries.createItinerary(osaka).getOrThrow()
        act(viewModel, ItineraryListIntent.Refresh)

        states.map { it.isRefreshing } shouldContain true
        viewModel.state.value.isRefreshing shouldBe false
        viewModel.state.value.titles shouldHaveSize 3
    }

    @Test
    fun `重新整理失敗`() = runTest(dispatcher) {
        lateinit var reads: SwitchableReadItineraryRepository
        val viewModel = viewModelWith(
            useCasesOf(listOf(kyoto, tokyo), wrap = { SwitchableReadItineraryRepository(it).also { r -> reads = r } })
        )
        advanceUntilIdle()

        reads.failReads = true
        act(viewModel, ItineraryListIntent.Refresh)

        viewModel.state.value.isRefreshing shouldBe false
        viewModel.state.value.error shouldNotBe null
        viewModel.state.value.titles shouldBe listOf("京都大阪 5 日遊", "東京 3 日遊")
    }

    // ── 搜尋行程 ──

    @Test
    fun `用項目的地點搜尋行程`() = runTest(dispatcher) {
        val viewModel = viewModelWith(
            useCasesOf(listOf(kyoto, tokyo), items = listOf(testItem("kinkaku", "kyoto", locationName = "金閣寺")))
        )
        advanceUntilIdle()

        act(viewModel, ItineraryListIntent.Search("金閣"))

        viewModel.state.value.titles shouldBe listOf("京都大阪 5 日遊")
    }

    @Test
    fun `用項目的活動搜尋行程`() = runTest(dispatcher) {
        val viewModel = viewModelWith(
            useCasesOf(
                listOf(kyoto, tokyo),
                items = listOf(testItem("fushimi", "kyoto", locationName = "伏見稻荷大社", activity = "千本鳥居參拜"))
            )
        )
        advanceUntilIdle()

        act(viewModel, ItineraryListIntent.Search("鳥居"))

        viewModel.state.value.titles shouldBe listOf("京都大阪 5 日遊")
    }

    @Test
    fun `不分大小寫`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf(listOf(kyoto, tokyo, osakaFoodTrip)))
        advanceUntilIdle()

        act(viewModel, ItineraryListIntent.Search("osaka"))

        viewModel.state.value.titles shouldBe listOf("Osaka Food Trip")
    }

    @Test
    fun `清空搜尋回到全部行程`() = runTest(dispatcher) {
        val viewModel = viewModelWith(
            useCasesOf(listOf(kyoto, tokyo), items = listOf(testItem("kinkaku", "kyoto", locationName = "金閣寺")))
        )
        advanceUntilIdle()
        act(viewModel, ItineraryListIntent.Search("金閣"))

        act(viewModel, ItineraryListIntent.Search(""))

        viewModel.state.value.titles shouldBe listOf("京都大阪 5 日遊", "東京 3 日遊")
    }

    /** 照畫面的接法：ViewModel 搜尋後的狀態交給畫面的計算 */
    @Test
    fun `搜尋沒有結果`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf(listOf(kyoto, tokyo)))
        advanceUntilIdle()

        act(viewModel, ItineraryListIntent.Search("巴黎"))

        val state = viewModel.state.value
        val dashboard = buildItineraryDashboard(state.itineraries, state.searchQuery, ItineraryFilter.All, LocalDate(2026, 10, 16))
        dashboard.emptyState shouldBe DashboardEmptyState.NoMatch
    }

    // ── 刪除行程 ──

    @Test
    fun `確認後刪除`() = runTest(dispatcher) {
        val useCases = useCasesOf(
            listOf(kyoto, tokyo),
            items = listOf(testItem("asakusa", "tokyo"), testItem("skytree", "tokyo")),
        )
        val viewModel = viewModelWith(useCases)
        advanceUntilIdle()

        act(viewModel, ItineraryListIntent.DeleteItinerary("tokyo"))

        viewModel.state.value.titles shouldBe listOf("京都大阪 5 日遊")
        useCases.items.getItemsByItinerary("tokyo").getOrThrow().shouldBeEmpty()
    }

    @Test
    fun `刪除失敗`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf(listOf(kyoto, tokyo), wrap = { FailingDeleteItineraryRepository(it) }))
        advanceUntilIdle()
        val events = collectEvents(viewModel)

        act(viewModel, ItineraryListIntent.DeleteItinerary("tokyo"))

        events.filterIsInstance<ItineraryListEvent.ShowError>() shouldHaveSize 1
        viewModel.state.value.titles shouldBe listOf("京都大阪 5 日遊", "東京 3 日遊")
    }

    // ── 測試工具 ──

    private val osaka = testItinerary("osaka", title = "大阪美食", createdAtMs = 3_000)
    private val osakaFoodTrip = testItinerary("osaka-food", title = "Osaka Food Trip", createdAtMs = 500)

    private fun useCasesOf(
        trips: List<Itinerary>,
        items: List<ItineraryItem> = emptyList(),
        importer: LegacyDataImporter = FakeLegacyDataImporter(Result.success(Unit)),
        wrap: (ItineraryRepository) -> ItineraryRepository = { it },
    ): TestUseCases {
        val itemRepository = InMemoryItineraryItemRepository(*items.toTypedArray())
        val itineraryRepository = wrap(InMemoryItineraryRepository(itemRepository, *trips.toTypedArray()))
        return TestUseCases(itineraryRepository, itemRepository, legacyDataImporter = importer)
    }

    private fun viewModelWith(useCases: TestUseCases) = ItineraryListViewModel(
        getAllItinerariesUseCase = useCases.getAllItineraries,
        searchUseCase = useCases.searchItineraries,
        getItemsByItineraryUseCase = useCases.getItemsByItinerary,
        ensureLocalDataReadyUseCase = useCases.ensureLocalDataReady,
        deleteItineraryUseCase = useCases.deleteItinerary,
    )

    /** 依序送出指令，每送一個就把排程跑完 */
    private fun TestScope.act(viewModel: ItineraryListViewModel, vararg intents: ItineraryListIntent) {
        intents.forEach {
            viewModel.processIntent(it)
            advanceUntilIdle()
        }
    }

    private fun TestScope.collectStates(viewModel: ItineraryListViewModel): List<ItineraryListState> {
        val states = mutableListOf<ItineraryListState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { states += it }
        }
        return states
    }

    private fun TestScope.collectEvents(viewModel: ItineraryListViewModel): List<ItineraryListEvent> {
        val events = mutableListOf<ItineraryListEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.collect { events += it }
        }
        return events
    }

    private val ItineraryListState.titles: List<String>
        get() = itineraries.map { it.title }
}

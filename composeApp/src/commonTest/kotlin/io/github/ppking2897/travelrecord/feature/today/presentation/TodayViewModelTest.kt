@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.feature.today.presentation

import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.core.domain.service.LocationSuggestion
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository
import io.github.ppking2897.travelrecord.test.FailingSaveImageStorageService
import io.github.ppking2897.travelrecord.test.FailingUpdateItemRepository
import io.github.ppking2897.travelrecord.test.FakeImageStorageService
import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotContain
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
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 今日頁的「邏輯」Scenario，測試名稱對應 .claude/docs/Today.md 的 Scenario
 *
 * 面板開關、按鈕外觀等畫面行為不在這裡測。
 */
class TodayViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** 現在是 2026-10-15 15:20；時區用 UTC，所以「今天」是 10/15 */
    private val now = Instant.parse("2026-10-15T15:20:00Z")
    private val clock = object : Clock {
        override fun now(): Instant = this@TodayViewModelTest.now
    }
    private val today = LocalDate(2026, 10, 15)

    private val kyoto = trip("kyoto", "京都大阪 5 日遊", LocalDate(2026, 10, 14), LocalDate(2026, 10, 18))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── 今日頁以日期涵蓋今天的行程作為進行中的旅行 ──

    @Test
    fun `有一趟行程涵蓋今天`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf(InMemoryItineraryItemRepository(), kyoto))

        act(viewModel, TodayIntent.LoadToday)

        viewModel.state.value.today shouldBe today
        viewModel.state.value.currentTrip?.title shouldBe "京都大阪 5 日遊"
        viewModel.state.value.isLoading shouldBe false
    }

    @Test
    fun `多趟行程同時涵蓋今天`() = runTest(dispatcher) {
        val tripA = trip("a", "行程 A", LocalDate(2026, 10, 10), LocalDate(2026, 10, 20))
        val tripB = trip("b", "行程 B", LocalDate(2026, 10, 14), LocalDate(2026, 10, 16))
        val viewModel = viewModelWith(useCasesOf(InMemoryItineraryItemRepository(), tripA, tripB))

        act(viewModel, TodayIntent.LoadToday)

        viewModel.state.value.currentTrip?.id shouldBe "b"
    }

    @Test
    fun `沒有行程涵蓋今天`() = runTest(dispatcher) {
        val later = trip("later", "之後的旅行", LocalDate(2026, 11, 1), LocalDate(2026, 11, 5))
        val viewModel = viewModelWith(useCasesOf(InMemoryItineraryItemRepository(), later))

        act(viewModel, TodayIntent.LoadToday)

        viewModel.state.value.currentTrip shouldBe null
        viewModel.state.value.today shouldBe today
    }

    @Test
    fun `沒填日期的行程不會成為進行中的旅行`() = runTest(dispatcher) {
        val startOnly = trip("start-only", "只填開始日", today, null)
        val noDates = trip("no-dates", "沒填日期", null, null)
        val viewModel = viewModelWith(useCasesOf(InMemoryItineraryItemRepository(), startOnly, noDates))

        act(viewModel, TodayIntent.LoadToday)

        viewModel.state.value.currentTrip shouldBe null
    }

    // ── 只列出進行中行程裡日期是今天的項目，並依時間排序 ──

    @Test
    fun `只顯示今天的項目`() = runTest(dispatcher) {
        val viewModel = kyotoViewModelWith(
            item("fushimi", "伏見稻荷", date = LocalDate(2026, 10, 14)),
            item("kiyomizu", "清水寺"),
            item("gion", "祇園晚餐"),
        )

        act(viewModel, TodayIntent.LoadToday)

        viewModel.state.value.itemNames shouldContainExactlyInAnyOrder listOf("清水寺", "祇園晚餐")
    }

    @Test
    fun `依抵達時間排序，沒有抵達時間時用出發時間`() = runTest(dispatcher) {
        val viewModel = kyotoViewModelWith(
            item("kiyomizu", "清水寺", arrival = LocalTime(14, 30)),
            item("fushimi", "伏見稻荷", departure = LocalTime(12, 0)),
        )

        act(viewModel, TodayIntent.LoadToday)

        viewModel.state.value.itemNames shouldBe listOf("伏見稻荷", "清水寺")
    }

    @Test
    fun `沒排時間的項目排在最後面`() = runTest(dispatcher) {
        val viewModel = kyotoViewModelWith(
            item("gion", "祇園晚餐"),
            item("kiyomizu", "清水寺", arrival = LocalTime(14, 30)),
            item("nishiki", "錦市場", arrival = LocalTime(14, 30)),
            item("fushimi", "伏見稻荷", departure = LocalTime(9, 0)),
            item("yasaka", "八坂神社"),
        )

        act(viewModel, TodayIntent.LoadToday)

        viewModel.state.value.itemNames shouldBe listOf("伏見稻荷", "清水寺", "錦市場", "祇園晚餐", "八坂神社")
    }

    @Test
    fun `顯示今日進度`() = runTest(dispatcher) {
        val viewModel = kyotoViewModelWith(
            item("kiyomizu", "清水寺", arrival = LocalTime(9, 0)).copy(isCompleted = true),
            item("fushimi", "伏見稻荷", arrival = LocalTime(12, 0)),
            item("gion", "祇園晚餐", arrival = LocalTime(18, 0)),
        )

        act(viewModel, TodayIntent.LoadToday)

        viewModel.state.value.progressText shouldBe "1/3 已完成"
    }

    @Test
    fun `今天沒有項目`() = runTest(dispatcher) {
        val viewModel = kyotoViewModelWith(item("fushimi", "伏見稻荷", date = LocalDate(2026, 10, 14)))

        act(viewModel, TodayIntent.LoadToday)

        viewModel.state.value.todayItems.shouldBeEmpty()
        viewModel.state.value.progressText shouldBe "今天還沒有安排"
    }

    // ── 打卡切換項目的完成狀態 ──

    @Test
    fun `打卡`() = runTest(dispatcher) {
        val items = InMemoryItineraryItemRepository(item("kiyomizu", "清水寺"))
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)

        act(viewModel, TodayIntent.ToggleItemCompletion("kiyomizu"))

        viewModel.state.value.item("清水寺").isCompleted shouldBe true
        viewModel.state.value.completedCount shouldBe 1
        val saved = items.getItem("kiyomizu").getOrThrow()!!
        saved.isCompleted shouldBe true
        saved.completedAt shouldBe now
    }

    @Test
    fun `取消打卡`() = runTest(dispatcher) {
        val items = InMemoryItineraryItemRepository(
            item("kiyomizu", "清水寺").copy(isCompleted = true, completedAt = Instant.parse("2026-10-15T09:00:00Z"))
        )
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)

        act(viewModel, TodayIntent.ToggleItemCompletion("kiyomizu"))

        viewModel.state.value.item("清水寺").isCompleted shouldBe false
        val saved = items.getItem("kiyomizu").getOrThrow()!!
        saved.isCompleted shouldBe false
        saved.completedAt shouldBe null
    }

    @Test
    fun `打卡存檔失敗`() = runTest(dispatcher) {
        val items = FailingUpdateItemRepository(InMemoryItineraryItemRepository(item("kiyomizu", "清水寺")))
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)
        val states = collectStates(viewModel)
        val messages = collectMessages(viewModel)

        act(viewModel, TodayIntent.ToggleItemCompletion("kiyomizu"))

        states.map { it.item("清水寺").isCompleted } shouldContain true
        viewModel.state.value.item("清水寺").isCompleted shouldBe false
        messages.shouldNotBeEmpty()
    }

    // ── 快速紀錄新增一筆今天的項目 ──

    @Test
    fun `成功儲存`() = runTest(dispatcher) {
        val items = InMemoryItineraryItemRepository()
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)
        val messages = collectMessages(viewModel)

        act(
            viewModel,
            TodayIntent.UpdateQuickActivity("喝抹茶"),
            TodayIntent.UpdateQuickLocation("中村藤吉"),
            TodayIntent.SaveQuickRecord,
        )

        val saved = items.getItemsByItinerary("kyoto").getOrThrow().single()
        saved.activity shouldBe "喝抹茶"
        saved.location.name shouldBe "中村藤吉"
        saved.date shouldBe today
        saved.arrivalTime shouldBe LocalTime(15, 20)
        viewModel.state.value.quickRecord shouldBe QuickRecordState()
        viewModel.state.value.itemNames shouldContain "中村藤吉"
        messages shouldContain "已加入今日紀錄"
    }

    @Test
    fun `活動沒填`() = runTest(dispatcher) {
        val items = InMemoryItineraryItemRepository()
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)

        act(viewModel, TodayIntent.UpdateQuickLocation("中村藤吉"), TodayIntent.SaveQuickRecord)

        viewModel.state.value.quickRecord.activityError shouldBe "請輸入活動"
        items.getItemsByItinerary("kyoto").getOrThrow().shouldBeEmpty()
    }

    @Test
    fun `活動有填、地點沒填`() = runTest(dispatcher) {
        val items = InMemoryItineraryItemRepository()
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)

        act(viewModel, TodayIntent.UpdateQuickActivity("喝抹茶"), TodayIntent.SaveQuickRecord)

        viewModel.state.value.quickRecord.locationError shouldBe "請輸入地點"
        items.getItemsByItinerary("kyoto").getOrThrow().shouldBeEmpty()
    }

    @Test
    fun `從搜尋建議選地點`() = runTest(dispatcher) {
        val items = InMemoryItineraryItemRepository()
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)

        act(viewModel, TodayIntent.SelectQuickLocation(nakamuraTokichi))

        viewModel.state.value.quickRecord.locationName shouldBe "中村藤吉 本店"
        viewModel.state.value.quickRecord.locationAddress shouldBe "京都府宇治市宇治壱番10"

        act(viewModel, TodayIntent.UpdateQuickActivity("喝抹茶"), TodayIntent.SaveQuickRecord)

        val location = items.getItemsByItinerary("kyoto").getOrThrow().single().location
        location.name shouldBe "中村藤吉 本店"
        location.address shouldBe "京都府宇治市宇治壱番10"
        location.latitude shouldBe 34.8893
        location.longitude shouldBe 135.8076
    }

    @Test
    fun `選了建議後又手動修改地點文字`() = runTest(dispatcher) {
        val viewModel = kyotoViewModelWith()
        act(viewModel, TodayIntent.LoadToday)
        act(viewModel, TodayIntent.SelectQuickLocation(nakamuraTokichi))

        act(viewModel, TodayIntent.UpdateQuickLocation("中村藤吉"))

        val quickRecord = viewModel.state.value.quickRecord
        quickRecord.locationName shouldBe "中村藤吉"
        quickRecord.locationAddress shouldBe null
        quickRecord.locationLatitude shouldBe null
        quickRecord.locationLongitude shouldBe null
    }

    @Test
    fun `加入照片`() = runTest(dispatcher) {
        val items = InMemoryItineraryItemRepository()
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)
        val messages = collectMessages(viewModel)

        act(viewModel, TodayIntent.AddQuickPhoto(byteArrayOf(1)), TodayIntent.AddQuickPhoto(byteArrayOf(2)))

        messages shouldBe listOf("已加入照片", "已加入照片")
        viewModel.state.value.quickRecord.photoPaths shouldHaveSize 2

        act(
            viewModel,
            TodayIntent.UpdateQuickActivity("喝抹茶"),
            TodayIntent.UpdateQuickLocation("中村藤吉"),
            TodayIntent.SaveQuickRecord,
        )

        items.getItemsByItinerary("kyoto").getOrThrow().single().photos shouldHaveSize 2
    }

    @Test
    fun `沒儲存就關掉面板`() = runTest(dispatcher) {
        val viewModel = kyotoViewModelWith()
        act(viewModel, TodayIntent.LoadToday)

        act(viewModel, TodayIntent.UpdateQuickActivity("喝抹茶"), TodayIntent.Refresh)

        viewModel.state.value.quickRecord.activity shouldBe "喝抹茶"
    }

    // ── 編輯項目備註 ──

    @Test
    fun `更新備註`() = runTest(dispatcher) {
        val items = InMemoryItineraryItemRepository(item("kiyomizu", "清水寺").copy(notes = "人很多"))
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)
        val messages = collectMessages(viewModel)

        act(viewModel, TodayIntent.StartEditingNote("kiyomizu"))

        viewModel.state.value.editingNoteItemId shouldBe "kiyomizu"
        viewModel.state.value.editingNoteText shouldBe "人很多"

        act(viewModel, TodayIntent.UpdateEditingNote("人很多，早點去"), TodayIntent.SaveEditingNote)

        viewModel.state.value.editingNoteItemId shouldBe null
        viewModel.state.value.item("清水寺").notes shouldBe "人很多，早點去"
        messages shouldContain "備註已更新"
    }

    @Test
    fun `取消編輯`() = runTest(dispatcher) {
        val items = InMemoryItineraryItemRepository(item("kiyomizu", "清水寺").copy(notes = "人很多"))
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)

        act(
            viewModel,
            TodayIntent.StartEditingNote("kiyomizu"),
            TodayIntent.UpdateEditingNote("改掉的備註"),
            TodayIntent.CancelEditingNote,
        )

        viewModel.state.value.editingNoteItemId shouldBe null
        viewModel.state.value.item("清水寺").notes shouldBe "人很多"
        items.getItem("kiyomizu").getOrThrow()!!.notes shouldBe "人很多"
    }

    @Test
    fun `備註存檔失敗`() = runTest(dispatcher) {
        val items = FailingUpdateItemRepository(InMemoryItineraryItemRepository(item("kiyomizu", "清水寺").copy(notes = "人很多")))
        val viewModel = viewModelWith(useCasesOf(items, kyoto))
        act(viewModel, TodayIntent.LoadToday)
        val messages = collectMessages(viewModel)

        act(
            viewModel,
            TodayIntent.StartEditingNote("kiyomizu"),
            TodayIntent.UpdateEditingNote("人很多，早點去"),
            TodayIntent.SaveEditingNote,
        )

        messages.shouldNotBeEmpty()
        viewModel.state.value.editingNoteItemId shouldBe "kiyomizu"
    }

    // ── 替今天的項目加照片 ──

    @Test
    fun `選好照片後加到項目`() = runTest(dispatcher) {
        val viewModel = kyotoViewModelWith(item("kiyomizu", "清水寺"))
        act(viewModel, TodayIntent.LoadToday)
        val messages = collectMessages(viewModel)

        act(viewModel, TodayIntent.AddPhotoToItem("kiyomizu", byteArrayOf(1, 2, 3)))

        viewModel.state.value.item("清水寺").photos shouldHaveSize 1
        messages shouldContain "已加入照片"
    }

    @Test
    fun `加照片失敗`() = runTest(dispatcher) {
        val useCases = useCasesOf(
            InMemoryItineraryItemRepository(item("kiyomizu", "清水寺")),
            kyoto,
            imageStorage = FailingSaveImageStorageService(),
        )
        val viewModel = viewModelWith(useCases)
        act(viewModel, TodayIntent.LoadToday)
        val messages = collectMessages(viewModel)

        act(viewModel, TodayIntent.AddPhotoToItem("kiyomizu", byteArrayOf(1, 2, 3)))

        messages.shouldNotBeEmpty()
        messages shouldNotContain "已加入照片"
        viewModel.state.value.item("清水寺").photos.shouldBeEmpty()
    }

    // ── 測試工具 ──

    private val nakamuraTokichi = LocationSuggestion(
        placeId = "place-nakamura",
        name = "中村藤吉 本店",
        address = "京都府宇治市宇治壱番10",
        latitude = 34.8893,
        longitude = 135.8076,
    )

    private fun viewModelWith(useCases: TestUseCases) = TodayViewModel(
        getCurrentTripUseCase = useCases.getCurrentTrip,
        getItemsByItineraryUseCase = useCases.getItemsByItinerary,
        addItineraryItemUseCase = useCases.addItem,
        updateItineraryItemUseCase = useCases.updateItem,
        addPhotoUseCase = useCases.addPhoto,
        generateThumbnailUseCase = useCases.generateThumbnail,
        imageStorageService = useCases.imageStorage,
        clock = clock,
        timeZone = TimeZone.UTC,
    )

    /** 行程與項目共用同一個項目 Repository，跟 App 裡的資料庫一樣 */
    private fun useCasesOf(
        items: ItineraryItemRepository,
        vararg trips: Itinerary,
        imageStorage: ImageStorageService = FakeImageStorageService(),
    ) = TestUseCases(InMemoryItineraryRepository(items, *trips), items, imageStorage)

    private fun kyotoViewModelWith(vararg items: ItineraryItem) =
        viewModelWith(useCasesOf(InMemoryItineraryItemRepository(*items), kyoto))

    /** 依序送出指令，每送一個就把排程跑完 */
    private fun TestScope.act(viewModel: TodayViewModel, vararg intents: TodayIntent) {
        intents.forEach {
            viewModel.processIntent(it)
            advanceUntilIdle()
        }
    }

    private fun TestScope.collectMessages(viewModel: TodayViewModel): List<String> {
        val messages = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.collect { if (it is TodayEvent.ShowMessage) messages += it.message }
        }
        return messages
    }

    private fun TestScope.collectStates(viewModel: TodayViewModel): List<TodayState> {
        val states = mutableListOf<TodayState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { states += it }
        }
        return states
    }

    private fun trip(id: String, title: String, start: LocalDate?, end: LocalDate?): Itinerary =
        testItinerary(id = id, title = title).copy(startDate = start, endDate = end)

    private fun item(
        id: String,
        location: String,
        date: LocalDate = today,
        arrival: LocalTime? = null,
        departure: LocalTime? = null,
    ): ItineraryItem =
        testItem(id, "kyoto", date = date, arrivalTime = arrival, departureTime = departure, locationName = location)

    private val TodayState.itemNames: List<String>
        get() = todayItems.map { it.location.name }

    private fun TodayState.item(locationName: String): ItineraryItem =
        todayItems.single { it.location.name == locationName }
}

@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.feature.itinerary_add_edit.presentation

import io.github.ppking2897.travelrecord.feature.draft.domain.model.DraftType
import io.github.ppking2897.travelrecord.feature.draft.domain.repository.DraftRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.test.CountingDraftRepository
import io.github.ppking2897.travelrecord.test.FailingUpdateItineraryRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.RecordingImageStorageService
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.inMemoryDraftRepository
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * 建立與編輯行程的「邏輯」情境，測試名稱對應 A 組修正討論時定下的情境
 */
class ItineraryAddEditViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** 原本的封面是 A */
    private val coverA = "covers/A.jpg"
    private val kyoto = testItinerary("kyoto", title = "京都大阪 5 日遊").copy(coverPhotoPath = coverA)

    private val imageStorage = RecordingImageStorageService()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── 封面：原本的封面檔等儲存成功才刪 ──

    @Test
    fun `換封面但沒儲存`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(viewModel, ItineraryAddEditIntent.LoadItinerary("kyoto"), ItineraryAddEditIntent.SetCoverPhoto(photoBytes))

        imageStorage.deleted.shouldBeEmpty()
    }

    @Test
    fun `換封面後儲存`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.SetCoverPhoto(photoBytes),
            ItineraryAddEditIntent.Save,
        )

        val coverB = imageStorage.saved.single()
        imageStorage.deleted shouldBe listOf(coverA)
        useCases.getItinerary("kyoto").getOrThrow()?.coverPhotoPath shouldBe coverB
    }

    @Test
    fun `沒換封面直接儲存`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.UpdateTitle("京都大阪 6 日遊"),
            ItineraryAddEditIntent.Save,
        )

        imageStorage.deleted.shouldBeEmpty()
    }

    @Test
    fun `換封面後儲存失敗`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf(wrap = ::FailingUpdateItineraryRepository))

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.SetCoverPhoto(photoBytes),
            ItineraryAddEditIntent.Save,
        )

        imageStorage.deleted.shouldBeEmpty()
    }

    @Test
    fun `先挑一張再換一張`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.SetCoverPhoto(photoBytes),
            ItineraryAddEditIntent.SetCoverPhoto(photoBytes),
        )

        val coverB = imageStorage.saved.first()
        imageStorage.deleted shouldBe listOf(coverB)
    }

    @Test
    fun `移除封面但沒儲存`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(viewModel, ItineraryAddEditIntent.LoadItinerary("kyoto"), ItineraryAddEditIntent.RemoveCoverPhoto)

        imageStorage.deleted.shouldBeEmpty()
    }

    @Test
    fun `移除封面後儲存`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.RemoveCoverPhoto,
            ItineraryAddEditIntent.Save,
        )

        imageStorage.deleted shouldBe listOf(coverA)
        useCases.getItinerary("kyoto").getOrThrow()?.coverPhotoPath shouldBe null
    }

    // ── 草稿：新增行程時停下打字 1 秒自動存 ──

    @Test
    fun `連續打字只在停下後存一次`() = runTest(dispatcher) {
        val drafts = CountingDraftRepository(inMemoryDraftRepository())
        val useCases = useCasesOf(drafts = drafts)
        val viewModel = viewModelWith(useCases)
        act(viewModel, ItineraryAddEditIntent.LoadDraft)

        type(viewModel, "京")
        advanceTimeBy(500)
        type(viewModel, "京都")
        advanceTimeBy(500)
        type(viewModel, "京都大阪")
        advanceTimeBy(999)
        runCurrent()
        drafts.saveCount shouldBe 0

        advanceTimeBy(2)
        runCurrent()
        drafts.saveCount shouldBe 1
        useCases.loadDraft(DraftType.ITINERARY).getOrThrow()?.get("title") shouldBe "京都大阪"
    }

    @Test
    fun `存好後提示三秒後消失`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())
        act(viewModel, ItineraryAddEditIntent.LoadDraft)

        type(viewModel, "京都大阪")
        advanceTimeBy(1001)
        runCurrent()
        viewModel.state.value.showDraftSaved shouldBe true

        advanceTimeBy(2998)
        runCurrent()
        viewModel.state.value.showDraftSaved shouldBe true

        advanceTimeBy(2)
        runCurrent()
        viewModel.state.value.showDraftSaved shouldBe false
    }

    @Test
    fun `離開再打開帶回草稿`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val first = viewModelWith(useCases)
        act(
            first,
            ItineraryAddEditIntent.LoadDraft,
            ItineraryAddEditIntent.UpdateTitle("京都大阪 5 日遊"),
            ItineraryAddEditIntent.UpdateDescription("秋天賞楓"),
            ItineraryAddEditIntent.UpdateStartDate(LocalDate(2026, 11, 20)),
            ItineraryAddEditIntent.UpdateEndDate(LocalDate(2026, 11, 24)),
        )

        val second = viewModelWith(useCases)
        act(second, ItineraryAddEditIntent.LoadDraft)

        second.state.value.title shouldBe "京都大阪 5 日遊"
        second.state.value.description shouldBe "秋天賞楓"
        second.state.value.startDate shouldBe LocalDate(2026, 11, 20)
        second.state.value.endDate shouldBe LocalDate(2026, 11, 24)
    }

    @Test
    fun `建立成功後清掉草稿`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val first = viewModelWith(useCases)
        act(
            first,
            ItineraryAddEditIntent.LoadDraft,
            ItineraryAddEditIntent.UpdateTitle("京都大阪 5 日遊"),
            *datesOfNewTrip,
            ItineraryAddEditIntent.Save,
        )

        val second = viewModelWith(useCases)
        act(second, ItineraryAddEditIntent.LoadDraft)

        second.state.value.title shouldBe ""
    }

    @Test
    fun `打字後馬上建立`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val first = viewModelWith(useCases)
        act(first, ItineraryAddEditIntent.LoadDraft, *datesOfNewTrip)
        type(first, "京都大阪 5 日遊")
        act(first, ItineraryAddEditIntent.Save)

        val second = viewModelWith(useCases)
        act(second, ItineraryAddEditIntent.LoadDraft)

        second.state.value.title shouldBe ""
    }

    @Test
    fun `編輯行程時不存草稿`() = runTest(dispatcher) {
        val drafts = CountingDraftRepository(inMemoryDraftRepository())
        val viewModel = viewModelWith(useCasesOf(drafts = drafts))

        act(viewModel, ItineraryAddEditIntent.LoadItinerary("kyoto"), ItineraryAddEditIntent.UpdateTitle("京都大阪 6 日遊"))

        drafts.saveCount shouldBe 0
    }

    // ── 捨棄：清掉草稿再離開 ──

    @Test
    fun `按捨棄後清掉草稿`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val first = viewModelWith(useCases)
        val events = eventsOf(first)
        act(first, ItineraryAddEditIntent.LoadDraft, ItineraryAddEditIntent.UpdateTitle("京都大阪 5 日遊"))
        useCases.loadDraft(DraftType.ITINERARY).getOrThrow()?.get("title") shouldBe "京都大阪 5 日遊"

        act(first, ItineraryAddEditIntent.Discard)

        events.last() shouldBe ItineraryAddEditEvent.NavigateBack
        val second = viewModelWith(useCases)
        act(second, ItineraryAddEditIntent.LoadDraft)
        second.state.value.title shouldBe ""
    }

    @Test
    fun `打字後馬上捨棄`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val first = viewModelWith(useCases)
        val events = eventsOf(first)
        act(first, ItineraryAddEditIntent.LoadDraft)
        type(first, "京都大阪 5 日遊")

        act(first, ItineraryAddEditIntent.Discard)

        events.last() shouldBe ItineraryAddEditEvent.NavigateBack
        val second = viewModelWith(useCases)
        act(second, ItineraryAddEditIntent.LoadDraft)
        second.state.value.title shouldBe ""
    }

    @Test
    fun `編輯行程時捨棄不動新增行程的草稿`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        act(viewModelWith(useCases), ItineraryAddEditIntent.LoadDraft, ItineraryAddEditIntent.UpdateTitle("京都大阪 5 日遊"))
        val editing = viewModelWith(useCases)
        val events = eventsOf(editing)

        act(editing, ItineraryAddEditIntent.LoadItinerary("kyoto"), ItineraryAddEditIntent.Discard)

        events.last() shouldBe ItineraryAddEditEvent.NavigateBack
        useCases.loadDraft(DraftType.ITINERARY).getOrThrow()?.get("title") shouldBe "京都大阪 5 日遊"
    }

    // ── 規格調整第 3 組：有修改才提醒捨棄（.claude/docs/SpecAdjustments.md）──

    @Test
    fun `編輯行程沒改就離開`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(viewModel, ItineraryAddEditIntent.LoadItinerary("kyoto"))

        viewModel.state.value.hasUnsavedChanges shouldBe false
    }

    @Test
    fun `改回原樣不算修改`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.UpdateTitle("京都 5 日遊"),
            ItineraryAddEditIntent.UpdateTitle("京都大阪 5 日遊"),
        )

        viewModel.state.value.hasUnsavedChanges shouldBe false
    }

    @Test
    fun `行程的每一欄都算`() = runTest(dispatcher) {
        val edits = listOf(
            "標題" to ItineraryAddEditIntent.UpdateTitle("京都 5 日遊"),
            "描述" to ItineraryAddEditIntent.UpdateDescription("秋天賞楓"),
            "開始日期" to ItineraryAddEditIntent.UpdateStartDate(LocalDate(2026, 10, 2)),
            "結束日期" to ItineraryAddEditIntent.UpdateEndDate(LocalDate(2026, 10, 6)),
            "換封面" to ItineraryAddEditIntent.SetCoverPhoto(photoBytes),
            "移除封面" to ItineraryAddEditIntent.RemoveCoverPhoto,
        )

        edits.forEach { (field, edit) ->
            val viewModel = viewModelWith(useCasesOf())
            act(viewModel, ItineraryAddEditIntent.LoadItinerary("kyoto"), edit)
            withClue(field) { viewModel.state.value.hasUnsavedChanges shouldBe true }
        }
    }

    @Test
    fun `新增時照舊`() = runTest(dispatcher) {
        val blank = viewModelWith(useCasesOf())
        act(blank, ItineraryAddEditIntent.LoadDraft)
        blank.state.value.hasUnsavedChanges shouldBe false

        val edits = listOf(
            "標題" to ItineraryAddEditIntent.UpdateTitle("京都大阪 5 日遊"),
            "描述" to ItineraryAddEditIntent.UpdateDescription("秋天賞楓"),
            "開始日期" to ItineraryAddEditIntent.UpdateStartDate(LocalDate(2026, 11, 20)),
            "結束日期" to ItineraryAddEditIntent.UpdateEndDate(LocalDate(2026, 11, 24)),
            "封面" to ItineraryAddEditIntent.SetCoverPhoto(photoBytes),
        )

        edits.forEach { (field, edit) ->
            val viewModel = viewModelWith(useCasesOf())
            act(viewModel, ItineraryAddEditIntent.LoadDraft, edit)
            withClue(field) { viewModel.state.value.hasUnsavedChanges shouldBe true }
        }
    }

    @Test
    fun `畫面重建後修改還在`() = runTest(dispatcher) {
        // 編輯行程：改了標題，畫面重建時會再送一次讀取
        val editing = viewModelWith(useCasesOf())
        act(editing, ItineraryAddEditIntent.LoadItinerary("kyoto"), ItineraryAddEditIntent.UpdateTitle("京都 5 日遊"))
        act(editing, ItineraryAddEditIntent.LoadItinerary("kyoto"))

        editing.state.value.title shouldBe "京都 5 日遊"
        editing.state.value.hasUnsavedChanges shouldBe true

        // 新增行程：草稿裡是之前存的「京都」，剛打的字還沒存成草稿就重建
        val useCases = useCasesOf()
        act(viewModelWith(useCases), ItineraryAddEditIntent.LoadDraft, ItineraryAddEditIntent.UpdateTitle("京都"))
        val adding = viewModelWith(useCases)
        act(adding, ItineraryAddEditIntent.LoadDraft)
        type(adding, "東京 3 日遊")
        act(adding, ItineraryAddEditIntent.LoadDraft)

        adding.state.value.title shouldBe "東京 3 日遊"
        adding.state.value.hasUnsavedChanges shouldBe true
    }

    @Test
    fun `連按儲存只存一次`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)
        act(viewModel, ItineraryAddEditIntent.LoadDraft, ItineraryAddEditIntent.UpdateTitle("東京 3 日遊"), *datesOfNewTrip)

        // 很快地連按兩下，第二下送出時第一下還沒跑
        viewModel.processIntent(ItineraryAddEditIntent.Save)
        viewModel.processIntent(ItineraryAddEditIntent.Save)
        advanceUntilIdle()

        useCases.titles().count { it == "東京 3 日遊" } shouldBe 1
    }

    // ── 規格調整第 4 組：行程一定要有日期（.claude/docs/SpecAdjustments.md）──

    @Test
    fun `新增行程沒選日期不能儲存`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)

        act(viewModel, ItineraryAddEditIntent.LoadDraft, ItineraryAddEditIntent.UpdateTitle("東京 3 日遊"), ItineraryAddEditIntent.Save)

        useCases.titles() shouldNotContain "東京 3 日遊"
        viewModel.state.value.dateError shouldBe "請選擇開始與結束日期"
    }

    @Test
    fun `只選了開始日期也不能儲存`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)

        act(
            viewModel,
            ItineraryAddEditIntent.LoadDraft,
            ItineraryAddEditIntent.UpdateTitle("東京 3 日遊"),
            ItineraryAddEditIntent.UpdateStartDate(nov10),
            ItineraryAddEditIntent.Save,
        )

        useCases.titles() shouldNotContain "東京 3 日遊"
        viewModel.state.value.dateError shouldBe "請選擇開始與結束日期"
    }

    @Test
    fun `補上日期後就能儲存`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)
        act(
            viewModel,
            ItineraryAddEditIntent.LoadDraft,
            ItineraryAddEditIntent.UpdateTitle("東京 3 日遊"),
            ItineraryAddEditIntent.UpdateStartDate(nov10),
            ItineraryAddEditIntent.Save,
        )
        viewModel.state.value.dateError shouldBe "請選擇開始與結束日期"

        act(viewModel, ItineraryAddEditIntent.UpdateEndDate(nov12))
        viewModel.state.value.dateError shouldBe null

        act(viewModel, ItineraryAddEditIntent.Save)
        val tokyo = useCases.getAllItineraries().getOrThrow().single { it.title == "東京 3 日遊" }
        tokyo.startDate shouldBe nov10
        tokyo.endDate shouldBe nov12
    }

    @Test
    fun `標題和日期都沒填時先提示標題`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)

        act(viewModel, ItineraryAddEditIntent.LoadDraft, ItineraryAddEditIntent.Save)

        viewModel.state.value.titleError shouldBe "標題不可為空"
        viewModel.state.value.dateError shouldBe null
        useCases.titles() shouldHaveSize 2
    }

    @Test
    fun `編輯沒日期的舊行程要先補日期`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("hokkaido"),
            ItineraryAddEditIntent.UpdateTitle("北海道 5 日遊"),
            ItineraryAddEditIntent.Save,
        )

        viewModel.state.value.dateError shouldBe "請選擇開始與結束日期"
        useCases.getItinerary("hokkaido").getOrThrow()?.title shouldBe "北海道"

        act(
            viewModel,
            ItineraryAddEditIntent.UpdateStartDate(LocalDate(2026, 12, 1)),
            ItineraryAddEditIntent.UpdateEndDate(LocalDate(2026, 12, 5)),
            ItineraryAddEditIntent.Save,
        )

        val saved = useCases.getItinerary("hokkaido").getOrThrow()
        saved?.title shouldBe "北海道 5 日遊"
        saved?.startDate shouldBe LocalDate(2026, 12, 1)
        saved?.endDate shouldBe LocalDate(2026, 12, 5)
    }

    @Test
    fun `編輯有日期的行程照舊`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.UpdateTitle("京都 5 日遊"),
            ItineraryAddEditIntent.Save,
        )

        val saved = useCases.getItinerary("kyoto").getOrThrow()
        saved?.title shouldBe "京都 5 日遊"
        saved?.startDate shouldBe LocalDate(2026, 10, 1)
        saved?.endDate shouldBe LocalDate(2026, 10, 5)
    }

    // ── 測試工具 ──

    private val nov10 = LocalDate(2026, 11, 10)
    private val nov12 = LocalDate(2026, 11, 12)

    /** 新增行程要有日期才存得進去（第 4 組起），只測草稿的情境用這組日期 */
    private val datesOfNewTrip = arrayOf<ItineraryAddEditIntent>(
        ItineraryAddEditIntent.UpdateStartDate(LocalDate(2026, 11, 20)),
        ItineraryAddEditIntent.UpdateEndDate(LocalDate(2026, 11, 24)),
    )

    /** 舊版留下、沒有日期的行程 */
    private val hokkaido = testItinerary("hokkaido", title = "北海道").copy(startDate = null, endDate = null)

    private suspend fun TestUseCases.titles(): List<String> = getAllItineraries().getOrThrow().map { it.title }

    private val photoBytes = byteArrayOf(1, 2, 3)

    private fun useCasesOf(
        wrap: (ItineraryRepository) -> ItineraryRepository = { it },
        drafts: DraftRepository = inMemoryDraftRepository(),
    ): TestUseCases {
        val itemRepository = InMemoryItineraryItemRepository()
        val itineraryRepository = wrap(InMemoryItineraryRepository(itemRepository, kyoto, hokkaido))
        return TestUseCases(itineraryRepository, itemRepository, imageStorage = imageStorage, drafts = drafts)
    }

    /** 輸入標題，只跑到指令開始執行，不推進時間 */
    private fun TestScope.type(viewModel: ItineraryAddEditViewModel, title: String) {
        viewModel.processIntent(ItineraryAddEditIntent.UpdateTitle(title))
        runCurrent()
    }

    private fun viewModelWith(useCases: TestUseCases) = ItineraryAddEditViewModel(
        createItineraryUseCase = useCases.createItinerary,
        updateItineraryUseCase = useCases.updateItinerary,
        getItineraryUseCase = useCases.getItinerary,
        saveDraftUseCase = useCases.saveDraft,
        loadDraftUseCase = useCases.loadDraft,
        deleteDraftUseCase = useCases.deleteDraft,
        imageStorageService = useCases.imageStorage,
    )

    /** 從現在起收集 [viewModel] 發出的事件 */
    private fun TestScope.eventsOf(viewModel: ItineraryAddEditViewModel): List<ItineraryAddEditEvent> {
        val events = mutableListOf<ItineraryAddEditEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events += it } }
        return events
    }

    /** 依序送出指令，每送一個就把排程跑完 */
    private fun TestScope.act(viewModel: ItineraryAddEditViewModel, vararg intents: ItineraryAddEditIntent) {
        intents.forEach {
            viewModel.processIntent(it)
            advanceUntilIdle()
        }
    }
}

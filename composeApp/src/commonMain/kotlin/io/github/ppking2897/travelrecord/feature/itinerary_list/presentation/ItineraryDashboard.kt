package io.github.ppking2897.travelrecord.feature.itinerary_list.presentation

import io.github.ppking2897.travelrecord.core.presentation.util.DateTimeFormatter
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.TravelPhase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetItineraryPhaseUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import kotlinx.datetime.LocalDate

/** 行程列表的階段篩選 */
enum class ItineraryFilter(val label: String) {
    All("全部"),
    Planning("規劃中"),
    InProgress("旅行中"),
    Completed("已結束")
}

/** 沒有行程可列時要顯示的空畫面 */
enum class DashboardEmptyState {
    /** 一筆行程都沒有 */
    NoItineraries,

    /** 搜尋或篩選後沒有符合的 */
    NoMatch
}

/**
 * 行程列表畫面要呈現的內容：各區塊、摘要卡數字與空畫面種類
 *
 * @property allTrips 搜尋與篩選後的全部行程，維持讀取順序
 * @property recentTrips 「最近編輯」區塊；只有一筆行程時為空，不顯示
 * @property emptyState 沒有行程可列時的空畫面種類；有行程時為 null
 */
data class ItineraryDashboard(
    val currentTrips: List<Itinerary>,
    val upcomingTrips: List<Itinerary>,
    val recentTrips: List<Itinerary>,
    val allTrips: List<Itinerary>,
    val currentCount: Int,
    val upcomingCount: Int,
    val completedCount: Int,
    val emptyState: DashboardEmptyState?,
)

/** 行程卡片上的統計：項目數、已完成數、照片數 */
data class ItineraryCardStats(
    val itemCount: Int,
    val completedCount: Int,
    val photoCount: Int
)

private const val SECTION_LIMIT = 3

/**
 * 依搜尋字、篩選與今天的日期，算出行程列表要呈現的內容
 *
 * @param itineraries 要列的行程；有搜尋字時是 ViewModel 搜尋後的結果
 */
fun buildItineraryDashboard(
    itineraries: List<Itinerary>,
    searchQuery: String,
    filter: ItineraryFilter,
    today: LocalDate,
): ItineraryDashboard {
    val phaseOf = GetItineraryPhaseUseCase()

    val phaseFiltered = when (filter) {
        ItineraryFilter.All -> itineraries
        ItineraryFilter.Planning -> itineraries.filter { phaseOf(it, today) == TravelPhase.Planning }
        ItineraryFilter.InProgress -> itineraries.filter { phaseOf(it, today) == TravelPhase.InProgress }
        ItineraryFilter.Completed -> itineraries.filter { phaseOf(it, today) == TravelPhase.Completed }
    }

    val filtered = if (searchQuery.isBlank()) {
        phaseFiltered
    } else {
        val query = searchQuery.trim()
        phaseFiltered.filter { itinerary ->
            itinerary.title.contains(query, ignoreCase = true) ||
                itinerary.description.contains(query, ignoreCase = true) ||
                itinerary.items.any { item ->
                    item.activity.contains(query, ignoreCase = true) ||
                        item.location.name.contains(query, ignoreCase = true)
                }
        }
    }

    val currentTrips = filtered
        .filter { phaseOf(it, today) == TravelPhase.InProgress }
        .sortedBy { it.endDate?.toString() ?: "9999-12-31" }
    val planningTrips = filtered
        .filter { phaseOf(it, today) == TravelPhase.Planning }
        .sortedBy { it.startDate?.toString() ?: "9999-12-31" }
    val recentTrips = if (filtered.size > 1) {
        filtered.sortedByDescending { it.modifiedAt }.take(SECTION_LIMIT)
    } else {
        emptyList()
    }

    return ItineraryDashboard(
        currentTrips = currentTrips,
        upcomingTrips = planningTrips.take(SECTION_LIMIT),
        recentTrips = recentTrips,
        allTrips = filtered,
        currentCount = currentTrips.size,
        upcomingCount = planningTrips.size,
        completedCount = filtered.count { phaseOf(it, today) == TravelPhase.Completed },
        emptyState = when {
            filtered.isNotEmpty() -> null
            // 搜尋時清單已經是 ViewModel 搜尋後的結果，空的不代表一筆行程都沒有
            itineraries.isEmpty() && searchQuery.isBlank() -> DashboardEmptyState.NoItineraries
            else -> DashboardEmptyState.NoMatch
        },
    )
}

fun Itinerary.cardStats(): ItineraryCardStats = ItineraryCardStats(
    itemCount = items.size,
    completedCount = items.count { it.isCompleted },
    photoCount = items.sumOf { it.photos.size }
)

/** 出現最多次的地點名稱；沒有項目時為 null */
fun mainLocationOf(items: List<ItineraryItem>): String? {
    if (items.isEmpty()) return null
    return items.groupingBy { it.location.name }.eachCount().maxByOrNull { it.value }?.key
}

/** 卡片上的日期範圍；只填一個日期時加上「開始：」或「結束：」，都沒填時為 null */
fun formatDateRange(startDate: LocalDate?, endDate: LocalDate?): String? {
    return when {
        startDate != null && endDate != null ->
            "${DateTimeFormatter.formatDate(startDate)} - ${DateTimeFormatter.formatDate(endDate)}"
        startDate != null -> "開始：${DateTimeFormatter.formatDate(startDate)}"
        endDate != null -> "結束：${DateTimeFormatter.formatDate(endDate)}"
        else -> null
    }
}

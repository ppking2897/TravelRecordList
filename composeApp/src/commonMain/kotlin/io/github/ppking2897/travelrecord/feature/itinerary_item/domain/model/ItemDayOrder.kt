package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model

/**
 * 項目在行程裡的先後：日期 → 有時間的在前 → 時間（有抵達時間用抵達，否則用出發）
 *
 * 時間相同、或都沒排時間的不再比，排序時維持原本的先後；資料層讀出時是先建立的在前。
 * 行程詳情、今日、地圖、路線、回憶故事都用這一套。
 */
val itemDayOrder: Comparator<ItineraryItem> =
    compareBy<ItineraryItem> { it.date }
        .thenBy { it.primaryTime() == null }
        .thenBy { it.primaryTime() }

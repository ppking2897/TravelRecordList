package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase

import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem

/**
 * 刪掉這些項目所有照片的原圖與縮圖檔
 *
 * 只在資料庫的紀錄刪掉之後呼叫。檔案可能早就不在，刪不掉就略過，不影響刪除的結果。
 */
internal suspend fun ImageStorageService.deletePhotoFiles(items: List<ItineraryItem>) {
    items.flatMap { it.photos }
        .flatMap { listOfNotNull(it.filePath, it.thumbnailPath) }
        .forEach { deleteImage(it) }
}

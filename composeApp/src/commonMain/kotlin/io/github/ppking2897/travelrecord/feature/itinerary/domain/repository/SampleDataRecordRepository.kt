package io.github.ppking2897.travelrecord.feature.itinerary.domain.repository

/**
 * 記錄範例行程是否已經放過；放過就不再放，使用者把行程刪光也一樣
 */
interface SampleDataRecordRepository {
    suspend fun isSampleDataAdded(): Result<Boolean>

    suspend fun markSampleDataAdded(): Result<Unit>
}

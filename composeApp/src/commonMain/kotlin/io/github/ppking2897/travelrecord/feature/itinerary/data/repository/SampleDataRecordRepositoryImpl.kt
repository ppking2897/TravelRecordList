package io.github.ppking2897.travelrecord.feature.itinerary.data.repository

import io.github.ppking2897.travelrecord.core.data.local.StorageService
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.SampleDataRecordRepository

/**
 * 「範例已放過」存在鍵值儲存，跟資料搬移的標記放在同一處
 */
class SampleDataRecordRepositoryImpl(
    private val storage: StorageService,
) : SampleDataRecordRepository {

    override suspend fun isSampleDataAdded(): Result<Boolean> =
        storage.load(KEY).map { it != null }

    override suspend fun markSampleDataAdded(): Result<Unit> =
        storage.save(KEY, "1")

    private companion object {
        const val KEY = "sample-data:added:v1"
    }
}

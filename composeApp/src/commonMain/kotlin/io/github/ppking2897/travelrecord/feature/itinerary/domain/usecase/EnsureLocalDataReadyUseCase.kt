package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.core.domain.service.LegacyDataImporter

/**
 * 確保本機資料可以使用：先把舊版資料搬進資料庫，再判斷要不要寫入範例資料
 *
 * 搬移失敗時不寫範例資料：範例資料用固定 id，先寫入會讓下次啟動的搬移撞到同樣的 id。
 */
class EnsureLocalDataReadyUseCase(
    private val legacyDataImporter: LegacyDataImporter,
    private val initializeSampleData: InitializeSampleDataUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        legacyDataImporter.importIfNeeded().getOrThrow()
        initializeSampleData().getOrThrow()
    }
}

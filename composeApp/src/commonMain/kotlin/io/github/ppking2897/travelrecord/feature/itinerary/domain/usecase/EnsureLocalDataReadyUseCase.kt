package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.core.domain.service.LegacyDataImporter
import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository

/**
 * 確保本機資料可以使用：清掉舊版存下的路線、把舊版資料搬進資料庫，再判斷要不要寫入範例資料
 *
 * 搬移失敗時不寫範例資料：範例資料用固定 id，先寫入會讓下次啟動的搬移撞到同樣的 id。
 * 清路線失敗不擋啟動：舊路線只佔空間，下次啟動會再清一次。清路線不放進搬移流程，
 * 因為已經搬過資料的手機不會再跑搬移。
 */
class EnsureLocalDataReadyUseCase(
    private val legacyDataImporter: LegacyDataImporter,
    private val initializeSampleData: InitializeSampleDataUseCase,
    private val routeRepository: RouteRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        routeRepository.deleteStoredRoutes()
        legacyDataImporter.importIfNeeded().getOrThrow()
        initializeSampleData().getOrThrow()
    }
}

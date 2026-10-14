package io.github.ppking2897.travelrecord.feature.draft.domain.usecase

import io.github.ppking2897.travelrecord.feature.draft.domain.model.DraftType
import io.github.ppking2897.travelrecord.feature.draft.domain.repository.DraftRepository

/**
 * 載入草稿的 Use Case
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class LoadDraftUseCase(
    private val draftRepository: DraftRepository
) {
    suspend operator fun invoke(
        type: DraftType
    ): Result<Map<String, String>?> {
        return try {
            draftRepository.getDraft(type).map { draft ->
                draft?.data
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

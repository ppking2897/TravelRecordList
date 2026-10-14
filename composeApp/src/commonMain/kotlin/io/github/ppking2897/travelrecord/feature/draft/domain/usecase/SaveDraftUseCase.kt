package io.github.ppking2897.travelrecord.feature.draft.domain.usecase

import io.github.ppking2897.travelrecord.feature.draft.domain.model.Draft
import io.github.ppking2897.travelrecord.feature.draft.domain.model.DraftType
import io.github.ppking2897.travelrecord.feature.draft.domain.repository.DraftRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 儲存草稿的 Use Case
 */
@OptIn(kotlin.time.ExperimentalTime::class, ExperimentalUuidApi::class)
class SaveDraftUseCase(
    private val draftRepository: DraftRepository
) {
    suspend operator fun invoke(
        type: DraftType,
        data: Map<String, String>
    ): Result<Unit> {
        return try {
            val now = kotlin.time.Clock.System.now()
            val draft = Draft(
                id = Uuid.random().toString(),
                type = type,
                data = data,
                createdAt = now,
                modifiedAt = now
            )

            draftRepository.saveDraft(draft)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

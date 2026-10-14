package io.github.ppking2897.travelrecord.feature.draft.domain.usecase

import io.github.ppking2897.travelrecord.feature.draft.domain.model.DraftType
import io.github.ppking2897.travelrecord.feature.draft.domain.repository.DraftRepository

class DeleteDraftUseCase(
    private val repository: DraftRepository
) {
    suspend operator fun invoke(type: DraftType): Result<Unit> {
        return repository.deleteDraft(type)
    }
}

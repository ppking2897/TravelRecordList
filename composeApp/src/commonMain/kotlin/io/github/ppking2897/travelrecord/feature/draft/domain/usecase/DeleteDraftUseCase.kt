package com.example.myapplication.feature.draft.domain.usecase

import com.example.myapplication.feature.draft.domain.model.DraftType
import com.example.myapplication.feature.draft.domain.repository.DraftRepository

class DeleteDraftUseCase(
    private val repository: DraftRepository
) {
    suspend operator fun invoke(type: DraftType): Result<Unit> {
        return repository.deleteDraft(type)
    }
}

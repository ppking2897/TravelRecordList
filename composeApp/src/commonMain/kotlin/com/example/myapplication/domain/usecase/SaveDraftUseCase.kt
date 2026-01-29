package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.entity.Draft
import com.example.myapplication.domain.entity.DraftType
import com.example.myapplication.domain.repository.DraftRepository
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

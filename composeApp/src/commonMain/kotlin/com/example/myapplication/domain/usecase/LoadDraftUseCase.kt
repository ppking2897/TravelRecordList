package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.DraftType
import com.example.myapplication.data.repository.DraftRepository

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

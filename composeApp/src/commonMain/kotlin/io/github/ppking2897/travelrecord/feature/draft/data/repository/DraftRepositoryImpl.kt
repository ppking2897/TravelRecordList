@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.feature.draft.data.repository

import com.example.myapplication.feature.draft.data.mapper.toDto
import com.example.myapplication.feature.draft.data.mapper.toDomain
import com.example.myapplication.core.data.local.JsonSerializer
import com.example.myapplication.core.data.local.StorageService
import com.example.myapplication.feature.draft.domain.model.Draft
import com.example.myapplication.feature.draft.domain.model.DraftType
import com.example.myapplication.feature.draft.domain.repository.DraftRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * ?�稿資�?存�?實�?
 */
class DraftRepositoryImpl(
    private val storageService: StorageService
) : DraftRepository {

    private val draftKeyPrefix = "draft_"

    override suspend fun saveDraft(draft: Draft): Result<Unit> {
        return try {
            val key = getDraftKey(draft.type)
            val dto = draft.toDto()
            val jsonString = JsonSerializer.serializeDraft(dto)
            storageService.save(key, jsonString).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDraft(type: DraftType): Result<Draft?> {
        return try {
            val key = getDraftKey(type)
            val jsonString = storageService.load(key).getOrNull() ?: return Result.success(null)

            val dto = JsonSerializer.deserializeDraft(jsonString)
            val draft = dto.toDomain()

            // 檢查是否過期（超過 7 天）
            val now = Clock.System.now()
            val age = now - draft.createdAt
            if (age > 7.days) {
                deleteDraft(type)
                return Result.success(null)
            }

            Result.success(draft)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDraft(type: DraftType): Result<Unit> {
        return try {
            val key = getDraftKey(type)
            storageService.delete(key)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpiredDrafts(): Result<Unit> {
        return try {
            DraftType.entries.forEach { type ->
                getDraft(type)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getDraftKey(type: DraftType): String {
        return "$draftKeyPrefix${type.name.lowercase()}"
    }
}

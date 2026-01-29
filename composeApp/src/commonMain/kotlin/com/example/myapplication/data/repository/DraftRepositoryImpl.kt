@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.data.repository

import com.example.myapplication.data.mapper.toDto
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.data.storage.JsonSerializer
import com.example.myapplication.data.storage.StorageService
import com.example.myapplication.domain.entity.Draft
import com.example.myapplication.domain.entity.DraftType
import com.example.myapplication.domain.repository.DraftRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * 草稿資料存取實作
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
            val draft = dto.toEntity()

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
                getDraft(type) // 這會自動刪除過期的草稿
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

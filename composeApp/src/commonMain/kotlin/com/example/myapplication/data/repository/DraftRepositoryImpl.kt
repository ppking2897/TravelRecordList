package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Draft
import com.example.myapplication.data.model.DraftType
import com.example.myapplication.data.storage.StorageService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.days

/**
 * 草稿資料存取實作
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class DraftRepositoryImpl(
    private val storageService: StorageService
) : DraftRepository {
    
    private val draftKeyPrefix = "draft_"
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun saveDraft(draft: Draft): Result<Unit> {
        return try {
            val key = getDraftKey(draft.type)
            val jsonString = json.encodeToString(draft)
            storageService.save(key, jsonString).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDraft(type: DraftType): Result<Draft?> {
        return try {
            val key = getDraftKey(type)
            val jsonString = storageService.load(key).getOrNull()
            
            if (jsonString == null) {
                return Result.success(null)
            }
            
            val draft = json.decodeFromString<Draft>(jsonString)
            
            // 檢查是否過期（超過 7 天）
            val now = kotlin.time.Clock.System.now()
            val age = now - draft.createdAt
            if (age > 7.days) {
                // 過期，刪除並返回 null
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
            // 檢查所有草稿類型
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

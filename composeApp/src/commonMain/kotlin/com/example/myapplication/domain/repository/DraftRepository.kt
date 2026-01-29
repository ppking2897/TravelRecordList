package com.example.myapplication.domain.repository

import com.example.myapplication.domain.entity.Draft
import com.example.myapplication.domain.entity.DraftType

/**
 * 草稿資料存取介面
 */
interface DraftRepository {
    /**
     * 儲存草稿
     */
    suspend fun saveDraft(draft: Draft): Result<Unit>

    /**
     * 取得指定類型的草稿
     */
    suspend fun getDraft(type: DraftType): Result<Draft?>

    /**
     * 刪除指定類型的草稿
     */
    suspend fun deleteDraft(type: DraftType): Result<Unit>

    /**
     * 刪除過期的草稿（超過 7 天）
     */
    suspend fun deleteExpiredDrafts(): Result<Unit>
}

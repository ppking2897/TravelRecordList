package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Draft
import com.example.myapplication.data.model.DraftType

/**
 * 草稿資料存取介面
 */
interface DraftRepository {
    /**
     * 儲存草稿
     * 
     * @param draft 草稿資料
     * @return 成功或錯誤
     */
    suspend fun saveDraft(draft: Draft): Result<Unit>
    
    /**
     * 取得指定類型的草稿
     * 
     * @param type 草稿類型
     * @return 草稿資料或 null（如果不存在）
     */
    suspend fun getDraft(type: DraftType): Result<Draft?>
    
    /**
     * 刪除指定類型的草稿
     * 
     * @param type 草稿類型
     * @return 成功或錯誤
     */
    suspend fun deleteDraft(type: DraftType): Result<Unit>
    
    /**
     * 刪除過期的草稿（超過 7 天）
     * 
     * @return 成功或錯誤
     */
    suspend fun deleteExpiredDrafts(): Result<Unit>
}

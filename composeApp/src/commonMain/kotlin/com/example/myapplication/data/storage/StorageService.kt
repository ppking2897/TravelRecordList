package com.example.myapplication.data.storage

/**
 * 平台無關的儲存服務介面
 * 
 * 各平台需要實現此介面：
 * - Android: 使用 SharedPreferences 或 DataStore
 * - iOS: 使用 UserDefaults 或 Core Data
 * - JS/Web: 使用 LocalStorage
 * - Desktop: 使用檔案系統
 */
interface StorageService {
    /**
     * 儲存資料
     * 
     * @param key 儲存鍵值
     * @param data 資料內容（JSON 字串）
     * @return 成功或錯誤
     */
    suspend fun save(key: String, data: String): Result<Unit>
    
    /**
     * 載入資料
     * 
     * @param key 儲存鍵值
     * @return 資料內容或 null（如果不存在）
     */
    suspend fun load(key: String): Result<String?>
    
    /**
     * 刪除資料
     * 
     * @param key 儲存鍵值
     * @return 成功或錯誤
     */
    suspend fun delete(key: String): Result<Unit>
    
    /**
     * 取得所有儲存鍵值
     * 
     * @return 所有鍵值列表
     */
    suspend fun getAllKeys(): Result<List<String>>
}

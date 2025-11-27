package com.example.myapplication.data.storage

/**
 * 記憶體內的 StorageService 實作
 * 用於測試和開發，資料不會持久化
 */
class InMemoryStorageService : StorageService {
    private val storage = mutableMapOf<String, String>()
    
    override suspend fun save(key: String, data: String): Result<Unit> {
        return try {
            storage[key] = data
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun load(key: String): Result<String?> {
        return try {
            Result.success(storage[key])
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun delete(key: String): Result<Unit> {
        return try {
            storage.remove(key)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllKeys(): Result<List<String>> {
        return try {
            Result.success(storage.keys.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

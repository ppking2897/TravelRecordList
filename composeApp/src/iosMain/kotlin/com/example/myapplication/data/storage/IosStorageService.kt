package com.example.myapplication.data.storage

import platform.Foundation.NSUserDefaults

/**
 * iOS 平台的儲存服務實作
 * 使用 UserDefaults 進行資料持久化
 */
class IosStorageService : StorageService {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    override suspend fun save(key: String, data: String): Result<Unit> {
        return try {
            userDefaults.setObject(data, forKey = key)
            userDefaults.synchronize()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun load(key: String): Result<String?> {
        return try {
            val value = userDefaults.stringForKey(key)
            Result.success(value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun delete(key: String): Result<Unit> {
        return try {
            userDefaults.removeObjectForKey(key)
            userDefaults.synchronize()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllKeys(): Result<List<String>> {
        return try {
            val dictionary = userDefaults.dictionaryRepresentation()
            val keys = dictionary.keys.mapNotNull { it as? String }
            Result.success(keys)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.example.myapplication.data.storage

import kotlinx.browser.localStorage

/**
 * JS/Web 平台的儲存服務實作
 * 使用 LocalStorage 進行資料持久化
 */
class JsStorageService : StorageService {
    
    override suspend fun save(key: String, data: String): Result<Unit> {
        return try {
            localStorage.setItem(key, data)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun load(key: String): Result<String?> {
        return try {
            val value = localStorage.getItem(key)
            Result.success(value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun delete(key: String): Result<Unit> {
        return try {
            localStorage.removeItem(key)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllKeys(): Result<List<String>> {
        return try {
            val keys = mutableListOf<String>()
            val length = localStorage.length
            for (i in 0 until length) {
                localStorage.key(i)?.let { keys.add(it) }
            }
            Result.success(keys)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

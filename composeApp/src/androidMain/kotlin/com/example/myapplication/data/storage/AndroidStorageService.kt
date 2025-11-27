package com.example.myapplication.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Android 平台的儲存服務實作
 * 使用 DataStore 進行資料持久化
 */
class AndroidStorageService(private val context: Context) : StorageService {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "travel_app_storage")
    
    override suspend fun save(key: String, data: String): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = data
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun load(key: String): Result<String?> {
        return try {
            val value = context.dataStore.data
                .map { preferences ->
                    preferences[stringPreferencesKey(key)]
                }
                .first()
            Result.success(value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun delete(key: String): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey(key))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllKeys(): Result<List<String>> {
        return try {
            val keys = context.dataStore.data
                .map { preferences ->
                    preferences.asMap().keys.map { it.name }
                }
                .first()
            Result.success(keys)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

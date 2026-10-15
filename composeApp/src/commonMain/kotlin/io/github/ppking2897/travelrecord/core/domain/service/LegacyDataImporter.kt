package io.github.ppking2897.travelrecord.core.domain.service

/**
 * 把舊版儲存格式的資料搬進目前的資料庫
 */
interface LegacyDataImporter {
    /**
     * 第一次執行時搬移，之後呼叫什麼都不做
     *
     * @return 讀取舊資料失敗時回傳 failure，且不視為已搬過，下次啟動會再試
     */
    suspend fun importIfNeeded(): Result<Unit>
}

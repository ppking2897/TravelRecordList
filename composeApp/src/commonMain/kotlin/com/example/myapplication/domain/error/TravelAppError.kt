package com.example.myapplication.domain.error

/**
 * 旅遊應用程式領域錯誤類型
 */
sealed class TravelAppError : Throwable() {
    /**
     * 驗證錯誤
     *
     * @property field 欄位名稱
     */
    data class ValidationError(val field: String, override val message: String) : TravelAppError()

    /**
     * 找不到資料錯誤
     *
     * @property entityType 實體類型
     * @property id 實體 ID
     */
    data class NotFoundError(val entityType: String, val id: String) : TravelAppError() {
        override val message: String = "Entity of type '$entityType' with id '$id' not found"
    }

    /**
     * 儲存錯誤
     */
    data class StorageError(override val message: String, override val cause: Throwable? = null) : TravelAppError()

    /**
     * 網路錯誤
     */
    data class NetworkError(override val message: String, override val cause: Throwable? = null) : TravelAppError()

    /**
     * 衝突錯誤
     */
    data class ConflictError(override val message: String) : TravelAppError()
}

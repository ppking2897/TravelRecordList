package com.example.myapplication.data.model

import kotlinx.datetime.LocalDate

/**
 * 驗證函數集合，用於驗證資料模型的欄位
 */
object Validation {
    
    /**
     * 驗證標題是否有效
     * 標題必須不為空且至少包含一個非空白字元
     * 
     * @param title 要驗證的標題
     * @return Result.success(Unit) 如果有效，否則 Result.failure(TravelAppError.ValidationError)
     * 
     * **Validates: Requirements 1.2**
     */
    fun validateTitle(title: String): Result<Unit> {
        return if (title.isBlank()) {
            Result.failure(
                TravelAppError.ValidationError(
                    field = "title",
                    message = "Title must contain at least one non-whitespace character"
                )
            )
        } else {
            Result.success(Unit)
        }
    }
    
    /**
     * 驗證日期範圍是否有效
     * 如果兩個日期都提供，結束日期不能早於開始日期
     * 
     * @param startDate 開始日期（可選）
     * @param endDate 結束日期（可選）
     * @return Result.success(Unit) 如果有效，否則 Result.failure(TravelAppError.ValidationError)
     * 
     * **Validates: Requirements 1.5**
     */
    fun validateDateRange(startDate: LocalDate?, endDate: LocalDate?): Result<Unit> {
        return if (startDate != null && endDate != null && endDate < startDate) {
            Result.failure(
                TravelAppError.ValidationError(
                    field = "dateRange",
                    message = "End date cannot be before start date"
                )
            )
        } else {
            Result.success(Unit)
        }
    }
    
    /**
     * 驗證地點名稱是否有效
     * 地點名稱必須不為空且至少包含一個非空白字元
     * 
     * @param locationName 要驗證的地點名稱
     * @return Result.success(Unit) 如果有效，否則 Result.failure(TravelAppError.ValidationError)
     * 
     * **Validates: Requirements 2.2**
     */
    fun validateLocationName(locationName: String): Result<Unit> {
        return if (locationName.isBlank()) {
            Result.failure(
                TravelAppError.ValidationError(
                    field = "locationName",
                    message = "Location name must not be empty"
                )
            )
        } else {
            Result.success(Unit)
        }
    }
    
    /**
     * 驗證緯度是否在有效範圍內
     * 
     * @param latitude 緯度值（可選）
     * @return Result.success(Unit) 如果有效，否則 Result.failure(TravelAppError.ValidationError)
     */
    fun validateLatitude(latitude: Double?): Result<Unit> {
        return if (latitude != null && (latitude < -90.0 || latitude > 90.0)) {
            Result.failure(
                TravelAppError.ValidationError(
                    field = "latitude",
                    message = "Latitude must be between -90 and 90"
                )
            )
        } else {
            Result.success(Unit)
        }
    }
    
    /**
     * 驗證經度是否在有效範圍內
     * 
     * @param longitude 經度值（可選）
     * @return Result.success(Unit) 如果有效，否則 Result.failure(TravelAppError.ValidationError)
     */
    fun validateLongitude(longitude: Double?): Result<Unit> {
        return if (longitude != null && (longitude < -180.0 || longitude > 180.0)) {
            Result.failure(
                TravelAppError.ValidationError(
                    field = "longitude",
                    message = "Longitude must be between -180 and 180"
                )
            )
        } else {
            Result.success(Unit)
        }
    }
    
    /**
     * 驗證 Itinerary 物件的所有欄位
     * 
     * @param itinerary 要驗證的 Itinerary
     * @return Result.success(Unit) 如果所有欄位都有效，否則 Result.failure(TravelAppError.ValidationError)
     */
    fun validateItinerary(itinerary: Itinerary): Result<Unit> {
        validateTitle(itinerary.title).getOrElse { return Result.failure(it) }
        validateDateRange(itinerary.startDate, itinerary.endDate).getOrElse { return Result.failure(it) }
        return Result.success(Unit)
    }
    
    /**
     * 驗證 Location 物件的所有欄位
     * 
     * @param location 要驗證的 Location
     * @return Result.success(Unit) 如果所有欄位都有效，否則 Result.failure(TravelAppError.ValidationError)
     */
    fun validateLocation(location: Location): Result<Unit> {
        validateLocationName(location.name).getOrElse { return Result.failure(it) }
        validateLatitude(location.latitude).getOrElse { return Result.failure(it) }
        validateLongitude(location.longitude).getOrElse { return Result.failure(it) }
        return Result.success(Unit)
    }
    
    /**
     * 驗證 ItineraryItem 物件的所有欄位
     * 
     * @param item 要驗證的 ItineraryItem
     * @return Result.success(Unit) 如果所有欄位都有效，否則 Result.failure(TravelAppError.ValidationError)
     */
    fun validateItineraryItem(item: ItineraryItem): Result<Unit> {
        validateLocation(item.location).getOrElse { return Result.failure(it) }
        
        if (item.activity.isBlank()) {
            return Result.failure(
                TravelAppError.ValidationError(
                    field = "activity",
                    message = "Activity description must not be empty"
                )
            )
        }
        
        // Validate that completedAt is only set when isCompleted is true
        if (!item.isCompleted && item.completedAt != null) {
            return Result.failure(
                TravelAppError.ValidationError(
                    field = "completedAt",
                    message = "completedAt can only be set when isCompleted is true"
                )
            )
        }
        
        return Result.success(Unit)
    }
}

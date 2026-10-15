package io.github.ppking2897.travelrecord.core.data.local.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

// TODO(等外部): iOS 的 Room 建置與 NSUserDefaults 舊資料搬移未驗證；移除條件：在 Mac 上 :composeApp:linkDebugFrameworkIosSimulatorArm64 成功，且模擬器升級後行程與項目數量一致
fun createAppDatabase(): AppDatabase =
    Room.databaseBuilder<AppDatabase>(name = "${documentDirectory()}/$APP_DATABASE_NAME")
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(directory?.path) { "找不到 iOS 的 Documents 目錄" }
}

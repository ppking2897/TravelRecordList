package com.example.myapplication

import android.app.Application
import com.example.myapplication.di.initKoin
import kotlin.time.ExperimentalTime
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/** Android 應用程式入口 負責初始化 Koin 依賴注入 */
@ExperimentalTime
class TravelRecordApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@TravelRecordApp)
        }
    }
}

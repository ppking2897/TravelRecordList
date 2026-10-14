package io.github.ppking2897.travelrecord

import android.app.Application
import io.github.ppking2897.travelrecord.di.initKoin
import kotlin.time.ExperimentalTime
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/** Android application entry point that initializes Koin. */
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

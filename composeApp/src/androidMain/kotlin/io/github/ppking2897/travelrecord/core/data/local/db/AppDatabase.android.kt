package io.github.ppking2897.travelrecord.core.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

fun createAppDatabase(context: Context): AppDatabase =
    Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = context.getDatabasePath(APP_DATABASE_NAME).absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

package com.example.myapplication.di

import com.example.myapplication.core.di.coreModule
import com.example.myapplication.feature.draft.di.draftModule
import com.example.myapplication.feature.footprint.di.footprintModule
import com.example.myapplication.feature.itinerary.di.itineraryModule
import com.example.myapplication.feature.itinerary_add_edit.di.itineraryAddEditModule
import com.example.myapplication.feature.itinerary_detail.di.itineraryDetailModule
import com.example.myapplication.feature.itinerary_item.di.itineraryItemModule
import com.example.myapplication.feature.itinerary_list.di.itineraryListModule
import com.example.myapplication.feature.itinerary_map.di.itineraryMapModule
import com.example.myapplication.feature.memory.di.memoryModule
import com.example.myapplication.feature.route.di.routeModule
import com.example.myapplication.feature.today.di.todayModule
import kotlin.time.ExperimentalTime

@ExperimentalTime
val appModule =
            coreModule +
            itineraryModule +
            itineraryListModule +
            itineraryDetailModule +
            itineraryAddEditModule +
            itineraryMapModule +
            itineraryItemModule +
            todayModule +
            draftModule +
            routeModule +
            footprintModule +
            memoryModule

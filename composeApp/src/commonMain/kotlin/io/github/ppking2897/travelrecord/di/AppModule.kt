package io.github.ppking2897.travelrecord.di

import io.github.ppking2897.travelrecord.core.di.coreModule
import io.github.ppking2897.travelrecord.feature.draft.di.draftModule
import io.github.ppking2897.travelrecord.feature.footprint.di.footprintModule
import io.github.ppking2897.travelrecord.feature.itinerary.di.itineraryModule
import io.github.ppking2897.travelrecord.feature.itinerary_add_edit.di.itineraryAddEditModule
import io.github.ppking2897.travelrecord.feature.itinerary_detail.di.itineraryDetailModule
import io.github.ppking2897.travelrecord.feature.itinerary_item.di.itineraryItemModule
import io.github.ppking2897.travelrecord.feature.itinerary_list.di.itineraryListModule
import io.github.ppking2897.travelrecord.feature.itinerary_map.di.itineraryMapModule
import io.github.ppking2897.travelrecord.feature.memory.di.memoryModule
import io.github.ppking2897.travelrecord.feature.route.di.routeModule
import io.github.ppking2897.travelrecord.feature.today.di.todayModule
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

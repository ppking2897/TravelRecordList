package com.example.myapplication.data.storage

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.model.Route
import com.example.myapplication.data.sync.SyncMarker
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

/**
 * Instant 的自訂序列化器
 */
@OptIn(kotlin.time.ExperimentalTime::class)
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
    
    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

/**
 * JSON 序列化工具
 * 提供統一的 JSON 序列化/反序列化功能
 */
@OptIn(kotlin.time.ExperimentalTime::class)
object JsonSerializer {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(InstantSerializer)
        }
    }
    
    /**
     * 將 Itinerary 序列化為 JSON 字串
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun serializeItinerary(itinerary: Itinerary): String {
        return json.encodeToString(Itinerary.serializer(), itinerary)
    }
    
    /**
     * 從 JSON 字串反序列化 Itinerary
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun deserializeItinerary(jsonString: String): Itinerary {
        return json.decodeFromString(Itinerary.serializer(), jsonString)
    }
    
    /**
     * 將 ItineraryItem 序列化為 JSON 字串
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun serializeItineraryItem(item: ItineraryItem): String {
        return json.encodeToString(ItineraryItem.serializer(), item)
    }
    
    /**
     * 從 JSON 字串反序列化 ItineraryItem
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun deserializeItineraryItem(jsonString: String): ItineraryItem {
        return json.decodeFromString(ItineraryItem.serializer(), jsonString)
    }
    
    /**
     * 將 Route 序列化為 JSON 字串
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun serializeRoute(route: Route): String {
        return json.encodeToString(Route.serializer(), route)
    }
    
    /**
     * 從 JSON 字串反序列化 Route
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun deserializeRoute(jsonString: String): Route {
        return json.decodeFromString(Route.serializer(), jsonString)
    }
    
    /**
     * 將字串列表序列化為 JSON 字串
     */
    fun serializeStringList(list: List<String>): String {
        return json.encodeToString(ListSerializer(String.serializer()), list)
    }
    
    /**
     * 從 JSON 字串反序列化字串列表
     */
    fun deserializeStringList(jsonString: String): List<String> {
        return json.decodeFromString(ListSerializer(String.serializer()), jsonString)
    }
    
    /**
     * 將 SyncMarker 列表序列化為 JSON 字串
     */
    fun serializeSyncMarkers(markers: List<SyncMarker>): String {
        return json.encodeToString(ListSerializer(SyncMarker.serializer()), markers)
    }
    
    /**
     * 從 JSON 字串反序列化 SyncMarker 列表
     */
    fun deserializeSyncMarkers(jsonString: String): List<SyncMarker> {
        return json.decodeFromString(ListSerializer(SyncMarker.serializer()), jsonString)
    }
}

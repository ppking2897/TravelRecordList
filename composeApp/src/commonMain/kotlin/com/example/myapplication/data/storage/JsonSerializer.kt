@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.data.storage

import com.example.myapplication.data.dto.DraftDto
import com.example.myapplication.data.dto.ItineraryDto
import com.example.myapplication.data.dto.ItineraryItemDto
import com.example.myapplication.data.dto.RouteDto
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
 * 提供統一的 JSON 序列化/反序列化功能（使用 DTO）
 */
object JsonSerializer {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(InstantSerializer)
        }
    }

    /**
     * 將 ItineraryDto 序列化為 JSON 字串
     */
    fun serializeItinerary(itinerary: ItineraryDto): String {
        return json.encodeToString(ItineraryDto.serializer(), itinerary)
    }

    /**
     * 從 JSON 字串反序列化 ItineraryDto
     */
    fun deserializeItinerary(jsonString: String): ItineraryDto {
        return json.decodeFromString(ItineraryDto.serializer(), jsonString)
    }

    /**
     * 將 ItineraryItemDto 序列化為 JSON 字串
     */
    fun serializeItineraryItem(item: ItineraryItemDto): String {
        return json.encodeToString(ItineraryItemDto.serializer(), item)
    }

    /**
     * 從 JSON 字串反序列化 ItineraryItemDto
     */
    fun deserializeItineraryItem(jsonString: String): ItineraryItemDto {
        return json.decodeFromString(ItineraryItemDto.serializer(), jsonString)
    }

    /**
     * 將 RouteDto 序列化為 JSON 字串
     */
    fun serializeRoute(route: RouteDto): String {
        return json.encodeToString(RouteDto.serializer(), route)
    }

    /**
     * 從 JSON 字串反序列化 RouteDto
     */
    fun deserializeRoute(jsonString: String): RouteDto {
        return json.decodeFromString(RouteDto.serializer(), jsonString)
    }

    /**
     * 將 DraftDto 序列化為 JSON 字串
     */
    fun serializeDraft(draft: DraftDto): String {
        return json.encodeToString(DraftDto.serializer(), draft)
    }

    /**
     * 從 JSON 字串反序列化 DraftDto
     */
    fun deserializeDraft(jsonString: String): DraftDto {
        return json.decodeFromString(DraftDto.serializer(), jsonString)
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

@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.core.data.local

import io.github.ppking2897.travelrecord.feature.draft.data.model.DraftResponse
import io.github.ppking2897.travelrecord.feature.itinerary.data.model.ItineraryResponse
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.model.ItineraryItemResponse
import io.github.ppking2897.travelrecord.feature.route.data.model.RouteResponse
import kotlin.time.Instant
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
     * 將 ItineraryResponse 序列化為 JSON 字串
     */
    fun serializeItinerary(itinerary: ItineraryResponse): String {
        return json.encodeToString(ItineraryResponse.serializer(), itinerary)
    }

    /**
     * 從 JSON 字串反序列化 ItineraryResponse
     */
    fun deserializeItinerary(jsonString: String): ItineraryResponse {
        return json.decodeFromString(ItineraryResponse.serializer(), jsonString)
    }

    /**
     * 將 ItineraryItemResponse 序列化為 JSON 字串
     */
    fun serializeItineraryItem(item: ItineraryItemResponse): String {
        return json.encodeToString(ItineraryItemResponse.serializer(), item)
    }

    /**
     * 從 JSON 字串反序列化 ItineraryItemResponse
     */
    fun deserializeItineraryItem(jsonString: String): ItineraryItemResponse {
        return json.decodeFromString(ItineraryItemResponse.serializer(), jsonString)
    }

    /**
     * 將 RouteResponse 序列化為 JSON 字串
     */
    fun serializeRoute(route: RouteResponse): String {
        return json.encodeToString(RouteResponse.serializer(), route)
    }

    /**
     * 將 DraftResponse 序列化為 JSON 字串
     */
    fun serializeDraft(draft: DraftResponse): String {
        return json.encodeToString(DraftResponse.serializer(), draft)
    }

    /**
     * 從 JSON 字串反序列化 DraftResponse
     */
    fun deserializeDraft(jsonString: String): DraftResponse {
        return json.decodeFromString(DraftResponse.serializer(), jsonString)
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
}

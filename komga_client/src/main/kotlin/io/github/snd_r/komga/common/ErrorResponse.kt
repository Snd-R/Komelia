package io.github.snd_r.komga.common

import io.github.snd_r.komga.serializers.InstantIsoStringSerializer
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import java.time.Instant

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val path: String,
    val status: Int,
    @Serializable(InstantIsoStringSerializer::class)
    val timestamp: Instant
)

suspend fun ResponseException.toErrorResponse(): ErrorResponse? =
    try {
        response.body()
    } catch (e: SerializationException) {
        null
    } catch (e: JsonConvertException) {
        null
    }

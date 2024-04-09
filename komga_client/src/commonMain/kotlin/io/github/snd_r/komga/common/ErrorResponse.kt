package io.github.snd_r.komga.common

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.serialization.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val path: String,
    val status: Int,
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

package io.github.snd_r.komga.common

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

@Serializable
data class ViolationErrorResponse(
    val violations: List<DataViolation>
)

@Serializable
data class DataViolation(
    val fieldName: String,
    val message: String
)

suspend fun ResponseException.toViolationResponse(): ViolationErrorResponse? =
    try {
        response.body()
    } catch (e: SerializationException) {
        null
    } catch (e: JsonConvertException) {
        null
    }

@file:UseSerializers(ZonedDateTimeSerializer::class)

package io.github.snd_r.komga.user

import io.github.snd_r.komga.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.ZonedDateTime

@Serializable
data class KomgaAuthenticationActivity(
    val userId: KomgaUserId?,
    val email: String?,
    val ip: String?,
    val userAgent: String?,
    val success: Boolean,
    val error: String?,
    val dateTime: ZonedDateTime,
    val source: String
)
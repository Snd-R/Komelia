package io.github.snd_r.komga.user

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class KomgaAuthenticationActivity(
    val userId: KomgaUserId?,
    val email: String?,
    val ip: String?,
    val userAgent: String?,
    val success: Boolean,
    val error: String?,
    val dateTime: Instant,
    val source: String
)
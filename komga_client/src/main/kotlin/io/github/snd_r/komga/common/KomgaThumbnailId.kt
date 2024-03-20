package io.github.snd_r.komga.common

import kotlinx.serialization.Serializable


@Serializable
@JvmInline
value class KomgaThumbnailId(val value: String) {
    override fun toString() = value
}


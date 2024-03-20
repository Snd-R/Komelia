package io.github.snd_r.komga.serializers

import io.github.snd_r.komga.common.KomgaReadingDirection
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class KomgaReadingDirectionSerializer : KSerializer<KomgaReadingDirection?> {

    override val descriptor = PrimitiveSerialDescriptor("KomgaReadingDirection", PrimitiveKind.STRING).nullable

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: KomgaReadingDirection?) {
        if (value == null) encoder.encodeNull()
        else encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): KomgaReadingDirection? {
        val value = decoder.decodeString()

        return if (value.isBlank()) null
        else KomgaReadingDirection.valueOf(value)
    }
}
package io.github.snd_r.komelia.platform

import kotlin.jvm.JvmInline

data class PlatformDecoderDescriptor(
    val platformType: PlatformDecoderType,
    val upscaleOptions: List<UpscaleOption>,
    val downscaleOptions: List<DownscaleOption>,

    //TODO move out of common?
    val isOnnx: Boolean,
)

data class PlatformDecoderSettings(
    val platformType: PlatformDecoderType,
    val upscaleOption: UpscaleOption,
    val downscaleOption: DownscaleOption,
)

expect enum class PlatformDecoderType {
    ;

    fun getDisplayName(): String

}

expect fun getPlatformDecoders(): List<PlatformDecoderDescriptor>

@JvmInline
value class UpscaleOption(val value: String)

@JvmInline
value class DownscaleOption(val value: String)

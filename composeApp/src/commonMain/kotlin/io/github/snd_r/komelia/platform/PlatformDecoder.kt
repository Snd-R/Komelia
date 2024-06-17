package io.github.snd_r.komelia.platform

import kotlin.jvm.JvmInline

data class PlatformDecoderDescriptor(
    val platformType: PlatformDecoderType,
    val upscaleOptions: List<UpscaleOption>,
    val downscaleOptions: List<DownscaleOption>,
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

@JvmInline
value class UpscaleOption(val value: String)

@JvmInline
value class DownscaleOption(val value: String)

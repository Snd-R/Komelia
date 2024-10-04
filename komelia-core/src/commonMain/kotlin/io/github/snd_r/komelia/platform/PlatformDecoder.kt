package io.github.snd_r.komelia.platform

import kotlin.jvm.JvmInline

data class PlatformDecoderDescriptor(
    val upscaleOptions: List<UpscaleOption>,
    val downscaleOptions: List<DownscaleOption>,
)

data class PlatformDecoderSettings(
    val upscaleOption: UpscaleOption,
    val downscaleOption: DownscaleOption,
)


@JvmInline
value class UpscaleOption(val value: String)

@JvmInline
value class DownscaleOption(val value: String)

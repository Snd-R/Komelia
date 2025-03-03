package io.github.snd_r.komelia.image

actual fun availableUpsamplingModes() = listOf(
    UpsamplingMode.CATMULL_ROM,
    UpsamplingMode.MITCHELL,
    UpsamplingMode.BILINEAR,
    UpsamplingMode.NEAREST,
)

package io.github.snd_r.komelia.image

actual fun availableUpsamplingModes() = listOf(
    UpsamplingMode.CATMULL_ROM,
    UpsamplingMode.BILINEAR,
    UpsamplingMode.NEAREST,
//    UpsamplingMode.MITCHELL,
)

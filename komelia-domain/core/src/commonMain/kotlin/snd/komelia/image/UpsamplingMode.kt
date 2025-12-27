package snd.komelia.image

enum class UpsamplingMode {
    NEAREST,
    BILINEAR,
    MITCHELL,
    CATMULL_ROM,
}

expect fun availableUpsamplingModes(): List<UpsamplingMode>
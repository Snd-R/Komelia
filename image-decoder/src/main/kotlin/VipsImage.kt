package io.github.snd_r


class VipsImage(
    val data: ByteArray,
    val width: Int,
    val height: Int,
    val bands: Int,
    val type: VipsInterpretation
)

enum class VipsInterpretation {
    VIPS_INTERPRETATION_ERROR,
    VIPS_INTERPRETATION_B_W,
    VIPS_INTERPRETATION_sRGB,
}

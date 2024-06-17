package io.github.snd_r

/**
 * [VipsInterpretation.VIPS_INTERPRETATION_sRGB] uses RGBA_8888 format
 * [VipsInterpretation.VIPS_INTERPRETATION_B_W] uses GRAYSCALE_8 format
 */
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

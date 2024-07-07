package io.github.snd_r.komelia.platform

val skiaSamplerMitchell = UpscaleOption("Bicubic Mitchell-Netravali")
val skiaSamplerCatmullRom = UpscaleOption("Bicubic Catmull-Rom")
val skiaSamplerNearest = UpscaleOption("Nearest neighbour")
val vipsDownscaleLanczos = DownscaleOption("Lanczos")

val upsamplingFilters = listOf(skiaSamplerMitchell, skiaSamplerCatmullRom, skiaSamplerNearest)

actual enum class PlatformDecoderType {
    VIPS,
    VIPS_ONNX;

    actual fun getDisplayName(): String {
        return when (this) {
            VIPS -> "Vips"
            VIPS_ONNX -> "Vips/ONNX"
        }
    }
}
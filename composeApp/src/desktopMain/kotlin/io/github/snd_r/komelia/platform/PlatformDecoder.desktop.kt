package io.github.snd_r.komelia.platform

val vipsUpscaleBicubic = UpscaleOption("Bicubic")
val vipsDownscaleLanczos = DownscaleOption("Lanczos")

val imageIoUpscale = UpscaleOption("Lanczos")
val imageIoDownscale = DownscaleOption("Lanczos")

actual enum class PlatformDecoderType {
    VIPS,
    VIPS_ONNX,
    IMAGE_IO;

    actual fun getDisplayName(): String {
        return when (this) {
            VIPS -> "Vips"
            VIPS_ONNX -> "Vips/ONNX"
            IMAGE_IO -> "Java ImageIO"
        }
    }
}
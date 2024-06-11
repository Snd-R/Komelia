package io.github.snd_r.komelia.platform

import io.github.snd_r.VipsDecoder
import io.github.snd_r.komelia.platform.PlatformDecoderType.IMAGE_IO
import io.github.snd_r.komelia.platform.PlatformDecoderType.VIPS

val vipsUpscaleBicubic = UpscaleOption("Bicubic")
val vipsDownscaleLanczos = DownscaleOption("Lanczos")

val imageIoUpscale = UpscaleOption("Lanczos")
val imageIoDownscale = DownscaleOption("Lanczos")

actual fun getPlatformDecoders(): List<PlatformDecoderDescriptor> {
    return listOf(
        if (VipsDecoder.isAvailable) {
            PlatformDecoderDescriptor(
                platformType = VIPS,
                upscaleOptions = listOf(vipsUpscaleBicubic),
                downscaleOptions = listOf(vipsDownscaleLanczos),
                isOnnx = true
            )
        } else {
            PlatformDecoderDescriptor(
                platformType = IMAGE_IO,
                upscaleOptions = listOf(imageIoUpscale),
                downscaleOptions = listOf(imageIoDownscale),
                isOnnx = false
            )
        }
    )
}

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
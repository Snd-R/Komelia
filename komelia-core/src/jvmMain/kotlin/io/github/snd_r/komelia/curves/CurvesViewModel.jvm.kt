package io.github.snd_r.komelia.curves

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import io.github.snd_r.VipsBitmapFactory
import io.github.snd_r.VipsImage
import io.github.snd_r.VipsImage.Companion.DIMENSION_MAX_SIZE
import io.github.snd_r.komelia.image.PlatformImage

actual fun getImage(): VipsImage {
    return VipsImage.decodeFromFile("")
}

actual fun toImageBitmap(image: PlatformImage): ImageBitmap {
    val skiaBitmap = VipsBitmapFactory.toSkiaBitmap(image)
    return skiaBitmap.asComposeImageBitmap()
}

@OptIn(ExperimentalUnsignedTypes::class)
actual fun getHistogram(image: VipsImage): Histogram {
    val vipsHistogram = image.makeHistogram()
    val bands = vipsHistogram.bands
    val data = vipsHistogram.getBytes()
//    vipsHistogram.close()

    val channels = mutableListOf<LookupTable>()
    for (i in 0 until bands) {
        val sliceStart = i * 256
        val channel = data.copyOfRange(sliceStart, sliceStart + 256).asUByteArray()
        channels.add(LookupTable(channel))
    }
    return Histogram(channels)
}

@OptIn(ExperimentalUnsignedTypes::class)
actual fun transformImage(
    image: VipsImage,
    targetHeight: Int,
    colorLut: LookupTable?,
    redLut: LookupTable?,
    greenLut: LookupTable?,
    blueLut: LookupTable?,
): VipsImage {

    val remapped = when (image.bands) {
        1 -> mapGrayscaleLut(image, colorLut)
        4 -> mapRGBALut(image, colorLut, redLut, greenLut, blueLut)
        else -> error("Unsupported number of image bands")
    }

    return remapped.resize(DIMENSION_MAX_SIZE, targetHeight, false)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun mapGrayscaleLut(image: VipsImage, lut: LookupTable?): VipsImage {
    if (lut == null) return image
    return image.mapLookupTable(lut.values.toByteArray())
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun mapRGBALut(
    image: VipsImage,
    colorLut: LookupTable?,
    redLut: LookupTable?,
    greenLut: LookupTable?,
    blueLut: LookupTable?,
): VipsImage {
    val colorTransformed = colorLut?.let {
        val lookupTable = colorLut.values.toByteArray()
        image.mapLookupTable(getRGBALut(lookupTable, lookupTable, lookupTable))
    } ?: image

    if (redLut == null && greenLut == null && blueLut == null) return colorTransformed

    val red = redLut?.values?.toByteArray() ?: identityMap
    val green = greenLut?.values?.toByteArray() ?: identityMap
    val blue = blueLut?.values?.toByteArray() ?: identityMap
    return colorTransformed.mapLookupTable(getRGBALut(red, green, blue))
}

private fun getRGBALut(
    red: ByteArray,
    green: ByteArray,
    blue: ByteArray
): ByteArray {
    val alpha = identityMap

    val result = ByteArray(256 * 4)
    for (i in (0..<256)) {
        val index = i * 4
        result[index] = red[i]
        result[index + 1] = green[i]
        result[index + 2] = blue[i]
        result[index + 3] = alpha[i]
    }

    return result

}

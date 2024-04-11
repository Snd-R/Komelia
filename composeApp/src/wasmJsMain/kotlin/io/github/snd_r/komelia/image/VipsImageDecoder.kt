package io.github.snd_r.komelia.image

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Scale
import coil3.size.isOriginal
import coil3.size.pxOrElse
import io.ktor.util.*
import okio.use
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

private const val vipsMaxSize = 10000000

// TODO offload to web worker?
class VipsImageDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val vips: JsAny,
) : Decoder {

    @OptIn(ExperimentalCoilApi::class)
    override suspend fun decode(): DecodeResult {
        // FIXME blocks and waits until loaded from network then copies to js array
        val data = source.source().use { it.readByteArray() }.toJsArray()

        // FIXME blocking decode
        val decoded = if (options.size.isOriginal) {
            vipsImageFromBuffer(vips, data)
        } else {
            val dstWidth = options.size.width.pxOrElse { vipsMaxSize }
            val dstHeight = options.size.height.pxOrElse { vipsMaxSize }
            val crop = options.scale == Scale.FILL
            vipsThumbnail(vips, data, dstWidth, dstHeight, crop)
        }

        val bitmap = toBitmap(decoded)
        bitmap.setImmutable()
        vipsImageDelete(decoded)

        return DecodeResult(
            image = bitmap.asCoilImage(),
            isSampled = !options.size.isOriginal
        )
    }

    private fun toBitmap(image: JsAny): Bitmap {

        return when (vipsGetInterpretation(image)) {
            "b-w" -> grayscaleToBitmap(image)
            "srgb" -> sRGBImageToBitmap(image)
            else -> throw IllegalStateException("Decode error")
        }
    }

    private fun grayscaleToBitmap(image: JsAny): Bitmap {
        val bands = vipsGetBands(image)
        require(bands == 1) { "Unexpected number of bands  for grayscale image \"${bands}\"" }

        val colorInfo = ColorInfo(
            ColorType.GRAY_8,
            ColorAlphaType.UNPREMUL,
            ColorSpace.sRGB
        )

        val imageInfo = ImageInfo(colorInfo, vipsGetWidth(image), vipsGetHeight(image))
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)
        bitmap.installPixels(vipsGetBytes(image).toByteArray())
        return bitmap
    }

    private fun sRGBImageToBitmap(image: JsAny): Bitmap {
        val bands = vipsGetBands(image)
        require(bands == 4) { "Unexpected number of bands  for sRGB image  \"${bands}\"" }
        val colorInfo = ColorInfo(
            ColorType.RGBA_8888,
            ColorAlphaType.UNPREMUL,
            ColorSpace.sRGB
        )

        val imageInfo = ImageInfo(colorInfo, vipsGetWidth(image), vipsGetHeight(image))
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)
        bitmap.installPixels(vipsGetBytes(image).toByteArray())
        return bitmap
    }

    class Factory(
        private
        val vips: JsAny
    ) : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder {
            return VipsImageDecoder(result.source, options, vips)
        }
    }
}


private fun vipsThumbnail(vips: JsAny, blob: Int8Array, dstWidth: Int, dstHeight: Int, shouldCrop: Boolean): JsAny {
    js(
        """
    let image = vips.Image.thumbnailBuffer(
        blob,
        dstWidth,
        {
            height: dstHeight,
            ...(shouldCrop && {crop: 'entropy'})
        }
    );

    if (image.interpretation == 'b-w' && image.bands != 1 ||
        image.interpretation != 'srgb' && image.interpretation != 'b-w'
    ) {
        let old = image
        image = image.colourspace('srgb');
        old.delete()
    }
    if (image.interpretation == 'srgb' && image.bands == 3) {
        let old = image
        image = image.bandjoin(255)
        old.delete()
    }

    return image;
"""
    )
}

private fun vipsImageFromBuffer(vips: JsAny, blob: Int8Array): JsAny {

    js(
        """
    let image = vips.Image.newFromBuffer(blob);

    if (image.interpretation == 'b-w' && image.bands != 1 ||
        image.interpretation != 'srgb' && image.interpretation != 'b-w'
    ) {
        let old = image;
        image = image.colourspace('srgb');
        old.delete();
    }
    if (image.interpretation == 'srgb' && image.bands == 3) {
        let old = image;
        image = image.bandjoin(255);
        old.delete();
    }

    return image;
    """
    )
}

private fun vipsGetInterpretation(image: JsAny): String {
    js(
        """
          return image.interpretation;
        """
    )
}

private fun vipsGetBytes(image: JsAny): Uint8Array {
    js(
        """
          return image.writeToMemory();
        """
    )
}

private fun vipsGetBands(image: JsAny): Int {
    js(
        """
          return image.bands;
        """
    )
}

private fun vipsGetWidth(image: JsAny): Int {
    js(
        """
          return image.width;
        """
    )
}

private fun vipsGetHeight(image: JsAny): Int {
    js(
        """
          return image.height;
        """
    )
}

private fun vipsImageDelete(image: JsAny): Int {
    js(
        """
          return image.delete();
        """
    )
}

private fun Uint8Array.toByteArray(): ByteArray = ByteArray(this.length) { this[it] }

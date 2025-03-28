package snd.komelia.image.wasm.actor

import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.toUByteArray
import org.khronos.webgl.toUint8Array
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.dom.HIGH
import org.w3c.dom.ImageBitmap
import org.w3c.dom.ImageBitmapOptions
import org.w3c.dom.ImageData
import org.w3c.dom.ResizeQuality
import org.w3c.files.Blob
import snd.komelia.image.ImageFormat
import snd.komelia.image.wasm.jsArray
import snd.komelia.image.wasm.messages.CloseImageRequest
import snd.komelia.image.wasm.messages.DecodeAndResizeRequest
import snd.komelia.image.wasm.messages.DecodeRequest
import snd.komelia.image.wasm.messages.ExtractAreaRequest
import snd.komelia.image.wasm.messages.FindTrimRequest
import snd.komelia.image.wasm.messages.GetBytesRequest
import snd.komelia.image.wasm.messages.ImageResponse
import snd.komelia.image.wasm.messages.MakeHistogramRequest
import snd.komelia.image.wasm.messages.MapLookupTableRequest
import snd.komelia.image.wasm.messages.ResizeRequest
import snd.komelia.image.wasm.messages.ShrinkRequest
import snd.komelia.image.wasm.messages.closeImageResponse
import snd.komelia.image.wasm.messages.findTrimResponse
import snd.komelia.image.wasm.messages.getBytesResponse
import snd.komelia.image.wasm.messages.imageResponse
import snd.komelia.image.wasm.toBlob
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalUnsignedTypes::class)
internal class CanvasImageActor(workerScope: DedicatedWorkerGlobalScope) :
    AbstractImageActor<ImageBitmap>(workerScope) {

    private val histograms = mutableMapOf<Int, UByteArray>()

    override suspend fun decodeAndResize(message: DecodeAndResizeRequest) {
        val blob = toBlob(message.buffer)

        val width = message.width
        val height = message.height
        val bitmap: ImageBitmap = workerScope.createImageBitmap(blob).await()

        if (width == null && height == null) {
            val imageId = saveImage(bitmap)
            workerScope.postMessage(bitmap.toImageResponse(message.requestId, imageId))
            return
        }

        val cropped = if (message.crop && width != null && height != null) {
            val cropped = cropToAspectRatio(bitmap, width, height)
            cropped
        } else null

        val resized = resizeImage(cropped ?: bitmap, message.width, message.height)
        cropped?.close()
        bitmap.close()

        val imageId = saveImage(resized)
        workerScope.postMessage(resized.toImageResponse(message.requestId, imageId))
    }

    override suspend fun decode(message: DecodeRequest) {
        val blob = toBlob(message.buffer)
        val bitmap: ImageBitmap = workerScope.createImageBitmap(blob).await()
        val imageId = saveImage(bitmap)
        workerScope.postMessage(bitmap.toImageResponse(message.requestId, imageId))
    }

    override suspend fun resize(message: ResizeRequest) {
        val image = requireNotNull(images[message.imageId])
        val resized = resizeImage(image, message.scaleWidth, message.scaleHeight)
        val imageId = saveImage(resized)
        workerScope.postMessage(resized.toImageResponse(message.requestId, imageId))
    }

    override suspend fun extractArea(message: ExtractAreaRequest) {
        val image = requireNotNull(images[message.imageId])
        val width = message.right - message.left
        val height = message.bottom - message.top
        val canvas = createOffscreenCanvas(width, height)
        canvas.getContext("2d").apply {
            drawImage(
                image = image,
                sx = message.left.toDouble(),
                sy = message.top.toDouble(),
                sw = width.toDouble(),
                sh = height.toDouble(),
                dx = 0.0,
                dy = 0.0,
                dw = width.toDouble(),
                dh = height.toDouble()
            )
        }

        val bitmap = canvas.transferToImageBitmap()
        val imageId = saveImage(bitmap)
        workerScope.postMessage(bitmap.toImageResponse(message.requestId, imageId))
    }

    override suspend fun shrink(message: ShrinkRequest) {
        val image = requireNotNull(images[message.imageId])
        val dstWidth = (image.width / message.factor).roundToInt()
        val dstHeight = (image.height / message.factor).roundToInt()
        val resized = resizeImage(image, dstWidth, dstHeight)
        val imageId = saveImage(resized)
        workerScope.postMessage(resized.toImageResponse(message.requestId, imageId))
    }

    //TODO
    override suspend fun findTrim(message: FindTrimRequest) {
        val image = requireNotNull(images[message.imageId])
        workerScope.postMessage(
            findTrimResponse(
                requestId = message.requestId,
                left = 0,
                top = 0,
                right = image.width,
                bottom = image.height
            )
        )
    }

    override suspend fun makeHistogram(message: MakeHistogramRequest) {
        val image = requireNotNull(images[message.imageId])
        val bytes = image.getBytes().toUByteArray()
        val id = imageCounter++
        histograms[id] = makeHistogram(bytes)

        workerScope.postMessage(
            imageResponse(
                requestId = message.requestId,
                imageId = id,
                width = 256,
                height = 1,
                bands = 4,
                format = ImageFormat.HISTOGRAM.name
            )
        )
    }

    private fun makeHistogram(bytes: UByteArray): UByteArray {
        val redFrequencies = LongArray(256)
        val greenFrequencies = LongArray(256)
        val blueFrequencies = LongArray(256)
        val alphaFrequencies = LongArray(256)
        val bandSize = bytes.size / 4

        for (i in 0 until bandSize) {
            val index = i * 4
            val redValue = bytes[index].toInt()
            val greenValue = bytes[index + 1].toInt()
            val blueValue = bytes[index + 2].toInt()
            val alphaValue = bytes[index + 3].toInt()
            redFrequencies[redValue] += 1L
            greenFrequencies[greenValue] += 1L
            blueFrequencies[blueValue] += 1L
            alphaFrequencies[alphaValue] += 1L
        }

        val redMaxFreq = (redFrequencies.max() - 1).toDouble()
        val greenMaxFreq = (greenFrequencies.max() - 1).toDouble()
        val blueMaxFreq = (blueFrequencies.max() - 1).toDouble()
        val alphaMaxFreq = (alphaFrequencies.max() - 1).toDouble()

        val histogram = UByteArray(1024)
        for (i in 0 until 256) {
            val index = i * 4
            histogram[index] = ((redFrequencies[i] / redMaxFreq) * 255).roundToInt().toUByte()
            histogram[index + 1] = ((greenFrequencies[i] / greenMaxFreq) * 255).roundToInt().toUByte()
            histogram[index + 2] = ((blueFrequencies[i] / blueMaxFreq) * 255).roundToInt().toUByte()
            histogram[index + 3] = ((alphaFrequencies[i] / alphaMaxFreq) * 255).roundToInt().toUByte()
        }

        return histogram
    }

    override suspend fun mapLookupTable(message: MapLookupTableRequest) {
        val image = requireNotNull(images[message.imageId])
        val bytes = image.getBytes()
        // 90% of time is spent copying image data from js to wasm and back (~100ms for 1000x1000px image)
        // it's faster to do all mapping on js side
        val imageData = mapLutJs(image, bytes, message.table)
        val bitmap = workerScope.createImageBitmap(imageData).await<ImageBitmap>()
        val imageId = saveImage(bitmap)
        workerScope.postMessage(bitmap.toImageResponse(message.requestId, imageId))
    }

    override suspend fun getBytes(message: GetBytesRequest) {
        val image = images[message.imageId]
        val histogram = histograms.remove(message.imageId)
        if (image == null && histogram == null) {
            error("Failed to find image ${message.imageId}")
        }
        val uint8Array = requireNotNull(image?.getBytes() ?: histogram?.toUint8Array())
        workerScope.postMessage(
            getBytesResponse(requestId = message.requestId, bytes = uint8Array),
            jsArray(uint8Array.buffer)
        )
    }

    private fun ImageBitmap.getBytes(): Uint8Array {
        val canvas = createOffscreenCanvas(width, height)
        val context = canvas.getContext("2d").apply { drawImage(this@getBytes, 0.0, 0.0) }
        val imageData = context.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        return Uint8Array(imageData.data.buffer)
    }

    override suspend fun closeImage(message: CloseImageRequest) {
        val image = images.remove(message.imageId)
        val histogram = histograms.remove(message.imageId)
        if (image == null && histogram == null) {
            error("Failed to find image ${message.imageId}")
        }
        image?.close()
        workerScope.postMessage(closeImageResponse(requestId = message.requestId))
    }

    private suspend fun resizeImage(bitmap: ImageBitmap, width: Int?, height: Int?): ImageBitmap {
        val widthRatio =
            (width ?: bitmap.width).toDouble() / bitmap.width
        val heightRatio =
            (height ?: bitmap.height).toDouble() / bitmap.height

        val scaleRatio = min(widthRatio, heightRatio)

        return workerScope.createImageBitmap(
            bitmap,
            ImageBitmapOptions(
                resizeWidth = (bitmap.width * scaleRatio).roundToInt(),
                resizeHeight = (bitmap.height * scaleRatio).roundToInt(),
                resizeQuality = ResizeQuality.HIGH
            )
        ).await()
    }

    private fun cropToAspectRatio(bitmap: ImageBitmap, width: Int, height: Int): ImageBitmap? {
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2

        val originalAspectRatio = bitmap.width.toDouble() / bitmap.height
        val targetAspectRatio = width.toDouble() / height
        val canvas: OffscreenCanvas
        if (originalAspectRatio > targetAspectRatio) {
            //width
            val targetWidth = (bitmap.height.toDouble() * width / height).roundToInt()
            if (targetWidth == width) return null
            canvas = createOffscreenCanvas(targetWidth, bitmap.height)
            canvas.getContext("2d").apply {
                drawImage(
                    image = bitmap,
                    sx = centerX - targetWidth.toDouble() / 2,
                    sy = centerY - bitmap.height.toDouble() / 2,
                    sw = targetWidth.toDouble(),
                    sh = bitmap.height.toDouble(),
                    dx = 0.0,
                    dy = 0.0,
                    dw = targetWidth.toDouble(),
                    dh = bitmap.height.toDouble()
                )
            }

        } else {
            //height
            val targetHeight = (bitmap.width.toDouble() * height / width).roundToInt()
            if (targetHeight == height) return null
            canvas = createOffscreenCanvas(bitmap.width, targetHeight)
            canvas.getContext("2d").apply {
                drawImage(
                    image = bitmap,
                    sx = centerX - bitmap.width.toDouble() / 2,
                    sy = centerY - targetHeight.toDouble() / 2,
                    sw = bitmap.width.toDouble(),
                    sh = targetHeight.toDouble(),
                    dx = 0.0,
                    dy = 0.0,
                    dw = bitmap.width.toDouble(),
                    dh = targetHeight.toDouble()
                )
            }

        }

        return canvas.transferToImageBitmap()
    }

    private fun ImageBitmap.toImageResponse(requestId: Int, imageId: Int): ImageResponse {
        return imageResponse(
            requestId = requestId,
            imageId = imageId,
            width = width,
            height = height,
            bands = 4,
            format = ImageFormat.RGBA_8888.name
        )
    }
}

external interface OffscreenCanvas : JsAny {
    val width: Int
    val height: Int
    fun getContext(type: String): CanvasRenderingContext2D
    fun convertToBlob(): Blob
    fun transferToImageBitmap(): ImageBitmap
}

private fun createOffscreenCanvas(width: Int, height: Int): OffscreenCanvas {
    js("return new OffscreenCanvas(width, height);")
}


private fun mapLutJs(image: ImageBitmap, imageBytes: Uint8Array, lut: Uint8Array): ImageData {
    js(
        """
        let newBytes = new Uint8ClampedArray(imageBytes.length);
        let bandSize = imageBytes.length / 4;

        let redLut = new Uint8Array(256);
        let greenLut = new Uint8Array(256);
        let blueLut = new Uint8Array(256);
        let alphaLut = new Uint8Array(256);

        for (let i = 0; i < 256; i++) {
          let index = i * 4;
          redLut[i] = lut[index];
          greenLut[i] = lut[index + 1];
          blueLut[i] = lut[index + 2];
          alphaLut[i] = lut[index + 3];
        }

        for (let i = 0; i < bandSize; i++) {
          let index = i * 4;
          let redIndex = imageBytes[index];
          let greenIndex = imageBytes[index + 1];
          let blueIndex = imageBytes[index + 2];
          let alphaIndex = imageBytes[index + 3];
          newBytes[index] = redLut[redIndex];
          newBytes[index + 1] = greenLut[greenIndex];
          newBytes[index + 2] = blueLut[blueIndex];
          newBytes[index + 3] = alphaLut[alphaIndex];
        }

        return new ImageData(newBytes, image.width, image.height);
    """
    )
}
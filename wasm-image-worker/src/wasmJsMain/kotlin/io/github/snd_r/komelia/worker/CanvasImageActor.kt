package io.github.snd_r.komelia.worker

import io.github.snd_r.komelia.worker.messages.DecodeAndGetRequest
import io.github.snd_r.komelia.worker.messages.DimensionsRequest
import io.github.snd_r.komelia.worker.messages.WorkerMessage
import io.github.snd_r.komelia.worker.messages.WorkerMessageType
import io.github.snd_r.komelia.worker.messages.WorkerMessageType.DECODE_AND_GET_DATA
import io.github.snd_r.komelia.worker.messages.WorkerMessageType.GET_DIMENSIONS
import io.github.snd_r.komelia.worker.messages.WorkerMessageType.INIT
import io.github.snd_r.komelia.worker.messages.decodeAndGetResponse
import io.github.snd_r.komelia.worker.messages.dimensionsResponse
import io.github.snd_r.komelia.worker.util.toBlob
import io.github.snd_r.komelia.worker.util.workerBufferTransferParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.dom.ImageBitmap
import org.w3c.dom.ImageBitmapOptions
import org.w3c.dom.ImageData
import org.w3c.dom.MEDIUM
import org.w3c.dom.ResizeQuality
import kotlin.math.min
import kotlin.math.roundToInt

class CanvasImageActor(
    private val self: DedicatedWorkerGlobalScope
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    fun launch() {
        self.onmessage = { messageEvent ->
            val message = messageEvent.data as WorkerMessage
            when (WorkerMessageType.valueOf(message.type)) {
                INIT -> self.postMessage(message)
                DECODE_AND_GET_DATA -> coroutineScope.launch { handleDecode(message as DecodeAndGetRequest) }
                GET_DIMENSIONS -> coroutineScope.launch { handleGetDimensions(message as DimensionsRequest) }
            }
        }
    }

    private suspend fun handleDecode(request: DecodeAndGetRequest) {
        val blob = toBlob(request.buffer)
        val originalBitmap: ImageBitmap = self.createImageBitmap(blob).await()

        val resizedBitmap: ImageBitmap = if (request.width == null && request.height == null) {
            originalBitmap
        } else {
            val widthRatio =
                (request.width ?: originalBitmap.width).toDouble() / originalBitmap.width
            val heightRatio =
                (request.height ?: originalBitmap.height).toDouble() / originalBitmap.height

            val scaleRatio = min(widthRatio, heightRatio).coerceAtMost(1.0)

            self.createImageBitmap(
                originalBitmap,
                ImageBitmapOptions(
                    resizeWidth = (originalBitmap.width * scaleRatio).roundToInt(),
                    resizeHeight = (originalBitmap.height * scaleRatio).roundToInt(),
                    resizeQuality = ResizeQuality.MEDIUM
                )
            ).await()
        }
        val canvas = createOffscreenCanvas(resizedBitmap.width, resizedBitmap.height)
        val context = getCanvasContext(canvas)
        drawImage(context, resizedBitmap)
        val imageData = getImageData(context, resizedBitmap.width, resizedBitmap.height)
        val uint8Array = toUint8Array(imageData.data.buffer)

        self.postMessage(
            decodeAndGetResponse(
                requestId = request.requestId,
                width = resizedBitmap.width,
                height = resizedBitmap.height,
                bands = 4,
                interpretation = "srgb",
                buffer = uint8Array,
            ),
            workerBufferTransferParam(uint8Array.buffer)
        )
        resizedBitmap.close()
        originalBitmap.close()
    }

    private suspend fun handleGetDimensions(request: DimensionsRequest) {
        val blob = toBlob(request.buffer)
        val imageBitmap = self.createImageBitmap(blob).await<ImageBitmap>()

        self.postMessage(
            dimensionsResponse(
                requestId = request.requestId,
                width = imageBitmap.width,
                height = imageBitmap.height,
                bands = 4
            )
        )

        imageBitmap.close()
    }
}


private fun createOffscreenCanvas(width: Int, height: Int): JsAny {
    js("return new OffscreenCanvas(width, height);")
}

private fun getCanvasContext(canvas: JsAny): JsAny {
    js("return canvas.getContext(\"2d\");")
}

private fun drawImage(context: JsAny, image: ImageBitmap) {
    js("context.drawImage(image, 0, 0);")
}

private fun getImageData(context: JsAny, width: Int, height: Int): ImageData {
    js("return context.getImageData(0, 0, width, height);")
}

private fun toUint8Array(buffer: ArrayBuffer): Uint8Array {
    js("return new Uint8Array(buffer);")
}
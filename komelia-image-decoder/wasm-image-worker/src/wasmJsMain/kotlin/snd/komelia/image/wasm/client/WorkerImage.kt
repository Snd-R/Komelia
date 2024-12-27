package snd.komelia.image.wasm.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import snd.komelia.image.ImageFormat
import snd.komelia.image.ImageRect
import snd.komelia.image.KomeliaImage
import snd.komelia.image.wasm.asByteArray
import snd.komelia.image.wasm.jsArray
import snd.komelia.image.wasm.messages.CloseImageResponse
import snd.komelia.image.wasm.messages.FindTrimResponse
import snd.komelia.image.wasm.messages.GetBytesResponse
import snd.komelia.image.wasm.messages.ImageResponse
import snd.komelia.image.wasm.messages.closeImageRequest
import snd.komelia.image.wasm.messages.extractAreaRequest
import snd.komelia.image.wasm.messages.findTrimRequest
import snd.komelia.image.wasm.messages.getBytesRequest
import snd.komelia.image.wasm.messages.makeHistogramRequest
import snd.komelia.image.wasm.messages.mapLookupTableRequest
import snd.komelia.image.wasm.messages.resizeRequest
import snd.komelia.image.wasm.messages.shrinkRequest
import snd.komelia.image.wasm.toJsArray

class WorkerImage(
    private val worker: ImageWorker,
    private val imageId: Int,
    override val width: Int,
    override val height: Int,
    override val bands: Int,
    override val type: ImageFormat
) : KomeliaImage {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    constructor(worker: ImageWorker, response: ImageResponse) : this(
        worker = worker,
        imageId = response.imageId,
        width = response.width,
        height = response.height,
        bands = response.bands,
        type = ImageFormat.valueOf(response.format)
    )

    override suspend fun extractArea(rect: ImageRect): KomeliaImage {
        val message = extractAreaRequest(worker.getNextId(), rect, imageId)
        val result = worker.postMessage<ImageResponse>(message)
        return WorkerImage(worker, result)
    }

    override suspend fun resize(scaleWidth: Int, scaleHeight: Int, crop: Boolean): KomeliaImage {
        val message = resizeRequest(worker.getNextId(), scaleWidth, scaleHeight, crop, imageId)
        val result = worker.postMessage<ImageResponse>(message)
        return WorkerImage(worker, result)
    }

    override suspend fun shrink(factor: Double): KomeliaImage {
        val message = shrinkRequest(worker.getNextId(), factor, imageId)
        val result = worker.postMessage<ImageResponse>(message)
        return WorkerImage(worker, result)
    }

    override suspend fun findTrim(): ImageRect {
        val message = findTrimRequest(worker.getNextId(), imageId)
        val result = worker.postMessage<FindTrimResponse>(message)
        return ImageRect(
            left = result.left,
            top = result.top,
            right = result.right,
            bottom = result.bottom
        )
    }

    override suspend fun makeHistogram(): KomeliaImage {
        val message = makeHistogramRequest(worker.getNextId(), imageId)
        val result = worker.postMessage<ImageResponse>(message)
        return WorkerImage(worker, result)
    }

    override suspend fun mapLookupTable(table: ByteArray): KomeliaImage {
        val tableJsArray = table.toJsArray()
        val message = mapLookupTableRequest(
            requestId = worker.getNextId(),
            imageId = imageId,
            table = tableJsArray
        )
        val result = worker.postMessage<ImageResponse>(message, jsArray(tableJsArray.buffer))
        return WorkerImage(worker, result)
    }

    override suspend fun getBytes(): ByteArray {
        val message = getBytesRequest(worker.getNextId(), imageId)
        val result = worker.postMessage<GetBytesResponse>(message)
        return result.bytes.asByteArray()
    }

    override fun close() {
        coroutineScope.launch {
            val message = closeImageRequest(worker.getNextId(), imageId)
            worker.postMessage<CloseImageResponse>(message)
        }
    }
}


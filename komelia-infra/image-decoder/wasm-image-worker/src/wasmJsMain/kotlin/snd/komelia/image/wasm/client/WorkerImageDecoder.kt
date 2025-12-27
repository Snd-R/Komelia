package snd.komelia.image.wasm.client

import snd.komelia.image.KomeliaImage
import snd.komelia.image.KomeliaImageDecoder
import snd.komelia.image.wasm.jsArray
import snd.komelia.image.wasm.messages.ImageResponse
import snd.komelia.image.wasm.messages.decodeAndResizeRequest
import snd.komelia.image.wasm.messages.decodeRequest
import snd.komelia.image.wasm.toJsArray

class WorkerImageDecoder : KomeliaImageDecoder {
    private val worker = ImageWorker()
    suspend fun init() = worker.init()

    override suspend fun decode(encoded: ByteArray, nPages: Int?): KomeliaImage {
        val jsArray = encoded.toJsArray()
        val message = decodeRequest(worker.getNextId(), jsArray)
        val result = worker.postMessage<ImageResponse>(message, jsArray(jsArray.buffer))
        return WorkerImage(worker, result)
    }

    override suspend fun decodeFromFile(path: String, nPages: Int?): KomeliaImage {
        error("File operations are not supported")
    }

    override suspend fun decodeAndResize(
        path: String,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
        nPages: Int?
    ): KomeliaImage {
        error("File operations are not supported")
    }

    override suspend fun decodeAndResize(
        encoded: ByteArray,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
        nPages: Int?
    ): KomeliaImage {
        val jsArray = encoded.toJsArray()
        val message = decodeAndResizeRequest(
            requestId = worker.getNextId(),
            width = scaleWidth,
            height = scaleHeight,
            crop = crop,
            buffer = jsArray
        )
        val result = worker.postMessage<ImageResponse>(message, jsArray(jsArray.buffer))
        return WorkerImage(worker, result)
    }
}

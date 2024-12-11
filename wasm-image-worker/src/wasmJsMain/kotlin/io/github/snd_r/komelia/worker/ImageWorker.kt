package io.github.snd_r.komelia.worker

import io.github.snd_r.komelia.worker.ImageWorker.Interpretation
import io.github.snd_r.komelia.worker.messages.DecodeAndGetResponse
import io.github.snd_r.komelia.worker.messages.DimensionsResponse
import io.github.snd_r.komelia.worker.messages.WorkerMessage
import io.github.snd_r.komelia.worker.messages.WorkerMessageType
import io.github.snd_r.komelia.worker.messages.WorkerMessageType.DECODE_AND_GET_DATA
import io.github.snd_r.komelia.worker.messages.WorkerMessageType.INIT
import io.github.snd_r.komelia.worker.messages.decodeAndGetRequest
import io.github.snd_r.komelia.worker.messages.dimensionsRequest
import io.github.snd_r.komelia.worker.messages.initMessage
import io.github.snd_r.komelia.worker.util.asJsArray
import io.github.snd_r.komelia.worker.util.workerBufferTransferParam
import kotlinx.coroutines.delay
import org.khronos.webgl.Uint8Array
import org.w3c.dom.Worker
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class ImageData(
    val width: Int,
    val height: Int,
    val bands: Int,
    val interpretation: Interpretation,
    val buffer: Uint8Array,
)

data class ImageDimensions(
    val width: Int,
    val height: Int,
    val bands: Int,
)

class ImageWorker {
    private val worker = Worker("komeliaImageWorker.js")
    private var jobIdCounter = 0
    private val decodeJobs = mutableMapOf<Int, Continuation<ImageData>>()
    private val dimensionJobs = mutableMapOf<Int, Continuation<ImageDimensions>>()
    var initialized = false
        private set


    init {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        worker.onmessage = { event ->
            val message = event.data as WorkerMessage
            when (WorkerMessageType.valueOf(message.type)) {
                INIT -> initialized = true
                DECODE_AND_GET_DATA -> decodeResponse(message as DecodeAndGetResponse)
                WorkerMessageType.GET_DIMENSIONS -> dimensionsResponse(message as DimensionsResponse)
            }
        }
    }

    suspend fun init() {
        while (!initialized) {
            worker.postMessage(initMessage())
            delay(50)
        }
    }

    suspend fun decodeAndGet(
        bytes: ByteArray,
        dstWidth: Int?,
        dstHeight: Int?,
        crop: Boolean
    ): ImageData {
        return suspendCoroutine { continuation ->
            val id = jobIdCounter++
            decodeJobs[id] = continuation

            val jsArray = bytes.asJsArray()
            worker.postMessage(
                decodeAndGetRequest(id, dstWidth, dstHeight, crop, jsArray),
                workerBufferTransferParam(jsArray.buffer)
            )
        }
    }

    suspend fun getDimensions(bytes: ByteArray): ImageDimensions {
        return suspendCoroutine { continuation ->
            val id = jobIdCounter++
            dimensionJobs[id] = continuation

            val jsArray = bytes.asJsArray()
            worker.postMessage(
                dimensionsRequest(id, jsArray),
                workerBufferTransferParam(jsArray.buffer)
            )
        }
    }

    private fun decodeResponse(message: DecodeAndGetResponse) {
        val continuation = requireNotNull(decodeJobs.remove(message.requestId))

        val response = ImageData(
            width = message.width,
            height = message.height,
            bands = message.bands,
            interpretation = when (message.interpretation) {
                "b-w" -> Interpretation.BW
                "srgb" -> Interpretation.SRGB
                else -> throw IllegalStateException(" Unsupported interpretation")
            },
            buffer = message.buffer
        )
        continuation.resume(response)
    }

    private fun dimensionsResponse(message: DimensionsResponse) {
        val continuation = requireNotNull(dimensionJobs.remove(message.requestId))
        val response = ImageDimensions(message.width, message.height, message.bands)
        continuation.resume(response)
    }


    enum class Interpretation {
        BW,
        SRGB
    }
}

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
import io.github.snd_r.komelia.worker.util.workerBufferTransferParam
import io.github.snd_r.komelia.worker.vips.Vips
import io.github.snd_r.komelia.worker.vips.vipsMaxSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.w3c.dom.DedicatedWorkerGlobalScope

class VipsImageActor(
    private val self: DedicatedWorkerGlobalScope
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    fun launch() {
        coroutineScope.launch {
            Vips.init()
            self.onmessage = { messageEvent ->
                val message = messageEvent.data as WorkerMessage
                when (WorkerMessageType.valueOf(message.type)) {
                    INIT -> self.postMessage(message)
                    DECODE_AND_GET_DATA -> handleDecode(message as DecodeAndGetRequest)
                    GET_DIMENSIONS -> handleGetDimensions(message as DimensionsRequest)
                }
            }
        }
    }

    private fun handleDecode(request: DecodeAndGetRequest) {
        val decodedImage = if (request.width == null && request.height == null) {
            Vips.vipsImageFromBuffer(request.buffer)
        } else {
            val dstWidth = request.width ?: vipsMaxSize
            val dstHeight = request.height ?: vipsMaxSize
            Vips.vipsThumbnail(request.buffer, dstWidth, dstHeight, request.crop)
        }
        val decodedBytes = decodedImage.writeToMemory()

        io.github.snd_r.komelia.worker.self.postMessage(
            decodeAndGetResponse(
                requestId = request.requestId,
                width = decodedImage.width,
                height = decodedImage.height,
                bands = decodedImage.bands,
                interpretation = decodedImage.interpretation,
                buffer = decodedBytes,
            ),
            workerBufferTransferParam(decodedBytes.buffer)
        )
        decodedImage.delete()
    }

    private fun handleGetDimensions(request: DimensionsRequest) {
        val image = Vips.vipsImageDecode(request.buffer)

        io.github.snd_r.komelia.worker.self.postMessage(
            dimensionsResponse(
                requestId = request.requestId,
                width = image.width,
                height = image.height,
                bands = image.bands
            )
        )
        image.delete()
    }
}




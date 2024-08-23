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
import io.github.snd_r.komelia.worker.vips.Vips
import io.github.snd_r.komelia.worker.vips.vipsMaxSize
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.DedicatedWorkerGlobalScope
import kotlin.time.measureTime

external val self: DedicatedWorkerGlobalScope

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
@OptIn(DelicateCoroutinesApi::class)
fun main() {
    self.importScripts("vips.js")
    GlobalScope.launch {
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
    val duration = measureTime {

        val decodedImage = if (request.width == null && request.height == null) {
            Vips.vipsImageFromBuffer(request.buffer)
        } else {
            val dstWidth = request.width ?: vipsMaxSize
            val dstHeight = request.height ?: vipsMaxSize
            Vips.vipsThumbnail(request.buffer, dstWidth, dstHeight, request.crop)
        }
        val decodedBytes = decodedImage.writeToMemory()

        self.postMessage(
            decodeAndGetResponse(
                requestId = request.requestId,
                width = decodedImage.width,
                height = decodedImage.height,
                bands = decodedImage.bands,
                interpretation = decodedImage.interpretation,
                buffer = decodedBytes,
            ),
            workerBufferTransfer(decodedBytes.buffer)
        )
        decodedImage.delete()
    }
    println("Worker finished image decode in $duration")
}

private fun handleGetDimensions(request: DimensionsRequest) {
    val image = Vips.vipsImageDecode(request.buffer)

    self.postMessage(
        dimensionsResponse(
            requestId = request.requestId,
            width = image.width,
            height = image.height,
            bands = image.bands
        )
    )
    image.delete()
}

internal fun workerBufferTransfer(bytes: ArrayBuffer): JsArray<JsAny> {
    js("return [bytes];")
}

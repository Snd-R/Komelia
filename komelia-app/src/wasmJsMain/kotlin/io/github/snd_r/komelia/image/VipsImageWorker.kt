package io.github.snd_r.komelia.image

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.Worker
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val InitMessage = "Init"
const val DecodeMessage = "Decode"

class ImageWorker {
    private val worker = Worker("imageWorker.js")
    private var jobIdCounter = 0
    private val jobs = mutableMapOf<Int, Continuation<WorkerDecodeResult>>()
    var initialized = false
        private set

    data class WorkerDecodeResult(
        val width: Int,
        val height: Int,
        val bands: Int,
        val interpretation: Interpretation,
        val buffer: Uint8Array,
    )

    init {
        worker.onmessage = { event ->
            val messageType = getMessageType(event.data)
            when (messageType) {
                InitMessage -> initialized = true
                DecodeMessage -> handleDecodeResponse(requireNotNull(event.data))
                else -> {}
            }
        }
    }

    private fun handleDecodeResponse(data: JsAny) {
        require(initialized)
        val id = getDecodeId(data)
        val continuation = requireNotNull(jobs.remove(id))

        val response = WorkerDecodeResult(
            width = getDecodeWidth(data),
            height = getDecodeHeight(data),
            bands = getDecodeBands(data),
            interpretation = when (getDecodeInterpretation(data)) {
                "b-w" -> Interpretation.BW
                "srgb" -> Interpretation.SRGB
                else -> throw IllegalStateException(" Unsupported interpretation")
            },
            buffer = getDecodeBuffer(data)
        )
        continuation.resume(response)
    }

    fun init() {
        worker.postMessage(initMessage())
    }

    suspend fun decode(
        bytes: Int8Array,
        dstWidth: Int?,
        dstHeight: Int?,
        crop: Boolean
    ) = suspendCoroutine { continuation ->
        val id = jobIdCounter++
        jobs[id] = continuation

        worker.postMessage(
            decodeRequest(id, bytes, dstWidth, dstHeight, crop),
            decodeRequestTransfer(bytes.buffer)
        )
    }


    enum class Interpretation {
        BW,
        SRGB
    }
}

private fun decodeRequest(
    requestId: Int,
    requestBuffer: Int8Array,
    requestWidth: Int?,
    requestHeight: Int?,
    requestCrop: Boolean
): JsAny {
    js(
        """
          return {
            type: 'Decode',
            id: requestId,
            width: requestWidth,
            height: requestHeight,
            crop: requestCrop,
            buffer: requestBuffer,
          };
      """
    )
}

private fun getMessageType(data: JsAny?): String? {
    js("return data.type;")
}

private fun decodeRequestTransfer(bytes: ArrayBuffer): JsArray<JsAny> {
    js("return [bytes];")
}

private fun getDecodeId(data: JsAny): Int {
    js("return data.id;")
}

private fun getDecodeWidth(data: JsAny): Int {
    js("return data.width;")
}

private fun getDecodeHeight(data: JsAny): Int {
    js("return data.height;")
}

private fun getDecodeBands(data: JsAny): Int {
    js("return data.bands;")
}

private fun getDecodeInterpretation(data: JsAny): String {
    js("return data.interpretation;")
}

private fun getDecodeBuffer(data: JsAny): Uint8Array {
    js("return data.buffer;")
}

private fun initMessage(): JsAny {
    js("return {type: 'Init'}")
}
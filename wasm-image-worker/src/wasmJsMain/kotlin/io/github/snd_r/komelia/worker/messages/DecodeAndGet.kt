package io.github.snd_r.komelia.worker.messages

import io.github.snd_r.komelia.worker.messages.WorkerMessageType.DECODE_AND_GET_DATA
import io.github.snd_r.komelia.worker.util.set
import org.khronos.webgl.Uint8Array

external interface DecodeAndGetRequest : WorkerMessage {
    val width: Int?
    val height: Int?
    val crop: Boolean
    val buffer: Uint8Array
}

external interface DecodeAndGetResponse : WorkerMessage {
    val width: Int
    val height: Int
    val bands: Int
    val interpretation: String
    val buffer: Uint8Array
}

internal fun decodeAndGetRequest(
    requestId: Int,
    width: Int?,
    height: Int?,
    crop: Boolean,
    buffer: Uint8Array,
): DecodeAndGetRequest {
    val jsObject = workerMessage<DecodeAndGetRequest>(DECODE_AND_GET_DATA, requestId)
    width?.let { jsObject["width"] = it.toJsNumber() }
    height?.let { jsObject["height"] = it.toJsNumber() }
    jsObject["crop"] = crop.toJsBoolean()
    jsObject["buffer"] = buffer
    return jsObject
}

internal fun decodeAndGetResponse(
    requestId: Int,
    width: Int,
    height: Int,
    bands: Int,
    interpretation: String,
    buffer: Uint8Array,
): DecodeAndGetResponse {
    val jsObject = workerMessage<DecodeAndGetResponse>(DECODE_AND_GET_DATA, requestId)
    jsObject["type"] = DECODE_AND_GET_DATA.name
    jsObject["requestId"] = requestId.toJsNumber()
    jsObject["width"] = width.toJsNumber()
    jsObject["height"] = height.toJsNumber()
    jsObject["bands"] = bands.toJsNumber()
    jsObject["interpretation"] = interpretation.toJsString()
    jsObject["buffer"] = buffer
    return jsObject
}
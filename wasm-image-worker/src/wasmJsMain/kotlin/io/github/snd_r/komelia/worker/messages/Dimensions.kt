package io.github.snd_r.komelia.worker.messages

import io.github.snd_r.komelia.worker.messages.WorkerMessageType.GET_DIMENSIONS
import io.github.snd_r.komelia.worker.util.set
import org.khronos.webgl.Uint8Array

external interface DimensionsRequest : WorkerMessage {
    val buffer: Uint8Array
}

external interface DimensionsResponse : WorkerMessage {
    val width: Int
    val height: Int
    val bands: Int
}


internal fun dimensionsRequest(
    requestId: Int,
    buffer: Uint8Array
): DimensionsRequest {
    val jsObject = workerMessage<DimensionsRequest>(GET_DIMENSIONS, requestId)
    jsObject["buffer"] = buffer
    return jsObject
}

internal fun dimensionsResponse(
    requestId: Int,
    width: Int,
    height: Int,
    bands: Int,
): DimensionsRequest {
    val jsObject = workerMessage<DimensionsRequest>(GET_DIMENSIONS, requestId)
    jsObject["width"] = width.toJsNumber()
    jsObject["height"] = height.toJsNumber()
    jsObject["bands"] = bands.toJsNumber()
    return jsObject
}

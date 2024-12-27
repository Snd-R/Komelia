package snd.komelia.image.wasm.messages

import snd.komelia.image.wasm.messages.WorkerMessageType.FIND_TRIM
import snd.komelia.image.wasm.set

external interface FindTrimRequest : WorkerMessage {
    val imageId: Int
}

external interface FindTrimResponse : WorkerMessage {
    val left: Int
    val top: Int
    val right: Int
    val bottom: Int
}

internal fun findTrimRequest(
    requestId: Int,
    imageId: Int,
): FindTrimRequest {
    val jsObject = workerMessage<FindTrimRequest>(FIND_TRIM, requestId)
    jsObject["imageId"] = imageId.toJsNumber()
    return jsObject
}

internal fun findTrimResponse(
    requestId: Int,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
): FindTrimResponse {
    val jsObject = workerMessage<FindTrimResponse>(FIND_TRIM, requestId)
    jsObject["left"] = left.toJsNumber()
    jsObject["top"] = top.toJsNumber()
    jsObject["right"] = right.toJsNumber()
    jsObject["bottom"] = bottom.toJsNumber()
    return jsObject
}

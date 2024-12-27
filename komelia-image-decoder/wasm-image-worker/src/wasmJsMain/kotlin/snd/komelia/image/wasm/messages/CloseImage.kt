package snd.komelia.image.wasm.messages

import snd.komelia.image.wasm.messages.WorkerMessageType.CLOSE
import snd.komelia.image.wasm.set

external interface CloseImageRequest : WorkerMessage {
    val imageId: Int
}

external interface CloseImageResponse : WorkerMessage {
}

internal fun closeImageRequest(
    requestId: Int,
    imageId: Int
): CloseImageRequest {
    val jsObject = workerMessage<CloseImageRequest>(CLOSE, requestId)
    jsObject["imageId"] = imageId.toJsNumber()
    return jsObject
}

internal fun closeImageResponse(
    requestId: Int,
): CloseImageResponse {
    val jsObject = workerMessage<CloseImageResponse>(CLOSE, requestId)
    return jsObject
}

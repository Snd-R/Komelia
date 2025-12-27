package snd.komelia.image.wasm.messages

import snd.komelia.image.ImageRect
import snd.komelia.image.wasm.messages.WorkerMessageType.EXTRACT_AREA
import snd.komelia.image.wasm.set

external interface ExtractAreaRequest : WorkerMessage {
    val imageId: Int
    val left: Int
    val top: Int
    val right: Int
    val bottom: Int
}

internal fun extractAreaRequest(
    requestId: Int,
    rect: ImageRect,
    imageId: Int
): ExtractAreaRequest {
    val jsObject = workerMessage<ExtractAreaRequest>(EXTRACT_AREA, requestId)
    jsObject["imageId"] = imageId.toJsNumber()
    jsObject["left"] = rect.left.toJsNumber()
    jsObject["top"] = rect.top.toJsNumber()
    jsObject["right"] = rect.right.toJsNumber()
    jsObject["bottom"] = rect.bottom.toJsNumber()
    return jsObject
}

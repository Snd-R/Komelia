package snd.komelia.image.wasm.messages

import snd.komelia.image.wasm.messages.WorkerMessageType.RESIZE
import snd.komelia.image.wasm.set

external interface ShrinkRequest : WorkerMessage {
    val imageId: Int
    val factor: Double
}

internal fun shrinkRequest(
    requestId: Int,
    factor: Double,
    imageId: Int
): ShrinkRequest {
    val jsObject = workerMessage<ShrinkRequest>(RESIZE, requestId)
    jsObject["imageId"] = imageId.toJsNumber()
    jsObject["factor"] = factor.toJsNumber()
    return jsObject
}

package snd.komelia.image.wasm.messages

import snd.komelia.image.wasm.messages.WorkerMessageType.RESIZE
import snd.komelia.image.wasm.set

external interface ResizeRequest : WorkerMessage {
    val imageId: Int
    val scaleWidth: Int
    val scaleHeight: Int
    val crop: Boolean
}

internal fun resizeRequest(
    requestId: Int,
    scaleWidth: Int,
    scaleHeight: Int,
    crop: Boolean,
    imageId: Int
): ResizeRequest {
    val jsObject = workerMessage<ResizeRequest>(RESIZE, requestId)
    jsObject["imageId"] = imageId.toJsNumber()
    jsObject["scaleWidth"] = scaleWidth.toJsNumber()
    jsObject["scaleHeight"] = scaleHeight.toJsNumber()
    jsObject["crop"] = crop.toJsBoolean()
    return jsObject
}

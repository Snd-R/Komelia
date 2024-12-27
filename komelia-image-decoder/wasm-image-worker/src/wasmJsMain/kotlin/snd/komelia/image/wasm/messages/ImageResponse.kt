package snd.komelia.image.wasm.messages

import snd.komelia.image.wasm.messages.WorkerMessageType.IMAGE
import snd.komelia.image.wasm.set

external interface ImageResponse : WorkerMessage {
    val imageId: Int
    val width: Int
    val height: Int
    val bands: Int
    val format: String
}

internal fun imageResponse(
    requestId: Int,
    imageId: Int,
    width: Int,
    height: Int,
    bands: Int,
    format: String,
): ImageResponse {
    val jsObject = workerMessage<ImageResponse>(IMAGE, requestId)
    jsObject["requestId"] = requestId.toJsNumber()
    jsObject["imageId"] = imageId.toJsNumber()
    jsObject["width"] = width.toJsNumber()
    jsObject["height"] = height.toJsNumber()
    jsObject["bands"] = bands.toJsNumber()
    jsObject["format"] = format.toJsString()
    return jsObject
}

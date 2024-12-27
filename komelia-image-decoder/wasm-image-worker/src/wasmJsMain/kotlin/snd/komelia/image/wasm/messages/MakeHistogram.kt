package snd.komelia.image.wasm.messages

import snd.komelia.image.wasm.messages.WorkerMessageType.MAKE_HISTOGRAM
import snd.komelia.image.wasm.set

external interface MakeHistogramRequest : WorkerMessage {
    val imageId: Int
}

internal fun makeHistogramRequest(
    requestId: Int,
    imageId: Int
): MakeHistogramRequest {
    val jsObject = workerMessage<MakeHistogramRequest>(MAKE_HISTOGRAM, requestId)
    jsObject["imageId"] = imageId.toJsNumber()
    return jsObject
}

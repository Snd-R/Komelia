package io.github.snd_r.komelia.worker.messages

import io.github.snd_r.komelia.worker.util.makeJsObject
import io.github.snd_r.komelia.worker.util.set

external interface WorkerMessage : JsAny {
    val type: String
    val requestId: Int
}

internal fun <T : WorkerMessage> workerMessage(
    type: WorkerMessageType,
    requestId: Int
): T {
    val jsObject = makeJsObject<T>()
    jsObject["type"] = type.name.toJsString()
    jsObject["requestId"] = requestId.toJsNumber()
    return jsObject
}

internal enum class WorkerMessageType {
    INIT,
    DECODE_AND_GET_DATA,
    GET_DIMENSIONS,
//    DECODE,
//    RESIZE,
//    DECODE_REGION,
//    CLOSE_IMAGE,
}

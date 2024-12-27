package snd.komelia.image.wasm.messages

import snd.komelia.image.wasm.messages.WorkerMessageType.ERROR
import snd.komelia.image.wasm.set

external interface ErrorResponse : WorkerMessage {
    val message: String
}

internal fun errorResponse(
    requestId: Int,
    message: String,
): ErrorResponse {
    val jsObject = workerMessage<ErrorResponse>(ERROR, requestId)
    jsObject["message"] = message.toJsString()
    return jsObject
}

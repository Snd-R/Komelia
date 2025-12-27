package snd.komelia.image.wasm.messages

import org.khronos.webgl.Uint8Array
import snd.komelia.image.wasm.messages.WorkerMessageType.DECODE
import snd.komelia.image.wasm.set

external interface DecodeRequest : WorkerMessage {
    val buffer: Uint8Array
}

internal fun decodeRequest(
    requestId: Int,
    buffer: Uint8Array,
): DecodeAndResizeRequest {
    val jsObject = workerMessage<DecodeAndResizeRequest>(DECODE, requestId)
    jsObject["buffer"] = buffer
    return jsObject
}

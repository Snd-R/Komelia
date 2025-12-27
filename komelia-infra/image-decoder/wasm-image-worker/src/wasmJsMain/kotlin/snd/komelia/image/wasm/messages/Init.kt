package snd.komelia.image.wasm.messages

import snd.komelia.image.wasm.messages.WorkerMessageType.INIT

external interface InitMessage : WorkerMessage

fun initMessage(): InitMessage {
    return workerMessage(INIT, 0)
}

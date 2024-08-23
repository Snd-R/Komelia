package io.github.snd_r.komelia.worker.messages

external interface InitMessage : WorkerMessage

fun initMessage(): InitMessage {
    return workerMessage(WorkerMessageType.INIT, 0)
}

package snd.komelia.image.wasm.client

import kotlinx.coroutines.delay
import org.w3c.dom.MessageEvent
import org.w3c.dom.Worker
import snd.komelia.image.wasm.messages.ErrorResponse
import snd.komelia.image.wasm.messages.WorkerMessage
import snd.komelia.image.wasm.messages.WorkerMessageType
import snd.komelia.image.wasm.messages.getType
import snd.komelia.image.wasm.messages.initMessage
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ImageWorker {
    private val counter: JobCounter = JobCounter()
    private val jobs = mutableMapOf<Int, WorkerJob<*>>()
    private var initialized = false
    private val worker = Worker("komeliaImageWorker.js")

    init {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        worker.onmessage = onMessage@{ event ->
            val message = event.data as WorkerMessage
            val job = jobs.remove(message.requestId) ?: return@onMessage

            if (message.getType() == WorkerMessageType.ERROR) {
                job.fail(IllegalStateException((message as ErrorResponse).message))
            } else {
                job.complete(message)
            }
        }
    }

    fun getNextId() = counter.incrementAndGet()

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    suspend fun init() {
        worker.onmessage = { event ->
            when ((event.data as WorkerMessage).getType()) {
                WorkerMessageType.INIT -> {
                    initialized = true
                    worker.onmessage = this::onMessage
                }

                else -> {}
            }
        }
        while (!initialized) {
            worker.postMessage(initMessage())
            delay(50)
        }
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    fun onMessage(event: MessageEvent) {
        val message = event.data as WorkerMessage
        val job = jobs.remove(message.requestId) ?: return@onMessage
        job.complete(message)
    }

    suspend fun <Result> postMessage(
        message: WorkerMessage,
        transfer: JsArray<JsAny> = JsArray(),
    ): Result {
        return suspendCoroutine { continuation ->
            worker.postMessage(message, transfer)
            jobs[message.requestId] = WorkerJob(message.requestId, continuation)
        }
    }

    private data class WorkerJob<Result>(
        val id: Int,
        val continuation: Continuation<Result>,
    ) {
        @Suppress("UNCHECKED_CAST")
        fun complete(value: Any) = continuation.resume(value as Result)

        fun fail(exception: Exception) = continuation.resumeWithException(exception)
    }

    private data class JobCounter(var value: Int = 0) {
        fun incrementAndGet(): Int {
            value += 1
            return value
        }
    }

    enum class WorkerType {
        VIPS,
        CANVAS
    }
}

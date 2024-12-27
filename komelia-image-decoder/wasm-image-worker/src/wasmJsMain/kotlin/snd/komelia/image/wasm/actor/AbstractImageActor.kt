package snd.komelia.image.wasm.actor

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.w3c.dom.DedicatedWorkerGlobalScope
import snd.komelia.image.wasm.messages.CloseImageRequest
import snd.komelia.image.wasm.messages.DecodeAndResizeRequest
import snd.komelia.image.wasm.messages.DecodeRequest
import snd.komelia.image.wasm.messages.ExtractAreaRequest
import snd.komelia.image.wasm.messages.FindTrimRequest
import snd.komelia.image.wasm.messages.GetBytesRequest
import snd.komelia.image.wasm.messages.MakeHistogramRequest
import snd.komelia.image.wasm.messages.MapLookupTableRequest
import snd.komelia.image.wasm.messages.ResizeRequest
import snd.komelia.image.wasm.messages.ShrinkRequest
import snd.komelia.image.wasm.messages.WorkerMessage
import snd.komelia.image.wasm.messages.WorkerMessageType
import snd.komelia.image.wasm.messages.WorkerMessageType.CLOSE
import snd.komelia.image.wasm.messages.WorkerMessageType.DECODE
import snd.komelia.image.wasm.messages.WorkerMessageType.DECODE_AND_RESIZE
import snd.komelia.image.wasm.messages.WorkerMessageType.ERROR
import snd.komelia.image.wasm.messages.WorkerMessageType.EXTRACT_AREA
import snd.komelia.image.wasm.messages.WorkerMessageType.FIND_TRIM
import snd.komelia.image.wasm.messages.WorkerMessageType.GET_BYTES
import snd.komelia.image.wasm.messages.WorkerMessageType.IMAGE
import snd.komelia.image.wasm.messages.WorkerMessageType.INIT
import snd.komelia.image.wasm.messages.WorkerMessageType.MAKE_HISTOGRAM
import snd.komelia.image.wasm.messages.WorkerMessageType.MAP_LOOKUP_TABLE
import snd.komelia.image.wasm.messages.WorkerMessageType.RESIZE
import snd.komelia.image.wasm.messages.WorkerMessageType.SHRINK
import snd.komelia.image.wasm.messages.errorResponse

private val logger = KotlinLogging.logger("ImageWorkerActor")

internal abstract class AbstractImageActor<T>(
    protected val workerScope: DedicatedWorkerGlobalScope
) {
    protected var imageCounter = 0
    protected val images = mutableMapOf<Int, T>()
    protected val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    fun launch() {
        coroutineScope.launch {
            beforeInit()
            workerScope.onmessage = { messageEvent ->
                coroutineScope.launch {
                    val message = messageEvent.data as WorkerMessage
                    try {
                        val type = WorkerMessageType.valueOf(message.type)
                        when (type) {
                            INIT -> workerScope.postMessage(message)
                            DECODE_AND_RESIZE -> decodeAndResize(message as DecodeAndResizeRequest)
                            DECODE -> decode(message as DecodeRequest)
                            RESIZE -> resize(message as ResizeRequest)
                            CLOSE -> closeImage(message as CloseImageRequest)
                            EXTRACT_AREA -> extractArea(message as ExtractAreaRequest)
                            GET_BYTES -> getBytes(message as GetBytesRequest)
                            SHRINK -> shrink(message as ShrinkRequest)
                            FIND_TRIM -> findTrim(message as FindTrimRequest)
                            MAKE_HISTOGRAM -> makeHistogram(message as MakeHistogramRequest)
                            MAP_LOOKUP_TABLE -> mapLookupTable(message as MapLookupTableRequest)
                            ERROR -> {}
                            IMAGE -> {}
                        }
                    } catch (e: Throwable) {
                        logger.catching(e)
                        val errorMessage = e.message ?: e.cause?.message ?: "${e::class.simpleName}"
                        workerScope.postMessage(errorResponse(message.requestId, errorMessage))
                    }
                }
            }
        }

    }

    protected fun saveImage(image: T): Int {
        val id = imageCounter++
        images[id] = image
        return id
    }

    protected open suspend fun beforeInit() {}
    protected abstract suspend fun decodeAndResize(message: DecodeAndResizeRequest)
    protected abstract suspend fun decode(message: DecodeRequest)
    protected abstract suspend fun resize(message: ResizeRequest)
    protected abstract suspend fun extractArea(message: ExtractAreaRequest)
    protected abstract suspend fun shrink(message: ShrinkRequest)
    protected abstract suspend fun findTrim(message: FindTrimRequest)
    protected abstract suspend fun makeHistogram(message: MakeHistogramRequest)
    protected abstract suspend fun mapLookupTable(message: MapLookupTableRequest)
    protected abstract suspend fun getBytes(message: GetBytesRequest)
    protected abstract suspend fun closeImage(message: CloseImageRequest)
}
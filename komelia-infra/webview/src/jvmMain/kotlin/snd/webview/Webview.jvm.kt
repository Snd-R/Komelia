package snd.webview

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import snd.jni.Managed
import snd.jni.NativePointer
import snd.webview.WebviewCallback.CallbackResponse
import java.awt.Component
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

val logger = KotlinLogging.logger {}

actual class KomeliaWebview private constructor(
    ptr: NativePointer,
) : AutoCloseable, Managed(ptr, WebViewFinalizer(ptr)) {
    val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @Volatile
    var isRunning = false

    private class WebViewFinalizer(private var ptr: Long) : Runnable {
        override fun run() = destroy(ptr)
    }

    actual fun start() {
        if (isRunning) return

        val future = CompletableFuture<Unit>()
        mainLoopExecutor.execute {
            runMainLoop {
                future.complete(Unit)
                isRunning = true
            }
        }
        future.get()
    }

    actual suspend inline fun <reified JsArgs, reified Result> bind(
        name: String,
        function: JsCallback<JsArgs, Result>
    ) {
        bind(name) { id, jsRequest ->
            coroutineScope.launch {
                runCatching {
                    // TODO avoid reflection?
                    val argsClass = typeOf<JsArgs>().classifier as KClass<*>
                    val resultClass = typeOf<Result>().classifier as KClass<*>

                    val arguments = json.decodeFromString<List<JsArgs>>(jsRequest)
                    val argument = if (argsClass == Unit::class) {
                        Unit as JsArgs
                    } else {
                        arguments[0]
                    }

                    val result = function.run(argument)
                    if (isClosed) return@launch

                    if (resultClass == Unit::class) {
                        bindReturn(id, json.encodeToString(CallbackResponse("")))
                    } else {
                        val json = json.encodeToString<CallbackResponse<Result>>(CallbackResponse(result))
                        bindReturn(id, json)
                    }
                }.onFailure { error ->
                    logger.error(error) { "Encountered error during execution of bind function \"$name\"; js params: $jsRequest" }
                    if (!isClosed) {
                        val message = json.encodeToString<CallbackResponse<String>>(
                            CallbackResponse(error.message ?: error.stackTraceToString())
                        )
                        bindReject(id, message)
                    }
                }
            }
        }
    }

    actual fun registerRequestInterceptor(handler: RequestInterceptor) {
        setRequestInterceptor { uri ->
            coroutineScope.async {
                handler.run(
                    ResourceRequest(
                        url = Url(uri),
                        method = HttpMethod.Get,
                        requestHeaders = Headers.Empty
                    )
                )
            }.asCompletableFuture()
        }
    }

    private external fun setRequestInterceptor(handler: RequestInterceptorCallback)

    actual external fun navigate(uri: String)

    external fun updateSize(width: Int, height: Int)

    external fun bind(name: String, callback: WebviewCallback)

    external fun bindReturn(id: String, result: String)

    external fun bindReject(id: String, message: String)

    private external fun runMainLoop(startCallback: Runnable)

    companion object {
        val mainLoopExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

        fun webview(window: Component, onLoad: (KomeliaWebview) -> Unit) {
            mainLoopExecutor.submit(Callable {
                val ptr = create(window)
                onLoad(KomeliaWebview(ptr))
            })
        }

        @JvmStatic
        private external fun create(window: Component): NativePointer

        @JvmStatic
        private external fun destroy(ptr: Long)
    }
}

fun interface WebviewCallback {
    fun run(id: String, request: String)

    @Serializable
    data class CallbackResponse<T>(val result: T)
}

fun interface RequestInterceptorCallback {
    fun run(uri: String): Future<ResourceLoadResult?>
}

actual fun webviewIsAvailable() = WebviewSharedLibraries.isAvailable
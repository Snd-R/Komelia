package snd.webview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import snd.jni.Managed
import snd.jni.NativePointer
import snd.webview.WebviewCallback.CallbackResponse
import java.awt.Component
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

actual class Webview private constructor(
    ptr: NativePointer,
) : AutoCloseable, Managed(ptr, WebViewFinalizer(ptr)) {
    val functionCallScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        val future = CompletableFuture<Unit>()
        mainLoopExecutor.execute {
            runMainLoop { future.complete(Unit) }
        }
        future.get()
    }

    private class WebViewFinalizer(private var ptr: Long) : Runnable {
        override fun run() = destroy(ptr)
    }

    actual inline fun <reified JsArgs, reified Result> bind(name: String, function: JsCallback<JsArgs, Result>) {
        bind(name) { id, jsRequest ->
            functionCallScope.launch {

                runCatching {
                    // TODO avoid reflection?
                    val argsClass = typeOf<JsArgs>().classifier as KClass<*>
                    val resultClass = typeOf<Result>().classifier as KClass<*>

                    val arguments = Json.decodeFromString<List<JsArgs>>(jsRequest)
                    val argument = if (argsClass == Unit::class) {
                        Unit as JsArgs
                    } else {
                        arguments[0]
                    }

                    val result = function.run(argument)
                    if (isClosed) return@launch

                    if (resultClass == Unit::class) {
                        bindReturn(id, Json.encodeToString(CallbackResponse("")))
                    } else {
                        val json = Json.encodeToString<CallbackResponse<Result>>(CallbackResponse(result))
                        bindReturn(id, json)
                    }
                }.onFailure { error ->
                    error.printStackTrace()
                    if (!isClosed) {
                        val message = Json.encodeToString<CallbackResponse<String>>(
                            CallbackResponse(error.message ?: error.stackTraceToString())
                        )
                        bindReject(id, message)
                    }
                }
            }
        }
    }

    actual external fun loadUri(uri: String)

    actual external fun registerResourceLoadHandler(scheme: String, handler: ResourceLoadHandler)

    external fun updateSize(width: Int, height: Int)

    external fun bind(name: String, callback: WebviewCallback)

    external fun bindReturn(id: String, result: String)

    external fun bindReject(id: String, message: String)

    external fun setParentWindow(component: Component)

    private external fun runMainLoop(startCallback: Runnable)

    companion object {
        val mainLoopExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

        fun webview(): Webview {
            val ptr = mainLoopExecutor.submit(Callable { create() }).get()
            return Webview(ptr)
        }

        @JvmStatic
        private external fun create(): NativePointer

        @JvmStatic
        private external fun destroy(ptr: Long)
    }
}

fun interface WebviewCallback {
    fun run(id: String, request: String)

    @Serializable
    data class CallbackResponse<T>(
        val result: T
    )
}

actual fun createWebview() = Webview.webview()
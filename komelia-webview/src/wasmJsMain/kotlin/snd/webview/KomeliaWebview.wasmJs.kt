package snd.webview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

actual class KomeliaWebview : AutoCloseable {
    val bindFunctions = mutableMapOf<String, JsCallback<*, *>>()
    private var currentUrl: String? = null
    private var isActive = false
    private var requestInterceptor: RequestInterceptor? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    actual suspend inline fun <reified JsArgs, reified Result> bind(
        name: String,
        function: JsCallback<JsArgs, Result>
    ) {
        bindFunctions[name] = function
    }

    actual fun start() {
        isActive = true
    }

    actual fun navigate(uri: String) {
    }

    actual fun registerRequestInterceptor(handler: RequestInterceptor) {
    }

    override fun close() {
    }

}

actual fun webviewIsAvailable() = false
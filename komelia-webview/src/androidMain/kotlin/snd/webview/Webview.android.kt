package snd.webview

actual class Webview : AutoCloseable {
    actual inline fun <reified JsArgs, reified Result> bind(
        name: String,
        function: JsCallback<JsArgs, Result>
    ) {
    }

    actual fun start() {
    }

    actual fun navigate(uri: String) {
    }

    actual fun registerRequestInterceptor(handler: ResourceLoadHandler) {
    }

    override fun close() {
    }
}

actual fun webviewIsAvailable() = false
package snd.webview


expect class KomeliaWebview : AutoCloseable {
    // TODO better bind API
    suspend inline fun <reified JsArgs, reified Result> bind(name: String, function: JsCallback<JsArgs, Result>)

    fun start()
    fun navigate(uri: String)
    fun registerRequestInterceptor(handler: RequestInterceptor)

}

fun interface JsCallback<JsArgs, Result> {
    suspend fun run(request: JsArgs): Result
}

expect fun webviewIsAvailable(): Boolean
package snd.webview


expect class Webview : AutoCloseable {
    // TODO better bind API
    inline fun <reified JsArgs, reified Result> bind(name: String, function: JsCallback<JsArgs, Result>)

    fun start()
    fun navigate(uri: String)
    fun registerRequestInterceptor(handler: ResourceLoadHandler)

}

fun interface JsCallback<JsArgs, Result> {
    suspend fun run(request: JsArgs): Result
}

expect fun webviewIsAvailable(): Boolean
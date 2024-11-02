package snd.webview

fun interface ResourceLoadHandler {
    fun run(uri: String): ResourceLoadResult?
}

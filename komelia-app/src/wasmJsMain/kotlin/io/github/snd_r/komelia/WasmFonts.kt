package io.github.snd_r.komelia

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import io.github.snd_r.komelia.worker.util.asByteArray
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.fetch.Response
import kotlin.js.Promise

private const val notoSansSC =
    "https://fonts.gstatic.com/s/a/eacedb2999b6cd30457f3820f277842f0dfbb28152a246fca8161779a8945425.ttf"
private const val notoSansJP =
    "https://fonts.gstatic.com/s/a/209c70f533554d512ef0a417b70dfe2997aeec080d2fe41695c55b361643f9ba.ttf"
private const val notoColorEmoji =
    "https://fonts.gstatic.com/s/a/a98676580777f4f555d83287fb6e515f7450a7eae58466cbd1f4fed32ee03aaa.ttf"

suspend fun loadFonts(resolver: FontFamily.Resolver) {
    runCatching {
        resolver.preload(FontFamily(listOf(Font("Noto Sans SC", loadFontBytes(notoSansSC)))))
        resolver.preload(FontFamily(listOf(Font("Noto Sans JP", loadFontBytes(notoSansJP)))))
        resolver.preload(FontFamily(listOf(Font("Noto Color Emoji", loadFontBytes(notoColorEmoji)))))
    }.onFailure { it.printStackTrace() }
}

private suspend fun loadFontBytes(url: String): ByteArray {
    val arrayBuffer = loadRes(url).await<Response>().arrayBuffer().await<ArrayBuffer>()
    return Uint8Array(arrayBuffer, 0, arrayBuffer.byteLength).asByteArray()
}

private fun loadRes(url: String): Promise<Response> {
    js("return window.originalFetch(url);")
}

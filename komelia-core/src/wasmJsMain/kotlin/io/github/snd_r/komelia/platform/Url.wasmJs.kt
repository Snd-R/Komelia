package io.github.snd_r.komelia.platform

import io.ktor.http.*
import org.w3c.dom.url.URL

actual fun Url.resolve(childUrl: String): Url {
    val jsUrl = URL(childUrl, this.toString())
    return Url(jsUrl.toString())
}
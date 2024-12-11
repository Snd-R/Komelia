package io.github.snd_r.komelia.platform

import io.ktor.http.*
import java.net.URI

actual fun Url.resolve(childUrl: String): Url {
    val baseUri = this.toURI()
    val childUri = URI(childUrl)
    val relative = baseUri.resolve(childUri)

    return Url(relative)
}
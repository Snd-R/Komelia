package snd.komelia

import io.ktor.http.*
import java.net.URI

actual fun codepointsCount(string: String): Long {
    return string.codePoints().count()
}

actual fun Float.formatDecimal(numberOfDecimals: Int) = "%.${numberOfDecimals}f".format(this)

actual fun Double.formatDecimal(numberOfDecimals: Int) = "%.${numberOfDecimals}f".format(this)

actual fun Url.resolve(childUrl: String): Url {
    val baseUri = this.toURI()
    val childUri = URI(childUrl)
    val relative = baseUri.resolve(childUri)

    return Url(relative)
}

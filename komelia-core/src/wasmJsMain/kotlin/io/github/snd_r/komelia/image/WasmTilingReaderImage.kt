package io.github.snd_r.komelia.image

import org.jetbrains.skia.Image

actual typealias RenderImage = Image

actual class PlatformImage {
    actual val width: Int
        get() = TODO("Not yet implemented")
    actual val height: Int
        get() = TODO("Not yet implemented")

    actual fun close() {
    }
}

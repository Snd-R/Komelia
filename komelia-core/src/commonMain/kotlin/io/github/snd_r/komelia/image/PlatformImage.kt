package io.github.snd_r.komelia.image

expect class PlatformImage {
    val width: Int
    val height: Int
    fun close()
}

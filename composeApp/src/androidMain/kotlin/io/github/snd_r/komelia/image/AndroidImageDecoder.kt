package io.github.snd_r.komelia.image

import kotlin.io.path.readBytes

class AndroidImageDecoder : ImageDecoder {
    override fun decode(bytes: ByteArray): ReaderImage {
        return AndroidTilingReaderImage(encoded = bytes)
    }

    override fun decode(cacheFile: okio.Path): ReaderImage {
        val bytes = cacheFile.toNioPath().readBytes()
        return AndroidTilingReaderImage(encoded = bytes)
    }
}


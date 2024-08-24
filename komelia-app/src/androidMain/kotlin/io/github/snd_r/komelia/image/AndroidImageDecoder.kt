package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import kotlin.io.path.readBytes

class AndroidImageDecoder : ImageDecoder {
    override suspend fun decode(bytes: ByteArray, pageId: PageId): ReaderImage {
        return AndroidTilingReaderImage(bytes, pageId)
    }

    override suspend fun decode(cacheFile: okio.Path, pageId: PageId): ReaderImage {
        val bytes = cacheFile.toNioPath().readBytes()
        return AndroidTilingReaderImage(bytes, pageId)
    }
}


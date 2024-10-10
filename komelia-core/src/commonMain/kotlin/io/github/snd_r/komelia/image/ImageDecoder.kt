package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import okio.Path

interface ImageDecoder {
    suspend fun decode(bytes: ByteArray, pageId: PageId): ReaderImage
    suspend fun decode(cacheFile: Path, pageId: PageId): ReaderImage
}

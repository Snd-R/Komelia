package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import okio.Path

interface ReaderImageFactory {
    suspend fun getImage(bytes: ByteArray, pageId: PageId): ReaderImage
    suspend fun getImage(cacheFile: Path, pageId: PageId): ReaderImage
}

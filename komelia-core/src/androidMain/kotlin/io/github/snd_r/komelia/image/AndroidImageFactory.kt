package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.ImageDecoder
import kotlin.io.path.readBytes

class AndroidImageFactory(
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
    private val decoder: ImageDecoder
) : ReaderImageFactory {
    override suspend fun getImage(bytes: ByteArray, pageId: PageId): ReaderImage {
        return AndroidTilingReaderImage(
            encoded = bytes,
            processingPipeline = processingPipeline,
            stretchImages = stretchImages,
            decoder = decoder,
            pageId = pageId
        )
    }

    override suspend fun getImage(cacheFile: okio.Path, pageId: PageId): ReaderImage {
        val bytes = cacheFile.toNioPath().readBytes()
        return AndroidTilingReaderImage(
            encoded = bytes,
            processingPipeline = processingPipeline,
            stretchImages = stretchImages,
            decoder = decoder,
            pageId = pageId
        )
    }
}


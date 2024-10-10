package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import kotlinx.coroutines.flow.StateFlow
import kotlin.io.path.readBytes

class AndroidImageDecoder(
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
) : ImageDecoder {
    override suspend fun decode(bytes: ByteArray, pageId: PageId): ReaderImage {
        return AndroidTilingReaderImage(
            encoded = bytes,
            processingPipeline = processingPipeline,
            stretchImages = stretchImages,
            pageId = pageId
        )
    }

    override suspend fun decode(cacheFile: okio.Path, pageId: PageId): ReaderImage {
        val bytes = cacheFile.toNioPath().readBytes()
        return AndroidTilingReaderImage(
            encoded = bytes,
            processingPipeline = processingPipeline,
            stretchImages = stretchImages,
            pageId = pageId
        )
    }
}


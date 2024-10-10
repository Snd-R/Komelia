package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.worker.ImageWorker
import kotlinx.coroutines.flow.StateFlow
import okio.Path

class WasmImageDecoder(
    private val imageWorker: ImageWorker,
    private val stretchImages: StateFlow<Boolean>,
) : ImageDecoder {
    override suspend fun decode(bytes: ByteArray, pageId: ReaderImage.PageId): ReaderImage {
        val dimensions = imageWorker.getDimensions(bytes)
        return WasmSimpleReaderImage(
            encoded = bytes,
            width = dimensions.width,
            height = dimensions.height,
            worker = imageWorker,
            stretchImages = stretchImages,
            pageId = pageId,
        )
    }

    override suspend fun decode(cacheFile: Path, pageId: ReaderImage.PageId): ReaderImage {
        TODO("Not yet implemented")
    }
}
package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.worker.ImageWorker
import okio.Path

class WasmImageDecoder(private val imageWorker: ImageWorker) : ImageDecoder {
    override suspend fun decode(bytes: ByteArray, pageId: ReaderImage.PageId): ReaderImage {
        val dimensions = imageWorker.getDimensions(bytes)
        return WasmSimpleReaderImage(
            encoded = bytes,
            width = dimensions.width,
            height = dimensions.height,
            pageId = pageId,
            worker = imageWorker
        )
    }

    override suspend fun decode(cacheFile: Path, pageId: ReaderImage.PageId): ReaderImage {
        TODO("Not yet implemented")
    }
}
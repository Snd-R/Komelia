package io.github.snd_r.komelia.image

import okio.Path

class WasmImageDecoder : ImageDecoder {
    override fun decode(bytes: ByteArray, pageId: ReaderImage.PageId): ReaderImage {
        TODO("Not yet implemented")
    }

    override fun decode(cacheFile: Path, pageId: ReaderImage.PageId): ReaderImage {
        TODO("Not yet implemented")
    }
}
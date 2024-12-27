package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.platform.UpscaleOption
import kotlinx.coroutines.flow.StateFlow
import okio.Path
import snd.komelia.image.wasm.client.WorkerImageDecoder

class WasmReaderImageFactory(
    private val upscaleOptionFlow: StateFlow<UpscaleOption>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
    private val showDebugGrid: StateFlow<Boolean>,
    private val decoder: WorkerImageDecoder
) : ReaderImageFactory {
    override suspend fun getImage(bytes: ByteArray, pageId: ReaderImage.PageId): ReaderImage {
        return WasmTilingReaderImage(
            encoded = bytes,
            processingPipeline = processingPipeline,
            upscaleOption = upscaleOptionFlow,
            stretchImages = stretchImages,
            decoder = decoder,
            pageId = pageId,
            showDebugGrid = showDebugGrid
        )
    }

    override suspend fun getImage(cacheFile: Path, pageId: ReaderImage.PageId): ReaderImage {
        TODO("Not yet implemented")
    }
}
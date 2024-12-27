package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.platform.UpscaleOption
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.ImageDecoder
import kotlin.io.path.readBytes

class DesktopReaderImageFactory(
    private val upscaleOptionFlow: StateFlow<UpscaleOption>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
    private val onnxUpscaler: ManagedOnnxUpscaler?,
    private val showDebugGrid: StateFlow<Boolean>,
    private val decoder: ImageDecoder
) : ReaderImageFactory {

    override suspend fun getImage(bytes: ByteArray, pageId: PageId): ReaderImage {
        return DesktopTilingReaderImage(
            encoded = bytes,
            processingPipeline = processingPipeline,
            pageId = pageId,
            upscaleOption = upscaleOptionFlow,
            stretchImages = stretchImages,
            upscaler = onnxUpscaler,
            showDebugGrid = showDebugGrid,
            decoder = decoder,
        )
    }

    override suspend fun getImage(cacheFile: okio.Path, pageId: PageId): ReaderImage {
        val bytes = cacheFile.toNioPath().readBytes()
        return DesktopTilingReaderImage(
            encoded = bytes,
            processingPipeline = processingPipeline,
            pageId = pageId,
            upscaleOption = upscaleOptionFlow,
            stretchImages = stretchImages,
            upscaler = onnxUpscaler,
            showDebugGrid = showDebugGrid,
            decoder = decoder,
        )
    }
}

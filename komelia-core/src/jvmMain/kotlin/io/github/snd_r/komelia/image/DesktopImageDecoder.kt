package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.platform.UpscaleOption
import kotlinx.coroutines.flow.StateFlow
import kotlin.io.path.readBytes

class DesktopImageDecoder(
    private val upscaleOptionFlow: StateFlow<UpscaleOption>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
    private val onnxUpscaler: ManagedOnnxUpscaler?,
    private val showDebugGrid: StateFlow<Boolean>,
) : ImageDecoder {

    override suspend fun decode(bytes: ByteArray, pageId: PageId): ReaderImage {
        return DesktopTilingReaderImage(
            encoded = bytes,
            processingPipeline = processingPipeline,
            pageId = pageId,
            upscaleOption = upscaleOptionFlow,
            stretchImages = stretchImages,
            upscaler = onnxUpscaler,
            showDebugGrid = showDebugGrid,
        )
    }

    override suspend fun decode(cacheFile: okio.Path, pageId: PageId): ReaderImage {
        val bytes = cacheFile.toNioPath().readBytes()
        return DesktopTilingReaderImage(
            encoded = bytes,
            processingPipeline = processingPipeline,
            pageId = pageId,
            upscaleOption = upscaleOptionFlow,
            stretchImages = stretchImages,
            upscaler = onnxUpscaler,
            showDebugGrid = showDebugGrid,
        )
    }
}

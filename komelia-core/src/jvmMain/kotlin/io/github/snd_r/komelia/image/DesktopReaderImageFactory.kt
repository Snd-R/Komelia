package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import io.github.snd_r.komelia.platform.UpscaleOption
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.KomeliaImage

class DesktopReaderImageFactory(
    private val upscaleOptionFlow: StateFlow<UpscaleOption>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
    private val onnxUpscaler: ManagedOnnxUpscaler?,
    private val showDebugGrid: StateFlow<Boolean>,
) : ReaderImageFactory {
    override suspend fun getImage(image: KomeliaImage, pageId: PageId): ReaderImage {
        return DesktopTilingReaderImage(
            originalImage = image,
            processingPipeline = processingPipeline,
            pageId = pageId,
            upscaleOption = upscaleOptionFlow,
            stretchImages = stretchImages,
            upscaler = onnxUpscaler,
            showDebugGrid = showDebugGrid,
        )
    }
}

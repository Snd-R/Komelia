package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import io.github.snd_r.komelia.platform.UpscaleOption
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.KomeliaImage

class WasmReaderImageFactory(
    private val upscaleOptionFlow: StateFlow<UpscaleOption>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
    private val showDebugGrid: StateFlow<Boolean>,
) : ReaderImageFactory {
    override suspend fun getImage(image: KomeliaImage, pageId: ReaderImage.PageId): ReaderImage {
        return WasmTilingReaderImage(
            originalImage = image,
            processingPipeline = processingPipeline,
            upscaleOption = upscaleOptionFlow,
            stretchImages = stretchImages,
            pageId = pageId,
            showDebugGrid = showDebugGrid
        )

    }
}
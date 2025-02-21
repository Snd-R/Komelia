package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.KomeliaImage
import snd.komelia.image.ReduceKernel

class DesktopReaderImageFactory(
    private val downSamplingKernel: StateFlow<ReduceKernel>,
    private val upsamplingMode: StateFlow<UpsamplingMode>,
    private val linearLightDownSampling: StateFlow<Boolean>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
    private val onnxUpscaler: ManagedOnnxUpscaler?,
) : ReaderImageFactory {
    override suspend fun getImage(image: KomeliaImage, pageId: PageId): ReaderImage {
        return DesktopTilingReaderImage(
            originalImage = image,
            processingPipeline = processingPipeline,
            stretchImages = stretchImages,
            upsamplingMode = upsamplingMode,
            downSamplingKernel = downSamplingKernel,
            linearLightDownSampling = linearLightDownSampling,
            pageId = pageId,
            upscaler = onnxUpscaler,
            showDebugGrid = MutableStateFlow(false),
        )
    }
}

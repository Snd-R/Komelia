package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.ImageDecoder
import snd.komelia.image.ReduceKernel

class DesktopReaderImageFactory(
    private val imageDecoder: ImageDecoder,
    private val downSamplingKernel: StateFlow<ReduceKernel>,
    private val upsamplingMode: StateFlow<UpsamplingMode>,
    private val linearLightDownSampling: StateFlow<Boolean>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
    private val onnxUpscaler: DesktopOnnxRuntimeUpscaler?,
) : ReaderImageFactory {

    override suspend fun getImage(imageSource: ImageSource, pageId: PageId): ReaderImage {
        return DesktopReaderImage(
            imageDecoder = imageDecoder,
            imageSource = imageSource,
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

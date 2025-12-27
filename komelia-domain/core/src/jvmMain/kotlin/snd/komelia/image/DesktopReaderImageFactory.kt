package snd.komelia.image

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.ReaderImage.PageId
import snd.komelia.image.processing.ImageProcessingPipeline

class DesktopReaderImageFactory(
    private val imageDecoder: KomeliaImageDecoder,
    private val downSamplingKernel: StateFlow<ReduceKernel>,
    private val upsamplingMode: StateFlow<UpsamplingMode>,
    private val linearLightDownSampling: StateFlow<Boolean>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
    private val onnxUpscaler: KomeliaUpscaler?,
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

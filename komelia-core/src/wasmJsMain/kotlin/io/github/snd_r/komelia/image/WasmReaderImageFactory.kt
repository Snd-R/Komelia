package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.ImageDecoder
import snd.komelia.image.ReduceKernel

class WasmReaderImageFactory(
    private val imageDecoder: ImageDecoder,
    private val downSamplingKernel: StateFlow<ReduceKernel>,
    private val upsamplingMode: StateFlow<UpsamplingMode>,
    private val linearLightDownSampling: StateFlow<Boolean>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
) : ReaderImageFactory {

    override suspend fun getImage(imageSource: ImageSource, pageId: PageId): ReaderImage {
        return WasmReaderImage(
            imageDecoder = imageDecoder,
            imageSource = imageSource,
            processingPipeline = processingPipeline,
            stretchImages = stretchImages,
            upsamplingMode = upsamplingMode,
            downSamplingKernel = downSamplingKernel,
            linearLightDownSampling = linearLightDownSampling,
            pageId = pageId,
            showDebugGrid = MutableStateFlow(false)
        )

    }
}
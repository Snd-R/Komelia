package snd.komelia.image

import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.ReaderImage.PageId
import snd.komelia.image.processing.ImageProcessingPipeline

class AndroidReaderImageFactory(
    private val imageDecoder: KomeliaImageDecoder,
    private val upsamplingMode: StateFlow<UpsamplingMode>,
    private val downSamplingKernel: StateFlow<ReduceKernel>,
    private val linearLightDownSampling: StateFlow<Boolean>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
) : ReaderImageFactory {

    override suspend fun getImage(
        imageSource: ImageSource,
        pageId: PageId
    ): ReaderImage {
        return AndroidReaderImage(
            imageDecoder = imageDecoder,
            imageSource = imageSource,
            processingPipeline = processingPipeline,
            stretchImages = stretchImages,
            upsamplingMode = upsamplingMode,
            downSamplingKernel = downSamplingKernel,
            linearLightDownSampling = linearLightDownSampling,
            pageId = pageId,
        )
    }
}


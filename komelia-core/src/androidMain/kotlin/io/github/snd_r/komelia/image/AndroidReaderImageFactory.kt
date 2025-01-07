package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.KomeliaImage

class AndroidReaderImageFactory(
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
) : ReaderImageFactory {
    override suspend fun getImage(image: KomeliaImage, pageId: PageId): ReaderImage {
        return AndroidTilingReaderImage(
            originalImage = image,
            processingPipeline = processingPipeline,
            pageId = pageId,
            stretchImages = stretchImages,
        )
    }
}


package io.github.snd_r.komelia.image

import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.skia.Image
import snd.komelia.image.ImageRect
import snd.komelia.image.KomeliaImage
import snd.komelia.image.ReduceKernel

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias RenderImage = Image

class WasmTilingReaderImage(
    originalImage: KomeliaImage,
    processingPipeline: ImageProcessingPipeline,
    stretchImages: StateFlow<Boolean>,
    upsamplingMode: StateFlow<UpsamplingMode>,
    downSamplingKernel: StateFlow<ReduceKernel>,
    linearLightDownSampling: StateFlow<Boolean>,
    pageId: PageId,
    private val showDebugGrid: StateFlow<Boolean>,
) : TilingReaderImage(
    originalImage = originalImage,
    processingPipeline = processingPipeline,
    stretchImages = stretchImages,
    upsamplingMode = upsamplingMode,
    downSamplingKernel = downSamplingKernel,
    linearLightDownSampling = linearLightDownSampling,
    pageId = pageId,
) {

    override fun closeTileBitmaps(tiles: List<ReaderImageTile>) {
        tiles.forEach { it.renderImage?.close() }
    }

    override fun createTilePainter(
        tiles: List<ReaderImageTile>,
        displaySize: IntSize,
        scaleFactor: Double
    ): TiledPainter {
        return SkiaTiledPainter(
            tiles = tiles,
            showDebugGrid = showDebugGrid.value,
            upsamplingMode = upsamplingMode.value,
            scaleFactor = scaleFactor,
            displaySize = displaySize
        )
    }

    override suspend fun resizeImage(image: KomeliaImage, scaleWidth: Int, scaleHeight: Int): ReaderImageData {
        image.resize(scaleWidth, scaleHeight).use { resized ->
            return resized.toReaderImageData()
        }
    }

    override suspend fun getImageRegion(
        image: KomeliaImage,
        imageRegion: IntRect,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {
        var region: KomeliaImage? = null
        var resized: KomeliaImage? = null
        try {
            region = image.extractArea(imageRegion.toImageRect())
            if (scaleWidth > imageRegion.width || scaleHeight > imageRegion.height) {
                val regionData = region.toReaderImageData()
                return regionData
            }
            resized = region.resize(scaleWidth, scaleHeight)
            return resized.toReaderImageData()
        } finally {
            region?.close()
            resized?.close()
        }
    }

    private suspend fun KomeliaImage.toReaderImageData(): ReaderImageData {
        val bitmap = this.toBitmap()
        val image = Image.makeFromBitmap(bitmap)
        bitmap.close()
        return ReaderImageData(this.width, this.height, image)
    }

    private fun IntRect.toImageRect() = ImageRect(left = left, top = top, right = right, bottom = bottom)
}

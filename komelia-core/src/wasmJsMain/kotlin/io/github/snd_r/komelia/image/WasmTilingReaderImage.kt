package io.github.snd_r.komelia.image

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.platform.skiaSamplerCatmullRom
import io.github.snd_r.komelia.platform.skiaSamplerMitchell
import io.github.snd_r.komelia.platform.skiaSamplerNearest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.skia.Image
import org.jetbrains.skia.SamplingMode
import snd.komelia.image.ImageDecoder
import snd.komelia.image.ImageRect
import snd.komelia.image.KomeliaImage

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias RenderImage = Image

class WasmTilingReaderImage(
    encoded: ByteArray,
    processingPipeline: ImageProcessingPipeline,
    upscaleOption: StateFlow<UpscaleOption>,
    stretchImages: StateFlow<Boolean>,
    decoder: ImageDecoder,
    pageId: PageId,
    private val showDebugGrid: StateFlow<Boolean>,
) : TilingReaderImage(
    encoded,
    processingPipeline,
    stretchImages,
    decoder,
    pageId,
) {
    private var upsamplingMode: SamplingMode = when (upscaleOption.value) {
        skiaSamplerMitchell -> SamplingMode.MITCHELL
        skiaSamplerCatmullRom -> SamplingMode.CATMULL_ROM
        skiaSamplerNearest -> SamplingMode.DEFAULT
        else -> SamplingMode.MITCHELL
    }

    init {
        upscaleOption.drop(1).onEach { option ->
            val samplingMode = when (option) {
                skiaSamplerMitchell -> SamplingMode.MITCHELL
                skiaSamplerCatmullRom -> SamplingMode.CATMULL_ROM
                skiaSamplerNearest -> SamplingMode.DEFAULT
                else -> SamplingMode.MITCHELL
            }
            this.upsamplingMode = samplingMode
        }.launchIn(processingScope)
    }


    override fun closeTileBitmaps(tiles: List<ReaderImageTile>) {
        tiles.forEach { it.renderImage?.close() }
    }

    override fun createTilePainter(
        tiles: List<ReaderImageTile>,
        displaySize: IntSize,
        scaleFactor: Double
    ): Painter {
        return TiledImagePainter(
            tiles = tiles,
            showDebugGrid = showDebugGrid.value,
            samplingMode = upsamplingMode,
            scaleFactor = scaleFactor,
            displaySize = displaySize
        )
    }

    override fun createPlaceholderPainter(displaySize: IntSize): Painter {
        return PlaceholderPainter(displaySize)
    }

    override suspend fun resizeImage(image: KomeliaImage, scaleWidth: Int, scaleHeight: Int): ReaderImageData {
        image.resize(scaleWidth, scaleHeight, false).use { resized ->
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
            resized = region.resize(scaleWidth, scaleHeight, false)
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

package io.github.snd_r.komelia.image

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.worker.ImageWorker
import org.jetbrains.skia.Image

actual typealias RenderImage = Image
actual typealias PlatformImage = Long
typealias VipsImageId = PlatformImage

class WasmTilingReaderImage(
    encoded: ByteArray,
    pageId: PageId,
    imageWorker: ImageWorker,
) : TilingReaderImage(encoded, pageId) {

    override fun getDimensions(encoded: ByteArray): IntSize {
        TODO("Not yet implemented")
    }

    override fun decode(encoded: ByteArray): PlatformImage {
        TODO("Not yet implemented")
    }

    override fun closeTileBitmaps(tiles: List<ReaderImageTile>) {
        TODO("Not yet implemented")
    }

    override fun createTilePainter(tiles: List<ReaderImageTile>, displaySize: IntSize, scaleFactor: Double): Painter {
        TODO("Not yet implemented")
    }

    override fun createPlaceholderPainter(displaySize: IntSize): Painter {
        TODO("Not yet implemented")
    }

    override suspend fun resizeImage(image: PlatformImage, scaleWidth: Int, scaleHeight: Int): ReaderImageData {
        TODO("Not yet implemented")
    }

    override suspend fun getImageRegion(
        image: PlatformImage,
        imageRegion: IntRect,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {
        TODO("Not yet implemented")
    }

    override fun closeImage(image: PlatformImage) {
        TODO("Not yet implemented")
    }

}

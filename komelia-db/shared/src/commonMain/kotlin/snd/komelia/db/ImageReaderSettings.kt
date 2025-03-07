package snd.komelia.db

import io.github.snd_r.komelia.image.UpsamplingMode
import io.github.snd_r.komelia.ui.reader.image.ReaderFlashColor
import io.github.snd_r.komelia.ui.reader.image.ReaderType
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType.SCREEN
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout.SINGLE_PAGE
import kotlinx.serialization.Serializable
import snd.komelia.image.OnnxRuntimeUpscaleMode
import snd.komelia.image.ReduceKernel

@Serializable
data class ImageReaderSettings(
    val readerType: ReaderType = ReaderType.PAGED,
    val stretchToFit: Boolean = true,
    val pagedScaleType: LayoutScaleType = SCREEN,
    val pagedReadingDirection: PagedReaderState.ReadingDirection = PagedReaderState.ReadingDirection.LEFT_TO_RIGHT,
    val pagedPageLayout: PageDisplayLayout = SINGLE_PAGE,
    val continuousReadingDirection: ContinuousReaderState.ReadingDirection = ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM,
    val continuousPadding: Float = 0f,
    val continuousPageSpacing: Int = 0,
    val cropBorders: Boolean = false,

    val flashOnPageChange: Boolean = false,
    val flashDuration: Long = 100L,
    val flashEveryNPages: Int = 1,
    val flashWith: ReaderFlashColor = ReaderFlashColor.BLACK,
    val downsamplingKernel: ReduceKernel = ReduceKernel.LANCZOS3,
    val linearLightDownsampling: Boolean = false,
    val upsamplingMode: UpsamplingMode = UpsamplingMode.CATMULL_ROM,
    val loadThumbnailPreviews: Boolean = true,
    val volumeKeysNavigation: Boolean = false,

    val onnxRuntimeMode: OnnxRuntimeUpscaleMode = OnnxRuntimeUpscaleMode.NONE,
    val onnxRuntimeModelPath: String? = null,
    val onnxRuntimeDeviceId: Int = 0,
    val onnxRuntimeTileSize: Int = 512,
)
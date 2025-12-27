package snd.komelia.db

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.serialization.Serializable
import snd.komelia.image.ReduceKernel
import snd.komelia.image.UpsamplingMode
import snd.komelia.image.UpscaleMode
import snd.komelia.settings.model.ContinuousReadingDirection
import snd.komelia.settings.model.LayoutScaleType
import snd.komelia.settings.model.PageDisplayLayout
import snd.komelia.settings.model.PagedReadingDirection
import snd.komelia.settings.model.ReaderFlashColor
import snd.komelia.settings.model.ReaderType
import snd.komelia.settings.model.ReaderType.PAGED

@Serializable
data class ImageReaderSettings(
    val readerType: ReaderType = PAGED,
    val stretchToFit: Boolean = true,
    val pagedScaleType: LayoutScaleType = LayoutScaleType.SCREEN,
    val pagedReadingDirection: PagedReadingDirection = PagedReadingDirection.LEFT_TO_RIGHT,
    val pagedPageLayout: PageDisplayLayout = PageDisplayLayout.SINGLE_PAGE,
    val continuousReadingDirection: ContinuousReadingDirection = ContinuousReadingDirection.TOP_TO_BOTTOM,
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

    val ortUpscalerMode: UpscaleMode = UpscaleMode.NONE,
    val ortUpscalerUserModelPath: PlatformFile? = null,
    val ortUpscalerDeviceId: Int = 0,
    val ortUpscalerTileSize: Int = 512,
)
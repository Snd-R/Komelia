package snd.komelia.db.settings

import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType.SCREEN
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.datetime.Instant

data class AppSettings(
    val username: String = "admin@example.org",
    val serverUrl: String = "http://localhost:25600",

    val cardWidth: Int = 170,
    val seriesPageLoadSize: Int = 20,
    val bookPageLoadSize: Int = 20,
    val bookListLayout: BooksLayout = BooksLayout.GRID,
    val appTheme: AppTheme = AppTheme.DARK,

    val checkForUpdatesOnStartup: Boolean = true,
    val updateLastCheckedTimestamp: Instant? = null,
    val updateLastCheckedReleaseVersion: AppVersion? = null,
    val updateDismissedVersion: AppVersion? = null,

    val upscaleOption: String,
    val downscaleOption: String,

    val onnxModelsPath: String = "/",
    val onnxRuntimeDeviceId: Int = 0,
    val onnxRuntimeTileSize: Int = 512,

    val readerType: ReaderType = ReaderType.PAGED,
    val stretchToFit: Boolean = true,
    val pagedScaleType: LayoutScaleType = SCREEN,
    val pagedReadingDirection: PagedReaderState.ReadingDirection = PagedReaderState.ReadingDirection.LEFT_TO_RIGHT,
    val pagedPageLayout: PageDisplayLayout = SINGLE_PAGE,
    val continuousReadingDirection: ContinuousReaderState.ReadingDirection = ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM,
    val continuousPadding: Float = .3f,
    val continuousPageSpacing: Int = 0,

    val cropBorders: Boolean = false,

    val komfEnabled: Boolean = false,
    val komfMode: KomfMode = KomfMode.REMOTE,
    val komfRemoteUrl: String = "http://localhost:8085",
)

package snd.komelia.db

import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
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


    val komfEnabled: Boolean = false,
    val komfMode: KomfMode = KomfMode.REMOTE,
    val komfRemoteUrl: String = "http://localhost:8085",

    val readerDebugTileGrid: Boolean = false
)

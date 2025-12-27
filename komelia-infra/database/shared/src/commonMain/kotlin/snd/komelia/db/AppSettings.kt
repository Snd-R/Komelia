package snd.komelia.db

import kotlinx.serialization.Serializable
import snd.komelia.settings.model.AppTheme
import snd.komelia.settings.model.BooksLayout
import snd.komelia.updates.AppVersion
import kotlin.time.Instant

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
)

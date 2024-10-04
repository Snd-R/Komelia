package snd.komelia.db.settings

import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.PageDisplayLayout
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set


const val serverUrlKey = "serverUrl"
const val usernameKey = "username"

const val cardWidthKey = "cardWidth"
const val seriesPageLoadSizeKey = "seriesPageLoadSize"
const val bookPageLoadSizeKey = "bookPageLoadSize"
const val bookListLayoutKey = "bookListLayout"
const val appThemeKey = "appTheme"

const val readerTypeKey = "readerType"
const val stretchToFitKey = "stretchToFit"

const val pagedReaderScaleTypeKey = "pagedReaderScaleType"
const val pagedReaderReadingDirectionKey = "pagedReaderReadingDirection"
const val pagedReaderLayoutKey = "pagedReaderLayout"

const val continuousReaderReadingDirectionKey = "continuousReaderReadingDirection"
const val continuousReaderPaddingKey = "continuousReaderPadding"
const val continuousReaderPageSpacingKey = "continuousReaderPageSpacing"

const val komfEnabledKey = "komfEnabled"
const val komfModeKey = "komfMode"
const val komfUrlKey = "KomfUrl"

class LocalStorageSettingsRepository {
    fun get(): AppSettings {
        return AppSettings(
            serverUrl = localStorage[serverUrlKey] ?: "http://localhost:25600",
            username = localStorage[usernameKey] ?: "",
            cardWidth = localStorage[cardWidthKey]?.toInt() ?: 170,
            seriesPageLoadSize = localStorage[seriesPageLoadSizeKey]?.toInt() ?: 20,
            bookPageLoadSize = localStorage[bookPageLoadSizeKey]?.toInt() ?: 20,
            bookListLayout = localStorage[bookListLayoutKey]?.let { BooksLayout.valueOf(it) } ?: BooksLayout.GRID,
            appTheme = localStorage[appThemeKey]?.let { AppTheme.valueOf(it) } ?: AppTheme.DARK,

            readerType = localStorage[readerTypeKey]?.let { ReaderType.valueOf(it) } ?: ReaderType.PAGED,
            stretchToFit = localStorage[stretchToFitKey]?.toBoolean() ?: true,
            pagedScaleType = localStorage[pagedReaderScaleTypeKey]?.let { LayoutScaleType.valueOf(it) }
                ?: LayoutScaleType.SCREEN,
            pagedReadingDirection = localStorage[pagedReaderReadingDirectionKey]
                ?.let { PagedReaderState.ReadingDirection.valueOf(it) }
                ?: PagedReaderState.ReadingDirection.LEFT_TO_RIGHT,
            pagedPageLayout = localStorage[pagedReaderLayoutKey]?.let { PageDisplayLayout.valueOf(it) }
                ?: PageDisplayLayout.SINGLE_PAGE,
            continuousReadingDirection = localStorage[continuousReaderReadingDirectionKey]
                ?.let { ContinuousReaderState.ReadingDirection.valueOf(it) }
                ?: ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM,
            continuousPadding = localStorage[continuousReaderPaddingKey]?.toFloat() ?: .3f,
            continuousPageSpacing = localStorage[continuousReaderPageSpacingKey]?.toInt() ?: 0,

            komfEnabled = localStorage[komfEnabledKey]?.toBoolean() ?: false,
            komfMode = localStorage[komfModeKey]?.let { KomfMode.valueOf(it) } ?: KomfMode.REMOTE,
            komfRemoteUrl = localStorage[komfUrlKey] ?: "http://localhost:8085",

            upscaleOption = "Default",
            downscaleOption = "Default",
        )
    }

    fun save(settings: AppSettings) {
        localStorage[serverUrlKey] = settings.serverUrl
        localStorage[usernameKey] = settings.username
        localStorage[cardWidthKey] = settings.cardWidth.toString()
        localStorage[seriesPageLoadSizeKey] = settings.seriesPageLoadSize.toString()
        localStorage[bookPageLoadSizeKey] = settings.bookPageLoadSize.toString()
        localStorage[bookListLayoutKey] = settings.bookListLayout.name
        localStorage[appThemeKey] = settings.appTheme.name

        localStorage[readerTypeKey] = settings.readerType.name
        localStorage[stretchToFitKey] = settings.stretchToFit.toString()
        localStorage[pagedReaderScaleTypeKey] = settings.pagedScaleType.name
        localStorage[pagedReaderReadingDirectionKey] = settings.pagedReadingDirection.name
        localStorage[pagedReaderLayoutKey] = settings.pagedPageLayout.name

        localStorage[continuousReaderReadingDirectionKey] = settings.continuousReadingDirection.name
        localStorage[continuousReaderPaddingKey] = settings.continuousPadding.toString()
        localStorage[continuousReaderPageSpacingKey] = settings.continuousPageSpacing.toString()

        localStorage[komfEnabledKey] = settings.komfEnabled.toString()
        localStorage[komfModeKey] = settings.komfMode.name
        localStorage[komfUrlKey] = settings.komfRemoteUrl
    }
}
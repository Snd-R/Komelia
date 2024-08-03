package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.series.BooksLayout
import kotlinx.browser.localStorage
import org.w3c.dom.get

const val serverUrlKey = "serverUrl"
const val usernameKey = "username"

const val cardWidthKey = "cardWidth"
const val seriesPageLoadSizeKey = "seriesPageLoadSize"
const val bookPageLoadSizeKey = "bookPageLoadSize"
const val bookListLayoutKey = "bookListLayout"

const val readerTypeKey = "readerType"
const val stretchToFitKey = "stretchToFit"

const val pagedReaderScaleTypeKey = "pagedReaderScaleType"
const val pagedReaderReadingDirectionKey = "pagedReaderReadingDirection"
const val pagedReaderLayoutKey = "pagedReaderLayout"

const val continuousReaderReadingDirectionKey = "continuousReaderReadingDirection"
const val continuousReaderPaddingKey = "continuousReaderPadding"
const val continuousReaderPageSpacingKey = "continuousReaderPageSpacing"

const val decoderTypeKey = "decoderType"

data class AppSettings(
    val server: ServerSettings,
    val user: UserSettings,
    val appearance: AppearanceSettings,
    val reader: ReaderSettings = ReaderSettings(),
) {
    companion object {
        fun loadSettings(): AppSettings {
            return AppSettings(
                server = ServerSettings(
                    url = localStorage[serverUrlKey] ?: "http://localhost:25600"
                ),
                user = UserSettings(
                    username = localStorage[usernameKey] ?: ""
                ),
                appearance = AppearanceSettings(
                    cardWidth = localStorage[cardWidthKey]?.toInt() ?: 240,
                    seriesPageLoadSize = localStorage[seriesPageLoadSizeKey]?.toInt() ?: 20,
                    bookPageLoadSize = localStorage[bookPageLoadSizeKey]?.toInt() ?: 20,
                    bookListLayout = localStorage[bookListLayoutKey]?.let { BooksLayout.valueOf(it) }
                        ?: BooksLayout.GRID,
                ),
                reader = ReaderSettings(
                    readerType = localStorage[readerTypeKey]?.let { ReaderType.valueOf(it) } ?: ReaderType.PAGED,
                    stretchToFit = localStorage[stretchToFitKey]?.let { it.toBoolean() } ?: true,
                    pagedReaderSettings = PagedReaderSettings(
                        scaleType = localStorage[pagedReaderScaleTypeKey]?.let { LayoutScaleType.valueOf(it) }
                            ?: LayoutScaleType.SCREEN,
                        readingDirection = localStorage[pagedReaderReadingDirectionKey]
                            ?.let { PagedReaderState.ReadingDirection.valueOf(it) }
                            ?: PagedReaderState.ReadingDirection.LEFT_TO_RIGHT,
                        pageLayout = localStorage[pagedReaderLayoutKey]?.let { PageDisplayLayout.valueOf(it) }
                            ?: PageDisplayLayout.SINGLE_PAGE,
                    ),
                    continuousReaderSettings = ContinuousReaderSettings(
                        readingDirection = localStorage[continuousReaderReadingDirectionKey]
                            ?.let { ContinuousReaderState.ReadingDirection.valueOf(it) }
                            ?: ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM,
                        padding = localStorage[continuousReaderPaddingKey]?.toFloat() ?: .3f,
                        pageSpacing = localStorage[continuousReaderPageSpacingKey]?.toInt() ?: 0,
                    )
                ),
            )
        }
    }
}

data class ServerSettings(
    val url: String
)

data class UserSettings(
    val username: String
)

data class AppearanceSettings(
    val cardWidth: Int,
    val seriesPageLoadSize: Int = 20,
    val bookPageLoadSize: Int = 20,
    val bookListLayout: BooksLayout = BooksLayout.GRID
)

data class ReaderSettings(
    val readerType: ReaderType = ReaderType.PAGED,
    val stretchToFit: Boolean = true,
    val pagedReaderSettings: PagedReaderSettings = PagedReaderSettings(),
    val continuousReaderSettings: ContinuousReaderSettings = ContinuousReaderSettings(),
)

data class PagedReaderSettings(
    val scaleType: LayoutScaleType = LayoutScaleType.SCREEN,
    val readingDirection: PagedReaderState.ReadingDirection = PagedReaderState.ReadingDirection.LEFT_TO_RIGHT,
    val pageLayout: PageDisplayLayout = PageDisplayLayout.SINGLE_PAGE
)

data class ContinuousReaderSettings(
    val readingDirection: ContinuousReaderState.ReadingDirection = ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM,
    val padding: Float = .3f,
    val pageSpacing: Int = 0
)



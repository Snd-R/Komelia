package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.platform.SamplerType
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
const val upsampleKey = "upsample"

const val pagedReaderScaleTypeKey = "pagedReaderScaleType"
const val pagedReaderStretchToFitKey = "pagedReaderStretchToFit"
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
    val decoder: DecoderSettings = DecoderSettings()
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
                    upsample = localStorage[upsampleKey]?.toBoolean() ?: false,
                    pagedReaderSettings = PagedReaderSettings(
                        scaleType = localStorage[pagedReaderScaleTypeKey]?.let { LayoutScaleType.valueOf(it) }
                            ?: LayoutScaleType.SCREEN,
                        stretchToFit = localStorage[pagedReaderStretchToFitKey]
                            ?.let { it.toBoolean() }
                            ?: true,
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
                decoder = DecoderSettings(
                    type = localStorage[decoderTypeKey]?.let { SamplerType.valueOf(it) }
                        ?: SamplerType.VIPS_LANCZOS_DOWN_BICUBIC_UP
                )
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
    val upsample: Boolean = false,
    val pagedReaderSettings: PagedReaderSettings = PagedReaderSettings(),
    val continuousReaderSettings: ContinuousReaderSettings = ContinuousReaderSettings(),
)

data class PagedReaderSettings(
    val scaleType: LayoutScaleType = LayoutScaleType.SCREEN,
    val stretchToFit: Boolean = true,
    val readingDirection: PagedReaderState.ReadingDirection = PagedReaderState.ReadingDirection.LEFT_TO_RIGHT,
    val pageLayout: PageDisplayLayout = PageDisplayLayout.SINGLE_PAGE
)

data class ContinuousReaderSettings(
    val readingDirection: ContinuousReaderState.ReadingDirection = ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM,
    val padding: Float = .3f,
    val pageSpacing: Int = 0
)

data class DecoderSettings(
    val type: SamplerType = SamplerType.VIPS_LANCZOS_DOWN_BICUBIC_UP
)



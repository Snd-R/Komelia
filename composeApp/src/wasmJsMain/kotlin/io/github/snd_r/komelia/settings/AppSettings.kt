package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.ui.reader.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import io.github.snd_r.komelia.ui.reader.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.series.BooksLayout

const val serverUrlKey = "serverUrl"
const val usernameKey = "username"

const val cardWidthKey = "cardWidth"
const val seriesPageLoadSizeKey = "seriesPageLoadSize"
const val bookPageLoadSizeKey = "bookPageLoadSize"
const val bookListLayoutKey = "bookListLayout"

const val scaleTypeKey = "scaleType"
const val upsampleKey = "upsample"
const val readingDirectionKey = "readingDirection"
const val pageLayoutKey = "pageLayout"
const val decoderTypeKey = "decoderType"

data class AppSettings(
    val server: ServerSettings,
    val user: UserSettings,
    val appearance: AppearanceSettings,
    val reader: ReaderSettings = ReaderSettings(),
    val decoder: DecoderSettings = DecoderSettings()
)

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
    val scaleType: LayoutScaleType = LayoutScaleType.SCREEN,
    val upsample: Boolean = false,
    val readingDirection: ReadingDirection = LEFT_TO_RIGHT,
    val pageLayout: PageDisplayLayout = SINGLE_PAGE
)

data class DecoderSettings(
    val type: SamplerType = SamplerType.VIPS_LANCZOS_DOWN_BICUBIC_UP
)

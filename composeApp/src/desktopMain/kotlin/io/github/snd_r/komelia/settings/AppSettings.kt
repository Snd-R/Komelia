package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.series.BooksLayout
import kotlinx.serialization.Serializable

const val defaultCardWidth = 240

@Serializable
data class AppSettings(
    val server: ServerSettings = ServerSettings(),
    val user: UserSettings = UserSettings(),
    val appearance: AppearanceSettings = AppearanceSettings(),
    val reader: ReaderBaseSettings = ReaderBaseSettings(),
    val decoder: DecoderSettings = DecoderSettings()
)

@Serializable
data class ServerSettings(
    val url: String = "http://localhost:25600"
)

@Serializable
data class UserSettings(
    val username: String = "admin@example.org"
)

@Serializable
data class AppearanceSettings(
    val cardWidth: Int = defaultCardWidth,
    val seriesPageLoadSize: Int = 20,
    val bookPageLoadSize: Int = 20,
    val bookListLayout: BooksLayout = BooksLayout.GRID
)

@Serializable
data class ReaderBaseSettings(
    val readerType: ReaderType = ReaderType.PAGED,
    val upsample: Boolean = false,
    val pagedReaderSettings: PagedReaderSettings = PagedReaderSettings(),
    val continuousReaderSettings: ContinuousReaderSettings = ContinuousReaderSettings(),
)

@Serializable
data class PagedReaderSettings(
    val scaleType: LayoutScaleType = LayoutScaleType.SCREEN,
    val readingDirection: PagedReaderState.ReadingDirection = PagedReaderState.ReadingDirection.LEFT_TO_RIGHT,
    val pageLayout: PageDisplayLayout = SINGLE_PAGE
)

@Serializable
data class ContinuousReaderSettings(
    val readingDirection: ContinuousReaderState.ReadingDirection = ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM,
    val padding: Float = .3f,
    val pageSpacing: Int = 0
)

@Serializable
data class DecoderSettings(
    val type: SamplerType = SamplerType.VIPS_LANCZOS_DOWN_BICUBIC_UP
)
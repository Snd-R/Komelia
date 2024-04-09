package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.ui.reader.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import io.github.snd_r.komelia.ui.series.BooksLayout
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.w3c.dom.get
import org.w3c.dom.set

class LocalStorageSettingsRepository : SettingsRepository {

    private val settings = MutableStateFlow(loadSettings())

    private fun loadSettings(): AppSettings {
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
                scaleType = localStorage[scaleTypeKey]?.let { LayoutScaleType.valueOf(it) }
                    ?: LayoutScaleType.SCREEN,
                upsample = localStorage[upsampleKey]?.toBoolean() ?: false,
                readingDirection = localStorage[readingDirectionKey]?.let { ReadingDirection.valueOf(it) }
                    ?: ReadingDirection.LEFT_TO_RIGHT,
                pageLayout = localStorage[pageLayoutKey]?.let { PageDisplayLayout.valueOf(it) }
                    ?: PageDisplayLayout.SINGLE_PAGE,
            )
        )
    }

    override fun getServerUrl(): Flow<String> {
        return settings.map { it.server.url }
    }

    override suspend fun putServerUrl(url: String) {
        settings.update {
            it.copy(
                server = it.server.copy(
                    url = url
                )
            )
        }
        localStorage[serverUrlKey] = url
    }

    override fun getCardWidth(): Flow<Dp> {
        return settings.map { it.appearance.cardWidth.dp }
    }

    override suspend fun putCardWidth(cardWidth: Dp) {
        settings.update {
            it.copy(
                appearance = it.appearance.copy(
                    cardWidth = cardWidth.value.toInt()
                )
            )
        }
        localStorage[cardWidthKey] = cardWidth.value.toInt().toString()
    }

    override fun getCurrentUser(): Flow<String> {
        return settings.map { it.user.username }
    }

    override suspend fun putCurrentUser(username: String) {
        settings.update {
            it.copy(user = it.user.copy(username = username))
        }
        localStorage[usernameKey] = username
    }

    override fun getReaderScaleType(): Flow<LayoutScaleType> {
        return settings.map { it.reader.scaleType }
    }

    override suspend fun putReaderScaleType(scaleType: LayoutScaleType) {
        settings.update {
            it.copy(reader = it.reader.copy(scaleType = scaleType))
        }
        localStorage[scaleTypeKey] = scaleType.name
    }

    override fun getReaderUpsample(): Flow<Boolean> {
        return settings.map { it.reader.upsample }
    }

    override suspend fun putReaderUpsample(upsample: Boolean) {
        settings.update {
            it.copy(reader = it.reader.copy(upsample = upsample))
        }
        localStorage[upsampleKey] = upsample.toString()
    }

    override fun getReaderReadingDirection(): Flow<ReadingDirection> {
        return settings.map { it.reader.readingDirection }
    }

    override suspend fun putReaderReadingDirection(readingDirection: ReadingDirection) {
        settings.update {
            it.copy(reader = it.reader.copy(readingDirection = readingDirection))
        }
        localStorage[readingDirectionKey] = readingDirection.name
    }

    override fun getReaderPageLayout(): Flow<PageDisplayLayout> {
        return settings.map { it.reader.pageLayout }
    }

    override suspend fun putReaderPageLayout(pageLayout: PageDisplayLayout) {
        settings.update {
            it.copy(reader = it.reader.copy(pageLayout = pageLayout))
        }
        localStorage[pageLayoutKey] = pageLayout.name
    }

    override fun getDecoderType(): Flow<SamplerType> {
        return flowOf(SamplerType.DEFAULT)
    }

    override suspend fun putDecoderType(type: SamplerType) {
    }

    override fun getSeriesPageLoadSize(): Flow<Int> {
        return settings.map { it.appearance.seriesPageLoadSize }
    }

    override suspend fun putSeriesPageLoadSize(size: Int) {
        settings.update {
            it.copy(appearance = it.appearance.copy(seriesPageLoadSize = size))
        }
        localStorage[seriesPageLoadSizeKey] = size.toString()
    }

    override fun getBookPageLoadSize(): Flow<Int> {
        return settings.map { it.appearance.bookPageLoadSize }
    }

    override suspend fun putBookPageLoadSize(size: Int) {
        settings.update {
            it.copy(appearance = it.appearance.copy(bookPageLoadSize = size))
        }
        localStorage[bookPageLoadSizeKey] = size.toString()
    }

    override fun getBookListLayout(): Flow<BooksLayout> {
        return settings.map { it.appearance.bookListLayout }
    }

    override suspend fun putBookListLayout(layout: BooksLayout) {
        settings.update {
            it.copy(appearance = it.appearance.copy(bookListLayout = layout))
        }
        localStorage[bookListLayoutKey] = layout.name
    }
}
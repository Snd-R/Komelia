package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.w3c.dom.set

class LocalStorageReaderSettingsRepository(private val settings: MutableStateFlow<AppSettings>) :
    ReaderSettingsRepository {
    override fun getReaderType(): Flow<ReaderType> {
        return settings.map { it.reader.readerType }
    }

    override suspend fun putReaderType(type: ReaderType) {
        settings.update { it.copy(reader = it.reader.copy(readerType = type)) }
        localStorage[readerTypeKey] = type.name
    }

    override fun getStretchToFit(): Flow<Boolean> {
        return settings.map { it.reader.stretchToFit }
    }

    override suspend fun putStretchToFit(stretch: Boolean) {
        settings.update { it.copy(reader = it.reader.copy(stretchToFit = stretch)) }
        localStorage[stretchToFitKey] = stretch.toString()
    }

    override fun getPagedReaderScaleType(): Flow<LayoutScaleType> {
        return settings.map { it.reader.pagedReaderSettings.scaleType }
    }

    override suspend fun putPagedReaderScaleType(type: LayoutScaleType) {
        settings.update {
            it.copy(
                reader = it.reader.copy(
                    pagedReaderSettings = it.reader.pagedReaderSettings.copy(
                        scaleType = type
                    )
                )
            )
        }
        localStorage[pagedReaderScaleTypeKey] = type.toString()
    }

    override fun getPagedReaderReadingDirection(): Flow<PagedReaderState.ReadingDirection> {
        return settings.map { it.reader.pagedReaderSettings.readingDirection }
    }

    override suspend fun putPagedReaderReadingDirection(direction: PagedReaderState.ReadingDirection) {
        settings.update {
            it.copy(
                reader = it.reader.copy(
                    pagedReaderSettings = it.reader.pagedReaderSettings.copy(
                        readingDirection = direction
                    )
                )
            )
        }
        localStorage[pagedReaderReadingDirectionKey] = direction.toString()
    }

    override fun getPagedReaderDisplayLayout(): Flow<PageDisplayLayout> {
        return settings.map { it.reader.pagedReaderSettings.pageLayout }
    }

    override suspend fun putPagedReaderDisplayLayout(layout: PageDisplayLayout) {
        settings.update {
            it.copy(
                reader = it.reader.copy(
                    pagedReaderSettings = it.reader.pagedReaderSettings.copy(
                        pageLayout = layout
                    )
                )
            )
        }
        localStorage[pagedReaderLayoutKey] = layout.toString()
    }

    override fun getContinuousReaderReadingDirection(): Flow<ContinuousReaderState.ReadingDirection> {
        return settings.map { it.reader.continuousReaderSettings.readingDirection }
    }

    override suspend fun putContinuousReaderReadingDirection(direction: ContinuousReaderState.ReadingDirection) {
        settings.update {
            it.copy(
                reader = it.reader.copy(
                    continuousReaderSettings = it.reader.continuousReaderSettings.copy(
                        readingDirection = direction
                    )
                )
            )
        }
        localStorage[continuousReaderReadingDirectionKey] = direction.toString()
    }

    override fun getContinuousReaderPadding(): Flow<Float> {
        return settings.map { it.reader.continuousReaderSettings.padding }
    }

    override suspend fun putContinuousReaderPadding(padding: Float) {
        settings.update {
            it.copy(
                reader = it.reader.copy(
                    continuousReaderSettings = it.reader.continuousReaderSettings.copy(
                        padding = padding
                    )
                )
            )
        }
        localStorage[continuousReaderPaddingKey] = padding.toString()
    }

    override fun getContinuousReaderPageSpacing(): Flow<Int> {
        return settings.map { it.reader.continuousReaderSettings.pageSpacing }
    }

    override suspend fun putContinuousReaderPageSpacing(spacing: Int) {
        settings.update {
            it.copy(
                reader = it.reader.copy(
                    continuousReaderSettings = it.reader.continuousReaderSettings.copy(
                        pageSpacing = spacing
                    )
                )
            )
        }
        localStorage[continuousReaderPageSpacingKey] = spacing.toString()
    }

}
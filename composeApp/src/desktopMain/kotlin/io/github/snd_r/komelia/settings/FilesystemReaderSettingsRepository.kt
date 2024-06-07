package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FilesystemReaderSettingsRepository(
    private val actor: FileSystemSettingsActor,
) : ReaderSettingsRepository {
    override fun getReaderType(): Flow<ReaderType> {
        return actor.getState().map { it.reader.readerType }
    }

    override suspend fun putReaderType(type: ReaderType) {
        actor.transform { settings -> settings.copy(reader = settings.reader.copy(readerType = type)) }
    }

    override fun getStretchToFit(): Flow<Boolean> {
        return actor.getState().map { it.reader.stretchToFit }
    }

    override suspend fun putStretchToFit(stretch: Boolean) {
        actor.transform { settings ->
            settings.copy(reader = settings.reader.copy(stretchToFit = stretch))
        }
    }

    override fun getPagedReaderScaleType(): Flow<LayoutScaleType> {
        return actor.getState().map { it.reader.pagedReaderSettings.scaleType }
    }

    override suspend fun putPagedReaderScaleType(type: LayoutScaleType) {
        actor.transform { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    pagedReaderSettings = settings.reader.pagedReaderSettings.copy(
                        scaleType = type
                    )
                )
            )
        }
    }

    override fun getPagedReaderReadingDirection(): Flow<PagedReaderState.ReadingDirection> {
        return actor.getState().map { it.reader.pagedReaderSettings.readingDirection }
    }

    override suspend fun putPagedReaderReadingDirection(direction: PagedReaderState.ReadingDirection) {
        actor.transform { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    pagedReaderSettings = settings.reader.pagedReaderSettings.copy(
                        readingDirection = direction
                    )
                )
            )
        }
    }

    override fun getPagedReaderDisplayLayout(): Flow<PageDisplayLayout> {
        return actor.getState().map { it.reader.pagedReaderSettings.pageLayout }
    }

    override suspend fun putPagedReaderDisplayLayout(layout: PageDisplayLayout) {
        actor.transform { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    pagedReaderSettings = settings.reader.pagedReaderSettings.copy(
                        pageLayout = layout
                    )
                )
            )
        }
    }

    override fun getContinuousReaderReadingDirection(): Flow<ContinuousReaderState.ReadingDirection> {
        return actor.getState().map { it.reader.continuousReaderSettings.readingDirection }
    }

    override suspend fun putContinuousReaderReadingDirection(direction: ContinuousReaderState.ReadingDirection) {
        actor.transform { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    continuousReaderSettings = settings.reader.continuousReaderSettings.copy(
                        readingDirection = direction
                    )
                )
            )
        }
    }

    override fun getContinuousReaderPadding(): Flow<Float> {
        return actor.getState().map { it.reader.continuousReaderSettings.padding }
    }

    override suspend fun putContinuousReaderPadding(padding: Float) {
        actor.transform { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    continuousReaderSettings = settings.reader.continuousReaderSettings.copy(
                        padding = padding
                    )
                )
            )
        }
    }

    override fun getContinuousReaderPageSpacing(): Flow<Int> {
        return actor.getState().map { it.reader.continuousReaderSettings.pageSpacing }
    }

    override suspend fun putContinuousReaderPageSpacing(spacing: Int) {
        actor.transform { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    continuousReaderSettings = settings.reader.continuousReaderSettings.copy(
                        pageSpacing = spacing
                    )
                )
            )
        }
    }
}
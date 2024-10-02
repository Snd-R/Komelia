package io.github.snd_r.komelia.settings

import androidx.datastore.core.DataStore
import io.github.snd_r.komelia.settings.PagedReaderSettings.PBLayoutScaleType
import io.github.snd_r.komelia.settings.PagedReaderSettings.PBPageDisplayLayout
import io.github.snd_r.komelia.settings.PagedReaderSettings.PBReadingDirection
import io.github.snd_r.komelia.settings.ReaderBaseSettings.PBReaderType
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.PageDisplayLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AndroidReaderSettingsRepository(
    private val dataStore: DataStore<AppSettings>
) : ReaderSettingsRepository {
    override fun getReaderType(): Flow<ReaderType> {
        return dataStore.data.map {
            when (it.reader.readerType) {
                PBReaderType.PAGED, PBReaderType.UNRECOGNIZED, null -> ReaderType.PAGED
                PBReaderType.CONTINUOUS -> ReaderType.CONTINUOUS
            }
        }.distinctUntilChanged()
    }

    override suspend fun putReaderType(type: ReaderType) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy {
                    readerType = when (type) {
                        ReaderType.PAGED -> PBReaderType.PAGED
                        ReaderType.CONTINUOUS -> PBReaderType.CONTINUOUS
                    }
                }
            }
        }
    }

    override fun getStretchToFit(): Flow<Boolean> {
        return dataStore.data.map {
            if (!it.reader.hasStretchToFit()) true
            else it.reader.stretchToFit
        }.distinctUntilChanged()
    }

    override suspend fun putStretchToFit(stretch: Boolean) {
        dataStore.updateData { current ->
            current.copy { reader = reader.copy { stretchToFit = stretch } }
        }
    }

    override fun getPagedReaderScaleType(): Flow<LayoutScaleType> {
        return dataStore.data.map {
            when (it.reader.pagedReaderSettings.scaleType) {
                PBLayoutScaleType.SCREEN, PBLayoutScaleType.UNRECOGNIZED, null -> LayoutScaleType.SCREEN
                PBLayoutScaleType.FIT_WIDTH -> LayoutScaleType.FIT_WIDTH
                PBLayoutScaleType.FIT_HEIGHT -> LayoutScaleType.FIT_HEIGHT
                PBLayoutScaleType.ORIGINAL -> LayoutScaleType.ORIGINAL
            }
        }.distinctUntilChanged()
    }

    override suspend fun putPagedReaderScaleType(type: LayoutScaleType) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy {
                    pagedReaderSettings = pagedReaderSettings.copy {
                        scaleType = when (type) {
                            LayoutScaleType.SCREEN -> PBLayoutScaleType.SCREEN
                            LayoutScaleType.FIT_WIDTH -> PBLayoutScaleType.FIT_WIDTH
                            LayoutScaleType.FIT_HEIGHT -> PBLayoutScaleType.FIT_HEIGHT
                            LayoutScaleType.ORIGINAL -> PBLayoutScaleType.ORIGINAL
                        }
                    }
                }
            }
        }
    }

    override fun getPagedReaderReadingDirection(): Flow<PagedReaderState.ReadingDirection> {
        return dataStore.data.map {
            when (it.reader.pagedReaderSettings.readingDirection) {
                PBReadingDirection.LEFT_TO_RIGHT, PBReadingDirection.UNRECOGNIZED, null -> PagedReaderState.ReadingDirection.LEFT_TO_RIGHT
                PBReadingDirection.RIGHT_TO_LEFT -> PagedReaderState.ReadingDirection.RIGHT_TO_LEFT
            }
        }.distinctUntilChanged()
    }

    override suspend fun putPagedReaderReadingDirection(direction: PagedReaderState.ReadingDirection) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy {
                    pagedReaderSettings = pagedReaderSettings.copy {
                        readingDirection = when (direction) {
                            PagedReaderState.ReadingDirection.LEFT_TO_RIGHT -> PBReadingDirection.LEFT_TO_RIGHT
                            PagedReaderState.ReadingDirection.RIGHT_TO_LEFT -> PBReadingDirection.RIGHT_TO_LEFT
                        }
                    }
                }
            }
        }
    }

    override fun getPagedReaderDisplayLayout(): Flow<PageDisplayLayout> {
        return dataStore.data.map {
            when (it.reader.pagedReaderSettings.pageLayout) {
                PBPageDisplayLayout.SINGLE_PAGE, PBPageDisplayLayout.UNRECOGNIZED, null -> PageDisplayLayout.SINGLE_PAGE
                PBPageDisplayLayout.DOUBLE_PAGES -> PageDisplayLayout.DOUBLE_PAGES
            }
        }.distinctUntilChanged()
    }

    override suspend fun putPagedReaderDisplayLayout(layout: PageDisplayLayout) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy {
                    pagedReaderSettings = pagedReaderSettings.copy {
                        pageLayout = when (layout) {
                            PageDisplayLayout.SINGLE_PAGE -> PBPageDisplayLayout.SINGLE_PAGE
                            PageDisplayLayout.DOUBLE_PAGES -> PBPageDisplayLayout.DOUBLE_PAGES
                        }
                    }
                }
            }
        }
    }

    override fun getContinuousReaderReadingDirection(): Flow<ContinuousReaderState.ReadingDirection> {
        return dataStore.data.map {
            when (it.reader.continuousReaderSettings.readingDirection) {
                ContinuousReaderSettings.PBReadingDirection.TOP_TO_BOTTOM, ContinuousReaderSettings.PBReadingDirection.UNRECOGNIZED, null -> ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM
                ContinuousReaderSettings.PBReadingDirection.LEFT_TO_RIGHT -> ContinuousReaderState.ReadingDirection.LEFT_TO_RIGHT
                ContinuousReaderSettings.PBReadingDirection.RIGHT_TO_LEFT -> ContinuousReaderState.ReadingDirection.RIGHT_TO_LEFT
            }
        }.distinctUntilChanged()
    }

    override suspend fun putContinuousReaderReadingDirection(direction: ContinuousReaderState.ReadingDirection) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy {
                    continuousReaderSettings = continuousReaderSettings.copy {
                        readingDirection = when (direction) {
                            ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM -> ContinuousReaderSettings.PBReadingDirection.TOP_TO_BOTTOM
                            ContinuousReaderState.ReadingDirection.LEFT_TO_RIGHT -> ContinuousReaderSettings.PBReadingDirection.LEFT_TO_RIGHT
                            ContinuousReaderState.ReadingDirection.RIGHT_TO_LEFT -> ContinuousReaderSettings.PBReadingDirection.RIGHT_TO_LEFT
                        }
                    }
                }
            }
        }
    }

    override fun getContinuousReaderPadding(): Flow<Float> {
        return dataStore.data.map { it.reader.continuousReaderSettings.padding }.distinctUntilChanged()
    }

    override suspend fun putContinuousReaderPadding(padding: Float) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy {
                    continuousReaderSettings = continuousReaderSettings.copy {
                        this.padding = padding
                    }
                }
            }
        }
    }

    override fun getContinuousReaderPageSpacing(): Flow<Int> {
        return dataStore.data.map { it.reader.continuousReaderSettings.pageSpacing }.distinctUntilChanged()
    }

    override suspend fun putContinuousReaderPageSpacing(spacing: Int) {
        dataStore.updateData { current ->
            current.copy {
                reader = reader.copy {
                    continuousReaderSettings = continuousReaderSettings.copy {
                        this.pageSpacing = spacing
                    }
                }
            }
        }
    }
}
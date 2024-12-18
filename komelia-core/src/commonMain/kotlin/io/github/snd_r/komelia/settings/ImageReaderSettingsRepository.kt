package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.ui.reader.image.ReaderType
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import kotlinx.coroutines.flow.Flow

interface ImageReaderSettingsRepository {
    fun getReaderType(): Flow<ReaderType>
    suspend fun putReaderType(type: ReaderType)

    fun getStretchToFit(): Flow<Boolean>
    suspend fun putStretchToFit(stretch: Boolean)

    fun getCropBorders(): Flow<Boolean>
    suspend fun putCropBorders(trim: Boolean)

    fun getPagedReaderScaleType(): Flow<PagedReaderState.LayoutScaleType>
    suspend fun putPagedReaderScaleType(type: PagedReaderState.LayoutScaleType)

    fun getPagedReaderReadingDirection(): Flow<PagedReaderState.ReadingDirection>
    suspend fun putPagedReaderReadingDirection(direction: PagedReaderState.ReadingDirection)

    fun getPagedReaderDisplayLayout(): Flow<PagedReaderState.PageDisplayLayout>
    suspend fun putPagedReaderDisplayLayout(layout: PagedReaderState.PageDisplayLayout)

    fun getContinuousReaderReadingDirection(): Flow<ContinuousReaderState.ReadingDirection>
    suspend fun putContinuousReaderReadingDirection(direction: ContinuousReaderState.ReadingDirection)

    fun getContinuousReaderPadding(): Flow<Float>
    suspend fun putContinuousReaderPadding(padding: Float)

    fun getContinuousReaderPageSpacing(): Flow<Int>
    suspend fun putContinuousReaderPageSpacing(spacing: Int)
}
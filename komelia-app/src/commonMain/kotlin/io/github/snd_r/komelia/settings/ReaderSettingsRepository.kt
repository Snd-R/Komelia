package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.PageDisplayLayout
import kotlinx.coroutines.flow.Flow

interface ReaderSettingsRepository {
    fun getReaderType(): Flow<ReaderType>
    suspend fun putReaderType(type: ReaderType)
    fun getStretchToFit(): Flow<Boolean>
    suspend fun putStretchToFit(stretch: Boolean)


    fun getPagedReaderScaleType(): Flow<LayoutScaleType>
    suspend fun putPagedReaderScaleType(type: LayoutScaleType)
    fun getPagedReaderReadingDirection(): Flow<PagedReaderState.ReadingDirection>
    suspend fun putPagedReaderReadingDirection(direction: PagedReaderState.ReadingDirection)
    fun getPagedReaderDisplayLayout(): Flow<PageDisplayLayout>
    suspend fun putPagedReaderDisplayLayout(layout: PageDisplayLayout)


    fun getContinuousReaderReadingDirection(): Flow<ContinuousReaderState.ReadingDirection>
    suspend fun putContinuousReaderReadingDirection(direction: ContinuousReaderState.ReadingDirection)
    fun getContinuousReaderPadding(): Flow<Float>
    suspend fun putContinuousReaderPadding(padding: Float)
    fun getContinuousReaderPageSpacing(): Flow<Int>
    suspend fun putContinuousReaderPageSpacing(spacing: Int)
}


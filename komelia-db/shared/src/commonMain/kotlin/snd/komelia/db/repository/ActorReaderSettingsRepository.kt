package snd.komelia.db.repository

import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.ui.reader.image.ReaderType
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import snd.komelia.db.ImageReaderSettings
import snd.komelia.db.SettingsStateActor

class ActorReaderSettingsRepository(
    private val actor: SettingsStateActor<ImageReaderSettings>,
) : ImageReaderSettingsRepository {

    override fun getReaderType(): Flow<ReaderType> {
        return actor.state.map { it.readerType }.distinctUntilChanged()
    }

    override suspend fun putReaderType(type: ReaderType) {
        actor.transform { settings -> settings.copy(readerType = type) }
    }

    override fun getStretchToFit(): Flow<Boolean> {
        return actor.state.map { it.stretchToFit }.distinctUntilChanged()
    }

    override suspend fun putStretchToFit(stretch: Boolean) {
        actor.transform { it.copy(stretchToFit = stretch) }
    }

    override fun getCropBorders(): Flow<Boolean> {
        return actor.state.map { it.cropBorders }.distinctUntilChanged()
    }

    override suspend fun putCropBorders(trim: Boolean) {
        actor.transform { it.copy(cropBorders = trim) }
    }

    override fun getPagedReaderScaleType(): Flow<LayoutScaleType> {
        return actor.state.map { it.pagedScaleType }.distinctUntilChanged()
    }

    override suspend fun putPagedReaderScaleType(type: LayoutScaleType) {
        actor.transform { it.copy(pagedScaleType = type) }
    }

    override fun getPagedReaderReadingDirection(): Flow<PagedReaderState.ReadingDirection> {
        return actor.state.map { it.pagedReadingDirection }.distinctUntilChanged()
    }

    override suspend fun putPagedReaderReadingDirection(direction: PagedReaderState.ReadingDirection) {
        actor.transform { it.copy(pagedReadingDirection = direction) }
    }

    override fun getPagedReaderDisplayLayout(): Flow<PageDisplayLayout> {
        return actor.state.map { it.pagedPageLayout }.distinctUntilChanged()
    }

    override suspend fun putPagedReaderDisplayLayout(layout: PageDisplayLayout) {
        actor.transform { it.copy(pagedPageLayout = layout) }
    }

    override fun getContinuousReaderReadingDirection(): Flow<ContinuousReaderState.ReadingDirection> {
        return actor.state.map { it.continuousReadingDirection }.distinctUntilChanged()
    }

    override suspend fun putContinuousReaderReadingDirection(direction: ContinuousReaderState.ReadingDirection) {
        actor.transform { it.copy(continuousReadingDirection = direction) }
    }

    override fun getContinuousReaderPadding(): Flow<Float> {
        return actor.state.map { it.continuousPadding }.distinctUntilChanged()
    }

    override suspend fun putContinuousReaderPadding(padding: Float) {
        actor.transform { it.copy(continuousPadding = padding) }
    }

    override fun getContinuousReaderPageSpacing(): Flow<Int> {
        return actor.state.map { it.continuousPageSpacing }.distinctUntilChanged()
    }

    override suspend fun putContinuousReaderPageSpacing(spacing: Int) {
        actor.transform { it.copy(continuousPageSpacing = spacing) }
    }
}
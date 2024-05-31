package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.settings.ActorMessage.Transform
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FilesystemReaderSettingsRepository(
    private val actor: FileSystemSettingsActor,
) : ReaderSettingsRepository {
    override fun getReaderType(): Flow<ReaderType> {
        return actor.getState().map { it.reader.readerType }
    }

    override suspend fun putReaderType(type: ReaderType) {
        val ack = CompletableDeferred<AppSettings>()
        actor.send(Transform(ack) { settings -> settings.copy(reader = settings.reader.copy(readerType = type)) })
        ack.await()
    }

    override fun getUpsample(): Flow<Boolean> {
        return actor.getState().map { it.reader.upsample }
    }

    override suspend fun putUpsample(upsample: Boolean) {
        val ack = CompletableDeferred<AppSettings>()
        actor.send(Transform(ack) { settings -> settings.copy(reader = settings.reader.copy(upsample = upsample)) })
        ack.await()
    }

    override fun getPagedReaderScaleType(): Flow<LayoutScaleType> {
        return actor.getState().map { it.reader.pagedReaderSettings.scaleType }
    }

    override suspend fun putPagedReaderScaleType(type: LayoutScaleType) {
        val ack = CompletableDeferred<AppSettings>()
        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    pagedReaderSettings = settings.reader.pagedReaderSettings.copy(
                        scaleType = type
                    )
                )
            )
        })
        ack.await()
    }

    override fun getPagedReaderReadingDirection(): Flow<PagedReaderState.ReadingDirection> {
        return actor.getState().map { it.reader.pagedReaderSettings.readingDirection }
    }

    override suspend fun putPagedReaderReadingDirection(direction: PagedReaderState.ReadingDirection) {
        val ack = CompletableDeferred<AppSettings>()
        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    pagedReaderSettings = settings.reader.pagedReaderSettings.copy(
                        readingDirection = direction
                    )
                )
            )
        })
        ack.await()
    }

    override fun getPagedReaderDisplayLayout(): Flow<PageDisplayLayout> {
        return actor.getState().map { it.reader.pagedReaderSettings.pageLayout }
    }

    override suspend fun putPagedReaderDisplayLayout(layout: PageDisplayLayout) {
        val ack = CompletableDeferred<AppSettings>()
        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    pagedReaderSettings = settings.reader.pagedReaderSettings.copy(
                        pageLayout = layout
                    )
                )
            )
        })
        ack.await()
    }

    override fun getPagedReaderStretchToFit(): Flow<Boolean> {
        return actor.getState().map { it.reader.pagedReaderSettings.stretchToFit }
    }

    override suspend fun putPagedReaderStretchToFit(stretch: Boolean) {
        val ack = CompletableDeferred<AppSettings>()
        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    pagedReaderSettings = settings.reader.pagedReaderSettings.copy(
                        stretchToFit = stretch
                    ),
                )
            )
        })
        ack.await()
    }

    override fun getContinuousReaderReadingDirection(): Flow<ContinuousReaderState.ReadingDirection> {
        return actor.getState().map { it.reader.continuousReaderSettings.readingDirection }
    }

    override suspend fun putContinuousReaderReadingDirection(direction: ContinuousReaderState.ReadingDirection) {
        val ack = CompletableDeferred<AppSettings>()
        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    continuousReaderSettings = settings.reader.continuousReaderSettings.copy(
                        readingDirection = direction
                    )
                )
            )
        })
        ack.await()
    }

    override fun getContinuousReaderPadding(): Flow<Float> {
        return actor.getState().map { it.reader.continuousReaderSettings.padding }
    }

    override suspend fun putContinuousReaderPadding(padding: Float) {
        val ack = CompletableDeferred<AppSettings>()
        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    continuousReaderSettings = settings.reader.continuousReaderSettings.copy(
                        padding = padding
                    )
                )
            )
        })
        ack.await()
    }

    override fun getContinuousReaderPageSpacing(): Flow<Int> {
        return actor.getState().map { it.reader.continuousReaderSettings.pageSpacing }
    }

    override suspend fun putContinuousReaderPageSpacing(spacing: Int) {
        val ack = CompletableDeferred<AppSettings>()
        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    continuousReaderSettings = settings.reader.continuousReaderSettings.copy(
                        pageSpacing = spacing
                    )
                )
            )
        })
        ack.await()
    }

}
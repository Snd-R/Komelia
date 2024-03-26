package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.image.SamplerType
import io.github.snd_r.komelia.settings.ActorMessage.Transform
import io.github.snd_r.komelia.ui.reader.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class FilesystemSettingsRepository(
    private val actor: FileSystemSettingsActor,
) : SettingsRepository {

    override fun getServerUrl(): Flow<String> {
        return actor.getState().map { it.server.url }
    }

    override suspend fun putServerUrl(url: String) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(server = settings.server.copy(url = url))
        })

        ack.await()
    }

    override fun getCardWidth(): Flow<Dp> {
        return actor.getState().map { it.appearance.cardWidth.dp }
    }

    override suspend fun putCardWidth(cardWidth: Dp) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(appearance = settings.appearance.copy(cardWidth = cardWidth.value.toInt()))
        })

        ack.await()
    }

    override fun getCurrentUser(): Flow<String> {
        return actor.getState().map { it.user.username }
    }

    override suspend fun putCurrentUser(username: String) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(user = settings.user.copy(username = username))
        })

        ack.await()
    }

    override fun getReaderScaleType(): Flow<LayoutScaleType> {
        return actor.getState().map { it.reader.scaleType }
    }

    override suspend fun putReaderScaleType(scaleType: LayoutScaleType) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    scaleType = scaleType
                )
            )
        })

        ack.await()
    }

    override fun getReaderUpsample(): Flow<Boolean> {
        return actor.getState().map { it.reader.upsample }
    }

    override suspend fun putReaderUpsample(upsample: Boolean) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    upsample = upsample
                )
            )
        })

        ack.await()
    }

    override fun getReaderReadingDirection(): Flow<ReadingDirection> {
        return actor.getState().map { it.reader.readingDirection }
    }

    override suspend fun putReaderReadingDirection(readingDirection: ReadingDirection) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    readingDirection = readingDirection
                )
            )
        })

        ack.await()
    }

    override fun getReaderPageLayout(): Flow<PageDisplayLayout> {
        return actor.getState().map { it.reader.pageLayout }
    }

    override suspend fun putReaderPageLayout(pageLayout: PageDisplayLayout) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(
                reader = settings.reader.copy(
                    pageLayout = pageLayout
                )
            )
        })

        ack.await()
    }

    override fun getDecoderType(): Flow<SamplerType> {
        return actor.getState().map { it.decoder.type }
    }

    override suspend fun putDecoderType(type: SamplerType) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(
                decoder = settings.decoder.copy(
                    type = type
                )
            )
        })

        ack.await()
    }
}
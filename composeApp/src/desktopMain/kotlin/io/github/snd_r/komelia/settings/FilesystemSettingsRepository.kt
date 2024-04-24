package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.settings.ActorMessage.Transform
import io.github.snd_r.komelia.ui.series.BooksLayout
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

    override fun getSeriesPageLoadSize(): Flow<Int> {
        return actor.getState().map { it.appearance.seriesPageLoadSize }
    }

    override suspend fun putSeriesPageLoadSize(size: Int) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(appearance = settings.appearance.copy(seriesPageLoadSize = size))
        })

        ack.await()
    }

    override fun getBookPageLoadSize(): Flow<Int> {
        return actor.getState().map { it.appearance.bookPageLoadSize }
    }

    override suspend fun putBookPageLoadSize(size: Int) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(appearance = settings.appearance.copy(bookPageLoadSize = size))
        })

        ack.await()
    }

    override fun getBookListLayout(): Flow<BooksLayout> {
        return actor.getState().map { it.appearance.bookListLayout }
    }

    override suspend fun putBookListLayout(layout: BooksLayout) {
        val ack = CompletableDeferred<AppSettings>()

        actor.send(Transform(ack) { settings ->
            settings.copy(
                appearance = settings.appearance.copy(
                    bookListLayout = layout
                )
            )
        })

        ack.await()
    }
}
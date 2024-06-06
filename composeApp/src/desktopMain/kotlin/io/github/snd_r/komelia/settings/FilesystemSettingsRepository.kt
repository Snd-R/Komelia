package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.settings.ActorMessage.Transform
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class FilesystemSettingsRepository(
    private val actor: FileSystemSettingsActor,
) : SettingsRepository {

    override fun getServerUrl(): Flow<String> {
        return actor.getState().map { it.server.url }
    }

    override suspend fun putServerUrl(url: String) {
        transform { settings -> settings.copy(server = settings.server.copy(url = url)) }
    }

    override fun getCardWidth(): Flow<Dp> {
        return actor.getState().map { it.appearance.cardWidth.dp }
    }

    override suspend fun putCardWidth(cardWidth: Dp) {
        transform { settings -> settings.copy(appearance = settings.appearance.copy(cardWidth = cardWidth.value.toInt())) }
    }

    override fun getCurrentUser(): Flow<String> {
        return actor.getState().map { it.user.username }
    }

    override suspend fun putCurrentUser(username: String) {
        transform { settings -> settings.copy(user = settings.user.copy(username = username)) }
    }

    override fun getDecoderType(): Flow<SamplerType> {
        return actor.getState().map { it.decoder.type }
    }

    override suspend fun putDecoderType(type: SamplerType) {
        transform { settings -> settings.copy(decoder = settings.decoder.copy(type = type)) }
    }

    override fun getSeriesPageLoadSize(): Flow<Int> {
        return actor.getState().map { it.appearance.seriesPageLoadSize }
    }

    override suspend fun putSeriesPageLoadSize(size: Int) {
        transform { settings -> settings.copy(appearance = settings.appearance.copy(seriesPageLoadSize = size)) }
    }

    override fun getBookPageLoadSize(): Flow<Int> {
        return actor.getState().map { it.appearance.bookPageLoadSize }
    }

    override suspend fun putBookPageLoadSize(size: Int) {
        transform { settings -> settings.copy(appearance = settings.appearance.copy(bookPageLoadSize = size)) }
    }

    override fun getBookListLayout(): Flow<BooksLayout> {
        return actor.getState().map { it.appearance.bookListLayout }
    }

    override suspend fun putBookListLayout(layout: BooksLayout) {
        transform { settings -> settings.copy(appearance = settings.appearance.copy(bookListLayout = layout)) }
    }

    override fun getCheckForUpdatesOnStartup(): Flow<Boolean> {
        return actor.getState().map { it.updates.checkForUpdatesOnStartup }
    }

    override suspend fun putCheckForUpdatesOnStartup(check: Boolean) {
        transform { settings -> settings.copy(updates = settings.updates.copy(checkForUpdatesOnStartup = check)) }
    }

    override fun getLastUpdateCheckTimestamp(): Flow<Instant?> {
        return actor.getState().map { it.updates.lastUpdateCheckTimestamp }
    }

    override suspend fun putLastUpdateCheckTimestamp(timestamp: Instant) {
        transform { settings -> settings.copy(updates = settings.updates.copy(lastUpdateCheckTimestamp = timestamp)) }
    }

    override fun getLastCheckedReleaseVersion(): Flow<AppVersion?> {
        return actor.getState().map { it.updates.lastCheckedReleaseVersion }
    }

    override suspend fun putLastCheckedReleaseVersion(version: AppVersion) {
        transform { settings -> settings.copy(updates = settings.updates.copy(lastCheckedReleaseVersion = version)) }
    }

    override fun getDismissedVersion(): Flow<AppVersion?> {
        return actor.getState().map { it.updates.dismissedVersion }
    }

    override suspend fun putDismissedVersion(version: AppVersion) {
        transform { settings -> settings.copy(updates = settings.updates.copy(dismissedVersion = version)) }
    }

    private suspend fun transform(transform: (settings: AppSettings) -> AppSettings) {
        val ack = CompletableDeferred<AppSettings>()
        actor.send(Transform(ack, transform))
        ack.await()
    }
}
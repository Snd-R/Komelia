package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant
import org.w3c.dom.set

class LocalStorageSettingsRepository(private val settings: MutableStateFlow<AppSettings>) : SettingsRepository {


    override fun getServerUrl(): Flow<String> {
        return settings.map { it.server.url }
    }

    override suspend fun putServerUrl(url: String) {
        settings.update {
            it.copy(
                server = it.server.copy(
                    url = url
                )
            )
        }
        localStorage[serverUrlKey] = url
    }

    override fun getCardWidth(): Flow<Dp> {
        return settings.map { it.appearance.cardWidth.dp }
    }

    override suspend fun putCardWidth(cardWidth: Dp) {
        settings.update {
            it.copy(
                appearance = it.appearance.copy(
                    cardWidth = cardWidth.value.toInt()
                )
            )
        }
        localStorage[cardWidthKey] = cardWidth.value.toInt().toString()
    }

    override fun getCurrentUser(): Flow<String> {
        return settings.map { it.user.username }
    }

    override suspend fun putCurrentUser(username: String) {
        settings.update {
            it.copy(user = it.user.copy(username = username))
        }
        localStorage[usernameKey] = username
    }

    override fun getDecoderType(): Flow<SamplerType> {
        return settings.map { it.decoder.type }
    }

    override suspend fun putDecoderType(type: SamplerType) {
        settings.update {
            it.copy(decoder = it.decoder.copy(type = type))
        }
        localStorage[decoderTypeKey] = type.name
    }

    override fun getSeriesPageLoadSize(): Flow<Int> {
        return settings.map { it.appearance.seriesPageLoadSize }
    }

    override suspend fun putSeriesPageLoadSize(size: Int) {
        settings.update {
            it.copy(appearance = it.appearance.copy(seriesPageLoadSize = size))
        }
        localStorage[seriesPageLoadSizeKey] = size.toString()
    }

    override fun getBookPageLoadSize(): Flow<Int> {
        return settings.map { it.appearance.bookPageLoadSize }
    }

    override suspend fun putBookPageLoadSize(size: Int) {
        settings.update {
            it.copy(appearance = it.appearance.copy(bookPageLoadSize = size))
        }
        localStorage[bookPageLoadSizeKey] = size.toString()
    }

    override fun getBookListLayout(): Flow<BooksLayout> {
        return settings.map { it.appearance.bookListLayout }
    }

    override suspend fun putBookListLayout(layout: BooksLayout) {
        settings.update {
            it.copy(appearance = it.appearance.copy(bookListLayout = layout))
        }
        localStorage[bookListLayoutKey] = layout.name
    }

    override fun getCheckForUpdatesOnStartup(): Flow<Boolean> {
        return flowOf(false)
    }

    override suspend fun putCheckForUpdatesOnStartup(check: Boolean) {
    }

    override fun getLastUpdateCheckTimestamp(): Flow<Instant?> {
        return flowOf(null)
    }

    override suspend fun putLastUpdateCheckTimestamp(timestamp: Instant) {
    }

    override fun getLastCheckedReleaseVersion(): Flow<AppVersion?> {
        return flowOf(null)
    }

    override suspend fun putLastCheckedReleaseVersion(version: AppVersion) {
    }

    override fun getDismissedVersion(): Flow<AppVersion?> {
        return flowOf(null)
    }

    override suspend fun putDismissedVersion(version: AppVersion) {
    }
}
package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import io.github.snd_r.komelia.platform.DownscaleOption
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformDecoderType
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.settings.AppearanceSettings.PBBooksLayout
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlin.math.roundToInt

class AndroidSettingsRepository(
    private val dataStore: DataStore<AppSettings>
) : SettingsRepository {
    override fun getServerUrl(): Flow<String> {
        return dataStore.data.map { it.server.url }
    }

    override suspend fun putServerUrl(url: String) {
        dataStore.updateData { current ->
            current.copy {
                server = server.copy { this.url = url }
            }
        }
    }

    override fun getCardWidth(): Flow<Dp> {
        return dataStore.data
            .map {
                val width = it.appearance.cardWidth
                if (width <= 0) 150.dp
                else width.dp
            }
    }

    override suspend fun putCardWidth(cardWidth: Dp) {
        dataStore.updateData { current ->
            current.copy {
                appearance = appearance.copy { this.cardWidth = cardWidth.value.roundToInt() }
            }
        }
    }

    override fun getCurrentUser(): Flow<String> {
        return dataStore.data.map { it.user.username }
    }

    override suspend fun putCurrentUser(username: String) {
        dataStore.updateData { current ->
            current.copy {
                user = user.copy { this.username = username }
            }
        }
    }

    override fun getDecoderType(): Flow<PlatformDecoderSettings> {
        return flowOf(
            PlatformDecoderSettings(
                PlatformDecoderType.DEFAULT,
                upscaleOption = UpscaleOption("Default"),
                downscaleOption = DownscaleOption("Default"),
            )
        )
    }

    override suspend fun putDecoderType(decoder: PlatformDecoderSettings) {
    }

    override fun getSeriesPageLoadSize(): Flow<Int> {
        return dataStore.data.map {
            val pageSize = it.appearance.seriesPageLoadSize
            if (pageSize <= 0) 20
            else pageSize
        }
    }

    override suspend fun putSeriesPageLoadSize(size: Int) {
        dataStore.updateData { current ->
            current.copy {
                appearance = appearance.copy { seriesPageLoadSize = size }
            }
        }
    }

    override fun getBookPageLoadSize(): Flow<Int> {
        return dataStore.data.map {
            val pageSize = it.appearance.bookPageLoadSize
            if (pageSize <= 0) 20
            else pageSize
        }
    }

    override suspend fun putBookPageLoadSize(size: Int) {
        dataStore.updateData { current ->
            current.copy {
                appearance = appearance.copy { bookPageLoadSize = size }
            }
        }
    }

    override fun getBookListLayout(): Flow<BooksLayout> {
        return dataStore.data.map {
            when (it.appearance.bookListLayout) {
                PBBooksLayout.LIST, PBBooksLayout.UNRECOGNIZED, null -> BooksLayout.LIST
                PBBooksLayout.GRID -> BooksLayout.GRID
            }
        }
    }

    override suspend fun putBookListLayout(layout: BooksLayout) {
        dataStore.updateData { current ->
            current.copy {
                appearance = appearance.copy {
                    bookListLayout = when (layout) {
                        BooksLayout.GRID -> PBBooksLayout.GRID
                        BooksLayout.LIST -> PBBooksLayout.LIST
                    }
                }
            }
        }
    }

    override fun getCheckForUpdatesOnStartup(): Flow<Boolean> {
        return dataStore.data.map {
            if (!it.updates.hasCheckForUpdatesOnStartup()) true
            else it.updates.checkForUpdatesOnStartup
        }
    }

    override suspend fun putCheckForUpdatesOnStartup(check: Boolean) {
        dataStore.updateData { current ->
            current.copy { updates = updates.copy { checkForUpdatesOnStartup = check } }
        }
    }

    override fun getLastUpdateCheckTimestamp(): Flow<Instant?> {
        return dataStore.data.map { Instant.fromEpochMilliseconds(it.updates.lastUpdateCheckTimestamp) }
    }

    override suspend fun putLastUpdateCheckTimestamp(timestamp: Instant) {
        dataStore.updateData { current ->
            current.copy { updates = updates.copy { lastUpdateCheckTimestamp = timestamp.toEpochMilliseconds() } }
        }
    }

    override fun getLastCheckedReleaseVersion(): Flow<AppVersion?> {
        return dataStore.data.map {
            if (!it.updates.hasLastCheckedReleaseVersion()) null
            else AppVersion.fromString(it.updates.lastCheckedReleaseVersion)
        }
    }

    override suspend fun putLastCheckedReleaseVersion(version: AppVersion) {
        dataStore.updateData { current ->
            current.copy { updates = updates.copy { lastCheckedReleaseVersion = version.toString() } }
        }
    }

    override fun getDismissedVersion(): Flow<AppVersion?> {
        return dataStore.data.map {
            if (!it.updates.hasDismissedVersion()) null
            else AppVersion.fromString(it.updates.dismissedVersion)
        }
    }

    override suspend fun putDismissedVersion(version: AppVersion) {
        dataStore.updateData { current ->
            current.copy { updates = updates.copy { dismissedVersion = version.toString() } }
        }
    }
}
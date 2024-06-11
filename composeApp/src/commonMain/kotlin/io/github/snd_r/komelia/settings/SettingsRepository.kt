package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant


interface SettingsRepository {

    fun getServerUrl(): Flow<String>
    suspend fun putServerUrl(url: String)

    fun getCardWidth(): Flow<Dp>
    suspend fun putCardWidth(cardWidth: Dp)

    fun getCurrentUser(): Flow<String>
    suspend fun putCurrentUser(username: String)

    fun getSeriesPageLoadSize(): Flow<Int>
    suspend fun putSeriesPageLoadSize(size: Int)

    fun getBookPageLoadSize(): Flow<Int>
    suspend fun putBookPageLoadSize(size: Int)

    fun getBookListLayout(): Flow<BooksLayout>
    suspend fun putBookListLayout(layout: BooksLayout)

    fun getDecoderType(): Flow<PlatformDecoderSettings>
    suspend fun putDecoderType(decoder: PlatformDecoderSettings)

    fun getOnnxModelsPath(): Flow<String>
    suspend fun putOnnxModelsPath(path: String)

    fun getCheckForUpdatesOnStartup(): Flow<Boolean>
    suspend fun putCheckForUpdatesOnStartup(check: Boolean)

    fun getLastUpdateCheckTimestamp(): Flow<Instant?>
    suspend fun putLastUpdateCheckTimestamp(timestamp: Instant)

    fun getLastCheckedReleaseVersion(): Flow<AppVersion?>
    suspend fun putLastCheckedReleaseVersion(version: AppVersion)

    fun getDismissedVersion(): Flow<AppVersion?>
    suspend fun putDismissedVersion(version: AppVersion)
}
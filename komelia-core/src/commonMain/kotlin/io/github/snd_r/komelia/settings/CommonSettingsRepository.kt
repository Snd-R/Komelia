package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface CommonSettingsRepository {
    fun getServerUrl(): Flow<String>
    suspend fun putServerUrl(url: String)

    fun getCardWidth(): Flow<Int>
    suspend fun putCardWidth(cardWidth: Int)

    fun getCurrentUser(): Flow<String>
    suspend fun putCurrentUser(username: String)

    fun getSeriesPageLoadSize(): Flow<Int>
    suspend fun putSeriesPageLoadSize(size: Int)

    fun getBookPageLoadSize(): Flow<Int>
    suspend fun putBookPageLoadSize(size: Int)

    fun getBookListLayout(): Flow<BooksLayout>
    suspend fun putBookListLayout(layout: BooksLayout)

    fun getCheckForUpdatesOnStartup(): Flow<Boolean>
    suspend fun putCheckForUpdatesOnStartup(check: Boolean)

    fun getLastUpdateCheckTimestamp(): Flow<Instant?>
    suspend fun putLastUpdateCheckTimestamp(timestamp: Instant)

    fun getLastCheckedReleaseVersion(): Flow<AppVersion?>
    suspend fun putLastCheckedReleaseVersion(version: AppVersion)

    fun getDismissedVersion(): Flow<AppVersion?>
    suspend fun putDismissedVersion(version: AppVersion)

    fun getAppTheme(): Flow<AppTheme>
    suspend fun putAppTheme(theme: AppTheme)

    fun getKomfEnabled(): Flow<Boolean>
    suspend fun putKomfEnabled(enabled: Boolean)

    fun getKomfMode(): Flow<KomfMode>
    suspend fun putKomfMode(mode: KomfMode)

    fun getKomfUrl(): Flow<String>
    suspend fun putKomfUrl(url: String)
}
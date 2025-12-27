package snd.komelia.settings

import kotlinx.coroutines.flow.Flow
import snd.komelia.settings.model.AppTheme
import snd.komelia.settings.model.BooksLayout
import snd.komelia.updates.AppVersion
import kotlin.time.Instant

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
}
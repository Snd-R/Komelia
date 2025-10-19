package snd.komelia.db.repository

import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import snd.komelia.db.AppSettings
import snd.komelia.db.SettingsStateActor
import kotlin.time.Instant

class ActorSettingsRepository(
    private val actor: SettingsStateActor<AppSettings>,
) : CommonSettingsRepository {

    override fun getServerUrl(): Flow<String> {
        return actor.state.map { it.serverUrl }.distinctUntilChanged()
    }

    override suspend fun putServerUrl(url: String) {
        actor.transform { it.copy(serverUrl = url) }
    }

    override fun getCardWidth(): Flow<Int> {
        return actor.state.map { it.cardWidth }.distinctUntilChanged()
    }

    override suspend fun putCardWidth(cardWidth: Int) {
        actor.transform { it.copy(cardWidth = cardWidth) }
    }

    override fun getCurrentUser(): Flow<String> {
        return actor.state.map { it.username }.distinctUntilChanged()
    }

    override suspend fun putCurrentUser(username: String) {
        actor.transform { it.copy(username = username) }
    }

    override fun getSeriesPageLoadSize(): Flow<Int> {
        return actor.state.map { it.seriesPageLoadSize }.distinctUntilChanged()
    }

    override suspend fun putSeriesPageLoadSize(size: Int) {
        actor.transform { it.copy(seriesPageLoadSize = size) }
    }

    override fun getBookPageLoadSize(): Flow<Int> {
        return actor.state.map { it.bookPageLoadSize }.distinctUntilChanged()
    }

    override suspend fun putBookPageLoadSize(size: Int) {
        actor.transform { it.copy(bookPageLoadSize = size) }
    }

    override fun getBookListLayout(): Flow<BooksLayout> {
        return actor.state.map { it.bookListLayout }.distinctUntilChanged()
    }

    override suspend fun putBookListLayout(layout: BooksLayout) {
        actor.transform { it.copy(bookListLayout = layout) }
    }

    override fun getCheckForUpdatesOnStartup(): Flow<Boolean> {
        return actor.state.map { it.checkForUpdatesOnStartup }.distinctUntilChanged()
    }

    override suspend fun putCheckForUpdatesOnStartup(check: Boolean) {
        actor.transform { it.copy(checkForUpdatesOnStartup = check) }
    }

    override fun getLastUpdateCheckTimestamp(): Flow<Instant?> {
        return actor.state.map { it.updateLastCheckedTimestamp }.distinctUntilChanged()
    }

    override suspend fun putLastUpdateCheckTimestamp(timestamp: Instant) {
        actor.transform { it.copy(updateLastCheckedTimestamp = timestamp) }
    }

    override fun getLastCheckedReleaseVersion(): Flow<AppVersion?> {
        return actor.state.map { it.updateLastCheckedReleaseVersion }.distinctUntilChanged()
    }

    override suspend fun putLastCheckedReleaseVersion(version: AppVersion) {
        actor.transform { it.copy(updateLastCheckedReleaseVersion = version) }
    }

    override fun getDismissedVersion(): Flow<AppVersion?> {
        return actor.state.map { it.updateDismissedVersion }.distinctUntilChanged()
    }

    override suspend fun putDismissedVersion(version: AppVersion) {
        actor.transform { it.copy(updateDismissedVersion = version) }
    }

    override fun getAppTheme(): Flow<AppTheme> {
        return actor.state.map { it.appTheme }.distinctUntilChanged()
    }

    override suspend fun putAppTheme(theme: AppTheme) {
        actor.transform { it.copy(appTheme = theme) }
    }
}
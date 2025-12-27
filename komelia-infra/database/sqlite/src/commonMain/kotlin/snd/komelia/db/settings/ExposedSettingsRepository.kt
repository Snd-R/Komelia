package snd.komelia.db.settings

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.AppSettings
import snd.komelia.db.ExposedRepository
import snd.komelia.db.tables.AppSettingsTable
import snd.komelia.settings.model.AppTheme
import snd.komelia.settings.model.BooksLayout
import snd.komelia.updates.AppVersion
import kotlin.time.Instant

class ExposedSettingsRepository(database: Database) : ExposedRepository(database) {

    suspend fun get(): AppSettings? {
        return transaction {
            AppSettingsTable.selectAll()
                .firstOrNull()
                ?.toAppSettings()
        }
    }

    suspend fun save(settings: AppSettings) {
        transaction {
            AppSettingsTable.upsert {
                it[version] = 1
                it[username] = settings.username
                it[serverUrl] = settings.serverUrl
                it[cardWidth] = settings.cardWidth

                it[seriesPageLoadSize] = settings.seriesPageLoadSize
                it[bookPageLoadSize] = settings.bookPageLoadSize
                it[bookListLayout] = settings.bookListLayout.name
                it[appTheme] = settings.appTheme.name

                it[checkForUpdatesOnStartup] = settings.checkForUpdatesOnStartup
                it[updateLastCheckedTimestamp] = settings.updateLastCheckedTimestamp?.toString()
                it[updateLastCheckedReleaseVersion] = settings.updateLastCheckedReleaseVersion?.toString()
                it[updateDismissedVersion] = settings.updateDismissedVersion?.toString()
            }
        }
    }

    private fun ResultRow.toAppSettings(): AppSettings {
        return AppSettings(
            username = get(AppSettingsTable.username),
            serverUrl = get(AppSettingsTable.serverUrl),
            cardWidth = get(AppSettingsTable.cardWidth),
            seriesPageLoadSize = get(AppSettingsTable.seriesPageLoadSize),
            bookPageLoadSize = get(AppSettingsTable.bookPageLoadSize),
            bookListLayout = BooksLayout.valueOf(get(AppSettingsTable.bookListLayout)),
            appTheme = AppTheme.valueOf(get(AppSettingsTable.appTheme)),
            checkForUpdatesOnStartup = get(AppSettingsTable.checkForUpdatesOnStartup),
            updateLastCheckedTimestamp = get(AppSettingsTable.updateLastCheckedTimestamp)?.let {
                runCatching { Instant.parse(it) }.getOrNull()
            },
            updateLastCheckedReleaseVersion = get(AppSettingsTable.updateLastCheckedReleaseVersion)
                ?.let { AppVersion.fromString(it) },
            updateDismissedVersion = get(AppSettingsTable.updateDismissedVersion)
                ?.let { AppVersion.fromString(it) },
        )
    }

}
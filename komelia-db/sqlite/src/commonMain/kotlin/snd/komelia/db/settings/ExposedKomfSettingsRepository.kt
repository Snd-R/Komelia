package snd.komelia.db.settings

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.KomfSettings
import snd.komelia.db.tables.AppSettingsTable
import snd.komelia.db.tables.KomfSettingsTable

class ExposedKomfSettingsRepository(database: Database) : ExposedRepository(database) {

    suspend fun get(): KomfSettings? {
        return transaction {
            KomfSettingsTable.selectAll()
                .firstOrNull()
                ?.toKomfSettings()
        }
    }

    suspend fun save(settings: KomfSettings) {
        transaction {
            AppSettingsTable.upsert {
                it[version] = 1
                it[KomfSettingsTable.enabled] = settings.enabled
                it[KomfSettingsTable.remoteUrl] = settings.remoteUrl
            }
        }
    }

    private fun ResultRow.toKomfSettings(): KomfSettings {
        return KomfSettings(
            enabled = get(KomfSettingsTable.enabled),
            remoteUrl = get(KomfSettingsTable.remoteUrl),
        )
    }

}
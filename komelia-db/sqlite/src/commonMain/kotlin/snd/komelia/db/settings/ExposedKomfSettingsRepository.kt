package snd.komelia.db.settings

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.KomfSettings
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
            KomfSettingsTable.upsert {
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
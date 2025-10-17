package snd.komelia.db.settings

import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.EpubReaderSettings
import snd.komelia.db.ExposedRepository
import snd.komelia.db.defaultBookId
import snd.komelia.db.tables.EpubReaderSettingsTable

class ExposedEpubReaderSettingsRepository(database: Database) : ExposedRepository(database) {

    suspend fun get(): EpubReaderSettings? {
        return transaction {
            EpubReaderSettingsTable.selectAll()
                .where { EpubReaderSettingsTable.bookId.eq(defaultBookId) }
                .firstOrNull()
                ?.let {
                    EpubReaderSettings(
                        readerType = EpubReaderType.valueOf(it[EpubReaderSettingsTable.readerType]),
                        komgaReaderSettings = it[EpubReaderSettingsTable.komgaSettingsJson],
                        ttsuReaderSettings = it[EpubReaderSettingsTable.ttsuSettingsJson]
                    )
                }
        }
    }

    suspend fun save(settings: EpubReaderSettings) {
        transaction {
            EpubReaderSettingsTable.upsert {
                it[bookId] = defaultBookId
                it[readerType] = settings.readerType.name
                it[komgaSettingsJson] = settings.komgaReaderSettings
                it[ttsuSettingsJson] = settings.ttsuReaderSettings
            }
        }
    }
}
package snd.komelia.db.settings

import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.ui.reader.epub.TtsuReaderSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import snd.komelia.db.tables.KomgaReaderSettingsTable
import snd.komelia.db.tables.TtsuReaderSettingsTable

private const val defaultBookId = "DEFAULT"

class ExposedEpubReaderSettingsRepository(
    private val database: Database
) : EpubReaderSettingsRepository {
    private suspend fun <T> transactionOnDefaultDispatcher(statement: Transaction.() -> T): T {
        return withContext(Dispatchers.Default) { transaction(database, statement) }
    }

    override suspend fun getKomgaReaderSettings(): JsonObject {
        return transactionOnDefaultDispatcher {
            val record = KomgaReaderSettingsTable.selectAll()
                .where { KomgaReaderSettingsTable.bookId.eq(defaultBookId) }
                .firstOrNull()

            record?.get(KomgaReaderSettingsTable.settingsJson) ?: buildJsonObject { }
        }
    }

    override suspend fun putKomgaReaderSettings(settings: JsonObject) {
        transactionOnDefaultDispatcher {
            KomgaReaderSettingsTable.upsert {
                it[bookId] = defaultBookId
                it[settingsJson] = settings
            }
        }
    }

    override suspend fun getTtsuReaderSettings(): TtsuReaderSettings {
        return transactionOnDefaultDispatcher {
            TtsuReaderSettingsTable.selectAll()
                .where { TtsuReaderSettingsTable.bookId.eq(defaultBookId) }
                .firstOrNull()
                ?.get(TtsuReaderSettingsTable.settingsJson)
                ?: TtsuReaderSettings()
        }
    }

    override suspend fun putTtsuReaderSettings(settings: TtsuReaderSettings) {
        transactionOnDefaultDispatcher {
            TtsuReaderSettingsTable.upsert {
                it[bookId] = defaultBookId
                it[settingsJson] = settings
            }
        }
    }
}
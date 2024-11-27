package snd.komelia.db.tables

import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json
import snd.komelia.db.JsonDbDefault

object KomgaReaderSettingsTable : Table("KomgaEpubReaderSettings") {
    val bookId = text("book_id")
    val settingsJson = json<JsonObject>("settings_json", JsonDbDefault)


    override val primaryKey = PrimaryKey(bookId)
}

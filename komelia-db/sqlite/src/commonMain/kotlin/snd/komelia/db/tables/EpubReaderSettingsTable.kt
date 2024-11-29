package snd.komelia.db.tables

import io.github.snd_r.komelia.ui.reader.epub.TtsuReaderSettings
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json
import snd.komelia.db.JsonDbDefault

object EpubReaderSettingsTable : Table("EpubReaderSettings") {
    val bookId = text("book_id")
    val readerType = text("reader_type")
    val komgaSettingsJson = json<JsonObject>("komga_settings_json", JsonDbDefault)
    val ttsuSettingsJson = json<TtsuReaderSettings>("ttsu_settings_json", JsonDbDefault)

    override val primaryKey = PrimaryKey(bookId)
}

package snd.komelia.db.tables

import io.github.snd_r.komelia.ui.reader.epub.TtsuReaderSettings
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json
import snd.komelia.db.JsonDbDefault

object TtsuReaderSettingsTable : Table("TtsuEpubReaderSettings") {
    val bookId = text("book_id")
    val settingsJson = json<TtsuReaderSettings>("settings_json", JsonDbDefault)

    override val primaryKey = PrimaryKey(KomgaReaderSettingsTable.bookId)
}
package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.json
import snd.komelia.db.JsonDbDefault
import snd.komelia.offline.media.model.MediaExtension

object OfflineMediaTable : Table("MEDIA") {
    val bookId = text("book_id")
    val status = text("status")
    val mediaType = text("media_type").nullable()
    val mediaProfile = text("media_profile").nullable()
    val pageCount = integer("page_count")
    val comment = text("comment")
    val epubDivinaCompatible = bool("epub_divina_compatible")
    val epubIsKepub = bool("epub_is_kepub")

    val extension = json<MediaExtension>("extension", JsonDbDefault).nullable()

    override val primaryKey = PrimaryKey(bookId)

    init {
        foreignKey(bookId, target = OfflineBookTable.primaryKey)
    }
}
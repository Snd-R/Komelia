package snd.komelia.db.tables

import org.jetbrains.exposed.v1.core.Table

object BookColorCorrectionTable : Table("BookColorCorrection") {
    val bookId = text("book_id")
    val type = text("type")
    override val primaryKey = PrimaryKey(bookId)
}

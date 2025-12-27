package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineLogJournalTable : Table("LOG_JOURNAL") {
    val id = text("id")
    val message = text("message")
    val type = text("type")
    val timestamp = long("timestamp")
    override val primaryKey = PrimaryKey(id)
}
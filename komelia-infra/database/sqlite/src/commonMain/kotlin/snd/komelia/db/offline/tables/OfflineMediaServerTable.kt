package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineMediaServerTable : Table("OFFLINE_MEDIA_SERVER") {
    val id = text("id")
    val url = text("url")

    override val primaryKey = PrimaryKey(id)
}
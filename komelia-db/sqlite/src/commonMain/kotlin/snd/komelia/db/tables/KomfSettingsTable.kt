package snd.komelia.db.tables

import org.jetbrains.exposed.sql.Table

object KomfSettingsTable : Table("KomfSettings") {
    val version = integer("version")
    val enabled = bool("enabled")
    val remoteUrl = text("remote_url")
    override val primaryKey = PrimaryKey(version)
}
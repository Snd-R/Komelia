package snd.komelia.db.tables

import org.jetbrains.exposed.v1.core.Table

object KomfSettingsTable : Table("KomfSettings") {
    val version = integer("version")
    val enabled = bool("enabled")
    val remoteUrl = text("remote_url")
    override val primaryKey = PrimaryKey(version)
}
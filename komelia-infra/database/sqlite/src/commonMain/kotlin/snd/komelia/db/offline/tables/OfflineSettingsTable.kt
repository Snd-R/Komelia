package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineSettingsTable : Table("SETTINGS") {
    val version = integer("version")
    val isOfflineModeEnabled = bool("is_offline_mode_enabled")
    val userId = text("user_id").nullable()
    val serverId = text("server_id").nullable()
    val downloadDirectory = text("download_directory")
    val readProgressSyncDate = long("read_progress_sync_date").nullable()
    val dataSyncDate = long("data_sync_date").nullable()

    override val primaryKey = PrimaryKey(version)
}
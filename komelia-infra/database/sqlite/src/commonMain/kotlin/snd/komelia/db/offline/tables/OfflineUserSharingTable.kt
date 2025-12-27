package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineUserSharingTable : Table("USER_SHARING") {
    val userId = text("user_id")
    val label = text("label")
    val allow = bool("allow")

    override val primaryKey = PrimaryKey(userId, label, allow)

    init {
        foreignKey(userId, target = OfflineUserTable.primaryKey)
    }
}
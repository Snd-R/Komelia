package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineUserRoleTable : Table("USER_ROLE") {
    val userId = text("user_id")
    val role = text("role")

    override val primaryKey = PrimaryKey(userId, role)

    init {
        foreignKey(userId, target = OfflineUserTable.primaryKey)
    }
}
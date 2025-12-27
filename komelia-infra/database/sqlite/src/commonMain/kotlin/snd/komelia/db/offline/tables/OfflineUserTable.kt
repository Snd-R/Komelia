package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineUserTable : Table("USER") {
    val id = text("id")
    val serverId = text("server_id").nullable()
    val email = text("email")
    val sharedAllLibraries = bool("shared_all_libraries")
    val ageRestriction = integer("age_restriction").nullable()
    val ageRestrictionAllowOnly = bool("age_restriction_allow_only").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(serverId, target = OfflineMediaServerTable.primaryKey)
    }
}
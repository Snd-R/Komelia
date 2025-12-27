package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineUserLibrarySharingTable : Table("USER_LIBRARY_SHARING") {
    val userId = text("user_id")
    val libraryId = text("library_id")

    override val primaryKey = PrimaryKey(userId, libraryId)

    init {
        foreignKey(userId, target = OfflineUserTable.primaryKey)
        foreignKey(libraryId, target = OfflineLibraryTable.primaryKey)
    }
}
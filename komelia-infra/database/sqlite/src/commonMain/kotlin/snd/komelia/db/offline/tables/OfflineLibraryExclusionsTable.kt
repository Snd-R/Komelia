package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineLibraryExclusionsTable : Table("LIBRARY_EXCLUSIONS") {
    val libraryId = text("library_id")
    val exclusion = text("exclusion")

    override val primaryKey = PrimaryKey(libraryId, exclusion)

    init {
        foreignKey(libraryId, target = OfflineLibraryTable.primaryKey)
    }
}
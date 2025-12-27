package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.json
import snd.komelia.db.JsonDbDefault
import snd.komga.client.book.R2Locator

object OfflineReadProgressTable : Table("READ_PROGRESS") {
    val bookId = text("book_id")
    val userId = text("user_id")
    val page = integer("page")
    val completed = bool("completed")
    val readDate = long("read_date")
    val deviceId = text("device_id")
    val deviceName = text("device_name")
    val locator = json<R2Locator>("locator", JsonDbDefault).nullable()
    val createdDate = long("created_date")
    val lastModifiedDate = long("last_modified_date")

    override val primaryKey = PrimaryKey(bookId, userId)

    init {
        foreignKey(bookId, target = OfflineBookTable.primaryKey)
        foreignKey(userId, target = OfflineUserTable.primaryKey)
    }
}
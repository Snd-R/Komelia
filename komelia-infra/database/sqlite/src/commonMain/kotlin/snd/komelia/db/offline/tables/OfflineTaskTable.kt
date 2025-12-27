package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.json
import snd.komelia.db.JsonDbDefault
import snd.komelia.offline.tasks.model.TaskData

object OfflineTaskTable : Table("TASK") {
    val uniqueName = text("unique_name")
    val priority = integer("priority")
    val status = text("status")
    val task = json<TaskData>("task", JsonDbDefault)

    val createdDate = long("created_date")

    override val primaryKey = PrimaryKey(uniqueName)
}
package snd.komelia.db.offline

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchUpsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.updateReturning
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.tables.OfflineTaskTable
import snd.komelia.offline.tasks.model.TaskEntry
import snd.komelia.offline.tasks.model.TaskEntry.TaskStatus
import snd.komelia.offline.tasks.model.TaskEntry.TaskStatus.NEW
import snd.komelia.offline.tasks.model.TaskEntry.TaskStatus.RUNNING
import snd.komelia.offline.tasks.repository.OfflineTasksRepository

class ExposedOfflineTasksRepository(database: Database) : ExposedRepository(database), OfflineTasksRepository {

    val tasksTable = OfflineTaskTable


    override suspend fun takeNew(): TaskEntry? {
        return transaction {
            val selectStatement = tasksTable.select(tasksTable.uniqueName)
                .where(tasksTable.status.eq(NEW.name))
                .orderBy(tasksTable.priority to SortOrder.DESC, tasksTable.createdDate to SortOrder.ASC)
                .limit(1)

            tasksTable.updateReturning(
                returning = tasksTable.columns,
                where = { tasksTable.uniqueName.inSubQuery(selectStatement) }
            ) {
                it[tasksTable.status] = RUNNING.name
            }.firstOrNull()?.toModel()
        }
    }

    override suspend fun save(entry: TaskEntry) {
        transaction {

            tasksTable.upsert(
                onUpdateExclude = listOf(tasksTable.createdDate)
            ) {
                it[tasksTable.uniqueName] = entry.uniqueName
                it[tasksTable.priority] = entry.priority
                it[tasksTable.status] = entry.status.name
                it[tasksTable.task] = entry.task
            }
        }
    }

//    override suspend fun findAllByStatus(status: TaskStatus, limit: Int, offset: Long): Page<TaskEntry> {
//        return transaction {
//            val count = tasksTable.select(tasksTable.id.count())
//                .where { tasksTable.status.eq(status.name) }
//                .first()
//                .get(tasksTable.id.count())
//
//            val result = tasksTable.selectAll()
//                .where { tasksTable.status.eq(status.name) }
//                .orderBy(tasksTable.createdDate, SortOrder.DESC)
//                .limit(limit)
//                .offset(offset)
//                .map { it.toModel() }
//
//            page(result = result, count = count, limit = limit, offset = offset, sorted = true)
//        }
//    }

    override suspend fun save(tasks: Collection<TaskEntry>) {
        transaction {
            tasksTable.batchUpsert(
                data = tasks,
                onUpdateExclude = listOf(tasksTable.createdDate)
            ) { entry ->
                this[tasksTable.uniqueName] = entry.uniqueName
                this[tasksTable.priority] = entry.priority
                this[tasksTable.status] = entry.status.name
                this[tasksTable.task] = entry.task
            }
        }
    }

    override suspend fun delete(taskId: String) {
        transaction {
            tasksTable.deleteWhere { tasksTable.uniqueName.eq(taskId) }
        }
    }

    override suspend fun resetAllRunning(): Int {
        return transaction {
            tasksTable.update(where = { tasksTable.status.eq(RUNNING.name) }) {
                it[status] = NEW.name
            }
        }
    }

    private fun ResultRow.toModel(): TaskEntry {
        return TaskEntry(
            uniqueName = this[tasksTable.uniqueName],
            priority = this[tasksTable.priority],
            status = TaskStatus.valueOf(this[tasksTable.status]),
            task = this[tasksTable.task]
        )
    }
}
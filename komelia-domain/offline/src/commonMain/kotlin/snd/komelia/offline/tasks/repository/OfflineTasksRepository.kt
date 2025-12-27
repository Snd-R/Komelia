package snd.komelia.offline.tasks.repository

import snd.komelia.offline.tasks.model.TaskEntry

interface OfflineTasksRepository {
    suspend fun takeNew(): TaskEntry?
    suspend fun save(entry: TaskEntry)
    suspend fun save(tasks: Collection<TaskEntry>)
    suspend fun delete(taskId: String)
    suspend fun resetAllRunning(): Int
}
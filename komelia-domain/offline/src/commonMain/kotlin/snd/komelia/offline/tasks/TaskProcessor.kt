package snd.komelia.offline.tasks

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import snd.komelia.offline.sync.model.OfflineLogEntry.Companion.logError
import snd.komelia.offline.sync.repository.LogJournalRepository
import snd.komelia.offline.tasks.model.TaskAddedEvent
import snd.komelia.offline.tasks.model.TaskEntry
import snd.komelia.offline.tasks.repository.OfflineTasksRepository
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger { }

private typealias JobId = Int

@OptIn(ExperimentalAtomicApi::class)
class TaskProcessor(
    private val tasksRepository: OfflineTasksRepository,
    private val taskHandler: TaskHandler,
    private val taskAddedEvents: SharedFlow<TaskAddedEvent>,
    private val logJournalRepository: LogJournalRepository,
) {

    private val processorScope = CoroutineScope(
        Dispatchers.Default +
                SupervisorJob() +
                CoroutineName("TaskProcessor")
    )
    private val managementScope = CoroutineScope(
        Dispatchers.Default.limitedParallelism(1) +
                SupervisorJob() +
                CoroutineName("TaskDispatcher")
    )
    private val taskFinishedEvents = MutableSharedFlow<JobId>(extraBufferCapacity = Int.MAX_VALUE)

    private val jobCounter = AtomicInt(0)
    private val jobs = mutableMapOf<JobId, Job>()
    private val mutex = Mutex()

    fun initialize() {
        managementScope.launch {
            val disowned = tasksRepository.resetAllRunning()
            if (disowned > 0) {
                logger.info { "Reset $disowned tasks that were not finished" }
            }

            processAvailableTasks()
            taskAddedEvents.onEach { processAvailableTasks() }.launchIn(managementScope)
            taskFinishedEvents.onEach { onTaskFinish(it) }.launchIn(managementScope)
        }
    }

    private suspend fun processTask(jobId: Int, task: TaskEntry) {
        try {
            taskHandler.handleTask(task)
            tasksRepository.delete(task.uniqueName)
        } catch (e: CancellationException) {
            logger.catching(e)
            logJournalRepository.logError(e) { "Task processing error" }
            tasksRepository.delete(task.uniqueName)
            throw e
        } catch (e: Exception) {
            logger.catching(e)
            logJournalRepository.logError(e) { "Task processing error" }
            tasksRepository.delete(task.uniqueName)
        } finally {
            taskFinishedEvents.emit(jobId)
        }
    }

    private suspend fun onTaskFinish(jobId: JobId) {
        mutex.withLock { jobs.remove(jobId) }
        processAvailableTasks()
    }

    private suspend fun processAvailableTasks() {
        mutex.withLock {
            do {
                val task = tasksRepository.takeNew()
                if (task != null) {
                    val jobId = jobCounter.incrementAndFetch()
                    val job = processorScope.launch { processTask(jobId, task) }
                    jobs[jobId] = job
                }
            } while (task != null)
        }
    }
}
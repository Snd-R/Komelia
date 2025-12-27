package snd.komelia.ui.settings.offline.logs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import snd.komelia.offline.sync.model.OfflineLogEntry
import snd.komelia.offline.sync.repository.LogJournalRepository

class OfflineLogsState(
    private val logJournalRepository: LogJournalRepository,
    private val coroutineScope: CoroutineScope,
) {

    val logs = MutableStateFlow<List<OfflineLogEntry>>(emptyList())

    val tab = MutableStateFlow(TaskTab.ERROR)
    val pageNumber = MutableStateFlow(1)
    val totalPages = MutableStateFlow(0)
    private val pageSize = 20

    suspend fun initialize() {
        loadTasks()
    }

    private suspend fun loadTasks() {
        val pageIndex = pageNumber.value - 1
        when (tab.value) {
            TaskTab.INFO -> {
                val page = logJournalRepository
                    .findAllByType(
                        type = OfflineLogEntry.Type.INFO,
                        limit = pageSize,
                        offset = pageIndex.toLong() * pageSize
                    )
                logs.value = page.content
                pageNumber.value = page.number + 1
                totalPages.value = page.totalPages
            }

            TaskTab.ERROR -> {
                val page = logJournalRepository
                    .findAllByType(
                        type = OfflineLogEntry.Type.ERROR,
                        limit = pageSize,
                        offset = (pageIndex).toLong() * pageSize
                    )
                logs.value = page.content
                pageNumber.value = page.number + 1
                totalPages.value = page.totalPages
            }
        }
    }

    fun onPageChange(page: Int) {
        this.pageNumber.value = page
        coroutineScope.launch { loadTasks() }
    }

    fun onTabChange(tab: TaskTab) {
        this.tab.value = tab
        this.pageNumber.value = 1
        coroutineScope.launch { loadTasks() }
    }

    fun onLogsDelete() {
        coroutineScope.launch {
            logJournalRepository.deleteAll()
            loadTasks()
        }
    }

    enum class TaskTab {
        ERROR,
        INFO,
    }

}
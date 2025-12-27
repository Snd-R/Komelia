package snd.komelia.offline.sync.actions

import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.sync.model.OfflineLogEntry
import snd.komelia.offline.sync.repository.LogJournalRepository

class SyncEntrySaveAction(
    private val logJournalRepository: LogJournalRepository,
) : OfflineAction {

    suspend fun execute(entry: OfflineLogEntry) {
        logJournalRepository.save(entry)
    }
}
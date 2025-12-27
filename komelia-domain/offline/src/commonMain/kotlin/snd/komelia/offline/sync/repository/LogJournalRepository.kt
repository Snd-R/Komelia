package snd.komelia.offline.sync.repository

import snd.komelia.offline.sync.model.LogEntryId
import snd.komelia.offline.sync.model.OfflineLogEntry
import snd.komga.client.common.Page

interface LogJournalRepository {
    suspend fun save(entry: OfflineLogEntry)
    suspend fun get(id: LogEntryId): OfflineLogEntry

    suspend fun findAll(limit: Int, offset: Long): Page<OfflineLogEntry>
    suspend fun findAllByType(type: OfflineLogEntry.Type, limit: Int, offset: Long): Page<OfflineLogEntry>

    suspend fun deleteAll()}
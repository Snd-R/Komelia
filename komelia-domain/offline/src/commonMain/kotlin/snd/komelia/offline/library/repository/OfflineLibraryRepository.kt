package snd.komelia.offline.library.repository

import snd.komelia.offline.library.model.OfflineLibrary
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komga.client.library.KomgaLibraryId

interface OfflineLibraryRepository {
    suspend fun save(library: OfflineLibrary)
    suspend fun get(id: KomgaLibraryId): OfflineLibrary
    suspend fun find(id: KomgaLibraryId): OfflineLibrary?
    suspend fun findAll(): List<OfflineLibrary>
    suspend fun findAllByMediaServer(mediaServerId: OfflineMediaServerId): List<OfflineLibrary>
    suspend fun delete(id: KomgaLibraryId)
}
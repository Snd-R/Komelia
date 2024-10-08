package io.github.snd_r.komelia.offline.client

import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryClient
import snd.komga.client.library.KomgaLibraryCreateRequest
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.library.KomgaLibraryUpdateRequest

class OfflineLibraryClient : KomgaLibraryClient {
    override suspend fun addOne(request: KomgaLibraryCreateRequest): KomgaLibrary {
        TODO("Not yet implemented")
    }

    override suspend fun analyze(libraryId: KomgaLibraryId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteOne(libraryId: KomgaLibraryId) {
        TODO("Not yet implemented")
    }

    override suspend fun emptyTrash(libraryId: KomgaLibraryId) {
        TODO("Not yet implemented")
    }

    override suspend fun getLibraries(): List<KomgaLibrary> {
        TODO("Not yet implemented")
    }

    override suspend fun getLibrary(libraryId: KomgaLibraryId): KomgaLibrary {
        TODO("Not yet implemented")
    }

    override suspend fun patchOne(libraryId: KomgaLibraryId, request: KomgaLibraryUpdateRequest) {
        TODO("Not yet implemented")
    }

    override suspend fun refreshMetadata(libraryId: KomgaLibraryId) {
        TODO("Not yet implemented")
    }

    override suspend fun scan(libraryId: KomgaLibraryId, deep: Boolean) {
        TODO("Not yet implemented")
    }
}
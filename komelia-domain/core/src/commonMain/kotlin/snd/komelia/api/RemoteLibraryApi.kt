package snd.komelia.api

import snd.komelia.komga.api.KomgaLibraryApi
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryClient
import snd.komga.client.library.KomgaLibraryCreateRequest
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.library.KomgaLibraryUpdateRequest

class RemoteLibraryApi(private val libraryClient: KomgaLibraryClient) : KomgaLibraryApi {
    override suspend fun getLibraries(): List<KomgaLibrary> = libraryClient.getLibraries()

    override suspend fun getLibrary(libraryId: KomgaLibraryId) = libraryClient.getLibrary(libraryId)

    override suspend fun addOne(request: KomgaLibraryCreateRequest) = libraryClient.addOne(request)

    override suspend fun patchOne(libraryId: KomgaLibraryId, request: KomgaLibraryUpdateRequest) =
        libraryClient.patchOne(libraryId, request)

    override suspend fun deleteOne(libraryId: KomgaLibraryId) = libraryClient.deleteOne(libraryId)

    override suspend fun scan(libraryId: KomgaLibraryId, deep: Boolean) = libraryClient.scan(libraryId, deep)

    override suspend fun analyze(libraryId: KomgaLibraryId) = libraryClient.analyze(libraryId)

    override suspend fun refreshMetadata(libraryId: KomgaLibraryId) = libraryClient.refreshMetadata(libraryId)

    override suspend fun emptyTrash(libraryId: KomgaLibraryId) = libraryClient.emptyTrash(libraryId)
}
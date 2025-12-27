package snd.komelia.komga.api

import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryCreateRequest
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.library.KomgaLibraryUpdateRequest


interface KomgaLibraryApi {
    suspend fun getLibraries(): List<KomgaLibrary>
    suspend fun getLibrary(libraryId: KomgaLibraryId): KomgaLibrary
    suspend fun addOne(request: KomgaLibraryCreateRequest): KomgaLibrary
    suspend fun patchOne(libraryId: KomgaLibraryId, request: KomgaLibraryUpdateRequest)
    suspend fun deleteOne(libraryId: KomgaLibraryId)
    suspend fun scan(libraryId: KomgaLibraryId, deep: Boolean = false)
    suspend fun analyze(libraryId: KomgaLibraryId)
    suspend fun refreshMetadata(libraryId: KomgaLibraryId)
    suspend fun emptyTrash(libraryId: KomgaLibraryId)
}
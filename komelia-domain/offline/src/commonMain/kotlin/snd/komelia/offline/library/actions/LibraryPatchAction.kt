package snd.komelia.offline.library.actions

import snd.komelia.offline.action.OfflineAction
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.library.KomgaLibraryUpdateRequest

class LibraryPatchAction : OfflineAction {

    suspend fun run(
        libraryId: KomgaLibraryId,
        request: KomgaLibraryUpdateRequest
    ) {
        TODO()
    }
}
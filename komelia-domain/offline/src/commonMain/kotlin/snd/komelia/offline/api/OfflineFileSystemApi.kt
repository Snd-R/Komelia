package snd.komelia.offline.api

import snd.komelia.komga.api.KomgaFileSystemApi
import snd.komga.client.filesystem.DirectoryListing
import snd.komga.client.filesystem.DirectoryRequest

class OfflineFileSystemApi : KomgaFileSystemApi {
    override suspend fun getDirectoryListing(request: DirectoryRequest) =
        DirectoryListing(null, emptyList())
}
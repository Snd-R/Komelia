package io.github.snd_r.komelia.offline.client

import snd.komga.client.filesystem.DirectoryListing
import snd.komga.client.filesystem.DirectoryRequest
import snd.komga.client.filesystem.KomgaFileSystemClient

class OfflineFileSystemClient: KomgaFileSystemClient {
    override suspend fun getDirectoryListing(request: DirectoryRequest): DirectoryListing {
        TODO("Not yet implemented")
    }
}
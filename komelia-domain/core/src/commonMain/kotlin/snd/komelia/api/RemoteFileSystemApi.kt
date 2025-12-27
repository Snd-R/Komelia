package snd.komelia.api

import snd.komelia.komga.api.KomgaFileSystemApi
import snd.komga.client.filesystem.DirectoryRequest
import snd.komga.client.filesystem.KomgaFileSystemClient

class RemoteFileSystemApi(private val fileSystemClient: KomgaFileSystemClient) : KomgaFileSystemApi {
    override suspend fun getDirectoryListing(request: DirectoryRequest) = fileSystemClient.getDirectoryListing(request)
}
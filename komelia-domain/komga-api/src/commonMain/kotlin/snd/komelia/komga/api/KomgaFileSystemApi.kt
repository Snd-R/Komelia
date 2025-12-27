package snd.komelia.komga.api

import snd.komga.client.filesystem.DirectoryListing
import snd.komga.client.filesystem.DirectoryRequest


interface KomgaFileSystemApi {
    suspend fun getDirectoryListing(request: DirectoryRequest): DirectoryListing
}
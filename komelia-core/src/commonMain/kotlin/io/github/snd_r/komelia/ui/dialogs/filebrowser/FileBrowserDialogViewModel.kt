package io.github.snd_r.komelia.ui.dialogs.filebrowser

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.ktor.client.plugins.*
import snd.komga.client.common.toErrorResponse
import snd.komga.client.filesystem.DirectoryListing
import snd.komga.client.filesystem.DirectoryRequest
import snd.komga.client.filesystem.KomgaFileSystemClient

class FileBrowserDialogViewModel(
    private val filesystemClient: KomgaFileSystemClient,
    private val appNotifications: AppNotifications,
) {
    var selectedPath by mutableStateOf("/")
    var directoryListing by mutableStateOf<DirectoryListing?>(null)

    suspend fun selectDirectory(path: String) {
        try {
            directoryListing = filesystemClient.getDirectoryListing(DirectoryRequest(path))
            selectedPath = path
        } catch (exception: ClientRequestException) {
            appNotifications.add(AppNotification.Error(exception.toErrorResponse()?.message ?: exception.message))
        }
    }
}
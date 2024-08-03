package io.github.snd_r.komelia.ui.dialogs.filebrowser

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komga.common.toErrorResponse
import io.github.snd_r.komga.filesystem.DirectoryListing
import io.github.snd_r.komga.filesystem.DirectoryRequest
import io.github.snd_r.komga.filesystem.KomgaFileSystemClient
import io.ktor.client.plugins.*

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
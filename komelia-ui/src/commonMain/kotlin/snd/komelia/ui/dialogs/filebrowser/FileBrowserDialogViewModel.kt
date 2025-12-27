package snd.komelia.ui.dialogs.filebrowser

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.plugins.*
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaFileSystemApi
import snd.komga.client.common.toErrorResponse
import snd.komga.client.filesystem.DirectoryListing
import snd.komga.client.filesystem.DirectoryRequest

class FileBrowserDialogViewModel(
    private val filesystemApi: KomgaFileSystemApi,
    private val appNotifications: AppNotifications,
) {
    var selectedPath by mutableStateOf("/")
    var directoryListing by mutableStateOf<DirectoryListing?>(null)

    suspend fun selectDirectory(path: String) {
        try {
            directoryListing = filesystemApi.getDirectoryListing(DirectoryRequest(path))
            selectedPath = path
        } catch (exception: ClientRequestException) {
            appNotifications.add(AppNotification.Error(exception.toErrorResponse()?.message ?: exception.message))
        }
    }
}
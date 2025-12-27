package snd.komelia.ui.settings.offline.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import snd.komelia.formatDecimal
import snd.komelia.offline.sync.model.DownloadEvent
import snd.komelia.ui.dialogs.permissions.StoragePermissionRequestDialog
import snd.komga.client.book.KomgaBookId
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun OfflineDownloadsContent(
    storageLocation: PlatformFile?,

    onStorageLocationChange: (PlatformFile) -> Unit,
    onStorageLocationReset: () -> Unit,
    downloads: Collection<DownloadEvent>,
    onDownloadCancel: (KomgaBookId) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (storageLocation != null) {
            Column {
                Text("Storage location")
                Text(
                    rememberStorageLabel(storageLocation),
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }

        var showDirectoryPickerDialog by remember { mutableStateOf(false) }
        if (showDirectoryPickerDialog) {
            StoragePermissionRequestDialog { directory ->
                if (directory != null) {
                    onStorageLocationChange(directory)
                }
                showDirectoryPickerDialog = false
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { showDirectoryPickerDialog = true }) { Text("Change location") }
            Button(onClick = onStorageLocationReset) { Text("Reset to internal") }
        }

        HorizontalDivider()
        for (event in downloads) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(5.dp)
                    .fillMaxWidth()
            ) {
                when (event) {
                    is DownloadEvent.BookDownloadProgress -> DownloadProgress(event, onDownloadCancel)
                    is DownloadEvent.BookDownloadCompleted -> DownloadCompleted(event)
                    is DownloadEvent.BookDownloadError -> DownloadError(event)
                }
            }
        }
    }
}

@Composable
private fun DownloadProgress(
    event: DownloadEvent.BookDownloadProgress,
    onCancel: (KomgaBookId) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DownloadProgressIndicator(event)
        IconButton(onClick = { onCancel(event.book.id) }) { Icon(Icons.Default.Cancel, null) }
    }
}

@Composable
private fun RowScope.DownloadProgressIndicator(
    event: DownloadEvent.BookDownloadProgress,
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(event.book.metadata.title)
        if (event.total == 0L) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(
                progress = { event.completed / event.total.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )

            val totalMiB = remember(event.total) {
                (event.total.toFloat() / 1024 / 1024).formatDecimal(2)
            }
            val completedMiB = remember(event.completed) {
                (event.completed.toFloat() / 1024 / 1024).formatDecimal(2)
            }
            Text("${completedMiB}MiB / ${totalMiB}MiB")
        }
    }
}

@Composable
private fun DownloadCompleted(event: DownloadEvent.BookDownloadCompleted) {
    Column {
        Text(event.book.metadata.title)
        Text("Download Complete ")
    }
}

@Composable
private fun DownloadError(event: DownloadEvent.BookDownloadError) {
    Column {
        Text(event.book?.metadata?.title ?: event.bookId.value)
        val errorMessage = remember {
            if (event.error is CancellationException) "Cancelled"
            else "${event.error::class.simpleName}: ${event.error.message}"
        }
        Text(errorMessage, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
internal expect fun rememberStorageLabel(file: PlatformFile): String
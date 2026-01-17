package snd.komelia.offline.book.actions

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import snd.komelia.offline.action.OfflineAction
import java.io.File

actual class BookDeleteFilesAction : OfflineAction {
    private val mutex = Mutex()

    actual suspend fun execute(file: PlatformFile) {
        mutex.withLock {
            when (val androidFile = file.androidFile) {
                is AndroidFile.FileWrapper -> deleteFile(androidFile.file)
                is AndroidFile.UriWrapper -> deleteSafFile(androidFile.uri)
            }
        }
    }

    private fun deleteFile(file: File) {
        val seriesDir: File? = file.parentFile
        val libraryDir = seriesDir?.parentFile
        val serverDir = libraryDir?.parentFile

        if (file.exists()) {
            file.delete()
        }
        seriesDir?.deleteDirectoryIfEmpty()
        libraryDir?.deleteDirectoryIfEmpty()
        serverDir?.deleteDirectoryIfEmpty()
    }

    private fun File.deleteDirectoryIfEmpty() {
        if (this.exists() && this.listFiles()?.isEmpty() == true) {
            this.delete()
        }
    }

    private fun deleteSafFile(uri: Uri) {
        val context = FileKit.context
        val file = DocumentFile.fromTreeUri(context, uri) ?: error("Can't get file $uri")
        val seriesDir = file.parentFile
        val libraryDir = seriesDir?.parentFile
        val serverDir = libraryDir?.parentFile

        if (file.exists()) {
            file.delete()
        }
        seriesDir?.deleteDirectoryIfEmpty()
        libraryDir?.deleteDirectoryIfEmpty()
        serverDir?.deleteDirectoryIfEmpty()
    }

    private fun DocumentFile.deleteDirectoryIfEmpty() {
        if (this.exists() && this.listFiles().isEmpty()) {
            this.delete()
        }
    }
}
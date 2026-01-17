package snd.komelia.offline.book.actions

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import snd.komelia.offline.action.OfflineAction
import java.io.File

actual class BookDeleteFilesAction actual constructor(downloadsDirectory: Flow<PlatformFile>) : OfflineAction {
    private val mutex = Mutex()
    private val downloadRoot = downloadsDirectory

    actual suspend fun execute(file: PlatformFile) {
        mutex.withLock {
            when (val androidFile = file.androidFile) {
                is AndroidFile.FileWrapper -> deleteFile(androidFile.file)
                is AndroidFile.UriWrapper -> deleteSafFile(androidFile.uri)
            }

            val downloadRoot = downloadRoot.first().androidFile
            if (downloadRoot is AndroidFile.UriWrapper) {
                doSafDirectoriesCleanup(downloadRoot.uri)
            }
        }
    }

    // can't reliably get parent from book file uri
    // instead do lookup for empty series directories starting from root directory
    // server -> library -> series -> book
    private fun doSafDirectoriesCleanup(rootUri: Uri) {
        val root = DocumentFile.fromTreeUri(FileKit.context, rootUri)
            ?: error("Can't get directory $rootUri")

        for (serverDir in root.listFiles()) {
            for (libraryDir in serverDir.listFiles()) {
                for (seriesDir in libraryDir.listFiles()) {
                    if (seriesDir.listFiles().isEmpty()) {
                        seriesDir.delete()
                    }
                }

                if (libraryDir.listFiles().isEmpty()) {
                    libraryDir.delete()
                }
            }

            if (serverDir.listFiles().isEmpty()) {
                serverDir.delete()
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

        if (file.exists()) {
            file.delete()
        }
    }
}
package snd.komelia.offline.book.actions

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import snd.komelia.offline.action.OfflineAction
import java.io.File

actual class BookDeleteFilesAction actual constructor(downloadsDirectory: Flow<PlatformFile>) : OfflineAction {
    private val mutex = Mutex()

    actual suspend fun execute(file: PlatformFile) {
        mutex.withLock {
            val bookFile = file.file
            val seriesDir: File? = bookFile.parentFile
            val libraryDir = seriesDir?.parentFile
            val serverDir = libraryDir?.parentFile

            if (bookFile.exists()) {
                bookFile.delete()
            }
            seriesDir?.deleteDirectoryIfEmpty()
            libraryDir?.deleteDirectoryIfEmpty()
            serverDir?.deleteDirectoryIfEmpty()
        }
    }

    private fun File.deleteDirectoryIfEmpty() {
        if (this.exists() && this.listFiles()?.isEmpty() == true) {
            this.delete()
        }
    }
}
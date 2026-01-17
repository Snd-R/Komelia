package snd.komelia.offline.sync

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import io.github.vinceglb.filekit.delete
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Sink
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem


internal actual suspend fun prepareOutput(
    downloadRoot: PlatformFile,
    serverName: String,
    libraryName: String,
    seriesName: String,
    bookFileName: String,
): Pair<PlatformFile, Sink> {
    return when (val androidFile = downloadRoot.androidFile) {
        is AndroidFile.FileWrapper -> prepareFileSink(
            rootPath = Path(androidFile.file.path),
            serverName = serverName,
            libraryName = libraryName,
            seriesName = seriesName,
            bookFileName = bookFileName,
        )

        is AndroidFile.UriWrapper -> prepareSAFSink(
            rootUri = androidFile.uri,
            serverName = serverName,
            libraryName = libraryName,
            seriesName = seriesName,
            bookFileName = bookFileName,
        )
    }
}

internal actual suspend fun deleteFile(file: PlatformFile) {
    file.delete(false)
}

private fun prepareFileSink(
    rootPath: Path,
    serverName: String,
    libraryName: String,
    seriesName: String,
    bookFileName: String,
): Pair<PlatformFile, Sink> {
    val seriesDirectory = Path(rootPath, serverName, libraryName, seriesName)
    SystemFileSystem.createDirectories(seriesDirectory, mustCreate = false)

    val bookFile = Path(seriesDirectory, bookFileName)
    return PlatformFile(bookFile) to SystemFileSystem.sink(bookFile).buffered()
}

private val fileCreateMutex = Mutex()
private suspend fun prepareSAFSink(
    rootUri: Uri,
    serverName: String,
    libraryName: String,
    seriesName: String,
    bookFileName: String,
): Pair<PlatformFile, Sink> {
    val context = FileKit.context
    val tree = DocumentFile.fromTreeUri(context, rootUri)
        ?: error("Can't get document tree $rootUri")

    val bookFile = fileCreateMutex.withLock {
        val seriesDirectory = tree
            .createSafDirectoryOrThrow(serverName)
            .createSafDirectoryOrThrow(libraryName)
            .createSafDirectoryOrThrow(seriesName)

        val existingBookFile = seriesDirectory.listFiles().firstOrNull { it.isFile && it.name == bookFileName }
        existingBookFile?.delete()

        seriesDirectory.createFile("application/octet-stream", bookFileName)
            ?: error("Can't create file in directory $seriesDirectory")
    }

    val outputStream = context.contentResolver.openOutputStream(bookFile.uri)
        ?: error("Can't write to file $bookFile")

    return PlatformFile(rootUri) to outputStream.asSink().buffered()

}

private fun DocumentFile.createSafDirectoryOrThrow(directoryName: String): DocumentFile {
    return this.listFiles().firstOrNull { it.isDirectory && it.name == directoryName }
        ?: this.createDirectory(directoryName)
        ?: error("Can't create subdirectory in $directoryName")
}

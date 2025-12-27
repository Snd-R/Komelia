package snd.komelia.offline.sync

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import io.github.vinceglb.filekit.delete
import kotlinx.io.Sink
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import snd.komga.client.book.KomgaBook


internal actual suspend fun prepareOutput(
    book: KomgaBook,
    downloadPath: PlatformFile
): Pair<PlatformFile, Sink> {
    return when (val androidFile = downloadPath.androidFile) {
        is AndroidFile.FileWrapper -> prepareFileSink(Path(androidFile.file.path), book)
        is AndroidFile.UriWrapper -> prepareSAFSink(androidFile.uri, book)
    }
}

internal actual suspend fun deleteFile(file: PlatformFile) {
    file.delete(false)
}

private fun prepareFileSink(libraryPath: Path, book: KomgaBook): Pair<PlatformFile, Sink> {
    val seriesDirectory = Path(libraryPath, book.seriesId.value)
    SystemFileSystem.createDirectories(seriesDirectory, mustCreate = false)
    val bookFile = Path(seriesDirectory, book.id.value)

    return PlatformFile(bookFile) to SystemFileSystem.sink(bookFile).buffered()
}

private fun prepareSAFSink(uri: Uri, book: KomgaBook): Pair<PlatformFile, Sink> {
    val context = FileKit.context
    val tree = DocumentFile.fromTreeUri(context, uri)
        ?: error("Can't get document tree $uri")
    val seriesDirectory = tree.createDirectory(book.seriesId.value)
        ?: error("Can't create subdirectory in $uri")
    val bookFile = seriesDirectory.createFile("application/octet-stream", book.name)
        ?: error("Can't create file in directory $seriesDirectory")

    val uri = bookFile.uri
    val outputStream = context.contentResolver.openOutputStream(uri)
        ?: error("Can't write to file $bookFile")

    return PlatformFile(uri) to outputStream.asSink().buffered()
}

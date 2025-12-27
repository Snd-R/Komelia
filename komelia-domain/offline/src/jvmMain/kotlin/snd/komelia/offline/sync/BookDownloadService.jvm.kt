package snd.komelia.offline.sync

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import snd.komga.client.book.KomgaBook

internal actual suspend fun prepareOutput(
    book: KomgaBook,
    downloadPath: PlatformFile
): Pair<PlatformFile, Sink> {
    val libraryPath = Path(downloadPath.file.path)
    val seriesDirectory = Path(libraryPath, book.seriesId.value)
    SystemFileSystem.createDirectories(seriesDirectory, mustCreate = false)
    val bookFile = Path(seriesDirectory, book.id.value)

    return PlatformFile(bookFile) to SystemFileSystem.sink(bookFile).buffered()
}

internal actual suspend fun deleteFile(file: PlatformFile) {
    file.delete(false)
}
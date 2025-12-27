package snd.komelia.offline.sync

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.io.Sink
import snd.komga.client.book.KomgaBook

internal actual suspend fun prepareOutput(
    book: KomgaBook,
    downloadPath: PlatformFile
): Pair<PlatformFile, Sink> {
    TODO("Not yet implemented")
}

internal actual suspend fun deleteFile(file: PlatformFile) {
}
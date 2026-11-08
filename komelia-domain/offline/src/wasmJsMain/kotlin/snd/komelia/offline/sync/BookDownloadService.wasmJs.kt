package snd.komelia.offline.sync

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.io.Sink

internal actual suspend fun prepareOutput(
    downloadRoot: PlatformFile,
    serverName: String,
    libraryName: String,
    seriesName: String,
    bookFileName: String,
): Pair<PlatformFile, Sink> {
    TODO("Not yet implemented")
}

internal actual suspend fun deleteFile(file: PlatformFile) {
}

internal actual class ZipWriter actual constructor(output: Sink) {
    actual fun putEntry(name: String, bytes: ByteArray) {
        TODO("Not yet implemented")
    }

    actual fun finish() {
        TODO("Not yet implemented")
    }
}
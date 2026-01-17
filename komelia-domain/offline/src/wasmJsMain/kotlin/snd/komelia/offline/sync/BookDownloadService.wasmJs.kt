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
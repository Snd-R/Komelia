package io.github.snd_r.komelia.updates

import io.github.snd_r.komelia.AppDirectories.mangaJaNaiInstallPath
import io.github.snd_r.komelia.AppNotifications
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.io.readByteArray
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.io.IOUtils
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.notExists
import kotlin.io.path.outputStream

private const val downloadLink = "https://github.com/Snd-R/mangajanai/releases/download/1.0.0/MangaJaNaiOnnxModels.zip"

class MangaJaNaiDownloader(
    private val updateClient: UpdateClient,
    private val appNotifications: AppNotifications
) {
    val downloadCompletionEventFlow = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        .also { it.tryEmit(Unit) }

    fun download(): Flow<UpdateProgress> {
        return flow {
            if (mangaJaNaiInstallPath.notExists()) {
                mangaJaNaiInstallPath.createDirectories()
            }

            emit(UpdateProgress(0, 0, "MangaJaNaiOnnxModels.zip"))
            val archiveFile = createTempFile("MangaJaNaiOnnxModels.zip")
            archiveFile.toFile().deleteOnExit()

            appNotifications.runCatchingToNotifications {
                downloadFile(archiveFile)
                emit(UpdateProgress(0, 0))
                extractZipArchive(archiveFile)
                archiveFile.deleteIfExists()
                downloadCompletionEventFlow.emit(Unit)
            }.onFailure { archiveFile.deleteIfExists() }
        }
    }

    private suspend fun FlowCollector<UpdateProgress>.downloadFile(file: Path) {
        updateClient.streamFile(downloadLink) { response ->
            val length = response.headers["Content-Length"]?.toLong() ?: 0L
            emit(UpdateProgress(length, 0, "MangaJaNaiOnnxModels.zip"))
            val channel = response.bodyAsChannel().counted()

            file.outputStream().buffered().use { outputStream ->
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    while (!packet.exhausted()) {
                        val bytes = packet.readByteArray()
                        outputStream.write(bytes)
                    }
                    outputStream.flush()
                    emit(UpdateProgress(length, channel.totalBytesRead, "MangaJaNaiOnnxModels.zip"))
                }
            }
        }
    }

    private fun extractZipArchive(path: Path) {
        ZipArchiveInputStream(path.inputStream().buffered()).use { archiveStream ->
            var entry: ZipArchiveEntry? = archiveStream.nextEntry
            while (entry != null) {
                val filename = Path(entry.name).fileName.toString()
                mangaJaNaiInstallPath.resolve(filename).outputStream()
                    .use { output -> IOUtils.copy(archiveStream, output) }
                entry = archiveStream.nextEntry
            }
        }
    }
}
package io.github.snd_r.komelia.updates

import io.github.snd_r.komelia.AppDirectories.mangaJaNaiInstallPath
import io.github.snd_r.komelia.AppDirectories.panelDetectionInstallPath
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.updates.OnnxModelDownloader.CompletionEvent
import io.github.snd_r.komelia.updates.OnnxModelDownloader.CompletionEvent.MangaJaNaiDownloaded
import io.github.snd_r.komelia.updates.OnnxModelDownloader.CompletionEvent.PanelModelDownloaded
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.counted
import io.ktor.utils.io.readRemaining
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

private const val mangaJaNaiDownloadLink =
    "https://github.com/Snd-R/mangajanai/releases/download/1.0.0/MangaJaNaiOnnxModels.zip"
private const val panelDetectionModelLink =
    "https://github.com/Snd-R/komelia-onnxruntime/releases/download/model/rf-detr-med.onnx.zip"

class DesktopOnnxModelDownloader(
    private val updateClient: UpdateClient,
    private val appNotifications: AppNotifications
) : OnnxModelDownloader {
    override val downloadCompletionEvents = MutableSharedFlow<CompletionEvent>()

    override fun mangaJaNaiDownload(): Flow<UpdateProgress> {
        return flow {
            if (mangaJaNaiInstallPath.notExists()) {
                mangaJaNaiInstallPath.createDirectories()
            }

            emit(UpdateProgress(0, 0, mangaJaNaiDownloadLink))
            val archiveFile = createTempFile("MangaJaNaiOnnxModels.zip")
            archiveFile.toFile().deleteOnExit()

            appNotifications.runCatchingToNotifications {
                downloadFile(mangaJaNaiDownloadLink, archiveFile)
                emit(UpdateProgress(0, 0))
                extractZipArchive(from = archiveFile, to = mangaJaNaiInstallPath)
                archiveFile.deleteIfExists()
                downloadCompletionEvents.emit(MangaJaNaiDownloaded)
            }.onFailure { archiveFile.deleteIfExists() }
        }
    }

    override fun panelDownload(): Flow<UpdateProgress> {
        return flow {
            if (panelDetectionInstallPath.notExists()) {
                panelDetectionInstallPath.createDirectories()
            }

            emit(UpdateProgress(0, 0, panelDetectionModelLink))
            val archiveFile = createTempFile("rf-detr-med.onnx.zip")
            archiveFile.toFile().deleteOnExit()

            appNotifications.runCatchingToNotifications {
                downloadFile(panelDetectionModelLink, archiveFile)
                emit(UpdateProgress(0, 0))
                extractZipArchive(archiveFile, panelDetectionInstallPath)
                archiveFile.deleteIfExists()
                downloadCompletionEvents.emit(PanelModelDownloaded)
            }.onFailure { archiveFile.deleteIfExists() }
        }
    }

    private suspend fun FlowCollector<UpdateProgress>.downloadFile(url: String, file: Path) {
        updateClient.streamFile(url) { response ->
            val length = response.headers["Content-Length"]?.toLong() ?: 0L
            emit(UpdateProgress(length, 0, url))
            val channel = response.bodyAsChannel().counted()

            file.outputStream().buffered().use { outputStream ->
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    while (!packet.exhausted()) {
                        val bytes = packet.readByteArray()
                        outputStream.write(bytes)
                    }
                    outputStream.flush()
                    emit(UpdateProgress(length, channel.totalBytesRead, url))
                }
            }
        }
    }

    private fun extractZipArchive(from: Path, to: Path) {
        ZipArchiveInputStream(from.inputStream().buffered()).use { archiveStream ->
            var entry: ZipArchiveEntry? = archiveStream.nextEntry
            while (entry != null) {
                val filename = Path(entry.name).fileName.toString()
                to.resolve(filename).outputStream()
                    .use { output -> IOUtils.copy(archiveStream, output) }
                entry = archiveStream.nextEntry
            }
        }
    }
}
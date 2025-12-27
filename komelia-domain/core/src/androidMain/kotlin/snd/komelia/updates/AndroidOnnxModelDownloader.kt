package snd.komelia.updates

import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.io.readByteArray
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.io.IOUtils
import snd.komelia.AppNotifications
import snd.komelia.updates.OnnxModelDownloader.CompletionEvent
import snd.komelia.updates.OnnxModelDownloader.CompletionEvent.PanelModelDownloaded
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

private const val panelDetectionModelLink =
    "https://github.com/Snd-R/komelia-onnxruntime/releases/download/model/rf-detr-nano.onnx.zip"

class AndroidOnnxModelDownloader(
    private val updateClient: UpdateClient,
    private val appNotifications: AppNotifications,
    private val dataDir: Path,
) : OnnxModelDownloader {
    override val downloadCompletionEvents = MutableSharedFlow<CompletionEvent>()

    override fun mangaJaNaiDownload(): Flow<UpdateProgress> {
        return emptyFlow()
    }

    override fun panelDownload(): Flow<UpdateProgress> {
        return flow {

            emit(UpdateProgress(0, 0, panelDetectionModelLink))
            val archiveFile = createTempFile("rf-detr-nano.onnx.zip")
            archiveFile.toFile().deleteOnExit()

            appNotifications.runCatchingToNotifications {
                downloadFile(panelDetectionModelLink, archiveFile)
                emit(UpdateProgress(0, 0))
                extractZipArchive(archiveFile, dataDir)
                archiveFile.deleteIfExists()
                downloadCompletionEvents.emit(PanelModelDownloaded)
            }.onFailure { archiveFile.deleteIfExists() }
        }.flowOn(Dispatchers.IO)
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
package io.github.snd_r.komelia.updates

import dev.dirs.ProjectDirectories
import io.github.snd_r.VipsOnnxRuntimeDecoder.OnnxRuntimeExecutionProvider
import io.github.snd_r.VipsOnnxRuntimeDecoder.OnnxRuntimeExecutionProvider.CPU
import io.github.snd_r.VipsOnnxRuntimeDecoder.OnnxRuntimeExecutionProvider.CUDA
import io.github.snd_r.VipsOnnxRuntimeDecoder.OnnxRuntimeExecutionProvider.DirectML
import io.github.snd_r.VipsOnnxRuntimeDecoder.OnnxRuntimeExecutionProvider.ROCm
import io.github.snd_r.komelia.DesktopPlatform
import io.github.snd_r.komelia.DesktopPlatform.Linux
import io.github.snd_r.komelia.DesktopPlatform.MacOS
import io.github.snd_r.komelia.DesktopPlatform.Unknown
import io.github.snd_r.komelia.DesktopPlatform.Windows
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.outputStream
import kotlin.io.use

const val onnxRuntimeTagName = "v1.18.0"
const val linuxCudaAssetName = "onnxruntime-linux-x64-gpu-cuda12-1.18.0.tgz"
const val linuxRocmAssetName = "onnxruntime-linux-x64-rocm-1.18.0.tgz"
const val linuxCPUAssetName = "onnxruntime-linux-x64-1.18.0.tgz"

const val windowsCudaAssetName = "onnxruntime-win-x64-gpu-cuda12-1.18.0.zip"
const val windowsDirectMLAssetName = "Microsoft.ML.OnnxRuntime.DirectML.1.18.0.zip"

private val linuxOnnxruntimeLibName = "libonnxruntime.so.1.18.0"
private val linuxLibs = listOf(
    linuxOnnxruntimeLibName,
    "libonnxruntime_providers_shared.so",
    "libonnxruntime_providers_cuda.so",
    "libonnxruntime_providers_rocm.so",
)
private val windowsLibs = listOf(
    "onnxruntime.dll",
    "onnxruntime_providers_shared.dll",
    "onnxruntime_providers_cuda.dll",
)

class OnnxRuntimeInstaller(private val githubClient: GithubClient) {
    private val installDir = Path(ProjectDirectories.from("io.github.snd-r.komelia", "", "Komelia").dataDir)
        .resolve("onnxruntime")
        .createDirectories()

    suspend fun install(provider: OnnxRuntimeExecutionProvider): Flow<UpdateProgress> {
        val release = githubClient.getOnnxRuntimeRelease(onnxRuntimeTagName)
        val asset = when (DesktopPlatform.Current) {
            Linux -> getLinuxAsset(release.assets, provider)
            Windows -> getWindowsAsset(release.assets, provider)
            MacOS, Unknown -> error("Unsupported OS")
        }

        return flow {
            emit(UpdateProgress(0, 0, asset.name))
            val tempFile = createTempFile(asset.name)

            githubClient.streamFile(asset.browserDownloadUrl) { response ->
                val length = response.headers["Content-Length"]?.toLong() ?: 0L
                emit(UpdateProgress(length, 0, asset.name))
                val channel = response.bodyAsChannel()

                tempFile.outputStream().buffered().use { outputStream ->
                    while (!channel.isClosedForRead) {
                        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                        while (!packet.isEmpty) {
                            val bytes = packet.readBytes()
                            outputStream.write(bytes)
                        }
                        outputStream.flush()
                        emit(UpdateProgress(length, channel.totalBytesRead, asset.name))
                    }
                }
            }
            installDir.listDirectoryEntries().filter { !it.isDirectory() }.forEach { it.deleteExisting() }

            emit(UpdateProgress(0, 0, "Extracting Archive"))

            val libraryNames = when (DesktopPlatform.Current) {
                Linux -> linuxLibs
                Windows -> windowsLibs
                else -> error("Unsupported OS")
            }
            if (asset.name.endsWith(".tgz")) {
                extractTarArchive(tempFile, libraryNames)
            } else {
                extractZipArchive(tempFile, libraryNames)
            }

        }.flowOn(Dispatchers.IO)
    }

    private fun extractTarArchive(path: Path, entryNames: List<String>) {
        TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(path.inputStream()))).use { archiveStream ->
            var entry: TarArchiveEntry? = archiveStream.nextEntry
            while (entry != null) {
                val filename = Path(entry.name).fileName.toString()

                if (filename in entryNames) {
                    installDir.resolve(filename).outputStream().use { output ->
                        IOUtils.copy(archiveStream, output)
                    }
                }
                entry = archiveStream.nextEntry
            }
        }

        if (DesktopPlatform.Current == Linux) {
            val symlinkPath = installDir.resolve("libonnxruntime.so")
            symlinkPath.deleteIfExists()
            Files.createSymbolicLink(symlinkPath, installDir.resolve(linuxOnnxruntimeLibName))
        }
    }

    private fun extractZipArchive(path: Path, entryNames: List<String>) {
        ZipFile.Builder().setPath(path).get().use { file ->
            file.entries.asSequence()
                .filter { Path(it.name).fileName.toString() in entryNames }
                .forEach { entry ->
                    file.getInputStream(entry).use { inputStream ->
                        ZipOutputStream(Files.newOutputStream(installDir.resolve(Path(entry.name).fileName))).use { outputStream ->
                            IOUtils.copy(inputStream, outputStream)
                        }
                    }
                }
        }
    }

    private fun getWindowsAsset(
        assets: List<GithubReleaseAsset>,
        provider: OnnxRuntimeExecutionProvider
    ): GithubReleaseAsset {
        val name = when (provider) {
            CUDA -> windowsCudaAssetName
            ROCm -> error("ROCm is unsupported on Windows")
            CPU -> windowsDirectMLAssetName
            DirectML -> windowsDirectMLAssetName
        }

        return assets.first { it.name == name }
    }

    private fun getLinuxAsset(
        assets: List<GithubReleaseAsset>,
        provider: OnnxRuntimeExecutionProvider
    ): GithubReleaseAsset {
        val name = when (provider) {
            CUDA -> linuxCudaAssetName
            ROCm -> linuxRocmAssetName
            CPU -> linuxCPUAssetName
            DirectML -> error("DirectML is unsupported on Linux")
        }
        return assets.first { it.name == name }
    }
}
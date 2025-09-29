package io.github.snd_r.komelia.updates

import io.github.snd_r.komelia.AppDirectories.onnxRuntimeInstallPath
import io.github.snd_r.komelia.DesktopPlatform
import io.github.snd_r.komelia.DesktopPlatform.Linux
import io.github.snd_r.komelia.DesktopPlatform.MacOS
import io.github.snd_r.komelia.DesktopPlatform.Unknown
import io.github.snd_r.komelia.DesktopPlatform.Windows
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.counted
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.io.readByteArray
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.IOUtils
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CUDA
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.DirectML
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.ROCm
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.TENSOR_RT
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.WEBGPU
import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.outputStream


class DesktopOnnxRuntimeInstaller(private val updateClient: UpdateClient) : OnnxRuntimeInstaller {

    override fun install(provider: OnnxRuntimeExecutionProvider): Flow<UpdateProgress> {
        onnxRuntimeInstallPath.createDirectories()

        return flow {
            val downloadInfo = when (provider) {
                TENSOR_RT, CUDA -> getCudaDownloadInfo(withTRT = provider == TENSOR_RT)
                CPU -> getCpuDownloadInfo()
                DirectML -> getDirectMlDownloadInfo()
                ROCm -> getROCmDownloadInfo()
                WEBGPU -> getWebGpuDownloadInfo()
            }

            emit(UpdateProgress(0, 0, downloadInfo.filename))
            val onnxRuntimeFile = createTempFile(downloadInfo.filename)

            updateClient.streamFile(downloadInfo.downloadUrl) {
                downloadToFile(
                    it,
                    onnxRuntimeFile,
                    downloadInfo.downloadUrl
                )
            }
            onnxRuntimeInstallPath.listDirectoryEntries().filter { !it.isDirectory() }.forEach { it.deleteExisting() }

            emit(UpdateProgress(0, 0, "Extracting Archive"))

            if (downloadInfo.filename.endsWith(".tgz")) {
                extractTarArchive(onnxRuntimeFile, downloadInfo.extractFiles)
            } else {
                extractZipArchive(onnxRuntimeFile, downloadInfo.extractFiles)
            }
            if (DesktopPlatform.Current == Linux && provider == ROCm) createOrtSymlinks()

            val directMlDownloadFilename = "microsoft.ai.directml.1.15.4.nupkg"
            val directMlLink = "https://globalcdn.nuget.org/packages/$directMlDownloadFilename"
            val directMlDllPath = Path("bin/x64-win/DirectML.dll")
            if (provider == DirectML) {
                val directMlFile = createTempFile(directMlDownloadFilename)
                updateClient.streamFile(directMlLink) { downloadToFile(it, directMlFile, directMlDownloadFilename) }
                extractZipArchive(directMlFile, listOf(directMlDllPath))
                directMlFile.deleteIfExists()
            }

            onnxRuntimeFile.deleteIfExists()
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun FlowCollector<UpdateProgress>.downloadToFile(
        response: HttpResponse,
        outFile: Path,
        filename: String?,
    ) {
        val length = response.headers["Content-Length"]?.toLong() ?: 0L
        emit(UpdateProgress(length, 0, filename))
        val channel = response.bodyAsChannel().counted()

        outFile.outputStream().buffered().use { outputStream ->
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.exhausted()) {
                    val bytes = packet.readByteArray()
                    outputStream.write(bytes)
                }
                outputStream.flush()
                emit(UpdateProgress(length, channel.totalBytesRead, filename))
            }
        }
    }

    private fun createOrtSymlinks() {
        val link1 = onnxRuntimeInstallPath.resolve("libonnxruntime.so").also { it.deleteIfExists() }
        val link2 = onnxRuntimeInstallPath.resolve("libonnxruntime.so.1").also { it.deleteIfExists() }

        val versionedSoFile = onnxRuntimeInstallPath.listDirectoryEntries()
            .firstOrNull {
                it.name.startsWith("libonnxruntime.so")
            }
        check(versionedSoFile != null) { "Failed to find downloaded libonnxruntime.so file" }

        Files.createSymbolicLink(link1, versionedSoFile)
        Files.createSymbolicLink(link2, versionedSoFile)
    }

    private fun extractTarArchive(path: Path, entryNames: List<Path>) {
        TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(path.inputStream()))).use {
            extractArchiveEntries(it, entryNames)
        }
    }

    private fun extractZipArchive(path: Path, entryNames: List<Path>) {
        ZipArchiveInputStream(path.inputStream().buffered()).use {
            extractArchiveEntries(it, entryNames)
        }
    }

    private fun extractArchiveEntries(archiveStream: ArchiveInputStream<*>, entryNames: List<Path>) {
        var entry = archiveStream.nextEntry
        while (entry != null) {
            val filename = Path(entry.name).fileName.toString()

            if (Path(entry.name) in entryNames) {
                onnxRuntimeInstallPath.resolve(filename).outputStream().use { output ->
                    IOUtils.copy(archiveStream, output)
                }
            }
            entry = archiveStream.nextEntry
        }
    }

    private fun getCudaDownloadInfo(withTRT: Boolean): OnnxRuntimeDownloadInfo {
        return when (DesktopPlatform.Current) {
            Linux -> {
                val basePath = Path("runtimes/linux-x64/native")
                OnnxRuntimeDownloadInfo(
                    "microsoft.ml.onnxruntime.gpu.linux.1.23.0.nupkg",
                    "https://globalcdn.nuget.org/packages/microsoft.ml.onnxruntime.gpu.linux.1.23.0.nupkg",
                    listOfNotNull(
                        "libonnxruntime.so",
                        "libonnxruntime.so",
                        "libonnxruntime_providers_cuda.so",
                        "libonnxruntime_providers_shared.so",
                        if (withTRT) "libonnxruntime_providers_tensorrt.so" else null,
                    ).map { basePath.resolve(it) }
                )
            }

            Windows -> {
                val basePath = Path("runtimes/win-x64/native")
                OnnxRuntimeDownloadInfo(
                    "microsoft.ml.onnxruntime.gpu.windows.1.23.0.nupkg",
                    "https://globalcdn.nuget.org/packages/microsoft.ml.onnxruntime.gpu.windows.1.23.0.nupkg",
                    listOfNotNull(
                        "onnxruntime.dll",
                        "onnxruntime_providers_cuda.dll",
                        "onnxruntime_providers_shared.dll",
                        if (withTRT) "onnxruntime_providers_tensorrt.dll" else null,
                    ).map { basePath.resolve(it) }
                )
            }

            MacOS, Unknown -> error("Unsupported OS")
        }
    }

    private fun getDirectMlDownloadInfo() = OnnxRuntimeDownloadInfo(
        "microsoft.ml.onnxruntime.directml.1.23.0.nupkg",
        "https://globalcdn.nuget.org/packages/microsoft.ml.onnxruntime.directml.1.23.0.nupkg",
        listOf(Path("runtimes/win-x64/native/onnxruntime.dll"))
    )


    private suspend fun getCpuDownloadInfo(): OnnxRuntimeDownloadInfo {
        val version = "1.20.1"
        val release = updateClient.getOnnxRuntimeRelease("v$version")
        return when (DesktopPlatform.Current) {
            Linux -> {
                val asset = release.assets.first { it.name == "onnxruntime-linux-x64-$version.tgz" }
                val basePath = Path("onnxruntime-linux-x64-$version/lib")
                OnnxRuntimeDownloadInfo(
                    asset.name,
                    asset.browserDownloadUrl,
                    listOf(
                        "libonnxruntime.so",
                        "libonnxruntime.so.1",
                        "libonnxruntime.so.$version",
                        "libonnxruntime_providers_shared.so",
                    ).map { basePath.resolve(it) }
                )
            }

            Windows -> getDirectMlDownloadInfo()
            MacOS, Unknown -> error("Unsupported OS")
        }
    }

    private fun getROCmDownloadInfo(): OnnxRuntimeDownloadInfo {
        check(DesktopPlatform.Current == Linux) { "ROCm is only supported on Linux" }

        val hipBlas = Path("onnxruntime_rocm.libs/libhipblas-7909492e.so.3.0.70000")
        val basePath = Path("onnxruntime/capi")
        return OnnxRuntimeDownloadInfo(
            filename = "onnxruntime_rocm-1.22.1-cp312-cp312-manylinux_2_27_x86_64.manylinux_2_28_x86_64.whl",
            downloadUrl = "https://repo.radeon.com/rocm/manylinux/rocm-rel-7.0/onnxruntime_rocm-1.22.1-cp312-cp312-manylinux_2_27_x86_64.manylinux_2_28_x86_64.whl",
            extractFiles = listOf(
                "libonnxruntime.so.1.22.1",
                "libonnxruntime_providers_migraphx.so",
                "libonnxruntime_providers_rocm.so",
                "libonnxruntime_providers_shared.so",
            ).map { basePath.resolve(it) } + listOf(hipBlas)
        )
    }

    private fun getWebGpuDownloadInfo(): OnnxRuntimeDownloadInfo {

        check(DesktopPlatform.Current == Linux) { "WebGPU is only supported on Linux" }

        return OnnxRuntimeDownloadInfo(
            filename = "libnonnxruntime.zip",
            downloadUrl = "https://github.com/Snd-R/komelia-onnxruntime/releases/download/webgpu/libonnxruntime.zip",
            extractFiles = listOf(
                Path("libonnxruntime.so"),
                Path("libonnxruntime_providers_shared.so")
            )
        )
    }

    private data class OnnxRuntimeDownloadInfo(
        val filename: String,
        val downloadUrl: String,
        val extractFiles: List<Path>
    )
}
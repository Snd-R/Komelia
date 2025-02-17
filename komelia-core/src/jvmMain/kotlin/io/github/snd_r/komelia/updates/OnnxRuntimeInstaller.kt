package io.github.snd_r.komelia.updates

import io.github.snd_r.komelia.AppDirectories.onnxRuntimeInstallPath
import io.github.snd_r.komelia.DesktopPlatform
import io.github.snd_r.komelia.DesktopPlatform.Linux
import io.github.snd_r.komelia.DesktopPlatform.MacOS
import io.github.snd_r.komelia.DesktopPlatform.Unknown
import io.github.snd_r.komelia.DesktopPlatform.Windows
import io.ktor.client.statement.*
import io.ktor.utils.io.*
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
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.CUDA
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.DirectML
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.ROCm
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.TENSOR_RT
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


class OnnxRuntimeInstaller(private val updateClient: UpdateClient) {


    suspend fun install(provider: OnnxRuntimeExecutionProvider): Flow<UpdateProgress> {
        onnxRuntimeInstallPath.createDirectories()

        val downloadInfo = when (provider) {
            TENSOR_RT, CUDA -> getCudaDownloadInfo()
            CPU -> getCpuDownloadInfo()
            DirectML -> getDirectMlDownloadInfo()
            ROCm -> getROCmDownloadInfo()
        }

        return flow {
            emit(UpdateProgress(0, 0, downloadInfo.filename))
            val onnxRuntimeFile = createTempFile(downloadInfo.filename)

            updateClient.streamFile(downloadInfo.downloadUrl) {
                downloadToFile(
                    it,
                    onnxRuntimeFile,
                    downloadInfo.filename
                )
            }
            onnxRuntimeInstallPath.listDirectoryEntries().filter { !it.isDirectory() }.forEach { it.deleteExisting() }

            emit(UpdateProgress(0, 0, "Extracting Archive"))

            if (downloadInfo.filename.endsWith(".tgz")) {
                extractTarArchive(onnxRuntimeFile, downloadInfo.extractFiles)
            } else {
                extractZipArchive(onnxRuntimeFile, downloadInfo.extractFiles)
            }
            if (DesktopPlatform.Current == Linux) createOrtSymlinks()

            val directMlDownloadFilename = "microsoft.ai.directml.1.15.2.nupkg"
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
                println(it.name)
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

//        if (DesktopPlatform.Current == Linux) {
//            val symlinkPath = onnxRuntimeInstallPath.resolve("libonnxruntime.so")
//            symlinkPath.deleteIfExists()
//            val linuxLibName =
//                if (provider == CUDA || provider == TENSOR_RT) getLinuxOnnxruntimeLib(onnxRuntimeVersion)
//                else getLinuxOnnxruntimeLib(onnxRuntimeVersion)
//
//            Files.createSymbolicLink(symlinkPath, onnxRuntimeInstallPath.resolve(linuxLibName))
//        }
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

    private suspend fun getCudaDownloadInfo(): OnnxRuntimeDownloadInfo {
        val version = "1.20.1"
        val release = updateClient.getOnnxRuntimeRelease("v$version")
        return when (DesktopPlatform.Current) {
            Linux -> {
                val asset = release.assets.first { it.name == "onnxruntime-linux-x64-gpu-$version.tgz" }
                val basePath = Path("onnxruntime-linux-x64-gpu-$version/lib")
                OnnxRuntimeDownloadInfo(
                    asset.name,
                    asset.browserDownloadUrl,
                    listOf(
                        "libonnxruntime.so",
                        "libonnxruntime.so.1",
                        "libonnxruntime.so.$version",
                        "libonnxruntime_providers_cuda.so",
                        "libonnxruntime_providers_shared.so",
                        "libonnxruntime_providers_tensorrt.so",
                    ).map { basePath.resolve(it) }
                )
            }

            Windows -> {
                val asset = release.assets.first { it.name == "onnxruntime-win-x64-gpu-$version.zip" }
                val basePath = Path("onnxruntime-win-x64-gpu-$version/lib")
                OnnxRuntimeDownloadInfo(
                    asset.name,
                    asset.browserDownloadUrl,
                    listOf(
                        "onnxruntime.dll",
                        "onnxruntime_providers_cuda.dll",
                        "onnxruntime_providers_shared.dll",
                        "onnxruntime_providers_tensorrt.dll",
                    ).map { basePath.resolve(it) }
                )
            }

            MacOS, Unknown -> error("Unsupported OS")
        }
    }

    private fun getDirectMlDownloadInfo() = OnnxRuntimeDownloadInfo(
        "microsoft.ml.onnxruntime.directml.1.20.1.nupkg",
        "https://globalcdn.nuget.org/packages/microsoft.ml.onnxruntime.directml.1.20.1.nupkg?packageVersion=1.20.1",
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

        val basePath = Path("onnxruntime/capi")
        return OnnxRuntimeDownloadInfo(
            filename = "onnxruntime_rocm-1.19.0-cp312-cp312-linux_x86_64.whl",
            downloadUrl = "https://repo.radeon.com/rocm/manylinux/rocm-rel-6.3.2/onnxruntime_rocm-1.19.0-cp312-cp312-linux_x86_64.whl",
            extractFiles = listOf(
                "libonnxruntime.so.1.19.0",
                "libonnxruntime_providers_migraphx.so",
                "libonnxruntime_providers_rocm.so",
                "libonnxruntime_providers_shared.so",
            ).map { basePath.resolve(it) }
        )
    }


    private data class OnnxRuntimeDownloadInfo(
        val filename: String,
        val downloadUrl: String,
        val extractFiles: List<Path>
    )
}
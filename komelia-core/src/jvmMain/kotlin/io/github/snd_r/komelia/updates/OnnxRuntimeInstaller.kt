package io.github.snd_r.komelia.updates

import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.CUDA
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.DirectML
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.ROCm
import snd.komelia.image.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.TENSOR_RT
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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.IOUtils
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
import kotlin.io.path.outputStream


class OnnxRuntimeInstaller(private val updateClient: UpdateClient) {
    private val onnxRuntimeTagName = "v1.18.1"
    private val onnxRuntimeVersion = "1.18.1"
    private val onnxRuntimeTagNameDirectMl = "v1.18.0"
    private val onnxRuntimeVersionDirectMl = "1.18.0"

    private val linuxCudaAssetName = "onnxruntime-linux-x64-gpu-cuda12-$onnxRuntimeVersion.tgz"
    private val linuxCudaLibPath = Path("onnxruntime-linux-x64-gpu-$onnxRuntimeVersion/lib/")
    private val windowsCudaAssetName = "onnxruntime-win-x64-gpu-cuda12-$onnxRuntimeVersion.zip"
    private val windowsCudaLibPath = Path("onnxruntime-win-x64-gpu-$onnxRuntimeVersion/lib/")

//    private val linuxRocmAssetName = "onnxruntime-linux-x64-rocm-$onnxRuntimeVersion.tgz"
//    private val linuxRocmLibPath = Path("onnxruntime-linux-x64-rocm-$onnxRuntimeVersion/lib/")

    private val linuxCPUAssetName = "onnxruntime-linux-x64-$onnxRuntimeVersion.tgz"
    private val linuxCpuLibPath = Path("onnxruntime-linux-x64-$onnxRuntimeVersion/lib/")

    private val windowsDirectMLAssetName = "Microsoft.ML.OnnxRuntime.DirectML.$onnxRuntimeVersionDirectMl.zip"
    private val windowsDirectMlLibPath = Path("runtimes/win-x64/native/")

    private val directMlDownloadFilename = "microsoft.ai.directml.1.15.0.nupkg"
    private val directMlLink = "https://globalcdn.nuget.org/packages/$directMlDownloadFilename"
    private val directMlDllPath = Path("bin/x64-win/DirectML.dll")

    suspend fun install(provider: OnnxRuntimeExecutionProvider): Flow<UpdateProgress> {
        onnxRuntimeInstallPath.createDirectories()

        val release = when (provider) {
            TENSOR_RT, CUDA, CPU -> updateClient.getOnnxRuntimeRelease(onnxRuntimeTagName)
            DirectML -> updateClient.getOnnxRuntimeRelease(onnxRuntimeTagNameDirectMl)
            ROCm -> error("Unsupported")
        }
        val githubAsset = when (DesktopPlatform.Current) {
            Linux -> getLinuxAsset(release.assets, provider)
            Windows -> getWindowsAsset(release.assets, provider)
            MacOS, Unknown -> error("Unsupported OS")
        }

        return flow {
            emit(UpdateProgress(0, 0, githubAsset.filename))
            val onnxruntimeFile = createTempFile(githubAsset.filename)

            updateClient.streamFile(githubAsset.downloadUrl) {
                downloadToFile(
                    it,
                    onnxruntimeFile,
                    githubAsset.filename
                )
            }
            onnxRuntimeInstallPath.listDirectoryEntries().filter { !it.isDirectory() }.forEach { it.deleteExisting() }

            emit(UpdateProgress(0, 0, "Extracting Archive"))

            if (githubAsset.filename.endsWith(".tgz")) {
                extractTarArchive(onnxruntimeFile, githubAsset.extractPaths, provider)
            } else {
                extractZipArchive(onnxruntimeFile, githubAsset.extractPaths)
            }

            if (provider == DirectML) {
                val directMlFile = createTempFile(directMlDownloadFilename)
                updateClient.streamFile(directMlLink) { downloadToFile(it, directMlFile, directMlDownloadFilename) }
                extractZipArchive(directMlFile, listOf(directMlDllPath))
                directMlFile.deleteIfExists()
            }

            onnxruntimeFile.deleteIfExists()
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

    private fun extractTarArchive(path: Path, entryNames: List<Path>, provider: OnnxRuntimeExecutionProvider) {
        TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(path.inputStream()))).use { archiveStream ->
            var entry: TarArchiveEntry? = archiveStream.nextEntry
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

        if (DesktopPlatform.Current == Linux) {
            val symlinkPath = onnxRuntimeInstallPath.resolve("libonnxruntime.so")
            symlinkPath.deleteIfExists()
            val linuxLibName =
                if (provider == CUDA || provider == TENSOR_RT) getLinuxOnnxruntimeLib(onnxRuntimeVersion)
                else getLinuxOnnxruntimeLib(onnxRuntimeVersion)

            Files.createSymbolicLink(symlinkPath, onnxRuntimeInstallPath.resolve(linuxLibName))
        }
    }

    private fun extractZipArchive(path: Path, entryNames: List<Path>) {
        ZipArchiveInputStream(path.inputStream().buffered()).use { archiveStream ->
            var entry: ZipArchiveEntry? = archiveStream.nextEntry
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
    }

    private fun getWindowsAsset(
        assets: List<GithubReleaseAsset>,
        provider: OnnxRuntimeExecutionProvider
    ): OnnxRuntimeDownloadInfo {
        return when (provider) {
            TENSOR_RT -> {
                val asset = assets.first { it.name == windowsCudaAssetName }
                OnnxRuntimeDownloadInfo(
                    asset.name,
                    asset.browserDownloadUrl,
                    getWindowsLibNames(provider).map { windowsCudaLibPath.resolve(it) }
                )
            }

            CUDA -> {
                val asset = assets.first { it.name == windowsCudaAssetName }
                OnnxRuntimeDownloadInfo(
                    asset.name,
                    asset.browserDownloadUrl,
                    getWindowsLibNames(provider).map { windowsCudaLibPath.resolve(it) }
                )
            }

            ROCm -> error("ROCm is unsupported on Windows")

            CPU -> {
                val asset = assets.first { it.name == windowsDirectMLAssetName }
                OnnxRuntimeDownloadInfo(
                    asset.name,
                    asset.browserDownloadUrl,
                    getWindowsLibNames(provider).map { windowsDirectMlLibPath.resolve(it) }
                )
            }

            DirectML -> {
                val asset = assets.first { it.name == windowsDirectMLAssetName }
                OnnxRuntimeDownloadInfo(
                    asset.name,
                    asset.browserDownloadUrl,
                    getWindowsLibNames(provider).map { windowsDirectMlLibPath.resolve(it) }
                )
            }
        }
    }

    private fun getLinuxAsset(
        assets: List<GithubReleaseAsset>,
        provider: OnnxRuntimeExecutionProvider
    ): OnnxRuntimeDownloadInfo {
        return when (provider) {
            TENSOR_RT -> {
                val asset = assets.first { it.name == linuxCudaAssetName }
                OnnxRuntimeDownloadInfo(
                    asset.name,
                    asset.browserDownloadUrl,
                    getLinuxLibNames(provider).map { linuxCudaLibPath.resolve(it) }
                )
            }

            CUDA -> {
                val asset = assets.first { it.name == linuxCudaAssetName }
                OnnxRuntimeDownloadInfo(
                    asset.name,
                    asset.browserDownloadUrl,
                    getLinuxLibNames(provider).map { linuxCudaLibPath.resolve(it) }
                )
            }

            ROCm -> error("ROCm is currently not supported")

//            ROCm -> {
//                val asset = assets.first { it.name == linuxRocmAssetName }
//                OnnxRuntimeDownloadInfo(
//                    asset.name,
//                    asset.browserDownloadUrl,
//                    getLinuxLibNames(onnxRuntimeVersion, provider).map { linuxRocmLibPath.resolve(it) }
//                )
//            }

            CPU -> {
                val asset = assets.first { it.name == linuxCPUAssetName }
                OnnxRuntimeDownloadInfo(
                    asset.name,
                    asset.browserDownloadUrl,
                    getLinuxLibNames(provider).map { linuxCpuLibPath.resolve(it) }
                )
            }

            DirectML -> error("DirectML is unsupported on Linux")
        }
    }

    private data class OnnxRuntimeDownloadInfo(
        val filename: String,
        val downloadUrl: String,
        val extractPaths: List<Path>
    )

    private fun getLinuxLibNames(provider: OnnxRuntimeExecutionProvider): List<String> {
        val libs = when (provider) {
            TENSOR_RT -> listOf(
                "libonnxruntime_providers_shared.so",
                "libonnxruntime_providers_cuda.so",
                "libonnxruntime_providers_tensorrt.so",
            )

            CUDA -> listOf(
                "libonnxruntime_providers_shared.so",
                "libonnxruntime_providers_cuda.so",
            )

            ROCm -> listOf(
                "libonnxruntime_providers_shared.so",
                "libonnxruntime_providers_rocm.so",
            )

            DirectML, CPU -> emptyList()
        }
        return libs + "libonnxruntime.so.$onnxRuntimeVersion"
    }

    private fun getWindowsLibNames(provider: OnnxRuntimeExecutionProvider): List<String> {
        val libs = when (provider) {
            TENSOR_RT -> listOf(
                "onnxruntime_providers_shared.dll",
                "onnxruntime_providers_cuda.dll",
                "onnxruntime_providers_tensorrt.dll"
            )

            CUDA -> listOf(
                "onnxruntime_providers_shared.dll",
                "onnxruntime_providers_cuda.dll",
            )

            ROCm, DirectML, CPU -> emptyList()
        }
        return libs + "onnxruntime.dll"
    }

    private fun getLinuxOnnxruntimeLib(version: String) = "libonnxruntime.so.$version"
}
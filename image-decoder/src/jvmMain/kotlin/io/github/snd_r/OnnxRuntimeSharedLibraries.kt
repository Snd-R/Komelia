package io.github.snd_r

import dev.dirs.ProjectDirectories
import io.github.snd_r.DesktopPlatform.Linux
import io.github.snd_r.DesktopPlatform.MacOS
import io.github.snd_r.DesktopPlatform.Unknown
import io.github.snd_r.DesktopPlatform.Windows
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.CPU
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.CUDA
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.DirectML
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.ROCm
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.fileSize
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.notExists

object OnnxRuntimeSharedLibraries {
    private val logger = LoggerFactory.getLogger(OnnxRuntimeSharedLibraries::class.java)
    private val ortSearchPath = System.getProperty("ort.search.path")
        ?: Path(ProjectDirectories.from("io.github.snd-r.komelia", "", "Komelia").dataDir)
            .resolve("onnxruntime").createDirectories()
            .toString()
    private val initialized = AtomicBoolean(false)
    private const val windowsLibGomp = "libgomp-1"


    var loadErrorMessage: String? = null
        private set

    var isAvailable = false
        private set
    var executionProvider = CPU
        private set

    @Synchronized
    @Suppress("UnsafeDynamicallyLoadedCode")
    fun load() {
        if (DesktopPlatform.Current == Windows) {
            SharedLibrariesLoader.loadLibrary(windowsLibGomp)
        }

        logger.info("searching for ONNX Runtime libraries in $ortSearchPath")

        var onnxruntimePath: Path? = null
        var sharedProvidersPath: Path? = null
        var cudaProviderPath: Path? = null
        var rocmProviderPath: Path? = null
        var directMLPath: Path? = null
        Path.of(ortSearchPath).createDirectories().listDirectoryEntries().forEach { entry ->
            when (entry.fileName.toString()) {
                "DirectML.dll" -> directMLPath = entry
                "libonnxruntime.so", "onnxruntime.dll" -> onnxruntimePath = entry
                "libonnxruntime_providers_shared.so", "onnxruntime_providers_shared.dll" -> sharedProvidersPath = entry
                "libonnxruntime_providers_cuda.so", "onnxruntime_providers_cuda.dll" -> cudaProviderPath = entry
                "libonnxruntime_providers_rocm.so" -> rocmProviderPath = entry
            }
        }

        try {
            directMLPath?.let { loadOrtLibrary(it) }
            onnxruntimePath?.let { loadOrtLibrary(it) }
                ?: throw UnsatisfiedLinkError("could not find ONNX Runtime library")

            // copy to temp dir on windows to allow overriding original dlls during runtime
            if (DesktopPlatform.Current == Windows) {
                sharedProvidersPath?.let { copyToTempDir(it) }
                cudaProviderPath?.let { copyToTempDir(it) }
                rocmProviderPath?.let { copyToTempDir(it) }
            }

            val executionProvider =
                if (cudaProviderPath != null) CUDA
                else if (rocmProviderPath != null) ROCm
                else if (DesktopPlatform.Current == Windows) DirectML
                else CPU
            this.executionProvider = executionProvider

            when (DesktopPlatform.Current) {
                Linux -> {
                    SharedLibrariesLoader.loadLibrary("komelia_onnxruntime")
                    when (executionProvider) {
                        CUDA -> SharedLibrariesLoader.loadLibrary("komelia_enumerate_devices_cuda")
                        else -> {}
                    }
                }

                Windows -> {
                    when (executionProvider) {
                        DirectML -> {
                            SharedLibrariesLoader.loadLibrary("libkomelia_onnxruntime_dml")
                            SharedLibrariesLoader.loadLibrary("libkomelia_enumerate_devices_dxgi")
                        }

                        CUDA -> {
                            SharedLibrariesLoader.loadLibrary("libkomelia_onnxruntime")
                            SharedLibrariesLoader.loadLibrary("libkomelia_enumerate_devices_cuda")
                        }

                        else -> {
                            SharedLibrariesLoader.loadLibrary("libkomelia_onnxruntime")
                        }
                    }
                }

                MacOS, Unknown -> error("Unsupported OS")
            }


            if (!initialized.compareAndSet(false, true)) return


            OnnxRuntimeUpscaler.init(
                when (executionProvider) {
                    CUDA -> "CUDA"
                    ROCm -> "ROCM"
                    DirectML -> "DML"
                    CPU -> "CPU"
                }
            )

            isAvailable = true
            this.executionProvider = executionProvider
        } catch (e: UnsatisfiedLinkError) {
            loadErrorMessage = e.message
            throw e
        }
    }

    @Suppress("UnsafeDynamicallyLoadedCode")
    private fun loadOrtLibrary(path: Path) {
        val loadFile =
            if (DesktopPlatform.Current == Windows) {
                copyToTempDir(path)
            } else {
                path
            }
        System.load(loadFile.toString())
        logger.info("loaded $loadFile")
    }

    private fun copyToTempDir(path: Path): Path {
        val destinationPath = SharedLibrariesLoader.tempDir.resolve(path.fileName)
        if (destinationPath.notExists() || path.fileSize() != destinationPath.fileSize()) {
            Files.copy(path, destinationPath, REPLACE_EXISTING)
        }

        return destinationPath
    }

    enum class OnnxRuntimeExecutionProvider {
        CUDA,
        ROCm,
        DirectML,
        CPU,
    }
}
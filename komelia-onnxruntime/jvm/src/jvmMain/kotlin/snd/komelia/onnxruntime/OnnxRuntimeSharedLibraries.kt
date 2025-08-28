package snd.komelia.onnxruntime

import dev.dirs.ProjectDirectories
import io.github.oshai.kotlinlogging.KotlinLogging
import snd.jni.DesktopPlatform
import snd.jni.SharedLibrariesLoader
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CUDA
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.DirectML
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.ROCm
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.TENSOR_RT
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.fileSize
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.notExists


object OnnxRuntimeSharedLibraries {
    private val logger = KotlinLogging.logger { }
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
    var availableDevices: List<DeviceInfo> = emptyList()
        private set

    @Synchronized
    @Suppress("UnsafeDynamicallyLoadedCode")
    fun load() {
        if (!initialized.compareAndSet(false, true)) return

        try {
            if (DesktopPlatform.Current == DesktopPlatform.Windows) {
                SharedLibrariesLoader.loadLibrary(windowsLibGomp)
            }
            logger.info { "searching for ONNX Runtime libraries in $ortSearchPath" }

            val executionProvider = loadOrtLibraries()
            this.executionProvider = executionProvider
            loadKomeliaJniLibs()

            isAvailable = true
        } catch (e: UnsatisfiedLinkError) {
            loadErrorMessage = e.message
            throw e
        } catch (e: OnnxRuntimeException) {
            loadErrorMessage = e.message
            throw e
        }
    }

    data class OrtLibraries(
        val onnxruntime: Path,
        val sharedEp: Path,
        val directMl: Path?,
        val cudaEp: Path?,
        val trtEp: Path?,
        val rocmEp: Path?,
    )

    private fun loadOrtLibraries(): OnnxRuntimeExecutionProvider {
        val ortLibraries = getAvailableOrtLibraries()
        ortLibraries.onnxruntime.let { loadOrtLibrary(it) }
        ortLibraries.directMl?.let { loadOrtLibrary(it) }

        // copy to temp dir on windows to allow overriding original dlls during runtime
        if (DesktopPlatform.Current == DesktopPlatform.Windows) {
            copyToTempDir(ortLibraries.sharedEp)
            ortLibraries.cudaEp?.let { copyToTempDir(it) }
            ortLibraries.trtEp?.let { copyToTempDir(it) }
            ortLibraries.rocmEp?.let { copyToTempDir(it) }
        }

        return when {
            ortLibraries.trtEp != null && ortLibraries.cudaEp != null -> TENSOR_RT
            ortLibraries.cudaEp != null -> CUDA
            ortLibraries.rocmEp != null -> ROCm
            ortLibraries.directMl != null -> DirectML
            else -> CPU
        }
    }

    private fun getAvailableOrtLibraries(): OrtLibraries {
        var onnxruntime: Path? = null
        var sharedEp: Path? = null
        var directML: Path? = null
        var cudaEp: Path? = null
        var trtEp: Path? = null
        var rocmEp: Path? = null
        Path.of(ortSearchPath).createDirectories().listDirectoryEntries().forEach { entry ->
            when (entry.fileName.toString()) {
                "libonnxruntime.so",
                "onnxruntime.dll" -> onnxruntime = entry

                "libonnxruntime_providers_shared.so",
                "onnxruntime_providers_shared.dll" -> sharedEp = entry

                "libonnxruntime_providers_cuda.so",
                "onnxruntime_providers_cuda.dll" -> cudaEp = entry

                "libonnxruntime_providers_rocm.so" -> rocmEp = entry

                "libonnxruntime_providers_tensorrt.so",
                "onnxruntime_providers_tensorrt.dll" ->
                    trtEp = entry

                "DirectML.dll" -> directML = entry
            }
        }
        check(onnxruntime != null)
        check(sharedEp != null)

        return OrtLibraries(
            onnxruntime = onnxruntime,
            sharedEp = sharedEp,
            directMl = directML,
            cudaEp = cudaEp,
            trtEp = trtEp,
            rocmEp = rocmEp,
        )
    }

    private fun loadKomeliaJniLibs() {
        when (DesktopPlatform.Current) {
            DesktopPlatform.Linux -> {
                SharedLibrariesLoader.loadLibrary("komelia_onnxruntime")
                when (executionProvider) {
                    CUDA, TENSOR_RT ->
                        SharedLibrariesLoader.loadLibrary("komelia_enumerate_devices_cuda")

                    ROCm -> SharedLibrariesLoader.loadLibrary("komelia_enumerate_devices_rocm")
                    else -> {}
                }
            }

            DesktopPlatform.Windows -> {
                when (executionProvider) {
                    DirectML -> {
                        SharedLibrariesLoader.loadLibrary("libkomelia_onnxruntime_dml")
                        SharedLibrariesLoader.loadLibrary("libkomelia_enumerate_devices_dxgi")
                    }

                    CUDA, TENSOR_RT -> {
                        SharedLibrariesLoader.loadLibrary("libkomelia_onnxruntime")
                        SharedLibrariesLoader.loadLibrary("libkomelia_enumerate_devices_cuda")
                    }

                    else -> {
                        SharedLibrariesLoader.loadLibrary("libkomelia_onnxruntime")
                    }
                }
            }

            DesktopPlatform.MacOS, DesktopPlatform.Unknown -> error("Unsupported OS")
        }
    }

    @Suppress("UnsafeDynamicallyLoadedCode")
    private fun loadOrtLibrary(path: Path) {
        val loadFile =
            if (DesktopPlatform.Current == DesktopPlatform.Windows) {
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
            Files.copy(path, destinationPath, StandardCopyOption.REPLACE_EXISTING)
        }

        return destinationPath
    }
}
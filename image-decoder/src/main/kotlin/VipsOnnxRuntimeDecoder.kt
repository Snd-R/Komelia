package io.github.snd_r

import dev.dirs.ProjectDirectories
import io.github.snd_r.DesktopPlatform.Linux
import io.github.snd_r.DesktopPlatform.MacOS
import io.github.snd_r.DesktopPlatform.Unknown
import io.github.snd_r.DesktopPlatform.Windows
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.listDirectoryEntries

object VipsOnnxRuntimeDecoder {
    private val logger = LoggerFactory.getLogger(VipsOnnxRuntimeDecoder::class.java)
    private val ortSearchPath = System.getProperty("ort.search.path")
        ?: Path(ProjectDirectories.from("io.github.snd-r.komelia", "", "Komelia").dataDir)
            .resolve("onnxruntime").createDirectories()
            .toString()
    private val initialized = AtomicBoolean(false)

    var loadErrorMessage: String? = null
        private set

    var isAvailable = false
        private set
    var isCudaAvailable = false
        private set
    var isRocmAvailable = false
        private set

    @Synchronized
    @Suppress("UnsafeDynamicallyLoadedCode")
    fun load() {
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
            sharedProvidersPath?.let { loadOrtLibrary(it) }
            cudaProviderPath?.let { loadOrtLibrary(it) }
            rocmProviderPath?.let { loadOrtLibrary(it) }

            when (DesktopPlatform.Current) {
                Linux -> SharedLibrariesLoader.loadLibrary("komelia_vips_ort")
                Windows -> {
                    if (cudaProviderPath == null && rocmProviderPath == null) {
                        println("loaded onnx dml")
                        SharedLibrariesLoader.loadLibrary("libkomelia_vips_ort_dml")
                    } else {
                        println("loaded onnx regular")
                        SharedLibrariesLoader.loadLibrary("libkomelia_vips_ort")
                    }
                }

                MacOS, Unknown -> error("Unsupported OS")
            }

        } catch (e: UnsatisfiedLinkError) {
            loadErrorMessage = e.message
            throw e
        }

        if (!initialized.compareAndSet(false, true)) return
        val tempDir = Path(System.getProperty("java.io.tmpdir")).resolve("komelia_ort").createDirectories()
        init(
            if (cudaProviderPath != null) "CUDA"
            else if (rocmProviderPath != null) "ROCM"
            else if (DesktopPlatform.Current == Windows) "DML"
            else "CPU",
            tempDir.toString()
        )

        isAvailable = true
        if (cudaProviderPath != null) isCudaAvailable = true
        if (rocmProviderPath != null) isRocmAvailable = true
    }

    // copy to temp dir on windows to allow overriding original dlls during runtime
    private fun loadOrtLibrary(path: Path) {
        val loadFile =
            if (DesktopPlatform.Current == Windows)
                Files.copy(path, SharedLibrariesLoader.tempDir.resolve(path.fileName), REPLACE_EXISTING)
            else path
        System.load(loadFile.toString())
    }

    @Throws(OrtException::class)
    private external fun init(
        provider: String,
        tempDir: String,
    )

    @Throws(VipsException::class, OrtException::class)
    external fun decodeAndResize(
        encoded: ByteArray,
        modelPath: String,
        cacheKey: String?,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
    ): VipsImage?

    enum class OnnxRuntimeExecutionProvider {
        CUDA,
        ROCm,
        CPU,
        DirectML
    }
}
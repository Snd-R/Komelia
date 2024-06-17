package io.github.snd_r

import dev.dirs.ProjectDirectories
import org.slf4j.LoggerFactory
import java.nio.file.Path
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
        Path.of(ortSearchPath).createDirectories().listDirectoryEntries().forEach { entry ->
            when (entry.fileName.toString()) {
                "libonnxruntime.so", "onnruntime.dll" -> onnxruntimePath = entry
                "libonnxruntime_providers_shared.so", "onnxruntime_providers_shared.dll" -> sharedProvidersPath = entry
                "libonnxruntime_providers_cuda.so", "onnxruntime_providers_cuda.dll" -> cudaProviderPath = entry
                "libonnxruntime_providers_rocm.so" -> rocmProviderPath = entry
            }
        }

        try {
            onnxruntimePath?.let { System.load(it.toString()) }
                ?: throw UnsatisfiedLinkError("could not find ONNX Runtime library")
            sharedProvidersPath?.let { System.load(it.toString()) }
            cudaProviderPath?.let { System.load(it.toString()) }
            rocmProviderPath?.let { System.load(it.toString()) }
            SharedLibrariesLoader.loadLibrary("komelia_vips_ort")
        } catch (e: UnsatisfiedLinkError) {
            loadErrorMessage = e.message
            throw e
        }

        if (!initialized.compareAndSet(false, true)) return
        val tempDir = Path(System.getProperty("java.io.tmpdir")).resolve("komelia_ort").createDirectories()
        init(
            if (cudaProviderPath != null) "CUDA" else if (rocmProviderPath != null) "ROCM" else "CPU",
            tempDir.toString()
        )

        isAvailable = true
        if (cudaProviderPath != null) isCudaAvailable = true
        if (rocmProviderPath != null) isRocmAvailable = true
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
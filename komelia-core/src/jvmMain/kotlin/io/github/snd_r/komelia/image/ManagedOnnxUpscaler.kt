package io.github.snd_r.komelia.image

import coil3.disk.DiskCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppDirectories
import io.github.snd_r.komelia.AppDirectories.mangaJaNaiInstallPath
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.Path.Companion.toOkioPath
import snd.komelia.image.ImageFormat
import snd.komelia.image.KomeliaImage
import snd.komelia.image.OnnxRuntime
import snd.komelia.image.OnnxRuntime.DeviceInfo
import snd.komelia.image.OnnxRuntimeSharedLibraries
import snd.komelia.image.OnnxRuntimeUpscaleMode
import snd.komelia.image.OnnxRuntimeUpscaleMode.MANGAJANAI_PRESET
import snd.komelia.image.OnnxRuntimeUpscaleMode.NONE
import snd.komelia.image.OnnxRuntimeUpscaleMode.USER_SPECIFIED_MODEL
import snd.komelia.image.OnnxRuntimeUpscaler
import snd.komelia.image.VipsBackedImage
import snd.komelia.image.VipsImage
import snd.komelia.image.toVipsImage
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.math.ceil
import kotlin.time.TimeSource

private val logger = KotlinLogging.logger {}

class ManagedOnnxUpscaler(private val settingsRepository: ImageReaderSettingsRepository) : OnnxRuntime {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    override val availableDevices = MutableStateFlow(emptyList<DeviceInfo>())
    override val provider = OnnxRuntimeSharedLibraries.executionProvider
    override val upscaleMode = settingsRepository.getOnnxRuntimeMode()
        .stateIn(scope, SharingStarted.Eagerly, NONE)
    override val mangaJaNaiIsAvailable = MutableStateFlow(
        OnnxRuntimeSharedLibraries.isAvailable && AppDirectories.containsMangaJaNaiModels()
    )
    override val selectedModelPath = settingsRepository.getSelectedOnnxModel()
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val mutex = Mutex()

    private val imageCache = DiskCache.Builder()
        .directory(AppDirectories.readerUpscaleCachePath.createDirectories().toOkioPath())
        .maxSizeBytes(500L * 1024 * 1024) // 500mb
        .build()

    fun initialize() {
        require(OnnxRuntimeSharedLibraries.isAvailable)
        runCatching { availableDevices.value = OnnxRuntimeUpscaler.enumerateDevices() }

        settingsRepository.getOnnxRuntimeDeviceId()
            .onEach { newDeviceId -> OnnxRuntimeUpscaler.setDeviceId(newDeviceId) }
            .launchIn(scope)

        settingsRepository.getOnnxRuntimeTileSize()
            .onEach { newTileSize -> OnnxRuntimeUpscaler.setTileSize(newTileSize) }
            .launchIn(scope)

        this.selectedModelPath.filterNotNull()
            .combine(upscaleMode) { modelPath, mode ->
                if (mode == USER_SPECIFIED_MODEL && Path(modelPath).exists()) {
                    OnnxRuntimeUpscaler.setModelPath(modelPath)
                }
            }.launchIn(scope)
    }

    override suspend fun upscale(image: KomeliaImage, cacheKey: String?): KomeliaImage? {
        val timeSource = TimeSource.Monotonic

        mutex.withLock {
            val start = timeSource.markNow()
            val vipsImage = image.toVipsImage()
            val result = withContext(Dispatchers.IO) {
                if (cacheKey == null) {
                    when (upscaleMode.value) {
                        USER_SPECIFIED_MODEL -> OnnxRuntimeUpscaler.upscale(vipsImage)
                        MANGAJANAI_PRESET -> mangaJaNaiUpscale(vipsImage, cacheKey)
                        NONE -> null
                    }
                } else imageCache.openSnapshot(cacheKey).use { snapshot ->
                    if (snapshot != null) {
                        return@withContext VipsImage.decodeFromFile(snapshot.data.toString())
                    }
                    val upscaled = when (upscaleMode.value) {
                        USER_SPECIFIED_MODEL -> OnnxRuntimeUpscaler.upscale(vipsImage)
                        MANGAJANAI_PRESET -> mangaJaNaiUpscale(vipsImage, cacheKey)
                        NONE -> null
                    }

                    upscaled?.let { image -> writeToDiskCache(image, cacheKey) }

                    return@withContext upscaled
                }
            }
            if (result != null) {
                val end = timeSource.markNow()
                logger.info { "image $cacheKey completed ORT upscaling in ${end - start}" }
            }
            return result?.let { VipsBackedImage(it) }
        }
    }

    override fun setOnnxModelPath(path: String?) {
        if (path == null) {
            scope.launch { settingsRepository.putSelectedOnnxModel(path) }
        } else {
            val filePath = Path(path)
            if (filePath.name.endsWith(".onnx")) {
                scope.launch { settingsRepository.putSelectedOnnxModel(path) }
                if (filePath.exists()) {
                    OnnxRuntimeUpscaler.setModelPath(filePath.toString())
                }
            }
        }
    }

    override fun setUpscaleMode(mode: OnnxRuntimeUpscaleMode) {
        scope.launch { settingsRepository.putOnnxRuntimeMode(mode) }
    }

    override fun clearCache() {
        imageCache.clear()
    }

    private fun writeToDiskCache(image: VipsImage, cacheKey: String) {
        val editor = imageCache.openEditor(cacheKey) ?: return
        try {
            image.encodeToFilePng(editor.data.toString())
            editor.commit()
        } catch (e: Exception) {
            editor.abort()
            throw e
        }
    }

    private fun mangaJaNaiUpscale(image: VipsImage, cacheKey: String?): VipsImage {
        val isGrayscale = if (image.type == ImageFormat.GRAYSCALE_8) true else isRgbaIsGrayscale(image)

        val modelPath =
            if (isGrayscale) {
                val model = when (image.height) {
                    in 0..1250 -> "2x_MangaJaNai_1200p_V1_ESRGAN_70k.onnx"
                    in 1251..1350 -> "2x_MangaJaNai_1300p_V1_ESRGAN_75k.onnx"
                    in 1351..1450 -> "2x_MangaJaNai_1400p_V1_ESRGAN_70k.onnx"
                    in 1451..1550 -> "2x_MangaJaNai_1500p_V1_ESRGAN_90k.onnx"
                    in 1551..1760 -> "2x_MangaJaNai_1600p_V1_ESRGAN_90k.onnx"
                    in 1761..1984 -> "2x_MangaJaNai_1920p_V1_ESRGAN_70k.onnx"
                    else -> "2x_MangaJaNai_2048p_V1_ESRGAN_95k.onnx"
                }
                mangaJaNaiInstallPath.resolve(model)
            } else {
                val illustration2x = mangaJaNaiInstallPath.resolve("2x_IllustrationJaNai_V1_ESRGAN_120k.onnx")
                val illustration4x = mangaJaNaiInstallPath.resolve("4x_IllustrationJaNai_V1_ESRGAN_135k.onnx")
                if (illustration2x.exists()) illustration2x
                else illustration4x

            }
        logger.info { "image $cacheKey: using model ${modelPath.name}" }

        OnnxRuntimeUpscaler.setModelPath(modelPath.toString())
        val upscaled = OnnxRuntimeUpscaler.upscale(image)
        return upscaled
    }

    private fun isRgbaIsGrayscale(image: VipsImage): Boolean {
        val shrinkFactor = minOf(image.width, image.height) / 64.0
        val resized = image.shrink(ceil(shrinkFactor))

        val rgba = resized.getBytes()
        resized.close()

        var grayScaleCount = 0
        val pixelBytesRead = ArrayList<Byte>(4)
        for (byte in rgba) {
            if (pixelBytesRead.size == 4) {
                val rgb = pixelBytesRead.subList(0, 3)
                if (rgb.max() - rgb.min() < 20) grayScaleCount += 1
                pixelBytesRead.clear()
            }

            pixelBytesRead.add(byte)
        }

        // consider image grayscale if less than 10% are not grayscale pixels
        val rgbaPixelCount = rgba.size / 4
        return (rgbaPixelCount - grayScaleCount) < rgbaPixelCount / 10
    }
}
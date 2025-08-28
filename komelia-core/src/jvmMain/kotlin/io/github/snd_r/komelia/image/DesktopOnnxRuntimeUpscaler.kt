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
import snd.komelia.image.VipsBackedImage
import snd.komelia.image.VipsImage
import snd.komelia.image.toVipsImage
import snd.komelia.onnxruntime.DeviceInfo
import snd.komelia.onnxruntime.OnnxRuntimeSharedLibraries
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.math.ceil
import kotlin.time.TimeSource

private val logger = KotlinLogging.logger {}

class DesktopOnnxRuntimeUpscaler(
    private val settingsRepository: ImageReaderSettingsRepository,
    private val ortUpscaler: snd.komelia.onnxruntime.OnnxRuntimeUpscaler
) : KomeliaUpscaler {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    override val availableDevices = MutableStateFlow(emptyList<DeviceInfo>())
    override val provider = OnnxRuntimeSharedLibraries.executionProvider
    override val upscaleMode = settingsRepository.getOnnxRuntimeMode()
        .stateIn(scope, SharingStarted.Eagerly, UpscaleMode.NONE)
    override val mangaJaNaiIsAvailable = MutableStateFlow(
        OnnxRuntimeSharedLibraries.isAvailable && AppDirectories.containsMangaJaNaiModels()
    )
    override val userModelPath = settingsRepository.getSelectedOnnxModel()
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val mutex = Mutex()

    private val imageCache = DiskCache.Builder()
        .directory(AppDirectories.readerUpscaleCachePath.createDirectories().toOkioPath())
        .maxSizeBytes(500L * 1024 * 1024) // 500mb
        .build()

    fun initialize() {
        require(OnnxRuntimeSharedLibraries.isAvailable)
        runCatching { availableDevices.value = ortUpscaler.getAvailableDevices() }

        settingsRepository.getOnnxRuntimeDeviceId()
            .onEach { newDeviceId -> ortUpscaler.setExecutionProvider(provider, newDeviceId) }
            .launchIn(scope)

        settingsRepository.getOnnxRuntimeTileSize()
            .onEach { newTileSize -> ortUpscaler.setTileSize(newTileSize) }
            .launchIn(scope)

//        this.selectedModelPath.filterNotNull()
//            .combine(upscaleMode) { modelPath, mode ->
//                if (mode == USER_SPECIFIED_MODEL && Path(modelPath).exists()) {
//                    OnnxRuntimeUpscaler.setModelPath(modelPath)
//                }
//            }.launchIn(scope)

        upscaleMode
            .onEach { mutex.withLock { clearCache() } }
            .launchIn(scope)
    }

    override suspend fun upscale(image: KomeliaImage, cacheKey: String?): KomeliaImage? {
        val timeSource = TimeSource.Monotonic

        mutex.withLock {
            val start = timeSource.markNow()
            val result = withContext(Dispatchers.IO) {
                if (cacheKey == null) {
                    when (upscaleMode.value) {
                        UpscaleMode.NONE -> null
                        UpscaleMode.USER_SPECIFIED_MODEL -> userModelUpscale(image)
                        UpscaleMode.MANGAJANAI_PRESET -> mangaJaNaiUpscale(image, cacheKey)
                    }
                } else imageCache.openSnapshot(cacheKey).use { snapshot ->
                    if (snapshot != null) {
                        return@withContext VipsBackedImage(VipsImage.decodeFromFile(snapshot.data.toString()))
                    }

                    val upscaled = when (upscaleMode.value) {
                        UpscaleMode.USER_SPECIFIED_MODEL -> ortUpscaler.upscale(image)
                        UpscaleMode.MANGAJANAI_PRESET -> mangaJaNaiUpscale(image, cacheKey)
                        UpscaleMode.NONE -> null
                    }

                    upscaled?.let { newImage ->
                        if (image.pagesLoaded == 1) writeToDiskCache(newImage, cacheKey)
                    }

                    return@withContext upscaled
                }
            }
            if (result != null) {
                val end = timeSource.markNow()
                logger.info { "image $cacheKey completed ORT upscaling in ${end - start}" }
            }
            return result
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
                    ortUpscaler.setModelPath(filePath.toString())
                }
            }
        }
    }

    override fun setUpscaleMode(mode: UpscaleMode) {
        scope.launch { settingsRepository.putOnnxRuntimeMode(mode) }
    }

    override fun clearCache() {
        imageCache.clear()
    }

    private fun writeToDiskCache(image: KomeliaImage, cacheKey: String) {
        val vipsImage = image.toVipsImage()
        val editor = imageCache.openEditor(cacheKey) ?: return
        try {
            vipsImage.encodeToFilePng(editor.data.toString())
            editor.commit()
        } catch (e: Exception) {
            editor.abort()
            throw e
        }
    }

    private fun userModelUpscale(image: KomeliaImage): KomeliaImage? {
        val modelPath = userModelPath.value ?: return null
        ortUpscaler.setModelPath(modelPath)
        return ortUpscaler.upscale(image)
    }

    private suspend fun mangaJaNaiUpscale(image: KomeliaImage, cacheKey: String?): KomeliaImage {
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

        ortUpscaler.setModelPath(modelPath.toString())
        val upscaled = ortUpscaler.upscale(image)
        return upscaled
    }

    private suspend fun isRgbaIsGrayscale(image: KomeliaImage): Boolean {
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
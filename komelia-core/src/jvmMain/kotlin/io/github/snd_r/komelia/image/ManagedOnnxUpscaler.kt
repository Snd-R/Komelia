package io.github.snd_r.komelia.image

import coil3.disk.DiskCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.ImageFormat
import io.github.snd_r.OnnxRuntimeSharedLibraries
import io.github.snd_r.OnnxRuntimeUpscaler
import io.github.snd_r.VipsImage
import io.github.snd_r.komelia.AppDirectories
import io.github.snd_r.komelia.AppDirectories.mangaJaNaiInstallPath
import io.github.snd_r.komelia.image.ManagedOnnxUpscaler.UpscaleMode.MANGAJANAI_PRESET
import io.github.snd_r.komelia.image.ManagedOnnxUpscaler.UpscaleMode.NONE
import io.github.snd_r.komelia.image.ManagedOnnxUpscaler.UpscaleMode.USER_SPECIFIED_MODEL
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.platform.mangaJaNai
import io.github.snd_r.komelia.platform.upsamplingFilters
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.Path.Companion.toOkioPath
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.math.ceil
import kotlin.time.TimeSource

private val logger = KotlinLogging.logger {}

class ManagedOnnxUpscaler(private val settingsRepository: CommonSettingsRepository) {
    val upscaleMode = MutableStateFlow(NONE)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mutex = Mutex()

    private val imageCache = DiskCache.Builder()
        .directory(AppDirectories.readerUpscaleCachePath.createDirectories().toOkioPath())
        .maxSizeBytes(500L * 1024 * 1024) // 500mb
        .build()

    fun initialize() {
        require(OnnxRuntimeSharedLibraries.isAvailable)

        settingsRepository.getOnnxRuntimeDeviceId()
            .onEach { newDeviceId -> OnnxRuntimeUpscaler.setDeviceId(newDeviceId) }
            .launchIn(scope)

        settingsRepository.getOnnxRuntimeTileSize()
            .onEach { newTileSize -> OnnxRuntimeUpscaler.setTileSize(newTileSize) }
            .launchIn(scope)

        settingsRepository.getDecoderSettings()
            .conflate()
            .onEach { decoderSettings ->
                mutex.withLock {
                    imageCache.clear()

                    val option = decoderSettings.upscaleOption
                    when (option) {
                        mangaJaNai -> upscaleMode.value = MANGAJANAI_PRESET
                        !in upsamplingFilters -> {
                            val modelPath = Path.of(settingsRepository.getOnnxModelsPath().first())
                                .resolve(decoderSettings.upscaleOption.value)
                                .toString()

                            scope.launch { OnnxRuntimeUpscaler.setModelPath(modelPath) }
                            upscaleMode.value = USER_SPECIFIED_MODEL
                        }

                        else -> upscaleMode.value = NONE
                    }

                }
            }.launchIn(scope)
    }

    suspend fun upscale(pageId: PageId, image: VipsImage): VipsImage? {
        val timeSource = TimeSource.Monotonic

        mutex.withLock {
            val start = timeSource.markNow()
            val result = withContext(Dispatchers.IO) {
                imageCache.openSnapshot(pageId.toString()).use { snapshot ->
                    if (snapshot != null) {
                        return@withContext VipsImage.decodeFromFile(snapshot.data.toString())
                    }
                    val upscaled = when (upscaleMode.value) {
                        USER_SPECIFIED_MODEL -> OnnxRuntimeUpscaler.upscale(image)
                        MANGAJANAI_PRESET -> mangaJaNaiUpscale(pageId, image)
                        NONE -> null
                    }

                    upscaled?.let { image -> writeToDiskCache(pageId, image) }

                    return@withContext upscaled
                }
            }
            if (result != null) {
                val end = timeSource.markNow()
                logger.info { "page ${pageId.pageNumber} completed ORT upscaling in ${end - start}" }
            }
            return result
        }
    }

    private fun writeToDiskCache(pageId: PageId, image: VipsImage) {
        val editor = imageCache.openEditor(pageId.toString()) ?: return
        try {
            image.encodeToFilePng(editor.data.toString())
            editor.commit()
        } catch (e: Exception) {
            editor.abort()
            throw e
        }
    }

    private fun mangaJaNaiUpscale(pageId: PageId, image: VipsImage): VipsImage {
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
        logger.info { "page ${pageId.pageNumber}: using model ${modelPath.name}" }

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

    private class OnnxRuntimeProcessingStep(
        private val upscaler: ManagedOnnxUpscaler,
    ) : ProcessingStep {
        override suspend fun process(pageId: PageId, image: VipsImage): PlatformImage? {
            return upscaler.upscale(pageId, image)
        }

        override suspend fun addChangeListener(callback: () -> Unit) {
            upscaler.upscaleMode.drop(1).collect { callback() }
        }
    }

    enum class UpscaleMode {
        USER_SPECIFIED_MODEL,
        MANGAJANAI_PRESET,
        NONE
    }
}
package io.github.snd_r.komelia.image

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.reactivecircus.cache4k.Cache
import io.github.reactivecircus.cache4k.CacheEvent.Evicted
import io.github.reactivecircus.cache4k.CacheEvent.Expired
import io.github.reactivecircus.cache4k.CacheEvent.Removed
import io.github.snd_r.ImageFormat
import io.github.snd_r.OnnxRuntimeSharedLibraries
import io.github.snd_r.OnnxRuntimeUpscaler
import io.github.snd_r.VipsImage
import io.github.snd_r.komelia.AppDirectories
import io.github.snd_r.komelia.image.ManagedOnnxUpscaler.UpscaleMode.MANGAJANAI_PRESET
import io.github.snd_r.komelia.image.ManagedOnnxUpscaler.UpscaleMode.NONE
import io.github.snd_r.komelia.image.ManagedOnnxUpscaler.UpscaleMode.USER_SPECIFIED_MODEL
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.platform.mangaJaNai
import io.github.snd_r.komelia.platform.upsamplingFilters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import snd.settings.CommonSettingsRepository
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.math.ceil

private val logger = KotlinLogging.logger {}

class ManagedOnnxUpscaler(private val settingsRepository: CommonSettingsRepository) {
    val upscaleMode = MutableStateFlow(NONE)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mutex = Mutex()

    private val imageCache = Cache.Builder<PageId, VipsImage>().maximumCacheSize(6)
        .eventListener {
            val image = when (it) {
                is Evicted -> it.value
                is Expired -> it.value
                is Removed -> it.value
                else -> null
            } ?: return@eventListener
            image.close()
        }.build()
    private val imagePathCache = Cache.Builder<PageId, Path>().maximumCacheSize(20)
        .eventListener {
            val path = when (it) {
                is Evicted -> it.value
                is Expired -> it.value
                is Removed -> it.value
                else -> null
            } ?: return@eventListener
            scope.launch { path.deleteIfExists() }
        }.build()

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
                    imageCache.invalidateAll()
                    imagePathCache.invalidateAll()

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
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                val cached = imageCache.get(pageId)
                    ?: imagePathCache.get(pageId)?.let { VipsImage.decodeFromFile(it.toString()) }
                if (cached != null) return@withContext cached

                val upscaled = when (upscaleMode.value) {
                    USER_SPECIFIED_MODEL -> OnnxRuntimeUpscaler.upscale(image)
                    MANGAJANAI_PRESET -> mangaJaNaiUpscale(pageId, image)
                    NONE -> null
                }

                upscaled?.also { addToCache(pageId, it) }
            }
        }
    }

    private fun mangaJaNaiUpscale(pageId: PageId, image: VipsImage): VipsImage {
        val isGrayscale = if (image.type == ImageFormat.GRAYSCALE_8) true else isRgbaIsGrayscale(image)

        val model =
            if (isGrayscale) {
                when (image.height) {
                    in 0..1250 -> "2x_MangaJaNai_1200p_V1_ESRGAN_70k.onnx"
                    in 1251..1350 -> "2x_MangaJaNai_1300p_V1_ESRGAN_75k.onnx"
                    in 1351..1450 -> "2x_MangaJaNai_1400p_V1_ESRGAN_70k.onnx"
                    in 1451..1550 -> "2x_MangaJaNai_1500p_V1_ESRGAN_90k.onnx"
                    in 1551..1760 -> "2x_MangaJaNai_1600p_V1_ESRGAN_90k.onnx"
                    in 1761..1984 -> "2x_MangaJaNai_1920p_V1_ESRGAN_70k.onnx"
                    else -> "2x_MangaJaNai_2048p_V1_ESRGAN_95k.onnx"
                }
            } else {
                "4x_IllustrationJaNai_V1_ESRGAN_135k.onnx"
            }
        logger.info { "page ${pageId.pageNumber}: using model $model" }

        OnnxRuntimeUpscaler.setModelPath(AppDirectories.mangaJaNaiInstallPath.resolve(model).toString())
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

    private fun addToCache(pageId: PageId, image: VipsImage) {
        imageCache.put(pageId, image)
        AppDirectories.readerUpscaleCachePath.createDirectories()
        val writePath = createTempFile(AppDirectories.readerUpscaleCachePath, suffix = "_onnxruntime.png")
        image.encodeToFile(writePath.absolutePathString())
        imagePathCache.put(pageId, writePath)
    }

    suspend fun invalidateCache() {
        mutex.withLock {
            imageCache.invalidateAll()
            imagePathCache.invalidateAll()
        }
    }

    enum class UpscaleMode {
        USER_SPECIFIED_MODEL,
        MANGAJANAI_PRESET,
        NONE
    }
}
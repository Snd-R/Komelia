package io.github.snd_r.komelia.ui.color

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.StateScreenModel
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.color.BookColorLevels
import io.github.snd_r.komelia.color.ChannelsLut
import io.github.snd_r.komelia.color.ColorCurveBookPoints
import io.github.snd_r.komelia.color.ColorCurvePoints
import io.github.snd_r.komelia.color.ColorLevelChannels
import io.github.snd_r.komelia.color.Histogram
import io.github.snd_r.komelia.color.RGBA8888LookupTable
import io.github.snd_r.komelia.color.repository.BookColorCorrectionRepository
import io.github.snd_r.komelia.color.repository.ColorCurvePresetRepository
import io.github.snd_r.komelia.color.repository.ColorLevelsPresetRepository
import io.github.snd_r.komelia.image.BookImageLoader
import io.github.snd_r.komelia.image.ImageResult
import io.github.snd_r.komelia.image.toImageBitmap
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.color.ColorCorrectionType.COLOR_CURVES
import io.github.snd_r.komelia.ui.color.ColorCorrectionType.COLOR_LEVELS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komelia.image.ImageFormat
import snd.komelia.image.KomeliaImage
import snd.komga.client.book.KomgaBookId
import kotlin.math.max

class ColorCorrectionViewModel(
    private val bookColorCorrectionRepository: BookColorCorrectionRepository,
    curvePresetRepository: ColorCurvePresetRepository,
    levelsPresetRepository: ColorLevelsPresetRepository,
    private val imageLoader: BookImageLoader,
    private val appNotifications: AppNotifications,
    private val bookId: KomgaBookId,
    private val pageNumber: Int,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    private val originalImage = MutableStateFlow<KomeliaImage?>(null)
    private val histogram = MutableStateFlow(Histogram(ByteArray(0)))
    private val imageMaxSize = MutableStateFlow<IntSize?>(null)
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val correctionType = MutableStateFlow(COLOR_CURVES)

    val curvesState = CurvesState(
        appNotifications = appNotifications,
        curvePresetRepository = curvePresetRepository,
        histogram = histogram,
        coroutineScope = coroutineScope,
        bookCurvesRepository = bookColorCorrectionRepository,
        bookId = bookId,
    )

    val levelsState = LevelsState(
        appNotifications = appNotifications,
        levelsPresetRepository = levelsPresetRepository,
        histogram = histogram,
        coroutineScope = coroutineScope,
        bookLevelsRepository = bookColorCorrectionRepository,
        bookId = bookId,
    )

    private val curveLut = combine(
        curvesState.colorCurve.lookupTable,
        curvesState.rgbaLut,
    ) { color, rgba -> ChannelsLut(color, rgba) }

    private val levelsLut = combine(
        levelsState.colorLevels.lookupTable,
        levelsState.rgbaLut,
    ) { color, rgba -> ChannelsLut(color, rgba) }

    private val currentLut = combine(correctionType, curveLut, levelsLut) { type, curves, levels ->
        when (type) {
            COLOR_CURVES -> curves
            COLOR_LEVELS -> levels
        }
    }

    val displayImage = combine(
        originalImage.filterNotNull(),
        imageMaxSize.filterNotNull(),
        currentLut
    ) { image, targetSize, channelsLut -> Triple(image, targetSize, channelsLut) }
        .conflate()
        .mapNotNull { (image, targetSize, channelsLut) ->
            val throttleDelay = coroutineScope { async { delay(100) } }
            val processed = processDisplayImage(image, targetSize, channelsLut)
            val bitmap = processed.toImageBitmap()
            if (image !== originalImage.value) processed.close()

            throttleDelay.await()
            bitmap
        }.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private suspend fun processDisplayImage(
        image: KomeliaImage,
        targetSize: IntSize,
        channelsLut: ChannelsLut
    ): KomeliaImage {
        suspend fun mapColor(image: KomeliaImage, colorLut: UByteArray?): KomeliaImage? {
            return if (colorLut == null) null
            else when (image.type) {
                ImageFormat.GRAYSCALE_8 -> image.mapLookupTable(colorLut.asByteArray())
                ImageFormat.RGBA_8888 -> image.mapLookupTable(RGBA8888LookupTable(colorLut).interleaved.asByteArray())
                else -> null
            }
        }

        suspend fun mapRGBA(image: KomeliaImage, rgbLut: RGBA8888LookupTable?): KomeliaImage? {
            return if (rgbLut == null) null
            else when (image.type) {
                ImageFormat.RGBA_8888 -> image.mapLookupTable(rgbLut.interleaved.asByteArray())
                else -> null
            }
        }

        suspend fun scale(image: KomeliaImage, targetSize: IntSize): KomeliaImage? {
            val heightFactor = image.height.toDouble() / targetSize.height
            val widthFactor = image.width.toDouble() / targetSize.width
            val scaleFactor = max(heightFactor, widthFactor)
            return if (scaleFactor > 1.0) {
                image.shrink(scaleFactor)
            } else null
        }

        val colorLut = channelsLut.colorLut
        val rgbLut = channelsLut.rgbaLut
        val colorMapped = mapColor(image, colorLut)
        val rgbaMapped = mapRGBA(colorMapped ?: image, rgbLut)
        val resized = scale(rgbaMapped ?: colorMapped ?: image, targetSize)

        when {
            resized != null -> {
                colorMapped?.close()
                rgbaMapped?.close()
                return resized
            }

            rgbaMapped != null -> {
                colorMapped?.close()
                return rgbaMapped
            }

            colorMapped != null -> return colorMapped
            else -> return image
        }

    }

    suspend fun initialize() {
        if (state.value !is LoadState.Uninitialized) return
        appNotifications.runCatchingToNotifications {
            mutableState.value = LoadState.Loading
            when (val result = imageLoader.loadImage(bookId, pageNumber)) {
                is ImageResult.Error -> {
                    mutableState.value = LoadState.Error(result.throwable)
                    return
                }

                is ImageResult.Success -> {
                    val image = result.image
                    originalImage.value = image
                    val histogramImage = image.makeHistogram()
                    histogram.value = Histogram(histogramImage.getBytes())
                    histogramImage.close()
                }
            }

            curvesState.initialize()
            levelsState.initialize()

            correctionType.value = bookColorCorrectionRepository.getCurrentType(bookId).first() ?: COLOR_CURVES
            mutableState.value = LoadState.Success(Unit)
        }.onFailure {
            mutableState.value = LoadState.Error(it)
        }
    }

    fun onImageMaxSizeChange(size: IntSize) {
        imageMaxSize.value = size
    }

    fun onCurveTypeChange(type: ColorCorrectionType) {
        correctionType.value = type
        coroutineScope.launch { bookColorCorrectionRepository.setCurrentType(bookId, type) }
    }

    suspend fun onSave() {
        val type = correctionType.value
        bookColorCorrectionRepository.setCurrentType(bookId, type)
        when (type) {
            COLOR_CURVES -> {
                val points = ColorCurvePoints(
                    colorCurvePoints = curvesState.colorCurve.points.value,
                    redCurvePoints = curvesState.redCurve.points.value,
                    greenCurvePoints = curvesState.greenCurve.points.value,
                    blueCurvePoints = curvesState.blueCurve.points.value,
                )
                val bookPoints = ColorCurveBookPoints(
                    bookId = bookId,
                    channels = points
                )

                if (points == ColorCurvePoints.DEFAULT) {
                    bookColorCorrectionRepository.deleteSettings(bookId)
                } else {
                    bookColorCorrectionRepository.saveCurve(bookPoints)
                }

            }

            COLOR_LEVELS -> {
                val channels = ColorLevelChannels(
                    color = levelsState.colorLevels.levelsConfig.value,
                    red = levelsState.redLevels.levelsConfig.value,
                    green = levelsState.greenLevels.levelsConfig.value,
                    blue = levelsState.blueLevels.levelsConfig.value,
                )
                val bookPoints = BookColorLevels(
                    bookId = bookId,
                    channels = channels
                )

                if (channels == ColorLevelChannels.DEFAULT) {
                    bookColorCorrectionRepository.deleteSettings(bookId)
                } else {
                    bookColorCorrectionRepository.saveLevels(bookPoints)
                }

            }
        }
    }

    override fun onDispose() {
        originalImage.value?.close()
        coroutineScope.cancel()
    }
}

enum class ColorCorrectionType {
    COLOR_CURVES,
    COLOR_LEVELS
}

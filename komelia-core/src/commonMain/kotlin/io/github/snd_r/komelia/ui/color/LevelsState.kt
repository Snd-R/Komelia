package io.github.snd_r.komelia.ui.color

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.color.ColorChannel
import io.github.snd_r.komelia.color.ColorLevelChannels
import io.github.snd_r.komelia.color.Histogram
import io.github.snd_r.komelia.color.HistogramPaths
import io.github.snd_r.komelia.color.Levels
import io.github.snd_r.komelia.color.RGBA8888LookupTable
import io.github.snd_r.komelia.color.identityMap
import io.github.snd_r.komelia.color.normalizeFromCanvas
import io.github.snd_r.komelia.color.repository.BookColorCorrectionRepository
import io.github.snd_r.komelia.color.repository.ColorLevelsPresetRepository
import io.github.snd_r.komelia.ui.color.LevelControlHandleType.GAMMA
import io.github.snd_r.komelia.ui.color.LevelControlHandleType.HIGH
import io.github.snd_r.komelia.ui.color.LevelControlHandleType.LOW
import io.github.snd_r.komelia.ui.color.view.HandleBarState
import io.github.snd_r.komelia.ui.color.view.handleBarSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import snd.komga.client.book.KomgaBookId
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt


enum class LevelControlHandleType { LOW, HIGH, GAMMA, }

@OptIn(ExperimentalCoroutinesApi::class)
class LevelsState(
    coroutineScope: CoroutineScope,
    appNotifications: AppNotifications,
    histogram: StateFlow<Histogram>,
    levelsPresetRepository: ColorLevelsPresetRepository,
    private val bookLevelsRepository: BookColorCorrectionRepository,
    private val bookId: KomgaBookId,
) {
    private val histogramCanvasSize = MutableStateFlow(Size.Zero)
    private val density = MutableStateFlow(Density(1f, 1f))
    private val inputBarCanvasSize = MutableStateFlow(Size.Zero)
    private val outputBarCanvasSize = MutableStateFlow(Size.Zero)

    val colorLevels = Levels()
    val redLevels = Levels()
    val greenLevels = Levels()
    val blueLevels = Levels()

    val currentChannel = MutableStateFlow(ColorChannel.VALUE)
    val selectedPoint = MutableStateFlow<LevelControlHandleType?>(null)
    private val currentLevels = currentChannel.map {
        when (it) {
            ColorChannel.VALUE -> colorLevels
            ColorChannel.RED -> redLevels
            ColorChannel.GREEN -> greenLevels
            ColorChannel.BLUE -> blueLevels
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, colorLevels)

    val inputPointerIcon = MutableStateFlow(PointerIcon.Default)
    private val lowInputPosition = currentLevels.flatMapLatest { level -> level.levelsConfig.map { it.lowInput } }
    private val highInputPosition = currentLevels.flatMapLatest { level -> level.levelsConfig.map { it.highInput } }
    private val gammaPosition = currentLevels.flatMapLatest { level ->
        level.levelsConfig.map { values ->
            val high = values.highInput
            val low = values.lowInput
            val delta = (high - low) / 2
            val mid = low + delta
            mid + delta * log10(1.0f / values.gamma)
        }
    }

    private val lowOutputPosition = currentLevels.flatMapLatest { level -> level.levelsConfig.map { it.lowOutput } }
    private val highOutputPosition = currentLevels.flatMapLatest { level -> level.levelsConfig.map { it.highOutput } }

    val canvasLowInputPosition = lowInputPosition
        .combine(inputBarCanvasSize) { position, size -> position * size.width }
        .stateIn(coroutineScope, SharingStarted.Lazily, 0f)
    val canvasHighInputPosition = highInputPosition
        .combine(inputBarCanvasSize) { position, size -> position * size.width }
        .stateIn(coroutineScope, SharingStarted.Lazily, 0f)
    val canvasGammaPosition = gammaPosition
        .combine(inputBarCanvasSize) { position, size -> position * size.width }
        .stateIn(coroutineScope, SharingStarted.Lazily, 0f)

    val canvasLowOutputPosition = lowOutputPosition
        .combine(inputBarCanvasSize) { position, size -> position * size.width }
        .stateIn(coroutineScope, SharingStarted.Lazily, 0f)
    val canvasHighOutputPosition = highOutputPosition
        .combine(inputBarCanvasSize) { position, size -> position * size.width }
        .stateIn(coroutineScope, SharingStarted.Lazily, 0f)


    private val inputPointsPositions = combine(lowInputPosition, gammaPosition, highInputPosition) { low, gamma, high ->
        listOf(low, gamma, high)
    }.stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
    val inputHandleBarState = HandleBarState(
        coroutineScope = coroutineScope,
        normalizedPointPositions = inputPointsPositions,
        density = density,
        onPositionChange = { index, newValue ->
            presetsState.deselectCurrent()
            val levelsState = currentLevels.value
            when (index) {
                0 -> levelsState.setLowInput(newValue)
                2 -> levelsState.setHighInput(newValue)
                1 -> {
                    val levelsConfig = levelsState.levelsConfig.value
                    val high = levelsConfig.highInput
                    val low = levelsConfig.lowInput

                    val delta = (high - low) / 2
                    val mid = low + delta
                    val tmp = (newValue - mid) / delta
                    val value = 1.0f / 10f.pow(tmp)

                    levelsState.setGamma(value)
                }
            }
        },
    )
    val outputHandleBarState = HandleBarState(
        coroutineScope = coroutineScope,
        normalizedPointPositions = combine(lowOutputPosition, highOutputPosition) { low, high -> listOf(low, high) }
            .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList()),
        density = density,
        onPositionChange = { index, newValue ->
            presetsState.deselectCurrent()
            val levelsState = currentLevels.value
            when (index) {
                0 -> levelsState.setLowOutput(newValue)
                1 -> levelsState.setHighOutput(newValue)
            }
        },
    )

    val lowInputValue = lowInputPosition.map { (it * 255).roundToInt() }
    val highInputValue = highInputPosition.map { (it * 255).roundToInt() }
    val gammaInputValue = currentLevels.flatMapLatest { levels -> levels.levelsConfig.map { it.gamma } }
    val lowOutputValue = currentLevels.flatMapLatest { levels ->
        levels.levelsConfig.map { (it.lowOutput * 255).roundToInt() }
    }
    val highOutputValue =
        currentLevels.flatMapLatest { levels ->
            levels.levelsConfig.map { (it.highOutput * 255).roundToInt() }
        }

    val rgbaLut = combine(
        redLevels.lookupTable,
        greenLevels.lookupTable,
        blueLevels.lookupTable,
    ) { red, green, blue ->
        if (red == null && green == null && blue == null) null
        else RGBA8888LookupTable(
            red = red ?: identityMap,
            green = green ?: identityMap,
            blue = blue ?: identityMap,
            alpha = identityMap
        )
    }

    private val channelLevels = combine(
        colorLevels.levelsConfig,
        redLevels.levelsConfig,
        greenLevels.levelsConfig,
        blueLevels.levelsConfig,
    ) { color, red, green, blue -> ColorLevelChannels(color, red, green, blue) }
        .stateIn(coroutineScope, SharingStarted.Eagerly, ColorLevelChannels.DEFAULT)

    val presetsState = LevelsPresetsState(
        presetRepository = levelsPresetRepository,
        appNotifications = appNotifications,
        coroutineScope = coroutineScope,
        config = channelLevels,
        onChange = { channels ->
            colorLevels.setConfig(channels.color)
            redLevels.setConfig(channels.red)
            greenLevels.setConfig(channels.green)
            blueLevels.setConfig(channels.blue)
        },
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val histogramPaths = histogram.flatMapLatest { it.getDrawPathFlow(histogramCanvasSize) }
        .stateIn(coroutineScope, SharingStarted.Lazily, HistogramPaths(null, null, null, null))

    suspend fun initialize() {
        bookLevelsRepository.getLevels(bookId).first()?.let {
            val channels = it.channels
            colorLevels.setConfig(channels.color)
            redLevels.setConfig(channels.red)
            greenLevels.setConfig(channels.green)
            blueLevels.setConfig(channels.blue)
        }

        presetsState.initialize()
    }

    fun onPointerEvent(event: PointerEvent) {
        when (event.type) {
            PointerEventType.Move -> onPointerMove(event)
            PointerEventType.Press -> onInputBarPointerPress(event)
            PointerEventType.Release -> selectedPoint.value = null
        }
    }

    private fun onPointerMove(event: PointerEvent) {
        val position = event.changes.last().position
        when {
            pointerHitTest(position.x, canvasLowInputPosition.value) ||
                    pointerHitTest(position.x, canvasHighInputPosition.value) ||
                    pointerHitTest(position.x, canvasGammaPosition.value) -> {
                inputPointerIcon.value = PointerIcon.Hand
            }

            else -> inputPointerIcon.value = PointerIcon.Default

        }

        val selectedPoint = selectedPoint.value ?: return

        val currentLevelValues = currentLevels.value
        val normalizedPosition = position.normalizeFromCanvas(1f, inputBarCanvasSize.value).x.coerceIn(0f, 1f)
        when (selectedPoint) {
            LOW -> {
                currentLevelValues.setLowInput(normalizedPosition)
            }

            HIGH -> {
                currentLevelValues.setHighInput(normalizedPosition)
            }

            GAMMA -> {
                val currentValues = currentLevelValues.levelsConfig.value
                val high = currentValues.highInput
                val low = currentValues.lowInput

                val delta = (high - low) / 2
                val mid = low + delta
                val tmp = (normalizedPosition - mid) / delta
                val value = 1.0f / 10f.pow(tmp)

                currentLevelValues.setGamma(value)

            }
        }
    }

    private fun onInputBarPointerPress(event: PointerEvent) {
        val position = event.changes.last().position
        when {
            pointerHitTest(position.x, canvasLowInputPosition.value) -> selectedPoint.value = LOW
            pointerHitTest(position.x, canvasHighInputPosition.value) -> selectedPoint.value = HIGH
            pointerHitTest(position.x, canvasGammaPosition.value) -> selectedPoint.value = GAMMA
        }
    }

    private fun onOutputBarPointerPress(event: PointerEvent) {
        val position = event.changes.last().position
        when {
            pointerHitTest(position.x, canvasLowInputPosition.value) -> selectedPoint.value = LOW
            pointerHitTest(position.x, canvasHighInputPosition.value) -> selectedPoint.value = HIGH
            pointerHitTest(position.x, canvasGammaPosition.value) -> selectedPoint.value = GAMMA
        }
    }

    private fun pointerHitTest(pointerX: Float, targetCenter: Float): Boolean {
        val halfSize = (handleBarSize * density.value.density) / 2
        return pointerX >= targetCenter - halfSize && pointerX <= targetCenter + halfSize
    }

    fun onCurveChannelChange(channel: ColorChannel) {
        selectedPoint.value = null
        currentChannel.value = channel
    }

    fun onChannelReset() {
        when (currentChannel.value) {
            ColorChannel.VALUE -> colorLevels.reset()
            ColorChannel.RED -> redLevels.reset()
            ColorChannel.GREEN -> greenLevels.reset()
            ColorChannel.BLUE -> blueLevels.reset()
        }
        presetsState.deselectCurrent()
    }

    fun onHistogramCanvasSizeChange(size: IntSize) {
        histogramCanvasSize.value = size.toSize()
    }

    fun onInputCanvasSizeChange(size: IntSize) {
        inputBarCanvasSize.value = size.toSize()
    }

    fun onOutputCanvasSizeChange(size: IntSize) {
        outputBarCanvasSize.value = size.toSize()
    }

    fun onDensityChange(density: Density) {
        this.density.value = density
    }

    fun onLowPointInputChange(value: Int) {
        currentLevels.value.setLowInput(value / 255f)
    }

    fun onHighPointInputChange(value: Int) {
        currentLevels.value.setHighInput(value / 255f)
    }

    fun onGammaInputChange(value: Float) {
        currentLevels.value.setGamma(value)
    }

    fun onLowOutputChange(value: Int) {
        currentLevels.value.setLowOutput(value / 255f)
    }

    fun onHighOutputChange(value: Int) {
        currentLevels.value.setHighOutput(value / 255f)
    }
}
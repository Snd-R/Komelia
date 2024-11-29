package io.github.snd_r.komelia.ui.reader.image

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.IntSize
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class ScreenScaleState {
    val zoom = MutableStateFlow(1f)
    private val zoomLimits = MutableStateFlow(1.0f..5f)
    private var currentOffset = Offset.Zero

    val areaSize = MutableStateFlow(IntSize.Zero)
    val targetSize = MutableStateFlow(Size(1f,1f))

    val offsetXLimits = MutableStateFlow(-1f..1f)
    val offsetYLimits = MutableStateFlow(-1f..1f)

    private val scrollScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val velocityTracker = VelocityTracker()

    val scrollOrientation = MutableStateFlow<Orientation?>(null)
    val scrollReversed = MutableStateFlow(false)
    private val scrollState = MutableStateFlow<ScrollableState?>(null)

    val transformation = MutableStateFlow(Transformation(offset = Offset.Zero, scale = 1f))

    fun scaleFor100PercentZoom() =
        max(
            areaSize.value.width.toFloat() / targetSize.value.width,
            areaSize.value.height.toFloat() / targetSize.value.height
        )

    private fun scaleForFullVisibility() =
        min(
            areaSize.value.width.toFloat() / targetSize.value.width,
            areaSize.value.height.toFloat() / targetSize.value.height
        )

    private fun zoomToScale(zoom: Float) = zoom * scaleFor100PercentZoom()

    private fun limitTargetInsideArea(areaSize: IntSize, targetSize: Size, zoom: Float?) {
        this.areaSize.value = areaSize
        this.targetSize.value = Size(
            width = targetSize.width,
            height = targetSize.height
        )
        zoomLimits.value = (scaleForFullVisibility() / scaleFor100PercentZoom())..zoomLimits.value.endInclusive
        if (zoom != null) this.zoom.value = zoom

        applyLimits()
    }

    fun setAreaSize(areaSize: IntSize) {
        this.areaSize.value = areaSize
    }

    fun setTargetSize(targetSize: Size, zoom: Float? = null) {
        if (targetSize == this.targetSize.value && zoom == this.zoom.value) return
        limitTargetInsideArea(areaSize.value, targetSize, zoom)
    }

    private fun applyLimits() {
        zoom.value = zoom.value.coerceIn(zoomLimits.value)
        val scale = zoomToScale(zoom.value)
        offsetXLimits.update { offsetLimits(targetSize.value.width * scale, areaSize.value.width.toFloat()) }
        offsetYLimits.update { offsetLimits(targetSize.value.height * scale, areaSize.value.height.toFloat()) }

        currentOffset = Offset(
            currentOffset.x.coerceIn(offsetXLimits.value),
            currentOffset.y.coerceIn(offsetYLimits.value),
        )


        val newTransform = Transformation(offset = currentOffset, scale = zoomToScale(zoom.value))
        transformation.value = newTransform
    }

    private fun offsetLimits(targetSize: Float, areaSize: Float): ClosedFloatingPointRange<Float> {
        val areaCenter = areaSize / 2
        val targetCenter = targetSize / 2
        val extra = (targetCenter - areaCenter).coerceAtLeast(0f)
        return -extra..extra
    }

    suspend fun performFling(spec: DecayAnimationSpec<Offset>) {
        val scale = transformation.value.scale
        val velocity = velocityTracker.calculateVelocity().div(scale)
        velocityTracker.resetTracking()

        var lastValue = Offset(0f, 0f)
        AnimationState(
            typeConverter = Offset.VectorConverter,
            initialValue = Offset.Zero,
            initialVelocity = Offset(velocity.x, velocity.y),
        ).animateDecay(spec) {
            val delta = value - lastValue
            lastValue = value

            if (scrollState.value == null) {
                val canPanHorizontally = when {
                    delta.x < 0 -> canPanLeft()
                    delta.x > 0 -> canPanRight()
                    else -> false
                }
                val canPanVertically = when {
                    delta.y > 0 -> canPanDown()
                    delta.y < 0 -> canPanUp()
                    else -> false
                }
                if (!canPanHorizontally && !canPanVertically) {
                    this.cancelAnimation()
                    return@animateDecay
                }
            }

            addPan(delta)
        }
    }

    private fun canPanUp(): Boolean {
        return currentOffset.y > offsetYLimits.value.start
    }

    private fun canPanDown(): Boolean {
        return currentOffset.y < offsetYLimits.value.endInclusive
    }

    private fun canPanLeft(): Boolean {
        return currentOffset.x > offsetXLimits.value.start
    }

    private fun canPanRight(): Boolean {
        return currentOffset.x < offsetXLimits.value.endInclusive
    }

    fun addPan(pan: Offset) {
        val zoomToScale = zoomToScale(zoom.value)
        val newOffset = currentOffset + (pan * zoomToScale)
        currentOffset = newOffset
        applyLimits()
        val delta = (newOffset - currentOffset)

        when (scrollOrientation.value) {
            Vertical -> applyScroll((delta / -zoomToScale).y)
            Horizontal -> applyScroll((delta / -zoomToScale).x)
            null -> {}
        }
    }

    fun addPan(change: PointerInputChange, pan: Offset) {
        velocityTracker.addPointerInputChange(change)
        addPan(pan)
    }

    private fun applyScroll(value: Float) {
        if (value == 0f) return
        val scrollState = this.scrollState.value
        if (scrollState != null) {
            scrollScope.launch { scrollState.scrollBy(if (scrollReversed.value) -value else value) }
        }
    }

    fun multiplyZoom(zoomMultiplier: Float, focus: Offset = Offset.Zero) {
        setZoom(zoom.value * zoomMultiplier, focus)
    }

    fun addZoom(addZoom: Float, focus: Offset = Offset.Zero) {
        setZoom(zoom.value + addZoom, focus)
    }

    fun setScrollState(scrollableState: ScrollableState?) {
        this.scrollState.value = scrollableState
    }

    fun setScrollOrientation(orientation: Orientation, reversed: Boolean) {
        this.scrollOrientation.value = orientation
        this.scrollReversed.value = reversed
    }

    fun setZoom(zoom: Float, focus: Offset = Offset.Zero) {
        val newZoom = zoom.coerceIn(zoomLimits.value)
        val newOffset = Transformation.offsetOf(
            point = transformation.value.pointOf(focus),
            transformedPoint = focus,
            scale = zoomToScale(newZoom)
        )
        this.currentOffset = newOffset
        this.zoom.value = newZoom
        applyLimits()
    }

    fun apply(other: ScreenScaleState) {
        currentOffset = other.currentOffset

        if (other.targetSize.value != this.targetSize.value || other.zoom.value != this.zoom.value) {
            this.areaSize.value = other.areaSize.value
            this.targetSize.value = Size(
                width = other.targetSize.value.width,
                height = other.targetSize.value.height
            )
            zoomLimits.value = (scaleForFullVisibility() / scaleFor100PercentZoom())..zoomLimits.value.endInclusive
            this.zoom.value = other.zoom.value
        }
        applyLimits()
    }

    data class Transformation(
        val offset: Offset,
        val scale: Float,
    ) {
        fun pointOf(transformedPoint: Offset) = (transformedPoint - offset) / scale

        companion object {
            // is derived from the equation `point = (transformedPoint - offset) / scale`
            fun offsetOf(point: Offset, transformedPoint: Offset, scale: Float) =
                transformedPoint - point * scale
        }
    }
}

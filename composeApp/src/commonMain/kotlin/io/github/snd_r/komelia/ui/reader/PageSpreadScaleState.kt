package io.github.snd_r.komelia.ui.reader

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.ui.reader.LayoutScaleType.FIT_HEIGHT
import io.github.snd_r.komelia.ui.reader.LayoutScaleType.FIT_WIDTH
import io.github.snd_r.komelia.ui.reader.LayoutScaleType.ORIGINAL
import io.github.snd_r.komelia.ui.reader.LayoutScaleType.SCREEN
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.max
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class PageSpreadScaleState {
    private var zoomLimits = 1.0f..5f
    private var offset = Offset.Zero
    private var zoom = 1f

    private var areaSize = Size(1f, 1f)
    private var targetSize = Size(1f, 1f)

    val transformation = MutableStateFlow(Transformation(offset = offset, scale = zoomToScale(zoom)))

    private fun scaleFor100PercentZoom() = max(areaSize.width / targetSize.width, areaSize.height / targetSize.height)
    private fun scaleForFullVisibility() = min(areaSize.width / targetSize.width, areaSize.height / targetSize.height)
    private fun zoomToScale(zoom: Float) = zoom * scaleFor100PercentZoom()

    private fun limitTargetInsideArea(areaSize: Size, targetSize: Size) {
        this.areaSize = areaSize
        this.targetSize = targetSize
        zoomLimits = (scaleForFullVisibility() / scaleFor100PercentZoom())..zoomLimits.endInclusive
        applyLimits()
    }

    private fun applyLimits() {
        val scale = zoomToScale(zoom)
        val offsetXLimits = offsetLimits(targetSize.width * scale, areaSize.width)
        val offsetYLimits = offsetLimits(targetSize.height * scale, areaSize.height)

        zoom = zoom.coerceIn(zoomLimits)
        offset = Offset(
            offset.x.coerceIn(offsetXLimits),
            offset.y.coerceIn(offsetYLimits),
        )

        val newTransform = Transformation(offset = offset, scale = zoomToScale(zoom))
        transformation.value = newTransform
//        logger.info { "Applied scale transforms: $newTransform" }
    }

    private fun offsetLimits(targetSize: Float, areaSize: Float): ClosedFloatingPointRange<Float> {
        val areaCenter = areaSize / 2
        val targetCenter = targetSize / 2
        val extra = (targetCenter - areaCenter).coerceAtLeast(0f)
        return -extra..extra
    }

    fun addPan(pan: Offset) {
        offset += pan * zoomToScale(zoom)
        applyLimits()
    }

    fun addZoom(zoomMultiplier: Float, focus: Offset = Offset.Zero) {
        setZoom(zoom * zoomMultiplier, focus)
    }

    private fun setZoom(zoom: Float, focus: Offset = Offset.Zero) {
        val newZoom = zoom.coerceIn(zoomLimits)
        val newOffset = Transformation.offsetOf(
            point = transformation.value.pointOf(focus),
            transformedPoint = focus,
            scale = zoomToScale(newZoom)
        )
        this.offset = newOffset
        this.zoom = newZoom
        applyLimits()
    }


    fun limitPagesInsideArea(
        pages: List<PageMetadata>,
        areaSize: IntSize,
//        constrainedContentSize: IntSize,
//        actualContentSize: IntSize,
        maxPageSize: IntSize,
        scaleType: LayoutScaleType
    ) {
        val constrainedContentSize = pages
            .map { it.contentSizeForArea(maxPageSize) }
            .fold(IntSize.Zero) { total, current ->
                IntSize(
                    width = (total.width + current.width),
                    height = max(total.height, current.height)
                )
            }

        limitTargetInsideArea(
            areaSize = areaSize.toSize(),
            targetSize = if (constrainedContentSize == IntSize.Zero) Size.Unspecified else constrainedContentSize.toSize()
        )
        when (scaleType) {
            SCREEN -> setZoom(0f)
            FIT_WIDTH -> {
                if (constrainedContentSize.width < areaSize.width) setZoom(1f)
                else setZoom(0f)
            }

            FIT_HEIGHT -> {
                if (constrainedContentSize.height < areaSize.height) setZoom(1f)
                else setZoom(0f)
            }

            ORIGINAL -> {
                val actualPageSize = pages.mapNotNull { it.size }.fold(IntSize.Zero) { total, current ->
                    IntSize((total.width + current.width), max(total.height, current.height))
                }

                if (actualPageSize.width > areaSize.width || actualPageSize.height > areaSize.height) {
                    val newZoom = max(
                        actualPageSize.width.toFloat() / constrainedContentSize.width,
                        actualPageSize.height.toFloat() / constrainedContentSize.height
                    ) / scaleFor100PercentZoom()
                    setZoom(newZoom)

                } else setZoom(0f)
            }
        }

        val offsetXLimits = offsetLimits(targetSize.width * zoomToScale(zoom), this.areaSize.width)
        val offsetYLimits = offsetLimits(targetSize.height * zoomToScale(zoom), this.areaSize.height)
        addPan(Offset(offsetXLimits.endInclusive, offsetYLimits.endInclusive))
        applyLimits()
    }

    fun canZoomIn(): Boolean {
        return zoom < zoomLimits.endInclusive
    }

    fun canZoomOUt(): Boolean {
        return zoom > zoomLimits.start
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

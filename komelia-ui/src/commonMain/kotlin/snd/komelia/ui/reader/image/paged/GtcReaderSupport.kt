package snd.komelia.ui.reader.image.paged

import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import snd.komelia.image.ReaderImage
import snd.komelia.settings.model.PageDisplayLayout
import snd.komelia.settings.model.PageDisplayLayout.SINGLE_PAGE
import snd.komelia.ui.reader.image.PageMetadata
import snd.komelia.ui.reader.image.ScreenScaleState

/**
 * GTC (device-height orientation): when enabled, a landscape page (its longest side
 * horizontal) viewed on a portrait device is rotated so its longest side aligns with
 * the device height, and the image is scaled to fit before display.
 * Only applies to single page layout - rotating a two-page spread isn't well-defined.
 *
 * All GTC-specific decision/scaling logic lives here so future upstream merges only
 * need to reconcile the small one-line call sites left in PagedReaderState.kt and
 * PagedReaderContent.kt, rather than colliding with inlined GTC logic.
 */
object GtcReaderSupport {

    fun isApplicable(
        gtcModeEnabled: Boolean,
        layout: PageDisplayLayout,
        pageMetadata: PageMetadata,
        containerSize: IntSize,
    ): Boolean {
        return gtcModeEnabled &&
                layout == SINGLE_PAGE &&
                pageMetadata.isLandscape() &&
                containerSize.height > containerSize.width
    }

    fun adjustedContainerSize(
        gtcModeEnabled: Boolean,
        layout: PageDisplayLayout,
        pages: List<PageMetadata>,
        containerSize: IntSize,
    ): IntSize {
        val page = pages.singleOrNull() ?: return containerSize
        return if (isApplicable(gtcModeEnabled, layout, page, containerSize))
            IntSize(containerSize.height, containerSize.width)
        else containerSize
    }

    fun swapIfApplicable(applicable: Boolean, size: IntSize): IntSize =
        if (applicable) IntSize(size.height, size.width) else size

    suspend fun updateImage(
        image: ReaderImage,
        maxPageSize: IntSize,
        stretchToFit: Boolean,
    ) {
        val imageDisplaySize = image.calculateSizeForArea(maxPageSize, stretchToFit) ?: maxPageSize
        image.requestUpdate(
            visibleDisplaySize = IntRect(0, 0, imageDisplaySize.width, imageDisplaySize.height),
            zoomFactor = 1f,
            maxDisplaySize = maxPageSize
        )
    }

    /**
     * fitToScreenSize is computed against a width/height-swapped maxPageSize (see
     * adjustedContainerSize), so it is expressed in rotated-page space. Locks the view
     * to a fit-to-screen scale, swapping the target size back to real screen space first
     * so the zoom-limit math compares matching axes.
     */
    fun applyFixedScale(scaleState: ScreenScaleState, fitToScreenSize: IntSize) {
        scaleState.setTargetSize(swapIfApplicable(true, fitToScreenSize).toSize())
        scaleState.setZoom(0f)
    }

    data class RotationLayout(
        val rotate: Boolean,
        val childConstraints: Constraints,
    )

    fun computeRotationLayout(
        gtcModeEnabled: Boolean,
        pageMetadata: PageMetadata,
        constraints: Constraints,
    ): RotationLayout {
        val containerIsPortrait = constraints.maxHeight > constraints.maxWidth
        val rotate = gtcModeEnabled && pageMetadata.isLandscape() && containerIsPortrait
        val childConstraints = if (rotate) {
            constraints.copy(
                minWidth = constraints.minHeight,
                maxWidth = constraints.maxHeight,
                minHeight = constraints.minWidth,
                maxHeight = constraints.maxWidth,
            )
        } else constraints
        return RotationLayout(rotate, childConstraints)
    }
}

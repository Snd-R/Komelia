package snd.komelia.ui.reader.image.panels

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import io.github.oshai.kotlinlogging.KotlinLogging
import snd.komelia.image.ImageRect
import snd.komelia.settings.model.PagedReadingDirection
import snd.komelia.settings.model.PagedReadingDirection.LEFT_TO_RIGHT
import snd.komelia.settings.model.PagedReadingDirection.RIGHT_TO_LEFT
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val IMAGE_WIDTH = 1000.0f
private const val IMAGE_HEIGHT = 1000.0f
private val logger = KotlinLogging.logger { }
fun sortPanels(
    panels: List<ImageRect>,
    imageSize: IntSize,
    readingDirection: PagedReadingDirection
): List<ImageRect> {
//    val start = TimeSource.Monotonic.markNow()
    val scaleRatio = max(
        IMAGE_WIDTH / imageSize.width,
        IMAGE_HEIGHT / imageSize.height
    )
    val scaledPanels = panels.associateBy {
        val scaled = Rect(
            left = it.left * scaleRatio,
            top = it.top * scaleRatio,
            right = it.right * scaleRatio,
            bottom = it.bottom * scaleRatio
        )
        when (readingDirection) {
            RIGHT_TO_LEFT -> scaled
            LEFT_TO_RIGHT -> flipX(scaled)
        }
    }
    val rows = sortByRows(scaledPanels.keys)
    val result = rows.flatten().mapNotNull { scaledPanels[it] }
//    val duration = TimeSource.Monotonic.markNow() - start
//    logger.info { "sorted panels in $duration" }
    return result

}

private fun flipX(panel: Rect) = panel.copy(
    left = IMAGE_WIDTH - panel.right,
    right = IMAGE_WIDTH - panel.left,
)

private fun sortByRows(panels: Collection<Rect>): Collection<List<Rect>> {
    val rows = LinkedHashMap<Float, List<Rect>>()
    var currentRowStart = 0f
    for (panel in panels.sortedBy { it.top }) {

        val thisRow = rows[currentRowStart]
        if (thisRow != null && !panelBelongsToRow(row = thisRow, panel = panel)
        ) {
            currentRowStart = panel.top
        }

        val currentRow = rows[currentRowStart]
        if (currentRow == null) {
            rows[currentRowStart] = listOf(panel)
        } else {
            rows[currentRowStart] = insertPanel(currentRow, panel)
        }
    }
    return rows.values
}

private fun insertPanel(row: List<Rect>, panel: Rect): List<Rect> {
    val row = row.toMutableList()
    val commonVerticalEdgePanels =
        row.filter { rowPanel -> (rowPanel.top < panel.bottom) && (panel.top < rowPanel.bottom) }
    val rightPanels = commonVerticalEdgePanels.filter { it.left > panel.left }
    val closestRightPanel = rightPanels
        .minByOrNull { it.left }
    val leftPanels = commonVerticalEdgePanels.filter { it.right < panel.right }
    val closestLeftPanel = leftPanels
        .maxByOrNull { it.right }
    val commonHorizontalEdgePanels =
        row.filter { rowPanel -> (rowPanel.left < panel.right) && (panel.left < rowPanel.right) }

    val topNeighbors = commonHorizontalEdgePanels.filter { it.top < panel.bottom }
        .filter { it.horizontalEdgePercentage(panel) > .2f }

    val closestTopNeighbor = if (topNeighbors.isNotEmpty()) {
        val closestTop = topNeighbors.maxBy { it.top }
        topNeighbors.filter { abs(it.top - closestTop.top) < 50 }.minByOrNull { it.left }
    } else {
        commonHorizontalEdgePanels.maxByOrNull { it.top }
    }


    if (closestTopNeighbor != null) {
        when {
            closestLeftPanel != null && closestTopNeighbor == closestLeftPanel ->
                addOverlappingPanel(row, closestTopNeighbor, panel)

            closestRightPanel != null && closestTopNeighbor == closestRightPanel ->
                row.add(row.indexOf(closestRightPanel) + 1, panel)

            closestRightPanel != null -> {
                val verticalEdge = closestRightPanel.verticalEdgePercentage(panel)
                val horizontalEdge = closestTopNeighbor.horizontalEdgePercentage(panel)
                when {
                    verticalEdge > .8f -> row.add(row.indexOf(closestRightPanel) + 1, panel)
                    verticalEdge < horizontalEdge -> row.add(row.indexOf(closestTopNeighbor) + 1, panel)
                    else -> row.add(row.indexOf(closestTopNeighbor) + 1, panel)
                }
            }

            //full overlap
            panel.isInside(closestTopNeighbor) -> {
                // if on the right part of the panel insert before
                if (closestTopNeighbor.right - panel.right < closestTopNeighbor.width / 2)
                    row.add(row.indexOf(closestTopNeighbor), panel)
                else
                    row.add(row.indexOf(closestTopNeighbor) + 1, panel)
            }

            else -> row.add(row.indexOf(closestTopNeighbor) + 1, panel)
        }

    } else if (closestLeftPanel != null) {
        row.add(row.indexOf(closestLeftPanel), panel)

    } else if (closestRightPanel != null) {
        row.add(row.indexOf(closestRightPanel) + 1, panel)

    } else row.add(panel)

    return row
}

private fun Rect.isInside(other: Rect): Boolean {
    if (!this.overlaps(other)) return false
    val intersection = this.intersect(other)
    return intersection.width == this.width && intersection.height == this.height
}

private fun addOverlappingPanel(row: MutableList<Rect>, leftAndTopOverlapping: Rect, panel: Rect) {
    if (!leftAndTopOverlapping.overlaps(panel)) {
        row.add(row.indexOf(leftAndTopOverlapping), panel)
        return
    }

    val intersection = leftAndTopOverlapping.intersect(panel)
    val minPanelWidth = min(leftAndTopOverlapping.width, panel.width)
    val minPanelHeight = min(leftAndTopOverlapping.height, panel.height)
    val intersectionWidthPercentage = (intersection.width / minPanelWidth)
    val intersectionHeightPercentage = (intersection.height / minPanelHeight)
    if (intersectionWidthPercentage > .8f && intersectionHeightPercentage > .8f) {
        row.add(row.indexOf(leftAndTopOverlapping), panel)
        return
    }

    val commonEdgeWidth = min(leftAndTopOverlapping.right, panel.right) - max(leftAndTopOverlapping.left, panel.left)
    val newPanelRatio = commonEdgeWidth / panel.width
    val topPanelRatio = commonEdgeWidth / leftAndTopOverlapping.width

    // if more than 80% of panel is above insert after
    if (newPanelRatio > .8f || topPanelRatio > .8f) {
        row.add(row.indexOf(leftAndTopOverlapping) + 1, panel)
    } else { // else consider it to be a neighboring panel to the right
        row.add(row.indexOf(leftAndTopOverlapping), panel)
    }
}


private fun panelBelongsToRow(
    row: List<Rect>,
    panel: Rect,
): Boolean {
    if (row.isEmpty()) return false

    val commonHorizontalEdgePanels =
        row.filter { rowPanel -> (rowPanel.left < panel.right) && (panel.left < rowPanel.right) }

    if (commonHorizontalEdgePanels.isNotEmpty()) {

        val (panelAboveOrBelow, commonTopWidth) = commonHorizontalEdgePanels
            .map { it to min(it.right, panel.right) - max(it.left, panel.left) }
            .maxBy { (_, commonWidth) -> commonWidth }
        val upperRowIsFullWidth = panelAboveOrBelow.width / IMAGE_WIDTH > 0.9f
        val thisPanelIsFullWidth = panel.width / IMAGE_WIDTH > 0.9f

        val maxHeightPanel = commonHorizontalEdgePanels.maxBy { it.height }
        val isFullyBellow = panel.bottom > maxHeightPanel.bottom
        if ((upperRowIsFullWidth || thisPanelIsFullWidth) && isFullyBellow)
            return false
    }

    val commonVerticalEdgePanels =
        row.filter { rowPanel -> (rowPanel.top < panel.bottom) && (panel.top < rowPanel.bottom) }
    for (rowPanel in commonVerticalEdgePanels) {
        if (rowPanel.overlaps(panel)) {
            val intersection = rowPanel.intersect(panel)
            val minPanelWidth = min(rowPanel.width, panel.width)
            val minPanelHeight = min(rowPanel.height, panel.height)

            val intersectionWidthPercentage = (intersection.width / minPanelWidth)
            val intersectionHeightPercentage = (intersection.height / minPanelHeight)
            if (intersectionWidthPercentage > .8f && intersectionHeightPercentage > .8f)
                return true
        }

        val commonEdgeHeight = min(rowPanel.bottom, panel.bottom) - max(rowPanel.top, panel.top)

        val panelEdgePercentage = commonEdgeHeight / panel.height
        val commonEdgePercentage = commonEdgeHeight / rowPanel.height
        if (panelEdgePercentage > .8f || commonEdgePercentage > .25f)
            return true

    }

    return false

}

private fun Rect.horizontalEdgePercentage(to: Rect): Float {
    val commonEdgeWidth = min(this.right, to.right) - max(this.left, to.left)
    return commonEdgeWidth / this.width
}

private fun Rect.verticalEdgePercentage(to: Rect): Float {
    val commonEdgeWidth = min(this.bottom, to.bottom) - max(this.top, to.top)
    return commonEdgeWidth / this.height
}

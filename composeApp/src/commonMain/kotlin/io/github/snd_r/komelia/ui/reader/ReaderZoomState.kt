package io.github.snd_r.komelia.ui.reader

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

//class ReaderZoomState : PagedReaderZoomState {
//    private val currentSpreadScale = PageSpreadScaleState()
//    override val scaleTransformation = currentSpreadScale.transformation
//
//    override fun addZoom(zoomMultiplier: Float, focus: Offset) {
//        if (zoomMultiplier == 1.0f) return
//        currentSpreadScale.addZoom(zoomMultiplier, focus)
////        resamplePages()
//    }
//
//    override fun addPan(pan: Offset) {
//        currentSpreadScale.addPan(pan)
//    }
//
//    fun limitPagesInsideArea(
//        pages: List<PageMetadata>,
//        areaSize: IntSize,
//        maxPageSize: IntSize,
//        scaleType: LayoutScaleType
//    ) {
//        currentSpreadScale.limitPagesInsideArea(
//            pages = pages,
//            areaSize = areaSize,
//            maxPageSize = maxPageSize,
//            scaleType = scaleType
//        )
//    }
//}
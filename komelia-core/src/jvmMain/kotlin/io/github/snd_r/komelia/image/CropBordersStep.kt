package io.github.snd_r.komelia.image

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.VipsImage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

class CropBordersStep(
    private val enabled: StateFlow<Boolean>
) : ProcessingStep {
    override suspend fun process(pageId: ReaderImage.PageId, image: VipsImage): PlatformImage? {
        if (!enabled.value) return null
        val result = measureTimedValue {
            val trim = image.findTrim()
            image.extractArea(trim)
        }
        logger.info { "page ${pageId.pageNumber} completed border crop in ${result.duration}" }
        return result.value
    }

    override suspend fun addChangeListener(callback: () -> Unit) {
        enabled.drop(1).collect { callback() }
    }
}
package io.github.snd_r.komelia.image

import io.github.snd_r.VipsImage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop

class CropBordersStep(
    private val enabled: StateFlow<Boolean>
) : ProcessingStep {
    override suspend fun process(pageId: ReaderImage.PageId, image: VipsImage): PlatformImage? {
        if (!enabled.value) return null
        val trim = image.findTrim()
        return image.extractArea(trim)
    }

    override suspend fun addChangeListener(callback: () -> Unit) {
        enabled.drop(1).collect { callback() }
    }
}
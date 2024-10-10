package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ImageProcessingPipeline {
    private val pipelineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val steps: MutableList<ProcessingStep> = mutableListOf()

    private val _changeFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)
    val changeFlow = _changeFlow.asSharedFlow()

    suspend fun process(pageId: PageId, image: PlatformImage): PlatformImage {
        var imageResult = image
        for (step in steps) {
            val oldResult = imageResult
            step.process(pageId, oldResult)?.let {
                imageResult = it
                oldResult.close()
            }
        }
        return imageResult
    }

    fun addStep(step: ProcessingStep) {
        steps.add(step)
        pipelineScope.launch { step.addChangeListener { _changeFlow.tryEmit(Unit) } }
        _changeFlow.tryEmit(Unit)
    }
}

interface ProcessingStep {
    suspend fun process(pageId: PageId, image: PlatformImage): PlatformImage?
    suspend fun addChangeListener(callback: () -> Unit)
}
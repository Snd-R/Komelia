package snd.komelia.image

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeRfDetr
import snd.komelia.onnxruntime.OnnxRuntimeRfDetr.DetectResult
import snd.komelia.updates.OnnxModelDownloader

abstract class KomeliaPanelDetector(
    private val ortRfDetr: OnnxRuntimeRfDetr,
    private val executionProvider: OnnxRuntimeExecutionProvider,
    private val deviceId: StateFlow<Int>,
    private val updateFlow: Flow<OnnxModelDownloader.CompletionEvent.PanelModelDownloaded>
) {
    private val _isAvailable = MutableStateFlow(false)
    val isAvailable = _isAvailable.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun detect(image: KomeliaImage): List<DetectResult> {
        check(isAvailable.value) { "model was not initialized" }

        return ortRfDetr.detect(image)
    }

    fun closeCurrentSession() {
        if (!isAvailable.value) return
        ortRfDetr.closeCurrentSession()
    }


    fun initialize() {
        updateModelPath()
        ortRfDetr.setExecutionProvider(executionProvider, deviceId.value)

        updateFlow.onEach { updateModelPath() }.launchIn(coroutineScope)
        deviceId.onEach { ortRfDetr.setExecutionProvider(executionProvider, deviceId.value) }
            .launchIn(coroutineScope)
    }

    private fun updateModelPath() {
        val modelPath = getModelPath()
        if (modelPath != null) {
            ortRfDetr.setModelPath(modelPath)
        }
        _isAvailable.value = modelPath != null
    }

    protected abstract fun getModelPath(): String?
}


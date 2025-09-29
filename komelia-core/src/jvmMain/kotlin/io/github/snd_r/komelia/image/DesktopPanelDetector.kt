package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.AppDirectories
import io.github.snd_r.komelia.updates.OnnxModelDownloader.CompletionEvent.PanelModelDownloaded
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeRfDetr
import kotlin.io.path.exists

class DesktopPanelDetector(
    rfDetr: OnnxRuntimeRfDetr,
    executionProvider: OnnxRuntimeExecutionProvider,
    deviceId: StateFlow<Int>,
    updateFlow: Flow<PanelModelDownloaded>,
) : KomeliaPanelDetector(rfDetr, executionProvider, deviceId, updateFlow) {
    override fun getModelPath(): String? {
        val path = AppDirectories.panelDetectionModelPath
        return if (path.exists()) path.toString() else null
    }
}

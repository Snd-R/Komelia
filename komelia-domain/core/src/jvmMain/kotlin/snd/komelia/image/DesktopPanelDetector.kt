package snd.komelia.image

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.AppDirectories
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeRfDetr
import snd.komelia.updates.OnnxModelDownloader.CompletionEvent.PanelModelDownloaded
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

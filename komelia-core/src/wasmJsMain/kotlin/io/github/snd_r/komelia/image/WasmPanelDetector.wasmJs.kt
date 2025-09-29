package io.github.snd_r.komelia.image

import kotlinx.coroutines.flow.Flow
import snd.komelia.onnxruntime.OnnxRuntimeRfDetr

class WasmPanelDetector(
    rfDetr: OnnxRuntimeRfDetr,
    updateFlow: Flow<Unit>
) : KomeliaPanelDetector(rfDetr, updateFlow) {
    override fun isModelAvailable(): Boolean {
        return false
    }

}

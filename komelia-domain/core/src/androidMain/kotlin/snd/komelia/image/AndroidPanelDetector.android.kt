package snd.komelia.image

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeRfDetr
import snd.komelia.updates.OnnxModelDownloader.CompletionEvent.PanelModelDownloaded
import java.nio.file.Path
import kotlin.io.path.exists

class AndroidPanelDetector(
    rfDetr: OnnxRuntimeRfDetr,
    executionProvider: OnnxRuntimeExecutionProvider,
    deviceId: StateFlow<Int>,
    updateFlow: Flow<PanelModelDownloaded>,
    private val dataDir: Path,
) : KomeliaPanelDetector(rfDetr, executionProvider, deviceId, updateFlow) {
    private val logger = KotlinLogging.logger { }

    override fun getModelPath(): String? {
        val path = dataDir.resolve("rf-detr-nano.onnx")
        logger.info { "panel detector path string $path" }
        val exists =  path.exists()
        return if (exists) path.toString() else null
    }

}

package snd.komelia.ui.settings.imagereader.onnxruntime

import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeSharedLibraries

actual fun isOnnxRuntimeSupported() = true
actual fun supportedOnnxRuntimeExecutionProviders(): List<OnnxRuntimeExecutionProvider> {
    return emptyList()
}

actual fun isOnnxRuntimeInstalled() = OnnxRuntimeSharedLibraries.isAvailable
actual fun onnxRuntimeLoadError(): String? = null
actual val ortExecutionProvider: OnnxRuntimeExecutionProvider? = OnnxRuntimeExecutionProvider.CPU

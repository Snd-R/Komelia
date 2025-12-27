package snd.komelia.ui.settings.imagereader.onnxruntime

import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider

actual fun isOnnxRuntimeSupported() = false
actual fun supportedOnnxRuntimeExecutionProviders(): List<OnnxRuntimeExecutionProvider> {
    return emptyList()
}

actual fun isOnnxRuntimeInstalled() = false
actual fun onnxRuntimeLoadError(): String? = null
actual val ortExecutionProvider: OnnxRuntimeExecutionProvider? = OnnxRuntimeExecutionProvider.CPU

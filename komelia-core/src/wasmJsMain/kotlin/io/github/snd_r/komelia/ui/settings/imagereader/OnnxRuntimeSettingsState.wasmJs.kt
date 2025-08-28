package io.github.snd_r.komelia.ui.settings.imagereader

import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider


actual fun isOnnxRuntimeSupported() = false
actual fun supportedOnnxRuntimeExecutionProviders(): List<OnnxRuntimeExecutionProvider> {
    return emptyList()
}

actual fun isOnnxRuntimeInstalled() = false
actual fun onnxRuntimeLoadError(): String? = null
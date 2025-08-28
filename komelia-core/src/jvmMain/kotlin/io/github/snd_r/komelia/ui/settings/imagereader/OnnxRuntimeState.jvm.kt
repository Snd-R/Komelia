package io.github.snd_r.komelia.ui.settings.imagereader

import io.github.snd_r.komelia.DesktopPlatform
import io.github.snd_r.komelia.DesktopPlatform.Linux
import io.github.snd_r.komelia.DesktopPlatform.Windows
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CUDA
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.DirectML
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.TENSOR_RT
import snd.komelia.onnxruntime.OnnxRuntimeSharedLibraries

actual fun isOnnxRuntimeSupported() = true

actual fun supportedOnnxRuntimeExecutionProviders(): List<OnnxRuntimeExecutionProvider> {
    return when (DesktopPlatform.Current) {
        Linux -> listOf(
            TENSOR_RT,
            CUDA,
//            ROCm,
            CPU,
        )

        Windows -> listOf(
            TENSOR_RT,
            CUDA,
//            ROCm,
            DirectML,
        )

        else -> listOf(CPU)
    }
}

actual fun isOnnxRuntimeInstalled() = OnnxRuntimeSharedLibraries.isAvailable
actual fun onnxRuntimeLoadError() = OnnxRuntimeSharedLibraries.loadErrorMessage
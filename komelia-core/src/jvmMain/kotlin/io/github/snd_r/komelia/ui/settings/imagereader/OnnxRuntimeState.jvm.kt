package io.github.snd_r.komelia.ui.settings.imagereader

import io.github.snd_r.komelia.DesktopPlatform
import io.github.snd_r.komelia.DesktopPlatform.Linux
import io.github.snd_r.komelia.DesktopPlatform.Windows
import snd.komelia.image.OnnxRuntimeExecutionProvider
import snd.komelia.image.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.image.OnnxRuntimeExecutionProvider.CUDA
import snd.komelia.image.OnnxRuntimeExecutionProvider.DirectML
import snd.komelia.image.OnnxRuntimeExecutionProvider.ROCm
import snd.komelia.image.OnnxRuntimeExecutionProvider.TENSOR_RT
import snd.komelia.image.OnnxRuntimeSharedLibraries

actual fun isOnnxRuntimeSupported() = true

actual fun supportedOnnxRuntimeExecutionProviders(): List<OnnxRuntimeExecutionProvider> {
    return when (DesktopPlatform.Current) {
        Linux -> listOf(
            TENSOR_RT,
            CUDA,
            ROCm,
            CPU,
        )

        Windows -> listOf(
            TENSOR_RT,
            CUDA,
            ROCm,
            DirectML,
        )

        else -> listOf(CPU)
    }
}

actual fun isOnnxRuntimeInstalled() = OnnxRuntimeSharedLibraries.isAvailable
actual fun onnxRuntimeLoadError() = OnnxRuntimeSharedLibraries.loadErrorMessage
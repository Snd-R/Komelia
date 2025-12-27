package snd.komelia.ui.settings.imagereader.onnxruntime

import snd.komelia.DesktopPlatform
import snd.komelia.DesktopPlatform.Linux
import snd.komelia.DesktopPlatform.Windows
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CUDA
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.DirectML
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.TENSOR_RT
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.WEBGPU
import snd.komelia.onnxruntime.OnnxRuntimeSharedLibraries

actual fun isOnnxRuntimeSupported() = true

actual fun supportedOnnxRuntimeExecutionProviders(): List<OnnxRuntimeExecutionProvider> {
    return when (DesktopPlatform.Current) {
        Linux -> listOf(
            TENSOR_RT,
            CUDA,
//            ROCm,
//            CPU,
            WEBGPU
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

actual val ortExecutionProvider: OnnxRuntimeExecutionProvider? = OnnxRuntimeSharedLibraries.executionProvider
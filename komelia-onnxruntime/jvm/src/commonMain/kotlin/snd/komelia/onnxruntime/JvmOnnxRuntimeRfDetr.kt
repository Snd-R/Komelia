package snd.komelia.onnxruntime

import snd.jni.Managed
import snd.jni.NativePointer
import snd.komelia.image.KomeliaImage
import snd.komelia.image.VipsImage
import snd.komelia.image.toVipsImage
import snd.komelia.onnxruntime.OnnxRuntimeRfDetr.DetectResult

class JvmOnnxRuntimeRfDetr private constructor(
    private val onnxRuntime: JvmOnnxRuntime,
    ptr: NativePointer
) : Managed(ptr, Finalizer(ptr)), OnnxRuntimeRfDetr {
    override fun setExecutionProvider(provider: OnnxRuntimeExecutionProvider, deviceId: Int) {
        setExecutionProvider(provider.nativeOrdinal, deviceId);
    }

    private external fun setExecutionProvider(nativeEnumOrdinal: Int, deviceId: Int)
    external override fun setModelPath(modelPath: String)
    external override fun closeCurrentSession()
    override fun getAvailableDevices() = onnxRuntime.enumerateDevices()

    override fun detect(image: KomeliaImage): List<DetectResult> {
        return detect(image.toVipsImage())
    }

    external fun detect(image: VipsImage): List<DetectResult>

    companion object {
        fun create(ort: JvmOnnxRuntime): JvmOnnxRuntimeRfDetr {
            val ptr = create(ort.ptr)
            return JvmOnnxRuntimeRfDetr(ort, ptr)
        }

        @JvmStatic
        private external fun create(ptr: NativePointer): NativePointer

        @JvmStatic
        private external fun destroy(ptr: NativePointer)
    }

    private class Finalizer(private var ptr: Long) : Runnable {
        override fun run() = destroy(ptr)
    }
}

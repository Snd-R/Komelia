package snd.komelia.onnxruntime

import snd.jni.Managed
import snd.jni.NativePointer
import snd.komelia.image.KomeliaImage
import snd.komelia.image.VipsBackedImage
import snd.komelia.image.VipsImage
import snd.komelia.image.toVipsImage

class JvmOnnxRuntimeUpscaler private constructor(
    private val onnxRuntime: JvmOnnxRuntime,
    ptr: NativePointer
) : Managed(ptr, Finalizer(ptr)), OnnxRuntimeUpscaler {
    override fun setExecutionProvider(provider: OnnxRuntimeExecutionProvider, deviceId: Int) {
        setExecutionProvider(provider.nativeOrdinal, deviceId);
    }

    private external fun setExecutionProvider(nativeEnumOrdinal: Int, deviceId: Int)
    external override fun setModelPath(modelPath: String)
    external override fun setTileSize(tileSize: Int)
    external override fun closeCurrentSession()
    override fun getAvailableDevices() = onnxRuntime.enumerateDevices()

    override fun upscale(image: KomeliaImage): KomeliaImage {
        val upscaled = upscale(image.toVipsImage())
        return VipsBackedImage(upscaled)
    }

    external fun upscale(image: VipsImage): VipsImage

    companion object {
        fun create(ort: JvmOnnxRuntime): JvmOnnxRuntimeUpscaler {
            val ptr = create(ort.ptr)
            return JvmOnnxRuntimeUpscaler(ort, ptr)
        }

        @JvmStatic
        private external fun create(ortPtr: NativePointer): NativePointer

        @JvmStatic
        private external fun destroy(ptr: NativePointer)
    }

    private class Finalizer(private var ptr: Long) : Runnable {
        override fun run() = destroy(ptr)
    }
}
package snd.komelia.onnxruntime

import snd.jni.Managed
import snd.jni.NativePointer

class JvmOnnxRuntime private constructor(
    internal val ptr: NativePointer
) : Managed(ptr, Finalizer(ptr)), OnnxRuntime {

    external override fun enumerateDevices(): List<DeviceInfo>

    companion object {
        @JvmStatic
        external fun create(): JvmOnnxRuntime

        @JvmStatic
        private external fun destroy(ptr: NativePointer)
    }

    private class Finalizer(private var ptr: Long) : Runnable {
        override fun run() = destroy(ptr)
    }
}

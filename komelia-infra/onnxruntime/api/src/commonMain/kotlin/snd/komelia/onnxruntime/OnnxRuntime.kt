package snd.komelia.onnxruntime

interface OnnxRuntime {
    fun enumerateDevices(): List<DeviceInfo>

}


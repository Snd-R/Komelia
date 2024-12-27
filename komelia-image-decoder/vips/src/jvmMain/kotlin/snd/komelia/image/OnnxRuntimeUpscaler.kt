package snd.komelia.image

object OnnxRuntimeUpscaler {
    external fun init(provider: String)

    external fun setTileSize(tileSize: Int)

    external fun setModelPath(modelPath: String)

    external fun setDeviceId(deviceId: Int)

    external fun enumerateDevices(): List<DeviceInfo>

    external fun closeCurrentSession()

    external fun upscale(image: VipsImage): VipsImage

    data class DeviceInfo(
        val name: String,
        val id: Int,
        val memory: Long,
    )

    class OrtException : RuntimeException {
        constructor() : super()
        constructor(message: String) : super(message)
    }
}
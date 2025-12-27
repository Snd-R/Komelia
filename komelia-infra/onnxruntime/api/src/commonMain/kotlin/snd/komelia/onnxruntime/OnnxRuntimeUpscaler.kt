package snd.komelia.onnxruntime

import snd.komelia.image.KomeliaImage

interface OnnxRuntimeUpscaler {
    fun setExecutionProvider(provider: OnnxRuntimeExecutionProvider, deviceId: Int)
    fun setModelPath(modelPath: String)
    fun setTileSize(tileSize: Int)
    fun closeCurrentSession()
    fun getAvailableDevices(): List<DeviceInfo>
    fun upscale(image: KomeliaImage): KomeliaImage
}
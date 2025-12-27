package snd.komelia.onnxruntime

import snd.komelia.image.ImageRect
import snd.komelia.image.KomeliaImage

interface OnnxRuntimeRfDetr {
    fun setExecutionProvider(provider: OnnxRuntimeExecutionProvider, deviceId: Int)
    fun setModelPath(modelPath: String)
    fun closeCurrentSession()
    fun getAvailableDevices(): List<DeviceInfo>

    fun detect(image: KomeliaImage): List<DetectResult>

    data class DetectResult(
        val classId: Int,
        val confidence: Float,
        val boundingBox: ImageRect
    )
}

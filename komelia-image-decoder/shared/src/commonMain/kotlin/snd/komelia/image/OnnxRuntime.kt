package snd.komelia.image

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface OnnxRuntime {
    val provider: OnnxRuntimeExecutionProvider
    val availableDevices: Flow<List<DeviceInfo>>
    val upscaleMode: Flow<OnnxRuntimeUpscaleMode>
    val mangaJaNaiIsAvailable: Flow<Boolean>
    val selectedModelPath: StateFlow<String?>

    suspend fun upscale(image: KomeliaImage, cacheKey: String? = null): KomeliaImage?
    fun setOnnxModelPath(path: String?)
    fun setUpscaleMode(mode: OnnxRuntimeUpscaleMode)

    fun clearCache()

    data class DeviceInfo(
        val name: String,
        val id: Int,
        val memory: Long,
    )
}

enum class OnnxRuntimeExecutionProvider {
    TENSOR_RT,
    CUDA,
    ROCm,
    DirectML,
    CPU,
}

enum class OnnxRuntimeUpscaleMode {
    NONE,
    USER_SPECIFIED_MODEL,
    MANGAJANAI_PRESET,
}


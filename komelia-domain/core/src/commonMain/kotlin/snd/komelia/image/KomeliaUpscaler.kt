package snd.komelia.image

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface KomeliaUpscaler {
    val upscaleMode: Flow<UpscaleMode>
    val mangaJaNaiIsAvailable: Flow<Boolean>
    val userModelPath: StateFlow<PlatformFile?>

    suspend fun upscale(image: KomeliaImage, cacheKey: String? = null): KomeliaImage?

    fun setOnnxModelPath(path: PlatformFile?)
    fun setUpscaleMode(mode: UpscaleMode)
    fun clearCache()
    fun closeCurrentSession()

}

enum class UpscaleMode {
    NONE,
    USER_SPECIFIED_MODEL,
    MANGAJANAI_PRESET,
}

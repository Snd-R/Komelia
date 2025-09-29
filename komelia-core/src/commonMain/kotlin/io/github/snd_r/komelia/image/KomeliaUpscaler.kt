package io.github.snd_r.komelia.image

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.KomeliaImage

interface KomeliaUpscaler {
    val upscaleMode: Flow<UpscaleMode>
    val mangaJaNaiIsAvailable: Flow<Boolean>
    val userModelPath: StateFlow<String?>

    suspend fun upscale(image: KomeliaImage, cacheKey: String? = null): KomeliaImage?

    fun setOnnxModelPath(path: String?)
    fun setUpscaleMode(mode: UpscaleMode)
    fun clearCache()
    fun closeCurrentSession()

}

enum class UpscaleMode {
    NONE,
    USER_SPECIFIED_MODEL,
    MANGAJANAI_PRESET,
}

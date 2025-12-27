package snd.komelia.settings

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import snd.komelia.image.ReduceKernel
import snd.komelia.image.UpsamplingMode
import snd.komelia.image.UpscaleMode
import snd.komelia.settings.model.ContinuousReadingDirection
import snd.komelia.settings.model.LayoutScaleType
import snd.komelia.settings.model.PageDisplayLayout
import snd.komelia.settings.model.PagedReadingDirection
import snd.komelia.settings.model.ReaderFlashColor
import snd.komelia.settings.model.ReaderType

interface ImageReaderSettingsRepository {
    fun getReaderType(): Flow<ReaderType>
    suspend fun putReaderType(type: ReaderType)

    fun getStretchToFit(): Flow<Boolean>
    suspend fun putStretchToFit(stretch: Boolean)

    fun getCropBorders(): Flow<Boolean>
    suspend fun putCropBorders(trim: Boolean)

    fun getPagedReaderScaleType(): Flow<LayoutScaleType>
    suspend fun putPagedReaderScaleType(type: LayoutScaleType)

    fun getPagedReaderReadingDirection(): Flow<PagedReadingDirection>
    suspend fun putPagedReaderReadingDirection(direction: PagedReadingDirection)

    fun getPagedReaderDisplayLayout(): Flow<PageDisplayLayout>
    suspend fun putPagedReaderDisplayLayout(layout: PageDisplayLayout)

    fun getContinuousReaderReadingDirection(): Flow<ContinuousReadingDirection>
    suspend fun putContinuousReaderReadingDirection(direction: ContinuousReadingDirection)

    fun getContinuousReaderPadding(): Flow<Float>
    suspend fun putContinuousReaderPadding(padding: Float)

    fun getContinuousReaderPageSpacing(): Flow<Int>
    suspend fun putContinuousReaderPageSpacing(spacing: Int)

    fun getFlashOnPageChange(): Flow<Boolean>
    suspend fun putFlashOnPageChange(flash: Boolean)

    fun getFlashDuration(): Flow<Long>
    suspend fun putFlashDuration(duration: Long)

    fun getFlashEveryNPages(): Flow<Int>
    suspend fun putFlashEveryNPages(pages: Int)

    fun getFlashWith(): Flow<ReaderFlashColor>
    suspend fun putFlashWith(color: ReaderFlashColor)

    fun getDownsamplingKernel(): Flow<ReduceKernel>
    suspend fun putDownsamplingKernel(kernel: ReduceKernel)

    fun getLinearLightDownsampling(): Flow<Boolean>
    suspend fun putLinearLightDownsampling(linear: Boolean)

    fun getUpsamplingMode(): Flow<UpsamplingMode>
    suspend fun putUpsamplingMode(mode: UpsamplingMode)

    fun getLoadThumbnailPreviews(): Flow<Boolean>
    suspend fun putLoadThumbnailPreviews(load: Boolean)

    fun getVolumeKeysNavigation(): Flow<Boolean>
    suspend fun putVolumeKeysNavigation(enable: Boolean)

    fun getUpscalerMode(): Flow<UpscaleMode>
    suspend fun putUpscalerMode(mode: UpscaleMode)

    fun getOnnxRuntimeDeviceId(): Flow<Int>
    suspend fun putOnnxRuntimeDeviceId(deviceId: Int)

    fun getOnnxRuntimeTileSize(): Flow<Int>
    suspend fun putOnnxRuntimeTileSize(tileSize: Int)

    fun getUpscalerOnnxModel(): Flow<PlatformFile?>
    suspend fun putUpscalerOnnxModel(name: PlatformFile?)
}
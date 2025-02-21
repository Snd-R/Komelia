package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.image.UpsamplingMode
import io.github.snd_r.komelia.ui.reader.image.ReaderFlashColor
import io.github.snd_r.komelia.ui.reader.image.ReaderType
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import kotlinx.coroutines.flow.Flow
import snd.komelia.image.OnnxRuntimeUpscaleMode
import snd.komelia.image.ReduceKernel

interface ImageReaderSettingsRepository {
    fun getReaderType(): Flow<ReaderType>
    suspend fun putReaderType(type: ReaderType)

    fun getStretchToFit(): Flow<Boolean>
    suspend fun putStretchToFit(stretch: Boolean)

    fun getCropBorders(): Flow<Boolean>
    suspend fun putCropBorders(trim: Boolean)

    fun getPagedReaderScaleType(): Flow<PagedReaderState.LayoutScaleType>
    suspend fun putPagedReaderScaleType(type: PagedReaderState.LayoutScaleType)

    fun getPagedReaderReadingDirection(): Flow<PagedReaderState.ReadingDirection>
    suspend fun putPagedReaderReadingDirection(direction: PagedReaderState.ReadingDirection)

    fun getPagedReaderDisplayLayout(): Flow<PagedReaderState.PageDisplayLayout>
    suspend fun putPagedReaderDisplayLayout(layout: PagedReaderState.PageDisplayLayout)

    fun getContinuousReaderReadingDirection(): Flow<ContinuousReaderState.ReadingDirection>
    suspend fun putContinuousReaderReadingDirection(direction: ContinuousReaderState.ReadingDirection)

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

    fun getOnnxRuntimeMode(): Flow<OnnxRuntimeUpscaleMode>
    suspend fun putOnnxRuntimeMode(mode: OnnxRuntimeUpscaleMode)

    fun getOnnxRuntimeDeviceId(): Flow<Int>
    suspend fun putOnnxRuntimeDeviceId(deviceId: Int)

    fun getOnnxRuntimeTileSize(): Flow<Int>
    suspend fun putOnnxRuntimeTileSize(tileSize: Int)

    fun getSelectedOnnxModel(): Flow<String?>
    suspend fun putSelectedOnnxModel(name: String?)
}
package snd.komelia.db.repository

import io.github.snd_r.komelia.image.UpsamplingMode
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.ui.reader.image.ReaderFlashColor
import io.github.snd_r.komelia.ui.reader.image.ReaderType
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout
import kotlinx.coroutines.flow.Flow
import snd.komelia.db.ImageReaderSettings
import snd.komelia.db.SettingsStateActor
import snd.komelia.image.OnnxRuntimeUpscaleMode
import snd.komelia.image.ReduceKernel

class ActorReaderSettingsRepository(
    private val actor: SettingsStateActor<ImageReaderSettings>,
) : ImageReaderSettingsRepository {

    override fun getReaderType(): Flow<ReaderType> {
        return actor.mapState { it.readerType }
    }

    override suspend fun putReaderType(type: ReaderType) {
        actor.transform { settings -> settings.copy(readerType = type) }
    }

    override fun getStretchToFit(): Flow<Boolean> {
        return actor.mapState { it.stretchToFit }
    }

    override suspend fun putStretchToFit(stretch: Boolean) {
        actor.transform { it.copy(stretchToFit = stretch) }
    }

    override fun getCropBorders(): Flow<Boolean> {
        return actor.mapState { it.cropBorders }
    }

    override suspend fun putCropBorders(trim: Boolean) {
        actor.transform { it.copy(cropBorders = trim) }
    }

    override fun getPagedReaderScaleType(): Flow<LayoutScaleType> {
        return actor.mapState { it.pagedScaleType }
    }

    override suspend fun putPagedReaderScaleType(type: LayoutScaleType) {
        actor.transform { it.copy(pagedScaleType = type) }
    }

    override fun getPagedReaderReadingDirection(): Flow<PagedReaderState.ReadingDirection> {
        return actor.mapState { it.pagedReadingDirection }
    }

    override suspend fun putPagedReaderReadingDirection(direction: PagedReaderState.ReadingDirection) {
        actor.transform { it.copy(pagedReadingDirection = direction) }
    }

    override fun getPagedReaderDisplayLayout(): Flow<PageDisplayLayout> {
        return actor.mapState { it.pagedPageLayout }
    }

    override suspend fun putPagedReaderDisplayLayout(layout: PageDisplayLayout) {
        actor.transform { it.copy(pagedPageLayout = layout) }
    }

    override fun getContinuousReaderReadingDirection(): Flow<ContinuousReaderState.ReadingDirection> {
        return actor.mapState { it.continuousReadingDirection }
    }

    override suspend fun putContinuousReaderReadingDirection(direction: ContinuousReaderState.ReadingDirection) {
        actor.transform { it.copy(continuousReadingDirection = direction) }
    }

    override fun getContinuousReaderPadding(): Flow<Float> {
        return actor.mapState { it.continuousPadding }
    }

    override suspend fun putContinuousReaderPadding(padding: Float) {
        actor.transform { it.copy(continuousPadding = padding) }
    }

    override fun getContinuousReaderPageSpacing(): Flow<Int> {
        return actor.mapState { it.continuousPageSpacing }
    }

    override suspend fun putContinuousReaderPageSpacing(spacing: Int) {
        actor.transform { it.copy(continuousPageSpacing = spacing) }
    }

    override fun getFlashOnPageChange(): Flow<Boolean> {
        return actor.mapState { it.flashOnPageChange }
    }

    override suspend fun putFlashOnPageChange(flash: Boolean) {
        actor.transform { it.copy(flashOnPageChange = flash) }
    }

    override fun getFlashDuration(): Flow<Long> {
        return actor.mapState { it.flashDuration }
    }

    override suspend fun putFlashDuration(duration: Long) {
        actor.transform { it.copy(flashDuration = duration) }
    }

    override fun getFlashEveryNPages(): Flow<Int> {
        return actor.mapState { it.flashEveryNPages }
    }

    override suspend fun putFlashEveryNPages(pages: Int) {
        actor.transform { it.copy(flashEveryNPages = pages) }
    }

    override fun getFlashWith(): Flow<ReaderFlashColor> {
        return actor.mapState { it.flashWith }
    }

    override suspend fun putFlashWith(color: ReaderFlashColor) {
        actor.transform { it.copy(flashWith = color) }
    }

    override fun getDownsamplingKernel(): Flow<ReduceKernel> {
        return actor.mapState { it.downsamplingKernel }
    }

    override suspend fun putDownsamplingKernel(kernel: ReduceKernel) {
        actor.transform { it.copy(downsamplingKernel = kernel) }
    }

    override fun getLinearLightDownsampling(): Flow<Boolean> {
        return actor.mapState { it.linearLightDownsampling }
    }

    override suspend fun putLinearLightDownsampling(linear: Boolean) {
        actor.transform { it.copy(linearLightDownsampling = linear) }
    }

    override fun getUpsamplingMode(): Flow<UpsamplingMode> {
        return actor.mapState { it.upsamplingMode }
    }

    override suspend fun putUpsamplingMode(mode: UpsamplingMode) {
        actor.transform { it.copy(upsamplingMode = mode) }
    }

    override fun getLoadThumbnailPreviews(): Flow<Boolean> {
        return actor.mapState { it.loadThumbnailPreviews }
    }

    override suspend fun putLoadThumbnailPreviews(load: Boolean) {
        actor.transform { it.copy(loadThumbnailPreviews = load) }
    }

    override fun getOnnxRuntimeMode(): Flow<OnnxRuntimeUpscaleMode> {
        return actor.mapState { it.onnxRuntimeMode }
    }

    override suspend fun putOnnxRuntimeMode(mode: OnnxRuntimeUpscaleMode) {
        actor.transform { it.copy(onnxRuntimeMode = mode) }
    }

    override fun getOnnxRuntimeDeviceId(): Flow<Int> {
        return actor.mapState { it.onnxRuntimeDeviceId }
    }

    override suspend fun putOnnxRuntimeDeviceId(deviceId: Int) {
        actor.transform { it.copy(onnxRuntimeDeviceId = deviceId) }
    }

    override fun getOnnxRuntimeTileSize(): Flow<Int> {
        return actor.mapState { it.onnxRuntimeTileSize }
    }

    override suspend fun putOnnxRuntimeTileSize(tileSize: Int) {
        actor.transform { it.copy(onnxRuntimeTileSize = tileSize) }
    }

    override fun getSelectedOnnxModel(): Flow<String?> {
        return actor.mapState { it.onnxRuntimeModelPath }
    }

    override suspend fun putSelectedOnnxModel(name: String?) {
        actor.transform { it.copy(onnxRuntimeModelPath = name) }
    }
}
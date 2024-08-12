package io.github.snd_r.komelia.settings

import com.akuleshov7.ktoml.annotations.TomlLiteral
import io.github.snd_r.komelia.platform.PlatformDecoderType
import io.github.snd_r.komelia.platform.PlatformDecoderType.VIPS
import io.github.snd_r.komelia.platform.skiaSamplerMitchell
import io.github.snd_r.komelia.platform.vipsDownscaleLanczos
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class AppSettings(
    val server: ServerSettings = ServerSettings(),
    val user: UserSettings = UserSettings(),
    val appearance: AppearanceSettings = AppearanceSettings(),
    val reader: ReaderBaseSettings = ReaderBaseSettings(),
    val decoder: DecoderSettings = DecoderSettings(),
    val updates: UpdateSettings = UpdateSettings(),
    val komf: KomfSettings = KomfSettings()
)

@Serializable
data class ServerSettings(
    val url: String = "http://localhost:25600"
)

@Serializable
data class UserSettings(
    val username: String = "admin@example.org"
)

@Serializable
data class AppearanceSettings(
    val cardWidth: Int = 200,
    val seriesPageLoadSize: Int = 20,
    val bookPageLoadSize: Int = 20,
    val bookListLayout: BooksLayout = BooksLayout.GRID,
    val appTheme: AppTheme = AppTheme.DARK
)

@Serializable
data class ReaderBaseSettings(
    val readerType: ReaderType = ReaderType.PAGED,
    val upsample: Boolean = false,
    val stretchToFit: Boolean = true,
    val pagedReaderSettings: PagedReaderSettings = PagedReaderSettings(),
    val continuousReaderSettings: ContinuousReaderSettings = ContinuousReaderSettings(),
)

@Serializable
data class PagedReaderSettings(
    val scaleType: LayoutScaleType = LayoutScaleType.SCREEN,
    val stretchToFit: Boolean = true,
    val readingDirection: PagedReaderState.ReadingDirection = PagedReaderState.ReadingDirection.LEFT_TO_RIGHT,
    val pageLayout: PageDisplayLayout = SINGLE_PAGE
)

@Serializable
data class ContinuousReaderSettings(
    val readingDirection: ContinuousReaderState.ReadingDirection = ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM,
    val padding: Float = .3f,
    val pageSpacing: Int = 0,
    val stretchToFit: Boolean = true,
)

@Serializable
data class DecoderSettings(
    val decoder: PlatformDecoderType = VIPS,
    val upscaleOption: String = skiaSamplerMitchell.value,
    val downscaleOption: String = vipsDownscaleLanczos.value,
    @TomlLiteral
    val onnxModelsPath: String = System.getProperty("user.home") ?: "/",
    val onnxRutnimeDeviceId: Int = 0,
    val onnxRuntimeTileSize: Int = 512,
)

@Serializable
data class UpdateSettings(
    val checkForUpdatesOnStartup: Boolean = true,
    @Serializable(with = InstantEpochMillisSerializer::class)
    val lastUpdateCheckTimestamp: Instant? = null,
    val lastCheckedReleaseVersion: AppVersion? = null,
    val dismissedVersion: AppVersion? = null,
)

@Serializable
data class KomfSettings(
    val enabled: Boolean = false,
    val mode: KomfMode = KomfMode.REMOTE,
    val remoteUrl: String = "http://localhost:8085",
)

object InstantEpochMillisSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("kotlinx.datetime.Instant", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Instant =
        Instant.fromEpochMilliseconds(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: Instant) =
        encoder.encodeLong(value.toEpochMilliseconds())
}

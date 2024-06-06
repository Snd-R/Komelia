package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

const val defaultCardWidth = 240

@Serializable
data class AppSettings(
    val server: ServerSettings = ServerSettings(),
    val user: UserSettings = UserSettings(),
    val appearance: AppearanceSettings = AppearanceSettings(),
    val reader: ReaderBaseSettings = ReaderBaseSettings(),
    val decoder: DecoderSettings = DecoderSettings(),
    val updates: UpdateSettings = UpdateSettings()
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
    val cardWidth: Int = defaultCardWidth,
    val seriesPageLoadSize: Int = 20,
    val bookPageLoadSize: Int = 20,
    val bookListLayout: BooksLayout = BooksLayout.GRID
)

@Serializable
data class ReaderBaseSettings(
    val readerType: ReaderType = ReaderType.PAGED,
    val upsample: Boolean = false,
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
    val pageSpacing: Int = 0
)

@Serializable
data class DecoderSettings(
    val type: SamplerType = SamplerType.VIPS_LANCZOS_DOWN_BICUBIC_UP
)

@Serializable
data class UpdateSettings(
    val checkForUpdatesOnStartup: Boolean = true,
    @Serializable(with = InstantEpochMillisSerializer::class)
    val lastUpdateCheckTimestamp: Instant? = null,
    val lastCheckedReleaseVersion: AppVersion? = null,
    val dismissedVersion: AppVersion? = null,
)

object InstantEpochMillisSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("kotlinx.datetime.Instant", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Instant =
        Instant.fromEpochMilliseconds(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: Instant) =
        encoder.encodeLong(value.toEpochMilliseconds())
}

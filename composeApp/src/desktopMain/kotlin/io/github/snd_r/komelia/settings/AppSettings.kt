package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.image.SamplerType
import io.github.snd_r.komelia.ui.reader.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import io.github.snd_r.komelia.ui.reader.ReadingDirection.LEFT_TO_RIGHT
import kotlinx.serialization.Serializable

const val defaultCardWidth = 240

@Serializable
data class AppSettings(
    val server: ServerSettings = ServerSettings(),
    val user: UserSettings = UserSettings(),
    val appearance: AppearanceSettings = AppearanceSettings(),
    val reader: ReaderSettings = ReaderSettings(),
    val decoder: DecoderSettings = DecoderSettings()
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
    val cardWidth: Int = defaultCardWidth
)

@Serializable
data class ReaderSettings(
    val scaleType: LayoutScaleType = LayoutScaleType.SCREEN,
    val upsample: Boolean = false,
    val readingDirection: ReadingDirection = LEFT_TO_RIGHT,
    val pageLayout: PageDisplayLayout = SINGLE_PAGE
)

@Serializable
data class DecoderSettings(
    val type: SamplerType = SamplerType.VIPS_LANCZOS_DOWN_BICUBIC_UP
)
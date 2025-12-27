package snd.komelia.settings.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import snd.komelia.settings.model.TtsuBlurMode.AFTER_TOC
import snd.komelia.settings.model.TtsuBlurMode.TtuBlurModeSerializer
import snd.komelia.settings.model.TtsuFuriganaStyle.Partial
import snd.komelia.settings.model.TtsuFuriganaStyle.TtuFuriganaStyleSerializer
import snd.komelia.settings.model.TtsuViewMode.TtuViewModeSerializer
import snd.komelia.settings.model.TtsuWritingMode.HORIZONTAL_TB
import snd.komelia.settings.model.TtsuWritingMode.TtuWritingModeSerializer
import snd.komelia.settings.model.TtuTheme.TtuThemeSerializer

@Serializable
data class TtsuReaderSettings(
    val theme: String = "light-theme",
    val customThemes: Map<String, TtsuThemeOption> = emptyMap(),
    val multiplier: Int = 20,
    val serifFontFamily: TtsuFont = TtsuFont(
        displayName = "Noto Sans CJK JP",
        familyName = "Noto Sans CJK JP"
    ),
    val sansFontFamily: TtsuFont = TtsuFont(
        displayName = "Noto Sans CJK JP",
        familyName = "Noto Sans CJK JP"
    ),
    val fontSize: Int = 20,
    val lineHeight: Float = 1.65f,
    val hideSpoilerImage: Boolean = true,
    val hideSpoilerImageMode: TtsuBlurMode = AFTER_TOC,
    val hideFurigana: Boolean = false,
    val furiganaStyle: TtsuFuriganaStyle = Partial,
    val writingMode: TtsuWritingMode = HORIZONTAL_TB,
    val enableReaderWakeLock: Boolean = false,
    val showCharacterCounter: Boolean = true,
    val viewMode: TtsuViewMode = TtsuViewMode.Continuous,
    val secondDimensionMaxValue: Int? = 0,
    val firstDimensionMargin: Int? = 0,
    val swipeThreshold: Int = 10,
    val disableWheelNavigation: Boolean = false,
    val autoPositionOnResize: Boolean = true,
    val avoidPageBreak: Boolean = false,
    val customReadingPointEnabled: Boolean = false,
    val selectionToBookmarkEnabled: Boolean = false,
    val confirmClose: Boolean = false,
    val manualBookmark: Boolean = false,
    val autoBookmark: Boolean = true,
    val autoBookmarkTime: Int = 3,
    val pageColumns: Int = 0,
    val verticalCustomReadingPosition: Int = 100,
    val horizontalCustomReadingPosition: Int = 0,
    val userFonts: List<TtsuUserFont> = emptyList(),
)

@Serializable
data class TtsuThemeOption(
    val fontColor: String,
    val backgroundColor: String,
    val selectionFontColor: String,
    val selectionBackgroundColor: String,
    val hintFuriganaShadowColor: String,
    val hintFuriganaFontColor: String,
    val tooltipTextFontColor: String,
)

@Serializable
data class TtsuFont(
    val displayName: String,
    val familyName: String,
)

@Serializable
data class TtsuUserFont(
    val displayName: String,
    val familyName: String,
    val path: String,
    val fileName: String,
)

@Serializable(with = TtuBlurModeSerializer::class)
enum class TtsuBlurMode(val value: String) {
    ALL("all"),
    AFTER_TOC("afterToc");

    class TtuBlurModeSerializer() : KSerializer<TtsuBlurMode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TtuBlurMode", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): TtsuBlurMode {
            val value = decoder.decodeString()
            return entries.first { it.value == value }
        }

        override fun serialize(encoder: Encoder, value: TtsuBlurMode) = encoder.encodeString(value.value)
    }
}

@Serializable(with = TtuFuriganaStyleSerializer::class)
enum class TtsuFuriganaStyle(val value: String) {
    Hide("hide"),
    Partial("partial"),
    Toggle("toggle"),
    Full("full");

    class TtuFuriganaStyleSerializer : KSerializer<TtsuFuriganaStyle> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TtuFuriganaStyle", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): TtsuFuriganaStyle {
            val value = decoder.decodeString()
            return entries.first { it.value == value }
        }

        override fun serialize(encoder: Encoder, value: TtsuFuriganaStyle) = encoder.encodeString(value.value)
    }
}

@Serializable(with = TtuThemeSerializer::class)
enum class TtuTheme(val value: String) {
    LIGHT("light-theme"),
    DARK("dark-theme");

    class TtuThemeSerializer() : KSerializer<TtuTheme> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TtuTheme", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): TtuTheme {
            val value = decoder.decodeString()
            return entries.first { it.value == value }
        }

        override fun serialize(encoder: Encoder, value: TtuTheme) = encoder.encodeString(value.value)
    }
}

@Serializable(with = TtuWritingModeSerializer::class)
enum class TtsuWritingMode(val value: String) {
    HORIZONTAL_TB("horizontal-tb"),
    VERTICAL_RL("vertical-rl");

    class TtuWritingModeSerializer : KSerializer<TtsuWritingMode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TtuWritingMode", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): TtsuWritingMode {
            val value = decoder.decodeString()
            return entries.first { it.value == value }
        }

        override fun serialize(encoder: Encoder, value: TtsuWritingMode) = encoder.encodeString(value.value)
    }
}

@Serializable(with = TtuViewModeSerializer::class)
enum class TtsuViewMode(val value: String) {
    Continuous("continuous"),
    Paginated("paginated");

    class TtuViewModeSerializer : KSerializer<TtsuViewMode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TtuViewMode", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): TtsuViewMode {
            val value = decoder.decodeString()
            return entries.first { it.value == value }
        }

        override fun serialize(encoder: Encoder, value: TtsuViewMode) = encoder.encodeString(value.value)
    }
}

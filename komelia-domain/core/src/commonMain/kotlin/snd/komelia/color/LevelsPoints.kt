package snd.komelia.color

import kotlinx.serialization.Serializable
import snd.komga.client.book.KomgaBookId


@Serializable
data class BookColorLevels(
    val bookId: KomgaBookId,
    val channels: ColorLevelChannels
)

@Serializable
data class ColorLevelChannels(
    val color: ColorLevelsConfig,
    val red: ColorLevelsConfig,
    val green: ColorLevelsConfig,
    val blue: ColorLevelsConfig,
) {
    companion object {
        val DEFAULT = ColorLevelChannels(
            ColorLevelsConfig.DEFAULT,
            ColorLevelsConfig.DEFAULT,
            ColorLevelsConfig.DEFAULT,
            ColorLevelsConfig.DEFAULT,
        )
    }
}

@Serializable
data class ColorLevelsConfig(
    val lowInput: Float,
    val highInput: Float,
    val lowOutput: Float,
    val highOutput: Float,
    val gamma: Float,
) {

    companion object {
        val DEFAULT = ColorLevelsConfig(
            lowInput = 0f,
            highInput = 1f,
            lowOutput = 0f,
            highOutput = 1f,
            gamma = 1f,
        )
    }
}

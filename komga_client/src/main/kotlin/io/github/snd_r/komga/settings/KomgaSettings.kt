package io.github.snd_r.komga.settings

import kotlinx.serialization.Serializable

@Serializable
data class KomgaSettings (
    val deleteEmptyCollections: Boolean,
    val deleteEmptyReadLists: Boolean,
    val rememberMeDurationDays: Int,
    val thumbnailSize: KomgaThumbnailSize,
    val taskPoolSize: Int,
    val serverPort: SettingMultiSource<Int?>,
    val serverContextPath: SettingMultiSource<String?>,
)

@Serializable
data class SettingMultiSource<T>(
    val configurationSource: T,
    val databaseSource: T,
    val effectiveValue: T,
)

enum class KomgaThumbnailSize {
    DEFAULT,
    MEDIUM,
    LARGE,
    XLARGE,
}

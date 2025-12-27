package snd.komelia.offline.api

import snd.komelia.komga.api.KomgaSettingsApi
import snd.komga.client.settings.KomgaSettings
import snd.komga.client.settings.KomgaSettingsUpdateRequest
import snd.komga.client.settings.KomgaThumbnailSize
import snd.komga.client.settings.SettingMultiSource

class OfflineSettingsApi : KomgaSettingsApi {
    override suspend fun getSettings(): KomgaSettings = KomgaSettings(
        deleteEmptyCollections = false,
        deleteEmptyReadLists = false,
        rememberMeDurationDays = 0,
        thumbnailSize = KomgaThumbnailSize.DEFAULT,
        taskPoolSize = 0,
        serverPort = SettingMultiSource(
            configurationSource = 0,
            databaseSource = 0,
            effectiveValue = 0
        ),
        serverContextPath = SettingMultiSource(
            configurationSource = "",
            databaseSource = "",
            effectiveValue = ""
        ),
    )

    override suspend fun updateSettings(request: KomgaSettingsUpdateRequest) = Unit
}
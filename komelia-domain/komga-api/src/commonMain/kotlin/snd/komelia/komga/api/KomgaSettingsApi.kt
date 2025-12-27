package snd.komelia.komga.api

import snd.komga.client.settings.KomgaSettings
import snd.komga.client.settings.KomgaSettingsUpdateRequest

interface KomgaSettingsApi {
    suspend fun getSettings(): KomgaSettings
    suspend fun updateSettings(request: KomgaSettingsUpdateRequest)
}
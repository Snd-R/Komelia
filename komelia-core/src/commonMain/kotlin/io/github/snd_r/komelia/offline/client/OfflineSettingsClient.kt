package io.github.snd_r.komelia.offline.client

import snd.komga.client.settings.KomgaSettings
import snd.komga.client.settings.KomgaSettingsClient
import snd.komga.client.settings.KomgaSettingsUpdateRequest

class OfflineSettingsClient : KomgaSettingsClient {
    override suspend fun getSettings(): KomgaSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateSettings(request: KomgaSettingsUpdateRequest) {
        TODO("Not yet implemented")
    }
}
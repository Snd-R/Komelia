package io.github.snd_r.komelia.settings

import kotlinx.coroutines.flow.Flow

interface KomfSettingsRepository {
    fun getKomfEnabled(): Flow<Boolean>
    suspend fun putKomfEnabled(enabled: Boolean)

    fun getKomfUrl(): Flow<String>
    suspend fun putKomfUrl(url: String)
}
package snd.komelia.settings

import androidx.datastore.core.DataStore
import io.github.snd_r.komelia.settings.AppSettings
import io.github.snd_r.komelia.settings.copy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class AndroidSecretsRepository(
    private val dataStore: DataStore<AppSettings>
) : SecretsRepository {
    override suspend fun getCookie(url: String): String? {
        return dataStore.data
            .map { it.user.rememberMe.ifBlank { null } }
            .firstOrNull()
    }

    override suspend fun setCookie(url: String, cookie: String) {
        dataStore.updateData {
            it.copy { user = user.copy { rememberMe = cookie } }
        }
    }

    override suspend fun deleteCookie(url: String) {
        dataStore.updateData {
            it.copy { user = user.copy { rememberMe = "" } }
        }
    }
}
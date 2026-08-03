package snd.komelia.settings

import android.content.SharedPreferences
import androidx.core.content.edit

private const val cookieKey = "cookieKey"

class AndroidSecretsRepository(
    private val preferences: SharedPreferences
) : SecretsRepository {
    override suspend fun getCookie(url: String): String? {
        return preferences.getString(cookieKey, null)
    }

    override suspend fun setCookie(url: String, cookie: String) {
        preferences.edit { putString(cookieKey, cookie) }
    }

    override suspend fun deleteCookie(url: String) {
        preferences.edit { remove(cookieKey) }
    }
}
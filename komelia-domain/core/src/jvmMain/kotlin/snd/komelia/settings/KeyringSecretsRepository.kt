package snd.komelia.settings

import com.github.javakeyring.PasswordAccessException
import snd.komelia.secrets.AppKeyring

private const val KEYRING_SERVICE_NAME = "komelia"

class KeyringSecretsRepository(private val keyring: AppKeyring) : SecretsRepository {

    override suspend fun getCookie(url: String): String? {
        return try {
            keyring.getPassword(KEYRING_SERVICE_NAME, url)
        } catch (e: PasswordAccessException) {
            null
        }
    }

    override suspend fun setCookie(url: String, cookie: String) {
        keyring.setPassword(KEYRING_SERVICE_NAME, url, cookie)
    }

    override suspend fun deleteCookie(url: String) {
        keyring.deletePassword(KEYRING_SERVICE_NAME, url)
    }

}
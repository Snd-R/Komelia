package io.github.snd_r.komelia.settings

import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException
import io.github.oshai.kotlinlogging.KotlinLogging

private const val KEYRING_SERVICE_NAME = "komelia"

private val logger = KotlinLogging.logger {}

class KeyringSecretsRepository : SecretsRepository {
    private val keyring: Keyring = createKeyring()

    private fun createKeyring(): Keyring {
        return try {
            Keyring.create()
        } catch (e: Exception) {
            logger.error(e) {}
            Thread.sleep(100)
            Keyring.create()
        }
    }

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
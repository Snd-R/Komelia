package io.github.snd_r.komelia.secrets

import com.github.javakeyring.PasswordAccessException
import com.github.javakeyring.internal.KeyringBackendFactory
import com.github.javakeyring.internal.windows.WinCredentialStoreBackend
import io.github.snd_r.komelia.DesktopPlatform
import io.github.snd_r.komelia.DesktopPlatform.Linux
import io.github.snd_r.komelia.DesktopPlatform.Windows

class AppKeyring {

    private val backend = when (DesktopPlatform.Current) {
        Linux -> runCatching { LinuxSecretService() }
            .onFailure { it.printStackTrace() }
            .getOrNull() ?: KeyringBackendFactory.create()

        Windows -> WinCredentialStoreBackend()
        else -> KeyringBackendFactory.create()
    }

    fun getPassword(service: String, account: String): String? {
        return try {
            val password: String? = backend.getPassword(service, account)
            password
        } catch (e: PasswordAccessException) {
            null
        }
    }

    fun setPassword(service: String, account: String, password: String) {
        backend.setPassword(service, account, password)
    }

    fun deletePassword(service: String, account: String) {
        backend.deletePassword(service, account)
    }

    fun close() {
        backend.close()
    }
}
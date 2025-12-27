package snd.komelia.secrets

import com.github.javakeyring.internal.KeyringBackend
import de.swiesend.secretservice.simple.SimpleCollection

class LinuxSecretService : KeyringBackend {

    private val collection: SimpleCollection = SimpleCollection()

    override fun getPassword(service: String, account: String): String? {
        return collection.getItems(attributesFor(service, account))
            ?.firstOrNull()
            ?.let { path ->
                collection.getSecret(path)?.let {
                    if (it.isEmpty()) null
                    else String(it)
                }
            }
    }

    override fun setPassword(service: String, account: String, password: String) {
        val attributes = attributesFor(service, account)
        val objectPaths: List<String>? = collection.getItems(attributes)
        val label = "$service|$account"
        if (objectPaths == null) {
            collection.createItem(label, password, attributes)
        } else {
            collection.updateItem(objectPaths.first(), label, password, attributes)
        }
    }

    override fun deletePassword(service: String, account: String) {
        collection.getItems(attributesFor(service, account))?.let {
            collection.deleteItems(it)
        }
    }

    private fun attributesFor(service: String, account: String) = mapOf("service" to service, "account" to account)

    override fun close() {
        collection.close()
    }
}
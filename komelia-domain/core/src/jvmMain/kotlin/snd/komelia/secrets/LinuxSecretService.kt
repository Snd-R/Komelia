package snd.komelia.secrets

import com.github.javakeyring.internal.KeyringBackend
import de.swiesend.secretservice.functional.Collection
import de.swiesend.secretservice.functional.interfaces.CollectionInterface
import kotlin.jvm.optionals.getOrNull

class LinuxSecretService : KeyringBackend {

    private val collection: CollectionInterface = Collection.openDefault()
        .orElseThrow { RuntimeException("Failed to connect to secret service.") }

    override fun getPassword(service: String, account: String): String? {

        return collection.getItems(attributesFor(service, account))
            .getOrNull()?.firstOrNull()
            ?.let { path ->
                collection.getSecret(path).getOrNull()?.let {
                    if (it.isEmpty()) null
                    else String(it)
                }
            }
    }

    override fun setPassword(service: String, account: String, password: String) {
        val attributes = attributesFor(service, account)
        val label = "$service|$account"

        val objectPaths: List<String>? = collection.getItems(attributes).getOrNull()

        if (objectPaths.isNullOrEmpty()) {
            collection.createItem(label, password, attributes)
        } else {
            collection.updateItem(objectPaths.first(), label, password, attributes)
        }
    }

    override fun deletePassword(service: String, account: String) {
        collection.getItems(attributesFor(service, account)).getOrNull()?.let {
            collection.deleteItems(it)
        }
    }

    private fun attributesFor(service: String, account: String) = mapOf("service" to service, "account" to account)

    override fun close() {
        collection.close()
    }
}
